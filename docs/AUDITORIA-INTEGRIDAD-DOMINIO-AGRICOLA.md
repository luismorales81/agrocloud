# Auditoría de integridad estructural del dominio agrícola

**Fecha:** 2026-01-25  
**Alcance:** Módulo agrícola (cultivos, labores, cosecha, configuración). Solo evaluación; sin rediseño ni propuestas de nueva arquitectura.

---

## Executive Summary

El dominio agrícola tiene **Plot** como núcleo operativo (lotes con estado, cultivo actual, fechas) y **Labor** como fuente de verdad de ejecución, con **TransicionEstadoService** alineando estado del lote al completar labores. No existen agregados explícitos: **Labor** se persiste desde varios servicios (LaborService, SiembraService, PlotService) sin que Plot sea raíz de consistencia; **HistorialCosecha** se crea solo en el flujo de SiembraService.registrarCosecha, pero **HistorialCosechaService.crearHistorialCosecha** permite crear ciclos sin Labor COMPLETADA si se expone o reutiliza. **Plot.estado** puede modificarse por **PlotService.actualizar** (loteData.estado) y por **resetearLote**, lo que genera riesgo de **deriva** respecto al último estado inferible desde labores. No hay **@Version** en Plot ni Labor; hay riesgo de condiciones de carrera en siembra/cosecha simultánea y en actualización de configuración con lotes activos. La **configuración** (EstadoLoteConfig, TransicionEstadoConfig, TareaPorEstadoConfig) no está versionada; si se elimina o cambia un estado/transición en uso, los Plot que lo referencian pueden quedar inconsistentes. **LaborInsumo** y **MovimientoInventario** se alinean cuando se usa **actualizarInventarioLabor** (SiembraService, flujo de creación con insumos); en **LaborService.crearLabor** el procesamiento de insumos está en TODO y no descuenta inventario. **Egreso/Ingreso** solo referencian **lote**, no labor; no hay garantía de trazabilidad costo–labor. **AplicacionAgroquimica** exige **labor** (FK not null). No se valida solapamiento temporal de labores en el mismo lote ni de ciclos HistorialCosecha. **Superficie** en HistorialCosecha proviene de **Plot.areaHectareas**; el rendimiento se calcula y puede no validarse contra umbrales biológicos. **Cultivo** en catálogo (ACTIVO/INACTIVO) es independiente de TipoCultivo/EstadoLoteConfig; un cultivo puede estar ACTIVO pero sin tipo configurado para la empresa. El endurecimiento más impactante recomendado es **un único punto de creación/actualización de Plot.estado** (y opcionalmente estadoConfigurado) desde la autoridad de transición, evitando mutaciones directas en PlotService y en respuestas a PUT.

---

## 1. Aggregate Boundary Findings

| Tema | Hallazgo | Inconsistencia |
|------|----------|----------------|
| **Plot como raíz de Labores** | Labor tiene `lote_id` nullable; se guarda con `laborRepository.save` desde LaborService, SiembraService y PlotService. Plot no controla la creación ni la vida de Labor. | **No hay agregado** Plot→Labores: los hijos se persisten de forma independiente; las invariantes (por ejemplo “una labor COMPLETADA de siembra implica Plot en SEMBRADO”) se aplican en servicios, no en frontera de agregado. |
| **Plot como raíz de HistorialCosecha** | HistorialCosecha tiene `lote_id` y `cultivo_id` obligatorios. Se crea solo en SiembraService.registrarCosecha (inline); HistorialCosechaService.crearHistorialCosecha(Plot, Cultivo, …) permite crear un ciclo sin ninguna Labor. | **Inconsistencia:** HistorialCosecha puede existir sin Labor COMPLETADA de cosecha si se usa crearHistorialCosecha (p. ej. desde otro servicio o un futuro endpoint). No hay raíz que garantice “cada ciclo tiene una labor de cosecha”. |
| **Cultivo: catálogo vs referencia en ejecución** | Cultivo es entidad de catálogo (ACTIVO/INACTIVO). Plot tiene cultivo_id y cultivoActual (string). Labor tiene cultivo_id opcional. | **Coherencia aceptable:** Cultivo se usa como referencia; Plot y Labor pueden apuntar a un cultivo INACTIVO (no se valida en BD). No hay agregado Cultivo→Plot. |
| **TipoCultivo + EstadoLoteConfig + TransicionEstadoConfig** | Se consultan juntos para “estado inicial” y transiciones. No hay entidad raíz que agrupe; Plot guarda estado_configurado_id (FK a EstadoLoteConfig). | **Configuración como pseudo-agregado:** Si se borra o desactiva un EstadoLoteConfig/TransicionEstadoConfig, los Plot que lo referencian pueden quedar con FK inválida o estado semántico obsoleto. No hay validación “en uso” al modificar configuración. |
| **Labor sin Plot** | Labor.lote es nullable (JoinColumn sin nullable=false). | **Estructural:** Labor puede existir sin Plot; el modelo lo permite. La lógica de negocio asume en muchos flujos que labor.getLote() != null. |
| **Plot.cultivoActual vs labores** | PlotService.resetearLote y HistorialCosechaService.liberar* ponen cultivoActual = null y limpian fechas. SiembraService y LaborService asignan cultivoActual al sembrar. | **Riesgo:** Si se cambia cultivoActual (o tipo de cultivo) por otro camino sin cancelar/reprogramar labores activas, las labores pueden quedar referenciando un “ciclo” distinto al que representa el lote. |
| **Repositorios guardando hijos solos** | laborRepository.save(labor) se usa en LaborService, SiembraService, PlotService (resetearLote). historialCosechaRepository.save en SiembraService y en HistorialCosechaService. | **Sin transacción de agregado:** Cada entidad se persiste por separado; no hay una “raíz” que persista en una misma transacción con sus hijos y valide invariantes. |

