# Especificación técnica: endurecimiento del módulo agrícola (Spec-Driven Development)

**Versión:** 1.0  
**Fecha:** 2026-01-25  
**Alcance:** Pequeños y medianos productores; arquitectura actual; cambios incrementales.  
**Uso:** Base para implementación guiada por especificación. No incluye código; solo análisis, invariantes, estrategias y roadmap.

---

# PASO 1 — Análisis del modelo actual

## 1.1 Agregados reales y pseudo-agregados

| Concepto | Tipo | Entidades implicadas | Observación |
|----------|------|----------------------|-------------|
| **Lote operativo** | Pseudo-agregado | Plot, Labor (por lote), HistorialCosecha (por lote) | Plot no actúa como raíz: Labor y HistorialCosecha se persisten desde distintos servicios sin transacción única ni validación en frontera. |
| **Ciclo agrícola** | Implícito | Plot (fechaSiembra, fechaCosechaReal, estado), HistorialCosecha, Labor SIEMBRA/COSECHA | El “ciclo” es inferido (Plot + último HistorialCosecha). No existe entidad CicloAgricola; no hay frontera explícita. |
| **Labor y detalle** | Pseudo-agregado | Labor, LaborInsumo, LaborMaquinaria, LaborManoObra | Labor es raíz de hecho para sus líneas; el inventario se actualiza en servicio, no en frontera de agregado. |
| **Cosecha e inventario** | Sin agregado | HistorialCosecha, InventarioGrano | Se crean en flujos separados; no hay transacción atómica ni invariante “cosecha ↔ inventario” en frontera. |
| **Configuración por tipo** | Pseudo-agregado | TipoCultivo, EstadoLoteConfig, TransicionEstadoConfig, TareaPorEstadoConfig | Sin raíz única; Plot referencia EstadoLoteConfig; no hay validación “en uso” al modificar. |

## 1.2 Aggregate Root recomendado (modelo actual, sin introducir CicloAgricola aún)

- **Plot** es el candidato natural a **aggregate root** del “lote operativo” en tanto:
  - Concentra estado del lote (estado, cultivo actual, fechas de siembra/cosecha).
  - Toda Labor con lote_id y todo HistorialCosecha pertenecen a un Plot.
  - Las reglas de negocio (“no cosechar sin siembra”, “no dos cosechas activas”) son sobre el lote.
- **Limitación actual:** Plot no controla la persistencia de Labor ni de HistorialCosecha; los repositorios de Labor e HistorialCosecha se usan desde servicios sin pasar por Plot. Por tanto, hoy **no** se comporta como raíz de agregado real.

## 1.3 Fronteras de consistencia faltantes

| Frontera | Estado actual | Faltante |
|----------|----------------|----------|
| **Plot ↔ estado** | Varios escritores (PlotService.actualizar, TransicionEstadoService, SiembraService, LaborService, HistorialCosechaService.liberar). | Un único punto de escritura para Plot.estado (y estadoConfigurado cuando aplique). |
| **Plot ↔ Labor** | Labor se guarda en varios servicios; Plot no valida ni crea Labores. | Garantía de que toda Labor con lote_id pertenezca a un Plot válido y que las transiciones de estado del Plot solo ocurran por labores (o por operaciones explícitas de reset/liberar). |
| **Plot ↔ HistorialCosecha** | HistorialCosecha se crea en SiembraService; crearHistorialCosecha permite ciclos sin Labor. | Garantía de que cada HistorialCosecha corresponda a un ciclo cerrado por cosecha (Labor COSECHA COMPLETADA) y que no existan dos ciclos “activos” solapados para el mismo Plot. |
| **Cosecha ↔ InventarioGrano** | crearInventarioDesdeCosecha se llama después de guardar HistorialCosecha; si falla, no hay rollback. | Transacción única: creación de HistorialCosecha + creación de InventarioGrano; o rollback de la cosecha si falla el inventario. |
| **Labor ↔ MovimientoInventario** | actualizarInventarioLabor/restaurarInventarioLabor se llaman en algunos flujos; en LaborService.crearLabor hay TODO (insumos no procesados). | Toda Labor que tenga LaborInsumo debe provocar descuento/reposición en la misma unidad transaccional (o rechazo si stock insuficiente). |

