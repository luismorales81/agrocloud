# Reporte técnico de deploy – AgroGestion / AgroCloud

**Objetivo:** Evaluar si la aplicación está correctamente configurada para ser desplegada en un entorno cloud (Render, Railway, Fly.io o similar).

**Fecha de análisis:** 2026-02-20  
**Alcance:** Backend Spring Boot + Frontend React/Vite; Docker y configuración de producción.

---

## 1. INFORMACIÓN GENERAL DEL PROYECTO

### 1.1 Stack

| Componente | Tecnología |
|------------|------------|
| **Backend** | Spring Boot 3.5.6 |
| **Java** | 17 (definido en `pom.xml`: `java.version`, compiler 17) |
| **Build** | Maven (sin Gradle) |
| **Frontend** | React 19 + Vite 7 (agrogestion-frontend) |

### 1.2 Estructura general

- **Raíz del repo:** `AgroGestion/`
  - `agrogestion-backend/` – aplicación Spring Boot (API REST).
  - `agrogestion-frontend/` – SPA React (Vite).
  - `docs/` – documentación y specs.
  - `docker-compose.yml` – solo MySQL + phpMyAdmin (no incluye backend ni frontend).
  - `Dockerfile` (en raíz) – build del backend desde raíz del repo.

- **Backend:** `agrogestion-backend/`
  - `pom.xml` – Maven, parent Spring Boot 3.5.6, artifact `agrocloud-backend` 1.1.0.
  - `src/main/java` – paquetes `com.agrocloud` (config, controller, service, porcinos, cultivos, etc.).
  - `src/main/resources/` – `application*.properties`, `db/migration/` (Flyway).
  - `Dockerfile` – multi-stage Maven + JRE 17 Alpine (dentro de `agrogestion-backend/`).

### 1.3 ¿Preparado para correr como aplicación standalone?

- **Sí.** El backend está preparado para ejecutarse como JAR standalone:
  - `spring-boot-maven-plugin` con `mainClass`: `com.agrocloud.AgroCloudApplication`.
  - Empaquetado tipo JAR (por defecto en Spring Boot).
  - Comando típico: `java -jar target/<artifact>.jar` (o equivalente en Docker).

**Observación:** El frontend es una SPA que en producción debe servirse por un servidor estático o CDN y apuntar la variable de API al backend desplegado. No se analiza aquí el deploy del frontend (Vercel/Netlify/etc.), pero la URL del backend debe configurarse vía variables de entorno en el build/run del frontend.

---

## 2. CONFIGURACIÓN DE BASE DE DATOS

### 2.1 Motor configurado actualmente

- **Por defecto / desarrollo:** MySQL 8 (driver `com.mysql.cj.jdbc.Driver`, URL `jdbc:mysql://localhost:3306/agrocloud`).
- **Perfil prod:** MySQL vía `DATABASE_URL` (por defecto misma URL local).
- **Perfil railway:** puede usar H2 en memoria (`railway-h2`) o MySQL (`railway-mysql`) según variables.
- **Tests:** H2 (scope test).

En `pom.xml` hay dependencias para MySQL y PostgreSQL (PostgreSQL en runtime para posible uso en Railway); la configuración actual de propiedades apunta a MySQL en todos los perfiles usados en deploy.

### 2.2 Dónde están definidas las credenciales

| Archivo | Credenciales / URL |
|---------|--------------------|
| `application.properties` | **Hardcodeado:** `spring.datasource.username=root`, `password=123456`, URL MySQL local. |
| `application-default.properties` | **Hardcodeado:** misma URL, `root` / `123456`. |
| `application-prod.properties` | **Solo variables de entorno:** `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD` (con valores por defecto locales). |
| `application-railway.properties` | `DATABASE_URL`, `DB_USERNAME`, `DB_PASSWORD` (por defecto H2). |
| `application-railway-mysql.properties` | `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`. |
| `application-agrocloud.properties` | **Hardcodeado:** `agrocloudbd` / `Jones1212` (y Flyway deshabilitado). |

### 2.3 Valores hardcodeados (crítico para seguridad)

- **`application.properties`** (carga base en todos los perfiles):
  - `spring.datasource.username=root`, `password=123456`.
  - **Email Zoho:** `spring.mail.password=XJa6LFJepdHm`, `spring.mail.username=info@AgroCloud.com.ar`.