---

## 2. EstadoLote vs Labor Drift Risks

| Escenario | Mecanismo | Riesgo |
|-----------|-----------|--------|
| **Plot.estado distinto del inferido por labores** | PlotService.actualizar permite `lote.setEstado(loteData.getEstado())` (línea ~146). TransicionEstadoService actualiza estado al completar labor; no hay un único punto de escritura. | **Deriva:** Un PUT en el lote puede dejar estado = SEMBRADO sin ninguna labor de siembra COMPLETADA, o COSECHADO sin labor de cosecha. |
| **SIEMBRA no obliga transición a SEMBRADO** | En LaborService, al crear labor con tipo SIEMBRA y cultivoId se llama asignarCultivoAlLotePorSiembra y se pone SEMBRADO. En SiembraService.registrarSiembra se actualiza lote a SEMBRADO y fechaSiembra. TransicionEstadoService solo se invoca cuando labor está COMPLETADA (en crearLabor no se llama si la labor se crea PLANIFICADA). | **Deriva:** Si la labor de siembra se crea PLANIFICADA y luego se completa por PATCH, transicionEstadoService.evaluarYAplicarTransicion sí se ejecuta; si la siembra se registra por SiembraService, el lote se actualiza en el mismo flujo. El riesgo es doble vía de actualización de estado (LaborService vs SiembraService vs TransicionEstadoService). |
| **Labor COMPLETADA sin actualizar Plot** | TransicionEstadoService.evaluarYAplicarTransicion se llama desde LaborService (actualizarParcialLabor, actualizarLabor cuando pasa a COMPLETADA) y desde confirmarLaborSiembra/confirmarLaborCosecha. Si una labor se marca COMPLETADA por un camino que no invoca evaluarYAplicarTransicion (p. ej. código futuro o bypass), el lote no cambia. | **Deriva:** Plot.estado y Plot.fechaSiembra/fechaCosechaReal pueden no reflejar la última labor completada. |
| **Estado configurado vs enum** | Plot tiene estado (enum EstadoLote) y estadoConfigurado (EstadoLoteConfig). TransicionEstadoService puede setear estadoConfigurado y luego estado = DISPONIBLE, o solo estado (tradicional). | **Deriva:** estado enum y estadoConfigurado pueden desincronizarse si algo escribe solo uno de los dos. |
| **HistorialCosecha vs Plot.estado** | Tras registrarCosecha, Plot pasa a COSECHADO y se crea HistorialCosecha. liberarLoteParaNuevaSiembra pone Plot en DISPONIBLE sin tocar historial. | **Alineado** en el flujo actual. Riesgo: si Plot.estado se cambia manualmente a COSECHADO sin registrar cosecha, no hay HistorialCosecha correspondiente. |

---

## 3. Ciclo Agrícola Integrity (Plot + HistorialCosecha)

