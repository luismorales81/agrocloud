# Verificación completa del módulo Porcinos (post-cambios)

**Fecha de auditoría:** Verificación contra código y migraciones actuales.  
**Objetivo:** Confirmar estado de migraciones, backend, frontend y detectar inconsistencias o regresiones.

---

# PARTE 1 — Verificación de base de datos

## 1.1 Nombres de migraciones solicitados vs existentes

| Solicitado en el objetivo | Existente en el proyecto | ¿Coincide? |
|---------------------------|---------------------------|------------|
| V1_128__fix_destete_unique.sql | **V1_128__Porcinos_Limpieza_Destetes_Duplicados_Y_Unique_Parto.sql** | **No** (mismo número, distinto nombre) |
| V1_129__remove_faena.sql | **V1_129__Porcinos_Parto_Gestacion_Id_Recria_Check_Origen.sql** | **No** (V1_129 actual **no** elimina faena) |
| V1_130__add_version_recria.sql | **V1_130__Porcinos_Recria_Version.sql** | **No** (mismo número, distinto nombre) |

**Conclusión:** Las migraciones que existen son las definidas en `EJECUCION-SPEC-PORCINOS.md`. No existe una migración `remove_faena`; la spec indicaba **mantener** la tabla `porcinos_faena` en solo lectura para historial.

---

## 1.2 Contenido de las migraciones existentes

### V1_128__Porcinos_Limpieza_Destetes_Duplicados_Y_Unique_Parto.sql

- Limpia destetes duplicados por `parto_id` (marca inactivos los sobrantes).
- Añade **UNIQUE(parto_id)** en `porcinos_destetes`.
- **No** añade FK explícita en esta migración; la FK `parto_id → porcinos_partos(id)` viene del esquema original (V1_28 / V1_99). En BD real debe verificarse que exista la restricción de clave foránea.

**Verificación en BD (ejecutar si hay acceso):**

```sql
-- UNIQUE en destetes
SELECT CONSTRAINT_NAME, CONSTRAINT_TYPE
FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'porcinos_destetes';

-- FK hacia partos
SELECT CONSTRAINT_NAME, COLUMN_NAME, REFERENCED_TABLE_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'porcinos_destetes' AND REFERENCED_TABLE_NAME IS NOT NULL;

-- Duplicados activos (debe devolver 0 filas)
SELECT parto_id, COUNT(*) AS cnt FROM porcinos_destetes WHERE activo = 1 GROUP BY parto_id HAVING cnt > 1;
```

---

### V1_129__Porcinos_Parto_Gestacion_Id_Recria_Check_Origen.sql

- Añade columna **gestacion_id** (nullable) en `porcinos_partos`.
- Crea índice **idx_partos_gestacion**.
- Añade **CHECK (cantidad_animales >= 0)** en `porcinos_recria`.
- **UPDATE** `porcinos_recria SET origen = 'EXTERNO' WHERE origen IS NULL`.

**No elimina** la tabla `porcinos_faena`. Si el objetivo de negocio es que esa tabla ya no exista, haría falta una migración adicional (DROP TABLE o equivalente), no presente en el proyecto.

---

### V1_130__Porcinos_Recria_Version.sql

- Añade columna **version** (BIGINT NULL DEFAULT 0) en `porcinos_recria`.
- **UPDATE** `porcinos_recria SET version = 0 WHERE version IS NULL`.

**Riesgo:** La columna se crea como NULL y luego se actualiza; tras la migración no deberían quedar NULLs. En MySQL, si se desea evitar NULLs en nuevos inserts, convendría `ALTER TABLE porcinos_recria MODIFY COLUMN version BIGINT NOT NULL DEFAULT 0` en una migración posterior (opcional).

---

## 1.3 Resumen BD

| Verificación | Estado | Notas |
|--------------|--------|--------|
| UNIQUE(parto_id) en porcinos_destetes | Definido en V1_128 | Confirmar en BD que el constraint exista tras ejecutar Flyway. |
| FK destetes → partos | No se añade en V1_128 | Debe existir desde creación (V1_28); verificar en BD. |
| Sin duplicados históricos (destetes activos por parto) | Previsto por V1_128 | Los UPDATE marcan inactivos los duplicados antes del UNIQUE. |
| Tabla porcinos_faena ya no exista | **No aplicado** | La spec mantuvo la tabla para historial; no hay migración que la elimine. Si se quiere eliminación, falta una migración. |
| Columna version en porcinos_recria | Definida en V1_130 | Confirmar en BD. |
| Valores version inicializados en 0 | UPDATE en V1_130 | Correcto. |
| Sin NULLs en version | Parcial | Tras UPDATE no deberían quedar; para garantía a futuro, considerar NOT NULL DEFAULT 0. |

