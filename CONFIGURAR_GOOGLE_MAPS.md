# Configuración de Google Maps para AgroCloud

## Estado Actual

✅ **API Key configurada**: Ya tenemos una API key válida configurada en el proyecto.

## Configuración Actual

La API key está configurada en `agrogestion-frontend/src/config/googleMaps.ts`:

```typescript
export const GOOGLE_MAPS_CONFIG = {
  // API Key para desarrollo local
  API_KEY: 'AIzaSyCWz9FKCHBdLqbjhHCPcECww5hs2ugiWA0',
  // ... resto de la configuración
};
```

## Prueba de Funcionamiento

Para probar que Google Maps funciona correctamente:

1. Ve a la sección "Campos" en la aplicación
2. Haz clic en "Agregar Nuevo Campo" o "Editar" en cualquier campo existente
3. El mapa de Google Maps debería cargar correctamente
4. Deberías poder dibujar polígonos en el mapa

### 3. Configuración para producción

Para el despliegue en producción, agrega también tu dominio de producción a las restricciones de la API key:

- `https://tu-dominio.com/*`
- `https://www.tu-dominio.com/*`

### 4. Variables de entorno (Opcional)

Para mayor seguridad, puedes usar variables de entorno:

1. Crea un archivo `.env.local` en `agrogestion-frontend/`:
```
VITE_GOOGLE_MAPS_API_KEY=tu_api_key_aqui
```

2. Actualiza `googleMaps.ts`:
```typescript
API_KEY: import.meta.env.VITE_GOOGLE_MAPS_API_KEY || 'fallback_key',
```

## APIs Requeridas

Asegúrate de tener habilitadas estas APIs en Google Cloud Console:
- Maps JavaScript API
- Drawing Library
- Geometry Library

## Notas Importantes

- La API key actual es de demostración y tiene limitaciones
- Para desarrollo local, agrega `http://localhost:3000/*` a las restricciones
- Para producción, agrega tu dominio real
- Nunca subas la API key real al repositorio público

## Prueba de Configuración

Una vez configurada la API key correcta:

1. Reinicia el servidor de desarrollo
2. Ve a la sección "Campos"
3. Intenta crear o editar un campo
4. El mapa de Google Maps debería cargar correctamente

## Errores Comunes

- **RefererNotAllowedMapError**: API key no configurada para el dominio
- **InvalidKey**: API key incorrecta o no válida
- **QuotaExceeded**: Límite de uso excedido (gratuito: 28,500 cargas/mes)