- **`application-default.properties`:** `root` / `123456`.
- **`application-agrocloud.properties`:** `agrocloudbd` / `Jones1212`.

**Riesgo:** Cualquier persona con acceso al repositorio ve credenciales de BD y de correo. En producción debe usarse siempre perfil que tome solo variables de entorno y **nunca** subir contraseñas al repo.

### 2.4 Soporte de configuración por variables de entorno

- **Perfil prod:** sí, para BD (`DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`) y JWT (`JWT_SECRET` sin valor por defecto).
- **Perfil railway / railway-mysql:** sí, con nombres propios (p. ej. `SPRING_DATASOURCE_*` en railway-mysql).
- **Perfil default:** no; todo está fijo en properties.

Para cloud es imprescindible usar un perfil que no dependa de default para credenciales (p. ej. `prod` o `railway-mysql`) y definir todas las variables en el proveedor.

### 2.5 Pool de conexiones (HikariCP)

- **application.properties (base):**  
  `maximum-pool-size=10`, `minimum-idle=5`, `idle-timeout=300000`, `max-lifetime=1200000`, `connection-timeout=20000`, `leak-detection-threshold=60000`, `validation-timeout=3000`, `connection-test-query=SELECT 1`.
- **application-prod.properties:**  
  `maximum-pool-size=20`, `minimum-idle=5`, timeouts coherentes con producción.
- **application-railway-mysql.properties:**  
  `maximum-pool-size=5`, `minimum-idle=2` (adecuado para entornos con límite de conexiones).

Configuración razonable para producción; en servicios con límite de conexiones (p. ej. MySQL gestionado) conviene bajar el máximo del pool si hace falta.

### 2.6 Estrategia ddl-auto

| Perfil | `spring.jpa.hibernate.ddl-auto` | Observación |
|--------|----------------------------------|-------------|
| default / base | `none` | Correcto si se usan migraciones. |
| prod | `validate` | Correcto: no modifica esquema, solo valida. |
| railway / railway-mysql | `none` | Correcto. |
| railway-h2 | `create-drop` | Solo para H2 en memoria; no usar en producción persistente. |

Flyway: en `application.properties` está `spring.flyway.enabled=true`; en prod y railway está `false` (comentario: migraciones manuales). Si en cloud se usa Flyway, debe habilitarse en el perfil de deploy y desplegar con cuidado para no duplicar migraciones ya aplicadas a mano.

---

## 3. CONFIGURACIÓN PARA PRODUCCIÓN

### 3.1 ¿Existe perfil "prod"?

**Sí.** `application-prod.properties` existe y está pensado para producción.

### 3.2 Diferencias relevantes entre dev y prod

- **Puerto:** base/default usa `server.port=${PORT:8080}`; **prod usa `server.port=${SERVER_PORT:8080}`**.  
  **Problema:** Render, Railway y Fly.io suelen inyectar la variable **`PORT`**, no `SERVER_PORT`. Con perfil `prod` y sin definir `SERVER_PORT`, la app seguiría en 8080 y el proxy del cloud podría no poder enrutar.  
  **Recomendación:** en prod usar `server.port=${PORT:8080}` (o al menos `${PORT:${SERVER_PORT:8080}}`) para compatibilidad con estos proveedores.
- **BD:** prod usa solo variables de entorno; dev/default usan valores fijos.
- **JWT:** prod exige `JWT_SECRET` (sin valor por defecto); base tiene valor por defecto.
- **Logs:** prod reduce nivel (p. ej. `show-sql=false`, menos verbose).
- **CORS:** prod usa `cors.allowed.origins=${FRONTEND_URL:...}` en properties; en código, `SecurityConfig` usa la variable **`CORS_ALLOWED_ORIGINS`** (lista de orígenes separados por coma). Hay que definir `CORS_ALLOWED_ORIGINS` en producción para que CORS sea el deseado.
- **Errores:** prod no expone mensaje, binding errors ni stack trace al cliente (`server.error.*`).

### 3.3 Manejo de logs

- Prod: `logging.level.root=INFO`, `com.agrocloud=INFO`, SQL en WARN, seguridad en INFO.
- Base: mezcla DEBUG en algunos paquetes y luego INFO; en producción el perfil prod debería prevalecer y evitar DEBUG en producción.

### 3.4 Configuración de puerto

