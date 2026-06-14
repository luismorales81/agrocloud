# Análisis: Alimentación en el módulo Porcinos

**Objetivo:** Describir cómo el sistema maneja actualmente la alimentación en Porcinos: fórmulas/recetas, consumo diario automático, consumo manual, calendario, inventario y derrames.

---

## 1. Visión general

La alimentación porcina en el sistema se compone de **dos flujos principales** que conviven:

| Flujo | Descripción | Descuento de inventario |
|-------|-------------|---------------------------|
| **Consumo diario automático** | Job que cada día (00:30) genera el consumo del día anterior por recría y madre, según recetas por etapa. | Sí, vía `InventoryService` (core.inventory). Permite stock negativo. |
| **Consumo manual (ConsumoAlimento)** | Registro manual de consumo por categoría (MADRES, RECRIA, etc.), tipo (BALANCEADO, MAIZ, GRANO_PROPIO) y cantidad. | Parcial: GRANO_PROPIO con cultivoId sí; BALANCEADO sin validación estricta. |

Además existen: **calendario de alimentación** (días por empresa, confirmación, alertas), **derrames/pérdidas** (descuentan inventario) y la **asociación receta–etapa** (qué fórmula se usa en cada etapa).

---

## 2. Modelo de datos de alimentación

### 2.1 Recetas y fórmulas

- **InsumoCompuesto** (tabla compartida, `model.entity`): representa una “receta” o fórmula: nombre, descripción, tipo, unidad, rendimiento, stock mínimo. Tiene **componentes** (insumos, grano propio u otros insumos compuestos) vía `ComponenteInsumoCompuesto` con proporción/cantidad por unidad de receta.
- **RecetaAlimentacionPorEtapa** (porcinos): asocia un **InsumoCompuesto** a una **etapa de alimentación** (GESTACION, LACTANCIA, F1, F2, F3, F4, DESARROLLO, TERMINACION) con:
  - `cantidadDiariaPorAnimal` (obligatoria)
  - opcionales: cantidad min/max, peso min/max animal, edad min/max días
  - `esPorDefecto`: si hay varias recetas para la etapa, se usa la marcada por defecto; si no, la primera activa.

La **fórmula** en negocio es: **InsumoCompuesto** (receta) + **RecetaAlimentacionPorEtapa** (por empresa y etapa). No existe una entidad “Formula” separada; el frontend habla de “fórmulas” pero el backend expone InsumoCompuesto y RecetaAlimentacionPorEtapa.

### 2.2 Consumo diario automático

- **DiaAlimentacion**: un registro por **empresa + fecha**. Estado: PENDIENTE, CONFIRMADO, CON_CORRECCIONES. Totales (lotes, animales, recetas, insumos), alertas de stock, y relación con los consumos del día.
- **ConsumoDiarioAutomatico**: una fila por **lote/recría o madre** en esa fecha: etapa, cantidad de animales, receta (InsumoCompuesto), cantidad diaria por animal, cantidad total de receta, flag `procesado`.
- **ConsumoDiarioDetalle**: por cada **componente** de la receta consumida: tipo (INSUMO, GRANO_PROPIO, INSUMO_COMPUESTO), cantidad requerida, disponible, descontada, stock resultante, déficit, porcentaje cobertura.

Flujo: **DiaAlimentacion** → N **ConsumoDiarioAutomatico** (uno por recría/madre) → cada uno con N **ConsumoDiarioDetalle** (uno por componente de la receta).

### 2.3 Consumo manual (ConsumoAlimento)

- **ConsumoAlimento**: categoría (MADRES, PADRILLOS, RECRIA, ENGORDE, LECHONES), fecha, cantidad kg, tipo (BALANCEADO, MAIZ, GRANO_PROPIO), opcionalmente cultivoRelacionadoId, loteId, madre. No está ligado a recetas ni a ConsumoDiarioAutomatico; es un registro histórico manual.

### 2.4 Trazabilidad e inventario

- **MovimientoStockPorcino**: cada descuento (consumo automático, derrame, ajuste, etc.) genera movimientos con tipo, fecha, tipo insumo (INSUMO, GRANO_PROPIO, INSUMO_COMPUESTO), cantidad, stock anterior/posterior, permiteNegativo, y referencia a consumo diario, derrame o día alimentación.
- El **descuento real** de stock lo hace **core.inventory** (`InventoryService`): consumir insumo, grano o insumo compuesto. Porcinos solo registra el movimiento en `MovimientoStockPorcino` y llama a `inventoryService.consumir...` o `consumir...PermitiendoNegativo`.

---

## 3. Flujo del consumo diario automático

### 3.1 Disparo

