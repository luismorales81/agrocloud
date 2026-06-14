# Especificación de dominio: máquina de estados de Labor (SDD)

**Versión:** 1.0  
**Fecha:** 2026-01-25  
**Contexto:** Sistema de gestión agropecuaria; Labor como fuente de verdad; calendario y notificaciones derivados.  
**Objetivo:** Unificar reglas de transición de estado y eliminar la inconsistencia entre PUT y PATCH.

---

## Executive Summary

Se detectó que **PATCH /api/labores/:id** aplica una máquina de estados (transiciones válidas, `fechaRealizacion` al completar) mientras **PUT /api/labores/:id** actualiza el estado directamente sin validar transiciones ni garantizar invariantes. El frontend usa PUT al guardar la edición completa de una labor desde LaboresManagement, por lo que no se puede deprecar PUT sin romper la UI.

**Decisión de diseño:** PUT debe **delegar internamente** en la misma lógica de transiciones que PATCH: al recibir cambios de estado se aplica la máquina de estados; el resto de campos (tipo, descripción, fechas no-estado, costo, observaciones, lote, etc.) se actualizan sin validación de transición. Así se mantiene un único punto de verdad para las transiciones, se preserva compatibilidad con el frontend y no se duplican reglas.

**Resumen de fases:** (1) Especificación formal de la máquina de estados e invariantes; (2) Rediseño del contrato API (PUT = actualización completa pero estado vía transiciones); (3) Consolidación de la lógica en un único flujo reutilizable por PATCH y PUT; (4) Plan de refactor mínimo y análisis de riesgos.

---

## 1. Formal State Machine Table

### 1.1 Estados

| Estado         | Descripción breve |
|----------------|-------------------|
| PLANIFICADA    | Labor creada; pendiente de ejecución. |
| EN_PROGRESO    | Labor en curso (opcional; permite marcar “en ejecución” antes de completar). |
| COMPLETADA     | Labor finalizada; debe tener fechaRealizacion. |
| CANCELADA      | No se ejecutará; típicamente activo=false tras DELETE. |
| ANULADA        | Se ejecutó pero se anuló después (flujo formal con justificación). |

### 1.2 Matriz de transiciones permitidas

| Estado actual  | Estado destino | Permitido | Efectos laterales / notas |
|----------------|----------------|-----------|---------------------------|
| PLANIFICADA    | EN_PROGRESO    | Sí        | Ninguno. |
| PLANIFICADA    | COMPLETADA     | Sí        | **Obligatorio:** asignar `fechaRealizacion` (si no se envía: hoy). Opcional: evaluar transición de estado del lote. |
| PLANIFICADA    | CANCELADA      | Sí        | Ninguno. |
| EN_PROGRESO    | COMPLETADA     | Sí        | **Obligatorio:** asignar `fechaRealizacion` (si no se envía: hoy). Opcional: evaluar transición del lote. |
| EN_PROGRESO    | CANCELADA      | Sí        | Ninguno. |
| COMPLETADA     | —              | No        | Terminal. Solo vía anulación formal. |
| CANCELADA      | PLANIFICADA    | Sí        | Reactivación. |
| ANULADA        | —              | No        | Terminal. |

**Transiciones prohibidas (explícitas):**

- COMPLETADA → PLANIFICADA, EN_PROGRESO, CANCELADA (salvo flujo de anulación).
- ANULADA → cualquier otro estado.
- Cualquier estado → ANULADA solo mediante el endpoint de anulación (POST .../anular), no mediante PUT/PATCH de estado.

### 1.3 Guardas (reglas que deben cumplirse antes de aplicar la transición)

| Guarda | Descripción |
|--------|-------------|
| Labor activa | No se aplican transiciones si `activo == false`. |
| Reprogramar fecha | Solo si estado actual = PLANIFICADA se puede cambiar `fechaInicio` (fecha planificada). |
| Completar | Al pasar a COMPLETADA, `fechaRealizacion` es obligatoria (por defecto hoy si no se envía). |
| Anulación | Pasar a ANULADA solo vía `anularLabor` (justificación, opción de restaurar insumos). |

### 1.4 Comportamiento: soft delete vs cancelar vs anular