- **Base / railway / railway-h2:** `server.port=${PORT:8080}` – correcto para cloud.
- **Prod:** `server.port=${SERVER_PORT:8080}` – **incorrecto** para Render/Railway/Fly.io si solo definen `PORT`.
- **application-prod:** `server.address=0.0.0.0` – correcto para escuchar en todas las interfaces en un contenedor.

### 3.5 CORS

- **SecurityConfig** (origen real usado en runtime):
  - Lee `CORS_ALLOWED_ORIGINS` (env) y, si no está definido y el perfil es prod, usa `https://agrocloud.com.ar` y `https://www.agrocloud.com.ar`.
  - Métodos: GET, POST, PUT, DELETE, PATCH, OPTIONS; headers `*`; `allowCredentials=true`.
- **application.properties:** `spring.web.cors.allowed-origins=http://localhost:3000` – aplicable cuando no se usa el bean de Security (por ejemplo en rutas no pasan por Spring Security). Para producción debe configurarse `CORS_ALLOWED_ORIGINS` en el entorno.
- **application-prod:** `cors.allowed.origins=${FRONTEND_URL:...}` – no es la variable que usa `SecurityConfig`; la que manda es `CORS_ALLOWED_ORIGINS`.

**Recomendación:** Documentar que en producción es obligatorio definir `CORS_ALLOWED_ORIGINS` (y opcionalmente alinear nombres con `FRONTEND_URL` si se desea un solo origen).

### 3.6 Seguridad (JWT, sesiones)

- **JWT:** configurado en properties (`jwt.secret`, `jwt.expiration`, `jwt.refresh-token.expiration`). En prod, `jwt.secret=${JWT_SECRET}` sin valor por defecto – correcto.
- **Sesiones:** `spring.session.store-type=none` – stateless, adecuado para API + JWT.
- **Spring Security:** uso de JWT (filter), CORS desde SecurityConfig, rutas públicas vs autenticadas. No se detalla aquí la lista de rutas, pero la arquitectura es coherente con API REST stateless.

---

## 4. DOCKERIZACIÓN

### 4.1 ¿Existe Dockerfile?

Sí. Hay varios:

- **Raíz del repo:** `Dockerfile` (build del backend asumiendo estructura desde raíz).
- **agrogestion-backend:** `Dockerfile` (build desde dentro del backend).
- Otros: `Dockerfile.simple`, `Dockerfile.simple-app`, `Dockerfile.debug`, `Dockerfile.railway-mysql` (en raíz o en backend).

### 4.2 ¿Está bien armado?

**Dockerfile en `agrogestion-backend/` (el más claro para servicio backend):**

- **Multi-stage:** sí (build con Maven 3.9 + Temurin 17, runtime con `eclipse-temurin:17-jre-alpine`).
- **Build:** `mvn dependency:go-offline` y luego `mvn clean package -DskipTests` – correcto.
- **Runtime:** copia del JAR desde la etapa build, `EXPOSE 8080`.
- **Problema:** `ENV SPRING_PROFILES_ACTIVE=testing` – perfil `testing` puede no existir o no ser el deseado para producción. Para cloud debería ser algo como `prod` o `railway-mysql`, configurable por variable de entorno.

**Dockerfile en raíz:**

- Copia `agrogestion-backend/` (pom y src), compila, etapa runtime con JRE Alpine.
- `HEALTHCHECK` usa `http://localhost:${PORT:-8080}/` – en Docker el `HEALTHCHECK` se ejecuta en shell; según imagen, `PORT` podría no estar definido en el contenedor a menos que el runtime lo inyecte, por lo que suele terminar siendo 8080. Aceptable si el cloud inyecta `PORT` y Spring lee esa variable.
- **ENTRYPOINT:** `-Dspring.profiles.active=railway-h2` – fija perfil **railway-h2** (H2 en memoria). No es adecuado para producción con BD persistente; debería ser configurable (p. ej. `SPRING_PROFILES_ACTIVE`).

### 4.3 ¿Expone correctamente el puerto?

Sí: `EXPOSE 8080` en los Dockerfiles revisados. El servicio escucha en 8080 (o en el que Spring use si se inyecta `PORT`).

### 4.4 ¿Es multi-stage?

Sí en los dos Dockerfiles principales (build + runtime), lo que reduce tamaño de la imagen final.

### 4.5 ¿Hay docker-compose?

Sí: `docker-compose.yml` en la raíz.

- **Servicios:** solo `mysql` (MySQL 8) y `phpmyadmin`.
- **No incluye:** el backend ni el frontend. No sirve por sí solo para “levantar todo en cloud”, pero es útil para desarrollo local con BD.

