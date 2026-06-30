# Diseño técnico — Módulo Avícola Carne

**Versión:** 1.0  
**Fecha:** Junio 2026  
**SPEC:** `SPEC-MODULO-AVICOLA-CARNE.md` v1.1  
**Estado:** Aprobado (implementación)

---

## 1. Migración de datos

| Archivo | Cambio |
|---------|--------|
| `V1_156__avicola_modulo_origen_carne.sql` | Columna `modulo_origen` en `avicola_establecimiento`, `avicola_raza`, `avicola_lote` (default `AVICOLA_CRIANZA`) + índices |

Enum Java: `AvicolaModuloOrigen` (`AVICOLA_CRIANZA`, `AVICOLA_CARNE`).

---

## 2. Backend — servicios

| Servicio | Responsabilidad |
|----------|-----------------|
| `ServicioAvicolaCarneLotes` | CRUD lotes filtrados por `AVICOLA_CARNE`; cierre manual |
| `ServicioAvicolaCarneCatalogos` | CRUD galpones/razas vía `ServicioAvicolaCrianzaCatalogo` + módulo CARNE |
| `ServicioAvicolaCarneOperaciones` | Pesadas, muertes (sin descuento), ventas, consumos, sanidad, edición consumos |
| `ServicioAvicolaCarneReportes` | Resumen empresa, análisis lotes, curva peso |
| `ServicioAvicolaCrianzaLote` | Overloads con `AvicolaModuloOrigen` para reutilización |

Controller: `AvicolaCarneController` — base `/api/avicola-carne`.

Calendario: `GET /api/calendario/avicola-carne` — tareas recurrentes ámbito `AVICOLA_CARNE`.

---

## 3. API REST (contratos)

### Lotes
- `GET/POST /lotes`, `GET/PUT /lotes/{id}`
- `POST /lotes/{id}/cierre` — body opcional `{ confirmarConAvesPendientes: true }`
- Operaciones hijas: pesadas, muertes, ventas, consumos, eventos-sanitarios, resumen
- `PUT /lotes/{id}/consumos/{consumoId}` — edición con ajuste inventario

### Catálogos
- `GET/POST/PUT /establecimientos`, `GET/POST/PUT /razas`

### Reportes
- `GET /reportes/resumen`
- `GET /reportes/analisis-lotes`
- `GET /reportes/lote/{id}/curva-peso`

---

## 4. Frontend — rutas `/avicola-carne`

| Ruta | Pantalla |
|------|----------|
| `/panel` | `AvicolaCarneDashboard` |
| `/dashboard` | `CalendarioCarneScreen` → `/calendario/avicola-carne` |
| `/lotes` | `AvicolaCarneListadoScreen` (alta/edición en diálogo) |
| `/lotes/:id` | `AvicolaCarneDetalleLoteScreen` |
| `/establecimientos` | `EstablecimientosCarneScreen` |
| `/establecimientos-mapa` | `UbicacionEstablecimientosCarneScreen` |
| `/razas` | `RazasCarneScreen` |
| `/insumos` | `InsumosCarneScreen` |
| `/reportes` | `ReportesCarneScreen` |
| `/configuracion/periodos` | `GestionCampanasScreen` |

Servicio: `avicolaCarneService.ts` — sin llamar a `avicolaCrianzaApi` desde UI carne.

---

## 5. Tests

- `ServicioAvicolaCarneOperacionesTest` — mortalidad sin descuento, lote cerrado.

---

## 6. Riesgos mitigados

- Coexistencia crianza+carne: filtro `modulo_origen` en listados y altas.
- Empresa solo-carne: API catálogos autónoma bajo `/avicola-carne`.
- Mortalidad: mensaje en listado y detalle.