## 1.4 Invariantes agrícolas no protegidos hoy

| Invariante | Entidad(es) | Protección actual |
|------------|-------------|-------------------|
| Plot no puede pasar a COSECHADO sin cosecha registrada | Plot | No: PlotService.actualizar puede setear estado = COSECHADO. |
| Plot no puede sembrarse si ya está EN_CRECIMIENTO (o posterior) | Plot | Parcial: puedeCosechar() y puedeSembrar() existen pero no se exigen en todos los puntos de escritura. |
| No puede haber dos “cosechas activas” en el mismo lote | Plot / HistorialCosecha | Parcial: tras cosechar, estado pasa a COSECHADO; no hay validación explícita de “ciclo abierto”. |
| Labor no puede existir fuera de un ciclo válido | Labor | No: Labor.lote es nullable; no se valida que el estado del Plot permita la labor. |
| Labor no puede solaparse con otra incompatible en el mismo lote | Labor | No: no hay validación de solapamiento temporal. |
| Labor con insumos debe afectar inventario | Labor / LaborInsumo | Parcial: SiembraService sí; LaborService.crearLabor no (TODO). |
| Toda cosecha debe crear InventarioGrano en la misma operación | HistorialCosecha / InventarioGrano | No: se llama después; si falla inventario, la cosecha queda sin inventario. |
| Cosecha debe cerrar ciclo y actualizar Plot | HistorialCosecha / Plot | Sí en SiembraService.registrarCosecha; no garantizado si se usa crearHistorialCosecha. |
| Fecha de cosecha > fecha de siembra | HistorialCosecha / Plot | No validado explícitamente en BD ni en dominio. |
| No sembrar antes de cerrar el ciclo anterior | Plot | Parcial: liberar/ resetear cierran; no hay invariante explícito “un solo ciclo abierto por Plot”. |

---

# PASO 2 — Especificación de invariantes

## 2.1 Plot

| ID | Descripción funcional | Condición lógica | Enforcement |
|----|------------------------|------------------|-------------|
| P1 | No puede pasar a COSECHADO sin que exista al menos un HistorialCosecha para ese lote con fechaCosecha coherente con el cierre del ciclo. | `estado == COSECHADO → ∃ h ∈ HistorialCosecha : h.lote.id == id ∧ h.fechaCosecha == fechaCosechaReal` (o equivalente: último historial del lote cierra el ciclo). | Servicio: único escritor de estado no permitirá COSECHADO sin flujo de cosecha. |
| P2 | No puede sembrarse si ya está en estado posterior a DISPONIBLE/PREPARADO/EN_PREPARACION (p. ej. SEMBRADO, EN_CRECIMIENTO, …). | `operación SIEMBRA permitida ↔ puedeSembrar() ↔ estado ∈ {DISPONIBLE, PREPARADO, EN_PREPARACION}`. | Servicio + dominio: validar antes de asignar fechaSiembra y estado SEMBRADO. |
| P3 | No puede tener dos ciclos “activos” al mismo tiempo (dos cosechas sin liberar en medio). | Para el mismo Plot, no pueden existir dos HistorialCosecha cuyos intervalos [fechaSiembra, fechaCosecha] se solapen, o bien se define “ciclo abierto” como “Plot.estado ∉ {DISPONIBLE, …} sin HistorialCosecha que lo cierre”. | Servicio: al registrar cosecha, comprobar que no exista otro ciclo no cerrado; al crear HistorialCosecha, validar no solapamiento. |
| P4 | estado y estadoConfigurado deben ser coherentes (si estadoConfigurado != null, estado debe reflejarlo o un valor por defecto). | Definido por regla de negocio (ej. estado = mapeo(estadoConfigurado.nombre) o DISPONIBLE). | Servicio: único escritor actualiza ambos juntos. |
| P5 | areaHectareas > 0 cuando el lote participa en siembra/cosecha. | `fechaSiembra != null ∨ fechaCosechaReal != null → areaHectareas != null ∧ areaHectareas > 0`. | Dominio/Servicio. |

## 2.2 Labor

