# Solución arquitectónica completa — Módulo Porcinos

**Autor:** Arquitectura backend (DDD, consistencia transaccional, invariantes).  
**Alcance:** Recría, stock de animales, ventas/faena, destete, madre-gestación, persistencia y BD.  
**Premisa:** Sistema en producción; cambios con migración de datos y sin parches.

---

# 1. Diagnóstico arquitectónico

## 1.1 Resumen ejecutivo

El módulo Porcinos tiene **una fuente de verdad difusa** para “animales en recría” y **dos vías de salida** (Faena vs VentaPorcino) que compiten por el mismo recurso (cantidad en recría) sin una política única. A eso se suman atributos de dominio no persistidos (Recria.origen), invariantes solo en aplicación y ausencia de restricciones de integridad en BD. El resultado es riesgo de **doble descuento**, **stock negativo o bloqueos erróneos** y **inconsistencias Madre–Gestación** que no se pueden detectar en base de datos.

## 1.2 Problemas críticos (raíz)

| # | Problema | Causa raíz |
|---|----------|------------|
| 1 | **Doble descuento en “animales disponibles”** | `cantidadAnimales` se actualiza en venta (stock actual), pero `calcularAnimalesDisponibles` además resta `ventasPrevias`. Tras la primera venta, la misma salida se cuenta dos veces. |
| 2 | **Dos caminos de salida (Faena vs VentaPorcino)** | Faena es entidad/servicio legacy; VentaPorcino tipo FAENA es el camino unificado. FaenaService no actualiza `cantidadAnimales`; VentaPorcinoService sí. Quien use Faena deja la recría con cantidad inflada; quien use VentaPorcino y luego calcule disponibles con ventasPrevias obtiene doble descuento. |
| 3 | **Recria.origen no persistido** | Campo `origen` en entidad marcado `@Transient`; columna existe en BD (V1_120). El valor nunca se escribe ni se lee de BD. |
| 4 | **Un destete por parto no garantizado** | Regla solo en DesteteService; tabla `porcinos_destetes` sin UNIQUE(parto_id). Inserción directa o otro flujo puede crear segundo destete → doble recría. |
| 5 | **Madre en GESTACION sin gestación activa** | Cierre/borrado de gestación no siempre actualiza madre en la misma transacción; no hay restricción en BD. Estado y realidad pueden divergir. |

## 1.3 Violaciones DDD y riesgos transaccionales

- **Agregado Recría mal delimitado:** Las salidas (venta/faena) y las muertes modifican o consultan el mismo recurso (cantidad/disponibles) pero la lógica está repartida entre VentaPorcinoService, FaenaService y MuerteRecriaService, con fórmulas distintas. No hay un único “aggregate root” que proteja el invariante “disponibles ≥ 0 y coherente con eventos”.
- **Lógica de dominio en servicios de aplicación:** “Qué es cantidadAnimales” y “cómo se calcula disponibilidad” deberían ser responsabilidad del agregado Recría (o de un dominio service bajo Recría), no de varios application services con implementaciones duplicadas/inconsistentes.
- **Transacciones:** Actualizar Recria y crear VentaPorcino en la misma transacción está bien, pero no hay bloqueo (pessimistic lock o versión optimista), por lo que dos ventas concurrentes pueden leer el mismo stock y ambas restar → stock negativo o venta en exceso.
- **Invariantes solo en aplicación:** Reglas como “un destete por parto” y “una gestación activa por madre” no están respaldadas por constraints en BD; un acceso directo o un bug puede romperlas.

---

# 2. Modelo de dominio corregido

## 2.1 Decisión: significado de cantidadAnimales y disponibilidad

**Decisión firme:**

- **`Recria.cantidadAnimales`** representa la **cantidad actual de cabezas en el lote** en el momento del último evento que la modifica.
  - **Eventos que la modifican:** (1) Alta de recría (valor inicial); (2) Venta o faena (resta); (3) Movimiento de etapa (resta en origen, suma en destino).
  - **Eventos que no la modifican:** Muerte en recría. La muerte se registra como evento (MuerteRecria) pero no se resta de `cantidadAnimales`.

