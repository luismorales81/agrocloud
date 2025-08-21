# 🚀 Desplegar Frontend en Railway

## 📋 **Prerrequisitos**

- ✅ Backend funcionando en Railway
- ✅ URL del backend disponible
- ✅ Cuenta de Railway configurada

## 🔧 **Paso 1: Configurar Frontend para Producción**

### **1.1 Actualizar URL del Backend**

Edita el archivo `agrogestion-frontend/src/services/api.ts`:

```typescript
// Cambiar esta línea:
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

// Por esta (reemplaza con tu URL real):
const API_BASE_URL = 'https://tu-app-railway.railway.app/api';
```

### **1.2 Crear Dockerfile para Frontend**

Crear `agrogestion-frontend/Dockerfile`:

```dockerfile
# Etapa de construcción
FROM node:18-alpine AS build

WORKDIR /app

# Copiar archivos de dependencias
COPY package*.json ./

# Instalar dependencias
RUN npm ci --only=production

# Copiar código fuente
COPY . .

# Construir la aplicación
RUN npm run build

# Etapa de producción
FROM nginx:alpine

# Copiar archivos construidos
COPY --from=build /app/dist /usr/share/nginx/html

# Copiar configuración de nginx
COPY nginx.conf /etc/nginx/nginx.conf

# Exponer puerto
EXPOSE 80

# Comando de inicio
CMD ["nginx", "-g", "daemon off;"]
```

### **1.3 Crear Configuración de Nginx**

Crear `agrogestion-frontend/nginx.conf`:

```nginx
events {
    worker_connections 1024;
}

http {
    include       /etc/nginx/mime.types;
    default_type  application/octet-stream;

    server {
        listen 80;
        server_name localhost;
        root /usr/share/nginx/html;
        index index.html;

        # Configuración para SPA (Single Page Application)
        location / {
            try_files $uri $uri/ /index.html;
        }

        # Configuración para archivos estáticos
        location /assets/ {
            expires 1y;
            add_header Cache-Control "public, immutable";
        }

        # Configuración de seguridad
        add_header X-Frame-Options "SAMEORIGIN" always;
        add_header X-Content-Type-Options "nosniff" always;
        add_header X-XSS-Protection "1; mode=block" always;
    }
}
```

## 🚀 **Paso 2: Desplegar en Railway**

### **2.1 Crear Nuevo Proyecto en Railway**

1. Ve a [railway.app](https://railway.app)
2. Haz clic en "New Project"
3. Selecciona "Deploy from GitHub repo"
4. Selecciona tu repositorio
5. Configura el directorio como `agrogestion-frontend`

### **2.2 Configurar Variables de Entorno**

En Railway, agrega estas variables:

```bash
NODE_ENV=production
VITE_API_BASE_URL=https://tu-app-railway.railway.app/api
```

### **2.3 Configurar Build**

En Railway, configura:
- **Build Command:** `npm run build`
- **Start Command:** `nginx -g 'daemon off;'`
- **Dockerfile Path:** `Dockerfile`

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
- Visita la URL del frontend en Railway
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

- [ ] ✅ Frontend desplegado en Railway
- [ ] ✅ URL del backend configurada correctamente
- [ ] ✅ CORS configurado en backend
- [ ] ✅ Login funcionando
- [ ] ✅ Comunicación backend-frontend establecida
- [ ] ✅ Aplicación completamente funcional

## 🔧 **Solución de Problemas**

### **Error de CORS**
- Verifica la configuración de CORS en el backend
- Asegúrate de que la URL del frontend esté permitida

### **Error de Conexión al Backend**
- Verifica que la URL del backend sea correcta
- Confirma que el backend esté funcionando
- Revisa los logs de Railway

### **Error de Build**
- Verifica que todas las dependencias estén instaladas
- Confirma que el Dockerfile esté correcto
- Revisa los logs de build en Railway

## 🎯 **Próximos Pasos**

1. **Configurar dominio personalizado** (opcional)
2. **Configurar SSL/HTTPS**
3. **Configurar monitoreo y alertas**
4. **Optimizar rendimiento**
5. **Configurar backups de base de datos**
