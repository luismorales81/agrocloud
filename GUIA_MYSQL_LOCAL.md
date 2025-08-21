# Guía de Configuración MySQL Local - AgroCloud

## 🚀 Configuración Rápida sin Docker

### Opción 1: MySQL Local (Recomendado para desarrollo)

#### 1. Instalar MySQL 8.0

**Windows:**
1. Descargar MySQL Installer desde: https://dev.mysql.com/downloads/installer/
2. Ejecutar el instalador
3. Seleccionar "Developer Default" o "Server only"
4. Configurar contraseña root: `password`
5. Finalizar instalación

**Alternativa - XAMPP:**
1. Descargar XAMPP desde: https://www.apachefriends.org/
2. Instalar XAMPP
3. Iniciar MySQL desde el panel de control

#### 2. Crear Base de Datos

```sql
-- Conectar a MySQL como root
mysql -u root -p

-- Crear base de datos
CREATE DATABASE agroclouddb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Crear usuario específico (opcional)
CREATE USER 'agrocloud'@'localhost' IDENTIFIED BY 'agrocloud123';
GRANT ALL PRIVILEGES ON agroclouddb.* TO 'agrocloud'@'localhost';
FLUSH PRIVILEGES;

-- Salir
EXIT;
```

#### 3. Ejecutar Script de Inicialización

```bash
# Desde la raíz del proyecto
mysql -u root -p agroclouddb < agrogestion-backend/src/main/resources/schema-mysql.sql
```

#### 4. Configurar Aplicación

Editar `agrogestion-backend/src/main/resources/application-dev.properties`:

```properties
# Configuración de la base de datos MySQL para desarrollo local
spring.datasource.url=jdbc:mysql://localhost:3306/agroclouddb?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=utf8
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.username=root
spring.datasource.password=password
```

#### 5. Ejecutar Aplicación

```bash
cd agrogestion-backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Opción 2: Con Docker (cuando esté disponible)

1. **Iniciar Docker Desktop**
2. **Ejecutar MySQL:**
   ```bash
   docker-compose up -d mysql
   ```
3. **Ejecutar aplicación:**
   ```bash
   cd agrogestion-backend
   mvn spring-boot:run -Dspring-boot.run.profiles=dev
   ```

## 🔧 Verificación de Configuración

### 1. Verificar Conexión MySQL

```bash
mysql -u root -p -e "SHOW DATABASES;"
```

### 2. Verificar Tablas Creadas

```bash
mysql -u root -p agroclouddb -e "SHOW TABLES;"
```

### 3. Verificar Usuario Admin

```bash
mysql -u root -p agroclouddb -e "SELECT * FROM usuarios;"
```

## 🌐 Acceso a la Aplicación

- **Backend API:** http://localhost:8080
- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **Health Check:** http://localhost:8080/actuator/health

## 👤 Credenciales por Defecto

- **Usuario:** admin
- **Contraseña:** admin123

## 🛠️ Troubleshooting

### Error: "Access denied for user 'root'@'localhost'"

**Solución:**
```sql
ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY 'password';
FLUSH PRIVILEGES;
```

### Error: "The server time zone value is unrecognized"

**Solución:**
Agregar `serverTimezone=UTC` a la URL de conexión (ya incluido en la configuración).

### Error: "Public Key Retrieval is not allowed"

**Solución:**
Agregar `allowPublicKeyRetrieval=true` a la URL de conexión (ya incluido en la configuración).

### Error: "Driver not found"

**Solución:**
Verificar que MySQL Connector esté en el `pom.xml` (ya incluido).

## 📝 Notas Importantes

1. **Puerto MySQL:** 3306 (por defecto)
2. **Encoding:** UTF-8 para soporte de caracteres especiales
3. **Timezone:** UTC para consistencia
4. **SSL:** Deshabilitado para desarrollo local

## 🔗 Enlaces Útiles

- [MySQL 8.0 Download](https://dev.mysql.com/downloads/mysql/)
- [XAMPP Download](https://www.apachefriends.org/)
- [MySQL Workbench](https://dev.mysql.com/downloads/workbench/) (GUI para MySQL)
