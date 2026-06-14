# Revisión de endurecimiento de dominio (post-implementación)

**Fecha:** 2026-01-25  
**Alcance:** Labor como fuente de verdad; transiciones centralizadas en `LaborService.aplicarTransicionDeEstado`.  
**Principios:** Spec-Driven Development; sin rediseño, sin cambios de contrato API ni de esquema.

---

## Executive Summary

La centralización de transiciones en `LaborService` es correcta y los invariantes se respetan dentro de ese flujo. Se detectan **cuatro vías de fuga de invariantes** fuera del servicio: (1) **SiembraService** persiste labores con `estado = COMPLETADA` sin `fechaRealizacion` (I1); (2) **PlotService.resetearLote** deja labores con `activo = false` y `estado = COMPLETADA` (I3); (3) **LaborService.deleteLabor** (método alternativo) pone `activo = false` sin ajustar `estado` (I3); (4) la entidad **Labor** expone setters públicos que permiten mutar estado/activo/fechaRealizacion desde cualquier capa. Riesgo de **concurrencia**: no hay `@Version`; existe pérdida de actualización y ventana de estado obsoleto. **Overdue** se calcula con `LocalDate.now()` sin zona explícita; jobs y API pueden discrepar según hora/JVM. **Cohesión**: estado está bien encapsulado en un único método; el servicio es muy grande (God Service) y SRP está tensionado, pero dentro del alcance no se rediseña.

---

## 1. Invariant Leakage Analysis

### 1.1 Fuga I1 (COMPLETADA → fechaRealizacion != null)

**Origen:** **SiembraService** crea y persiste `Labor` con `setEstado(EstadoLabor.COMPLETADA)` sin asignar `fechaRealizacion`.

**Trayectoria:**

- `SiembraService.registrarSiembra`: `laborSiembra.setEstado(EstadoLabor.COMPLETADA)` (línea ~97), luego `laborRepository.save(laborSiembra)`. No se llama a `setFechaRealizacion`.
- `SiembraService.registrarCosecha`: `laborCosecha.setEstado(EstadoLabor.COMPLETADA)` (línea ~277), `laborRepository.save(laborCosecha)` sin `fechaRealizacion`.
- `SiembraService.abandonarCultivo`: `laborAbandono.setEstado(EstadoLabor.COMPLETADA)` (línea ~481), `laborRepository.save(laborAbandono)` sin `fechaRealizacion`.
- `SiembraService.limpiarCultivo`: `laborLimpieza.setEstado(EstadoLabor.COMPLETADA)` (línea ~521), `laborRepository.save(laborLimpieza)` sin `fechaRealizacion`.

**Contención mínima (sin rediseño):** En cada uno de esos cuatro puntos, inmediatamente después de `setEstado(EstadoLabor.COMPLETADA)`, asignar `setFechaRealizacion(fechaInicio != null ? fechaInicio : LocalDate.now())` (ya tienen `fechaInicio` o equivalente). Así se cumple I1 sin pasar por `aplicarTransicionDeEstado` ni cambiar flujos ni API.

---

### 1.2 Fuga I3 (activo == false → estado CANCELADA o ANULADA)

**Origen A – PlotService.resetearLote**

- Primer bucle (líneas ~229–237): labores con estado distinto de COMPLETADA/CANCELADA/ANULADA se pasan a CANCELADA y `activo = false` → I3 respetado.
- Segundo bucle (líneas ~239–244): para toda labor con `activo == null || activo == true` se hace `setActivo(false)` y `save`. Aquí se incluyen labores que ya están en COMPLETADA (y que no se tocaron en el primer bucle). Resultado: **activo = false y estado = COMPLETADA** → violación de I3.