| Acción | Comportamiento |
|--------|----------------|
| **DELETE /api/labores/:id** (labor PLANIFICADA) | Transición implícita a CANCELADA + `activo = false`; restauración de insumos si aplica. |
| **DELETE** (labor EN_PROGRESO o COMPLETADA) | No permitido; se exige anulación formal (POST .../anular). |
| **DELETE** (labor ya CANCELADA o ANULADA) | Solo `activo = false` (soft delete). |
| **Cancelar** (PATCH/PUT estado = CANCELADA) | Solo cambia estado; no pone `activo = false` por sí solo (el frontend puede seguir mostrando “canceladas” si se desea). La eliminación de lista/calendario se hace filtrando por `activo` donde corresponda. |
| **Anular** (POST .../anular) | Estado → ANULADA; justificación obligatoria; opción de restaurar insumos; puede setear `activo = false` según política. |

Para mantener simplicidad y compatibilidad: al **cancelar** por PATCH/PUT solo se actualiza el estado a CANCELADA; el soft delete (`activo = false`) queda asociado al flujo de DELETE. No se exige cambiar `activo` en la misma operación que el cambio a CANCELADA.

---

## 2. Domain Invariants

Las siguientes condiciones deben ser verdaderas después de cada operación que modifique una Labor (crear, actualizar, anular, eliminar):

| ID | Invariante | Comentario |
|----|------------|------------|
| I1 | Si `estado == COMPLETADA` entonces `fechaRealizacion != null`. | La fecha de realización es obligatoria al completar. |
| I2 | Si `estado == PLANIFICADA` o `estado == EN_PROGRESO` entonces `fechaRealizacion` debe ser `null` (o se ignora / se limpia si se recibe en request). | Opcional: permitir dejar una fecha previa si se “reabre” desde COMPLETADA; en el diseño actual no hay reapertura desde COMPLETADA. |
| I3 | Si `activo == false` entonces `estado` debe ser uno de CANCELADA, ANULADA. | Las labores eliminadas (soft delete) no quedan en PLANIFICADA/EN_PROGRESO/COMPLETADA. |
| I4 | `fechaRealizacion` no debe ser anterior a `fechaInicio`. | Política: al validar, si se envía `fechaRealizacion` y es anterior a `fechaInicio`, rechazar o ajustar a `fechaInicio`. Recomendación: rechazar con error 400. |
| I5 | Overdue es siempre derivado: `overdue = (estado == PLANIFICADA && fechaInicio < hoy)`. No se persiste; se calcula en lectura. | Ya cumplido en el modelo actual. |
| I6 | No existen transiciones directas desde COMPLETADA o ANULADA a ningún otro estado mediante PUT/PATCH; solo vía anulación (ANULADA) o sin cambio de estado. | Evita “reabrir” una labor completada sin flujo explícito. |

**Resumen de política para fechaRealizacion:**

- Al transicionar a COMPLETADA: setear `fechaRealizacion` (valor enviado o hoy).
- Si el cliente envía `fechaRealizacion` anterior a `fechaInicio`: rechazar la petición (invariante I4).
- No persistir “overdue”; seguir calculándolo en entidad/DTO.

---

## 3. API Contract Redesign

### 3.1 Opciones evaluadas

| Opción | Descripción | Pros | Contras |
|--------|-------------|------|---------|
| A | PUT restringido a campos no-estado (tipo, descripción, fechas planificadas, costo, observaciones, lote; estado y fechaRealizacion solo vía PATCH). | Separación clara. | El frontend actual envía el objeto completo al editar (incluido estado); habría que cambiar el frontend para no enviar estado en PUT o usar solo PATCH al cambiar estado. |
| B | Deprecar PUT. | Un solo verbo para actualizar. | LaboresManagement usa PUT al guardar; deprecar rompe la edición actual sin cambios en frontend. |
| C | PUT acepta el mismo payload que hoy pero **delega en la misma lógica de transiciones** que PATCH cuando se envía cambio de estado; el resto de campos se aplican como hasta ahora. | Un solo lugar con las reglas de transición; compatibilidad con el frontend sin cambiar la llamada actual. | PUT sigue siendo “full update” en superficie; la diferencia es que el backend ya no aplica estado “a pelo”. |

### 3.2 Decisión: Opción C (PUT delega en la máquina de estados)

**Justificación SDD:**

