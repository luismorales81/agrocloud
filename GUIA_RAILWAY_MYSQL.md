# Guía de Despliegue en Railway con MySQL - AgroCloud

## 🚀 Configuración de Railway con MySQL

### Paso 1: Crear Proyecto en Railway

1. **Ir a Railway.app** y crear cuenta/iniciar sesión
2. **Crear nuevo proyecto**
3. **Conectar repositorio Git** (GitHub, GitLab, etc.)

### Paso 2: Agregar Servicio MySQL

1. **En el proyecto de Railway, hacer clic en "New Service"**
2. **Seleccionar "Database"**
3. **Elegir "MySQL"**
4. **Seleccionar plan:**
   - **Free:** Para pruebas (limitado)
   - **Pro:** Para producción (recomendado)

### Paso 3: Configurar Variables de Entorno

En el servicio de la aplicación, configurar las siguientes variables:

```env
# Perfil de Spring Boot
SPRING_PROFILES_ACTIVE=railway-mysql

# Configuración de MySQL (Railway las proporciona automáticamente)
DATABASE_URL=mysql://[host]:[port]/[database]?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
DB_USERNAME=[username]
DB_PASSWORD=[password]

# JWT Secret (generar uno seguro)
JWT_SECRET=tu_jwt_secret_muy_seguro_aqui_2024_agrocloud

# Configuración adicional
SPRING_DATASOURCE_DRIVER_CLASS_NAME=com.mysql.cj.jdbc.Driver
SPRING_JPA_HIBERNATE_DDL_AUTO=update
SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT=org.hibernate.dialect.MySQL8Dialect
```

### Paso 4: Configurar Build y Deploy

1. **En el servicio de la aplicación, ir a "Settings"**
2. **Configurar Build Command:**
   ```bash
   cd agrogestion-backend && mvn clean package -DskipTests
   ```
3. **Configurar Start Command:**
   ```bash
   java -jar agrogestion-backend/target/agrocloud-backend-1.0.0.jar
   ```

### Paso 5: Conectar Base de Datos

1. **En el servicio MySQL, ir a "Connect"**
2. **Copiar las variables de entorno proporcionadas**
3. **Pegarlas en el servicio de la aplicación**

### Paso 6: Desplegar

1. **Railway detectará automáticamente los cambios**
2. **El build comenzará automáticamente**
3. **La aplicación se desplegará**

## 🔧 Configuración Avanzada

### Variables de Entorno Recomendadas

```env
# Configuración de la aplicación
SPRING_PROFILES_ACTIVE=railway-mysql
SERVER_PORT=8080

# Configuración de MySQL
DATABASE_URL=mysql://[host]:[port]/[database]?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=utf8
DB_USERNAME=[username]
DB_PASSWORD=[password]

# JWT Configuration
JWT_SECRET=tu_jwt_secret_muy_seguro_aqui_2024_agrocloud
JWT_EXPIRATION=86400000
JWT_REFRESH_TOKEN_EXPIRATION=604800000

# Logging
LOGGING_LEVEL_COM_AGROCLOUD=INFO
LOGGING_LEVEL_ORG_SPRINGFRAMEWORK_SECURITY=WARN

# CORS
SPRING_WEB_CORS_ALLOWED_ORIGINS=*

# Actuator
MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,info,metrics
```

### Configuración de Dominio Personalizado

1. **En Railway, ir a "Settings" del servicio**
2. **Sección "Domains"**
3. **Agregar dominio personalizado**
4. **Configurar DNS según las instrucciones**

## 📊 Monitoreo y Logs

### Ver Logs en Railway

```bash
# Instalar Railway CLI
npm install -g @railway/cli

# Login
railway login

# Ver logs
railway logs

# Ver logs en tiempo real
railway logs --follow
```

### Health Check

```bash
# Verificar estado de la aplicación
curl https://tu-app.railway.app/actuator/health

# Verificar base de datos
curl https://tu-app.railway.app/actuator/health/db
```

## 🔒 Seguridad

### Variables de Entorno Críticas

- **JWT_SECRET:** Generar una clave segura de al menos 32 caracteres
- **DB_PASSWORD:** Railway la genera automáticamente
- **DATABASE_URL:** Railway la proporciona automáticamente

### Recomendaciones de Seguridad

1. **JWT Secret:** Usar una clave aleatoria fuerte
2. **HTTPS:** Railway proporciona SSL automáticamente
3. **CORS:** Configurar solo los orígenes necesarios
4. **Logs:** No exponer información sensible en logs

## 🛠️ Troubleshooting

### Problemas Comunes

#### 1. Error de Conexión a MySQL

**Síntomas:**
```
Could not create connection to database server
```

**Solución:**
- Verificar que las variables de entorno estén correctas
- Comprobar que el servicio MySQL esté ejecutándose
- Verificar la URL de conexión

#### 2. Error de Build

**Síntomas:**
```
Build failed
```

**Solución:**
- Verificar que el `pom.xml` esté correcto
- Comprobar que todas las dependencias estén disponibles
- Revisar los logs de build

#### 3. Error de Inicio

**Síntomas:**
```
Application failed to start
```

**Solución:**
- Verificar variables de entorno
- Comprobar configuración de MySQL
- Revisar logs de la aplicación

### Comandos Útiles

```bash
# Ver estado de los servicios
railway status

# Reiniciar servicio
railway service restart

# Ver variables de entorno
railway variables

# Conectar a la base de datos
railway connect
```

## 📝 Checklist de Despliegue

- [ ] Proyecto creado en Railway
- [ ] Servicio MySQL agregado
- [ ] Variables de entorno configuradas
- [ ] Build command configurado
- [ ] Start command configurado
- [ ] Base de datos conectada
- [ ] Aplicación desplegada
- [ ] Health check exitoso
- [ ] Dominio configurado (opcional)
- [ ] SSL verificado
- [ ] Logs monitoreados

## 🔗 Enlaces Útiles

- [Railway Documentation](https://docs.railway.app/)
- [Railway MySQL Service](https://docs.railway.app/databases/mysql)
- [Railway CLI](https://docs.railway.app/reference/cli)
- [Spring Boot Railway](https://docs.railway.app/deploy/deployments/spring-boot)

## 💡 Tips

1. **Usar Railway CLI** para gestión desde terminal
2. **Configurar alerts** para monitoreo
3. **Hacer backups** regulares de la base de datos
4. **Usar variables de entorno** para configuración
5. **Monitorear logs** regularmente
