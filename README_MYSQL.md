# AgroCloud - Configuración con MySQL

## 🗄️ Configuración de Base de Datos MySQL

Este documento explica cómo configurar y ejecutar AgroCloud usando MySQL como motor de base de datos.

## 📋 Prerrequisitos

- Java 17 o superior
- Maven 3.6+
- Docker y Docker Compose
- MySQL 8.0 (opcional, se puede usar Docker)

## 🚀 Ejecución Local con MySQL

### Opción 1: Usando Docker Compose (Recomendado)

1. **Iniciar MySQL con Docker:**
   ```bash
   docker-compose up -d mysql
   ```

2. **Ejecutar la aplicación:**
   ```bash
   # En Windows
   run-mysql-local.bat
   
   # En Linux/Mac
   cd agrogestion-backend
   mvn spring-boot:run -Dspring-boot.run.profiles=dev
   ```

### Opción 2: MySQL Local

1. **Instalar MySQL 8.0**
2. **Crear la base de datos:**
   ```sql
   CREATE DATABASE agroclouddb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

3. **Ejecutar el script de inicialización:**
   ```bash
   mysql -u root -p agroclouddb < agrogestion-backend/src/main/resources/schema-mysql.sql
   ```

4. **Configurar credenciales en `application-dev.properties`**

5. **Ejecutar la aplicación:**
   ```bash
   cd agrogestion-backend
   mvn spring-boot:run -Dspring-boot.run.profiles=dev
   ```

## 🔧 Configuración de Perfiles

### Desarrollo Local (`application-dev.properties`)
- Base de datos: MySQL local
- Logging: Detallado
- DDL: `update` (actualiza esquema automáticamente)

### Producción Railway (`application-railway-mysql.properties`)
- Base de datos: MySQL en Railway
- Logging: Básico
- DDL: `update`
- Variables de entorno para configuración

## 🌐 Acceso a la Aplicación

- **Backend API:** http://localhost:8080
- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **PhpMyAdmin:** http://localhost:8081 (con Docker Compose)

## 👤 Credenciales por Defecto

- **Usuario:** admin
- **Contraseña:** admin123

## 🚀 Despliegue en Railway con MySQL

### 1. Configurar Base de Datos MySQL en Railway

1. **Crear nuevo proyecto en Railway**
2. **Agregar servicio MySQL:**
   - Template: MySQL
   - Plan: Pro (recomendado para producción)

### 2. Configurar Variables de Entorno

En Railway, configurar las siguientes variables:

```env
SPRING_PROFILES_ACTIVE=railway-mysql
DATABASE_URL=mysql://[host]:[port]/[database]?useSSL=false&serverTimezone=UTC
DB_USERNAME=[username]
DB_PASSWORD=[password]
JWT_SECRET=[tu_jwt_secret_seguro]
```

### 3. Desplegar la Aplicación

1. **Conectar repositorio Git**
2. **Configurar build:**
   - Build Command: `cd agrogestion-backend && mvn clean package -DskipTests`
   - Start Command: `java -jar agrogestion-backend/target/agrocloud-backend-1.0.0.jar`

### 4. Configurar Dominio Personalizado (Opcional)

1. **Agregar dominio personalizado en Railway**
2. **Configurar DNS**
3. **Actualizar CORS en la aplicación**

## 📊 Monitoreo y Logs

### Railway Logs
```bash
railway logs
```

### Health Check
```bash
curl https://tu-app.railway.app/actuator/health
```

## 🔒 Seguridad

### Variables de Entorno Críticas
- `JWT_SECRET`: Clave secreta para JWT
- `DB_PASSWORD`: Contraseña de base de datos
- `DATABASE_URL`: URL de conexión a MySQL

### Recomendaciones
- Usar contraseñas fuertes
- Rotar JWT secrets regularmente
- Habilitar SSL en producción
- Configurar firewall en Railway

## 🛠️ Troubleshooting

### Problemas Comunes

1. **Error de conexión a MySQL:**
   - Verificar credenciales
   - Comprobar que MySQL esté ejecutándose
   - Verificar puerto 3306

2. **Error de migración:**
   - Verificar permisos de usuario MySQL
   - Comprobar que la base de datos existe

3. **Error en Railway:**
   - Verificar variables de entorno
   - Comprobar logs de Railway
   - Verificar configuración de MySQL

### Comandos Útiles

```bash
# Verificar estado de MySQL
docker-compose ps

# Ver logs de MySQL
docker-compose logs mysql

# Conectar a MySQL
docker exec -it agrocloud-mysql mysql -u root -p

# Reiniciar servicios
docker-compose restart
```

## 📝 Notas Importantes

- MySQL 8.0 requiere configuración específica para autenticación
- El script `schema-mysql.sql` se ejecuta automáticamente con Docker Compose
- Railway proporciona MySQL como servicio gestionado
- La aplicación usa `update` para DDL en producción (cuidado con cambios de esquema)

## 🔗 Enlaces Útiles

- [MySQL 8.0 Documentation](https://dev.mysql.com/doc/refman/8.0/en/)
- [Railway MySQL Service](https://docs.railway.app/databases/mysql)
- [Spring Boot MySQL](https://spring.io/guides/gs/accessing-data-mysql/)
