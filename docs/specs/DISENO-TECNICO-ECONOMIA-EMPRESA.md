# Diseño técnico — Economía de la empresa

**Versión:** 1.0  
**Fecha:** Octubre 2026  
**SPEC:** `SPEC-ECONOMIA-EMPRESA.md` (aprobada)  
**Estado:** En implementación local

## Decisiones

- No hay tablas nuevas de finanzas. Se agregan columnas a `cultivo_ingresos` y `cultivo_egresos`: `empresa_id`, `modulo_origen`, `imputacion`, `tipo_referencia`, `referencia_hecho_id`.
- `lecheria_venta_leche` gana `ingreso_id`, igual que las ventas de feedlot, porcinos y avícola.
- Un solo caso de uso, `ServicioLibroEmpresa`, escribe el libro y llama a `InventoryService`.
- La compra de insumo actualiza `precioUnitario` con el promedio ponderado (`CalculoCostoInsumo`) y después llama a `reponer` con el id del egreso.
- Anular la compra llama a `consumir` solo si el saldo alcanza. Si no, no cambia el egreso.
- Las ventas nuevas con total mayor que cero crean un `Ingreso` y guardan `ingreso_id`. Si ya hay id, no crean otro.
- El balance general (`GET /api/v1/balance/general`) suma el libro de la empresa en el rango de fechas. No suma costos de labores, para no contar dos veces el insumo ya comprado.
- La pantalla de finanzas es la misma en todos los módulos. El filtro trae las líneas del módulo y las compras de imputación `COMPARTIDA`.
- Los egresos de tipo insumo que hoy restaban stock en el controlador dejan de tocar el saldo. La única entrada de stock por compra es `POST /api/v1/economia/compras`.

## API local

| Método | Ruta | Uso |
|--------|------|-----|
| GET | `/api/v1/economia/egresos?modulo=&incluirCompartidas=` | Libro de egresos |
| GET | `/api/v1/economia/ingresos?modulo=` | Libro de ingresos |
| POST | `/api/v1/economia/compras` | Reponer insumo ya cargado |
| POST | `/api/v1/economia/compras/{id}/anulacion` | Anular compra |
| POST | `/api/v1/economia/egresos` | Gasto manual, sin stock |
| POST | `/api/v1/economia/ingresos` | Ingreso manual |
| POST | `/api/v1/economia/ingresos/{id}/anulacion` | Cancelar ingreso |

## Fuera de este corte

- No se anula la venta desde la pantalla del lote: todavía no existe esa acción. Anular el ingreso del libro deja la línea cancelada y el `ingreso_id` apuntando a ella.
- No se generan egresos automáticos de mano de obra ni de alquiler.
- No se renombran las tablas `cultivo_*`.
