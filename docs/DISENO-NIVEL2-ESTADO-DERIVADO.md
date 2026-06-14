# Diseño Nivel 2 — Estado de lote derivado del dominio

**Contexto:** Nivel 1 + T1 completados; auditoría técnica detecta modelo híbrido (estado autónomo + eventos) e inconsistencias.  
**Objetivo:** Rediseñar el módulo agrícola para que `Plot.estado` sea **derivado** del dominio, no una variable editable.  
**Alcance:** Solo diseño (arquitectura, invariantes formales, plan de migración). No se modifica código en este documento.

**Referencias:** `AUDITORIA-TECNICA-MODULO-AGRICOLA.md`, `SPEC-ENDURECIMIENTO-MODULO-AGRICOLA.md`.

---

## PASO 1 — MODELO CANÓNICO DE ESTADO

### 1.1 Definiciones

- **Estado derivado:** El valor de `Plot.estado` (y, si se mantiene, `Plot.estadoConfigurado`) es el **resultado** de una función sobre el dominio (labores activas, historial de cosecha, fechas, reglas de transición). No se escribe directamente salvo como salida de esa función.
- **Labor activa:** Labor con `activo == true` y estado distinto de CANCELADA/ANULADA para efectos de “presencia de ciclo” (para SEMBRADO se considera Labor SIEMBRA activa; para COSECHADO se considera existencia de HistorialCosecha confirmado).
- **HistorialCosecha confirmado:** Registro de cosecha creado por el flujo canónico de cosecha (Labor COSECHA + HistorialCosecha + InventarioGrano). Se considera “confirmado” si existe y no ha sido eliminado.

### 1.2 Reglas determinísticas (tabla)

| Regla | Condición (todas necesarias) | Estado resultante |
|-------|------------------------------|-------------------|
| R1 | Existe al menos una Labor de tipo SIEMBRA activa en el lote. | SEMBRADO (o estado intermedio según R5–R8) |
| R2 | No existe Labor SIEMBRA activa; existe al menos un HistorialCosecha para el lote (última cosecha del ciclo actual). | COSECHADO |
| R3 | No existe Labor SIEMBRA activa; no existe HistorialCosecha “vigente” para el ciclo (o no hay cosecha pendiente de liberar). | DISPONIBLE |
| R4 | La última labor relevante del lote fue de abandono (tipo OTROS + descripción/observaciones de abandono) y no existe posterior Labor SIEMBRA activa. | ABANDONADO |
| R5 | R1 se cumple; tipo de cultivo configurado + fechaSiembra; días desde fechaSiembra en rango [0, 14]. | SEMBRADO |
| R6 | R1 se cumple; días desde fechaSiembra en rango [15, 44] (o según configuración por tipo de cultivo). | EN_CRECIMIENTO |
| R7 | R1 se cumple; días desde fechaSiembra en rango [45, 64]. | EN_FLORACION |
| R8 | R1 se cumple; días desde fechaSiembra en rango [65, 99]. | EN_FRUTIFICACION |
| R9 | R1 se cumple; días desde fechaSiembra ≥ 100 (o según configuración). | LISTO_PARA_COSECHA |
| R10 | Lote en LISTO_PARA_COSECHA y existe Labor COSECHA en curso (EN_PROGRESO). | EN_COSECHA |
| R11 | Existe Labor de tipo MANTENIMIENTO/FERTILIZACION completada según regla de transición; no hay Labor SIEMBRA activa; no hay HistorialCosecha vigente. | PREPARADO / EN_PREPARACION |
| R12 | Estado especial explícito (plagas, enfermedad) registrado por labor o por marca temporal. | ENFERMO |

**Prioridad de evaluación (orden de precedencia):**  
ABANDONADO (R4) → COSECHADO (R2) → DISPONIBLE (R3) → EN_COSECHA (R10) → LISTO_PARA_COSECHA (R9) → EN_FRUTIFICACION (R8) → EN_FLORACION (R7) → EN_CRECIMIENTO (R6) → SEMBRADO (R5/R1) → PREPARADO/EN_PREPARACION (R11) → ENFERMO (R12).  
Si ninguna regla aplica, valor por defecto: **DISPONIBLE**.

### 1.3 Diagrama lógico de cálculo de estado

