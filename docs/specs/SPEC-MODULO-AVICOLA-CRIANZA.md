# SPEC — Módulo Avícola Crianza (pollos parrilleros)

**Versión:** 2.0  
**Fecha:** Junio 2026  
**Metodología:** SDD  
**Estado:** Aprobada  
**Cambio v2.0:** Unificación con módulo `AVICOLA_CARNE` (deprecado). Un solo producto comercial.

---

## 1. Objetivo

Gestionar **crianza de pollos parrilleros** (engorde batch): galpones, razas, lotes, pesadas, mortalidad informativa, consumos (inventario CORE), sanidad, faena/venta, reportes zootécnicos y calendario.

---

## 2. Módulo y seguridad

| Concepto | Valor |
|----------|--------|
| Código `modules.code` | `AVICOLA_CRIANZA` |
| `@RequiresModule` | `"AVICOLA_CRIANZA"` |
| API | `/api/avicola-crianza` |
| Frontend | `/avicola-crianza` |
| Inventario CORE | `InventoryOrigin.AVICOLA_CRIANZA` |
| `modulo_origen` datos | `AVICOLA_CRIANZA` |

**Deprecado:** `AVICOLA_CARNE` — no ofertar a clientes nuevos; datos migrados a crianza (V1_157).

---

## 3. Reglas de negocio (v2)

### Mortalidad
- Registra en `avicola_muerte`.
- **No** descuenta `cantidad_animales`.
- KPI: `cantidadDisponible = plantel − suma(muertes)`; `mortalidadPct = suma(muertes) / cantidadInicial × 100`.

### Venta / faena
- Descuenta `cantidad_animales`.
- Cierre automático si plantel = 0.
- Hereda `campana_id` del lote.

### Consumo
- Egreso inventario `AVICOLA_CRIANZA`.
- Edición con reversión de stock (`PUT /lotes/{id}/consumos/{consumoId}`).

### Cierre manual
- `POST /lotes/{id}/cierre` con confirmación si quedan aves en plantel.

### Reportes
- Resumen empresa, análisis por lote, curva de peso.

---

## 4. Tablas

`avicola_establecimiento`, `avicola_raza`, `avicola_lote`, `avicola_pesada`, `avicola_muerte`, `avicola_venta`, `avicola_consumo`, `avicola_evento_sanitario`.

---

## 5. Referencias

- Diseño: `DISENO-TECNICO-UNIFICACION-AVICOLA-CRIANZA.md`
- Obsoleto: `SPEC-MODULO-AVICOLA-CARNE.md`
