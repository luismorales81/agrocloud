# Informe de alineación Frontend – Backend (cambios recientes)

**Objetivo:** Verificar si los cambios recientes del backend (módulo agrícola, estado derivado, entidades y endpoints) están correctamente reflejados en el frontend.  
**Alcance:** Endpoints modificados o afectados, DTOs/contratos, modelos en frontend, consumo de API, validaciones y tests.  
**Restricción:** Solo análisis e informe; no se ha modificado código.

---

## 1. Resumen ejecutivo

| Aspecto | Estado | Notas |
|---------|--------|--------|
| **Lotes (Plot)** | Alineado | Frontend mapea `areaHectareas` → `superficie`, `estado`, `estadoConfigurado`, `campoId`. No usa `liberadoParaSiembra` ni `version` (no necesario para la UI actual). |
| **Labores** | Alineado | Frontend usa `fechaRealizacion`, `overdue`, `cultivoId`; listado y PATCH coinciden con backend. |
| **Historial cosechas / liberar** | Alineado | Endpoints `liberar` y `liberar-forzado` existen en frontend y coinciden con backend. |
| **Estados de lote (API)** | Parcial | Backend expone `/api/estados-lotes` (proponer, confirmar, cancelar, resumen, listos-para-siembra, etc.). En frontend **no existen** esos endpoints en `apiEndpoints.ts` ni servicios que los llamen. Si la UI no usa ese flujo (estado ahora derivado), es coherente; si se quisiera usar, faltarían. |
| **Confirmar siembra/cosecha (labores)** | Parcial | Backend tiene `POST /labores/{id}/confirmar-siembra` y `POST /labores/{id}/confirmar-cosecha` (deprecados en servicio). En frontend **no hay** entradas en `apiEndpoints` ni métodos en `laboresService` para esos endpoints. |

**Conclusión:** Para el uso actual del frontend (listar/crear/editar lotes, labores, liberar lote, mostrar estado), la alineación es correcta. Hay endpoints de backend (estados-lotes, confirmar-siembra/cosecha) que el frontend no consume; si no se usan en la UI, no hay error; si se quisieran usar, habría que añadirlos y tener en cuenta que confirmar cambio estado está restringido/deprecado en backend.

---

## 2. Endpoints y contratos del backend revisados

### 2.1 Lotes (Plot)

- **Backend:** `PlotController` (`/api/v1/lotes`), `LoteController` (`/api/lotes`). Respuesta: entidad **Plot** (Jackson serializa la entidad).
- **Campos relevantes en JSON:** `id`, `nombre`, `areaHectareas`, `estado` (enum → string), `estadoConfigurado` (objeto con id, nombre, color, icono), `liberadoParaSiembra`, `version`, `campoId` (vía `@JsonProperty("campoId")`), `cultivoActual`, `tipoSuelo`, `activo`, `fechaCreacion`, `fechaActualizacion`, etc. No se expone `campo` (está `@JsonIgnore`).
- **PlotDTO:** En backend existe pero los controllers de lotes devuelven la **entidad** Plot, no PlotDTO. El contrato efectivo es el de la entidad.

### 2.2 Estados de lote

- **Backend:** `EstadoLoteController` (`/api/estados-lotes`).
  - `POST /proponer-cambio` → ProponerCambioEstadoRequest → RespuestaCambioEstado
  - `POST /confirmar-cambio` → ConfirmacionCambioEstado → Map (success, mensaje, loteId, nuevoEstado)
  - `POST /cancelar-cambio` → Map (loteId, estadoActual)
  - `GET /resumen` → Map (conteos por estado, requierenAtencion, lotesRequierenAtencion)
  - `GET /listos-para-siembra` → List&lt;Plot&gt;
  - `GET /listos-para-cosecha` → List&lt;Plot&gt;
  - `GET /por-estado/{estado}` → List&lt;Plot&gt;
- **Frontend:** En `apiEndpoints.ts` **no** hay ninguna clave para `estados-lotes`. No hay llamadas a estos endpoints desde los componentes revisados.

### 2.3 Labores