| Pregunta | Respuesta | Riesgo |
|----------|-----------|--------|
| **¿Varios HistorialCosecha solapados para el mismo Plot?** | No hay validación de solapamiento (mismo lote, rangos fechaSiembra–fechaCosecha que se crucen). Se puede tener más de un registro por lote en el mismo período si se usa crearHistorialCosecha o flujos alternativos. | **MEDIUM:** Ciclos solapados en el mismo lote son biológicamente incoherentes. |
| **¿HistorialCosecha sin Labor COMPLETADA de cosecha?** | Sí: HistorialCosechaService.crearHistorialCosecha(Plot, Cultivo, fechaCosecha, …) crea registro sin ninguna Labor. El controller actual no expone POST de creación; el riesgo es de diseño. | **MEDIUM:** Trazabilidad cosecha → labor no garantizada si se usa ese método. |
| **¿Cosecha registrada dos veces en el mismo ciclo?** | SiembraService.cosecharLote exige lote.puedeCosechar() (estado en SEMBRADO…LISTO_PARA_COSECHA). Tras guardar, lote pasa a COSECHADO; puedeCosechar() devuelve false. No se puede volver a llamar cosecharLote para el mismo lote sin antes liberar. | **Protegido** en el flujo actual. |
| **¿Rendimiento real validado contra superficie?** | rendimientoReal = cantidadCosechada / areaHectareas (Plot). No hay comprobación de umbral máximo (p. ej. tn/ha razonables). | **LOW:** Valores extremos posibles. |
| **¿Superficie de Plot o manual?** | HistorialCosecha.superficieHectareas se asigna desde lote.getAreaHectareas() en SiembraService y en HistorialCosechaService.crearHistorialCosecha. | **Consistente** con Plot. |
| **¿Liberar permite “doble cosecha”?** | liberarLoteParaNuevaSiembra exige que no haya cosechas recientes (countCosechasRecientesPorLote &lt; 7 días). Tras liberar, el lote está DISPONIBLE; una nueva siembra+cosecha crea otro HistorialCosecha. No se impide tener dos ciclos con fechas que se solapen si se manipulan fechas o se usa crearHistorialCosecha. | **LOW** en uso normal; **MEDIUM** si se abren más vías de creación de historial. |

---

## 4. Configuration vs Execution Drift

| Riesgo | Detalle |
|--------|---------|
| **Cambio de configuración con lotes activos** | Si se desactiva (activo=false) o se elimina un EstadoLoteConfig que está en Plot.estadoConfigurado_id, los Plot que lo referencian quedan con FK a fila inactiva o inexistente. No hay comprobación “estado X está en uso por N lotes” al modificar configuración. |
| **Transición eliminada** | Si se borra una TransicionEstadoConfig que es la única salida de un estado en el que hay lotes, evaluarTransicionConfigurada dejará de devolver siguiente estado; los lotes pueden quedar “atascados” en ese estado hasta cambio manual. |
| **Sin versionado de configuración** | No hay versión ni fecha efectiva en EstadoLoteConfig / TransicionEstadoConfig / TareaPorEstadoConfig. No se puede saber qué configuración regía cuando se creó una labor o un ciclo. |
| **TareaPorEstadoConfig** | Las tareas sugeridas por estado se leen en tiempo real. Si se reordenan o desactivan tareas, el calendario/tareas disponibles cambian de inmediato; no afecta datos ya guardados (labores existentes), pero la oferta de “próxima tarea” puede no coincidir con lo que el usuario tenía en pantalla. |

---

## 5. Cross-Module Mismatch Risks

| Par | Garantía | Riesgo |
|-----|----------|--------|
| **LaborInsumo ↔ MovimientoInventario** | actualizarInventarioLabor (InventarioService) consume stock al crear/editar labor con insumos; restaurarInventarioLabor al cancelar/anular. | **LaborService.crearLabor** tiene TODO: “Implementar procesamiento de insumos usados”; si se crean LaborInsumo por esa ruta sin llamar a actualizarInventarioLabor, el inventario no se descuenta y hay **inconsistencia**. SiembraService sí llama actualizarInventarioLabor. |
| **Cosecha ↔ InventarioGrano** | crearInventarioDesdeCosecha se llama tras guardar HistorialCosecha en SiembraService.registrarCosecha; si falla, se loguea pero no se hace rollback de la cosecha. | **Inventario de grano puede no crearse** aunque la cosecha quede registrada (inconsistencia). No hay transacción única que exija “cosecha + inventario” juntos. |
| **HistorialCosecha ↔ Ingreso** | HistorialCosecha tiene precioVentaUnitario e ingresoTotal; no hay entidad Ingreso creada automáticamente desde HistorialCosecha. | **Ingresos por venta de cultivo** pueden registrarse en Ingreso con lote_id sin vínculo a HistorialCosecha; no hay garantía de que el ingreso coincida con un ciclo. |
| **Egreso ↔ Labor** | Egreso tiene lote_id y referencia_id opcional; no hay labor_id. Los costos de labor están en Labor.costoTotal y en LaborInsumo/LaborMaquinaria/LaborManoObra. | **Egresos** pueden crearse a mano por lote sin correspondencia con labores; no hay trazabilidad obligatoria Egreso→Labor. |
| **AplicacionAgroquimica ↔ Labor** | AplicacionAgroquimica.labor_id es NOT NULL. | **Consistente:** toda aplicación está ligada a una labor. |