| ID | Descripción funcional | Condición lógica | Enforcement |
|----|------------------------|------------------|-------------|
| L1 | No puede existir fuera de un ciclo válido cuando tiene lote_id: el estado del Plot debe permitir esa labor (o se acepta “planificada” sin restricción fuerte de ciclo). | Para Labor con lote_id y tipo SIEMBRA: Plot.puedeSembrar(). Para COSECHA: Plot.puedeCosechar(). Para otras: definido por reglas de transición. | Servicio: al crear/completar labor con lote, validar contra Plot.estado. |
| L2 | No puede solaparse con otra labor incompatible en el mismo lote (mismo intervalo de fechas y tipo que exige exclusividad). | Definición: dos labores en el mismo Plot con [fechaInicio, fechaFin] que se solapan y tipos “incompatibles” (ej. dos SIEMBRA, o SIEMBRA y COSECHA) no permitidas. | Servicio: antes de persistir, consultar labores activas del lote en rango y validar. |
| L3 | Si tiene LaborInsumo con cantidad > 0, debe haber provocado descuento (o reposición si se anula) en MovimientoInventario. | Para cada LaborInsumo asociado a Labor en estado COMPLETADA (o creada con insumos): existe movimiento de inventario con origen labor_id. | Servicio: actualizarInventarioLabor en la misma transacción que save de Labor/LaborInsumo. |
| L4 | (Ya existente) COMPLETADA → fechaRealizacion != null; activo=false → estado CANCELADA o ANULADA; etc. | Según spec Labor (I1–I6). | Dominio/Servicio (ya especificado). |

## 2.3 Cosecha (HistorialCosecha + flujo de registro)

| ID | Descripción funcional | Condición lógica | Enforcement |
|----|------------------------|------------------|-------------|
| C1 | Toda operación de “registro de cosecha” debe crear InventarioGrano en la misma operación (transacción). | `crear HistorialCosecha en flujo de cosecha → en la misma transacción crear InventarioGrano asociado a ese HistorialCosecha; si falla InventarioGrano → rollback HistorialCosecha`. | Servicio + transacción: unidad atómica. |
| C2 | La cosecha debe “cerrar” el ciclo agrícola: Plot pasa a COSECHADO y fechaCosechaReal seteadas. | Tras persistir HistorialCosecha e InventarioGrano: `Plot.estado = COSECHADO ∧ Plot.fechaCosechaReal = fechaCosecha del historial`. | Servicio: mismo flujo que hoy en SiembraService; sin bypass por crearHistorialCosecha sin actualizar Plot. |
| C3 | Debe existir una Labor de tipo COSECHA en estado COMPLETADA asociada al mismo lote y fecha coherente. | Opcional fuerte: `∀ HistorialCosecha creado por flujo de cosecha, ∃ Labor : labor.lote = h.lote ∧ labor.tipoLabor = COSECHA ∧ labor.estado = COMPLETADA ∧ labor.fechaRealizacion = h.fechaCosecha` (o rango). | Servicio: el único camino de creación de HistorialCosecha “de cosecha” es el que ya crea Labor COSECHA + HistorialCosecha; no exponer crearHistorialCosecha como registro de cosecha sin Labor. |
| C4 | fechaCosecha debe ser posterior o igual a fechaSiembra. | `fechaCosecha >= fechaSiembra`. | Dominio/Servicio al crear HistorialCosecha. |
| C5 | superficieHectareas debe provenir de Plot.areaHectareas (no editable libre en este flujo). | `superficieHectareas == lote.areaHectareas` en creación. | Servicio. |

## 2.4 Resumen de niveles de enforcement

- **Dominio:** Reglas que pueden expresarse en la entidad (validaciones en setters o métodos de negocio); sin acceso a repositorios.
- **Servicio:** Reglas que requieren lectura de otras entidades o repositorios; un único punto de escritura (orquestación).
- **DB:** Constraints (CHECK, FK, UNIQUE) cuando sea posible; no sustituyen la lógica de negocio pero refuerzan.

---

# PASO 3 — Concurrencia

## 3.1 Entidades que requieren @Version

| Entidad | Justificación |
|---------|----------------|
| **Plot** | Múltiples escritores (siembra, cosecha, actualización estado, reset, liberar); riesgo de lost update y de estado incoherente. |
| **Labor** | Ya identificado en spec Labor; PATCH/PUT concurrentes. |
| **HistorialCosecha** | Si en el futuro se permite creación/edición desde más de un flujo; además, evita doble creación de ciclo en concurrencia. |
| **InventarioGrano** | Ajustes y movimientos pueden concurrir; opcional si el inventario se crea solo en flujo de cosecha y no se edita en paralelo. |

