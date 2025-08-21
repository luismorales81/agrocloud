# 📋 Resumen de Configuración - AgroGestion

## ✅ Configuraciones Realizadas

### 1. Base de Datos MySQL Local
- ✅ **Archivo creado**: `agrogestion-backend/src/main/resources/application-mysql.properties`
- ✅ **Configuración**: Conexión a MySQL local (XAMPP)
- ✅ **Script SQL**: `setup-mysql-local.sql` - Crea base de datos y datos de prueba
- ✅ **Usuario por defecto**: `admin` / `admin123`

### 2. Scripts de Ejecución
- ✅ **`start-project.bat`** - Script completo para iniciar backend y frontend
- ✅ **`run-mysql-local.bat`** - Script para ejecutar solo el backend con MySQL
- ✅ **`run-frontend.bat`** - Script para ejecutar solo el frontend

### 3. Configuración para Railway
- ✅ **`GUIA_RAILWAY.md`** - Guía completa de despliegue en Railway
- ✅ **`railway.json`** - Configuración de Railway existente
- ✅ **Perfiles Spring**: `railway` y `railway-mysql` configurados

## 🚀 Cómo Ejecutar Localmente

### Opción 1: Script Completo (Recomendado)
```bash
# Ejecutar el script completo
start-project.bat
```

### Opción 2: Manual
```bash
# 1. Configurar base de datos
# - Abrir XAMPP y iniciar MySQL
# - Abrir phpMyAdmin: http://localhost/phpmyadmin
# - Crear base de datos 'agrocloud'
# - Ejecutar script: setup-mysql-local.sql

# 2. Ejecutar Backend
cd agrogestion-backend
mvn spring-boot:run -Dspring.profiles.active=mysql

# 3. Ejecutar Frontend (en otra terminal)
cd agrogestion-frontend
npm install
npm run dev
```

## 🌐 URLs de Acceso

### Local
- **Frontend**: http://localhost:5173
- **Backend API**: http://localhost:8080/api
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **phpMyAdmin**: http://localhost/phpmyadmin

### Credenciales
- **Usuario**: `admin`
- **Contraseña**: `admin123`

## 🔧 Configuración de Base de Datos

### MySQL Local (XAMPP)
```properties
# application-mysql.properties
spring.datasource.url=jdbc:mysql://localhost:3306/agrocloud?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=update
```

### Railway MySQL
```properties
# application-railway-mysql.properties
spring.datasource.url=${DATABASE_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=update
```

## 📁 Archivos Creados/Modificados

### Nuevos Archivos
1. `agrogestion-backend/src/main/resources/application-mysql.properties`
2. `setup-mysql-local.sql`
3. `start-project.bat`
4. `run-mysql-local.bat`
5. `run-frontend.bat`
6. `GUIA_RAILWAY.md`
7. `RESUMEN_CONFIGURACION.md`

### Archivos Existentes Verificados
1. `agrogestion-backend/pom.xml` - ✅ Dependencias MySQL incluidas
2. `agrogestion-frontend/src/services/api.ts` - ✅ Configuración API correcta
3. `railway.json` - ✅ Configuración Railway correcta

## 🚀 Próximos Pasos para Railway

### 1. Preparar Repositorio
```bash
# Asegurar que todos los cambios estén committeados
git add .
git commit -m "Configuración MySQL local y Railway"
git push origin main
```

### 2. Desplegar en Railway
1. Ir a [Railway Dashboard](https://railway.app/dashboard)
2. Crear nuevo proyecto desde GitHub
3. Conectar repositorio `AgroGestion`
4. Configurar variables de entorno según `GUIA_RAILWAY.md`
5. Agregar servicio MySQL
6. Desplegar

### 3. Variables de Entorno en Railway
```bash
SPRING_PROFILES_ACTIVE=railway-mysql
DATABASE_URL=${DATABASE_URL}
DB_USERNAME=${DB_USERNAME}
DB_PASSWORD=${DB_PASSWORD}
JWT_SECRET=agrogestionSecretKey2024ForJWTTokenGenerationAndValidation
PORT=8080
```

## 🔍 Verificación

### Verificar Backend
```bash
# Health check
curl http://localhost:8080/actuator/health

# API endpoints
curl http://localhost:8080/api/auth/login
```

### Verificar Frontend
```bash
# Verificar que se conecte al backend
# Abrir http://localhost:5173
# Intentar login con admin/admin123
```

### Verificar Base de Datos
```bash
# Conectar a MySQL
mysql -u root -p agrocloud

# Verificar tablas
SHOW TABLES;

# Verificar datos
SELECT * FROM users;
SELECT * FROM roles;
```

## 🛠️ Troubleshooting

### Problemas Comunes

#### 1. Error de Conexión MySQL
```bash
# Verificar que XAMPP MySQL esté corriendo
# Verificar puerto 3306
# Verificar credenciales en application-mysql.properties
```

#### 2. Error de Puerto Ocupado
```bash
# Verificar puertos en uso
netstat -ano | findstr :8080
netstat -ano | findstr :5173

# Matar proceso si es necesario
taskkill /PID <PID> /F
```

#### 3. Error de Dependencias Frontend
```bash
cd agrogestion-frontend
rm -rf node_modules package-lock.json
npm install
```

#### 4. Error de Build Maven
```bash
cd agrogestion-backend
mvn clean
mvn compile
```

## 📞 Soporte

Si encuentras problemas:

1. **Revisar logs** en las consolas de backend y frontend
2. **Verificar configuración** de base de datos
3. **Consultar** `GUIA_RAILWAY.md` para despliegue
4. **Revisar** archivos de configuración creados

---

**¡El proyecto está listo para ejecutarse localmente y desplegarse en Railway! 🚀**