**Fórmula oficial de disponibilidad (única en todo el sistema):**

```text
disponibles(recria) = recria.cantidadAnimales - Σ(muerteRecria.cantidad WHERE recria_id = recria.id AND activo = 1)
```

**Consecuencias:**

- Las ventas/faenas **ya están reflejadas** en `cantidadAnimales`. No se debe restar “ventas previas” en ningún cálculo de disponibles.
- Las muertes **no** se restan de `cantidadAnimales`; solo se restan en la fórmula de disponibles. Así se mantiene trazabilidad (cada muerte es un registro) y un solo campo que se actualiza en salidas/movimientos.
- **Un único lugar** donde se calcula disponibilidad: un **domain service** o método del agregado Recría (por ejemplo `Recria.obtenerAnimalesDisponibles(MuerteRecriaRepository)` o un `RecriaStockService` en dominio que reciba la recría y el repositorio de muertes). Todos los application services (venta, muerte, movimiento, cierre) **solo** llaman a ese lugar y no implementan su propia fórmula.

## 2.2 Decisión: Faena vs VentaPorcino

**Decisión firme: Faena se elimina como camino de escritura. Toda salida de animales (venta en pie, reproductor, faena) es un `VentaPorcino`.**

- **Justificación:** Ya existe migración V1_109 que unificó faena en `porcinos_ventas_porcinos` (tipo FAENA) y migró datos desde `porcinos_faena`. Mantener dos entidades (Faena y VentaPorcino) y dos servicios (FaenaService y VentaPorcinoService) rompe el principio de una sola fuente de verdad para “salidas que descontan stock”. Faena es conceptualmente un **tipo de venta** (venta a faena/matadero), no un agregado distinto.
- **Acciones:**
  - **Eliminar** FaenaService.registrarFaena y FaenaController como vía de registro de nuevas faenas. Redirigir toda la UI/API de “registrar faena” a VentaPorcinoController con tipo FAENA.
  - **Mantener** la entidad Faena y la tabla `porcinos_faena` en solo lectura para historial (reportes que lean datos ya migrados o que aún apunten a la tabla antigua). Opcional: vista o query que unifique ventas + faenas históricas desde ambas tablas hasta que se decida deprecar la lectura de `porcinos_faena`.
  - **No** añadir nuevas escrituras en `porcinos_faena`. Si en producción aún se llama a FaenaController, ese endpoint debe devolver 410 Gone o redirigir al cliente a usar POST /api/v1/porcinos/ventas con tipo FAENA.

## 2.3 Recria.origen — persistencia y enum

**Decisión firme: `origen` es un atributo persistido de dominio y debe estar en BD y en la entidad.**

- **Valores:** `DESTETE` (recría generada desde destete interno), `EXTERNO` (compra/ingreso manual).
- **Cambios en entidad:** Quitar `@Transient`. Mapear con `@Enumerated(EnumType.STRING)` y `@Column(name = "origen", length = 20)`. El enum `OrigenRecria` ya existe en la entidad; debe quedar como atributo persistido.
- **Quién setea origen:** (1) DesteteService.crearRecriaDesdeDestete → `DESTETE`. (2) RecriaService.crearRecria (alta manual) → `EXTERNO` (o el que envíe el cliente si se expone en API; por defecto EXTERNO).
- **Migración de datos:** Script que actualice `porcinos_recria SET origen = 'EXTERNO'` donde `origen IS NULL` (o heurística: si existe destete con misma fecha y lote coherente, poner DESTETE; si no, EXTERNO). Opcional: dejar NULL para filas muy antiguas y documentar “NULL = histórico, tratar como EXTERNO en reportes”.

## 2.4 Un destete por parto

**Decisión firme: por cada parto existe como máximo un destete activo. Debe ser garantizado por BD.**

