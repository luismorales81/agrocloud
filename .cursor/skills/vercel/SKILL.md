---
name: vercel
description: Guía para desplegar y configurar el frontend de AgroGestion en Vercel. Usar cuando se hable de Vercel, deploy, vercel.json, variables de entorno o build del frontend.
---

# Skill Vercel – AgroGestion

## Cómo funciona Vercel en este proyecto

- **Frontend**: la app React/Vite (`agrogestion-frontend`) se despliega en **Vercel** como sitio estático (SPA).
- **Backend**: corre en **Railway** (no en Vercel). La URL de API en producción está en `vercel.json` como `VITE_API_BASE_URL`.

### Archivos relevantes

| Archivo | Uso |
|--------|-----|
| `agrogestion-frontend/vercel.json` | Configuración de build, rutas y variables de entorno para Vercel. |
| `agrogestion-frontend/.vercelignore` | Archivos/carpetas que Vercel no sube (node_modules, .env.*, etc.). |
| `agrogestion-frontend/package.json` | Scripts de build; Vercel usa `@vercel/static-build` y el `distDir` indicado en vercel.json. |

### Build en Vercel

1. Vercel usa **Root Directory** = `agrogestion-frontend` (o el repo apuntando a esa carpeta).
2. Comando de build: el que esté configurado en el proyecto (por ejemplo `npm run build` / `vite build`).
3. Directorio de salida: `dist` (definido en vercel.json como `distDir`).
4. Las variables que empiezan por `VITE_` se inyectan en build; en este proyecto la importante es `VITE_API_BASE_URL` (API del backend en Railway).

### Rutas (SPA)

- Todas las rutas que no sean archivos estáticos se sirven con `index.html` (fallback SPA).
- `sw.js`, `manifest.json`, favicon e iconos tienen rutas y cabeceras propias en vercel.json para cache y tipo de contenido.

## Qué hacer cuando se trabaje con Vercel

1. **Cambios en vercel.json**
   - Mantener `version: 2`, `builds` con `@vercel/static-build` y `distDir: "dist"`.
   - No borrar las rutas de SPA ni las de `sw.js`/`manifest.json` salvo que se cambie la estrategia de PWA.
   - Si se añade una variable de entorno por defecto, hacerlo en `vercel.json` > `env` y/o documentar que debe configurarse en el dashboard de Vercel.

2. **Variables de entorno**
   - En producción Docker (Caddy, mismo origen) `VITE_API_BASE_URL` es `/api`.
- El dashboard de Vercel queda como legado: no volver a desplegar el SPA ahí tras el cutover al VPS.
   - Recordar que las variables `VITE_*` son públicas (se incluyen en el bundle del frontend).
   - Para secrets o URLs sensibles, usar el dashboard de Vercel (Project > Settings > Environment Variables) y no commitear valores de producción en vercel.json si no es necesario.

3. **Build y errores de deploy**
   - Si el build falla en Vercel, revisar que en el proyecto de Vercel el **Root Directory** sea `agrogestion-frontend` (o la carpeta donde está el frontend).
   - Revisar que existan los scripts `build` (y si aplica `prebuild`) en `package.json` del frontend.
   - Los logs de build en Vercel indican si falla por dependencias, variables faltantes o errores de TypeScript/Vite.

4. **Preview y producción**
   - Cada push/PR puede generar un **Preview Deployment** con su propia URL.
   - La rama de producción (p. ej. `main`) despliega a la URL principal del proyecto.
   - Si el frontend llama a la API por nombre de host (p. ej. `vercel.app`), el backend debe permitir CORS para ese origen; en este proyecto eso se configura en el backend (Railway).

## Resumen rápido

- Frontend → Vercel (estático, SPA).
- Backend → Railway; la URL se configura con `VITE_API_BASE_URL`.
- No tocar la estructura de rutas SPA en vercel.json sin motivo.
- Variables `VITE_*` en Vercel (dashboard o vercel.json) según entorno.