**Contención mínima:** En el segundo bucle, antes de `labor.setActivo(false)`, si `labor.getEstado() == COMPLETADA || labor.getEstado() == EN_PROGRESO`, no cambiar solo `activo`; opciones: (a) no poner `activo = false` para esas (dejarlas activas) o (b) tratarlas como “archivadas” y forzar estado a ANULADA (o a un estado terminal definido) antes de `setActivo(false)`. La opción (a) es la más pequeña: en el segundo bucle, solo hacer `setActivo(false)` cuando `estado` sea CANCELADA o ANULADA (por ejemplo, las que ya se marcaron en el primer bucle). Así I3 se mantiene.

**Origen B – LaborService.deleteLabor (método alternativo)**

- Método `deleteLabor(Long id, User user)` (aprox. líneas 1788–1823): hace `labor.setActivo(false)` y `laborRepository.save(labor)` sin modificar `estado`. Si la labor estaba en PLANIFICADA, EN_PROGRESO o COMPLETADA, queda **activo = false** con **estado distinto de CANCELADA/ANULADA** → I3 violado.

**Contención mínima:** No usar este método para “eliminar” labores en sentido de dominio. O bien: antes de `setActivo(false)`, si `estado` no es ya CANCELADA ni ANULADA, delegar en la lógica existente de eliminación/anulación (por ejemplo llamar a `eliminarLabor(id, usuario)` para PLANIFICADA o exigir anulación para EN_PROGRESO/COMPLETADA), de modo que el único camino que pone `activo = false` sea el que ya garantiza estado CANCELADA o ANULADA.

---

### 1.3 Deserialización y mutación directa

- **Controller PUT:** Recibe `@RequestBody Labor`. Jackson rellena el objeto (incluidos `estado`, `fechaRealizacion`, `activo`). Ese objeto se usa solo como DTO en `actualizarLabor(id, labor, user)`: el servicio carga la entidad desde BD y aplica transiciones y reglas; no se persiste el cuerpo del request directamente. No hay bypass por deserialización en este endpoint.
- **Entidad Labor:** Setters públicos `setEstado`, `setActivo`, `setFechaRealizacion` permiten que cualquier código que tenga una referencia a `Labor` viole invariantes. Las fugas observadas (SiembraService, PlotService, deleteLabor) son precisamente usos de esos setters + `laborRepository.save` fuera de la autoridad de transición.
- **Constructor / builder:** No hay builder; el constructor con argumentos inicializa estado PLANIFICADA y activo true. No se detectan fugas por construcción.
- **Test factories:** En tests se crean `Labor` y se llama a setters; no persisten en BD de producción. No se considera fuga operativa; opcional documentar que el único camino válido en producción es vía LaborService.

**Contención adicional (opcional):** No cambiar firma de API ni DTOs. Si se quisiera endurecer solo a nivel de entidad, la opción mínima sería documentar que los setters de estado/activo/fechaRealizacion son “solo para LaborService y flujos que garantizan invariantes”; no eliminar setters (JPA y deserialización los usan). La contención fuerte sigue siendo corregir los tres orígenes anteriores (SiembraService, PlotService, deleteLabor).

---

## 2. Concurrency Risk Analysis

- **Dos PATCH simultáneos sobre el mismo id:** Ambos hacen `findById`, luego uno aplica transición y `save`, el otro aplica sobre el mismo estado leído y `save`. El último `save` gana; el primero puede quedar perdido (lost update). Ejemplo: ambos leen PLANIFICADA; uno pasa a COMPLETADA con fechaRealizacion; el otro pasa a CANCELADA; según el orden de guardado, el resultado final puede ser CANCELADA (y se pierde la realización) o COMPLETADA (y se pierde la cancelación).
- **Un request reprograma y otro completa:** Mismo patrón: ambos leen la misma versión; uno cambia `fechaInicio` (si PLANIFICADA), otro cambia estado a COMPLETADA; el último `save` sobrescribe al anterior. Riesgo de combinaciones inconsistentes (por ejemplo fecha de realización con fecha planificada de otro día).
- **Ventana de estado obsoleto:** Entre `findById` y `save` hay una ventana donde otro hilo puede haber cambiado estado o activo; no se relee antes de guardar, por lo que no hay detección de modificación concurrente.