- **Job programado:** `ConsumoDiarioAutomaticoService.generarConsumoDiarioAutomatico()` con `@Scheduled(cron = "0 30 0 * * ?")` (todos los días a las 00:30).
- Genera el consumo del **día anterior** para **todas las empresas activas**.
- También se puede invocar **manual** vía API: `POST /api/porcinos/calendario-alimentacion/generar-consumo?fecha=YYYY-MM-DD`. Solo genera si el día no está confirmado (o no existe).

### 3.2 Pasos del servicio (por empresa y fecha)

1. **Día existente:** Si ya existe `DiaAlimentacion` para esa fecha y empresa:
   - Si está **confirmado** → no se recalcula, se retorna.
   - Si está **pendiente** → se borran los `ConsumoDiarioAutomatico` y detalles previos y se recalcula.

2. **Cálculo de consumos (recrías):**
   - Recrías activas de la empresa con fecha ingreso ≤ fecha y (sin fecha salida o fecha salida ≥ fecha) y cantidad animales > 0.
   - Para cada recría:
     - **Etapa de alimentación:** Se usa `Recria.etapa` si existe (mapeo F1→F1, F2→F2, …); si no, por **edad en días** desde ingreso: F1 (0–35), F2 (36–70), F3 (71–105), F4 (106–140), DESARROLLO (141–180), TERMINACION (>180).
     - **Receta:** `RecetaAlimentacionPorEtapa` para esa etapa (por defecto o primera activa).
     - **Cantidad total** = cantidadDiariaPorAnimal × cantidadAnimales.
     - Se crea un `ConsumoDiarioAutomatico` (recría, etapa, receta, cantidades).

3. **Cálculo de consumos (madres):**
   - Madres activas (sin baja o fecha baja ≥ fecha).
   - Para cada madre:
     - **Etapa:** Si tiene **gestación activa** en la fecha → GESTACION. Si tiene **parto sin destete** y la fecha está dentro del período de lactancia (días configurables, ej. 21) → LACTANCIA. Por defecto GESTACION.
     - **Receta** para esa etapa y **cantidad** = cantidadDiariaPorAnimal (1 “animal” = 1 madre).
     - Se crea un `ConsumoDiarioAutomatico` (madre, etapa, receta).

4. **Procesamiento de cada consumo:**
   - Se persiste el `ConsumoDiarioAutomatico` y se llama a `procesarComponentesReceta`:
     - Se obtienen los **componentes** de la receta (InsumoCompuesto) desde `ComponenteInsumoCompuesto`.
     - Por cada componente se calcula la **cantidad necesaria** (según cantidad total de receta y proporción del componente).
     - **Descuento según tipo:**
       - **INSUMO:** `inventoryService.consultarStock` + `consumirPermitiendoNegativo` (Insumo).
       - **GRANO_PROPIO:** `consultarStockGrano` + `consumirGranoPermitiendoNegativo` (Cultivo) — aquí el servicio depende de `CultivoRepository` (módulo Cultivos).
       - **INSUMO_COMPUESTO:** `consultarStockInsumoCompuesto` + `consumirInsumoCompuestoPermitiendoNegativo`.
     - Se crea **ConsumoDiarioDetalle** (requerido, disponible, descontado, resultante, déficit, cobertura).
     - Se registra **MovimientoStockPorcino** (tipo CONSUMO_AUTOMATICO, fecha, insumo/grano/compuesto, cantidades, permiteNegativo = true).
   - Se marca el consumo como `procesado` y se actualizan totales del día (lotes, animales, recetas, insumos, alertas).

### 3.3 Reglas importantes

- **No se recalcula** un día ya confirmado (estado CONFIRMADO o CON_CORRECCIONES).
- **Stock negativo permitido** solo en consumo automático (`consumirPermitiendoNegativo`, `consumirGranoPermitiendoNegativo`, etc.). Los detalles guardan déficit y porcentaje de cobertura para alertas.
- **Trazabilidad:** Cada descuento queda en `MovimientoStockPorcino` y en `ConsumoDiarioDetalle`.

---

## 4. Calendario de alimentación

- **API base:** `/api/porcinos/calendario-alimentacion` (sin `/v1` a diferencia de otros porcinos).
- **Servicio:** `CalendarioAlimentacionService`.

Funcionalidad:

