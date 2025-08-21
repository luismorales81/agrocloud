# 🚀 Guía de Despliegue en Railway

Esta guía te ayudará a desplegar el proyecto AgroGestion en Railway.

## 📋 Prerrequisitos

1. **Cuenta en Railway**: [railway.app](https://railway.app)
2. **Git**: Para subir el código
3. **Railway CLI** (opcional): Para despliegues desde línea de comandos

## 🔧 Configuración del Proyecto

### 1. Preparar el Backend

El proyecto ya tiene configuraciones para Railway:

- `application-railway.properties` - Para Railway con PostgreSQL
- `application-railway-mysql.properties` - Para Railway con MySQL
- `railway.json` - Configuración de Railway

### 2. Variables de Entorno en Railway

Configura las siguientes variables de entorno en tu proyecto de Railway:

```bash
# Base de datos
DATABASE_URL=jdbc:mysql://your-mysql-host:3306/your-database
DB_USERNAME=your-username
DB_PASSWORD=your-password

# JWT
JWT_SECRET=your-super-secret-jwt-key-here

# Puerto
PORT=8080

# Perfil de Spring
SPRING_PROFILES_ACTIVE=railway-mysql
```

### 3. Configurar Base de Datos

#### Opción A: MySQL en Railway
1. Crear un nuevo servicio MySQL en Railway
2. Railway te proporcionará las credenciales automáticamente
3. Usar el perfil `railway-mysql`

#### Opción B: PostgreSQL en Railway
1. Crear un nuevo servicio PostgreSQL en Railway
2. Usar el perfil `railway`

## 🚀 Pasos de Despliegue

### Paso 1: Conectar Repositorio

1. Ve a [Railway Dashboard](https://railway.app/dashboard)
2. Haz clic en "New Project"
3. Selecciona "Deploy from GitHub repo"
4. Conecta tu repositorio de GitHub
5. Selecciona el repositorio `AgroGestion`

### Paso 2: Configurar Servicios

#### Configurar Backend
1. Railway detectará automáticamente que es un proyecto Java/Maven
2. En la configuración del servicio:
   - **Build Command**: `mvn clean package -DskipTests`
   - **Start Command**: `java -jar target/agrocloud-backend-1.0.0.jar`
   - **Port**: `8080`

#### Configurar Base de Datos
1. Agregar un nuevo servicio MySQL o PostgreSQL
2. Railway generará automáticamente las variables de entorno

### Paso 3: Configurar Variables de Entorno

En el servicio del backend, configurar:

```bash
# Para MySQL
SPRING_PROFILES_ACTIVE=railway-mysql
DATABASE_URL=${DATABASE_URL}
DB_USERNAME=${DB_USERNAME}
DB_PASSWORD=${DB_PASSWORD}
JWT_SECRET=agrogestionSecretKey2024ForJWTTokenGenerationAndValidation

# Para PostgreSQL
SPRING_PROFILES_ACTIVE=railway
DATABASE_URL=${DATABASE_URL}
JWT_SECRET=agrogestionSecretKey2024ForJWTTokenGenerationAndValidation
```

### Paso 4: Configurar Frontend (Opcional)

Si quieres desplegar también el frontend:

1. Crear un nuevo servicio para el frontend
2. Configurar:
   - **Build Command**: `npm install && npm run build`
   - **Start Command**: `npm run preview`
   - **Port**: `4173`

3. Variables de entorno del frontend:
```bash
VITE_API_BASE_URL=https://your-backend-url.railway.app/api
```

## 🔗 Configuración de Dominios

### Backend
- Railway asignará automáticamente un dominio
- Ejemplo: `https://agrogestion-backend-production.up.railway.app`

### Frontend
- Configurar el dominio del backend en las variables de entorno
- Actualizar `VITE_API_BASE_URL` con la URL del backend

## 📊 Monitoreo

### Logs
- Ve a la pestaña "Logs" en Railway para ver los logs en tiempo real
- Útil para debugging

### Métricas
- Railway proporciona métricas de uso de CPU, memoria y red
- Monitorea el rendimiento de tu aplicación

## 🔧 Troubleshooting

### Problemas Comunes

#### 1. Error de Conexión a Base de Datos
```bash
# Verificar variables de entorno
echo $DATABASE_URL
echo $DB_USERNAME
echo $DB_PASSWORD
```

#### 2. Error de Puerto
```bash
# Asegurar que PORT esté configurado
echo $PORT
```

#### 3. Error de Build
```bash
# Verificar que Maven esté configurado correctamente
# Revisar logs de build en Railway
```

#### 4. Error de JWT
```bash
# Verificar que JWT_SECRET esté configurado
echo $JWT_SECRET
```

### Comandos Útiles

```bash
# Ver logs en tiempo real
railway logs

# Conectar a la base de datos
railway connect

# Ver variables de entorno
railway variables
```

## 🔄 Actualizaciones

### Despliegue Automático
1. Railway se conecta automáticamente a tu repositorio
2. Cada push a `main` o `master` desplegará automáticamente
3. Puedes configurar ramas específicas para despliegue

### Despliegue Manual
```bash
# Usando Railway CLI
railway up

# O desde GitHub
# Simplemente haz push a tu rama principal
```

## 📱 Configuración de CORS

El backend ya tiene configurado CORS para permitir conexiones desde cualquier origen. Si necesitas restringir:

```java
// En SecurityConfig.java
.cors(cors -> cors.configurationSource(corsConfigurationSource()))
```

## 🔒 Seguridad

### Variables Sensibles
- Nunca commits credenciales en el código
- Usa variables de entorno de Railway
- Rota las claves JWT regularmente

### Base de Datos
- Railway proporciona conexiones SSL por defecto
- Las credenciales se manejan automáticamente

## 📈 Escalabilidad

### Auto-scaling
- Railway puede escalar automáticamente según la demanda
- Configura límites de recursos según tus necesidades

### Monitoreo
- Usa las métricas de Railway para optimizar recursos
- Configura alertas para problemas de rendimiento

## 🎯 Próximos Pasos

1. **Configurar dominio personalizado** (opcional)
2. **Configurar SSL** (automático en Railway)
3. **Configurar backups** de la base de datos
4. **Configurar monitoreo** avanzado
5. **Configurar CI/CD** personalizado

---

**¡Tu aplicación AgroGestion estará lista para producción en Railway! 🚀**
