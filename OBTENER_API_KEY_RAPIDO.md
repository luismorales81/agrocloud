# 🚀 Guía Rápida: Obtener API Key de OpenWeatherMap

## ⚡ Pasos Rápidos (2 minutos)

### 1. Registrarse (30 segundos)
- Ve a: https://openweathermap.org/
- Haz clic en "Sign Up" (esquina superior derecha)
- Completa: Email + Contraseña
- Confirma tu email

### 2. Obtener API Key (30 segundos)
- Inicia sesión
- Ve a tu perfil (tu nombre arriba a la derecha)
- Selecciona "My API Keys"
- **Copia la API key** (32 caracteres)

### 3. Configurar en AgroCloud (30 segundos)
- Abre: `agrogestion-frontend/config-local.js`
- Reemplaza: `'1234567890abcdef'` con tu API key real
- Guarda el archivo
- Recarga la página

## ✅ Resultado
- Widget mostrará clima real de tu ubicación
- Pronósticos de 5 días
- Consejos agrícolas personalizados

## 🔑 Ejemplo de Configuración
```javascript
window.ENV = {
  VITE_API_BASE_URL: 'http://localhost:8080/api',
  VITE_APP_NAME: 'AgroCloud',
  VITE_APP_VERSION: '1.0.0',
  VITE_OPENWEATHER_API_KEY: 'TU_API_KEY_AQUI_32_CARACTERES'
};
```

## 💡 Sin API Key
- El widget funciona con datos de demostración
- Perfecto para pruebas y desarrollo
- No requiere configuración adicional

## 🆓 Gratis
- API key completamente gratuita
- 1,000 llamadas por día
- Suficiente para uso personal y desarrollo
