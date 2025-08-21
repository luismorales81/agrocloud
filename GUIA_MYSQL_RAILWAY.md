# 🗄️ Guía para Configurar MySQL en Railway

## Opción 1: MySQL en Railway (Recomendado)

### Paso 1: Agregar MySQL a tu Proyecto
1. Ve a tu proyecto en Railway Dashboard
2. Haz clic en **"New Service"**
3. Selecciona **"Database"** → **"MySQL"**
4. Railway creará automáticamente una base de datos MySQL

### Paso 2: Configurar Variables de Entorno
En tu servicio backend, configura estas variables:

```bash
DATABASE_URL=${MYSQL_URL}
DB_USERNAME=${MYSQL_USERNAME}
DB_PASSWORD=${MYSQL_PASSWORD}
JWT_SECRET=agrogestionSecretKey2024ForJWTTokenGenerationAndValidationSecureAndLongEnough
```

### Paso 3: Actualizar Dockerfile
Cambia el perfil en el Dockerfile:
```dockerfile
ENTRYPOINT ["java", "-Dspring.profiles.active=railway-mysql", "-jar", "app.jar"]
```

### Paso 4: Crear Base de Datos
Una vez que MySQL esté configurado, ejecuta el script SQL:
```sql
-- Crear base de datos
CREATE DATABASE IF NOT EXISTS agrocloud CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Usar la base de datos
USE agrocloud;

-- Ejecutar el script setup-mysql-local.sql
```

## Opción 2: H2 sin MySQL (Solución Rápida)

### Paso 1: Configurar Variables Mínimas
Solo necesitas configurar:
```bash
JWT_SECRET=agrogestionSecretKey2024ForJWTTokenGenerationAndValidationSecureAndLongEnough
```

### Paso 2: Usar Perfil H2
El Dockerfile ya está configurado para usar H2:
```dockerfile
ENTRYPOINT ["java", "-Dspring.profiles.active=railway-h2", "-jar", "app.jar"]
```

## Verificación

### 1. Verificar Logs
Busca estos mensajes de éxito:
```
Started AgroCloudApplication
Tomcat started on port(s): 8080
```

### 2. Probar Endpoints
```bash
# Healthcheck
curl https://tu-app.railway.app/

# Health simple
curl https://tu-app.railway.app/health

# Ping
curl https://tu-app.railway.app/ping
```

## Ventajas de Cada Opción

### MySQL en Railway
✅ **Persistencia de datos**
✅ **Escalabilidad**
✅ **Producción lista**
❌ **Requiere configuración adicional**

### H2 en Memoria
✅ **Configuración rápida**
✅ **Sin dependencias externas**
✅ **Perfecto para pruebas**
❌ **Datos se pierden al reiniciar**

## Recomendación

**Para desarrollo/pruebas**: Usa H2 (Opción 2)
**Para producción**: Usa MySQL en Railway (Opción 1)