### 4.6 ¿Incluye base de datos en compose?

Sí. MySQL con volúmenes, usuario y base definidos; phpMyAdmin para administración. Credenciales en el compose (típico para desarrollo; en producción la BD suele ser un servicio gestionado externo).

---

## 5. VARIABLES DE ENTORNO

### 5.1 Listado para producción (perfil `prod`)

Variables que **deben** estar definidas en el proveedor cloud:

| Variable | Uso | Obligatoria en prod |
|----------|-----|----------------------|
| `SPRING_PROFILES_ACTIVE` | Perfil activo (usar `prod`) | Sí |
| `PORT` o `SERVER_PORT` | Puerto HTTP (recomendado usar `PORT` y alinear prod) | Sí (según proveedor) |
| `DATABASE_URL` | URL JDBC de la BD | Sí |
| `DATABASE_USERNAME` | Usuario BD | Sí |
| `DATABASE_PASSWORD` | Contraseña BD | Sí |
| `JWT_SECRET` | Clave para firmar JWT | Sí |
| `CORS_ALLOWED_ORIGINS` | Orígenes permitidos (p. ej. URL del frontend) | Muy recomendado |
| `FRONTEND_URL` | URL del frontend (emails, referencias) | Recomendado |

Opcionales (con valores por defecto en prod):

| Variable | Uso | Default en prod |
|----------|-----|------------------|
| `JWT_EXPIRATION` | Expiración del token (ms) | 86400000 |
| `MAIL_HOST` | SMTP | smtp.zoho.com |
| `MAIL_PORT` | Puerto SMTP | 587 |
| `MAIL_USERNAME` | Usuario SMTP | info@AgroCloud.com.ar |
| `MAIL_PASSWORD` | Contraseña SMTP | (vacío si no se define) |
| `MAIL_FROM` | Remitente | info@AgroCloud.com.ar |

Para **Railway con MySQL** (perfil `railway-mysql`):

- `SPRING_PROFILES_ACTIVE=railway-mysql`
- `PORT` (normalmente inyectado por Railway)
- `SPRING_DATASOURCE_URL` (o el nombre que Railway asigne a la URL MySQL)
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `JWT_SECRET`

### 5.2 Qué NO debería estar en el repo

- Contraseñas de BD (`root`/`123456`, `agrocloudbd`/`Jones1212`).
- Contraseña de email (`XJa6LFJepdHm`) – **crítico:** está en `application.properties`.
- Cualquier `JWT_SECRET` real.
- Claves de APIs externas (p. ej. mapas, email, etc.) si se añadieran a properties.

**Recomendación:** mover credenciales a variables de entorno y, en los properties que se suben al repo, usar solo placeholders o valores vacíos por defecto para producción (como en `application-prod.properties` para mail).

---

## 6. RIESGOS PARA SUBIR A PRODUCCIÓN

### 6.1 Seguridad

- **Credenciales en repo:** `application.properties` y `application-default.properties` contienen usuario/contraseña de BD; `application.properties` contiene contraseña de correo. Riesgo alto si el repo es público o compartido.
- **JWT en default:** valor por defecto de `jwt.secret` en base; en prod está bien que no haya default. Asegurarse de no desplegar nunca con perfil default en producción.
- **H2 en railway-h2:** si por error se despliega con perfil `railway-h2`, la consola H2 puede quedar deshabilitada por defecto (`H2_CONSOLE_ENABLED:false`), pero la BD es en memoria y los datos se pierden; no es adecuado para producción.

### 6.2 Configuración que puede consumir recursos

- **Pool de conexiones:** en prod está en 20 máximo; en planes con límite de conexiones (p. ej. MySQL gestionado con 10–20 conexiones), puede acercarse al límite si hay varias instancias. Ajustar según oferta del proveedor.
- **Logs:** en prod está controlado; evitar activar DEBUG en producción.
- **Scheduled tasks:** hay dos:
  - `NotificacionLaborScheduler`: cron `0 0 8 * * ?` (diario 08:00).
  - `ConsumoDiarioAutomaticoService`: cron `0 30 0 * * ?` (diario 00:30).  
  Se ejecutan en el mismo proceso que la API. En despliegues con varias réplicas, cada réplica ejecutará el job; si se quiere una única ejecución, habría que externalizar a un worker o usar bloqueo distribuido.