- **Restricción:** `UNIQUE(parto_id)` en `porcinos_destetes`. Si el modelo usa soft delete, la restricción debe considerar solo filas activas. En MySQL, si no se usa filtro por activo en la tabla, UNIQUE(parto_id) puede permitir un solo destete por parto en la vida (si se “anula” un destete marcando activo=0, no se podría crear otro por el mismo parto; si el negocio lo permite, entonces UNIQUE(parto_id) donde activo=1 requeriría índice parcial o trigger).
- **Recomendación simple:** `UNIQUE(parto_id)` en `porcinos_destetes`. Un parto tiene a lo sumo un destete; si se anula, no se crea otro destete para ese parto (o se documenta como limitación).
- **Limpieza previa:** Antes de añadir el UNIQUE, ejecutar consulta que detecte parto_id duplicados; para cada parto con más de un destete, dejar uno (por ejemplo el más reciente por id o por fecha_destete) y marcar los otros como inactivos o moverlos a tabla de auditoría si se requiere historial.

## 2.5 Invariantes Madre ↔ Gestación

**Decisiones firmes:**

- **Invariante 1:** Si `Madre.estadoActual == GESTACION` entonces existe exactamente una gestación activa (estado EN_CURSO, activo=1) para esa madre.
- **Invariante 2:** Cualquier operación que cierre o desactive una gestación (ABORTO, FINALIZADA, activo=false) debe ejecutarse **en la misma transacción** que la actualización de la madre a estado ADULTA.
- **Centralización:** Un único punto que “cierra gestación y actualiza madre”: por ejemplo `MadreService.cerrarGestacionYActualizarMadre(Gestacion g, LocalDate fecha, String motivo)` o que GestacionService y ServicioService **solo** llamen a MadreService para cambiar estado de madre (ya parcialmente hecho con actualizarEstadoMadre). Al registrar parto, la gestación se marca FINALIZADA y la madre pasa a LACTANCIA; no debe quedar gestación EN_CURSO ni madre en GESTACION sin gestación activa.
- **Detección de incoherencia:** Job o endpoint de diagnóstico que liste madres con estado_actual = 'GESTACION' para las que no exista fila en porcinos_gestacion con estado='EN_CURSO' y activo=1; para esas, proponer corrección (poner madre en ADULTA) o alertar.

**Parto → Gestación:** Añadir `Parto.gestacion_id` (FK a porcinos_gestacion, nullable al inicio). En PartoService.registrarParto setear gestacion_id con la gestación que se usó en la validación. Así se formaliza “todo parto proviene de una gestación” y se evita heurística por madre+fecha.

---

# 3. Invariantes formales y dónde se validan

## 3.1 Recría

| Invariante | Dónde se garantiza |
|------------|--------------------|
| `cantidadAnimales >= 0` | Al restar en venta o movimiento: si `cantidadAnimales - cantidad < 0` → fallar. Check constraint en BD: `cantidad_animales >= 0`. |
| `disponibles(recria) = cantidadAnimales - Σ(muertes)` | Un único método/componente (domain service o Recria) que reciba recría + muertes y devuelva el número; usado por VentaPorcinoService, MuerteRecriaService, MovimientoEtapaService, RecriaService. |
| No vender/faenar/mover más que `disponibles` | Antes de actualizar cantidadAnimales y persistir venta/movimiento: calcular disponibles con la fórmula oficial; si `cantidadSolicitada > disponibles` → lanzar excepción de dominio. |
| Al registrar muerte: `cantidadSolicitada <= disponibles` | MuerteRecriaService: calcular disponibles con la fórmula oficial (sin restar ventas); validar; no modificar cantidadAnimales. |
| Cierre de recría solo si `disponibles == 0` | RecriaService.cerrarRecria: calcular disponibles; si > 0 → lanzar excepción. |

## 3.2 VentaPorcino (único camino de salida)

| Invariante | Dónde se garantiza |
|------------|--------------------|
| Toda salida (venta en pie, reproductor, faena) es un registro en `porcinos_ventas_porcinos`. | Eliminación de FaenaService como vía de escritura; redirección a VentaPorcinoService. |
| Al registrar venta/faena: actualizar Recria.cantidadAnimales y insertar VentaPorcino en la misma transacción. | VentaPorcinoService.registrarVenta: dentro de @Transactional, calcular disponibles (vía dominio), validar, recria.setCantidadAnimales(cantidad - cantidadVendida), save(recria), save(venta). |
| Bloqueo o versión para evitar doble venta. | Recria con @Version o SELECT FOR UPDATE en la transacción de venta (ver plan de refactor). |

