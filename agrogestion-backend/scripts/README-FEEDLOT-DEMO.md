# Demo y QA — Módulo Feedlot

## Actores (no confundir)

| Rol | Usuario | Cuándo usarlo |
|-----|---------|---------------|
| **SUPERADMIN (plataforma)** | `superman` | Habilitar módulo FEEDLOT, empresas, usuarios globales (`/api/admin-global`). |
| **Administrador de empresa** | `admin@agrocloud.com` / `admin123` (local) | **Todo el QA funcional** del feedlot: lotes, operaciones, closeout, reportes, calendario, expediente. |
| **Operario** (opcional) | Usuario con rol OPERARIO en AgroCloud Demo | Validar permisos de escritura limitados. |

**Importante:** `superman` omite la validación `@RequiresModule` (`ModuleAccessInterceptor`). Probar el módulo con superman **no** valida permisos reales de un cliente.

Referencia: [`GUIA_DEMO_CLIENTE.md`](../GUIA_DEMO_CLIENTE.md), SPEC §15 en [`docs/specs/SPEC-MODULO-FEEDLOT.md`](../../docs/specs/SPEC-MODULO-FEEDLOT.md).

---

## Preparación de datos (una vez)

```powershell
cd agrogestion-backend\scripts

# 1. Habilitar módulo + catálogos + corrales en AgroCloud Demo
.\ejecutar-habilitar-feedlot-demo.ps1 -DbHost localhost -DbUser root -DbPass 123456 -DbName agrocloud

# 2. Lote demo con pesadas y muerte (~90 días)
.\ejecutar-insertar-lote-demo.ps1 -DbHost localhost -DbUser root -DbPass 123456 -DbName agrocloud
```

Requisitos: migraciones Flyway `V1_153` y `V1_154` aplicadas (arrancar backend).

---

## QA E2E en browser

1. Login: **`admin@agrocloud.com`** / **`admin123`**
2. Seleccionar empresa: **AgroCloud Demo**
3. Recorrer checklist SPEC §15:

| Ruta | Verificar |
|------|-----------|
| `/feedlot/panel` | KPIs cargan |
| `/feedlot/lotes` → lote demo | Detalle, pestañas operativas |
| Closeout | Modal + PDF |
| `/feedlot/reportes` | Gráficos + Excel |
| `/feedlot/dietas` | CRUD; asignar dieta al lote |
| `/feedlot/calendario` | Eventos FEEDLOT |
| `/feedlot/expediente` | PDF `FEEDLOT_LOTE` |
| `/feedlot/configuracion/closeout` | Deads-in / deads-out |

---

## Tests automatizados (backend)

```powershell
cd agrogestion-backend
.\mvnw.cmd test -Dtest="ServicioFeedlot*,FeedlotControllerIntegracionTest"
```

Los tests de integración simulan un **administrador de empresa**, no SUPERADMIN.

---

## Verificación API (smoke test)

Con backend en `localhost:8080`, login `admin@agrocloud.com` / `admin123`:

| Endpoint | HTTP |
|----------|------|
| `GET /api/feedlot/panel/resumen` | 200 |
| `GET /api/feedlot/lotes` | 200 |
| `GET /api/feedlot/lotes/1` | 200 |
| `GET /api/feedlot/lotes/1/closeout` | 200 |
| `GET /api/feedlot/lotes/1/closeout/pdf` | 200 |
| `GET /api/feedlot/dietas` | 200 |
| `GET /api/feedlot/reportes/analisis-lotes` | 200 |
| `GET /api/feedlot/reportes/exportar` | 200 |
| `GET /api/feedlot/configuracion/closeout` | 200 |

Pendiente: recorrido manual en browser (checklist SPEC §15, ítems calendario/expediente/campaña cerrada).
