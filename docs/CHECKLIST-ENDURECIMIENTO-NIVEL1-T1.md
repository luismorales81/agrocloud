# Checklist — Endurecimiento Módulo Agrícola (Nivel 1 + T1)

**Spec:** `docs/SPEC-ENDURECIMIENTO-MODULO-AGRICOLA.md`  
**Análisis previo:** `docs/ANALISIS-PREVIO-NIVEL1-T1.md`

---

## 1. Optimistic locking (Plot y Labor)

| Item | Estado | Detalle |
|------|--------|---------|
| Campo `version` en Plot | ✅ | `agrogestion-backend/.../cultivos/domain/Plot.java`: `@Version` + `private Long version = 0L` |
| Campo `version` en Labor | ✅ | `agrogestion-backend/.../model/entity/Labor.java`: `@Version` + `private Long version = 0L` |
| Migración SQL | ✅ | `V1_126__Add_version_Plot_Labor.sql`: `ALTER TABLE cultivo_lotes ADD COLUMN version BIGINT NOT NULL DEFAULT 0` y análogo para `cultivo_labores` |

---

## 2. Respuesta 409 ante conflicto de concurrencia

| Item | Estado | Detalle |
|------|--------|---------|
| Handler global | ✅ | `GlobalExceptionHandler.java`: `@ExceptionHandler({ OptimisticLockException.class, ObjectOptimisticLockingFailureException.class })` → `HttpStatus.CONFLICT` (409), mensaje de recurso modificado por otro usuario |

---

## 3. Transacción atómica cosecha–inventario

| Item | Estado | Detalle |
|------|--------|---------|
| Sin try/catch que trague excepción | ✅ | `SiembraService.cosecharLote`: llamada directa a `inventarioGranoService.crearInventarioDesdeCosecha(...)`; si falla, la excepción propaga y la transacción hace rollback |
| Orden en cosecharLote | ✅ | Labor COSECHA → HistorialCosecha → InventarioGrano → actualizar Plot → plotRepository.save (todo en la misma transacción @Transactional de SiembraService) |

---

## 4. Centralizar escritura de Plot.estado (PUT no modifica estado)

| Item | Estado | Detalle |
|------|--------|---------|
| PlotService.updateLote | ✅ | No asigna `estado` ni `estadoConfigurado` desde `loteData`; solo actualiza nombre, descripción, área, tipoSuelo, activo, campo. Comentario en código: "Estado y estadoConfigurado no se modifican por PUT" |
| Resto de escritores | ✅ | Solo flujos de negocio modifican estado: SiembraService, LaborService, TransicionEstadoService, HistorialCosechaService, PlotService.resetearLote, CrearLotePorcinoAdapter, EstadoLoteService (según análisis previo) |

---

## 5. Validación T1 (no dos SIEMBRA ni dos COSECHA solapadas por lote)

| Item | Estado | Detalle |
|------|--------|---------|
| Repositorio | ✅ | `LaborRepository.findLaboresActivasSolapadas(loteId, tipo, fechaInicioMin, fechaFinMax, excluirId)` |
| Servicio | ✅ | `LaborService.validarSolapamientoT1(loteId, tipo, fechaInicio, fechaFin, laborIdExcluir)` — solo para SIEMBRA y COSECHA; lanza `IllegalArgumentException` si hay solapadas |
| Crear labor | ✅ | `LaborService.crearLabor`: llama a `validarSolapamientoT1` antes de save cuando tipo es SIEMBRA o COSECHA |
| Crear labor desde request | ✅ | `LaborService.crearLaborDesdeRequest` / flujos que terminan en save: validación aplicada en los puntos que crean labores SIEMBRA/COSECHA |
| Confirmar siembra/cosecha | ✅ | `LaborService.confirmarLaborSiembra` y `confirmarLaborCosecha`: llaman a `validarSolapamientoT1` antes de guardar |
| Actualizar labor | ✅ | `LaborService.actualizarLabor` y `actualizarParcialLabor`: llaman a `validarSolapamientoT1` con `labor.getId()` como excluir al cambiar fechas |
| SiembraService.registrarSiembra | ✅ | Llama a `laborService.validarSolapamientoT1(lote.getId(), TipoLabor.SIEMBRA, ...)` antes de guardar labor de siembra |
| SiembraService.cosecharLote | ✅ | Llama a `laborService.validarSolapamientoT1(lote.getId(), TipoLabor.COSECHA, ...)` antes de guardar labor de cosecha |

---

## 6. Contratos API y esquema

| Item | Estado |
|------|--------|
| Rutas y DTOs sin cambios (salvo uso de `version` donde corresponda) | ✅ |
| Esquema: único cambio es columna `version` en `cultivo_lotes` y `cultivo_labores` | ✅ |

---

## 7. Archivos modificados / involucrados

- **Análisis:** `docs/ANALISIS-PREVIO-NIVEL1-T1.md` (nuevo)
- **Entidades:** `Plot.java` (version), `Labor.java` (version)
- **Migración:** `V1_126__Add_version_Plot_Labor.sql`
- **Config:** `GlobalExceptionHandler.java` (409 optimistic lock)
- **Servicios:** `PlotService.java` (updateLote sin estado), `SiembraService.java` (cosecha atómica, T1 en registrarSiembra y cosecharLote), `LaborService.java` (validarSolapamientoT1 e integración en crear/actualizar/confirmar)
- **Repositorio:** `LaborRepository.java` (findLaboresActivasSolapadas)

---

**Estado final:** Nivel 1 completo + T1 implementados según spec. Listo para pruebas (concurrencia, cosecha con fallo de inventario, PUT lote sin estado, creación/edición de labores SIEMBRA/COSECHA solapadas).