## 3.3 Destete

| Invariante | Dónde se garantiza |
|------------|--------------------|
| Como máximo un destete activo por parto. | BD: UNIQUE(parto_id) en porcinos_destetes. Aplicación: DesteteService comprueba que no exista destete para ese parto antes de crear. |
| cantidad_destetados <= parto.nacidosVivos. | DesteteService antes de persistir. |
| Parto.fechaFin >= fechaDestete (o se setea al registrar destete). | DesteteService: si parto.fechaFin == null, setear parto.fechaFin = fechaDestete; si parto.fechaFin != null, validar parto.fechaFin >= fechaDestete. |

## 3.4 Madre y Gestación

| Invariante | Dónde se garantiza |
|------------|--------------------|
| Si madre.estadoActual == GESTACION entonces existe gestación activa para esa madre. | Aplicación: al cerrar/desactivar gestación, siempre actualizar madre a ADULTA en la misma transacción. Opcional: job de consistencia. |
| Transiciones de estado solo vía eventos definidos. | Centralizar en MadreService.actualizarEstadoMadre (y eliminar copia en ServicioService). Opcional: máquina de estados explícita que valide (estadoActual, evento) → nuevoEstado. |
| Parto tiene gestacion_id cuando se registra desde aplicación. | PartoService.registrarParto asigna gestacion_id con la gestación usada en la validación. |

---

# 4. Cambios en entidades

| Entidad | Cambio |
|---------|--------|
| **Recria** | (1) `origen`: quitar @Transient, añadir @Column(name = "origen", length = 20) y @Enumerated(EnumType.STRING). (2) Opcional: añadir @Version para concurrencia optimista. |
| **Parto** | Añadir `Long gestacionId` o `@ManyToOne Gestacion gestacion` y columna `gestacion_id` (nullable). |
| **Faena** | Sin cambios estructurales; entidad y tabla se mantienen en solo lectura para historial. No se elimina la clase para no romper lecturas existentes hasta que se migren reportes. |

---

# 5. Cambios en servicios

| Servicio | Cambio |
|----------|--------|
| **Cálculo de disponibles** | Extraer a un único componente: por ejemplo `RecriaDominioService.animalesDisponibles(Recria r)` que internamente use `r.getCantidadAnimales() - sumaMuertes(r)`. O método en Recria que reciba solo la suma de muertes. Inyectado en VentaPorcinoService, MuerteRecriaService, MovimientoEtapaService, RecriaService. |
| **VentaPorcinoService** | (1) Eliminar `ventasPrevias` de calcularAnimalesDisponibles; usar solo la fórmula oficial (cantidadAnimales - muertes). (2) O mejor: dejar de tener método privado calcularAnimalesDisponibles y usar el componente único de dominio. (3) Añadir bloqueo: RecriaRepository.findByIdWithLock(id) o Recria con @Version y actualizar con WHERE version = X. |
| **MuerteRecriaService** | (1) Eliminar ventasPrevias del cálculo; usar solo cantidadAnimales - Σ(muertes). (2) Usar el mismo componente de dominio para disponibles. (3) No modificar cantidadAnimales al registrar muerte. |
| **MovimientoEtapaService** | Usar el mismo componente de dominio para disponibles; no duplicar fórmula. |
| **RecriaService** | (1) Cierre de recría: usar el mismo componente de dominio para disponibles. (2) Ya no asumir “ventas descontaron”; la fórmula oficial unifica. |
| **FaenaService / FaenaController** | (1) No registrar nuevas faenas: FaenaController endpoints que crean faena devuelven 410 Gone o redirigen a documentación de uso de VentaPorcino con tipo FAENA. (2) FaenaService.registrarFaena puede lanzar UnsupportedOperationException con mensaje “Use VentaPorcinoService.registrarVenta con tipo FAENA”. (3) Lectura de faenas históricas (listar por recría, por empresa) puede seguir leyendo porcinos_faena para reportes; o unificar en un solo listado “ventas” que consulte solo porcinos_ventas_porcinos si ya no se usa la tabla faena. |
| **DesteteService** | (1) Validar parto.fechaFin >= fechaDestete si parto.fechaFin != null; si no, setear parto.fechaFin = fechaDestete. (2) Asegurar que Recria creada tenga origen = DESTETE y que se persista (entidad ya mapeada). |
| **GestacionService / ServicioService** | Cierre de gestación (ABORTO, FINALIZADA): en la misma transacción llamar a MadreService.actualizarEstadoMadre(madre, ADULTA, ...). Eliminar duplicado de actualizarEstadoMadre en ServicioService; usar solo MadreService. |
| **PartoService** | Al registrar parto, setear parto.setGestacion(gestacion) o parto.setGestacionId(gestacion.getId()) con la gestación que se usó en la validación. |