No se exige @Version en LaborInsumo, LaborMaquinaria, LaborManoObra si solo se modifican dentro de la misma transacción que la Labor (y la raíz es Labor con @Version).

## 3.2 Errores a lanzar ante conflicto

- Al hacer **save** de una entidad con @Version, si JPA detecta que la versión en BD es distinta de la que tiene la entidad cargada:
  - **Lanzar:** `OptimisticLockException` (o la excepción que provea el stack: p. ej. `JpaOptimisticLockingFailureException`).
- **No** silenciar el error ni hacer merge automático de estado; el cliente debe ser informado de conflicto.

## 3.3 Comportamiento ante conflicto

- **API REST:** Devolver **409 Conflict** con cuerpo que indique que el recurso fue modificado por otro usuario/request y que se refresque y reintente.
- **Cliente:** Mostrar mensaje del tipo “Los datos fueron modificados por otro usuario. Recargue y vuelva a intentar.” y no sobrescribir sin confirmación.

## 3.4 Diagrama de secuencia (textual)

```
Usuario A                    Servidor                         Usuario B                    BD
    |                            |                                 |                          |
    |  GET /api/lotes/1          |                                 |                          |
    |--------------------------->|  SELECT Plot (version=5)         |                          |
    |<---------------------------|                                 |                          |
    |  GET /api/lotes/1          |                                 |                          |
    |                            |-------------------------------->|  SELECT Plot (version=5) |
    |                            |<--------------------------------|                          |
    |  PUT /api/lotes/1          |                                 |  PUT /api/lotes/1         |
    |  (cambia nombre)           |                                 |  (cambia estado)          |
    |--------------------------->|                                 |                          |
    |                            |  UPDATE ... WHERE id=1 AND version=5                        |
    |                            |----------------------------------------------------------->|
    |                            |                            version=6                        |
    |<---------------------------| 200 OK                                                      |
    |                            |                                 |-------------------------->|
    |                            |                                 |  UPDATE ... WHERE id=1 AND version=5
    |                            |                                 |  (0 rows updated)         |
    |                            |                                 |<--------------------------|
    |                            |                                 |  OptimisticLockException  |
    |                            |                                 |  409 Conflict             |
    |                            |                                 |<--------------------------|
```

## 3.5 Estrategia retry vs fail-fast

- **Recomendación: fail-fast.** No reintentar automáticamente en el servidor:
  - Evita que un retry sobrescriba cambios del otro usuario sin que el usuario actual decida.
  - El cliente puede reintentar después de refrescar (GET) y volver a enviar (PUT).
- Si en el futuro se implementa retry, debe ser **con re-lectura** (GET del recurso actualizado) y **un único reintento**; no retry ciego.

---

# PASO 4 — Transaccionalidad

## 4.1 Operaciones que deben ser atómicas

| Caso de uso | Unidad transaccional mínima | Qué no puede quedar en estado intermedio |
|-------------|-----------------------------|------------------------------------------|
| **Registro de cosecha** (SiembraService.cosecharLote o equivalente) | Crear Labor COSECHA (COMPLETADA) + HistorialCosecha + InventarioGrano + actualizar Plot (estado COSECHADO, fechaCosechaReal). | No puede existir HistorialCosecha sin InventarioGrano creado; no puede quedar Plot en COSECHADO sin HistorialCosecha. |
| **Registro de siembra** (SiembraService.registrarSiembra) | Crear Labor SIEMBRA (COMPLETADA) + LaborInsumo + actualizar inventario (actualizarInventarioLabor) + actualizar Plot (estado, fechaSiembra, cultivo, etc.). | No puede quedar Labor guardada con insumos sin descuento en inventario; no puede quedar Plot con fechaSiembra sin Labor de siembra completada. |
| **Crear/editar Labor con insumos** (LaborService.crearLabor / actualizarLabor con LaborInsumo) | Guardar Labor (+ LaborInsumo) + actualizarInventarioLabor (consumir/reponer). | No puede quedar LaborInsumo persistido sin movimiento de inventario correspondiente (o labor en estado que no requiera inventario). |
| **Cancelar/anular Labor** (con insumos) | Actualizar estado Labor + restaurarInventarioLabor (si aplica) + actualizar Plot si la transición lo requiere. | No puede quedar Labor cancelada/anulada con insumos no restaurados. |
| **Liberar lote** (HistorialCosechaService.liberarLoteParaNuevaSiembra) | Actualizar Plot (estado DISPONIBLE, limpiar fechas y cultivo actual). | N/A (una sola entidad). |
| **Resetear lote** (PlotService.resetearLote) | Actualizar todas las Labores del lote (estado CANCELADA/ANULADA, activo) + actualizar Plot. | No debe quedar Plot reseteado con labores aún activas en estado no terminal. |

