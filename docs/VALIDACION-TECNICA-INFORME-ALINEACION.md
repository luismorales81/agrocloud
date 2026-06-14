# Validación técnica del informe de alineación Frontend–Backend

**Objetivo:** Confirmar si el informe `INFORME-ALINEACION-FRONTEND-BACKEND.md` es correcto según el código actual y detectar desalineaciones reales no mencionadas.  
**Metodología:** Lectura directa de controladores, entidades, DTOs, `apiEndpoints.ts`, `apiServices.ts` y componentes del frontend. Sin modificación de código.

---

## 1. Backend – Contratos reales expuestos

### 1.1 PlotController y LoteController

| Verificación | Resultado | Referencia en código |
|--------------|-----------|----------------------|
| Qué devuelven | **Entidad Plot** en todos los endpoints de listado/obtención/creación/actualización | `PlotController.java`: `ResponseEntity<List<Plot>>`, `ResponseEntity<Plot>`, `ResponseEntity.ok(lotes)` / `savedLote` / `updatedLote` |
| PlotDTO | No se usa en estos controladores; el contrato es la entidad Plot serializada por Jackson | `LoteController.java`, `PlotController.java`: no importan ni usan PlotDTO |
| Estructura JSON efectiva | `id`, `nombre`, `areaHectareas`, `estado`, `estadoConfigurado`, `liberadoParaSiembra`, `version`, `campoId` (vía `@JsonProperty("campoId")`), `cultivoActual`, `tipoSuelo`, `activo`, `fechaCreacion`, `fechaActualizacion`, etc. No se expone `campo` ni `user` ni `cultivo` (tienen `@JsonIgnore`) | `Plot.java`: líneas 92-114, 206-207 |

✔ **Confirmado:** El informe es correcto: devolución de entidad Plot, campos serializados y ausencia de PlotDTO en estos controladores.

---

### 1.2 LaborController

| Verificación | Resultado | Referencia en código |
|--------------|-----------|----------------------|
| GET sin parámetros | Devuelve **List&lt;LaborDetalladoDTO&gt;** (no entidad Labor) | `LaborController.java` líneas 66-72: si no hay filtros, `laborService.getLaboresDetalladasByUser(user)` → `ResponseEntity.ok(labores)` con DTOs |
| GET con al menos un filtro | Devuelve **List&lt;Labor&gt;** (entidad) | `LaborController.java` líneas 66-69: `laborService.getLaboresConFiltros(...)` → `ResponseEntity.ok(labores)` con entidades |
| GET /{id} | Devuelve **LaborDetalladoDTO** | `LaborController.java` líneas 82-94 |
| POST crear | Devuelve **Labor** (entidad) | `LaborController.java` línea 112 |
| PATCH /{id} | Devuelve **Labor** (entidad) | `LaborController.java` línea 146 |
| POST confirmar-siembra / confirmar-cosecha | Devuelve **Map** (success, mensaje, laborId) | `LaborController.java` líneas 278-284, 306-312 |

**LaborDetalladoDTO (JSON):** camelCase: `loteId`, `loteNombre`, `fechaRealizacion`, `cultivoId`, `overdue`, `fechaInicio`, `fechaFin`, `estado`, etc. **No** incluye propiedad `lote` (objeto).

**Labor (entidad) cuando se serializa:** Incluye relación `lote` (objeto Plot) porque en `Labor.java` el campo `lote` **no** tiene `@JsonIgnore` (líneas 70-72). Por tanto, al devolver `List<Labor>`, cada ítem tiene `lote: { id, nombre, areaHectareas, ... }`.

✔ **Confirmado:** El informe describe correctamente que el listado puede ser entidad o DTO según filtros; la diferencia exacta (sin filtros = DTO, con filtros = entidad) queda validada.

---

### 1.3 HistorialCosechaController

