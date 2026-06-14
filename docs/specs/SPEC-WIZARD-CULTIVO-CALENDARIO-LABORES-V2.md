# SPEC: Wizard IA cultivos — Calendario de labores, ubicación, clima y rendimientos (v2)

**Versión:** 2.1 · **Estado:** **Aprobada** (aclaraciones del producto incorporadas el 2026-05-03)

**Antecedente:** `SPEC-WIZARD-CONFIGURACION-CULTIVO.md` (v1.0, aprobada) cubre propuesta de estados/transiciones/tareas por tipo de cultivo y persistencia de `PlantillaLabor` sin acoplar lote, fecha absoluta ni clima. Esta v2 **extiende** el wizard hacia planificación operativa en un lote concreto y la **materialización del esquema de estados/transiciones** asistida por IA para el contexto del lote.

---

## 1. Problema

Tras generar la configuración del cultivo, el usuario necesita **pasar a la acción** en forma **ordenada paso a paso**: elegir un **cultivo ya registrado** y un **lote disponible** (con **ubicación** tomada del **campo** del lote), disponer de un **esquema de estados y transiciones** coherente con ayuda de la IA, ver un **calendario de labores** con fechas sugeridas según **clima** y el cultivo, **editar** fechas y recursos, **aceptar** para agendar **labores planificadas** en el calendario, y ver **estimación de rendimiento** apoyada en los **datos del cultivo seleccionado** y el clima. Se requiere **leyenda** de propósito del asistente.

## 2. Objetivos de la funcionalidad (leyenda — borrador de texto UI)

Texto orientativo para el wizard (editable en implementación):

> Este asistente te guía **paso a paso** a **planificar el ciclo del cultivo en un lote real**: elegís un **cultivo guardado** y un **lote**; la **ubicación** se toma del **campo** al que pertenece el lote. Con ayuda de la IA se **define o completa el esquema de estados y transiciones** asociado al tipo de cultivo de ese lote/cultivo. Luego se proponen **labores y fechas** según **clima** (cuando hay datos) y tu configuración; podés **editar fechas y recursos** y, al confirmar, las labores quedan **planificadas** en el **Calendario** hasta que las gestionés manualmente. El **rendimiento** mostrado se basa en los **datos del cultivo que seleccionaste** más el escenario climático; es **orientativo**, no una garantía de resultado.

## 3. Alcance funcional

### 3.0 Experiencia: paso a paso

La UI debe ser un **asistente secuencial** (un paso visible a la vez o equivalente con validación entre pasos), mínimo orden lógico:

1. **Leyenda / objetivo** del wizard.  
2. **Selección de cultivo** guardado (empresa).  
3. **Selección de lote** disponible.  
4. **Resumen de contexto:** ubicación desde **campo del lote**, datos de cultivo relevantes (incl. lo necesario para rendimiento — ver §3.5).  
5. **Estados y transiciones (IA):** vista previa, edición si aplica, confirmación parcial o acumulativa hacia persistencia (ver §3.6).  
6. **Clima + calendario de labores:** vista previa IA, edición de fechas y recursos.  
7. **Confirmación final:** persistir labores `PLANIFICADA` y dejar cerrado el esquema de estados/transiciones acordado en el paso 5 (si aún no estaba persistido).

(El diseño técnico puede agrupar pasos en pantallas compuestas siempre que el flujo respete este orden.)

### 3.1 Entrada — referencias obligatorias

| Paso | Contenido |
|------|-----------|
| A | **Cultivo de referencia:** selector de `Cultivo` existente de la empresa (API de cultivos guardados). Opcional: prellenar desde wizard v1 si existe flujo encadenado. |
| B | **Lote:** selector de lotes **disponibles** para la empresa (criterio operativo por defecto en diseño técnico: `tipo_uso = CULTIVO` y estados que permitan planificar; documentar exclusiones p. ej. `PORCINO`). |
| C | **Ubicación:** **solo** desde el **campo** al que pertenece el lote (`Plot` → `Campo`): coordenadas o localidad que ya existan en el modelo del campo. **No** se exige paso adicional de mapa salvo que falten datos; en ese caso mostrar advertencia y fallback (región texto o clima deshabilitado). |