## 4.2 Si falla la creación de inventario (cosecha)

- **Comportamiento requerido:** La transacción del caso de uso “Registro de cosecha” debe ser **una sola transacción** (mismo `@Transactional`):
  - Si `crearInventarioDesdeCosecha` (o el paso equivalente) lanza excepción, la transacción debe hacer **rollback** completo: no se persiste HistorialCosecha ni el cambio de Plot.
- **No** es aceptable: guardar HistorialCosecha, luego intentar crear InventarioGrano, capturar excepción y solo loguear (estado actual). Debe ser todo-o-nada.
- **Orden sugerido dentro de la transacción:** (1) Crear y persistir Labor COSECHA; (2) Crear y persistir HistorialCosecha; (3) Crear InventarioGrano desde HistorialCosecha; (4) Actualizar Plot. Cualquier fallo en (2)–(4) provoca rollback de (1)–(4).

---

# PASO 5 — Validaciones temporales

## 5.1 Reglas en lenguaje natural y forma lógica

| # | Lenguaje natural | Regla lógica | Punto de validación |
|---|------------------|--------------|----------------------|
| T1 | No puede haber dos labores activas en el mismo lote en las mismas fechas si son incompatibles (p. ej. dos siembras, o siembra y cosecha solapadas). | Para Plot P, no existir Labor L1, L2 tales que L1.lote = P, L2.lote = P, L1.activo = true, L2.activo = true, L1 ≠ L2, y [L1.fechaInicio, L1.fechaFin] ∩ [L2.fechaInicio, L2.fechaFin] ≠ ∅ y tipos incompatibles (definir lista: ej. SIEMBRA–SIEMBRA, SIEMBRA–COSECHA). | Servicio: antes de persistir Labor (crear o actualizar fechaInicio/fechaFin), consultar labores del lote en rango y validar. |
| T2 | La fecha de cosecha debe ser posterior o igual a la fecha de siembra. | Para HistorialCosecha H: `H.fechaCosecha >= H.fechaSiembra`. | Servicio al crear/actualizar HistorialCosecha. |
| T3 | No puede sembrarse antes de cerrar el ciclo anterior (el lote debe estar DISPONIBLE o en estado “pre-siembra” para una nueva siembra). | `operación SIEMBRA permitida ↔ Plot.puedeSembrar()`. Ya definido en P2. | Servicio antes de asignar fechaSiembra y estado SEMBRADO. |
| T4 | No pueden solaparse dos ciclos (HistorialCosecha) para el mismo lote. | Para Plot P, no existir HistorialCosecha H1, H2 con H1.lote = H2.lote = P, [H1.fechaSiembra, H1.fechaCosecha] ∩ [H2.fechaSiembra, H2.fechaCosecha] ≠ ∅. | Servicio al crear HistorialCosecha (o al registrar cosecha). |
| T5 | fechaRealizacion de una Labor debe ser >= fechaInicio (ya cubierto por invariante Labor I4). | `Labor.fechaRealizacion >= Labor.fechaInicio` cuando estado = COMPLETADA. | Dominio/Servicio (spec Labor). |

## 5.2 Definición de “tipos incompatibles” para solapamiento (T1)

- **Incompatibles:** Misma labor que exige exclusividad en el lote en ese período: por ejemplo dos labores de tipo SIEMBRA, o una SIEMBRA y una COSECHA cuyos intervalos se solapan (en la práctica, SIEMBRA y COSECHA no deberían solaparse si las fechas son coherentes; la regla fuerte es no permitir dos SIEMBRA solapadas ni dos COSECHA solapadas en el mismo lote).
- **Compatibles:** Por ejemplo FERTILIZACION y RIEGO en el mismo rango pueden permitirse según reglas de negocio; si no se desea restricción tan fuerte, limitar a “no dos SIEMBRA ni dos COSECHA solapadas”.

