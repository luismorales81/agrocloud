# Solución: Problema de Caché en Vercel

## 🔍 Problema Identificado

Estás viendo en Vercel (producción) una versión antigua del login con "cajas de usuarios para ingresar", pero localmente ves la versión correcta sin esas cajas.

## 🎯 Causa del Problema

El problema es causado por **caché en múltiples niveles**:

1. **Service Worker**: Está cacheando la versión antigua (`v1.0.1`)
2. **Caché del navegador**: Puede tener archivos antiguos almacenados
3. **Caché de Vercel**: Puede estar sirviendo un build anterior

## ✅ Soluciones Aplicadas

### 1. Service Worker Actualizado

Se actualizó el Service Worker de `v1.0.1` a `v1.1.0` para forzar la limpieza del caché antiguo.

**Archivo**: `agrogestion-frontend/public/sw.js`

```javascript
const CACHE_NAME = 'agrocloud-v1.1.0'; // Nueva versión
const STATIC_CACHE = 'agrocloud-static-v1.1.0';
const DYNAMIC_CACHE = 'agrocloud-dynamic-v1.1.0';
const API_CACHE = 'agrocloud-api-v1.1.0';
```

### 2. Verificación de Componentes

✅ **Componente correcto en uso**: `src/pages/Login.tsx` (el nuevo, sin cajas)
❌ **Componente antiguo**: `src/components/Login.tsx` (no se usa, puede eliminarse)

## 🚀 Pasos para Resolver el Problema

### Paso 1: Limpiar Caché del Navegador

**En Chrome/Edge:**
1. Abre las DevTools (F12)
2. Ve a la pestaña **Application**
3. En el menú lateral, expande **Storage**
4. Haz clic en **Clear site data**
5. Marca todas las opciones:
   - ✅ Cookies and other site data
   - ✅ Cached images and files
   - ✅ Service Workers
6. Haz clic en **Clear data**

**O más rápido:**
- Presiona `Ctrl + Shift + Delete`
- Selecciona "Cached images and files"
- Período: "All time"
- Haz clic en "Clear data"

### Paso 2: Desregistrar Service Worker

**En Chrome/Edge DevTools:**
1. Abre DevTools (F12)
2. Ve a **Application** → **Service Workers**
3. Busca el Service Worker de tu sitio
4. Haz clic en **Unregister**
5. Recarga la página con `Ctrl + Shift + R` (hard refresh)

### Paso 3: Forzar Nuevo Build en Vercel

1. **Opción A: Redeploy manual**
   - Ve a tu proyecto en Vercel
   - Ve a la pestaña **Deployments**
   - Encuentra el último deployment
   - Haz clic en los tres puntos (⋯) → **Redeploy**
   - Selecciona "Use existing Build Cache" = **NO** (desmarcado)

2. **Opción B: Push nuevo commit**
   ```bash
   # Hacer un cambio mínimo para forzar nuevo build
   git add agrogestion-frontend/public/sw.js
   git commit -m "fix: Actualizar Service Worker para limpiar caché"
   git push origin production  # o la rama que uses para Vercel
   ```

### Paso 4: Verificar Variables de Entorno en Vercel

Asegúrate de que las variables de entorno estén configuradas:

1. Ve a tu proyecto en Vercel
2. **Settings** → **Environment Variables**
3. Verifica que exista:
   - `VITE_API_BASE_URL` = `https://agrocloud-production.up.railway.app/api`

### Paso 5: Limpiar Caché de Vercel (si es necesario)

Si el problema persiste:

1. Ve a **Settings** → **General**
2. Busca la sección **Build & Development Settings**
3. Haz clic en **Clear Build Cache**
4. Haz un nuevo deploy

## 🔍 Verificación

Después de aplicar las soluciones:

1. **Abre la consola del navegador** (F12)
2. Busca mensajes del Service Worker:
   ```
   🔧 [SW] Instalando Service Worker...
   ✅ [SW] Service Worker instalado correctamente
   ```
3. Verifica que la versión sea `v1.1.0`:
   - En DevTools → Application → Service Workers
   - Debe mostrar `agrocloud-v1.1.0`

4. **Verifica el componente Login**:
   - Debe usar `src/pages/Login.tsx` (el nuevo)
   - NO debe usar `src/components/Login.tsx` (el antiguo)

## 🛠️ Solución Permanente

Para evitar este problema en el futuro:

### 1. Actualizar versión del Service Worker en cada release

Cada vez que hagas un cambio importante, actualiza la versión en `sw.js`:

```javascript
const CACHE_NAME = 'agrocloud-v{X.Y.Z}'; // Incrementar versión
```

### 2. Agregar headers de no-cache en vercel.json

Ya está configurado en `vercel.json`:
```json
{
  "src": "/(.*)",
  "dest": "/index.html",
  "headers": {
    "Cache-Control": "no-cache"
  }
}
```

### 3. Invalidar caché en cada deploy

Considera agregar un parámetro de versión a los archivos estáticos en producción.

## 📋 Checklist de Resolución

- [ ] Service Worker actualizado a v1.1.0
- [ ] Caché del navegador limpiado
- [ ] Service Worker desregistrado
- [ ] Nuevo build en Vercel (sin caché)
- [ ] Variables de entorno verificadas
- [ ] Versión correcta verificada en producción

## 🆘 Si el Problema Persiste

1. **Verifica qué componente se está usando**:
   - Abre DevTools → Sources
   - Busca `Login.tsx`
   - Verifica que sea el de `pages/Login.tsx` y no `components/Login.tsx`

2. **Verifica el build de Vercel**:
   - Ve a Deployments → Último deployment → Build Logs
   - Verifica que no haya errores

3. **Elimina el componente antiguo** (si ya no se usa):
   ```bash
   # Si confirmas que no se usa, puedes eliminarlo
   rm agrogestion-frontend/src/components/Login.tsx
   ```

4. **Forzar actualización completa**:
   - Desregistra todos los Service Workers
   - Limpia todo el caché del sitio
   - Haz un hard refresh (`Ctrl + Shift + R`)
   - Si es necesario, prueba en modo incógnito

---

**Fecha**: 2025-01-16
**Versión del Service Worker**: v1.1.0
**Estado**: ✅ Solución aplicada