---

## 6. Temporal Violations

| Tipo | Hallazgo |
|------|----------|
| **Solapamiento de labores en el mismo Plot** | No hay validación que impida dos labores activas con fechas (fechaInicio–fechaFin) solapadas en el mismo lote. Se pueden crear/planificar labores que se superponen en el tiempo. |
| **Plot.fechaSiembra vs HistorialCosecha.fechaSiembra** | En registrarCosecha, historialCosecha.setFechaSiembra(lote.getFechaSiembra() != null ? lote.getFechaSiembra() : request.getFechaCosecha().minusDays(120)). Si después se cambia Plot.fechaSiembra (p. ej. por otro flujo), HistorialCosecha no se actualiza. **Deriva posible.** |
| **Múltiples asignaciones de cultivoActual** | Plot puede recibir setCultivoActual desde SiembraService, LaborService (actualizarInformacionCultivo, asignarCultivoAlLotePorSiembra), PlotService (resetearLote, liberar). No hay secuencia temporal única (p. ej. “solo la última siembra define cultivo actual”). |
| **Labor.fechaInicio vs Labor.fechaRealizacion** | Invariante I4 (fechaRealizacion no anterior a fechaInicio) se valida en LaborService. No se valida que fechaInicio no sea futura al momento de planificar (depende de reglas de negocio). |
| **Campaign / ciclo** | No existe entidad “campaña” o “ciclo”; el ciclo se infiere de Plot (fechaSiembra, fechaCosechaReal) y de HistorialCosecha. Varios HistorialCosecha para el mismo lote pueden tener fechas que se solapan si se crean por crearHistorialCosecha. |

---

## 7. Biological Unrealism

| Escenario | ¿Permitido? | Comentario |
|-----------|-------------|------------|
| **Siembra sin área** | No | Plot.areaHectareas es obligatorio; la siembra siempre tiene lote con área. |
| **Cosecha sin siembra** | Parcialmente | cosecharLote exige lote.puedeCosechar() (estado SEMBRADO…LISTO_PARA_COSECHA), lo que implica que antes hubo transición a SEMBRADO (por siembra). Si se crea HistorialCosecha por crearHistorialCosecha sin pasar por siembra, **sí** se puede tener “cosecha sin siembra” en datos. |
| **Rendimiento por encima de umbral** | Sí | No hay tope de tn/ha; se puede guardar cualquier rendimientoReal. |
| **Cambio de cultivo sin “limpieza”** | Sí | resetearLote o liberar ponen cultivoActual = null; no se exige una labor de “limpieza” ni anulación de labores previas. Cambiar cultivo en un lote (por otro flujo) sin cancelar labores planificadas del cultivo anterior es posible. |
| **Cultivo ACTIVO en catálogo pero sin TipoCultivo** | Sí | Cultivo.estado y TipoCultivo/EstadoLoteConfig son independientes; un cultivo puede estar ACTIVO y no tener tipo configurado para la empresa (o el tipo desactivado). |
| **Varios cultivos “activos” por Plot** | N/A | Plot tiene un solo cultivo_id y un solo cultivoActual (string); no hay lista de cultivos activos por lote. |

---

## 8. Concurrency Risk Map (Agricultural Scope)

