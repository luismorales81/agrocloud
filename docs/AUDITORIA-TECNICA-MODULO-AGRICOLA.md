# Auditoría técnica — Módulo agrícola (post Nivel 1 + T1)

**Objetivo:** Evaluar coherencia sistémica del módulo agrícola respecto a integridad transaccional, consistencia de estado, invariantes de negocio, bypasses, concurrencia y acoplamientos.  
**Alcance:** Solo análisis y reporte; no se modifica código.  
**Referencias:** `SPEC-ENDURECIMIENTO-MODULO-AGRICOLA.md`, `ANALISIS-PREVIO-NIVEL1-T1.md`, `CHECKLIST-ENDURECIMIENTO-NIVEL1-T1.md`.

---

## PASO 1 — MAPEO DE ESCRITURAS INDIRECTAS

Tabla de todos los puntos donde se modifica Plot.estado/estadoConfigurado, se crea/modifica Labor, HistorialCosecha, InventarioGrano o operaciones en cascada que impactan el lote. Se indica si está en @Transactional, si puede romper atomicidad, estados intermedios inválidos o T1.

### 1.1 Escrituras de Plot (estado / estadoConfigurado)

| Archivo | Método | ¿@Transactional? | Tipo de riesgo | Descripción | Nivel |
|---------|--------|-------------------|----------------|------------|--------|
| SiembraService | registrarSiembra | Sí (clase) | Bajo | setEstado(SEMBRADO), setEstadoConfigurado; flujo canónico. | Bajo |
| SiembraService | cosecharLote | Sí | Bajo | setEstado(COSECHADO); flujo canónico. | Bajo |
| SiembraService | abandonarCultivo | Sí | Bajo | setEstado(ABANDONADO). | Bajo |
| SiembraService | limpiarCultivo | Sí | Bajo | setEstado(DISPONIBLE). | Bajo |
| LaborService | asignarCultivoAlLotePorSiembra | Sí | Bajo | setEstado(SEMBRADO), setEstadoConfigurado; llamado desde confirmar labor. | Bajo |
| LaborService | actualizarInformacionCultivo | Sí | Bajo | No setea estado; solo fechaSiembra, cultivoActual, plotRepository.save. | Bajo |
| LaborService | crearRegistroCosecha | Sí | Medio | No setea estado; actualiza fechaCosechaReal y rendimiento. Llamado desde confirmarLaborCosecha **después** de EstadoLoteService.confirmarCambioEstado, que **sí** setea COSECHADO. | Medio |
| PlotService | updateLote | Sí | N/A | Ya no asigna estado (centralización Nivel 1). | — |
| PlotService | resetearLote | Sí | Medio | setEstado(DISPONIBLE), setEstadoConfigurado; cancela/archiva labores. No toca HistorialCosecha ni InventarioGrano; puede dejar historial “huérfano” respecto a labores canceladas. | Medio |
| PlotService | validarYCorregirLotes | Sí | Bajo | Solo limpia cultivo/cultivoActual cuando estado == DISPONIBLE; no cambia estado. | Bajo |
| TransicionEstadoService | evaluarYAplicarTransicion | Sí | Medio | setEstado(DISPONIBLE) o setEstado(nuevoEstado) según transición configurada/tradicional. Puede llevar lote a COSECHADO, SEMBRADO, etc. sin pasar por SiembraService; invariantes “SEMBRADO ↔ Labor SIEMBRA activa” y “COSECHADO ↔ Labor COSECHA / HistorialCosecha” pueden romperse si la labor se anula después. | Medio |
| HistorialCosechaService | liberarLoteParaNuevaSiembra | Sí | Bajo | setEstado(DISPONIBLE); regla 7 días. | Bajo |
| HistorialCosechaService | liberarLoteForzadamente | Sí | Bajo | setEstado(DISPONIBLE); bypass de descanso. | Bajo |
| EstadoLoteService | confirmarCambioEstado | Sí | **Alto** | lote.cambiarEstado(confirmacion.getEstadoPropuesto(), motivo); plotRepository.save(lote). Permite setear **cualquier** estado (SEMBRADO, COSECHADO, etc.) sin crear Labor SIEMBRA/COSECHA ni HistorialCosecha ni InventarioGrano. Invariantes rotas por diseño. | **Alto** |
| CrearLotePorcinoAdapter | crearOObtenerLotePorcino | No | Bajo | setEstado(DISPONIBLE) en **nuevo** Plot; lote recién creado. Sin @Transactional en el adapter: si falla después de save, posible estado inconsistente solo en el lote nuevo. | Bajo |