| Verificación | Resultado | Referencia en código |
|--------------|-----------|----------------------|
| GET /lote/{loteId}, GET (listado) | **List&lt;CosechaDTO&gt;** | `HistorialCosechaController.java` líneas 36-54, 59-76: `CosechaDTO::new` |
| GET /lote/{loteId}/ultima | **Entidad HistorialCosecha** | `HistorialCosechaController.java` líneas 83-102 |
| PUT /lote/{loteId}/liberar | Cuerpo de respuesta: **String** ("Lote liberado exitosamente...") | `HistorialCosechaController.java` líneas 107-134: `ResponseEntity.ok("Lote liberado...")` |
| PUT /lote/{loteId}/liberar-forzado | Cuerpo de respuesta: **String** | `HistorialCosechaController.java` líneas 140-165 |

✔ **Confirmado:** El informe es correcto. El frontend que hace `response.data` en liberar/liberar-forzado recibe un string; si no parsea JSON en ese endpoint, es coherente con el contrato.

---

### 1.4 EstadoLoteController

| Verificación | Resultado | Referencia en código |
|--------------|-----------|----------------------|
| Rutas y tipos de respuesta | Proponer → RespuestaCambioEstado; Confirmar/Cancelar → Map; Resumen → Map (conteos, requierenAtencion, lotesRequierenAtencion); Listos-para-siembra/cosecha, por-estado → List&lt;Plot&gt; | `EstadoLoteController.java` líneas 36-210 |

✔ **Confirmado:** El informe describe correctamente los endpoints y tipos; en el frontend no hay referencias a `estados-lotes` en `apiEndpoints.ts` ni en los componentes revisados.

---

### 1.5 Campos expuestos que el frontend no contempla

- **Plot:** `liberadoParaSiembra`, `version`, `tipoUso`, `fechaUltimoCambioEstado`, `motivoCambioEstado`, `fechaSiembra`, `fechaCosechaEsperada`, `fechaCosechaReal`, `rendimientoEsperado`, `rendimientoReal`. El informe ya indica que el frontend no usa `liberadoParaSiembra` ni `version`; el resto son opcionales para la UI actual.
- **LaborDetalladoDTO:** Incluye `loteCampo`, `loteSuperficie`, `loteCultivo`, `costoMaquinaria`, `costoManoObra`, `costoInsumos`, listas `maquinarias`, `manoObra`, `insumosUsados`. El frontend en LaboresManagement sí mapea varios de estos; no se detecta conflicto de contrato.

---

## 2. Frontend – Consumo real

### 2.1 apiEndpoints.ts

- **LOTES:** `BASE: '/v1/lotes'`, `LISTAR`, `LISTAR_CULTIVO`, `OBTENER`, `CREAR`, `ACTUALIZAR`, `ELIMINAR`, `SEMBRAR`, `COSECHAR`, `INFO_COSECHA`, `ABANDONAR`, `CONVERTIR_FORRAJE`, `RESETEAR`. No hay entradas para `/api/lotes` (LoteController); el frontend usa solo `/v1/lotes` (PlotController). ✔ Coherente.
- **LABORES:** `LISTAR: '/labores'`, `LISTAR_CON_FILTROS: '/labores'`, `OBTENER`, `CREAR`, `ACTUALIZAR`, `ACTUALIZAR_PARCIAL`, `ELIMINAR`, `ANULAR`, `TAREAS_DISPONIBLES`, `TAREAS_DISPONIBLES_POR_LOTE`. **No hay** entradas para `POST /labores/{id}/confirmar-siembra` ni `confirmar-cosecha`. ✔ Coincide con el informe.
- **HISTORIAL_COSECHAS:** Solo `LIBERAR(loteId)` y `LIBERAR_FORZADO(loteId)`. No hay endpoints para GET por lote, GET ultima, puede-liberar, dias-descanso, etc.; si no se usan en la UI, no es error.

### 2.2 apiServices.ts

- **lotesService:** listar, listarCultivo, obtener, crear, actualizar, eliminar, sembrar, resetear, cosechar, obtenerInfoCosecha, abandonar, convertirForraje. Usan `API_ENDPOINTS.LOTES.*`. ✔
- **laboresService:** listar → `API_ENDPOINTS.LABORES.LISTAR` (GET sin params); listarConFiltros → arma query con fecha_desde, fecha_hasta, lote_id, estado, overdue. **No hay** métodos `confirmarSiembra(laborId, body)` ni `confirmarCosecha(laborId, body)`. ✔
- **cosechasService:** liberarLote(loteId) → PUT `HISTORIAL_COSECHAS.LIBERAR(loteId)`; liberarLoteForzado(loteId, justificacion) → PUT `HISTORIAL_COSECHAS.LIBERAR_FORZADO(loteId)` con body `{ justificacion }`. ✔