---

# 6. Restricciones de base de datos

## 6.1 Migraciones recomendadas (orden)

1. **Migración A — Limpieza destetes duplicados**  
   - Consulta: partos con más de un destete activo. Para cada uno, dejar un destete (ej. MAX(id)) y marcar los otros como activo=0 o borrarlos según política.  
   - Luego: `ALTER TABLE porcinos_destetes ADD CONSTRAINT uk_destetes_parto UNIQUE (parto_id);`  
   - Si la tabla tiene activo y se quiere permitir solo un destete “activo” por parto, en MySQL sin índices parciales se puede usar UNIQUE(parto_id) y aceptar que un parto solo pueda tener un destete en la vida (el primero insertado).

2. **Migración B — Recria.origen**  
   - La columna ya existe (V1_120). Solo asegurar que la entidad la mapee. Opcional: `UPDATE porcinos_recria SET origen = 'EXTERNO' WHERE origen IS NULL;` para datos existentes.

3. **Migración C — Parto.gestacion_id**  
   - `ALTER TABLE porcinos_partos ADD COLUMN gestacion_id BIGINT NULL REFERENCES porcinos_gestacion(id);`  
   - Índice: `CREATE INDEX idx_partos_gestacion ON porcinos_partos(gestacion_id);`  
   - Opcional: rellenar con UPDATE desde gestaciones finalizadas por madre y fecha.

4. **Migración D — Checks**  
   - `ALTER TABLE porcinos_recria ADD CONSTRAINT chk_recria_cantidad CHECK (cantidad_animales >= 0);`  
   - `ALTER TABLE porcinos_partos ADD CONSTRAINT chk_parto_totales CHECK (total_nacidos = nacidos_vivos + nacidos_muertos + momias AND total_nacidos > 0);`  
   - (Ajustar según nombres reales de columnas.)

5. **Migración E — Unique empresa + identificacion (madres/padrillos)**  
   - Si hoy existe UNIQUE(identificacion) sin empresa_id: crear UNIQUE(empresa_id, identificacion) y eliminar el unique solo sobre identificacion tras verificar que no hay duplicados inter-empresa.

## 6.2 No hacer (por riesgo o limitación de motor)

- UNIQUE parcial “una gestación activa por madre” en MySQL antiguo: no soportado. Dejar en aplicación + tests.
- UNIQUE parcial “un parto abierto por madre”: idem. Dejar en aplicación.

---

# 7. Plan de refactor en fases

## Fase 1 — Fuente única de verdad de disponibilidad (sin cambiar BD)

**Objetivo:** Un solo lugar que calcule disponibles; eliminar doble descuento; no tocar aún Faena en producción para no romper llamadas hasta tener redirección.

| Paso | Acción | Riesgo |
|------|--------|--------|
| 1.1 | Crear componente de dominio (ej. `RecriaStockService` en paquete domain/application porcinos) con método `int animalesDisponibles(Recria recria)` que devuelva `recria.getCantidadAnimales() - sumaMuertes(recria)`. La suma de muertes se obtiene vía MuerteRecriaRepository.findByRecriaAndActivoTrue. | Bajo |
| 1.2 | En VentaPorcinoService: inyectar ese componente; reemplazar calcularAnimalesDisponibles por llamada al componente; eliminar ventasPrevias. Mantener actualización de cantidadAnimales y guardado de VentaPorcino en la misma transacción. | Bajo |
| 1.3 | En MuerteRecriaService: usar el mismo componente para validar cantidadSolicitada <= animalesDisponibles(recria); eliminar ventasPrevias del cálculo. | Bajo |
| 1.4 | En MovimientoEtapaService y RecriaService (cierre): usar el mismo componente en lugar de su propia fórmula. | Bajo |
| 1.5 | Tests: venta parcial múltiple sobre la misma recría; cierre de recría con ventas y muertes; movimiento después de venta. | — |