### 1.2 Creación / modificación de Labor

| Archivo | Método | ¿@Transactional? | Tipo de riesgo | Descripción | Nivel |
|---------|--------|-------------------|----------------|------------|--------|
| LaborService | crearLabor, crearLaborDesdeRequest | Sí | Bajo | T1 validado antes de save. | Bajo |
| LaborService | crearLaborSiembraConConfirmacion, crearLaborCosechaConConfirmacion | Sí | Bajo | T1 validado. | Bajo |
| LaborService | actualizarLabor, actualizarParcialLabor | Sí | Bajo | T1 con excluirId. | Bajo |
| LaborService | eliminarLabor | Sí | **Alto** | PLANIFICADA → cancelar y setActivo(false). No recalcula Plot.estado: si era la única Labor SIEMBRA activa, el lote puede quedar SEMBRADO sin labor activa. | **Alto** |
| LaborService | anularLabor | Sí | **Alto** | EN_PROGRESO/COMPLETADA → ANULADA, setActivo(false). No recalcula Plot.estado: lote puede quedar SEMBRADO o COSECHADO sin labor activa. | **Alto** |
| LaborService | deleteLabor | Sí | Bajo | Solo desactiva si estado ya CANCELADA o ANULADA. | Bajo |
| LaborService | deleteLaborFisicamente | Sí | **Alto** | laborRepository.delete(labor). Borrado físico; Plot.estado no se recalcula; puede quedar SEMBRADO/COSECHADO sin ninguna Labor. | **Alto** |
| SiembraService | registrarSiembra, cosecharLote, abandonarCultivo, limpiarCultivo | Sí | Bajo | Crean y guardan labor; T1 en registrarSiembra y cosecharLote. | Bajo |
| PlotService | resetearLote | Sí | Medio | Cancela/archiva labores (setEstado(CANCELADA), setActivo(false)); no borra HistorialCosecha. | Medio |

### 1.3 HistorialCosecha e InventarioGrano

| Archivo | Método | ¿@Transactional? | Tipo de riesgo | Descripción | Nivel |
|---------|--------|-------------------|----------------|------------|--------|
| SiembraService | cosecharLote | Sí | Bajo | Crea HistorialCosecha, llama crearInventarioDesdeCosecha, actualiza Plot. Atómico. | Bajo |
| HistorialCosechaService | crearHistorialCosecha | Sí | **Alto** | **Bypass:** crea HistorialCosecha sin Labor COSECHA y sin InventarioGrano. Público; ningún otro lugar lo llama actualmente, pero expone invariante “HistorialCosecha → Labor COSECHA + Inventario”. | **Alto** |
| HistorialCosechaService | eliminarHistorial | Sí | Medio | historialCosechaRepository.deleteById(id). FK en InventarioGrano (cosecha_id NOT NULL) impide borrar si hay inventario; si no hay inventario, queda HistorialCosecha eliminado sin reflejo en Plot/Labor. | Medio |
| InventarioGranoService | crearInventarioDesdeCosecha | Sí | Bajo | Solo llamado desde SiembraService.cosecharLote en flujo canónico. | Bajo |

### 1.4 Resumen atomicidad / estados intermedios

- **Atomicidad:** Todos los servicios agrícolas relevantes están @Transactional a nivel clase. Riesgo: CrearLotePorcinoAdapter no tiene @Transactional; fallo tras save puede dejar lote guardado sin campo consistente en escenarios de excepción.
- **Estados intermedios:** El flujo EstadoLoteService.confirmarCambioEstado puede dejar Plot en SEMBRADO/COSECHADO sin Labor activa; eliminarLabor/anularLabor/deleteLaborFisicamente no recalculan estado del lote.

---

## PASO 2 — VALIDACIÓN DE INVARIANTES DE NEGOCIO

Invariantes a garantizar:

1. Un lote no puede estar SEMBRADO sin Labor SIEMBRA activa.  
2. Un lote no puede estar COSECHADO sin Labor COSECHA (activa o completada asociada).  
3. No puede existir HistorialCosecha sin Labor COSECHA.  
4. No puede existir InventarioGrano sin HistorialCosecha.  
5. No puede haber SIEMBRA superpuesta (T1).  
6. No puede haber COSECHA superpuesta (T1).

