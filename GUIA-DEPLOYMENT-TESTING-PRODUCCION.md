# 🚀 Guía Completa: Deployment Testing y Producción

## 📊 Arquitectura de Ambientes

```
┌─────────────────────────────────────────────────────────────┐
│                    AMBIENTE TESTING                         │
├─────────────────────────────────────────────────────────────┤
│ Frontend: agrogestion-testing.vercel.app                    │
│ Backend:  agrogestion-testing.up.railway.app                │
│ BD MySQL: Railway MySQL (Testing)                           │
│ Propósito: Pruebas, desarrollo, demos                       │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                  AMBIENTE PRODUCCIÓN                        │
├─────────────────────────────────────────────────────────────┤
│ Frontend: agrogestion.vercel.app (o tu dominio)            │
│ Backend:  agrogestion.up.railway.app                        │
│ BD MySQL: Railway MySQL (Producción)                        │
│ Propósito: Uso real, clientes, datos reales                │
└─────────────────────────────────────────────────────────────┘
```

---

## 🎯 Paso 1: Preparar el Backend para Deployment

### 1.1. Crear Perfiles de Spring Boot

**Archivo:** `agrogestion-backend/src/main/resources/application.yml`

```yaml
# Configuración por defecto (Development)
spring:
  application:
    name: agrogestion-backend
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}

---
# Perfil DEVELOPMENT (local)
spring:
  config:
    activate:
      on-profile: dev
  datasource:
    url: jdbc:mysql://localhost:3306/agrogestion_db?useSSL=false&serverTimezone=UTC
    username: root
    password: ${DB_PASSWORD:tu_password}
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true

server:
  port: 8080

---
# Perfil TESTING
spring:
  config:
    activate:
      on-profile: testing
  datasource:
    url: ${DATABASE_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect

server:
  port: ${PORT:8080}

cors:
  allowed-origins: https://agrogestion-testing.vercel.app,http://localhost:3000

---
# Perfil PRODUCTION
spring:
  config:
    activate:
      on-profile: production
  datasource:
    url: ${DATABASE_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: validate  # ⚠️ No auto-actualizar en producción
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect

server:
  port: ${PORT:8080}

cors:
  allowed-origins: https://agrogestion.vercel.app,https://www.agrogestion.com

# Configuración de seguridad adicional
logging:
  level:
    root: WARN
    com.agrocloud: INFO
```

### 1.2. Actualizar application.properties (Alternativa Simple)

**Archivo:** `agrogestion-backend/src/main/resources/application.properties`

```properties
# Perfil activo (será sobreescrito por variable de entorno)
spring.profiles.active=${SPRING_PROFILES_ACTIVE:mysql}

# Configuración de base de datos (usar variables de entorno)
spring.datasource.url=${DATABASE_URL:jdbc:mysql://localhost:3306/agrogestion_db?useSSL=false&serverTimezone=UTC}
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD:}

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=${HIBERNATE_DDL_AUTO:update}
spring.jpa.show-sql=${SHOW_SQL:false}
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect

# Puerto del servidor
server.port=${PORT:8080}

# JWT
jwt.secret=${JWT_SECRET:tu_secret_muy_largo_y_seguro_aqui_cambialo_en_produccion}
jwt.expiration=${JWT_EXPIRATION:86400000}

# CORS (será configurado por variable de entorno)
cors.allowed.origins=${CORS_ALLOWED_ORIGINS:http://localhost:3000,http://localhost:5173}
```

### 1.3. Configurar CORS Dinámico

**Archivo:** `agrogestion-backend/src/main/java/com/agrocloud/config/WebConfig.java`

```java
package com.agrocloud.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${cors.allowed.origins:http://localhost:3000}")
    private String allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins.split(","))
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
```

---

## 🚂 Paso 2: Railway - Backend y Base de Datos

### 2.1. Crear Proyecto TESTING en Railway