**Entregable:** Cálculo de disponibles unificado; doble descuento eliminado. Sin cambios de esquema.

---

## Fase 2 — Persistencia de origen y restricciones BD

**Objetivo:** Recria.origen persistido; un destete por parto garantizado en BD; Parto.gestacion_id; checks básicos.

| Paso | Acción | Riesgo |
|------|--------|--------|
| 2.1 | Entidad Recria: mapear `origen` (quitar @Transient, @Column, @Enumerated). DesteteService y RecriaService ya setean origen; verificar que se persista. | Bajo |
| 2.2 | Migración: actualizar filas con origen NULL a 'EXTERNO' (o heurística DESTETE si hay datos cruzados). | Bajo |
| 2.3 | Migración: detectar destetes duplicados por parto_id; resolver (dejar uno, inactivar otros). Añadir UNIQUE(parto_id) en porcinos_destetes. | Medio (requiere revisión de datos) |
| 2.4 | Parto: añadir gestacion_id (nullable); migración ADD COLUMN. PartoService: setear gestacion_id al registrar parto. | Bajo |
| 2.5 | Migraciones: CHECK cantidad_animales >= 0 en recría; CHECK total_nacidos en parto. | Bajo |

**Entregable:** Origen trazable; integridad de destete y parto reforzada en BD.

---

## Fase 3 — Un solo camino de salida (Faena → VentaPorcino)

**Objetivo:** Ninguna escritura nueva en Faena; toda faena por VentaPorcinoService.

| Paso | Acción | Riesgo |
|------|--------|--------|
| 3.1 | Front/API: identificar todas las llamadas a “registrar faena” (FaenaController). Cambiarlas a POST /api/v1/porcinos/ventas con body tipo FAENA y mismos datos (recriaId, cantidad, peso, etc.). | Medio (coordinación front) |
| 3.2 | FaenaController: endpoints de creación (POST) devolver 410 Gone con mensaje “Use POST /api/v1/porcinos/ventas con tipo FAENA”. O redirigir con documentación. | Bajo |
| 3.3 | FaenaService.registrarFaena: lanzar UnsupportedOperationException con mensaje claro. | Bajo |
| 3.4 | Reportes que lean “faenas”: si hoy leen porcinos_faena, mantener lectura solo para datos históricos; nuevos reportes usar solo porcinos_ventas_porcinos WHERE tipo = 'FAENA'. | Bajo |

**Entregable:** Una sola vía de escritura para salidas; modelo conceptualmente unificado.

---

## Fase 4 — Invariantes Madre–Gestación y concurrencia

**Objetivo:** Cierre de gestación siempre actualiza madre; un solo lugar para actualizar estado; concurrencia en venta controlada.

| Paso | Acción | Riesgo |
|------|--------|--------|
| 4.1 | Revisar todos los puntos que ponen gestación en ABORTO o FINALIZADA o activo=false: GestacionService, ServicioService. Asegurar que en la misma transacción se llame a MadreService.actualizarEstadoMadre(madre, ADULTA, ...). | Bajo |
| 4.2 | Eliminar método privado actualizarEstadoMadre de ServicioService; inyectar MadreService y usar MadreService.actualizarEstadoMadre. | Bajo |
| 4.3 | Concurrencia en venta: añadir @Version en Recria o RecriaRepository.findByIdForUpdate(id) con SELECT FOR UPDATE. En VentaPorcinoService, dentro de la transacción, cargar recría con lock/versión, calcular disponibles, actualizar cantidad y version, guardar venta. | Medio (comportamiento bajo carga) |
| 4.4 | Opcional: job de consistencia que liste madres con estado GESTACION sin gestación activa y las corrija a ADULTA (o alerte). | Bajo |