### 2.1 Rupturas detectadas

| Invariante | Dónde se puede romper | Archivo / método | Descripción |
|------------|------------------------|-------------------|-------------|
| SEMBRADO ↔ Labor SIEMBRA activa | Cambio de estado sin labor; eliminación/anulación de labor | EstadoLoteService.confirmarCambioEstado | Permite SEMBRADO sin crear Labor SIEMBRA. |
| | | LaborService.eliminarLabor (Planificada) | Cancela la labor SIEMBRA; Plot sigue SEMBRADO. |
| | | LaborService.anularLabor | Anula Labor SIEMBRA; Plot sigue SEMBRADO. |
| | | LaborService.deleteLaborFisicamente | Borra Labor; Plot no se actualiza. |
| COSECHADO ↔ Labor COSECHA / HistorialCosecha | Cambio de estado sin historial; confirmación de cosecha sin historial | EstadoLoteService.confirmarCambioEstado | Permite COSECHADO sin Labor COSECHA ni HistorialCosecha. |
| | | LaborService.confirmarLaborCosecha | Llama confirmarCambioEstado (Plot → COSECHADO) + crearRegistroCosecha (solo fecha/rendimiento). **No** crea HistorialCosecha ni InventarioGrano. |
| | | LaborService.eliminarLabor / anularLabor / deleteLaborFisicamente | Si se anula/elimina la Labor COSECHA, Plot puede quedar COSECHADO sin labor activa. |
| HistorialCosecha → Labor COSECHA | Creación directa de historial | HistorialCosechaService.crearHistorialCosecha | Crea HistorialCosecha sin pasar por cosecharLote; no hay Labor COSECHA asociada. |
| InventarioGrano → HistorialCosecha | — | — | Respetada por FK (cosecha_id NOT NULL) y por flujo único crearInventarioDesdeCosecha(HistorialCosecha). |
| T1 SIEMBRA/COSECHA | — | — | T1 aplicado en crear/actualizar/confirmar y en SiembraService; no se detectan bypasses que eviten validarSolapamientoT1. |

### 2.2 Reset y liberaciones

| Acción | Efecto sobre invariantes |
|--------|--------------------------|
| PlotService.resetearLote | Pone Plot DISPONIBLE y cancela/archiva labores. No borra HistorialCosecha ni InventarioGrano: quedan registros de cosecha “históricos” sin Labor COSECHA activa (coherente con “reset”). No recalcula estado desde cero según labores restantes. |
| HistorialCosechaService.liberarLoteParaNuevaSiembra / liberarLoteForzadamente | Solo setean DISPONIBLE y limpian fechas/cultivo; no tocan labores ni historial. No rompen invariantes de SEMBRADO/COSECHADO. |

---

## PASO 3 — CONCURRENCIA AVANZADA

### 3.1 Cobertura de Optimistic Lock

- **Plot:** @Version presente; plotRepository.save en todos los flujos pasa por la entidad gestionada → OptimisticLock cubre actualizaciones concurrentes de Plot.
- **Labor:** @Version presente; laborRepository.save idem.
- **HistorialCosecha:** **Sin @Version.** Dos transacciones que creen/modifiquen el mismo historial no tendrían detección de concurrencia vía JPA.
- **InventarioGrano:** **Sin @Version.** Mismo riesgo en actualizaciones concurrentes.

### 3.2 Escenarios evaluados

| Escenario | Cobertura | Notas |
|-----------|------------|--------|
| Dos usuarios registrando siembra en el mismo lote a la vez | T1 + OptimisticLock | T1 evita dos SIEMBRA solapadas; si ambos pasan validación y uno guarda después, el segundo puede recibir 409 al guardar Plot (version). |
| Usuario A cosecha (SiembraService.cosecharLote) mientras B actualiza lote (PlotService.updateLote) | OptimisticLock | updateLote no toca estado; si B tocara otro campo (nombre, área, etc.), el último save de Plot podría dar 409. Correcto. |
| Confirmación de labor mientras otra transacción la modifica | OptimisticLock Labor | Al guardar la labor (confirmar, actualizar), version detecta conflicto → 409. |
| Eliminación de labor mientras otra la confirma | OptimisticLock Labor | eliminarLabor y confirmar ambas hacen laborRepository.save; 409 en uno de los dos. |