---

# PASO 6 — Plan incremental de implementación

## Nivel 1 — MVP endurecido

**Objetivo:** Concurrencia básica y transacción cosecha–inventario sin cambiar modelo de dominio.

| Cambio | Descripción | Riesgo | Impacto en código existente | Complejidad |
|--------|-------------|--------|-----------------------------|-------------|
| Añadir @Version a Plot y Labor | Columna `version` (BIGINT, default 0); en entidades, campo `private Long version`. | Bajo si se despliega con migración; clientes que no envíen version en PUT pueden recibir 409 hasta que re-creen el recurso. | Controladores: manejar OptimisticLockException y devolver 409. Servicios: no cambiar lógica; JPA incrementa version en save. | Baja |
| Transacción única cosecha–inventario | En el flujo de registro de cosecha (SiembraService), incluir creación de InventarioGrano dentro de la misma transacción; si falla crearInventarioDesdeCosecha, no capturar y tragar excepción; dejar que la transacción haga rollback. | Bajo. | Una transacción ya envuelve el flujo; quitar try/catch que solo loguea y no relanza. Ajustar orden si hace falta (HistorialCosecha antes de InventarioGrano). | Baja |
| Centralizar escritura de Plot.estado | En PlotService.actualizar (PUT lote), no asignar `estado` ni `estadoConfigurado` desde el DTO; ignorar o rechazar si vienen en el body. | Medio: clientes que envíen estado en PUT dejarán de poder cambiarlo por esa vía; deben usar flujos de siembra/cosecha/reset/liberar. | PlotService: eliminar o condicionar setEstado/setEstadoConfigurado desde loteData. Documentar API. | Baja |

**Entregables Nivel 1:** Plot y Labor con @Version; 409 en conflicto; cosecha e inventario en una transacción; estado del Plot no editable por PUT genérico.

---

## Nivel 2 — Multiusuario estable

**Objetivo:** Validaciones temporales básicas y garantía Labor–inventario en todos los flujos.

| Cambio | Descripción | Riesgo | Impacto en código existente | Complejidad |
|--------|-------------|--------|-----------------------------|-------------|
| Validación de solapamiento de labores (T1) | Antes de persistir Labor (crear o actualizar fechas), consultar labores activas del mismo lote en [fechaInicio, fechaFin] y rechazar si hay incompatibilidad (ej. dos SIEMBRA o dos COSECHA solapadas). | Medio: usuarios que hoy crean labores “solapadas” recibirán error. | LaborService (crearLabor, actualizarLabor, actualizarParcialLabor cuando cambien fechas); SiembraService si crea labores. Nuevo método de validación o regla en servicio. | Media |
| Procesar insumos en LaborService.crearLabor | Implementar el TODO: al crear Labor con LaborInsumo, llamar actualizarInventarioLabor en la misma transacción; si falla stock, rollback y excepción. | Bajo. | LaborService.crearLabor: después de guardar Labor y LaborInsumo, llamar inventarioService.actualizarInventarioLabor. | Media |
| Añadir @Version a HistorialCosecha | Para evitar doble creación en concurrencia. | Bajo. | Migración + entidad; manejo 409 si se expone edición. | Baja |
| Validación fechaCosecha >= fechaSiembra (T2, C4) | Al crear HistorialCosecha, validar en servicio; si no se cumple, lanzar excepción de validación. | Bajo. | SiembraService y, si se mantiene, HistorialCosechaService.crearHistorialCosecha. | Baja |

**Entregables Nivel 2:** No solapamiento de labores incompatibles; Labor con insumos siempre afecta inventario; HistorialCosecha con version y regla temporal fechaCosecha >= fechaSiembra.

---

## Nivel 3 — Consistencia fuerte

**Objetivo:** Un solo escritor de Plot.estado, invariantes P1–P3 y C1–C3 reforzados.