**Inconsistencias detectadas (Parte 1):**

1. Nombres de archivos de migración distintos a los indicados en el objetivo (solo impacto documental).
2. La tabla `porcinos_faena` **sigue existiendo**; no hay migración "remove_faena". Decisión de negocio pendiente: eliminar tabla o mantener solo lectura.
3. No se puede confirmar sin acceso a BD que las migraciones se ejecutaron correctamente (Flyway); se recomienda ejecutar las consultas de verificación anteriores en el entorno real.

---

# PARTE 2 — Verificación backend

## 2.1 RecriaStockService y lógica de stock

**Resultado:** **RecriaStockService no existe** en el código.

- En `VentaPorcinoService`, `MuerteRecriaService`, `MovimientoEtapaService` y `RecriaService` sigue usándose un **método privado `calcularAnimalesDisponibles(Recria)`** en cada clase.
- En **VentaPorcinoService** y **MuerteRecriaService** la fórmula incluye **ventasPrevias** (o faenasPrevias en FaenaService):  
  `cantidadInicial - muertes - ventasPrevias` → **doble descuento** respecto a la spec (las ventas ya descontaron `cantidadAnimales`).
- En **MovimientoEtapaService** y **RecriaService** la fórmula es `cantidadInicial - muertes` (sin ventas), pero la fuente de muertes es **recria.getMuertesRecria()** (colección de la entidad), no un repositorio único.

**Conclusión:** La lógica de stock **no** pasa por un único RecriaStockService. Hay lógica duplicada y fórmula incorrecta (doble descuento) en venta y muerte.

---

## 2.2 ServicioService y actualizarEstadoMadre

**Resultado:** **Sí existe lógica duplicada.**

- **ServicioService** tiene un método **privado** `actualizarEstadoMadre` (aprox. líneas 534–557) que actualiza historial y madre.
- Usa ese método en varios puntos (p. ej. marcar servicio fallido, control de celo, etc.).
- La spec indicaba eliminar este método y usar **solo** `MadreService.actualizarEstadoMadre`.

**Conclusión:** No se aplicó el refactor; sigue existiendo duplicación en ServicioService.

---

## 2.3 Referencias activas a la entidad Faena

**Resultado:** **Sí hay referencias activas.**

- **Faena** (entidad), **FaenaRepository**, **FaenaService**, **FaenaController** siguen presentes.
- **FaenaController** en `POST /{recriaId}` **sigue llamando** a `faenaService.registrarFaena(...)` y devuelve 200; no devuelve 410 Gone.
- **FaenaService.registrarFaena** **sigue escribiendo** en BD (porcinos_faena) y actualizando recria.cantidadAnimales; no lanza UnsupportedOperationException.

**Conclusión:** Faena sigue siendo una vía de escritura activa; no se aplicaron los cambios de la spec (410 en controller, UnsupportedOperationException en service).

---

## 2.4 Ventas descuentan animales correctamente / doble descuento

**Resultado:** **Las ventas sí descuentan cantidadAnimales**, pero el cálculo de "disponibles" **resta además las ventas previas**, por lo que:

- Tras la primera venta, `cantidadAnimales` ya está descontada.
- En la segunda venta, `calcularAnimalesDisponibles` vuelve a restar las ventas (incluida la primera) → **doble descuento**.
- Efecto: "animales disponibles" puede quedar en 0 o negativo tras la segunda venta; riesgo de bloqueos incorrectos o de permitir ventas cuando el sistema cree que no hay stock.

**Conclusión:** Hay **doble descuento**; el backend no está alineado con la fórmula oficial de la spec.

---

## 2.5 @Version en Recria

**Resultado:** **No está mapeado.**

- En la entidad **Recria** no existe el campo `version` ni la anotación `@Version`.
- La migración V1_130 añade la columna en BD, pero la entidad no la usa, por lo que JPA no hace control de concurrencia optimista.

**Conclusión:** La concurrencia en ventas **no** está protegida por @Version; riesgo de race conditions.

---

## 2.6 Race conditions y concurrencia

