# 🐳 Railway Dockerfile Setup - Guía Completa

## ✅ **Archivos creados:**

### **Dockerfile multistage:**
- ✅ `agrogestion-backend/Dockerfile` - Dockerfile multistage optimizado
- ✅ `agrogestion-backend/.dockerignore` - Optimización de build
- ✅ `railway.json` - Configuración Railway para usar Dockerfile personalizado

### **Archivos eliminados:**
- ❌ `Procfile` - Ya no necesario (usa ENTRYPOINT del Dockerfile)
- ❌ `railway-testing-fixed.json` - Reemplazado por railway.json

---

## 🐳 **Dockerfile Multistage - Explicación:**

### **Etapa 1: BUILD**
```dockerfile
FROM maven:3.9.5-eclipse-temurin-17 AS build
```
- ✅ Usa Maven para compilar
- ✅ Descarga dependencias (con cache)
- ✅ Compila el proyecto (`mvn clean package -DskipTests`)
- ✅ Genera el JAR en `/app/target/`

### **Etapa 2: RUNTIME**
```dockerfile
FROM eclipse-temurin:17-jre-alpine AS runtime
```
- ✅ Usa JRE ligero (Alpine)
- ✅ Copia el JAR desde la etapa de build
- ✅ Configura usuario no-root (seguridad)
- ✅ Expone puerto 8080
- ✅ Usa `ENTRYPOINT ["java", "-jar", "app.jar"]`

---

## 🚀 **Configuración Railway:**

### **1️⃣ railway.json:**
```json
{
  "build": {
    "builder": "DOCKERFILE",
    "dockerfilePath": "agrogestion-backend/Dockerfile"
  }
}
```

### **2️⃣ Railway detectará automáticamente:**
- ✅ El Dockerfile en `agrogestion-backend/Dockerfile`
- ✅ El contexto de build será `agrogestion-backend/`
- ✅ Usará el ENTRYPOINT del Dockerfile (no Start Command)

---

## 📋 **Variables de entorno necesarias en Railway:**

```bash
# Perfil de Spring
SPRING_PROFILES_ACTIVE=testing

# Base de datos MySQL (Railway)
SPRING_DATASOURCE_URL=jdbc:mysql://mysql.railway.internal:3306/railway
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=[TU_PASSWORD_DE_RAILWAY]
SPRING_DATASOURCE_DRIVER_CLASS_NAME=com.mysql.cj.jdbc.Driver

# JWT Secret
JWT_SECRET=testing-jwt-secret-2025-agrogestion-railway

# Puerto (Railway lo maneja automáticamente)
PORT=8080

# Logging
LOGGING_LEVEL_ROOT=INFO
LOGGING_LEVEL_COM_AGROCLOUD=DEBUG
```

---

## 🔧 **Configuración en Railway:**

### **1️⃣ Crear proyecto:**
1. Ir a [Railway.app](https://railway.app)
2. "New Project" → "Deploy from GitHub repo"
3. Seleccionar tu repositorio
4. Seleccionar rama: `main`

### **2️⃣ Configurar servicio:**
- **Build Context:** `agrogestion-backend/` (automático)
- **Dockerfile Path:** `agrogestion-backend/Dockerfile` (automático)
- **Start Command:** (vacío - usa ENTRYPOINT del Dockerfile)

### **3️⃣ Agregar MySQL:**
1. Click "+" → "Database" → "MySQL"
2. Esperar que se cree

### **4️⃣ Configurar variables:**
- Agregar todas las variables de entorno listadas arriba
- Usar las credenciales MySQL de Railway

---

## ✅ **Ventajas de este setup:**

### **🐳 Dockerfile multistage:**
- ✅ **Build optimizado:** Solo copia lo necesario
- ✅ **Imagen ligera:** Usa Alpine Linux
- ✅ **Seguridad:** Usuario no-root
- ✅ **Cache eficiente:** Dependencias Maven cacheadas

### **🚀 Railway optimizado:**
- ✅ **Sin Procfile:** Usa ENTRYPOINT del Dockerfile
- ✅ **Contexto correcto:** Build desde `agrogestion-backend/`
- ✅ **Detección automática:** Railway encuentra el Dockerfile
- ✅ **Health check:** `/actuator/health`

---

## 🎯 **Próximos pasos:**

1. **Subir cambios al repositorio**
2. **Configurar Railway** (usar guía arriba)
3. **Agregar variables de entorno**
4. **Migrar base de datos**
5. **Probar la aplicación**

---

## 🆘 **Solución de problemas:**

### **Build falla:**
- Verificar que `pom.xml` esté en `agrogestion-backend/`
- Verificar que `src/` esté en `agrogestion-backend/`

### **Contenedor no inicia:**
- Verificar variables de entorno
- Revisar logs en Railway
- Verificar conexión a MySQL

### **Health check falla:**
- Verificar que Spring Boot inicie correctamente
- Verificar que `/actuator/health` esté disponible

---

**¡Listo para deployment! 🚀**