### 6.3 Posibles problemas en Railway / Render / Fly.io

- **Puerto:** si se usa perfil `prod`, la app usa `SERVER_PORT`; estos proveedores suelen dar `PORT`. Resultado: app en 8080 y proxy en otro puerto → 503 o “connection refused”. Solución: usar `server.port=${PORT:8080}` en prod o definir `SERVER_PORT=$PORT` en el comando de inicio.
- **Base de datos:** Render y Fly.io suelen ofrecer PostgreSQL; el proyecto está configurado para MySQL. Railway ofrece MySQL. Si se usa PostgreSQL, haría falta un perfil con `spring.datasource.*` y dialecto PostgreSQL (la dependencia ya está en el pom).
- **Flyway:** está deshabilitado en prod/railway; si la BD ya tiene esquema aplicado a mano, no hay conflicto. Si en el futuro se quiere Flyway en cloud, habilitarlo solo en el perfil de deploy y asegurar que el usuario de BD tenga permisos y que no se dupliquen migraciones.
- **Health check:** el Dockerfile de raíz hace HEALTHCHECK a `localhost:${PORT:-8080}`. En algunas plataformas el health check se ejecuta antes de que la variable `PORT` esté disponible en el contenedor; puede fallar hasta que la app arranque. Tiempo de arranque de Spring Boot puede ser alto; `start-period=120s` en el HEALTHCHECK ayuda.

---

## 7. RECOMENDACIÓN DE DEPLOY

### 7.1 Estrategia recomendada

- **Backend:** desplegar como **contenedor Docker** (o buildpack si el proveedor lo soporta y se prefiere).
  - Ventaja Docker: perfil y variables controlados por el proveedor; misma imagen probada en local/staging.
  - Usar un **Dockerfile único** (p. ej. el de `agrogestion-backend/`) con:
    - `SPRING_PROFILES_ACTIVE` inyectado por entorno (no fijar `testing` ni `railway-h2` en la imagen).
    - Sin credenciales en la imagen.
- **Base de datos:** **separada** y gestionada (Railway MySQL, Render PostgreSQL, PlanetScale, etc.). No depender de un MySQL dentro del mismo compose en producción si el proveedor no lo recomienda.
- **Frontend:** desplegar en Vercel, Netlify, o estático en el mismo proveedor, con la URL del backend configurada por variable de entorno en build o runtime.

### 7.2 Proveedor

- **Railway:** buena opción: soporta MySQL, inyecta `PORT`, despliegue por Docker o buildpack. Ya hay perfiles `railway` y `railway-mysql`.
- **Render:** viable; suele usar `PORT`; oferta típica con PostgreSQL. Si se usa MySQL externo (p. ej. Railway o PlanetScale), Render puede usarse solo para el backend en Docker.
- **Fly.io:** viable; usa `PORT`; buena opción si se prefiere PostgreSQL y se añade un perfil `fly` con URL de PostgreSQL.

### 7.3 Cambios mínimos antes de subir

1. **Puerto en prod:** en `application-prod.properties` cambiar a:
   - `server.port=${PORT:${SERVER_PORT:8080}}`
   - o directamente `server.port=${PORT:8080}` para compatibilidad con Render/Railway/Fly.io.
2. **Quitar credenciales del repo:** eliminar de `application.properties` (y de `application-default.properties` si se usa):
   - `spring.datasource.password`, y opcionalmente username/url, dejando solo placeholders o referencias a variables.
   - `spring.mail.password` (y si aplica username) – usar solo variables de entorno en prod.
3. **Dockerfile de producción:** no fijar perfil `testing` ni `railway-h2`; usar variable de entorno:
   - `ENV SPRING_PROFILES_ACTIVE=prod` como valor por defecto seguro, o no definir y exigir `SPRING_PROFILES_ACTIVE` en el cloud.
4. **Documentar variables obligatorias:** crear un `env.example` o sección en README con la lista de variables (incluida `CORS_ALLOWED_ORIGINS` y `JWT_SECRET`) para que quien despliegue no olvide ninguna.
5. **CORS:** en producción definir siempre `CORS_ALLOWED_ORIGINS` en el proveedor (URL del frontend de producción).

Con estos cambios, la aplicación queda en condiciones de desplegarse en un entorno cloud típico (Railway, Render o Fly.io) con base de datos externa y sin credenciales en el repositorio.

---

*Fin del reporte.*