```
                    ┌─────────────────────────────────────────────────────────┐
                    │                  calcularEstado(lote)                    │
                    └─────────────────────────────────────────────────────────┘
                                              │
                    ┌─────────────────────────┼─────────────────────────┐
                    ▼                         ▼                         ▼
            ¿Última labor relevante    ¿Existe HistorialCosecha   ¿Existe Labor
             fue ABANDONO y no hay     vigente (última cosecha     SIEMBRA activa?
             nueva SIEMBRA?            del lote) y no hay
                    │                 Labor SIEMBRA activa?              │
                    ▼                         │                         ▼
              ABANDONADO                      ▼                   ┌─────┴─────┐
                                        COSECHADO                 │    SÍ     │
                                                                  ▼           │ NO
                                                    ¿Días desde fechaSiembra?  │
                                                    [0,14]   → SEMBRADO       │
                                                    [15,44]  → EN_CRECIMIENTO  │
                                                    [45,64]  → EN_FLORACION    │
                                                    [65,99]  → EN_FRUTIFICACION│
                                                    ≥100     → LISTO_PARA_      │
                                                              COSECHA          │
                                                                               ▼
                                                    ¿Labor COSECHA EN_PROGRESO? → EN_COSECHA
                                                                               │
                                                    Si no hay SIEMBRA activa   ▼
                                                    ni cosecha vigente  → DISPONIBLE
                                                    (o PREPARADO/EN_PREPARACION
                                                     si aplican labores preparación)
```

### 1.4 Función conceptual `calcularEstado(lote)`

**Entrada:** `lote` (Plot con relaciones cargadas: labores activas del lote, último HistorialCosecha del lote si existe, fechaSiembra, tipoCultivo, estadoConfigurado).  
**Salida:** `EstadoLote` (enum).

**Pseudocódigo:**

```
función calcularEstado(lote):
  laboresActivas = labores del lote donde activo=true y estado ∉ {CANCELADA, ANULADA}
  hayLaborSiembraActiva = existe l ∈ laboresActivas con tipoLabor = SIEMBRA
  ultimaCosecha = último HistorialCosecha por lote (por fechaCosecha desc) para lote.id
  hayCosechaVigente = (ultimaCosecha ≠ null) y (no hay Labor SIEMBRA activa posterior en tiempo lógico)
  ultimaLaborRelevante = última labor activa o no (por fecha) de tipo SIEMBRA, COSECHA u OTROS (abandono)

  si ultimaLaborRelevante es “abandono” y no hayLaborSiembraActiva:
    devolver ABANDONADO

  si hayCosechaVigente y no hayLaborSiembraActiva:
    devolver COSECHADO

  si hayLaborSiembraActiva:
    dias = días desde lote.fechaSiembra hasta hoy (o fecha de referencia)
    si dias >= 100 (o según config tipoCultivo): devolver LISTO_PARA_COSECHA
    si existe Labor COSECHA en estado EN_PROGRESO para el lote: devolver EN_COSECHA
    si dias en [65,99]: devolver EN_FRUTIFICACION
    si dias en [45,64]: devolver EN_FLORACION
    si dias en [15,44]: devolver EN_CRECIMIENTO
    devolver SEMBRADO

  si no hayLaborSiembraActiva y no hayCosechaVigente:
    si existen labores MANTENIMIENTO/FERTILIZACION completadas según regla de “preparado”:
      devolver PREPARADO o EN_PREPARACION (según config)
    devolver DISPONIBLE

  devolver DISPONIBLE
```

**Nota:** “Cosecha vigente” puede definirse como: existe HistorialCosecha para el lote y no se ha ejecutado después una liberación explícita (liberarLote) que marque el ciclo como cerrado. En la implementación se puede usar “último HistorialCosecha por lote” y considerar vigente si el lote no está explícitamente liberado (campo o convención a definir).

---

## PASO 2 — ELIMINAR ESCRITURA DIRECTA DE ESTADO

### 2.1 Inventario de escrituras directas de estado

Todos los puntos donde se escribe `estado` o `estadoConfigurado` en Plot (o se invoca lógica que lo hace):

