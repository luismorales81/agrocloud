# Configuración de Variables de Entorno - Frontend

## 📋 Archivos necesarios

Crear estos archivos en `agrogestion-frontend/`:

### 1. `.env.development` (Desarrollo local)
```env
VITE_API_BASE_URL=http://localhost:8080
VITE_ENV=development
VITE_APP_NAME=AgroCloud
VITE_APP_VERSION=1.0.0
```

### 2. `.env.production` (Producción)
```env
VITE_API_BASE_URL=https://api.tudominio.com
VITE_ENV=production
VITE_APP_NAME=AgroCloud
VITE_APP_VERSION=1.0.0
```

### 3. `.env.local` (Personalización local - NO subir a Git)
```env
# Sobrescribe las variables para tu entorno local si es necesario
VITE_API_BASE_URL=http://192.168.1.100:8080
```

## 🔧 Uso en el código

### Antes (❌):
```typescript
const response = await fetch('http://localhost:8080/api/labores', ...);
```

### Después (✅):
```typescript
import api from '../services/api';
const response = await api.get('/api/labores');
```

O si necesitas usar fetch directo:
```typescript
const BASE_URL = import.meta.env.VITE_API_BASE_URL;
const response = await fetch(`${BASE_URL}/api/labores`, ...);
```

## 📝 Scripts de package.json

Asegúrate de tener estos scripts:
```json
{
  "scripts": {
    "dev": "vite",
    "build": "vite build",
    "build:prod": "vite build --mode production",
    "preview": "vite preview"
  }
}
```

## 🚀 Despliegue

- **Desarrollo**: `npm run dev` (usa `.env.development`)
- **Producción**: `npm run build:prod` (usa `.env.production`)
- **Vista previa**: `npm run preview`

## ⚠️ Importante

- ✅ Agregar `.env.local` a `.gitignore`
- ✅ Nunca subir credenciales a Git
- ✅ Documentar variables necesarias en `.env.example`