**Recomendación de protección mínima (solo descripción, sin implementar):**

- Añadir **optimistic locking** con `@Version` en la entidad Labor.
- **Punto de inserción entidad:** En `Labor.java`, añadir campo `private Long version;` anotado con `@Version` (JPA).
- **Punto de inserción BD:** Migración (Flyway/Liquibase) que añada columna `version` (BIGINT, default 0) en `cultivo_labores`.
- Comportamiento: en cada `save`, JPA incluirá `WHERE version = :current` y actualizará `version = version + 1`. Si otro hilo ya actualizó la fila, el update no afectará filas y JPA lanzará una excepción de entidad obsoleta; el servicio puede capturarla y devolver 409 Conflict o reintentar según política.
- No se requiere bloqueo pesimista ni rediseño de API; solo manejo de la excepción en el controller/servicio y posible reintento o mensaje claro al cliente.

---

## 3. Soft Delete Consistency Check

- **Invariante:** Si `activo == false` entonces `estado` debe ser CANCELADA o ANULADA.
- **¿Se puede cambiar `activo` de forma independiente?** Sí: en PlotService (segundo bucle) y en LaborService.deleteLabor se hace `setActivo(false)` sin garantizar estado CANCELADA/ANULADA. Ya cubierto en la sección de fugas I3.
- **¿Existe un camino con estado != CANCELADA/ANULADA y activo = false?** Sí: (1) resetearLote segundo bucle (COMPLETADA + activo false), (2) deleteLabor (cualquier estado + activo false).
- **Idempotencia de la eliminación:**  
  - `eliminarLabor` (flujo principal): PLANIFICADA → CANCELADA + activo false; EN_PROGRESO/COMPLETADA → exige anulación; CANCELADA/ANULADA → solo activo false. Llamar dos veces con la misma labor: la segunda falla por “La labor ya está eliminada” (comprobación `!labor.getActivo()`). Comportamiento idempotente desde el punto de vista del cliente (mismo resultado tras la primera llamada).  
  - `deleteLabor` (alternativo): no comprueba si ya está inactiva; cada llamada hace `setActivo(false)` y save. Idempotente en resultado (activo queda false) pero refuerza la violación de I3 si el estado no era CANCELADA/ANULADA.

**Contención mínima:** Corregir deleteLabor y el segundo bucle de resetearLote como se indicó en la sección 1, de modo que el único camino que pone `activo = false` sea el que ya deja `estado` en CANCELADA o ANULADA (o no marque como inactivas las labores que no cumplan eso).

---

## 4. Overdue Derivation Audit

- **Definición:** Overdue es derivado: `overdue = (estado == PLANIFICADA && fechaInicio < hoy)` (I5; no se persiste).
- **Cálculo:** En `Labor.isVencida()` / `getOverdue()` se usa `LocalDate.now()` (zona por defecto de la JVM). Calendario y listados usan `labor.isVencida()` en el momento de la petición.
- **Timezone:** No hay zona explícita; “hoy” depende del reloj y de la zona por defecto del proceso (servidor/job). Si el servidor está en UTC y el usuario en Argentina, “hoy” en API y en job puede no coincidir con el “hoy” del usuario a medianoche local.
- **Jobs vs API:** NotificacionLaborService usa `LocalDate.now()` para “hoy” y “mañana” al buscar labores próximas. Un job que corre a las 00:00 UTC y una petición a las 23:00 UTC del día anterior en Argentina pueden usar “hoy” distinto; una labor con fechaInicio = “hoy” en Argentina podría no considerarse “hoy” en el job o al revés.
- **Riesgo:** Pequeña discrepancia entre “overdue” / “próxima” en API y en notificaciones; no invalida la regla de negocio “overdue es derivado”, pero puede generar confusión o notificaciones en día distinto al esperado.

**Estrategia de estandarización (solo recomendación, sin implementar):**