- **Calendario mensual:** `GET /mensual?ano=...&mes=...` → mapa fecha → DTO del día (estado, totales, alertas).
- **Detalle de un día:** `GET /dia/{fecha}` → consumos del día con detalles, nombres de lote/receta/componente y alertas de stock (déficit por insumo).
- **Confirmar día:** `POST /dia/{fecha}/confirmar` (body opcional con observaciones) → marca el día como CONFIRMADO o CON_CORRECCIONES y evita regeneraciones.
- **Alertas:** `GET /alertas?fechaDesde=...&fechaHasta=...` → lista de alertas de stock insuficiente en ese rango.
- **Derrames/pérdidas:** `POST /derrames-perdidas`, `GET /derrames-perdidas`, `GET /derrames-perdidas/dia/{fecha}` → alta y consulta de derrames (descuentan inventario vía `DerramePerdidaService`).
- **Generar consumo manual:** `POST /generar-consumo?fecha=...` → llama a `ConsumoDiarioAutomaticoService.generarConsumoDiarioParaFecha` para la empresa del usuario (solo si el día no está confirmado).

---

## 5. Consumo manual (ConsumoAlimento)

- **API:** `ConsumoAlimentoController` en `/api/v1/porcinos/alimentacion`.
  - `POST /consumos` → registrar consumo.
  - `GET /consumos?fechaDesde=...&fechaHasta=...` → listar.
  - `GET /consumos/total-categoria?categoria=...&fechaDesde=...&fechaHasta=...` → total kg por categoría.

- **Lógica en servicio:**  
  - Validación de cantidad > 0 y empresa.  
  - **GRANO_PROPIO** con `cultivoRelacionadoId`: descuento vía `inventoryService.consumirGrano` (no permite negativo).  
  - **GRANO_PROPIO** sin cultivo: se busca `StockAlimento` por nombre (ej. “Grano Propio”); si existe se descuenta ahí; si no, se permite el consumo “sin validación estricta”.  
  - **BALANCEADO:** no hay validación/descuento de inventario en el código (comentario: “en producción se debería tener un control de stock de balanceados”).

Por tanto, el consumo manual **no** genera `ConsumoDiarioAutomatico` ni `ConsumoDiarioDetalle`; es un registro independiente y solo en algunos casos descuenta inventario (grano propio con cultivo o StockAlimento).

---

## 6. Fórmulas en backend y frontend

### 6.1 Backend

- **FormulaController** (`/api/v1/porcinos/formulas`): **stub**. `GET` devuelve lista vacía; `POST` devuelve mapa vacío. No hay implementación real.
- **InsumoCompuestoController** (`/api/v1/insumos-compuestos`): aquí está la lógica real:
  - CRUD de InsumoCompuesto (recetas).
  - `POST /{id}/asociar-etapa` → crea/actualiza **RecetaAlimentacionPorEtapa** (receta + etapa + cantidadDiariaPorAnimal).
  - `GET /etapas/{etapa}` → recetas por etapa.
  - `GET /etapas/{etapa}/por-defecto` → receta por defecto de la etapa.
  - `GET /{id}/calcular-preparacion` → cálculo de ingredientes necesarios y máximo preparable (sin descontar).
  - `POST /{id}/preparar` → preparar receta (descontar ingredientes vía InventarioPorcinoService / InventoryService).

Las “fórmulas” por etapa que usa el consumo automático son, por tanto, las **RecetaAlimentacionPorEtapa** asociadas a **InsumoCompuesto** y gestionadas desde InsumoCompuestoController.

### 6.2 Frontend

- **FormulaListScreen** usa `alimentacionService.listarFormulas()` y `obtenerFormulasPorEtapa()`, que llaman a **PORCINOS_FORMULAS** (`/v1/porcinos/formulas` y `/v1/porcinos/formulas/etapa/{etapa}`). Esas rutas las sirve el **FormulaController stub**, por lo que la pantalla “Fórmulas de Alimentación” **siempre verá lista vacía** (salvo que el front cambie a usar insumos-compuestos + etapas).
- **InsumosCompuestosScreen** probablemente use `/api/v1/insumos-compuestos` para listar/crear recetas; ahí sí hay datos.
- **ConsumosScreen** usa `consumoAlimentoService` con `PORCINOS_ALIMENTACION.CONSUMOS` (`/v1/porcinos/alimentacion/consumos`), que sí coincide con `ConsumoAlimentoController` (consumo manual).

Resumen: la **lista de fórmulas por etapa** que el usuario esperaría en “Fórmulas” no está conectada al backend real; el backend real está en **InsumoCompuesto** + **RecetaAlimentacionPorEtapa** vía `/api/v1/insumos-compuestos` y no vía `/api/v1/porcinos/formulas`.

---

## 7. Integración con inventario

- **Core (InventoryService):**  
  - Consultas: `consultarStock`, `consultarStockGrano`, `consultarStockInsumoCompuesto`.  
  - Consumo: `consumir`, `consumirPermitiendoNegativo`, `consumirGrano`, `consumirGranoPermitiendoNegativo`, `consumirInsumoCompuestoPermitiendoNegativo`.  
  - Origen registrado: `InventoryOrigin.PORCINOS` con referencia (ej. consumo diario id, evento sanitario id).