| Archivo | Método | Acción actual |
|---------|--------|----------------|
| SiembraService | registrarSiembra | setEstado(SEMBRADO), setEstadoConfigurado |
| SiembraService | cosecharLote | setEstado(COSECHADO) |
| SiembraService | abandonarCultivo | setEstado(ABANDONADO) |
| SiembraService | limpiarCultivo | setEstado(DISPONIBLE) |
| LaborService | asignarCultivoAlLotePorSiembra | setEstado(SEMBRADO), setEstadoConfigurado |
| LaborService | actualizarInformacionCultivo | plotRepository.save (no setea estado) |
| LaborService | crearRegistroCosecha | plotRepository.save (no setea estado) |
| LaborService | confirmarLaborSiembra / confirmarLaborCosecha | indirecto vía EstadoLoteService |
| PlotService | resetearLote | setEstado(DISPONIBLE), setEstadoConfigurado |
| PlotService | validarYCorregirLotes | no setea estado |
| TransicionEstadoService | evaluarYAplicarTransicion | setEstado(DISPONIBLE) o setEstado(nuevoEstado) |
| HistorialCosechaService | liberarLoteParaNuevaSiembra | setEstado(DISPONIBLE) |
| HistorialCosechaService | liberarLoteForzadamente | setEstado(DISPONIBLE) |
| EstadoLoteService | confirmarCambioEstado | lote.cambiarEstado(estadoPropuesto), save |
| CrearLotePorcinoAdapter | crearOObtenerLotePorcino | setEstado(DISPONIBLE) en lote nuevo |
| Plot (domain) | cambiarEstado / setEstadoConfigurado | setters de estado |

### 2.2 Rediseño propuesto: un solo componente que deriva el estado

- **Componente único:** `EstadoLoteCalculator` (o `CalculadorEstadoLote`).
  - Responsabilidad: dada la entidad Plot y el contexto de dominio (labores activas, último historial de cosecha, reglas de transición por tipo de cultivo), devolver el `EstadoLote` (y opcionalmente `EstadoLoteConfig`) que corresponde.
  - No persiste; solo calcula.
- **Componente de aplicación:** `ActualizadorEstadoLote` (o uso directo del calculator + save).
  - Responsabilidad: invocar `EstadoLoteCalculator.calcularEstado(lote)` (o equivalente), asignar el resultado a `lote.setEstado(...)` y, si aplica, `lote.setEstadoConfigurado(...)`, y persistir `lote` (plotRepository.save).  
  - Único lugar donde se permite llamar a `setEstado` / `setEstadoConfigurado` sobre Plot con valor derivado.
- **Servicios de dominio/application:** No llaman a `setEstado` ni `cambiarEstado` ni `setEstadoConfigurado`. Tras cada operación que afecte al estado del lote, llaman a `recalcularEstado(lote)` (o `ActualizadorEstadoLote.actualizar(lote)`).

### 2.3 Patrón “recalcular estado tras operación relevante”

**Definición:**  
Después de cualquier operación que cambie el resultado de `calcularEstado(lote)`, se debe ejecutar:

- `recalcularEstado(lote)`  
  que en implementación:
  1. (Re)carga el lote con labores activas y último HistorialCosecha si hace falta.
  2. Llama a `EstadoLoteCalculator.calcularEstado(lote)`.
  3. Asigna el resultado a `lote.setEstado(...)` (y estadoConfigurado si aplica).
  4. Persiste con `plotRepository.save(lote)`.

**Puntos de enganche (después de los cuales se debe llamar a recalcularEstado(lote)):**

| Operación | Servicio / flujo | Lote afectado |
|-----------|-------------------|---------------|
| Crear Labor (SIEMBRA, COSECHA, OTROS, etc.) | LaborService.crearLabor, crearLaborDesdeRequest, crearLaborSiembraConConfirmacion, crearLaborCosechaConConfirmacion | labor.getLote() |
| Actualizar Labor (fechas, estado) | LaborService.actualizarLabor, actualizarParcialLabor, confirmarLaborSiembra | labor.getLote() |
| Eliminar / anular Labor | LaborService.eliminarLabor, anularLabor, deleteLabor, deleteLaborFisicamente | labor.getLote() |
| Crear HistorialCosecha | Solo desde flujo único de cosecha (ejecutarCosecha) | historial.getLote() |
| Reset lote | PlotService.resetearLote | lote |
| Liberar lote | HistorialCosechaService.liberarLoteParaNuevaSiembra, liberarLoteForzadamente | lote |
| Crear lote nuevo | PlotService.saveLote, CrearLotePorcinoAdapter | lote (recién creado: estado inicial DISPONIBLE por cálculo) |

**Eliminación de escritura directa:**