- La **fuente de verdad** para las transiciones es la máquina de estados; no debe haber dos caminos (PUT y PATCH) que escriban estado de forma distinta.
- **Un único punto de aplicación:** tanto PUT como PATCH deben usar la misma función/método que aplica la matriz de transiciones y los efectos laterales (fechaRealizacion, transición de lote).
- **Compatibilidad:** el frontend puede seguir enviando PUT con el labor completo (incluido estado); el backend interpreta el cambio de estado como una solicitud de transición y la valida.
- **Simplicidad:** no se añaden nuevos endpoints ni se obliga a refactorizar el frontend en esta fase; solo se unifica la lógica en backend.

**Contrato resultante:**

- **PATCH /api/labores/:id:** Sin cambios semánticos. Payload parcial: `estado`, `fecha_planificada`, `fecha_realizacion`, `observaciones`. Las transiciones se validan según la matriz; `fechaRealizacion` se setea/valida según invariantes.
- **PUT /api/labores/:id:** Acepta el cuerpo completo de la labor (como hoy). Para los campos que no son estado/fechaRealizacion (tipo, descripción, fechaInicio, fechaFin, costo, observaciones, responsable, lote, etc.) se aplican directamente. Para `estado` (y si aplica `fechaRealizacion`): en lugar de asignar a pelo, se llama a la **misma lógica de transición** que usa PATCH (un método compartido que recibe estado actual, estado deseado, y fechaRealizacion opcional). Así se garantizan invariantes y se evita duplicar reglas.

---

## 4. Domain Logic Consolidation Plan

### 4.1 Dónde debe vivir la lógica de transición

- **Recomendación:** en el **servicio de aplicación** (LaborService), no en la entidad Labor.
- **Motivo:** las transiciones tienen efectos laterales (transición de estado del lote, restauración de insumos en anulación, etc.) que requieren repositorios y otros servicios; la entidad no debe depender de ellos. La entidad puede exponer **consultas** (p. ej. `puedeTransicionarA(EstadoLabor destino)` o `esTransicionPermitida(EstadoLabor destino)`) que encapsulen solo la matriz de transiciones, si se desea evitar duplicar la matriz en el servicio.

**Dos niveles posibles:**

1. **Solo en servicio:** LaborService tiene un método interno `aplicarTransicionDeEstado(Labor labor, EstadoLabor nuevoEstado, LocalDate fechaRealizacionOpcional)` que:
   - Verifica estado actual y nuevo contra la matriz.
   - Si la transición es a COMPLETADA, setea `fechaRealizacion` (o rechaza si no se puede).
   - Aplica efectos laterales (transición de lote si aplica).
   - No persiste; devuelve la entidad modificada para que el caller haga save.
2. **Entidad + servicio:** Labor tiene un método `puedeTransicionarA(EstadoLabor destino)` que devuelve boolean según la matriz (solo lectura). El servicio usa ese método y además aplica efectos laterales y asignación de `fechaRealizacion`. Esto evita tener la matriz en dos sitios (documentación + código en un solo lugar en la entidad como “reglas de negocio puras”).

Se recomienda **opción 1 (solo servicio)** para no añadir complejidad; la matriz puede vivir en un único método privado o en una pequeña clase de “reglas de transición” si se prefiere. Si más adelante se quiere que la entidad sea la única que “conozca” las transiciones permitidas, se puede extraer `puedeTransicionarA` en Labor sin efectos laterales.

### 4.2 Evitar duplicación entre PUT y PATCH

- **Método compartido:** por ejemplo `aplicarTransicionDeEstado(Labor labor, EstadoLabor nuevoEstado, LocalDate fechaRealizacionOpcional)` (y si hace falta, `actualizarFechaPlanificada(Labor labor, LocalDate nuevaFecha)` con guarda PLANIFICADA).
- **PATCH:** después de cargar la labor y validar permisos y activo, si el request trae `estado`, se llama a `aplicarTransicionDeEstado` con el estado actual y el nuevo; si trae `fecha_planificada`, se llama a la guarda de reprogramación y se actualiza `fechaInicio`. Luego se aplican observaciones y se hace save.
- **PUT:** después de cargar la labor y validar permisos, se actualizan **todos los campos no-estado** desde el payload (tipo, descripción, fechaInicio, fechaFin, costo, observaciones, lote, etc.). Para el campo `estado`: si el estado enviado es distinto del actual, se llama al **mismo** `aplicarTransicionDeEstado` en lugar de hacer `labor.setEstado(laborData.getEstado())`. Para `fechaRealizacion`: solo se actualiza si la transición a COMPLETADA ya está aplicada y el valor cumple I4 (no anterior a fechaInicio). Luego save.

