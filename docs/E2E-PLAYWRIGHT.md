# Tests e2e Playwright — AgroGestion Frontend

## Requisitos

- Node 18+
- Backend en `http://localhost:8080` (solo tests con login autenticado)
- Frontend en `http://localhost:3000` (Playwright puede levantarlo con `npm run dev`)

## Instalación

```bash
cd agrogestion-frontend
npm install
npm run e2e:install
cp .env.e2e.example .env.e2e.local
# Completar E2E_EMAIL y E2E_PASSWORD para tests autenticados
```

## Ejecutar

```bash
# Smoke UI (login sin backend): 2 tests
npm run e2e:smoke

# Suite completa (omite tests autenticados si faltan credenciales)
npm run e2e

# Con credenciales (PowerShell)
$env:E2E_EMAIL = 'tu@email.com'
$env:E2E_PASSWORD = 'tu-password'
npm run e2e

# Frontend ya corriendo
$env:E2E_SKIP_WEB_SERVER = 'true'
npm run e2e:smoke
```

## Cobertura actual

| Archivo | Qué valida |
|---------|------------|
| `e2e/smoke/login.spec.ts` | Formulario login, validación campos, login → dashboard (opcional) |
| `e2e/smoke/maquinaria.spec.ts` | Pantalla `/cultivos/maquinaria` tras login (opcional) |

## CI

Job `e2e-smoke` en `.github/workflows/e2e-playwright.yml`: ejecuta solo smoke de login (sin credenciales).
