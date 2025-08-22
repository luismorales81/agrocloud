# 🚀 Deployment en Vercel - AgroCloud Frontend

## Problemas Solucionados

### 1. Error de MIME Type
- **Problema**: Vercel servía archivos HTML en lugar de JavaScript/JSON
- **Solución**: Configuración correcta de `vercel.json` con rutas específicas para archivos estáticos

### 2. Service Worker y Manifest
- **Problema**: Archivos deshabilitados temporalmente
- **Solución**: Habilitados correctamente en `index.html`

### 3. Configuración de Vite
- **Problema**: Falta de configuración específica para build
- **Solución**: Creado `vite.config.ts` con configuración optimizada

## Archivos Modificados

### 1. `vercel.json`
```json
{
  "version": 2,
  "builds": [
    {
      "src": "package.json",
      "use": "@vercel/static-build",
      "config": {
        "distDir": "dist"
      }
    }
  ],
  "routes": [
    {
      "src": "/sw.js",
      "dest": "/sw.js",
      "headers": {
        "Content-Type": "application/javascript",
        "Cache-Control": "no-cache"
      }
    },
    {
      "src": "/manifest.json",
      "dest": "/manifest.json",
      "headers": {
        "Content-Type": "application/json",
        "Cache-Control": "no-cache"
      }
    },
    {
      "src": "/(.*)\\.(js|css|png|jpg|jpeg|gif|svg|ico|woff|woff2|ttf|eot)$",
      "dest": "/$1.$2",
      "headers": {
        "Cache-Control": "public, max-age=31536000, immutable"
      }
    },
    {
      "src": "/(.*)",
      "dest": "/index.html",
      "headers": {
        "Cache-Control": "no-cache"
      }
    }
  ]
}
```

### 2. `index.html`
- Habilitado el manifest: `<link rel="manifest" href="/manifest.json" />`
- Habilitado el service worker
- Corregidas las referencias de iconos

### 3. `vite.config.ts` (Nuevo)
- Configuración optimizada para build
- Chunk splitting para mejor performance
- Configuración de puertos

### 4. `package.json`
- Agregada dependencia `@vitejs/plugin-react`

## Pasos para Deployment

### 1. Instalar Dependencias
```bash
cd agrogestion-frontend
npm install
```

### 2. Build Local (Opcional)
```bash
npm run build
```

### 3. Deploy en Vercel
```bash
# Si tienes Vercel CLI instalado
vercel

# O desde el dashboard de Vercel
# 1. Conectar repositorio
# 2. Configurar build settings:
#    - Build Command: npm run build
#    - Output Directory: dist
#    - Install Command: npm install
```

### 4. Variables de Entorno en Vercel
Configurar en el dashboard de Vercel:
```
VITE_API_BASE_URL=https://agrocloud-production.up.railway.app/api
```

## Verificación Post-Deployment

### 1. Verificar Archivos Estáticos
- `https://tu-app.vercel.app/sw.js` → Debe devolver JavaScript
- `https://tu-app.vercel.app/manifest.json` → Debe devolver JSON
- `https://tu-app.vercel.app/icons/agrocloud-logo.svg` → Debe devolver SVG

### 2. Verificar Console
- No debe haber errores de MIME type
- Service Worker debe registrarse correctamente
- Manifest debe cargar sin errores

### 3. Verificar Funcionalidad
- Login debe funcionar
- Navegación debe ser fluida
- API calls deben funcionar

## Troubleshooting

### Si persisten errores de MIME type:
1. Verificar que `vercel.json` esté en la raíz del proyecto frontend
2. Asegurar que las rutas estén correctamente configuradas
3. Hacer un nuevo deploy después de los cambios

### Si el service worker no se registra:
1. Verificar que `/sw.js` esté en la carpeta `public`
2. Verificar que el archivo tenga el contenido correcto
3. Limpiar cache del navegador

### Si la API no funciona:
1. Verificar la variable de entorno `VITE_API_BASE_URL`
2. Verificar que el backend esté funcionando en Railway
3. Verificar CORS en el backend

## Estructura Final del Proyecto
```
agrogestion-frontend/
├── public/
│   ├── manifest.json
│   ├── sw.js
│   └── icons/
├── src/
├── vercel.json
├── vite.config.ts
├── package.json
└── index.html
```

## URLs Importantes
- **Frontend**: https://agrocloud.vercel.app
- **Backend**: https://agrocloud-production.up.railway.app/api
- **Dashboard Vercel**: https://vercel.com/dashboard
- **Dashboard Railway**: https://railway.app/dashboard
