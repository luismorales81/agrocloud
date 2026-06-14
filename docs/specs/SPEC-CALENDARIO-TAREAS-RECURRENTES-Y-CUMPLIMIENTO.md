# SPEC: Calendario — tareas recurrentes y marcado de cumplimiento

**Versión:** 1.1 · **Estado:** **Aprobada e implementada** (backend Flyway `V1_138`, API `/api/calendario/tareas-recurrentes`, fusión en `/api/calendario/eventos`, UI `CalendarioDashboard`)

**Marco:** Spec Driven Development (`/cursor-development-rules.md`). Esta SPEC describe el comportamiento deseado y el gap respecto al sistema actual. **No debe generarse código de producto** hasta aprobación de esta SPEC y, en su caso, del diseño técnico subsiguiente.

---

## 1. Verificación del estado actual (sistema)

### 1.1 Calendario general (`GET /api/calendario/eventos`)

| Fuente | Qué muestra | Recurrencia | “¿Cumplida?” en UI |
|--------|-------------|---------------|-------------------|
| **Labores** (`cultivo_labores` / entidad `Labor`) | Una fila por labor; `fecha_inicio` dentro del rango | **No existe** campo ni regla de repetición (diaria/semanal/mensual/anual) | El estado de negocio es `EstadoLabor` (`PLANIFICADA`, `COMPLETADA`, etc.); el calendario expone `estado`, `fechaRealizacion`, `overdue`. **No** hay un checkbox único “cumplida” separado del flujo de labor; completar implica transición de labor (fuera del alcance mínimo de “check” del usuario). |
| **Cosechas** | Fecha esperada por lote | No aplica | No aplica |
| **Recordatorios** (`recordatorios` / `Recordatorio`) | Una fila por **una** `fecha` | **No existe** recurrencia: cada recordatorio es un evento puntual | Existe `completado` y API `PATCH /recordatorios/{id}/completar`. El front (`CalendarioDashboard`) ya consume `completado` para recordatorios. |

### 1.2 Calendario porcinos (`GET /api/calendario/porcinos`)

Combina eventos calculados del módulo porcinos y recordatorios generales. **Misma limitación:** sin series recurrentes definidas en dominio para “tareas de calendario” genéricas.

### 1.3 Conclusión del gap

- **No** es posible hoy definir una **tarea** del calendario con **repetición** diaria, semanal, mensual o anual de forma nativa.
- El **check de cumplido** existe **solo** para **recordatorios** puntuales (`completado`), no para cada ocurrencia de una serie.
- Las **labores** no deben confundirse con “tareas checklist del calendario”: tienen ciclo de vida agropecuario propio; forzar un checkbox simple sin alinear a `LaborService` podría duplicar semántica o romper consistencia.

---

## 2. Problema

El usuario necesita en el **calendario**:

1. **Crear tareas** (obligaciones recordatorias operativas) con **frecuencia de repetición**: **diaria**, **semanal**, **mensual** o **anual**.
2. Para **cada aparición** de la tarea en el calendario (cada fecha/ocurrencia), un **control tipo checkbox** para indicar si **se cumplió o no**.

---

## 3. Objetivos

1. Permitir **alta** de una tarea recurrente con tipo de repetición acotado a los cuatro valores indicados.
2. Que el calendario **muestre** la tarea en **todas las fechas** del rango visible que correspondan a la regla de repetición.
3. Persistir el **cumplimiento por ocurrencia** (fecha concreta de la serie), de modo que al cambiar de mes se conserve qué días se marcaron cumplidos.
4. Mantener coherencia con **multiempresa / usuario** según reglas ya usadas en recordatorios y calendario.

---

## 4. Alcance funcional (producto)

### 4.1 Creación / edición

- Campos mínimos sugeridos: **título**, **descripción** (opcional), **fecha de inicio** de la serie, **tipo de repetición** (`DIARIA`, `SEMANAL`, `MENSUAL`, `ANUAL`), **fecha de fin de la serie** (opcional; si falta, definir límite — ver §6).
- Opcional en **SEMANAL**: día(s) de la semana (si no se define, asumir **mismo día de la semana** que la fecha de inicio).
- Opcional en **MENSUAL / ANUAL**: anclaje (ej. “día 31” → comportamiento en meses cortos — ver §6).

### 4.2 Visualización en calendario

- Cada ocurrencia se muestra como evento de tipo distingible (p. ej. `TAREA_RECURRENTE` o `TAREA_CALENDARIO`) para no colisionar semánticamente con `LABOR` ni con recordatorios porcinos.
- En la vista **día** o **detalle** de la ocurrencia: **checkbox** “Cumplida” / “Pendiente” que persiste al marcar/desmarcar (definir si se permite desmarcar — ver §6).

### 4.3 Alcance de calendarios