### 2.3 Búsqueda de llamadas a estados-lotes y confirmar-siembra/cosecha

- **Grep** en el frontend por `estados-lotes`, `confirmar-siembra`, `confirmar-cosecha`: **sin coincidencias**. ✔ El informe es correcto: el frontend no consume esos endpoints.

### 2.4 Endpoints definidos pero no utilizados / usados pero no definidos

- **Definidos y usados:** LOTES.*, LABORES.LISTAR / LISTAR_CON_FILTROS / OBTENER / CREAR / ACTUALIZAR_PARCIAL / TAREAS_DISPONIBLES_POR_LOTE, HISTORIAL_COSECHAS.LIBERAR / LIBERAR_FORZADO.
- **Definidos y no usados (en revisión):** LABORES.ANULAR, ACTUALIZAR_COSTO — pueden usarse en otros componentes.
- **Backend existen pero no en apiEndpoints:** `/api/estados-lotes/*`, `POST /labores/{id}/confirmar-siembra`, `POST /labores/{id}/confirmar-cosecha`. El informe ya lo indica.

---

## 3. Validación de modelos (Plot y Labor)

### 3.1 Plot – LotesManagement

- **Backend JSON (Plot):** `id`, `nombre`, `areaHectareas`, `estado`, `estadoConfigurado`, `campoId`, `cultivoActual`, `descripcion`, `tipoSuelo`, etc. Sin `campo` (JsonIgnore).
- **Mapeo en LotesManagement:** `superficie: lote.areaHectareas || 0`, `estado: lote.estado || 'DISPONIBLE'`, `campo_id: lote.campoId || lote.campo?.id`, `estadoConfigurado: lote.estadoConfigurado`, `cultivo` desde `lote.cultivoActual` o `lote.cultivo` (el backend no envía `cultivo`; solo `cultivoActual` tiene valor). ✔ Correcto.
- **Interface Lote (frontend):** No declara `liberadoParaSiembra` ni `version`; no es necesario para la UI actual. ✔

### 3.2 Labor – LaboresManagement y LotesManagement

- **Origen de datos labores en LaboresManagement:** `offlineService.getLabores()` (que en práctica puede usar el mismo endpoint que el backend). El mapeo usa `labor.loteId`, `labor.loteNombre`, `labor.fechaRealizacion`, `labor.overdue`, `labor.estado`, etc. — coincide con **LaborDetalladoDTO** (camelCase). ✔
- **Origen de datos labores en LotesManagement:** `laboresService.listar()` → GET `/api/labores` **sin parámetros** → backend devuelve **List&lt;LaborDetalladoDTO&gt;** (no List&lt;Labor&gt;). Cada ítem tiene `loteId`, `loteNombre` y **no** tiene propiedad `lote` (objeto).

**Inconsistencia detectada (no mencionada en el informe original):**

En `LotesManagement.tsx`, en `cargarLabores` (aprox. líneas 358-373), el mapeo de cada labor hace:

```ts
lote_nombre: labor.lote?.nombre || '',
```

Como la API devuelve **LaborDetalladoDTO**, la propiedad es `loteNombre`, no `lote`. Por tanto `labor.lote` es **undefined** y `lote_nombre` queda siempre **''** (string vacío) para todas las labores mostradas en ese componente.

- **Archivo:** `agrogestion-frontend/src/components/LotesManagement.tsx`
- **Corrección sugerida:** usar `lote_nombre: labor.loteNombre || labor.lote?.nombre || ''` para ser compatibles con DTO y con entidad (por si en el futuro se usara listado con filtros que devuelve Labor con `lote`).

### 3.3 Labor – Resumen de formas de respuesta

| Llamada frontend | Endpoint | Respuesta backend | Campos relevantes para lote |
|------------------|----------|-------------------|------------------------------|
| laboresService.listar() | GET /api/labores (sin query) | List&lt;LaborDetalladoDTO&gt; | loteId, loteNombre (no `lote`) |
| laboresService.listarConFiltros(...) | GET /api/labores?... | List&lt;Labor&gt; | lote (objeto Plot) |
| laboresService.obtener(id) | GET /api/labores/{id} | LaborDetalladoDTO | loteId, loteNombre (no `lote`) |

