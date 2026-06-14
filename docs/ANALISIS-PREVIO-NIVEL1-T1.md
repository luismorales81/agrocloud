# Análisis previo — Implementación Nivel 1 + T1

**Objetivo:** Listar archivos y métodos impactados antes de codificar. Confirmar puntos de escritura reales.

---

## 1. Escrituras de Plot (setEstado, setEstadoConfigurado, plotRepository.save)

| Archivo | Método | Líneas | Acción |
|---------|--------|--------|--------|
| **LaborService** | asignarCultivoAlLotePorSiembra | 872, 877, 882, 885-886 | setEstadoConfigurado, setEstado(SEMBRADO), plotRepository.save |
| **LaborService** | actualizarInformacionCultivo | 952 | plotRepository.save(lote) |
| **LaborService** | crearRegistroCosecha | 965 | plotRepository.save(lote) |
| **SiembraService** | registrarSiembra | 203-204, 214, 222, 232, 245 | setEstadoConfigurado, setEstado(SEMBRADO), plotRepository.save |
| **SiembraService** | cosecharLote | 451, 454 | setEstado(COSECHADO), plotRepository.save |
| **SiembraService** | abandonarCultivo | 494, 498 | setEstado(ABANDONADO), plotRepository.save |
| **SiembraService** | limpiarCultivo | 536, 545 | setEstado(DISPONIBLE), plotRepository.save |
| **PlotService** | updateLote | 146, 150 | **lote.setEstado(loteData.getEstado())** → centralizar: no permitir |
| **PlotService** | resetearLote | 249-250, 261, 270 | setEstadoConfigurado, setEstado(DISPONIBLE), plotRepository.save (y labores) |
| **TransicionEstadoService** | evaluarYAplicarTransicion | 52-56, 69-73 | setEstadoConfigurado/setEstado, plotRepository.save |
| **HistorialCosechaService** | liberarLoteParaNuevaSiembra, liberarLoteForzadamente | 128-133, 158-163 | setEstado(DISPONIBLE), plotRepository.save |
| **EstadoLoteService** | (método que hace save) | 110 | plotRepository.save(lote) |
| **CrearLotePorcinoAdapter** | crearLote | 61, 65 | setEstado(DISPONIBLE), plotRepository.save (nuevo lote) |

**Conclusión:** El único punto que debe dejar de escribir estado por “actualización genérica” es **PlotService.updateLote** (línea 146). El resto son flujos de siembra, cosecha, reset, liberar o transición por labor → se mantienen.

---

## 2. Flujos de registro de cosecha

| Archivo | Método | Qué hace |
|---------|--------|----------|
| **SiembraService** | cosecharLote | Crea Labor COSECHA, HistorialCosecha, try/catch crearInventarioDesdeCosecha, actualiza Plot COSECHADO. **Transacción:** clase tiene @Transactional. **Problema:** try/catch traga excepción de inventario. |

**Método principal:** `SiembraService.cosecharLote(Long loteId, CosechaRequest request, User usuario, Empresa empresa)`.

**Orden actual:** Labor → HistorialCosecha → (try) InventarioGrano → Plot.  
**Requisito:** Misma transacción; si falla inventario, rollback total. Eliminar try/catch y dejar que la excepción propague.

---

## 3. Flujos que crean o actualizan Labor

| Archivo | Método | Persistencia |
|---------|--------|---------------|
| **LaborService** | crearLabor | laborRepository.save(labor) |
| **LaborService** | crearLaborDesdeRequest | laborRepository.save(labor) |
| **LaborService** | crearLaborSiembraConConfirmacion | laborRepository.save(laborGuardada) |
| **LaborService** | crearLaborCosechaConConfirmacion | laborRepository.save(laborGuardada) |
| **LaborService** | confirmarLaborSiembra, confirmarLaborCosecha | laborRepository.save(labor) |
| **LaborService** | actualizarLabor | laborRepository.save(labor) |
| **LaborService** | actualizarParcialLabor | laborRepository.save(labor) |
| **LaborService** | eliminarLabor, anularLabor, deleteLabor | laborRepository.save(labor) |
| **SiembraService** | registrarSiembra | laborRepository.save(laborSiembra) x2 |
| **SiembraService** | cosecharLote | laborRepository.save(laborCosecha) |
| **SiembraService** | abandonarCultivo | laborRepository.save(laborAbandono) |
| **SiembraService** | limpiarCultivo | laborRepository.save(laborLimpieza) |
| **PlotService** | resetearLote | laborRepository.save(labor) en bucle |

**Puntos para validación T1 (solapamiento):**  
- LaborService: crearLabor, crearLaborDesdeRequest, actualizarLabor, actualizarParcialLabor (cuando cambien fechas).  
- SiembraService: registrarSiembra (crea labor SIEMBRA), cosecharLote (crea labor COSECHA). abandonarCultivo y limpiarCultivo crean labor OTROS/COMPLETADA; no son SIEMBRA ni COSECHA, por tanto no requieren T1 entre sí para esos tipos.

---

## 4. Servicios ya @Transactional

| Servicio | Anotación |
|----------|------------|
| LaborService | @Transactional (clase) |
| SiembraService | @Transactional (clase) |
| PlotService | @Transactional (clase) |
| HistorialCosechaService | @Transactional (clase) |
| TransicionEstadoService | @Transactional (clase) |
| InventarioGranoService | @Transactional (clase) |

**Confirmación:** cosecharLote se ejecuta en la transacción de SiembraService; si quitamos el try/catch, cualquier excepción en crearInventarioDesdeCosecha hará rollback de toda la transacción.

---

## 5. Archivos impactados (resumen)

| Cambio | Archivos |
|--------|----------|
| @Version Plot | Plot.java, migración V1_126 |
| @Version Labor | Labor.java, migración V1_126 |
| 409 OptimisticLock | GlobalExceptionHandler.java |
| Transacción cosecha | SiembraService.java (quitar try/catch) |
| Centralizar estado Plot | PlotService.java (updateLote: no setEstado/setEstadoConfigurado desde DTO) |
| T1 solapamiento | LaborService.java (método validación + llamadas en crearLabor, crearLaborDesdeRequest, actualizarLabor, actualizarParcialLabor), SiembraService.java (validar antes de save en registrarSiembra y cosecharLote) |
| Repositorio (consulta por lote y rango fechas) | LaborRepository.java (método para labores activas por lote y rango) |

---

**Fin del análisis previo.**
