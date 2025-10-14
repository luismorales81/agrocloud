# 🚀 Deployment Railway Testing - Guía Completa

## 📋 **Archivos Creados**

### **Configuración Railway:**
- ✅ `railway.json` - Configuración base
- ✅ `railway-testing.json` - Configuración Testing
- ✅ `railway-production.json` - Configuración Production

### **Variables de Entorno:**
- ✅ `env-testing.example` - Variables Testing
- ✅ `env-production.example` - Variables Production
- ✅ `agrogestion-frontend/env-testing.example` - Frontend Testing
- ✅ `agrogestion-frontend/env-production.example` - Frontend Production

### **Base de Datos:**
- ✅ `database-migration-testing-limpio.sql` - Migración Testing

---

## 🎯 **Pasos para Deployment Testing**

### **1️⃣ Subir código al repositorio**

```bash
# Hacer commit de todos los archivos
git add .
git commit -m "Configuración Railway Testing - Lista para deployment"
git push origin main
```

### **2️⃣ Configurar Railway Testing**

#### **A. Crear Proyecto:**
1. Ir a [Railway.app](https://railway.app)
2. Login con tu cuenta
3. Click "New Project"
4. "Deploy from GitHub repo"
5. Seleccionar tu repositorio "AgroGestion"
6. Seleccionar rama: `main`

#### **B. Agregar Base de Datos:**
1. En el proyecto, click "+"
2. "Database" → "MySQL"
3. Esperar que se cree (1-2 minutos)

#### **C. Configurar Variables de Entorno:**
En Railway, ir a "Variables" y agregar:

```bash
# Backend
SPRING_PROFILES_ACTIVE=testing
JWT_SECRET=testing-jwt-secret-2025-agrogestion-railway
SPRING_DATASOURCE_URL=jdbc:mysql://mysql.railway.internal:3306/railway
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=[COPIAR_DE_RAILWAY_MYSQL]
SPRING_DATASOURCE_DRIVER_CLASS_NAME=com.mysql.cj.jdbc.Driver
PORT=8080

# Logging
LOGGING_LEVEL_ROOT=INFO
LOGGING_LEVEL_COM_AGROCLOUD=DEBUG

# CORS (actualizar después con URL real)
CORS_ALLOWED_ORIGINS=https://agrogestion-frontend-testing.vercel.app
```

### **3️⃣ Migrar Base de Datos**

#### **A. Obtener credenciales MySQL:**
En Railway → MySQL service → Variables:
- `MYSQLHOST`
- `MYSQLPORT` 
- `MYSQLUSER`
- `MYSQLPASSWORD`
- `MYSQLDATABASE`

#### **B. Ejecutar migración:**
```bash
# Usar las credenciales reales de Railway
mysql -h [MYSQLHOST] -P [MYSQLPORT] -u [MYSQLUSER] -p[MYSQLPASSWORD] [MYSQLDATABASE] < database-migration-testing-limpio.sql
```

### **4️⃣ Configurar Frontend (Vercel)**

#### **A. Crear proyecto en Vercel:**
1. Ir a [Vercel.com](https://vercel.com)
2. "New Project"
3. Importar repositorio GitHub
4. Configurar:
   - **Framework Preset:** Vite
   - **Root Directory:** `agrogestion-frontend`
   - **Build Command:** `npm run build`
   - **Output Directory:** `dist`

#### **B. Variables de entorno en Vercel:**
```bash
VITE_API_URL=https://[URL_BACKEND_RAILWAY]
NODE_ENV=testing
VITE_APP_ENV=testing
```

### **5️⃣ Verificar Deployment**

#### **A. Backend (Railway):**
- URL: `https://[tu-proyecto].railway.app`
- Health check: `https://[tu-proyecto].railway.app/api/v1/health`

#### **B. Frontend (Vercel):**
- URL: `https://[tu-proyecto].vercel.app`
- Debe conectar con el backend

#### **C. Base de datos:**
- Verificar que las tablas se crearon
- Probar login con usuarios

---

## 🔧 **Configuración Adicional**

### **Actualizar URLs después del deploy:**

1. **Backend URL en Railway:**
   - Copiar la URL del backend
   - Actualizar en Vercel: `VITE_API_URL`

2. **Frontend URL en Vercel:**
   - Copiar la URL del frontend
   - Actualizar en Railway: `CORS_ALLOWED_ORIGINS`

### **Generar JWT Secret seguro:**
```bash
# Ejecutar en terminal
node -e "console.log(require('crypto').randomBytes(64).toString('hex'))"
```

---

## ✅ **Checklist Final**

- [ ] Código subido al repositorio
- [ ] Railway proyecto creado
- [ ] MySQL agregado a Railway
- [ ] Variables de entorno configuradas
- [ ] Base de datos migrada
- [ ] Backend desplegado y funcionando
- [ ] Vercel proyecto creado
- [ ] Frontend desplegado y funcionando
- [ ] URLs actualizadas en ambos servicios
- [ ] Login funcionando
- [ ] Todas las funcionalidades probadas

---

## 🆘 **Solución de Problemas**

### **Backend no inicia:**
- Verificar variables de entorno
- Revisar logs en Railway
- Verificar conexión a MySQL

### **Frontend no conecta:**
- Verificar `VITE_API_URL`
- Verificar CORS en backend
- Revisar console del navegador

### **Base de datos:**
- Verificar credenciales MySQL
- Ejecutar migración nuevamente
- Revisar logs de conexión

---

**¡Listo para deployment! 🚀**