1. **Ve a [railway.app](https://railway.app)**
2. Click en **"New Project"**
3. Nombra el proyecto: **"AgroGestion-Testing"**

#### A. Crear Servicio MySQL (Testing)

1. Click en **"+ New"** → **"Database"** → **"MySQL"**
2. Espera a que se cree
3. Ve a la pestaña **"Variables"** y copia:
   - `MYSQL_URL`
   - `MYSQL_USER`
   - `MYSQL_PASSWORD`
   - `MYSQL_DATABASE`

#### B. Crear Servicio Backend (Testing)

1. En el mismo proyecto, click **"+ New"** → **"GitHub Repo"**
2. Conecta tu repositorio
3. Selecciona la carpeta: `agrogestion-backend`
4. Ve a **"Settings"** → **"Build"**:
   - **Build Command:** `mvn clean package -DskipTests`
   - **Start Command:** `java -jar target/agrocloud-backend-1.0.0.jar`

5. Ve a **"Variables"** y agrega:

```env
SPRING_PROFILES_ACTIVE=testing
DATABASE_URL=jdbc:mysql://${MYSQL_HOST}:${MYSQL_PORT}/${MYSQL_DATABASE}?useSSL=false&serverTimezone=UTC
DB_USERNAME=${MYSQL_USER}
DB_PASSWORD=${MYSQL_PASSWORD}
HIBERNATE_DDL_AUTO=update
SHOW_SQL=false
JWT_SECRET=testing_secret_key_change_me_12345678901234567890
CORS_ALLOWED_ORIGINS=https://agrogestion-testing.vercel.app,http://localhost:3000
PORT=8080
```

6. Click en **"Deploy"**

#### C. Obtener URL del Backend Testing

Una vez desplegado, Railway te dará una URL como:
```
https://agrogestion-testing.up.railway.app
```

Guárdala, la necesitarás para el frontend.

---

### 2.2. Crear Proyecto PRODUCCIÓN en Railway

Repite el proceso anterior pero con estos cambios:

1. **Nombre del proyecto:** "AgroGestion-Production"
2. **Variables de entorno diferentes:**

```env
SPRING_PROFILES_ACTIVE=production
DATABASE_URL=jdbc:mysql://${MYSQL_HOST}:${MYSQL_PORT}/${MYSQL_DATABASE}?useSSL=true&serverTimezone=UTC
DB_USERNAME=${MYSQL_USER}
DB_PASSWORD=${MYSQL_PASSWORD}
HIBERNATE_DDL_AUTO=validate
SHOW_SQL=false
JWT_SECRET=production_secret_muy_largo_y_seguro_cambialo_ahora_123456789
CORS_ALLOWED_ORIGINS=https://agrogestion.vercel.app,https://www.agrogestion.com
PORT=8080
```

⚠️ **IMPORTANTE:** Usa un `JWT_SECRET` DIFERENTE y MÁS SEGURO en producción.

---

## 🎨 Paso 3: Vercel - Frontend

### 3.1. Preparar Variables de Entorno del Frontend

**Archivo:** `agrogestion-frontend/.env.testing`

```env
VITE_API_URL=https://agrogestion-testing.up.railway.app
VITE_ENVIRONMENT=testing
```

**Archivo:** `agrogestion-frontend/.env.production`

```env
VITE_API_URL=https://agrogestion.up.railway.app
VITE_ENVIRONMENT=production
```

### 3.2. Actualizar la Configuración de API

**Archivo:** `agrogestion-frontend/src/services/api.ts`

Asegúrate de que use la variable de entorno:

```typescript
const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

const api = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});
```

### 3.3. Deploy en Vercel - TESTING

1. **Ve a [vercel.com](https://vercel.com)**
2. Click en **"Add New..."** → **"Project"**
3. Importa tu repositorio de GitHub
4. Configura el proyecto:
   - **Project Name:** `agrogestion-testing`
   - **Framework Preset:** Vite
   - **Root Directory:** `agrogestion-frontend`
   - **Build Command:** `npm run build`
   - **Output Directory:** `dist`

5. En **"Environment Variables"**, agrega:
   - Key: `VITE_API_URL`
   - Value: `https://agrogestion-testing.up.railway.app`
   - Environment: **Preview** y **Development**

6. Click en **"Deploy"**

Tu app estará disponible en:
```
https://agrogestion-testing.vercel.app
```

---

### 3.4. Deploy en Vercel - PRODUCCIÓN

1. Repite el proceso anterior
2. Configura el proyecto:
   - **Project Name:** `agrogestion-production` (o solo `agrogestion`)
   - Mismas configuraciones

3. En **"Environment Variables"**, agrega:
   - Key: `VITE_API_URL`
   - Value: `https://agrogestion.up.railway.app`
   - Environment: **Production**

4. Click en **"Deploy"**

Tu app estará disponible en:
```
https://agrogestion.vercel.app
```

---

## 🔒 Paso 4: Seguridad y Mejores Prácticas

### 4.1. Configurar Dominios Personalizados (Opcional)

#### En Vercel:
1. Ve a tu proyecto → **"Settings"** → **"Domains"**
2. Agrega tu dominio:
   - Testing: `testing.agrogestion.com`
   - Production: `agrogestion.com` y `www.agrogestion.com`

#### En Railway:
1. Ve a tu servicio → **"Settings"** → **"Networking"**
2. Agrega dominio personalizado:
   - Testing: `api-testing.agrogestion.com`
   - Production: `api.agrogestion.com`

### 4.2. Actualizar CORS con Dominios Personalizados

Actualiza las variables `CORS_ALLOWED_ORIGINS` en Railway:

**Testing:**
```
https://agrogestion-testing.vercel.app,https://testing.agrogestion.com,http://localhost:3000
```

**Production:**
```
https://agrogestion.vercel.app,https://agrogestion.com,https://www.agrogestion.com
```

### 4.3. Secrets y Variables Sensibles

✅ **Hacer:**
- Usa secretos diferentes para Testing y Production
- Genera un JWT_SECRET único por ambiente (mínimo 32 caracteres)
- Nunca commits archivos `.env` con secretos reales

❌ **No hacer:**
- No usar los mismos secretos en ambos ambientes
- No compartir credenciales de producción con testing
- No usar passwords débiles en ningún ambiente

---

## 📊 Paso 5: Migración de Datos

### 5.1. Preparar Datos Iniciales para Testing

**Script:** `seed-testing-data.sql`

```sql
-- Datos de prueba para Testing
INSERT INTO usuarios (email, username, password, first_name, last_name, activo) VALUES
('admin.testing@agrogestion.com', 'admin', '$2a$10$...', 'Admin', 'Testing', true);

-- Empresa de prueba
INSERT INTO empresas (nombre, cuit, email_contacto, telefono, estado) VALUES
('Empresa Demo Testing', '20-12345678-9', 'demo@testing.com', '123456789', 'ACTIVO');

-- Más datos de prueba...
```

### 5.2. Ejecutar Migraciones

**Para Testing:**
1. Conéctate a la BD de Railway Testing
2. Ejecuta los scripts de inicialización
3. Crea usuarios de prueba

**Para Production:**
1. Haz un backup completo de la BD actual (si migras datos existentes)
2. Ejecuta migraciones validadas
3. Verifica integridad de datos

---

## 🔄 Paso 6: Workflow de Deployment

### Estrategia Recomendada

```
┌──────────────┐
│   Desarrollo │  (Local)
│              │
└──────┬───────┘
       │
       │ git push origin develop
       ▼
┌──────────────┐
│   Testing    │  (Railway Testing + Vercel Testing)
│              │  - Auto-deploy desde rama 'develop'
└──────┬───────┘  - Pruebas completas
       │          - QA
       │
       │ Aprobado → git push origin main
       ▼
┌──────────────┐
│  Production  │  (Railway Production + Vercel Production)
│              │  - Deploy manual o desde rama 'main'
└──────────────┘  - Solo código probado
```

### 6.1. Configurar Ramas en Git

```bash
# Crear rama develop si no existe
git checkout -b develop

# Crear rama main/master para producción
git checkout -b main
```

### 6.2. Configurar Auto-Deploy

#### En Railway:
1. Testing: Auto-deploy desde rama `develop`
2. Production: Auto-deploy desde rama `main`

#### En Vercel:
1. Testing:
   - **Production Branch:** `develop`
   - Auto-deploy: ✅ Enabled

2. Production:
   - **Production Branch:** `main`
   - Auto-deploy: ⚠️ Manual approval (recomendado)

---

## 🧪 Paso 7: Verificación Post-Deployment

### Checklist Testing

- [ ] Backend responde en `https://agrogestion-testing.up.railway.app/actuator/health`
- [ ] Frontend carga en `https://agrogestion-testing.vercel.app`
- [ ] Login funciona correctamente
- [ ] CORS está configurado (no hay errores en consola)
- [ ] Base de datos se conecta correctamente
- [ ] Todos los módulos son accesibles

### Checklist Production

- [ ] Backend responde correctamente
- [ ] Frontend carga sin errores
- [ ] Certificados SSL válidos
- [ ] Logs no muestran errores críticos
- [ ] Performance es aceptable
- [ ] Backup de BD configurado

---

## 📝 Paso 8: Documentación y Monitoreo

### URLs Importantes

**Testing:**
- Frontend: `https://agrogestion-testing.vercel.app`
- Backend: `https://agrogestion-testing.up.railway.app`
- Health Check: `https://agrogestion-testing.up.railway.app/actuator/health`

**Production:**
- Frontend: `https://agrogestion.vercel.app`
- Backend: `https://agrogestion.up.railway.app`
- Health Check: `https://agrogestion.up.railway.app/actuator/health`

### Monitoreo

1. **Railway Dashboard:** Ver logs y métricas
2. **Vercel Analytics:** Monitorear performance del frontend
3. **Health Checks:** Configurar alertas si el servicio cae

---

## 🎯 Resumen de Costos Estimados

| Servicio | Plan | Costo Testing | Costo Production |
|----------|------|---------------|------------------|
| Railway | Hobby | $5/mes | $20-50/mes |
| Vercel | Hobby | Gratis | Gratis (o Pro $20) |
| **TOTAL** | | **~$5/mes** | **~$20-70/mes** |

---

## ⚡ Comandos Rápidos

### Compilar y Probar Localmente Antes de Deploy

```bash
# Backend
cd agrogestion-backend
mvn clean package -DskipTests
java -jar target/agrocloud-backend-1.0.0.jar

# Frontend
cd agrogestion-frontend
npm run build
npm run preview
```

### Hacer Deploy Manual

```bash
# Commit y push a testing
git checkout develop
git add .
git commit -m "feat: nueva funcionalidad"
git push origin develop

# Después de aprobar en testing, merge a production
git checkout main
git merge develop
git push origin main
```

---

## 🆘 Troubleshooting Común

### Error: "CORS policy"
➡️ Verifica `CORS_ALLOWED_ORIGINS` en Railway

### Error: "Cannot connect to database"
➡️ Verifica `DATABASE_URL` y credenciales en Railway

### Error: "404 Not Found" en API
➡️ Verifica que el backend esté desplegado y corriendo

### Frontend muestra "localhost:8080"
➡️ Verifica que `VITE_API_URL` esté configurada en Vercel

---

## 📚 Próximos Pasos

1. ✅ Configurar Railway Testing
2. ✅ Configurar Railway Production  
3. ✅ Configurar Vercel Testing
4. ✅ Configurar Vercel Production
5. ✅ Probar flujo completo en Testing
6. ✅ Migrar datos a Production
7. ✅ Deploy a Production
8. ✅ Configurar monitoreo
9. ✅ Documentar credenciales (en lugar seguro)

---

**¿Listo para empezar?** Podemos ir paso a paso. ¿Con cuál prefieres comenzar: Railway o Vercel?