Así, la regla “cómo se puede cambiar el estado” existe una sola vez.

### 4.3 Garantía de invariantes a nivel de dominio

- **I1 (COMPLETADA → fechaRealizacion no null):** dentro de `aplicarTransicionDeEstado`, al transicionar a COMPLETADA se asigna siempre `fechaRealizacion` (request o hoy).
- **I2 (PLANIFICADA/EN_PROGRESO → fechaRealizacion null):** al transicionar a PLANIFICADA (reactivación desde CANCELADA) se puede poner `fechaRealizacion = null`. En PUT, si el cliente envía estado PLANIFICADA/EN_PROGRESO y fechaRealizacion no null, se puede ignorar fechaRealizacion o limpiarla para cumplir I2.
- **I3 (activo false → CANCELADA o ANULADA):** el flujo de DELETE ya pone CANCELADA o activo=false; anulación pone ANULADA. No se permite PUT/PATCH que pongan activo=false dejando estado en PLANIFICADA/COMPLETADA; si acaso el único que puede tocar activo es DELETE o anulación.
- **I4 (fechaRealizacion >= fechaInicio):** en el método que setea `fechaRealizacion`, validar; si `fechaRealizacion != null && fechaInicio != null && fechaRealizacion.isBefore(fechaInicio)` → lanzar excepción de validación (400).
- **I5 e I6:** ya cubiertos por no persistir overdue y por restringir transiciones en la matriz.

---

## 5. Minimal Refactor Plan

### 5.1 Archivos afectados

| Archivo | Cambio |
|---------|--------|
| `LaborService.java` | (1) Extraer método `aplicarTransicionDeEstado(Labor labor, EstadoLabor nuevoEstado, LocalDate fechaRealizacionOpcional)` (y opcionalmente `actualizarFechaPlanificada` con guarda). (2) PATCH: usar ese método en lugar de lógica inline. (3) PUT: dejar de hacer `labor.setEstado(laborData.getEstado())`; si `laborData.getEstado()` != labor.getEstado(), llamar a `aplicarTransicionDeEstado`; al setear fechaRealizacion validar I4. (4) Añadir validación I4 donde se asigne fechaRealizacion. |
| `Labor.java` | Opcional: añadir `puedeTransicionarA(EstadoLabor destino)` para centralizar la matriz en la entidad y que el servicio la use. No obligatorio en el refactor mínimo. |
| Ningún otro | No se cambian contratos HTTP ni DTOs; no se tocan controladores más allá de que sigan llamando a los mismos métodos del servicio. |

### 5.2 Modificaciones concretas (resumen)

1. **LaborService**
   - Crear método privado (o paquete) que implemente la matriz de transiciones y efectos:
     - Entrada: labor, nuevoEstado, fechaRealizacion (opcional).
     - Comprobar transición permitida; si no, lanzar excepción.
     - Si nuevoEstado == COMPLETADA: validar I4 si se pasa fechaRealizacion; setear fechaRealizacion (request o LocalDate.now()).
     - Si transición a COMPLETADA desde PLANIFICADA/EN_PROGRESO: llamar a transicionEstadoService si aplica.
     - Aplicar setEstado en la labor. No hacer save dentro de este método.
   - En `actualizarParcialLabor`: reemplazar el bloque que maneja `request.getEstado()` por una llamada a este método; mantener el manejo de fecha_planificada y observaciones.
   - En `actualizarLabor`: en lugar de `labor.setEstado(laborData.getEstado())`, si hay cambio de estado llamar al mismo método de transición; para fechaRealizacion, si ya está COMPLETADA y viene en laborData, validar I4 y setear. Mantener la actualización del resto de campos desde laborData.
   - Añadir validación I4 en el único lugar donde se asigna fechaRealizacion (dentro del método de transición o en un setter validado).

2. **Extensión de la matriz en el método de transición**
   - Incluir EN_PROGRESO en la matriz: PLANIFICADA → EN_PROGRESO; EN_PROGRESO → COMPLETADA, CANCELADA. Así PUT/PATCH pueden reflejar “en progreso” si el frontend lo usa.

3. **Backward compatibility**
   - Respuesta de PUT y PATCH sigue siendo la entidad Labor (o DTO) actualizada; códigos HTTP sin cambio. El frontend que envía PUT con el mismo payload no requiere cambios; solo el backend aplica reglas consistentes.

### 5.3 No hacer (para mantener simplicidad)