### 3.3 Entidades sin version que pueden quedar inconsistentes

- **HistorialCosecha:** sin @Version; doble creación para mismo “evento” es poco probable (flujo único en cosecharLote), pero actualizaciones concurrentes no están protegidas.
- **InventarioGrano:** sin @Version; creación única por cosecha; actualizaciones (ej. estado, cantidades) concurrentes sin protección.
- **MovimientoInventarioGrano:** no revisado en detalle; típicamente inserciones; riesgo menor si no se reutiliza la misma fila.

---

## PASO 4 — ANÁLISIS DE BORRADOS Y ANULACIONES

### 4.1 Métodos que eliminan o desactivan

| Archivo | Método | Acción | ¿Recalcula Plot? | ¿Puede romper T1 tras cambio de fechas? |
|---------|--------|--------|-------------------|------------------------------------------|
| LaborService | eliminarLabor | PLANIFICADA → CANCELADA + setActivo(false); otros → exige anulación. | No | N/A (labor se desactiva; T1 excluye por activo). |
| LaborService | anularLabor | ANULADA + setActivo(false). | No | N/A. |
| LaborService | deleteLabor | setActivo(false) solo si ya CANCELADA/ANULADA. | No | N/A. |
| LaborService | deleteLaborFisicamente | laborRepository.delete(labor). | No | No; la labor desaparece; T1 no la vería. |
| PlotService | resetearLote | Cancela/archiva labores; Plot → DISPONIBLE. | Sí (fijado a DISPONIBLE) | No. |
| HistorialCosechaService | eliminarHistorial | deleteById. | No | N/A. |

### 4.2 Cambio de fechas y T1

- **actualizarLabor / actualizarParcialLabor:** Tras cambiar fechaInicio/fechaFin se llama validarSolapamientoT1(loteId, tipo, fechaInicio, fechaFin, labor.getId()). Correcto.
- **Eliminación de labor:** Tras eliminar/anular, no se vuelve a validar T1; las demás labores activas del lote siguen existiendo; T1 sigue cumplido para las que quedan.

### 4.3 Estado del lote tras borrado/anulación

- **Riesgo:** Lote en SEMBRADO con la única Labor SIEMBRA cancelada/anulada/borrada.  
- **Riesgo:** Lote en COSECHADO con la única Labor COSECHA anulada/borrada.  
- No existe en el código ningún “recalculo de estado del lote según labores activas” tras eliminarLabor, anularLabor o deleteLaborFisicamente.

---

## PASO 5 — ACOPLAMIENTOS PELIGROSOS

### 5.1 Servicios que llaman repositorios saltando lógica de negocio

- **Controllers:** EgresoController, IngresoController usan plotRepository.findById (solo lectura). TestDashboardController usa plotRepository.count, laborRepository.count (lectura). PublicPlotController usa plotRepository.findByActivoTrue / findByUserIdAndActivoTrue (lectura). **No hay escritura** directa a Plot/Labor desde controllers.
- **LaborService** y **SiembraService** escriben siempre a través de sus propios servicios; no se detectan escrituras directas a repositorios de otros módulos que bypaseen reglas agrícolas.

### 5.2 Adapters que setean estado directamente

- **CrearLotePorcinoAdapter:** setEstado(DISPONIBLE) en **nuevo** Plot antes de save. Es creación de lote, no actualización; estado inicial DISPONIBLE es coherente. Riesgo: adapter sin @Transactional (ver 1.1).

### 5.3 Métodos públicos reutilizables que no validan invariantes

| Archivo | Método | Problema |
|---------|--------|----------|
| HistorialCosechaService | crearHistorialCosecha | Público; crea HistorialCosecha sin Labor COSECHA y sin InventarioGrano. Cualquier caller futuro podría romper “HistorialCosecha → Labor COSECHA + Inventario”. |
| EstadoLoteService | confirmarCambioEstado | Público; permite cualquier estado (SEMBRADO, COSECHADO, etc.) sin garantizar Labor ni HistorialCosecha. |
| LaborService | crearRegistroCosecha | Privado; solo actualiza fecha/rendimiento del lote. Llamado desde confirmarLaborCosecha **después** de confirmarCambioEstado; no crea HistorialCosecha ni InventarioGrano, dejando posible COSECHADO sin historial. |