- **Backend:** `LaborController` (`/api/labores`).
  - Listado con filtros (fecha_desde, fecha_hasta, lote_id, estado, overdue) → lista de labores (entidad o DTO según método).
  - `PATCH /{id}` → actualización parcial (estado, fecha_planificada, fecha_realizacion, observaciones).
  - `POST /siembra` → crear labor siembra con confirmación (respuesta RespuestaCambioEstado).
  - `POST /cosecha` → crear labor cosecha con confirmación.
  - `POST /{laborId}/confirmar-siembra` → ConfirmacionCambioEstado → Map (success, mensaje, laborId).
  - `POST /{laborId}/confirmar-cosecha` → ConfirmacionCambioEstado → Map (success, mensaje, laborId).
- **Confirmación:** En backend, `EstadoLoteService.confirmarCambioEstado` y `LaborService.confirmarLaborCosecha` están **deprecados** y restringidos (p. ej. no permiten forzar SEMBRADO/COSECHADO); el estado del lote se deriva vía `EstadoLoteUpdater.recalcularEstado(...)`.

### 2.4 Historial de cosechas y liberar lote

- **Backend:** `HistorialCosechaController` (`/api/historial-cosechas`, `/api/v1/cosechas`).
  - `PUT /lote/{loteId}/liberar` → liberar lote para nueva siembra.
  - `PUT /lote/{loteId}/liberar-forzado` → body `{ justificacion }`.
  - `GET /lote/{loteId}`, `GET`, `GET /lote/{loteId}/ultima`, etc.
- **Frontend:** `API_ENDPOINTS.HISTORIAL_COSECHAS.LIBERAR(loteId)` y `LIBERAR_FORZADO(loteId)`; `cosechasService.liberarLote(loteId)` y `liberarLoteForzado(loteId, justificacion)`. Rutas y métodos alineados con el backend.

---

## 3. Frontend: modelos y consumo

### 3.1 Lotes (LotesManagement.tsx)

- **Interface local `Lote`:** `id`, `nombre`, `superficie`, `cultivo`, `campo_id`, `estado`, `descripcion`, `tipoSuelo`, `estadoConfigurado?: { id, nombre, color, icono }`.
- **Mapeo desde API:** `superficie: lote.areaHectareas || 0`, `estado: lote.estado || 'DISPONIBLE'`, `estadoConfigurado: lote.estadoConfigurado`, `campo_id: lote.campoId || lote.campo?.id`. El backend envía `areaHectareas` y `campoId`; el mapeo es correcto.
- **No usados en la interfaz:** `liberadoParaSiembra`, `version`. El backend sí los envía (entidad Plot); el frontend no los declara ni usa. No genera error; si en el futuro la UI mostrara “liberado para siembra” o usara bloqueo optimista, habría que añadirlos al tipo y a la UI.

### 3.2 Labores (LaboresManagement.tsx)

- Se usan `fechaRealizacion`, `overdue`, `cultivoId` en el mapeo y en la UI. Coinciden con `Labor`/`LaborDetalladoDTO` (fecha_realizacion, overdue, cultivo_id).
- **Confirmar siembra/cosecha:** No hay llamadas a `confirmar-siembra` ni `confirmar-cosecha` en los componentes revisados. La creación de labores se hace por los flujos habituales (crear labor, etc.); el backend recalcula el estado del lote. No hay desalineación de contrato en lo que el frontend hace hoy.

### 3.3 Cosechas y liberar (CosechasManagement.tsx)

- Se llama a `cosechasService.liberarLote(loteId)` y `liberarLoteForzado(loteId, justificacion)`. Coinciden con el backend.

### 3.4 Estado de lote en UI

- **EstadoLoteDisplay**, **ResetLoteModal**, **LotesManagement:** reciben `estado` (string) y `estadoConfigurado` (objeto con nombre, color, icono). El backend los envía en la entidad Plot. Alineado.

---

## 4. Validaciones específicas