| Área | Riesgo | Observación |
|------|--------|-------------|
| **Dos siembras simultáneas en el mismo Plot** | Lost update / estado incoherente | Si dos requests ejecutan registrarSiembra o crean labor SIEMBRA COMPLETADA para el mismo lote, ambos leen Plot, ambos escriben estado/fechaSiembra; el último save gana. No hay @Version en Plot. |
| **Dos cosechas simultáneas** | Doble cosecha o mezcla de datos | cosecharLote lee lote, crea Labor y HistorialCosecha, actualiza lote a COSECHADO. Dos llamadas concurrentes pueden pasar puedeCosechar() y ambas crear labor + historial; luego solo una actualización de Plot gana. Riesgo de doble HistorialCosecha y doble creación de inventario. |
| **Descuento de inventario** | Condición de carrera en stock | actualizarInventarioLabor llama a consumir; si dos labores consumen el mismo insumo en paralelo, el inventario puede quedar negativo si la comprobación no es atómica (depende de InventoryService). |
| **Configuración mientras hay ejecución** | Lectura de config obsoleta | Un request puede leer EstadoLoteConfig/TransicionEstadoConfig y otro modificar/eliminar entre lectura y uso; no hay versionado ni inmutabilidad. |
| **Varios usuarios actualizando Plot.estado** | Lost update | PlotService.actualizar y TransicionEstadoService/otros flujos escriben Plot sin bloqueo optimista; último write gana. |
| **Labor** | Ya identificado en spec SDD | Sin @Version en Labor; PATCH/PUT concurrentes pueden perder actualizaciones. |

**Dónde sería más crítico optimistic locking (solo identificación):** Plot (siembra, cosecha, resetear, actualizar estado), Labor (ya documentado), y opcionalmente HistorialCosecha si se abre creación concurrente.

---

## 9. Severity Table

| ID | Hallazgo | Severidad |
|----|----------|-----------|
| 1 | Plot.estado actualizable por PUT sin pasar por transición de labor | **HIGH** (deriva estado–labor) |
| 2 | Labor puede existir sin Plot (lote_id nullable) | **MEDIUM** (inconsistencia de modelo) |
| 3 | HistorialCosecha creable sin Labor COMPLETADA (crearHistorialCosecha) | **MEDIUM** (trazabilidad ciclo–labor) |
| 4 | LaborService.crearLabor no procesa insumos (TODO); posible LaborInsumo sin descuento | **HIGH** (inventario incorrecto) |
| 5 | Sin validación de solapamiento de labores en mismo lote | **MEDIUM** (coherencia temporal) |
| 6 | Sin validación de solapamiento de ciclos HistorialCosecha por lote | **MEDIUM** (coherencia biológica) |
| 7 | Configuración (EstadoLoteConfig, etc.) modificable/eliminable sin comprobar uso en Plot | **MEDIUM** (config vs ejecución) |
| 8 | Sin @Version en Plot/Labor; condiciones de carrera en siembra/cosecha/actualización | **HIGH** (riesgo de corrupción) |
| 9 | Cosecha + inventario grano no en transacción atómica; inventario puede fallar y cosecha quedar | **MEDIUM** (inconsistencia cross-módulo) |
| 10 | Egreso/Ingreso sin vínculo a Labor; trazabilidad costo–labor no garantizada | **LOW** (diseño) |
| 11 | Cultivo ACTIVO sin TipoCultivo configurado permitido | **LOW** |
| 12 | Rendimiento sin tope biológico | **LOW** |

---

## 10. One Recommended Next Hardening Step

**Recomendación única:** Centralizar la escritura de **Plot.estado** (y, si se considera necesario, de **estadoConfigurado**) en la capa de aplicación que ya implementa la autoridad de transición (TransicionEstadoService + flujos que hoy actualizan el lote tras siembra/cosecha), y **dejar de permitir la asignación directa de estado desde PlotService.actualizar**.

En concreto:

- En **PlotService.actualizar** (o equivalente PUT del lote), **no** actualizar los campos `estado` ni `estadoConfigurado` desde el DTO cuando vengan en el request; en su lugar, tratarlos como solo-lectura para ese endpoint, o rechazar el request si se envían (devolviendo 400 o ignorando el valor y dejando el estado actual).
- Mantener como únicos escritores de Plot.estado (y fechas/estadoConfigurado cuando corresponda): **TransicionEstadoService**, **SiembraService** (registrarSiembra, registrarCosecha, abandonar, limpiar), **LaborService** (asignarCultivoAlLotePorSiembra, actualizarInformacionCultivo, crearRegistroCosecha), **PlotService** solo en operaciones explícitas de “reset” (resetearLote) y **HistorialCosechaService** (liberar), sin que un “update genérico” del lote pueda sobrescribir estado.

Con esto se reduce la deriva entre estado del lote y labores/ciclos, sin cambiar esquema, sin nuevos endpoints y sin rediseño de agregados; es una contención mínima en el punto donde hoy la incoherencia es más probable (actualización directa de estado vía PUT del lote).

---

**Fin del documento.** Evaluación estructural únicamente; no se ha modificado código ni contratos.
