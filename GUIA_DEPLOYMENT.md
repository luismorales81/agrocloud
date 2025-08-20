# 🚀 Guía de Deployment - AgroCloud

## 📋 **Resumen**
Esta guía te ayudará a subir AgroCloud online **GRATIS** usando:
- **Frontend**: Vercel (React/Vite)
- **Backend**: Railway (Spring Boot + PostgreSQL)

---

## 🎯 **Prerequisitos**

### 1. **Cuenta en GitHub**
- Crear cuenta en [GitHub](https://github.com) (gratis)
- Subir tu código a un repositorio

### 2. **Cuentas de Deployment**
- [Vercel](https://vercel.com) - Frontend (gratis)
- [Railway](https://railway.app) - Backend + BD (gratis $5/mes de créditos)

---

## 📂 **Paso 1: Subir Código a GitHub**

### **1.1 Inicializar Git (si no está hecho)**
```bash
# En la carpeta raíz del proyecto
git init
git add .
git commit -m "Initial commit - AgroCloud"
```

### **1.2 Crear Repositorio en GitHub**
1. Ir a [GitHub](https://github.com)
2. Crear nuevo repositorio: "agrocloud"
3. NO inicializar con README (ya tienes código)

### **1.3 Conectar con GitHub**
```bash
git remote add origin https://github.com/TU_USUARIO/agrocloud.git
git branch -M main
git push -u origin main
```

---

## 🌐 **Paso 2: Deploy del Frontend en Vercel**

### **2.1 Crear Cuenta en Vercel**
1. Ir a [vercel.com](https://vercel.com)
2. Registrarse con GitHub
3. Autorizar acceso a repositorios

### **2.2 Importar Proyecto**
1. Click en "New Project"
2. Seleccionar tu repositorio "agrocloud"
3. **Framework Preset**: Vite
4. **Root Directory**: `agrogestion-frontend`
5. **Build Command**: `npm run build`
6. **Output Directory**: `dist`

### **2.3 Configurar Variables de Entorno**
En la configuración del proyecto en Vercel:
```
VITE_API_BASE_URL = https://tu-backend.railway.app
```
*(Obtendrás esta URL en el siguiente paso)*

### **2.4 Deploy**
- Click "Deploy"
- Vercel te dará una URL: `https://tu-proyecto.vercel.app`

---

## 🚂 **Paso 3: Deploy del Backend en Railway**

### **3.1 Crear Cuenta en Railway**
1. Ir a [railway.app](https://railway.app)
2. Registrarse con GitHub
3. Autorizar acceso a repositorios

### **3.2 Crear Nuevo Proyecto**
1. Click "New Project"
2. Seleccionar "Deploy from GitHub repo"
3. Elegir tu repositorio "agrocloud"
4. **Root Directory**: `/agrogestion-backend`

### **3.3 Agregar Base de Datos PostgreSQL**
1. En tu proyecto Railway, click "New Service"
2. Seleccionar "PostgreSQL"
3. Railway creará la BD automáticamente

### **3.4 Configurar Variables de Entorno**
En Railway, ir a tu servicio backend > Variables:

```bash
# Perfiles de Spring
SPRING_PROFILES_ACTIVE=prod

# JWT Configuration
JWT_SECRET=agroCloudSecretKey2024ParaProduccion123456789
JWT_EXPIRATION=86400000
JWT_REFRESH_EXPIRATION=604800000

# CORS para Vercel
CORS_ALLOWED_ORIGINS=https://tu-proyecto.vercel.app

# Puerto (Railway lo asigna automáticamente)
PORT=8080
```

### **3.5 Configurar Railway.json**
El archivo `railway.json` ya está creado, Railway lo detectará automáticamente.

### **3.6 Deploy**
- Railway deployará automáticamente
- Te dará una URL: `https://tu-backend.railway.app`

---

## 🔄 **Paso 4: Conectar Frontend y Backend**

### **4.1 Actualizar URL del Backend en Vercel**
1. Ir a tu proyecto en Vercel
2. Settings > Environment Variables
3. Actualizar `VITE_API_BASE_URL` con la URL de Railway:
   ```
   VITE_API_BASE_URL = https://tu-backend.railway.app
   ```

### **4.2 Actualizar CORS en Railway**
1. Ir a tu proyecto en Railway
2. Variables de entorno
3. Actualizar `CORS_ALLOWED_ORIGINS`:
   ```
   CORS_ALLOWED_ORIGINS = https://tu-proyecto.vercel.app
   ```

### **4.3 Re-deployar**
Ambas plataformas re-deployarán automáticamente al detectar cambios.

---

## ✅ **Paso 5: Verificar Deployment**

### **URLs de tu aplicación:**
- **Frontend**: `https://tu-proyecto.vercel.app`
- **Backend API**: `https://tu-backend.railway.app`
- **Swagger UI**: `https://tu-backend.railway.app/swagger-ui.html`
- **Health Check**: `https://tu-backend.railway.app/actuator/health`

### **Pruebas a realizar:**
1. ✅ Frontend carga correctamente
2. ✅ Login funciona
3. ✅ APIs responden desde el backend
4. ✅ Base de datos guarda información

---

## 💰 **Costos y Límites Gratuitos**

### **Vercel (Frontend):**
- ✅ **Gratis**: 100GB/mes de ancho de banda
- ✅ **Gratis**: SSL automático + CDN global
- ✅ **Gratis**: Dominio personalizado
- ⚠️ **Límite**: 100 deployments/mes

### **Railway (Backend + BD):**
- ✅ **Gratis**: $5 USD de créditos mensuales
- ✅ **Gratis**: PostgreSQL incluida
- ⚠️ **Costo después**: ~$0.000463/GB-hora
- ⚠️ **Estimado**: $3-8/mes para uso moderado

---

## 🔧 **Solución de Problemas**

### **Frontend no carga:**
```bash
# Verificar variables de entorno en Vercel
VITE_API_BASE_URL = https://tu-backend.railway.app
```

### **Backend no conecta a BD:**
```bash
# Verificar que Railway asignó DATABASE_URL automáticamente
# En Variables debe aparecer DATABASE_URL
```

### **CORS Errors:**
```bash
# Verificar CORS_ALLOWED_ORIGINS en Railway
CORS_ALLOWED_ORIGINS = https://tu-proyecto.vercel.app
```

### **Error 404 en rutas del Frontend:**
- Vercel maneja esto automáticamente con el `vercel.json`

---

## 🎉 **¡Listo!**

Tu aplicación AgroCloud está online y funcionando. Usuarios pueden acceder desde cualquier dispositivo con conexión a internet.

### **Próximos pasos opcionales:**
1. **Dominio personalizado**: Configurar `tudominio.com`
2. **SSL Certificado**: Viene incluido automáticamente
3. **Monitoring**: Railway y Vercel incluyen analytics básicos
4. **Backups**: Railway hace backups automáticos de PostgreSQL

---

## 📞 **Soporte**

Si tienes problemas:
1. Revisar logs en Railway (pestaña "Deployments")
2. Revistar logs en Vercel (pestaña "Functions")
3. Usar las herramientas de desarrollador del navegador
4. Verificar el Health Check: `https://tu-backend.railway.app/actuator/health`