- No hay bloqueo pesimista (SELECT FOR UPDATE) ni versión optimista en el flujo de venta.
- Dos peticiones concurrentes de venta sobre la misma recría pueden leer el mismo `cantidadAnimales`, ambas validar "disponibles" y ambas descontar → stock negativo o más animales vendidos de los reales.

**Conclusión:** Hay **puntos débiles de concurrencia** en el flujo de venta/faena.

---

## 2.7 Otras entidades (origen, Parto.gestacionId)

- **Recria.origen:** Sigue con **@Transient**; no se persiste ni se lee de BD (la columna existe por V1_120 y V1_129 actualiza NULLs).
- **Parto:** No tiene campo **gestacionId** ni getter/setter; la migración V1_129 añade la columna en BD pero la entidad no la mapea, y PartoService no la setea al registrar parto.

**Resumen backend (Parte 2):**

| Verificación | Estado |
|--------------|--------|
| Toda lógica de stock pasa por RecriaStockService | No (RecriaStockService no existe) |
| No existe lógica duplicada en ServicioService | No (actualizarEstadoMadre duplicado) |
| No existan referencias activas a Faena como escritura | No (FaenaController/FaenaService siguen escribiendo) |
| Ventas descuentan correctamente | Parcial (descuentan, pero hay doble descuento en cálculo de disponibles) |
| No haya doble descuento | No (hay doble descuento) |
| @Version en Recria | No (entidad sin version) |
| Concurrencia / race conditions | Débil (sin lock ni @Version) |

---

# PARTE 3 — Verificación frontend

## 3.1 Pantallas de recría y stock

- **RecriaListScreen:** Muestra `recria.cantidadAnimales` (campo que envía el backend).
- **RecriaDetailScreen:** Muestra `recria.cantidadAnimales` y calcula `animalesRestantes = recria.cantidadAnimales - totalMuertes`.  
  - Si el backend devolviera un `cantidadAnimales` ya descontado por ventas (stock actual), esa fórmula sería la correcta para "disponibles" (cantidad - muertes).  
  - El problema está en el **backend** (doble descuento y fórmula), no en que el front muestre el número; cuando el backend esté corregido, el valor mostrado será coherente.
- Las ventas **sí impactan** en el backend (se descuenta cantidadAnimales), por lo que tras una venta, al recargar recría, el número debería bajar. Si hay doble descuento, en ventas sucesivas el valor puede volverse incoherente.

**Conclusión:** El frontend refleja lo que envía el backend. El origen de la incoherencia es el backend (fórmula y doble descuento).

---

## 3.2 Referencias visibles a Faena

- **Rutas:** Existen `/porcinos/faena` y `/porcinos/faena/nueva/:recriaId` (menú "Ventas y Faena").
- **Pantallas:** FaenaListScreen, FaenaCreateScreen; AyudaPorcinosScreen tiene sección "Faena".
- **FaenaCreateScreen:** Usa **ventaPorcinoService.crear** con `tipo: 'FAENA'` (POST a `/v1/porcinos/ventas`), **no** al endpoint antiguo de faena. Por tanto, la **creación** de faenas en el front ya va por el camino unificado (ventas).
- **faenaService.ts** existe y define `registrar()` contra `PORCINOS_FAENA.REGISTRAR(recriaId)`, pero **no se usa** en ningún componente (grep sin usos). FaenaListScreen carga datos con ventaPorcinoService.listar() y filtra por tipo FAENA.

**Conclusión:** No hay referencias frontend que **escriban** faena por el endpoint obsoleto; la escritura de faena en el front es vía ventas. Las referencias a "Faena" son de UI (rutas, listado, ayuda) y están alineadas con ventas tipo FAENA.

---

## 3.3 Endpoints obsoletos consumidos

- **Lectura:** FaenaListScreen y VentasScreen usan ventaPorcinoService (listar/crear) y no faenaService para listar/crear.
- **apiEndpoints.ts** sigue definiendo `PORCINOS_FAENA.REGISTRAR`, `LISTAR`, `INGRESOS_TOTALES`; si algún código los usara, estaría consumiendo endpoints que la spec indicaba devolver 410 para escritura. En el estado actual, **ningún componente** usa faenaService.registrar ni faenaService.listar para la pantalla principal de faenas (FaenaListScreen usa ventaPorcinoService.listar).

