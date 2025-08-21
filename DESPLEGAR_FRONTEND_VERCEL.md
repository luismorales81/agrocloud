# 🚀 Desplegar Frontend en Vercel

## 📋 **Prerrequisitos**

- ✅ Backend funcionando en Railway
- ✅ URL del backend disponible
- ✅ Cuenta de Vercel (gratuita)

## 🔧 **Paso 1: Configurar Frontend para Producción**

### **1.1 Actualizar URL del Backend**

Edita el archivo `agrogestion-frontend/src/services/api.ts`:

```typescript
// Cambiar esta línea:
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'https://agrocloud-backend-production-xxxx.up.railway.app/api';

// Por esta (reemplaza con tu URL real):
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'https://tu-backend-railway.up.railway.app/api';
```

### **1.2 Actualizar vercel.json**

Edita `agrogestion-frontend/vercel.json`:

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
      "src": "/(.*)",
      "dest": "/index.html"
    }
  ],
  "env": {
    "VITE_API_BASE_URL": "https://tu-backend-railway.up.railway.app/api"
  }
}
```

## 🚀 **Paso 2: Desplegar en Vercel**

### **2.1 Crear Cuenta en Vercel**

1. Ve a [vercel.com](https://vercel.com)
2. Haz clic en "Sign Up"
3. Conecta tu cuenta de GitHub

### **2.2 Importar Proyecto**

1. Haz clic en "New Project"
2. Selecciona tu repositorio: `luismorales81/agrogestion`
3. En **"Root Directory"** pon: `agrogestion-frontend`
4. Haz clic en "Deploy"

### **2.3 Configurar Variables de Entorno**

En Vercel Dashboard:
1. Ve a tu proyecto
2. Ve a "Settings" → "Environment Variables"
3. Agrega:
   ```
   VITE_API_BASE_URL = https://tu-backend-railway.up.railway.app/api
   ```

## 🔗 **Paso 3: Configurar CORS en Backend**

Si tienes problemas de CORS, actualiza la configuración en el backend:

```java
// En SecurityConfig.java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOriginPatterns(Arrays.asList("*"));
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setAllowCredentials(true);
    configuration.setMaxAge(3600L);
    
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}
```

## 🧪 **Paso 4: Probar la Integración**

### **4.1 Verificar Frontend**
- Visita la URL de Vercel (algo como: `https://agrogestion-frontend.vercel.app`)
- Verifica que la aplicación cargue correctamente
- Prueba el login con las credenciales por defecto

### **4.2 Verificar Comunicación Backend**
- Abre las herramientas de desarrollador (F12)
- Ve a la pestaña Network
- Intenta hacer login
- Verifica que las peticiones al backend funcionen

### **4.3 Credenciales por Defecto**
```
Usuario: admin
Contraseña: admin123
```

## 📋 **Checklist de Verificación**

- [ ] ✅ Frontend desplegado en Vercel
- [ ] ✅ URL del backend configurada correctamente
- [ ] ✅ CORS configurado en backend
- [ ] ✅ Login funcionando
- [ ] ✅ Comunicación backend-frontend establecida
- [ ] ✅ Aplicación completamente funcional

## 🔧 **Solución de Problemas**

### **Error de CORS**
- Verifica la configuración de CORS en el backend
- Asegúrate de que la URL de Vercel esté permitida

### **Error de Conexión al Backend**
- Verifica que la URL del backend sea correcta
- Confirma que el backend esté funcionando
- Revisa los logs de Railway

### **Error de Build**
- Verifica que todas las dependencias estén instaladas
- Confirma que el package.json esté correcto
- Revisa los logs de build en Vercel

## 🎯 **Ventajas de Vercel**

- ⚡ **Despliegue ultra rápido**
- 🌍 **CDN global automático**
- 🔒 **SSL/HTTPS automático**
- 📱 **Optimización automática para móviles**
- 🔄 **Despliegue automático desde GitHub**
- 💰 **Plan gratuito generoso**

## 📊 **URLs Finales**

Una vez desplegado, tendrás:

```
🌐 Frontend (Vercel): https://agrogestion-frontend.vercel.app
🔧 Backend (Railway): https://tu-backend-railway.up.railway.app
📚 API Docs: https://tu-backend-railway.up.railway.app/swagger-ui.html
```

## 🎉 **¡Listo para Mostrar al Cliente!**

Con Vercel tendrás:
- ✅ **Aplicación web completa** y funcional
- ✅ **Interfaz moderna** y responsive
- ✅ **Despliegue automático** con cada push
- ✅ **Excelente rendimiento** global
- ✅ **SSL/HTTPS** automático