- **EstadoLoteService.confirmarCambioEstado:** Se rediseña para que **no** setee estado de forma arbitraria. Opciones: (a) eliminar el endpoint/caso de uso de “confirmar cambio de estado” que permita elegir cualquier estado; (b) o que “confirmar” solo dispare el flujo canónico (ej. confirmar siembra = completar labor + recalcular; confirmar cosecha = ejecutarCosecha) y el estado sea siempre derivado.
- **TransicionEstadoService.evaluarYAplicarTransicion:** En lugar de hacer setEstado/setEstadoConfigurado, debe invocar `recalcularEstado(lote)`. Las reglas de TransicionEstadoService se integran en `EstadoLoteCalculator` (estados intermedios por días desde siembra y por labores de preparación).
- **SiembraService, LaborService, PlotService, HistorialCosechaService:** Eliminar todas las llamadas a `lote.setEstado(...)` y `lote.setEstadoConfigurado(...)`. Sustituir por una única llamada a `recalcularEstado(lote)` al final del flujo transaccional que afecte al lote.
- **CrearLotePorcinoAdapter:** Para lote nuevo, o bien no setear estado (y dejar que el primer “recalcularEstado” lo ponga en DISPONIBLE), o bien invocar recalcularEstado tras crear el Plot (si el calculator soporta lote sin labores → DISPONIBLE).

---

## PASO 3 — UNIFICAR FLUJO DE COSECHA

### 3.1 Caso de uso único: `ejecutarCosecha(loteId, request)`

**Objetivo:** Un solo punto de entrada que garantice: Labor COSECHA + HistorialCosecha + InventarioGrano + estado derivado, en una transacción.

**Orden de ejecución:**

1. Validar lote existe y está en estado cosechable (o permitir cosecha anticipada según reglas actuales).
2. Validar T1: no existe otra Labor COSECHA activa solapada (validarSolapamientoT1).
3. Crear y persistir **Labor COSECHA** (estado COMPLETADA, fechas y datos del request).
4. Crear y persistir **HistorialCosecha** (asociado al lote, cultivo, Labor COSECHA si se modela la relación, usuario, cantidad, fechas, etc.).
5. Llamar a **InventarioGranoService.crearInventarioDesdeCosecha(historialCosecha, usuario)**.
6. **Recalcular estado del lote** (recalcularEstado(lote)) → resultado esperado COSECHADO por R2.
7. Persistir Plot (ya incluido en recalcularEstado).

Todo dentro de la misma transacción (@Transactional). Si falla cualquier paso, rollback total.

### 3.2 Ubicación del caso de uso

- **Propuesta:** Mantener el flujo en **SiembraService** (o en un servicio de aplicación dedicado “CosechaService”) con un método público único, por ejemplo:
  - `SiembraService.ejecutarCosecha(Long loteId, CosechaRequest request, User usuario, Empresa empresa)`  
  que contenga los pasos 1–7. El método actual `cosecharLote` puede renombrarse a `ejecutarCosecha` o delegar en él.
- **LaborService.confirmarLaborCosecha:**  
  - **Opción A (recomendada):** Deprecar. El frontend/API que hoy “confirma labor de cosecha” debe migrarse a llamar al único caso de uso `ejecutarCosecha(loteId, request)`. No se crea “labor cosecha pendiente de confirmación” que luego se confirma con cambio de estado manual; la cosecha es siempre el flujo atómico ejecutarCosecha.
  - **Opción B:** Hacer que `confirmarLaborCosecha` delegue en `ejecutarCosecha`: si la confirmación es “sí”, construir el `CosechaRequest` a partir de la labor existente (si existe) y del lote, y llamar a `ejecutarCosecha`. Así no se duplica lógica y el estado queda siempre derivado.

### 3.3 Eliminación del doble flujo

- Eliminar la ruta que hoy hace: crear Labor COSECHA → proponerCambioEstado → confirmarCambioEstado (EstadoLoteService) → crearRegistroCosecha (solo fechas). Esa ruta no crea HistorialCosecha ni InventarioGrano y deja estado “editado”.
- Toda cosecha que deba reflejarse en inventario y en estado COSECHADO debe pasar por `ejecutarCosecha`.

---

## PASO 4 — INVARIANTES FORMALIZADAS

Contrato formal del dominio (siempre que el sistema esté en el modelo Nivel 2):