**Conclusión:** No se detectan llamadas frontend a endpoints obsoletos de **escritura** de faena. Los de **lectura** (GET faena por recría, listar, ingresos) siguen existiendo en backend; si el front los usara en algún flujo secundario, seguirían funcionando mientras el backend los mantenga.

---

## 3.4 Formularios y validaciones

- **RecriaDetailScreen** (línea ~87): Valida muerte con `formMuerte.cantidad > recria.cantidadAnimales`. Según la spec, lo correcto sería comparar con **disponibles** (cantidadAnimales - totalMuertes), no solo con cantidadAnimales. Así se evita permitir una muerte que deje disponibles en negativo.  
  **Riesgo:** Si en backend cantidadAnimales ya incluye ventas pero no muertes, entonces "disponibles" = cantidadAnimales - muertes; en ese caso la validación front debería ser `formMuerte.cantidad > (recria.cantidadAnimales - totalMuertes)`. Hoy la validación es más restrictiva (no permite muerte mayor a cantidadAnimales), pero conceptualmente debería ser "no mayor a disponibles".

**Resumen frontend (Parte 3):**

| Verificación | Estado |
|--------------|--------|
| Recría muestra stock actualizado | Depende del backend (hoy puede verse afectado por doble descuento) |
| Ventas impactan visualmente en stock | Sí (backend descuenta; front muestra cantidadAnimales) |
| No haya referencias visibles a Faena como camino distinto de venta | Faena visible en menú/rutas pero la escritura es vía ventas tipo FAENA |
| No existan errores de carga en consola (frontend) | No comprobable sin ejecutar la app |
| No haya endpoints obsoletos consumidos para escritura | Correcto (FaenaCreateScreen usa ventas) |
| Formularios funcionan tras cambios | Correcto para faena/ventas; validación de muerte en detalle recría mejorable (usar disponibles) |

---

# PARTE 4 — Test funcional recomendado

Flujo a simular:

1. Crear parto (madre en GESTACION, gestación activa).
2. Registrar destete (cantidad destetados ≤ nacidos vivos).
3. Verificar que se creó recría (desde destete) con cantidadAnimales = cantidad destetados.
4. Registrar muerte en recría (cantidad ≤ disponibles; disponibles = cantidadAnimales - Σ muertes).
5. Registrar venta (cantidad ≤ disponibles).
6. Validar stock final: **stock = cantidad inicial (destetados) - muertes - vendidos**.  
   En el modelo de la spec: `cantidadAnimales` tras la venta debe ser exactamente `cantidad inicial - vendidos`; y **disponibles** = cantidadAnimales - Σ(muertes). Por tanto:  
   - Tras venta: cantidadAnimales = destetados - vendidos.  
   - Disponibles = (destetados - vendidos) - muertes.  
   - Comprobación: destetados - muertes - vendidos = disponibles + vendidos - vendidos = disponibles (coherente).

**Qué validar:**

- Que no haya doble descuento: después de dos ventas parciales, el "animales disponibles" mostrado (o el que use el backend) sea igual a cantidadAnimales - muertes, sin restar de nuevo las ventas.
- Que no se permita venta por encima de disponibles ni muerte por encima de disponibles.
- Que cierre de recría solo se permita cuando disponibles = 0.

Con el **código actual** (doble descuento), el test fallará en el escenario de varias ventas parciales (disponibles incorrectos). Cuando se apliquen los refactors de la spec (RecriaStockService, sin ventasPrevias, @Version), este mismo flujo debe usarse como test de regresión.

---

# PARTE 5 — Reporte final

## Estado de migraciones

| Aspecto | Estado |
|---------|--------|
| Archivos V1_128, V1_129, V1_130 | Presentes con nombres distintos a los citados en el objetivo (Porcinos_...). |
| V1_128 (destete único) | Contenido correcto: limpieza + UNIQUE(parto_id). FK a partos no se añade aquí (preexistente). |
| V1_129 (gestacion_id, CHECK, origen) | Contenido correcto. **No** elimina porcinos_faena. |
| V1_130 (version recría) | Contenido correcto. |
| Ejecución en BD (Flyway) | No verificable sin acceso; se recomienda comprobar con las consultas SQL de la Parte 1. |

## Estado del dominio (backend)