### 3.2 Contexto agroclimático

- Pronóstico/clima usando coordenadas u otra clave derivada del **campo** del lote (`WeatherSimpleController` o servicio unificado).
- IA recibe: cultivo, lote, ventana temporal del ciclo, resumen climático.
- **Fallback** si no hay clima: fechas por **día relativo a fecha de referencia** (definir en diseño técnico: `fechaSiembra` del lote vs fecha elegida por usuario — pendiente menor si no se decide antes).

### 3.3 Salida — calendario de labores

- Lista o vista calendario: `tipoLabor`, fechas sugeridas, recursos sugeridos; **editables** fecha (inicio/fin según `Labor`) y recursos alineados a `CrearLaborRequest` / capacidades actuales del front.

### 3.4 Confirmación — calendario del sistema

- Crear `Labor` en estado **`PLANIFICADA`** únicamente; **no** pasan a `EN_PROGRESO` ni `COMPLETADA` por el wizard — **confirmado por producto.**
- Transaccionalidad e idempotencia: definir en diseño técnico (p. ej. clave de idempotencia por confirmación).

### 3.5 Rendimientos estimados

- **Fuente de detalle:** los atributos del **cultivo seleccionado** (rendimiento esperado, unidad, notas u otros campos que el modelo `Cultivo` ya exponga o deba exponer). La IA combina eso con el **resumen climático a futuro** para un texto/rango **orientativo** en la respuesta de vista previa.
- **Disclaimer** en UI: estimación no vinculante.

### 3.6 Estados y transiciones del lote (IA) — **nuevo requisito explícito**

- Con cultivo y lote elegidos, el asistente debe permitir **generar o completar con IA** el **esquema de estados, transiciones y tareas por estado** que aplicará el ciclo en ese contexto, **reutilizando la semántica** de `SPEC-WIZARD-CONFIGURACION-CULTIVO.md` / `ServicioWizardCultivoPersistencia` (entidades `TipoCultivo` / `EstadoLoteConfig` / `TransicionEstadoConfig` / `TareaPorEstadoConfig` según modelo actual).
- **Persistencia:** al confirmar el paso correspondiente (o la confirmación final, según diseño técnico), debe quedar el tipo de cultivo vinculado al lote/cultivo de forma que el **lote** use la configuración (`tipo_cultivo_id` / `estado_configurado_id` / reglas ya existentes en dominio). Si el tipo ya tiene plantilla global, la IA puede **proponer ajustes** o **rellenar vacíos**; el usuario revisa antes de confirmar.
- **Orden:** este bloque **antes** o **en paralelo controlado** respecto al calendario de labores, de modo que las labores sugeridas respeten estados/tareas habilitadas (coherencia con v1).

## 4. Relación con código existente

| Componente | Uso |
|------------|-----|
| `WizardCultivoScreen` (o wizard nuevo por flujo) | Pasos secuenciales UI. |
| `ServicioWizardCultivoIa` / `ConstruirPromptsWizard` | Prompts y JSON ampliados: lote, cultivo, campo/ubicación, clima, **y bloque estados/transiciones**. |
| `ServicioWizardCultivoPersistencia` | Persistencia del esquema de configuración (reutilización directa o factor común). |
| `LaborService` / `crearLaborDesdeRequest` | Alta masiva de labores `PLANIFICADA`. |
| `WeatherSimpleController` | Clima por coordenadas del campo. |
| `Plot`, `Campo`, `Cultivo`, `PlantillaLabor` | Enlaces y datos de rendimiento del cultivo. |

## 5. Reglas de negocio cerradas (producto)

| Tema | Decisión |
|------|----------|
| UX | **Paso a paso** (asistente secuencial). |
| Rendimiento | **Detalle desde el cultivo seleccionado** + IA/clima para el texto/rango orientativo. |
| Estado de labores creadas | **Siempre `PLANIFICADA`** hasta acción manual del usuario. |
| Ubicación | **Campo del lote**; sin paso mapa obligatorio. |
| Estados/transiciones del lote | **Crear / completar esquema con IA** y persistir según modelo existente, alineado a v1. |

