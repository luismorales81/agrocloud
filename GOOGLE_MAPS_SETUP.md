# 🗺️ Configuración de Google Maps para AgroGestion

## **⚠️ IMPORTANTE: El mapa no funciona sin API Key**

Para que los mapas funcionen correctamente, necesitas configurar una API Key de Google Maps.

## **📋 Pasos para Obtener tu API Key (Gratuita)**

### **1. Crear Proyecto en Google Cloud Console**
1. Ve a [Google Cloud Console](https://console.cloud.google.com/)
2. Crea un nuevo proyecto o selecciona uno existente
3. Habilita la facturación (requerido, pero tienes $200 de crédito gratis)

### **2. Habilitar las APIs Necesarias**
En la consola de Google Cloud, habilita estas APIs:
- **Maps JavaScript API**
- **Drawing Library** (incluida en Maps JavaScript API)
- **Geometry Library** (incluida en Maps JavaScript API)

### **3. Crear Credenciales**
1. Ve a "Credenciales" en el menú lateral
2. Haz clic en "Crear credenciales" → "Clave de API"
3. Copia la API Key generada

### **4. Configurar Restricciones (Opcional pero Recomendado)**
- Restringe la API Key a tu dominio: `localhost:5173`
- Restringe a las APIs específicas que habilitaste

## **🔧 Configurar la API Key en el Proyecto**

### **Opción 1: Editar el Archivo de Configuración**
1. Abre el archivo: `agrogestion-frontend/src/config/googleMaps.ts`
2. Reemplaza `'YOUR_API_KEY'` con tu API Key real:

```typescript
export const GOOGLE_MAPS_CONFIG = {
  API_KEY: 'TU_API_KEY_AQUI', // ← Reemplaza esto
  // ... resto de la configuración
};
```

### **Opción 2: Usar Variables de Entorno (Recomendado)**
1. Crea un archivo `.env` en `agrogestion-frontend/`:
```env
VITE_GOOGLE_MAPS_API_KEY=tu_api_key_aqui
```

2. Modifica `agrogestion-frontend/src/config/googleMaps.ts`:
```typescript
export const GOOGLE_MAPS_CONFIG = {
  API_KEY: import.meta.env.VITE_GOOGLE_MAPS_API_KEY || 'YOUR_API_KEY',
  // ... resto de la configuración
};
```

## **🚀 Probar la Configuración**

1. Reinicia el servidor de desarrollo:
```bash
npm run dev
```

2. Ve a `http://localhost:5173`
3. Inicia sesión y navega a "Gestión de Lotes"
4. Haz clic en "🗺️ Mostrar Mapa"
5. Deberías ver el mapa de Google Maps cargado correctamente

## **🔍 Solución de Problemas**

### **Error: "Error cargando el mapa"**
- Verifica que la API Key sea correcta
- Asegúrate de que las APIs estén habilitadas
- Revisa la consola del navegador para errores específicos

### **Error: "This API project is not authorized"**
- Verifica que la facturación esté habilitada
- Asegúrate de que las APIs estén habilitadas en el proyecto

### **Error: "RefererNotAllowedMapError"**
- Configura las restricciones de dominio en la API Key
- Agrega `localhost:5173` a los dominios permitidos

## **💰 Costos**

- **Gratis**: $200 de crédito mensual (suficiente para desarrollo)
- **Desarrollo**: Generalmente no excede los $200 gratuitos
- **Producción**: Depende del uso, pero muy económico

## **📞 Soporte**

Si tienes problemas:
1. Revisa la [documentación oficial de Google Maps](https://developers.google.com/maps/documentation/javascript)
2. Verifica la consola del navegador para errores específicos
3. Asegúrate de que todas las APIs estén habilitadas

---

**¡Una vez configurada la API Key, los mapas funcionarán perfectamente!** 🎉