- **Formularios:** Los formularios de lotes envían `areaHectareas` (mapeado desde `formData.superficie`); el backend Plot espera `areaHectareas`. Correcto.
- **Tablas/listados:** Lotes muestran nombre, superficie, cultivo, estado (y estadoConfigurado cuando existe). La estructura de datos recibida coincide con el uso.
- **TypeScript:** No se han revisado todos los archivos; en los revisados no aparecen propiedades obsoletas en los tipos de Lote/Labor que choquen con el backend. Los tipos locales son más estrechos (menos campos) que el JSON; no hay conflicto.
- **Posibles errores silenciosos:** Si en algún lugar se usara `lote.campo` en lugar de `lote.campoId`, podría ser `undefined` porque el backend no envía `campo`. En el mapeo actual se usa `lote.campoId || lote.campo?.id`, por lo que está cubierto.

---

## 5. Testing

- **Backend:** Se ejecutaron tests de integración del módulo agrícola (`EstadoDerivadoIntegracionTest`, etc.) en una sesión anterior; con el bean EMF que propaga `JpaProperties` y el ajuste de `cuit` en el setUp, los tests pasan.
- **Frontend:** En `package.json` no hay script de tests (no Jest ni Vitest). No hay archivos `*.test.ts`/`*.test.tsx`. No es posible ejecutar tests de frontend ni validar contratos vía tests automáticos.

**Sugerencias de tests (si se añade soporte):**
- Tests de integración o E2E que comprueben: listado de lotes (estructura de respuesta), listado de labores con filtros, liberar lote y liberar forzado.
- Tests unitarios de los mapeos API → modelo local (p. ej. `lote.areaHectareas` → `superficie`, `lote.estado`, `lote.campoId`).

---

## 6. Inconsistencias y recomendaciones

### 6.1 Sin impacto con el uso actual

- **Plot: `liberadoParaSiembra` y `version`:** Backend los envía; frontend no los usa. No es inconsistencia; si más adelante se muestra “liberado para siembra” o se implementa edición con bloqueo optimista, añadir al tipo e UI.
- **Confirmar cambio de estado / confirmar labor cosecha deprecados:** El frontend no llama a esos endpoints; el estado se deriva en backend. No requiere cambio en frontend por ahora.

### 6.2 Opcionales (si se quisiera usar la API de estados-lotes)

- **Falta de endpoints en frontend para `/api/estados-lotes`:** Si la UI debiera mostrar “lotes listos para siembra”, “resumen por estado” o “proponer/confirmar cambio” desde una pantalla específica, habría que:
  - Añadir en `apiEndpoints.ts` una sección `ESTADOS_LOTES` con las rutas (listos-para-siembra, resumen, por-estado, etc.).
  - Añadir en `apiServices.ts` métodos que llamen a esos endpoints.
  - Tener en cuenta que “confirmar cambio” está restringido/deprecado para SEMBRADO/COSECHADO.

### 6.3 Contratos y tipos (recomendación de mejora)

- **Tipos TypeScript centralizados:** Hoy los tipos de Lote/Labor están definidos localmente en componentes. Sería útil tener tipos o interfaces compartidos (p. ej. en `src/types/`) que reflejen el contrato del backend (incluyendo `liberadoParaSiembra` y `version` como opcionales) para evitar errores al extender la UI.

---

## 7. Conclusión

- **Alineación con el uso actual:** El frontend está **alineado** con el backend para:
  - Lotes (listado, creación, edición, estado, estadoConfigurado, superficie/areaHectareas, campoId).
  - Labores (listado, filtros, fechaRealizacion, overdue, cultivoId, PATCH parcial).
  - Historial de cosechas y liberación de lote (liberar y liberar forzado).

- **Endpoints no consumidos:** La API de estados de lote (`/api/estados-lotes`) y los endpoints de confirmar siembra/cosecha de labores no están en el frontend; con el modelo de estado derivado actual, no es necesario consumirlos para el flujo existente.

- **Acciones sugeridas (solo si se requiere):**
  1. Añadir endpoints y servicios para `/api/estados-lotes` si se implementa una pantalla que los use.
  2. Introducir tipos compartidos para Lote/Labor que reflejen el contrato del backend.
  3. Añadir suite de tests en frontend (p. ej. Vitest) y, si aplica, tests de integración/E2E para los flujos críticos de lotes y labores.

No se ha modificado código; el informe se limita a diagnóstico y propuestas.