**Entregable:** Invariantes Madre–Gestación protegidos; ventas concurrentes sin exceder stock.

---

## Fase 5 — Consolidación y limpieza

**Objetivo:** Documentación de invariantes; DIAS_CACHORRA por empresa; opcional máquina de estados Madre.

| Paso | Acción | Riesgo |
|------|--------|--------|
| 5.1 | Documento de dominio: “Invariantes Porcinos” con las fórmulas y reglas de esta solución. Ubicación en código donde se garantiza cada una. | Bajo |
| 5.2 | ConfiguracionPorcino / MadreService: DIAS_CACHORRA por empresa_id (no por usuario). | Bajo |
| 5.3 | Opcional: clase MadreEstadoMachine que reciba (estadoActual, evento) y devuelva nuevoEstado; todos los actualizarEstadoMadre pasan por ella. | Bajo |

**Entregable:** Dominio documentado y consistente; menos riesgo de regresiones.

---

# 8. Orden recomendado de implementación

1. **Fase 1** (disponibilidad y doble descuento) — primero, porque corrige el bug crítico sin tocar BD ni Faena.  
2. **Fase 2** (origen, destete único, gestacion_id, checks) — segundo, para reforzar integridad y trazabilidad.  
3. **Fase 3** (Faena → VentaPorcino) — tercero, una vez el único camino de escritura (VentaPorcino) tiene la fórmula correcta.  
4. **Fase 4** (Madre–Gestación y concurrencia) — cuarto.  
5. **Fase 5** (documentación y opcionales) — último.

---

# 9. Riesgos técnicos y mitigación

| Riesgo | Mitigación |
|--------|------------|
| Datos existentes con destetes duplicados | Script de análisis previo; decisión de qué destete conservar por parto; backup antes de UNIQUE. |
| Front sigue llamando a FaenaController | Fase 3: primero cambiar front a VentaPorcino; luego deshabilitar escritura en FaenaController. Si no se puede cambiar front de inmediato, FaenaController puede delegar internamente en VentaPorcinoService (crear VentaPorcino tipo FAENA) y dejar de escribir en porcinos_faena, manteniendo el mismo contrato de API temporalmente. |
| Concurrencia alta en ventas | Bloqueo pesimista (SELECT FOR UPDATE) puede aumentar contención; versión optimista puede generar reintentos. Elegir según carga; monitorear tiempos de respuesta. |
| Migración de origen NULL | Si hay muchas recrías sin origen, UPDATE por lotes para no bloquear tabla. |
| Parto sin gestacion_id en históricos | Columna nullable; nuevos partos tendrán valor; reportes que necesiten “parto → gestación” solo para datos recientes. |

---

# 10. Resumen de decisiones firmes

- **cantidadAnimales** = cantidad actual; se actualiza en venta y movimiento; no en muerte.  
- **disponibles** = cantidadAnimales − Σ(muertes); una sola fórmula y un solo componente que la implemente.  
- **Faena** deja de ser camino de escritura; toda salida es VentaPorcino (tipo ENGORDE, REPRODUCTOR o FAENA).  
- **Recria.origen** se persiste; enum DESTETE | EXTERNO; seteo en destete y alta manual.  
- **Un destete por parto:** UNIQUE(parto_id) en BD; limpieza previa de duplicados.  
- **Madre–Gestación:** cierre de gestación y paso de madre a ADULTA en la misma transacción; Parto con gestacion_id.  
- **Concurrencia:** bloqueo o @Version en Recria para registrarVenta.  
- **Orden:** Fase 1 → 2 → 3 → 4 → 5; cada fase con tests y, si aplica, migraciones reversibles o con rollback documentado.

Este documento es la **solución arquitectónica coherente** (no parches) y sirve como especificación para implementación y tareas en Notion.

**Referencia:** Detalle de errores estructurales, máquinas de estado y listado exhaustivo en `AUDITORIA-SDD-MODULO-PORCINOS.md`.