---

## PASO 6 — REPORTE FINAL

### 6.1 Hallazgos críticos

1. **EstadoLoteService.confirmarCambioEstado** (EstadoLoteService.java)  
   Permite setear cualquier estado del lote (SEMBRADO, COSECHADO, etc.) sin crear Labor SIEMBRA/COSECHA ni HistorialCosecha ni InventarioGrano. Rompe invariantes “SEMBRADO ↔ Labor SIEMBRA activa” y “COSECHADO ↔ Labor COSECHA + HistorialCosecha”.

2. **Flujo confirmarLaborCosecha** (LaborService.java)  
   Al confirmar cosecha vía LaborService se llama EstadoLoteService.confirmarCambioEstado (Plot → COSECHADO) y crearRegistroCosecha (solo fechas/rendimiento). No se crea HistorialCosecha ni InventarioGrano. Resultado: Plot COSECHADO sin historial ni inventario.

3. **Eliminación/anulación de labor sin recalcular Plot** (LaborService.java)  
   eliminarLabor (cancelar SIEMBRA planificada), anularLabor (anular SIEMBRA o COSECHA), deleteLaborFisicamente: ninguna recalcula Plot.estado. El lote puede quedar SEMBRADO o COSECHADO sin ninguna labor activa (o sin labor en caso de borrado físico).

4. **HistorialCosechaService.crearHistorialCosecha** (HistorialCosechaService.java)  
   API pública que crea HistorialCosecha sin Labor COSECHA y sin InventarioGrano. Bypass directo a la invariante “HistorialCosecha implica Labor COSECHA (y flujo de inventario)”.

### 6.2 Hallazgos medios

5. **TransicionEstadoService.evaluarYAplicarTransicion**  
   Puede setear estado del lote (p. ej. COSECHADO, SEMBRADO) según reglas de transición. Si después se anula la labor que disparó la transición, el lote queda en ese estado sin labor activa.

6. **PlotService.resetearLote**  
   Pone lote DISPONIBLE y cancela/archiva labores; no borra HistorialCosecha ni InventarioGrano. Deja historial “huérfano” respecto a labores (aceptable como diseño de reset), pero no hay un único punto documentado que aclare que el estado “oficial” del ciclo es el de Plot y no el de existencia de labores.

7. **HistorialCosecha y InventarioGrano sin @Version**  
   Actualizaciones concurrentes de las mismas filas no disparan 409; posible inconsistencia o pérdida de actualizaciones.

8. **HistorialCosechaService.eliminarHistorial**  
   deleteById; FK en InventarioGrano impide borrar si hay inventario. Si en el futuro InventarioGrano permitiera cosecha_id nullable o se borrara en cascada, se podría orfanar inventario.

9. **CrearLotePorcinoAdapter sin @Transactional**  
   Creación de Plot + posible uso de campo; si algo falla después del save, no hay rollback en el adapter.

### 6.3 Hallazgos menores

10. **LaborService.crearRegistroCosecha**  
    Solo actualiza lote (fechaCosechaReal, rendimiento); no crea HistorialCosecha. El riesgo real es el flujo que lo invoca (confirmarLaborCosecha) sin crear historial.

11. **Controllers con plotRepository/laborRepository**  
    Solo lecturas (findById, count, findBy…). Sin escritura directa; riesgo bajo.

12. **validarYCorregirLotes**  
    Solo corrige cultivo/cultivoActual cuando estado == DISPONIBLE; no cambia estado; bajo impacto.

### 6.4 Recomendaciones para endurecimiento Nivel 2

- **Estado y flujo único de cosecha:**  
  - Restringir o eliminar la posibilidad de setear COSECHADO (y opcionalmente SEMBRADO) desde EstadoLoteService.confirmarCambioEstado; o hacer que confirmarCambioEstado a COSECHADO exija y use el flujo SiembraService.cosecharLote (o un único “caso de uso cosecha” que cree Labor + HistorialCosecha + InventarioGrano).  
  - Unificar “cosecha” en un solo flujo: o bien confirmarLaborCosecha no setea COSECHADO y solo el flujo de cosecharLote puede dejar el lote en COSECHADO (creando HistorialCosecha + InventarioGrano), o bien confirmarLaborCosecha delega en ese flujo.