| Vista | ¿Incluye tareas recurrentes? |
|-------|-------------------------------|
| Calendario general (`/calendario/eventos` y UI principal de cultivos) | **Sí** (objetivo por defecto de esta SPEC) |
| Calendario porcinos | **Pendiente de decisión** (§6): puede reutilizar el mismo motor con filtro por empresa/usuario o quedar fuera del v1 para reducir riesgo. |

---

## 5. Reglas de negocio propuestas (cerradas donde se pudo)

| ID | Regla |
|----|--------|
| R1 | Una **serie** tiene un **dueño** (`usuario_id`) alineado a recordatorios actuales. |
| R2 | El **cumplimiento** es por **par (serie, fecha_ocurrencia)** — una fila o registro equivalente por fecha marcada. |
| R3 | Los tipos de repetición permitidos son **exactamente cuatro**: diaria, semanal, mensual, anual (nombres en API/UI en castellano según convención del proyecto). |
| R4 | Ocurrencias **futuras** pueden mostrarse como pendientes; **no** es obligatorio permitir marcar cumplida una fecha futura (recomendación: **solo hasta hoy** o permitir libre — decidir en §6). |
| R5 | Eliminar o desactivar la **serie** debe ocultar futuras ocurrencias y conservar o anonimizar cumplimientos históricos (decisión §6). |

---

## 6. Preguntas abiertas / decisión de producto (no bloquean redacción de SPEC; sí al diseño técnico)

1. **Límite temporal:** ¿hasta cuándo se generan ocurrencias si no hay `fecha_fin`? (ej. máximo 24 meses vista, o solo materializar al consultar rango).
2. **Mensual con día 29–31 y anual 29 feb:** ¿último día del mes válido, se omite, o se desplaza?
3. **Semanal:** ¿un solo día por semana o multi-select de días?
4. **Desmarcar cumplida:** ¿permitido para cualquier rol o solo administrador?
5. **Relación con `Labor`:** ¿las tareas recurrentes son **independientes** de labores de cultivo (recomendado en v1), o debe existir acción “convertir ocurrencia en labor”?
6. **Calendario porcinos:** ¿mismo modelo en v1 o exclusión explícita?

---

## 7. Riesgos arquitectónicos

| Riesgo | Mitigación sugerida |
|--------|---------------------|
| **Materializar** miles de filas por serie “sin fin” | Preferir **serie + ocurrencias materializadas bajo techo** o **serie + tabla de cumplimiento** y cálculo de fechas en servicio al consultar rango. |
| Duplicar concepto con `Recordatorio` | O bien extender recordatorios (rompe modelo actual de una fecha), o **nueva entidad** “serie / tarea calendario” en Core con migración Flyway clara. |
| Performance del endpoint `/calendario/eventos` | Paginación por rango ya existe; la expansión de series debe ser **O(rango)** acotado y con índices por `usuario_id` y fechas. |
| Permisos | Alinear a `RecordatorioController` / empresa activa si aplica `X-Company-Id`. |

---

## 8. Criterios de aceptación (verificables)

- [ ] El usuario puede **crear** una tarea con repetición **diaria, semanal, mensual o anual**.
- [ ] El calendario muestra la tarea en **cada fecha** del mes consultado que cumpla la regla.
- [ ] Cada ocurrencia visible tiene **checkbox** (o equivalente accesible) de cumplido **persistente** al recargar.
- [ ] Cambiar de mes conserva los cumplimientos ya marcados en fechas pasadas del mismo mes u otros meses.
- [ ] Documentación de API y textos de UI en **castellano**, según reglas del proyecto.

---

## 9. Relación con otras SPEC

- `SPEC-WIZARD-CULTIVO-CALENDARIO-LABORES-V2.md`: labores planificadas por cultivo/lote; **no sustituye** esta SPEC (distinto concepto: labores de cultivo vs tareas recurrentes personales/operativas).
- Calendario SDD existente en código (`CalendarioController` comentarios “Spec SDD”): esta SPEC **extiende** el contrato de eventos con un nuevo tipo o estructura acordada en diseño técnico.

---

## 10. Próximos pasos (SDD)

1. **Revisión y aprobación explícita** de esta SPEC por el solicitante/producto.  
2. **Diseño técnico** (modelo de datos, endpoints, cambios en `CalendarioController` y front) — **aprobación aparte**.  
3. Implementación (Flyway, servicios, UI) solo tras lo anterior.

---

## 11. Resumen para el solicitante

**Verificado:** hoy **no** se puede generar repetición diaria/semanal/mensual/anual en calendario; el check de cumplido existe solo en **recordatorios** de **una** fecha. Esta SPEC define el comportamiento pedido y deja explícitas las decisiones pendientes (§6) antes de codificar.

**Acción requerida:** indicar **“Aprobada”** (con respuestas a §6 si podés) para pasar a diseño técnico e implementación. aprobada.