Por tanto, en **LotesManagement** el uso de `labor.lote?.nombre` es incorrecto para la respuesta actual de `listar()`.

### 3.4 CosechasManagement

- Interfaces locales `Cosecha`, `Lote`, `Cultivo`; uso de `cosechasService.liberarLote` y `liberarLoteForzado`. No se revisó en detalle el resto del flujo de datos; el informe indica alineación en liberar/liberar forzado. ✔

### 3.5 Posibles undefined silenciosos

- **LotesManagement – labores:** `lote_nombre` siempre '' por el uso de `labor.lote?.nombre` con DTO. ⚠ Inconsistencia real.
- **LotesManagement – lotes:** `lote.campo` no viene en el JSON; se usa `lote.campoId || lote.campo?.id`. ✔ Cubierto.
- **Plot – cultivo:** Backend no envía `cultivo`; el código usa `cultivoActual` y luego `lote.cultivo`; en la práctica solo `cultivoActual` aporta valor. ✔ Sin error.

---

## 4. Estado derivado

- **Backend:** En el código se verifica que `EstadoLoteUpdater.recalcularEstado(loteId)` se invoca desde:
  - `PlotService`
  - `LaborService` (tras crear/actualizar/anular labores, confirmar siembra/cosecha, etc.)
  - `HistorialCosechaService` (liberar / liberar forzado)
  - `SiembraService` (sembrar, cosechar, abandonar, limpiar, etc.)
  - `TransicionEstadoService`
  - `CrearLotePorcinoAdapter`

✔ **Confirmado:** El estado del lote se recalcula automáticamente en backend; no depende de que el frontend llame a “confirmar cambio” manual.

- **Frontend:** No se encontraron llamadas a `/api/estados-lotes` ni a `confirmar-siembra` / `confirmar-cosecha`. El flujo actual (crear labor, sembrar/cosechar desde PlotController, liberar lote) depende del backend para derivar el estado. ✔

- **Endpoints deprecados:** El informe indica que `EstadoLoteService.confirmarCambioEstado` y lógica asociada a confirmar-siembra/cosecha están restringidos/deprecados. No se comprobó en detalle el código del servicio; el informe se da por válido en que el frontend no usa esos endpoints y por tanto no hay flujo roto. ✔

---

## 5. Testing

- **Backend:** Se intentó ejecutar tests con `mvn test -Dtest="*EstadoDerivado*"` en el backend; en el entorno usado el comando no completó (PowerShell / entorno). Se recomienda ejecutar localmente `mvn test` o los tests del módulo agrícola y documentar el resultado. El informe original indica que en una sesión anterior los tests pasaron con el bean EMF y el ajuste de `cuit`. ✔ Informar que la validación técnica no pudo re-ejecutar los tests en este entorno.
- **Frontend:** En `package.json` no hay script `test` ni dependencias Jest/Vitest; no hay archivos `*.test.ts`/`*.test.tsx`. ✔ El informe es correcto: no hay tests de frontend.
- **Cobertura sobre contratos API:** No existe suite de tests que valide respuestas JSON de lotes/labores/historial-cosechas en frontend ni en backend de integración contra el contrato. ✔ Coincide con el informe.

---

## 6. Resultado estructurado

### ✔ Confirmaciones validadas por código

1. PlotController y LoteController devuelven entidad Plot; no usan PlotDTO; JSON con `areaHectareas`, `estado`, `estadoConfigurado`, `campoId`, etc.
2. LaborController: sin filtros devuelve List&lt;LaborDetalladoDTO&gt;, con filtros List&lt;Labor&gt;; PATCH devuelve Labor; confirmar-siembra/cosecha devuelven Map.
3. HistorialCosechaController: liberar/liberar-forzado con PUT; respuestas en texto; frontend tiene endpoints y servicios alineados.
4. EstadoLoteController: rutas y tipos como en el informe; frontend no consume `/api/estados-lotes` ni confirmar-siembra/cosecha.
5. Estado derivado: EstadoLoteUpdater.recalcularEstado usado en varios servicios; frontend no depende de confirmación manual de estado.
6. apiEndpoints.ts y apiServices.ts: no definen ni usan estados-lotes ni confirmar-siembra/cosecha; liberar/liberar forzado sí definidos y usados.
7. Mapeo de Plot en LotesManagement (areaHectareas → superficie, estado, estadoConfigurado, campoId) correcto.
8. Mapeo de labores en LaboresManagement (loteId, loteNombre, fechaRealizacion, overdue, etc.) correcto para LaborDetalladoDTO.
9. Sin tests de frontend; sin cobertura de contratos API en los tests revisados.

