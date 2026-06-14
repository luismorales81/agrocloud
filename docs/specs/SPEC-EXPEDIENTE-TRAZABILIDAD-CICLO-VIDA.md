# SPEC: Expediente de trazabilidad — ciclo de vida del producto

**Estado:** aprobada para implementación v1  
**Fecha:** 2026-05-16  
**Relacionada con:** `SPEC-TRAZABILIDAD-COMERCIAL-CERTIFICABLE.md` (certificaciones con reglas; distinto de este expediente)

## Objetivo

Permitir generar un **documento PDF** (y snapshot JSON) con el **detalle del ciclo de vida** y datos de origen de un producto/lote, para:

- **Cultivos:** lote, ciclo de cosecha
- **Porcinos:** recría, venta
- **Avícola:** lote huevos, lote crianza/carne, galpón ponedoras

Sin reglas de rechazo comercial (siempre resultado VALIDO). Complementa las certificaciones `LIBRE_AGROQUIMICOS` / `SIN_ANTIBIOTICOS`.

## API

- `POST /api/trazabilidad/expedientes` — genera expediente
- `GET /api/trazabilidad/reportes` — listado (incluye expedientes con `certificacionCodigo = EXPEDIENTE_CICLO_VIDA`)
- `GET /api/trazabilidad/reportes/{id}/pdf` — descarga PDF

## Tipos de entidad (`entidadTipo`)

| Código | Módulo | Descripción documento |
|--------|--------|------------------------|
| `LOTE` | Cultivos | Parcela + labores + insumos + sanitarios porcinos en parcela |
| `COSECHA` | Cultivos | Ciclo siembra–cosecha del historial |
| `RECRIA` | Porcinos | Recría + pesadas + consumos + sanitarios + movimientos stock |
| `VENTA_PORCINO` | Porcinos | Venta + recría asociada si existe |
| `AVICOLA_HUEVOS` | Avícola huevos | Lote postura + producción + consumos |
| `AVICOLA_CRIANZA` | Avícola crianza | Lote `avicola_lote` |
| `AVICOLA_CARNE` | Avícola carne | Lote parrillero |
| `AVICOLA_PONEDORAS` | Avícola ponedoras | Galpón |

## Contenido mínimo del PDF (v1)

1. Identificación (empresa, tipo, id, título)
2. Origen / identidad del producto
3. Línea de tiempo (eventos ordenados por fecha)
4. Insumos y labores (cultivos)
5. Sanidad (porcinos/avícola donde aplique)
6. Producción / salida (cosecha, venta, postura)
7. Hashes e id de reporte (TRC-{id})

## Frontend

Pantalla compartida por módulo: elegir tipo de alcance, entidad, generar y descargar PDF.

## Fuera de alcance v1

- Trazabilidad genética/genealogía completa de reproductoras
- Integración con blockchain
- QR en etiqueta física