- No añadir nueva capa de “domain service” separada; mantener la lógica en LaborService.
- No cambiar rutas ni verbos.
- No introducir versionado de API por esto.
- No migrar BD; no hay cambios de esquema.

---

## 6. Risk Assessment

### 6.1 Bugs que esta corrección previene

- Estado COMPLETADA con `fechaRealizacion == null` (invariante I1 rota) al editar por PUT.
- Transiciones inválidas por PUT (ej. COMPLETADA → PLANIFICADA, o CANCELADA → COMPLETADA).
- Reprogramación de labor ya COMPLETADA (cambio de fechaInicio sin guarda) vía PUT.
- Doble aplicación de reglas (una en PATCH y otra distinta en PUT) que lleve a comportamientos distintos según el verbo usado.

### 6.2 Posibles regresiones

- **Frontend que dependa de que PUT “acepte cualquier estado”:** si algún cliente enviaba COMPLETADA sin fechaRealizacion y el backend lo guardaba, a partir de ahora el backend rechazará o asignará hoy. Revisar que el frontend al editar una labor no envíe estado COMPLETADA sin fecha_realizacion si no quiere “hoy”.
- **Edición de labor ya COMPLETADA:** si el usuario solo cambia descripción u observaciones y el frontend reenvía el labor completo con estado COMPLETADA, el método de transición verá “actual == COMPLETADA, nuevo == COMPLETADA” y no hará nada (no hay transición); solo se actualizarán los otros campos. Comportamiento correcto.
- **Reactivación CANCELADA → PLANIFICADA:** ya soportada en PATCH; PUT que envíe estado PLANIFICADA para una labor cancelada debe seguir funcionando si se delega en el mismo método (transición permitida).

### 6.3 Tests que deben añadirse o actualizarse

- **Unitarios (LaborService):**
  - Transiciones permitidas: PLANIFICADA → COMPLETADA setea fechaRealizacion; PLANIFICADA → CANCELADA; CANCELADA → PLANIFICADA.
  - Transiciones rechazadas: COMPLETADA → PLANIFICADA; COMPLETADA → CANCELADA; ANULADA → cualquier otro.
  - Guarda reprogramación: intento de cambiar fechaInicio con estado COMPLETADA debe rechazarse (en PATCH; en PUT no se debe permitir cambiar fechaInicio si estado no es PLANIFICADA).
  - Invariante I4: intento de setear fechaRealizacion anterior a fechaInicio debe lanzar excepción.
  - PUT con cambio de estado: que el estado resultante y fechaRealizacion sean los mismos que si se hubiera hecho PATCH con ese estado.
- **Integración (API):**
  - PUT con estado COMPLETADA sin fechaRealizacion en body: respuesta 200 y labor con fechaRealizacion = hoy (o 400 si se decide rechazar; en la spec se recomienda asignar hoy).
  - PUT con estado COMPLETADA y fechaRealizacion anterior a fechaInicio: 400.
  - PATCH con transición inválida: 400 con mensaje de transición no permitida.

### 6.4 Migración de base de datos

- **No se requiere.** No se añaden ni modifican columnas. Opcionalmente, un script de validación (fuera del despliegue) que compruebe I1 en datos existentes (COMPLETADA → fechaRealizacion not null) y reporte filas inconsistentes para corregirlas a mano o con script único.

---

## 7. Required Tests (checklist)

- [ ] PLANIFICADA → COMPLETADA asigna fechaRealizacion (default hoy).
- [ ] PLANIFICADA → CANCELADA sin efectos laterales de fechaRealizacion.
- [ ] CANCELADA → PLANIFICADA (reactivación).
- [ ] COMPLETADA → cualquier otro estado rechazado (en PATCH y en PUT).
- [ ] Reprogramar (cambiar fechaInicio) solo permitido si estado == PLANIFICADA; si no, rechazo.
- [ ] fechaRealizacion anterior a fechaInicio → rechazo (400).
- [ ] PUT con mismo estado que actual solo actualiza otros campos (no falla).
- [ ] PUT con cambio de estado aplica la misma transición que PATCH (mismo resultado).
- [ ] Labor con activo=false: PATCH/PUT de estado rechazado (o no aplicado) según regla definida.

---

**Fin del documento de especificación.** No se debe implementar código hasta que esta especificación sea revisada y aprobada; la implementación debe seguir el plan de refactor mínimo y los tests indicados.
