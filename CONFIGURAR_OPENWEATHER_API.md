# 🌤️ Configuración de OpenWeatherMap API

## ¿Qué es OpenWeatherMap?

OpenWeatherMap es un servicio que proporciona datos meteorológicos en tiempo real y pronósticos para cualquier ubicación del mundo. En AgroCloud, lo utilizamos para mostrar el clima actual y pronósticos en el widget del dashboard.

## 🔑 Cómo obtener una API Key gratuita

### Paso 1: Registrarse en OpenWeatherMap
1. Ve a [OpenWeatherMap](https://openweathermap.org/)
2. Haz clic en "Sign Up" en la esquina superior derecha
3. Completa el formulario de registro con tu email y contraseña
4. Confirma tu email

### Paso 2: Obtener la API Key
1. Inicia sesión en tu cuenta
2. Ve a tu perfil (tu nombre en la esquina superior derecha)
3. Selecciona "My API Keys"
4. Verás tu API key predeterminada o puedes generar una nueva
5. **Copia la API key** (es una cadena de 32 caracteres)

### Paso 3: Configurar en AgroCloud

#### Para desarrollo local:
1. Abre el archivo `agrogestion-frontend/config-local.js`
2. Reemplaza `'1234567890abcdef'` con tu API key real:

```javascript
window.ENV = {
  VITE_API_BASE_URL: 'http://localhost:8080/api',
  VITE_APP_NAME: 'AgroCloud',
  VITE_APP_VERSION: '1.0.0',
  VITE_OPENWEATHER_API_KEY: 'TU_API_KEY_AQUI' // ← Reemplaza con tu API key
};
```

#### Para producción:
1. En tu plataforma de hosting (Vercel, Railway, etc.)
2. Agrega la variable de entorno:
   - **Nombre**: `VITE_OPENWEATHER_API_KEY`
   - **Valor**: Tu API key de OpenWeatherMap

## 📊 Límites de la API gratuita

- **60 llamadas por minuto** (1 por segundo)
- **1,000 llamadas por día**
- Datos meteorológicos actuales
- Pronósticos de 5 días
- Geocodificación inversa (obtener nombre de ubicación)

## 🔧 Funcionalidades del Widget

Una vez configurado, el widget de clima:

1. **Detecta automáticamente tu ubicación** usando el GPS del navegador
2. **Muestra el clima actual** con temperatura, humedad, viento y presión
3. **Proporciona pronósticos** de 5 días
4. **Genera consejos agrícolas** basados en las condiciones meteorológicas
5. **Muestra alertas** cuando las condiciones no son favorables para labores agrícolas

## 🚨 Solución de problemas

### Error: "Invalid API key"
- Verifica que la API key esté correctamente copiada
- Asegúrate de que no haya espacios extra
- La API key puede tardar hasta 2 horas en activarse después del registro

### Error: "Geolocalización no soportada"
- El navegador debe soportar geolocalización
- El usuario debe permitir el acceso a la ubicación
- En HTTPS es obligatorio, en HTTP puede no funcionar

### Error: "No se pudo obtener la ubicación"
- Verifica que el navegador tenga permisos de ubicación
- Intenta recargar la página
- El widget usará Buenos Aires como ubicación por defecto

## 📱 Permisos del navegador

Cuando accedas a AgroCloud, el navegador te pedirá permiso para acceder a tu ubicación:

- **Permitir**: El widget mostrará el clima de tu ubicación actual
- **Denegar**: El widget usará Buenos Aires como ubicación por defecto

## 🔄 Actualización de datos

- Los datos se actualizan automáticamente cada 30 minutos
- Puedes actualizar manualmente haciendo clic en el botón 🔄
- El widget muestra la hora de la última actualización

## 💡 Consejos

1. **API Key gratuita**: La versión gratuita es suficiente para uso personal y desarrollo
2. **Ubicación precisa**: Usa un navegador moderno para mejor precisión de ubicación
3. **Conexión estable**: Se requiere conexión a internet para obtener datos meteorológicos
4. **Privacidad**: La ubicación solo se usa para obtener datos del clima, no se almacena

## 🔗 Enlaces útiles

- [OpenWeatherMap API Documentation](https://openweathermap.org/api)
- [Geolocation API MDN](https://developer.mozilla.org/en-US/docs/Web/API/Geolocation_API)
- [OpenWeatherMap Pricing](https://openweathermap.org/price)
