// Configuración para producción (Railway)
export const config = {
  // URL del backend en Railway
  API_BASE_URL: 'https://agrocloud-backend-production-xxxx.up.railway.app/api',
  
  // Configuración de la aplicación
  APP_NAME: 'AgroCloud',
  APP_VERSION: '1.0.0',
  
  // Configuración de Google Maps (si se usa)
  GOOGLE_MAPS_API_KEY: 'tu-api-key-de-google-maps',
  
  // Configuración de timeout
  API_TIMEOUT: 10000,
  
  // Configuración de CORS
  CORS_ENABLED: true
};

// Instrucciones de uso:
// 1. Reemplaza 'tu-app-railway.railway.app' con la URL real de tu aplicación en Railway
// 2. Actualiza la API key de Google Maps si la usas
// 3. Importa este archivo en api.ts para usar la configuración de producción