| Aspecto | Estado |
|---------|--------|
| RecriaStockService | **No implementado.** |
| Fórmula única disponibles = cantidadAnimales - Σ(muertes) | **No aplicada;** sigue usándose ventasPrevias en venta y muerte. |
| Doble descuento | **Presente.** |
| Recria.origen persistido | **No** (sigue @Transient). |
| Recria.version (@Version) | **No** (entidad sin campo). |
| Parto.gestacionId | **No** (entidad sin campo; PartoService no setea). |
| Faena como única vía de escritura deshabilitada | **No** (FaenaController/FaenaService siguen escribiendo). |
| ServicioService sin duplicado actualizarEstadoMadre | **No** (sigue con método privado). |
| Concurrencia en venta | **No** (sin @Version ni lock). |

## Estado frontend

| Aspecto | Estado |
|---------|--------|
| Creación de faena | Correcta vía ventaPorcinoService.crear con tipo FAENA. |
| Listado de faenas | Correcto (ventaPorcinoService.listar + filtro tipo FAENA). |
| Visualización de stock en recría | Muestra cantidadAnimales; coherente con backend cuando este esté corregido. |
| Validación muerte vs disponibles | Mejorable (comparar con disponibles, no solo cantidadAnimales). |
| Uso de endpoints obsoletos de escritura | No detectado. |

## Problemas encontrados

1. **Crítico:** Doble descuento en cálculo de disponibles (VentaPorcinoService, MuerteRecriaService).
2. **Crítico:** RecriaStockService no existe; lógica de stock duplicada en cuatro servicios.
3. **Crítico:** Faena sigue siendo vía de escritura (controller 200, service escribe en porcinos_faena).
4. **Alto:** Recria sin @Version; sin control de concurrencia en ventas.
5. **Alto:** Recria.origen no persistido (@Transient).
6. **Alto:** Parto sin gestacionId mapeado; PartoService no setea gestación al registrar parto.
7. **Alto:** ServicioService mantiene actualizarEstadoMadre duplicado.
8. **Medio:** Nombres de migraciones distintos a los listados en el objetivo; tabla porcinos_faena no eliminada (si ese fuera el objetivo).
9. **Medio:** Frontend valida muerte contra cantidadAnimales en lugar de contra disponibles.

## Riesgos detectados

- **Stock negativo o ventas en exceso** por doble descuento y/o concurrencia.
- **Datos incoherentes** si se mezclan faenas por FaenaController y ventas por VentaPorcinoController (dos fuentes de bajas).
- **Pérdida de trazabilidad** (origen recría, parto–gestación) por campos no persistidos o no mapeados.
- **Comportamiento divergente** en transiciones de estado de madre si se modifica solo MadreService y no ServicioService.

## Recomendaciones técnicas

1. **Aplicar los refactors de EJECUCION-SPEC-PORCINOS.md:** Crear RecriaStockService; sustituir todos los calcularAnimalesDisponibles por animalesDisponibles(recria); eliminar ventasPrevias/faenasPrevias; persistir Recria.origen y añadir @Version; mapear Parto.gestacionId y setearlo en PartoService; FaenaController POST → 410, FaenaService.registrarFaena → UnsupportedOperationException; ServicioService usar MadreService.actualizarEstadoMadre; MadreMuerteService actualizar madre al cerrar gestación.
2. **Verificar en BD real** que las migraciones V1_128, V1_129, V1_130 se ejecutaron (consultas de la Parte 1) y que no queden destetes activos duplicados por parto ni version NULL en porcinos_recria.
3. **Frontend:** Ajustar validación de muerte en RecriaDetailScreen a disponibles (cantidadAnimales - totalMuertes) si el backend ya entrega cantidadAnimales como stock actual.
4. **Decisión de negocio:** Si se desea eliminar porcinos_faena, añadir una migración explícita (después de asegurar que todo escribe por ventas) y deprecar lecturas; si se mantiene solo lectura, dejarlo documentado y evitar cualquier escritura desde FaenaService.

---

**Conclusión:** Las migraciones SQL definidas en el proyecto son coherentes con la spec de documento (destete único, gestacion_id, CHECK, origen, version), pero **los cambios de código Java y comportamiento descritos en la spec no están aplicados**. El backend sigue con doble descuento, sin RecriaStockService, sin @Version, con Faena como escritura activa y con lógica duplicada en ServicioService. El frontend está en buena parte alineado (faena por ventas tipo FAENA); la verificación en base de datos real depende de la ejecución efectiva de Flyway y de las comprobaciones SQL indicadas.