- **Porcinos** no modifica stock “a mano”: siempre pasa por `InventoryService` para insumos, grano e insumo compuesto, y además persiste **MovimientoStockPorcino** para trazabilidad.

- **Dependencia de Cultivos:** Para **GRANO_PROPIO**, `ConsumoDiarioAutomaticoService` usa `CultivoRepository` y la entidad `Cultivo` (cultivos.domain) porque el componente de la receta puede tener un `Cultivo` asociado. Eso acopla el módulo Porcinos al módulo Cultivos en este flujo.

---

## 8. Derrames y pérdidas

- **DerramePerdida:** entidad porcinos; se registra con insumo/insumo compuesto, cantidad, motivo, fecha, etc.
- **DerramePerdidaService:** al registrar, descuenta del inventario vía `InventoryService` (o lógica equivalente) y registra **MovimientoStockPorcino** (tipo DERRAME/PERDIDA). No permite negativo por defecto (comportamiento a confirmar en código).
- Expuesto en **CalendarioAlimentacionController** (derrames-perdidas).

---

## 9. Resumen: cómo se maneja la alimentación

| Aspecto | Cómo lo maneja el sistema |
|---------|----------------------------|
| **Definición de “fórmula”** | InsumoCompuesto (receta) con componentes (insumos, grano, sub-recetas). Asociación a etapa por empresa vía RecetaAlimentacionPorEtapa (cantidad diaria por animal, opcional por defecto). |
| **Consumo diario automático** | Job 00:30 para día anterior; por empresa calcula consumos por recría y madre según etapa y receta por etapa; descompone cada receta en componentes y descuenta vía InventoryService (permite negativo); persiste DiaAlimentacion, ConsumoDiarioAutomatico, ConsumoDiarioDetalle y MovimientoStockPorcino. |
| **Etapa recría** | Recria.etapa o, si no, por edad en días desde ingreso (F1→TERMINACION). |
| **Etapa madre** | Gestación activa → GESTACION; parto sin destete en período lactancia → LACTANCIA (días configurables); por defecto GESTACION. |
| **Calendario** | Un DiaAlimentacion por empresa+fecha; estados PENDIENTE/CONFIRMADO/CON_CORRECCIONES; confirmación bloquea regeneración; alertas por déficit de stock en detalles. |
| **Consumo manual** | ConsumoAlimento: categoría, tipo, cantidad kg; solo en algunos casos descuenta (grano propio con cultivo o StockAlimento); BALANCEADO sin control de stock. No se mezcla con consumo automático. |
| **Fórmulas en UI** | Pantalla “Fórmulas” llama a API stub (/porcinos/formulas) → lista vacía. La lógica real está en InsumoCompuesto + asociar-etapa/etapas (insumos-compuestos). |
| **Inventario** | Todo el descuento porcinos pasa por core.inventory (InventoryService); Porcinos además registra MovimientoStockPorcino. Para grano propio el servicio de consumo automático depende de Cultivo (Cultivos). |

---

## 10. Puntos débiles y recomendaciones

- **Fórmulas en front:** Conectar FormulaListScreen (y detalle/alta de fórmulas por etapa) a InsumoCompuestoController + RecetaAlimentacionPorEtapa (p. ej. insumos-compuestos + asociar-etapa y etapas/{etapa}), o implementar el FormulaController delegando en esa lógica y devolviendo DTOs consistentes con RecetaAlimentacionPorEtapa.
- **Consumo manual BALANCEADO:** Definir si debe descontar de un insumo/stock concreto y aplicar validación y descuento; hoy no descuenta.
- **Dos flujos de consumo:** ConsumoDiarioAutomatico (por receta y etapa) y ConsumoAlimento (manual por categoría/tipo) no están unificados; reportes o dashboards que quieran “todo el consumo” deben sumar ambos orígenes.
- **Dependencia de Cultivos:** ConsumoDiarioAutomaticoService usa Cultivo y CultivoRepository para GRANO_PROPIO; a largo plazo conviene un puerto en core (ej. “consulta/descuento de grano por empresa/cultivoId”) implementado por el módulo que tenga Cultivo, para desacoplar Porcinos de Cultivos.
- **Documentación de etapas:** Dejar documentado en un solo lugar el mapeo edad → etapa (F1–TERMINACION) y los días de lactancia por defecto/configurables, para evitar divergencias entre código y negocio.

Con esto queda descrito cómo el sistema maneja actualmente la parte de alimentación en Porcinos y qué ajustes serían útiles para coherencia y mantenibilidad.
