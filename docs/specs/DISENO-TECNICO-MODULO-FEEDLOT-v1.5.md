# Diseño técnico — Módulo Feedlot v1.5

**Versión:** 1.0  
**Fecha:** Junio 2026  
**Estado:** Aprobada (implementación autorizada)  
**SPEC funcional:** `SPEC-MODULO-FEEDLOT.md` v1.1  
**Base:** `DISENO-TECNICO-MODULO-FEEDLOT.md` (v1)

---

## 1. Alcance v1.5

Extensión analítica y transversal del módulo feedlot (SPEC §11.2):

1. Tablas dietas, fases, lecturas comedero (bunk score)
2. Configuración `metodo_closeout` por empresa
3. Reportes: curva peso, proyección, breakeven, Excel comparativa
4. Closeout PDF
5. Calendario ámbito `FEEDLOT`
6. Expediente trazabilidad `FEEDLOT_LOTE`
7. Consumo teórico vs real (por dieta/fase)

---

## 2. Migración Flyway `V1_154__Feedlot_v1_5.sql`

```sql
-- feedlot_configuracion_empresa (1:1 empresa)
-- feedlot_dieta, feedlot_dieta_fase
-- feedlot_lectura_comedero
-- ALTER feedlot_lote ADD dieta_id FK nullable
```

Enums Java: `FeedlotMetodoCloseout`, `FeedlotBunkScore`.

---

## 3. API nuevos endpoints

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET/POST/PUT | `/dietas`, `/dietas/{id}/fases` | CRUD dietas |
| GET/POST/PUT/DELETE | `/lotes/{id}/lecturas-comedero` | Bunk score |
| GET | `/reportes/lote/{id}/curva-peso` | Serie + proyección |
| GET | `/reportes/exportar` | Excel período activo |
| GET | `/lotes/{id}/closeout/pdf` | PDF closeout |
| GET/PUT | `/configuracion/closeout` | Deads-in/out |
| GET | `/lotes/{id}/consumo-teorico` | Teórico vs real del día/rango |

Calendario: `GET /api/calendario/feedlot` (controller CORE).

Expediente: `FEEDLOT_LOTE` en trazabilidad comercial.

---

## 4. Servicios backend

- `ServicioFeedlotDietas` — CRUD + cálculo teórico por fase
- `ServicioFeedlotBunk` — lecturas comedero
- `ServicioFeedlotReportes` — curva, export Excel
- `GeneradorPdfFeedlotCloseout` — PDFBox
- Ampliar `ServicioFeedlotCloseout` — lee config empresa, head days por intervalos

---

## 5. Frontend

- `ReportesFeedlotScreen`, `DietasFeedlotScreen`, `CalendarioFeedlotScreen`
- `FeedlotCloseoutModal` + descarga PDF
- Pestañas Comederos/Dieta en detalle lote
- `configuracionExpedienteFeedlot`

---

## 6. Tests

- `ServicioFeedlotDietasTest`, `ServicioFeedlotReportesTest`
- Integración calendario feedlot, expediente FEEDLOT_LOTE
- QA E2E browser: usuario **`admin@agrocloud.com`** en empresa **AgroCloud Demo** (no `superman` de plataforma) — ver SPEC §15

---

**Aprobación:** Junio 2026 — implementación autorizada en plan Feedlot v1→v1.5.