### ⚠ Inconsistencias reales encontradas

1. **LotesManagement – nombre del lote en labores:** En `cargarLabores` se asigna `lote_nombre: labor.lote?.nombre || ''`. La API usada (`laboresService.listar()`) devuelve **LaborDetalladoDTO**, que tiene `loteNombre` y no `lote`. Resultado: `lote_nombre` queda siempre vacío.  
   **Archivo:** `agrogestion-frontend/src/components/LotesManagement.tsx` (mapeo en `cargarLabores`).  
   **Corrección sugerida:** `lote_nombre: labor.loteNombre || labor.lote?.nombre || ''`.

### 🔍 Riesgos potenciales

1. **Doble contrato de labores:** El listado sin filtros devuelve DTO (loteId, loteNombre); con filtros devuelve entidad (lote). Cualquier componente que asuma siempre `labor.lote` o siempre `labor.loteNombre` sin contemplar ambos puede mostrar datos vacíos o fallar en un escenario.
2. **Respuesta PUT liberar:** El backend devuelve un string; si en el futuro el frontend esperara un objeto (p. ej. `{ success, message }`), habría que cambiar el backend o adaptar el cliente.
3. **Tipos locales dispersos:** Interfaces Lote/Labor definidas en cada componente; si el backend añade campos obligatorios o cambia nombres, hay que actualizar varios archivos y es fácil dejar uno desincronizado.

### 🛠 Recomendaciones concretas

1. **Corregir LotesManagement:** En el mapeo de labores, usar `labor.loteNombre || labor.lote?.nombre || ''` para compatibilidad con DTO y entidad.
2. **Documentar en código o en API:** Dejar explícito que GET /labores sin params devuelve LaborDetalladoDTO (sin `lote`) y con params devuelve Labor (con `lote`), para que los mapeos en frontend contemplen ambos.
3. **Tipos compartidos:** Introducir interfaces TypeScript (p. ej. en `src/types/`) para respuestas de lotes y labores (Plot, LaborDetalladoDTO, Labor con lote opcional) y usarlas en servicios y componentes.
4. **Tests:** Ejecutar `mvn test` en backend en entorno local y documentar; valorar añadir Vitest (o similar) en frontend y tests de integración que validen estructura de respuestas de lotes/labores/liberar.

### 🧪 Estado real de los tests

- **Backend:** Tests del módulo agrícola referidos en el informe; en esta validación no se pudo re-ejecutarlos en el entorno usado. Se recomienda ejecución local y documentar resultado.
- **Frontend:** No hay script ni dependencias de tests; no hay tests de frontend.
- **Contratos API:** No hay tests automáticos que comprueben la estructura JSON de Plot, Labor/LaborDetalladoDTO o respuestas de historial-cosechas.

---

## 7. Conclusión sobre el informe original

El informe de alineación es **en su mayoría correcto** y las conclusiones (alineación en lotes, labores, liberar lote; ausencia de consumo de estados-lotes y confirmar-siembra/cosecha; estado derivado en backend) se confirman leyendo el código.

**Corrección respecto al informe:** Hay una **desalineación no mencionada**: en **LotesManagement**, el mapeo de labores usa `labor.lote?.nombre` para `lote_nombre`, pero la API de listado devuelve **LaborDetalladoDTO**, que expone `loteNombre` y no `lote`, por lo que el nombre del lote en la lista de labores de ese componente queda siempre vacío. Se recomienda corregir como se indica en el apartado 6.

No se ha modificado código; esta validación se limita a diagnóstico y referencias a archivos y líneas.