- **Invariantes Plot ↔ Labores:**  
  - Tras eliminarLabor, anularLabor y deleteLaborFisicamente, recalcular Plot.estado según labores activas del lote (p. ej. si no queda Labor SIEMBRA activa, no mantener SEMBRADO; si no queda Labor COSECHA activa, no mantener COSECHADO sin HistorialCosecha), o al menos rechazar eliminación/anulación cuando sea la última labor que sostiene el estado actual.

- **HistorialCosecha:**  
  - Deprecar o restringir HistorialCosechaService.crearHistorialCosecha para que solo sea usado por el flujo canónico (p. ej. SiembraService.cosecharLote), o eliminar el método y crear historial solo dentro de ese flujo.  
  - Valorar @Version en HistorialCosecha (y en InventarioGrano) para concurrencia.

- **Transiciones automáticas:**  
  - Documentar que TransicionEstadoService puede dejar lote en estados que dependen de labores; si se anula la labor, valorar recalcular estado del lote o no permitir transición a COSECHADO/SEMBRADO solo por transición automática sin labor explícita de cosecha/siembra.

- **Adapter porcinos:**  
  - Añadir @Transactional a CrearLotePorcinoAdapter.crearOObtenerLotePorcino (o al método que realiza la creación completa).

- **Borrado de historial:**  
  - Mantener FK InventarioGrano → HistorialCosecha NOT NULL; si se permite eliminar historial, solo cuando no exista InventarioGrano asociado y documentar la decisión.

### 6.5 Diagrama lógico del flujo agrícola ideal (texto estructurado)

```
FLUJO IDEAL (referencia para consistencia)

1) Lote DISPONIBLE
   └─ Origen: creación (PlotService, CrearLotePorcinoAdapter), resetearLote, liberarLote*, TransicionEstadoService.

2) Siembra (único punto que debe dejar SEMBRADO)
   └─ Entrada: SiembraService.registrarSiembra O LaborService (crear labor SIEMBRA + confirmarLaborSiembra con asignarCultivoAlLotePorSiembra).
   └─ Efectos: Labor SIEMBRA activa creada; Plot.estado = SEMBRADO; Plot.fechaSiembra, cultivo, estadoConfigurado según configuración.
   └─ Invariante: SEMBRADO ⇒ existe al menos una Labor SIEMBRA activa en el lote.

3) Ciclo de crecimiento (opcional)
   └─ TransicionEstadoService puede mover estado (EN_CRECIMIENTO, EN_FLORACION, etc.) según labores completadas y fechas.
   └─ EstadoLoteService.confirmarCambioEstado no debería setear SEMBRADO/COSECHADO sin flujo de siembra/cosecha.

4) Cosecha (único punto que debe dejar COSECHADO con historial e inventario)
   └─ Entrada canónica: SiembraService.cosecharLote.
   └─ Efectos: Labor COSECHA activa; HistorialCosecha; InventarioGrano; Plot.estado = COSECHADO; Plot.fechaCosechaReal, etc.
   └─ Entrada alternativa actual (inconsistente): LaborService.crearLaborCosechaConConfirmacion + confirmarLaborCosecha.
   └─ Efectos actuales en alternativa: Plot.estado = COSECHADO vía EstadoLoteService; crearRegistroCosecha solo actualiza fechas/rendimiento; NO se crea HistorialCosecha ni InventarioGrano.
   └─ Invariante deseada: COSECHADO ⇒ existe Labor COSECHA (activa o no) + HistorialCosecha + InventarioGrano creado desde ese historial.

5) Salida a DISPONIBLE
   └─ resetearLote, liberarLoteParaNuevaSiembra, liberarLoteForzadamente, o transición automática (ej. desde COSECHADO por mantenimiento).
   └─ Abandono: abandonarCultivo → ABANDONADO; limpiarCultivo → DISPONIBLE.

6) Eliminación/anulación de labores
   └─ Debe: o bien recalcular Plot.estado (si ya no hay labor que sostenga SEMBRADO/COSECHADO), o bien impedir anulación/eliminación cuando sea la última labor que sostiene el estado.
```

---

**Fin del informe de auditoría.**  
No se ha modificado código; solo diagnóstico estructurado con citas de archivo y método.