## 6. Reglas aún para diseño técnico (no bloquean aprobación SPEC)

1. Definición exacta de **lote disponible** (filtros por estado de lote y cultivo activo).  
2. **Fecha de anclaje** del calendario (siembra en lote vs fecha usuario).  
3. **Idempotencia** y límites de filas por confirmación.  
4. **Recursos** en wizard: nivel mínimo vs paridad con alta manual de labor.

## 7. Contrato API (borrador — detalle en diseño técnico)

- Endpoints separados o flujo único: p. ej. `plan-lote/contexto`, `plan-lote/estados-vista-previa`, `plan-lote/estados-confirmar`, `plan-lote/labores-vista-previa`, `plan-lote/labores-confirmar`, o menos granular si se prefiere transacción única final.
- Headers: JWT + `X-Company-Id`; módulo `crops`.

## 8. Criterios de aceptación

- [ ] Flujo **paso a paso** con leyenda al inicio.  
- [ ] Sin cultivo y lote seleccionados no se avanza a IA de estados ni de labores.  
- [ ] Ubicación mostrada desde **campo** del lote o advertencia si faltan datos.  
- [ ] Paso de **estados/transiciones/tareas** con IA persistido de forma coherente con el **lote/cultivo/tipo**.  
- [ ] Calendario de labores editable; confirmación crea labores **planificadas** visibles en calendario.  
- [ ] **Rendimiento** alimentado por datos del **cultivo seleccionado** + clima + disclaimer.  
- [ ] Fallback si **clima** no disponible.

## 9. Riesgos

- Complejidad de **dos** confirmaciones (estados vs labores) vs una sola: mitigar con UX clara y transacción global opcional.  
- Coherencia IA entre **estados** y **labores**: validación server-side contra enums y plantillas.

## 10. Aclaraciones del solicitante (archivo histórico)

| # | Pregunta | Respuesta acordada |
|---|----------|-------------------|
| 1 | ¿Flujo en una sola pantalla o pasos? | **Paso a paso.** |
| 2 | ¿Unidad/detalle de rendimiento? | **En el cultivo a seleccionar** (modelo/datos del cultivo). |
| 3 | ¿Estado de labores al agendar? | **Sí, siempre `PLANIFICADA`.** |
| 4 | ¿Ubicación? | **Campo al que pertenece el lote** (sin mapa obligatorio). |

**Adicional:** crear **esquema de transiciones y estados** del lote **con ayuda de la IA** (§3.6).

---

**Siguiente paso (SDD):** diseño técnico (DTO, secuencia, pantallas, transacciones) → implementación.

---

## 11. Implementación inicial (2026-05-03)

- **Backend:** `WizardPlanLoteCultivoController` bajo `/api/wizard/cultivo/plan-lote` — `contexto`, `estados-vista-previa`, `estados-confirmar`, `labores-vista-previa`, `labores-confirmar`. Servicio `ServicioWizardPlanLoteCultivo`; persistencia ampliada en `ServicioWizardCultivoPersistencia.confirmarPropuestaYVincularAlLote` (tipo + estados + plantillas y vínculo lote/cultivo/estado inicial). Listado de cultivos por empresa: `CultivoRepository.findByEmpresa_IdAndActivoTrueOrderByNombreAsc`.
- **Frontend:** ruta `/cultivos/wizard-plan-lote`, menú «Wizard plan por lote (IA)», pantalla paso a paso con leyenda, selección cultivo/lote, contexto, JSON de estados (IA), confirmación, tabla de labores editable y alta en calendario como `PLANIFICADA`.
- **Pendiente de evolución:** integración explícita de pronóstico meteorológico en el prompt de estados/rendimiento; selectores ricos de recursos (maquinaria/insumos) en la tabla de labores; idempotencia al confirmar labores.