| Cambio | Descripción | Riesgo | Impacto en código existente | Complejidad |
|--------|-------------|--------|-----------------------------|-------------|
| Un único “orquestador” de cambio de estado de Plot | Extraer a un componente (ej. “PlotStateWriter” o métodos en un servicio existente) todas las escrituras de Plot.estado y estadoConfigurado; PlotService.actualizar, TransicionEstadoService, SiembraService, LaborService, HistorialCosechaService llaman a ese punto en lugar de setEstado directamente. | Medio: refactor de llamadas. | Sustituir todas las asignaciones directas a setEstado/setEstadoConfigurado por llamadas a métodos que validen (P2, P3) y luego escriban. | Media |
| Garantizar que HistorialCosecha solo se cree desde flujo de cosecha | No exponer crearHistorialCosecha como API de “registro de cosecha”; o documentar que es solo para corrección/importación y que no actualiza Plot. Si se usa, no actualizar Plot.estado a COSECHADO. | Bajo. | HistorialCosechaService.crearHistorialCosecha: no actualizar Plot; o deprecar y dejar un solo flujo (SiembraService). | Baja |
| Validación “no dos ciclos solapados” (T4) | Al crear HistorialCosecha en el flujo de cosecha, comprobar que no exista otro HistorialCosecha del mismo lote con [fechaSiembra, fechaCosecha] solapado. | Bajo. | Consulta en HistorialCosechaRepository por lote y rango; rechazar si hay solapamiento. | Media |
| Invariante P1 (COSECHADO solo con cosecha registrada) | El único camino que pone Plot.estado = COSECHADO es el flujo que acaba de crear HistorialCosecha + InventarioGrano; no permitir COSECHADO por PUT ni por otro servicio. | Ya cubierto en Nivel 1 si estado no se escribe en PUT; reforzar en Nivel 3 con orquestador único. | Ver “Un único orquestador”. | Incluido arriba |

**Entregables Nivel 3:** Estado del Plot solo cambia por orquestador; HistorialCosecha sin bypass que deje Plot incoherente; no solapamiento de ciclos.

---

## Nivel 4 — Modelo agrícola explícito con CicloAgricola (opcional)

**Objetivo:** Introducir entidad CicloAgricola como frontera explícita del ciclo siembra–cosecha.

| Cambio | Descripción | Riesgo | Impacto en código existente | Complejidad |
|--------|-------------|--------|-----------------------------|-------------|
| Entidad CicloAgricola | Nueva entidad: Plot, fechaInicio (siembra), fechaFin (cosecha), estado (ABIERTO/CERRADO), referencia a HistorialCosecha cuando se cierra. | Alto: cambio de modelo y de flujos. | Plot tendría relación “ciclo actual” o lista de ciclos; HistorialCosecha podría asociarse a CicloAgricola. Creación de ciclos al sembrar; cierre al cosechar. | Alta |
| Plot sin fechaSiembra/fechaCosechaReal directas | O bien Plot mantiene “último ciclo” (redundante) para compatibilidad, o se derivan de CicloAgricola. | Alto: muchos puntos leen Plot.fechaSiembra / fechaCosechaReal. | Migración de datos; adaptar lecturas a “ciclo activo” o último ciclo. | Alta |
| Invariantes sobre CicloAgricola | Un Plot tiene como máximo un ciclo ABIERTO; toda Labor de siembra/cosecha pertenece a un ciclo. | — | Servicios de siembra y cosecha crean/cierran ciclos. | Alta |

**Recomendación:** Nivel 4 es opcional y de mayor alcance; solo abordar si se prioriza un modelo de dominio más explícito y se acepta el coste. Los Niveles 1–3 ya endurecen el sistema sin introducir CicloAgricola.

---

# Resumen de uso del documento

- **PASO 1** sirve para alinear agregados, raíz y fronteras antes de codificar.
- **PASO 2** es la referencia de invariantes (Plot, Labor, Cosecha) con nivel de enforcement.
- **PASO 3** define dónde usar @Version, 409 y fail-fast.
- **PASO 4** define transacciones atómicas y comportamiento si falla inventario.
- **PASO 5** fija reglas temporales y punto de validación.
- **PASO 6** da el roadmap en 4 niveles (MVP → multiusuario → consistencia fuerte → CicloAgricola opcional).

Cada cambio de implementación posterior debe poder trazarse a un ítem de esta especificación.

---

**Fin del documento.** No se incluye código; solo especificación para implementación guiada por spec.