**INVARIANTE 1 (estado SEMBRADO):**  
`Plot.estado == SEMBRADO ⇒ existe al menos una Labor de tipo SIEMBRA activa (activo=true, estado ∉ {CANCELADA, ANULADA}) en el lote.`

**INVARIANTE 2 (estado COSECHADO):**  
`Plot.estado == COSECHADO ⇒ existe al menos un HistorialCosecha para el lote (y no se ha liberado el lote para nueva siembra en el mismo ciclo).`  
Opcionalmente: existe al menos una Labor de tipo COSECHA asociada al mismo ciclo (si se modela la relación Labor–HistorialCosecha).

**INVARIANTE 3 (HistorialCosecha implica Labor COSECHA):**  
`Para todo HistorialCosecha h creado por el sistema, existe una Labor de tipo COSECHA (activa o no) que corresponde al mismo evento de cosecha (mismo lote, misma fecha/ciclo).`  
En implementación: HistorialCosecha solo se crea dentro de `ejecutarCosecha`, que siempre crea antes la Labor COSECHA. No existe API pública que cree HistorialCosecha sin Labor COSECHA.

**INVARIANTE 4 (InventarioGrano implica HistorialCosecha):**  
`Para todo InventarioGrano, existe un HistorialCosecha tal que inventario.cosecha_id = historial.id.`  
Garantizado por FK (cosecha_id NOT NULL) y por flujo único crearInventarioDesdeCosecha(HistorialCosecha).

**INVARIANTE 5 (estado DISPONIBLE):**  
`Plot.estado == DISPONIBLE ⇒ no existe Labor SIEMBRA activa en el lote y no existe HistorialCosecha “vigente” (o el lote fue liberado).`  
Equivalente a: el estado DISPONIBLE es el resultado de calcularEstado cuando no hay R1 ni R2.

**INVARIANTE 6 (estado ABANDONADO):**  
`Plot.estado == ABANDONADO ⇒ la última labor relevante del lote fue de abandono y no existe posterior Labor SIEMBRA activa.`  
Derivado por R4 en calcularEstado.

**INVARIANTE 7 (T1 — no dos SIEMBRA solapadas):**  
`No existen dos Labores de tipo SIEMBRA activas en el mismo lote con intervalos [fechaInicio, fechaFin] solapados.`  
Ya garantizado en Nivel 1; se mantiene.

**INVARIANTE 8 (T1 — no dos COSECHA solapadas):**  
`No existen dos Labores de tipo COSECHA activas en el mismo lote con intervalos [fechaInicio, fechaFin] solapados.`  
Ya garantizado en Nivel 1; se mantiene.

**INVARIANTE 9 (estado derivado):**  
`En todo momento, Plot.estado = EstadoLoteCalculator.calcularEstado(lote).`  
Es decir, el estado persistido es siempre el resultado del cálculo; no existe escritura directa de estado fuera de `recalcularEstado`.

---

## PASO 5 — PLAN DE MIGRACIÓN (RESUMEN)

1. **Introducir EstadoLoteCalculator y ActualizadorEstadoLote (o recalcularEstado)**  
   Sin quitar aún escrituras directas; implementar el cálculo y la actualización en paralelo.

2. **Sustituir escrituras directas por recalcularEstado**  
   En SiembraService, LaborService, PlotService, TransicionEstadoService, HistorialCosechaService, CrearLotePorcinoAdapter. Eliminar setEstado/setEstadoConfigurado y llamar a recalcularEstado(lote) al final de cada flujo que afecte al lote.

3. **Rediseñar EstadoLoteService**  
   confirmarCambioEstado deja de setear estado arbitrario; o se elimina el flujo de “confirmar estado” libre, o se limita a disparar acciones que terminen en recalcularEstado.

4. **Unificar cosecha en ejecutarCosecha**  
   Implementar ejecutarCosecha (o refactorizar cosecharLote) con los 7 pasos; deprecar o hacer que confirmarLaborCosecha delegue en ejecutarCosecha.

5. **Restringir HistorialCosechaService.crearHistorialCosecha**  
   Deprecar o hacer que sea interno y solo invocado desde ejecutarCosecha (o eliminar el método público).

6. **Validación y pruebas**  
   Probar que tras crear/anular/eliminar labores, resetear, liberar, el estado del lote coincide con calcularEstado; y que las invariantes 1–9 se cumplen.

---

**Fin del diseño Nivel 2.**  
Documento solo de arquitectura e invariantes; implementación en fases posteriores.
