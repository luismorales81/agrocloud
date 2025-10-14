# Configuración de Variables de Entorno - Backend

## 📋 Archivos necesarios

### 1. `agrogestion-backend/src/main/resources/application-prod.properties`

Crear este archivo para producción:

```properties
# ========================================
# AgroCloud - Configuración PRODUCCIÓN
# ========================================

# Perfil activo
spring.profiles.active=prod

# Configuración del servidor
server.port=${SERVER_PORT:8080}
server.address=0.0.0.0

# Base de datos - Variables de entorno
spring.datasource.url=${DATABASE_URL}
spring.datasource.username=${DATABASE_USERNAME}
spring.datasource.password=${DATABASE_PASSWORD}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# Pool de conexiones
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.idle-timeout=600000
spring.datasource.hikari.max-lifetime=1800000

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect

# Flyway - Migraciones
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true
spring.flyway.validate-on-migrate=true

# Logging
logging.level.root=INFO
logging.level.com.agrocloud=INFO
logging.level.org.hibernate.SQL=WARN

# JWT
jwt.secret=${JWT_SECRET}
jwt.expiration=${JWT_EXPIRATION:86400000}

# CORS - URL del frontend
cors.allowed.origins=${FRONTEND_URL:https://app.tudominio.com}

# Email Service URLs
email.frontend.url=${FRONTEND_URL:https://app.tudominio.com}
email.reset.password.url=${FRONTEND_URL}/reset-password
email.verify.email.url=${FRONTEND_URL}/verify-email

# Configuración de archivos
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
```

### 2. Variables de entorno en el servidor

Configurar en el servidor (Linux/Docker):

```bash
# Base de datos
export DATABASE_URL="jdbc:mysql://tu-host-mysql:3306/agrocloud?useSSL=true&serverTimezone=UTC"
export DATABASE_USERNAME="agrocloud_user"
export DATABASE_PASSWORD="tu-password-seguro"

# JWT
export JWT_SECRET="tu-secreto-jwt-muy-largo-y-seguro-123456789"
export JWT_EXPIRATION="86400000"

# Frontend URL
export FRONTEND_URL="https://app.tudominio.com"

# Puerto del servidor
export SERVER_PORT="8080"
```

### 3. Docker (opcional)

Si usas Docker, crear `.env` en la raíz:

```env
# Base de datos
DATABASE_URL=jdbc:mysql://mysql:3306/agrocloud?useSSL=false&serverTimezone=UTC
DATABASE_USERNAME=agrocloud
DATABASE_PASSWORD=password_seguro

# JWT
JWT_SECRET=tu-secreto-jwt-muy-largo
JWT_EXPIRATION=86400000

# Frontend
FRONTEND_URL=https://app.tudominio.com

# Puerto
SERVER_PORT=8080
```

## 🔧 Cambios necesarios en el código

### 1. SecurityConfig.java - CORS dinámico

```java
@Value("${cors.allowed.origins}")
private String allowedOrigins;

@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    
    // Convertir String separado por comas en List
    List<String> origins = Arrays.asList(allowedOrigins.split(","));
    configuration.setAllowedOriginPatterns(origins);
    
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setAllowCredentials(true);
    configuration.setMaxAge(3600L);
    
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}
```

### 2. EmailService.java - URLs dinámicas

```java
@Value("${email.reset.password.url}")
private String resetPasswordUrl;

@Value("${email.verify.email.url}")
private String verifyEmailUrl;

public void sendPasswordResetEmail(String email, String resetToken) {
    String resetUrl = resetPasswordUrl + "?token=" + resetToken;
    logger.info("URL de recuperación: {}", resetUrl);
    // ... resto del código
}
```

### 3. Remover @CrossOrigin hardcodeado

En todos los controllers, **REMOVER**:
```java
@CrossOrigin(origins = {"http://localhost:3000", ...})  // ❌ Eliminar
```

La configuración global de CORS en SecurityConfig es suficiente.

## 🚀 Despliegue

### Desarrollo local:
```bash
mvn spring-boot:run
```

### Producción:
```bash
# Construir
mvn clean package -DskipTests

# Ejecutar con perfil de producción
java -jar target/agrogestion-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

### Con Docker:
```bash
docker-compose up -d
```

## ⚠️ Importante

- ✅ Nunca subir archivos `.env` a Git
- ✅ Usar secrets managers en producción (AWS Secrets, Azure Key Vault, etc.)
- ✅ Cambiar JWT_SECRET en cada ambiente
- ✅ Usar SSL/TLS en producción
- ✅ Configurar firewall para MySQL (no exponer puerto 3306 públicamente)

