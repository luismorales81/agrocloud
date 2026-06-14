# Análisis previo — Implementación Nivel 1 + T1

**Objetivo:** Listar archivos y métodos impactados antes de codificar.

---

## 1. Escrituras de Plot (setEstado / setEstadoConfigurado / plotRepository.save)

| Archivo | Método / contexto | Acción |
|---------|-------------------|--------|
| **LaborService.java** | asignarCultivoAlLotePorSiembra | setEstadoConfigurado, setEstado(SEMBRADO), plotRepository.save (882, 886) |
| **LaborService.java** | actualizarInformacionCultivo | setFechaSiembra, setCultivoActual, plotRepository.save (952) |
| **LaborService.java** | crearRegistroCosecha | setFechaCosechaReal, plotRepository.save (965) |
| **SiembraService.java** | registrarSiembra | setEstadoConfigurado, setEstado(SEMBRADO), setFechaSiembra, plotRepository.save (203-245) |
| **SiembraService.java** | cosecharLote | setEstado(COSECHADO), setFechaCosechaReal, plotRepository.save (451-454) |
| **SiembraService.java** | abandonarCultivo | setEstado(ABANDONADO), plotRepository.save (494-498) |
| **SiembraService.java** | limpiarCultivo | setEstado(DISPONIBLE), setCultivoActual(null), fechas null, plotRepository.save (536-545) |
| **PlotService.java** | updateLote | **lote.setEstado(loteData.getEstado())** (146), plotRepository.save (150) → **Centralizar: no permitir** |
| **PlotService.java** | resetearLote | setEstado(CANCELADA) en labores, setActivo(false), luego setEstadoConfigurado, setEstado(DISPONIBLE), plotRepository.save (233-261) |
| **PlotService.java** | deleteLote | setActivo(false), plotRepository.save (160) |
| **TransicionEstadoService.java** | evaluarYAplicarTransicion | setEstadoConfigurado, setEstado, plotRepository.save (52-56, 69-73) |
| **HistorialCosechaService.java** | liberarLoteParaNuevaSiembra | setEstado(DISPONIBLE), setCultivoActual(null), fechas null, plotRepository.save (128-133) |
| **HistorialCosechaService.java** | liberarLoteForzadamente | setEstado(DISPONIBLE), fechas null, plotRepository.save (158-163) |
| **EstadoLoteService.java** | (método que hace save) | plotRepository.save(lote) (110) |
| **CrearLotePorcinoAdapter.java** | crearLote | setEstado(DISPONIBLE), plotRepository.save (61-65) |

**Conclusión:** El único punto que debe dejar de escribir estado por “actualización genérica” es **PlotService.updateLote** (línea 146). El resto son flujos válidos: Siembra, Cosecha, Reset, Liberar, TransicionEstado, LaborService (asignar cultivo/actualizar info/crear registro cosecha), CrearLote (nuevo lote).

---

## 2. Flujos de registro de cosecha

| Archivo | Método | Qué hace |
|---------|--------|----------|
| **SiembraService.java** | cosecharLote (línea 251) | Crea Labor COSECHA, HistorialCosecha, try { crearInventarioDesdeCosecha } catch log, actualiza Plot COSECHADO. **Problema:** try/catch traga excepción; no hay rollback si falla inventario. |

**Otros:** HistorialCosechaService.crearHistorialCosecha no se usa en el flujo de cosecha desde API; SiembraService es el único registro de cosecha que crea Labor + HistorialCosecha + InventarioGrano.

**Requisito:** En cosecharLote, una sola transacción; orden: Labor → HistorialCosecha → InventarioGrano → Plot; eliminar try/catch para que la excepción propague y haga rollback.

---

## 3. Flujos que crean o actualizan Labor

| Archivo | Método | Impacto T1 |
|---------|--------|------------|
| **LaborService.java** | crearLabor | Crear: validar solapamiento antes de save. |
| **LaborService.java** | crearLaborDesdeRequest | Crear: validar solapamiento antes de save. |
| **LaborService.java** | crearLaborSiembraConConfirmacion | Crear labor siembra: validar solapamiento. |
| **LaborService.java** | crearLaborCosechaConConfirmacion | Crear labor cosecha: validar solapamiento. |
| **LaborService.java** | actualizarLabor | Puede cambiar fechaInicio/fechaFin: validar solapamiento. |
| **LaborService.java** | actualizarParcialLabor | Puede cambiar fecha_planificada: validar solapamiento. |
| **SiembraService.java** | registrarSiembra | Crea laborSiembra (SIEMBRA COMPLETADA): validar solapamiento antes de save. |
| **SiembraService.java** | cosecharLote | Crea laborCosecha (COSECHA COMPLETADA): validar solapamiento antes de save. |
| **SiembraService.java** | abandonarCultivo | Crea laborAbandono (OTROS COMPLETADA): no obligatorio T1 (no SIEMBRA/COSECHA). |
| **SiembraService.java** | limpiarCultivo | Crea laborLimpieza (OTROS COMPLETADA): no obligatorio T1. |
| **PlotService.java** | resetearLote | Cambia estado de labores a CANCELADA, setActivo(false); no crea labores nuevas. |

**Regla T1:** Aplicar validación de solapamiento en: LaborService.crearLabor, crearLaborDesdeRequest, crearLaborSiembraConConfirmacion, crearLaborCosechaConConfirmacion, actualizarLabor, actualizarParcialLabor; SiembraService.registrarSiembra y SiembraService.cosecharLote (antes de guardar la labor de siembra o de cosecha).

---

## 4. Servicios ya @Transactional

- LaborService: `@Transactional` a nivel de clase.
- SiembraService: `@Transactional` a nivel de clase.
- PlotService: `@Transactional` a nivel de clase.
- HistorialCosechaService: `@Transactional` a nivel de clase.
- TransicionEstadoService: `@Transactional` a nivel de clase.
- InventarioGranoService: `@Transactional` a nivel de clase.

No es necesario añadir @Transactional a cosecharLote; ya está en la misma transacción por defecto.

---

## 5. Resumen de archivos impactados

| Archivo | Cambios previstos |
|---------|-------------------|
| Plot.java | Añadir campo `version` y anotación @Version. |
| Labor.java | Añadir campo `version` y anotación @Version. |
| db/migration | Nueva migración: ADD COLUMN version a cultivo_lotes y cultivo_labores. |
| GlobalExceptionHandler.java | Añadir @ExceptionHandler para OptimisticLockException → 409. |
| SiembraService.java | (1) Quitar try/catch en crearInventarioDesdeCosecha; (2) Validar solapamiento antes de save laborSiembra y antes de save laborCosecha. |
| PlotService.java | En updateLote: no asignar estado ni estadoConfigurado desde loteData. |
| LaborService.java | (1) Validar solapamiento T1 antes de persistir en crearLabor, crearLaborDesdeRequest, crearLaborSiembraConConfirmacion, crearLaborCosechaConConfirmacion, actualizarLabor, actualizarParcialLabor. |
| LaborRepository.java | Añadir método para labores activas por lote (ya existe findByLoteIdAndActivoTrue) o consulta por rango para solapamiento. |

---

**Fin del análisis previo.** Puntos de escritura confirmados; se procede a la implementación.