- Introducir un único proveedor de “fecha de referencia” (por ejemplo `Supplier<LocalDate> fechaHoy` o un bean `Clock`/`LocalDate now()`) inyectado en LaborService, NotificacionLaborService y en cualquier lugar que use “hoy” para labores.
- Opcionalmente parametrizar por zona (por ejemplo zona de la empresa o del usuario) para que “hoy” sea “hoy en esa zona”.
- Mismo valor de “hoy” en API, jobs y cálculos de overdue; así se evita la deriva entre componentes.

---

## 5. Domain Cohesion Evaluation

| Criterio | Valoración | Comentario |
|----------|------------|------------|
| LaborService como “God Service” | ⚠ Needs Attention | El servicio es muy grande (miles de líneas): CRUD, transiciones, filtros, costos, eliminación, anulación, reportes, integración con lotes/cultivos. La lógica de estado está concentrada en un único método, pero el resto de responsabilidades hace que el servicio sea difícil de mantener y testear. |
| Cohesión de la lógica de estado | ✔ Safe | `aplicarTransicionDeEstado` es el único punto que cambia estado y aplica I1–I4; PUT y PATCH delegan en él. No hay duplicación de matriz de transiciones ni de efectos (fechaRealizacion, transición de lote). |
| Duplicación de lógica de negocio | ⚠ Needs Attention | La creación de labores “completadas” en SiembraService (siembra, cosecha, abandono, limpieza) no usa la autoridad de transición ni garantiza I1; es lógica de “labor completada” duplicada y con fuga. PlotService y deleteLabor duplican el efecto “desactivar” sin garantizar I3. |
| SRP (Single Responsibility) | ⚠ Needs Attention | LaborService tiene muchas razones de cambio: transiciones, permisos, costos, insumos, integración con lotes, reportes, eliminación, anulación. SRP está tensionado; dentro del alcance acordado no se propone dividir el servicio. |
| Riesgo estructural a medio plazo | ❗ Structural Risk | Si se siguen añadiendo casos de uso (más tipos de labor, más integraciones) en el mismo servicio sin contener las fugas actuales, la mezcla de “autoridad de estado” y “puntos que ignoran invariantes” puede generar más inconsistencias y bugs difíciles de rastrear. La contención de fugas (sección 1) y la opcional protección por `@Version` reducen este riesgo. |

---

## 6. Recommended Next Hardening Step (ONE)

**Paso único recomendado:** Corregir las fugas de invariantes en los tres orígenes identificados (SiembraService, PlotService, LaborService.deleteLabor), con los cambios mínimos descritos en la sección 1, sin rediseño ni cambio de API:

1. **SiembraService:** En los cuatro flujos que crean labor con `estado = COMPLETADA`, asignar también `setFechaRealizacion(fechaInicio != null ? fechaInicio : LocalDate.now())` antes del primer `save`.
2. **PlotService.resetearLote:** En el segundo bucle, solo aplicar `setActivo(false)` (y observaciones/save) a labores cuyo `estado` sea CANCELADA o ANULADA (por ejemplo las ya tratadas en el primer bucle), de modo que nunca quede `activo = false` con estado COMPLETADA o EN_PROGRESO.
3. **LaborService.deleteLabor:** Dejar de usar este método como camino principal de “eliminación”, o hacer que delegue en `eliminarLabor` (o en la misma lógica que garantiza estado CANCELADA/ANULADA antes de poner `activo = false`), de forma que I3 quede siempre satisfecho.

Con esto se cierra la posibilidad de que el dominio quede en estados que violen I1 o I3 por caminos que evaden `aplicarTransicionDeEstado` y el flujo de eliminación/anulación. Los demás endurecimientos (optimistic locking, estandarización de “hoy” para overdue) pueden planificarse en pasos posteriores.

---

**Fin del documento.** No se ha modificado código ni contratos; solo análisis y recomendaciones de contención mínima.
