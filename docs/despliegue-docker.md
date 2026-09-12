# Despliegue Docker — AgroGestion

Fecha: 2026-08-17

Este documento describe cómo ejecutar el sistema completo con Docker en un entorno local (equivalente de producción). **No incluye GitHub Actions ni configuración de Hetzner**; eso se detalla al final como trabajo posterior.

## Archivos modificados o creados

| Archivo | Acción |
|---------|--------|
| `agrogestion-backend/Dockerfile` | Reescrito: multi-stage, usuario no root, TZ, healthcheck Actuator, sin secretos |
| `agrogestion-backend/.dockerignore` | Actualizado (excluye tests, `bin/`, `.env`, EULA locales) |
| `agrogestion-frontend/Dockerfile` | **Nuevo**: Node 22 + nginx 1.27, SPA estático |
| `agrogestion-frontend/nginx.conf` | **Nuevo**: fallback SPA, caché de assets, cabeceras de seguridad |
| `agrogestion-frontend/.dockerignore` | **Nuevo** |
| `docker-compose.yml` | Reescrito: MySQL + backend + frontend desde cero |
| `.env.example` | Ampliado para Compose (sin secretos reales) |
| `agrogestion-backend/src/main/resources/application-prod.properties` | Solo compatibilidad: Flyway/cookies/mail/EULA/shutdown por variables |
| `agrogestion-backend/src/main/resources/application.properties` | `app.eula.storage.path` por variable de entorno |
| `Dockerfile` (raíz) | Comentario: legado; no usar |
| `docs/despliegue-docker.md` | **Nuevo** (este archivo) |

No se cambió la arquitectura funcional ni se agregaron features de negocio.

---

## Problemas detectados (antes y durante el análisis)

### Críticos para levantar producción / Docker

1. **Flyway desactivado en perfil `prod`**  
   `application-prod.properties` tenía `spring.flyway.enabled=false` y `ddl-auto=validate`. Una base vacía no arranca: Hibernate valida un esquema que no existe.  
   **Mitigación actual:** Compose define `SPRING_FLYWAY_ENABLED=true`. El default del perfil sigue en `false` para no alterar Railway/Render actuales.

2. **`schema-mysql.sql` en el compose anterior**  
   El compose viejo montaba ese SQL en `docker-entrypoint-initdb.d`. El script está desactualizado respecto de ~170 migraciones Flyway y chocaría con ellas. **Ya no se usa.**

3. **Cookies JWT incompatibles con HTTP local**  
   En `prod`: `Secure=true` y `SameSite=None`. El navegador **no guarda** esa cookie en `http://localhost`.  
   **Mitigación actual:** Compose fuerza `JWT_COOKIE_SECURE=false` y `JWT_COOKIE_SAME_SITE=Lax`. En el VPS con HTTPS hay que volver a `true` / `None` (o `Lax` si frontend y API van por el mismo origen).

4. **Compose anterior incompleto y con secretos**  
   Solo MySQL + phpMyAdmin, contraseñas en el YAML (`password`, `agrocloud123`). No levantaba backend ni frontend.

5. **`MAIL_HOST` sin default en `prod`**  
   `${MAIL_HOST}` sin valor hacía fallar el arranque. Ahora admite vacío.

6. **Validación de secretos en `prod`**  
   `ValidacionSecretosArranque` exige `JWT_SECRET`, `DATABASE_PASSWORD` y `CHAT_IA_ENCRYPTION_KEY`. Compose no arranca si el `.env` está vacío (es intencional).

7. **URL de API del frontend se hornea en el build**  
   Vite embebe `VITE_API_BASE_URL` en el JavaScript. Cambiar la URL del backend exige **reconstruir** la imagen frontend. Además `api.ts` tiene un fallback hardcodeado a Railway si no hay variable y el hostname parece producción; Compose siempre pasa el ARG, así que en Docker no aplica.

### Importantes (no bloquean Compose si se configuran bien)

8. **CORS se lee de `CORS_ALLOWED_ORIGINS` (variable de entorno OS), no de `cors.allowed.origins` de Spring.** Sin esa variable, en `prod` cae a `https://agrocloud.com.ar`. Compose la setea a `http://localhost:3000`. No usar espacios tras las comas.

9. **No existe `application-dev.properties` en `src/`** (solo en backup). El perfil `dev` de `application.properties` no aporta datasource. El arranque local sin Docker sigue dependiendo de variables o del IDE.

10. **Zona horaria**  
    Hibernate en `prod` usa `jdbc.time_zone=UTC` (correcto para persistencia). El contenedor puede usar `TZ=America/Argentina/Buenos_Aires` para logs y `LocalDateTime.now()`.

11. **Uploads / EULA**  
    Los PDF del EULA se escriben en disco (`app.eula.storage.path`). Sin volumen se pierden al recrear el contenedor. Compose monta `eula_pdfs`. No hay almacenamiento de uploads de usuario más allá de multipart en memoria/temp.

12. **Health**  
    - `/api/health`: liveness simple (no mira la BD).  
    - `/actuator/health`: incluye datasource; es el que usa Docker.  
    Arranque lento la primera vez (Flyway). `start_period` del backend: 180 s.

13. **Logs**  
    No hay `logging.file`; salen a stdout (adecuado para Docker). No se copian `.env` ni secretos a la imagen.

14. **Swagger / Actuator**  
    Swagger sigue expuesto en `prod`. Actuator deja `health`, `info` y `metrics` públicos o semi-públicos (`metrics` exige rol salvo lo permitido). Conviene cerrarlo detrás del proxy en el VPS.

15. **Imágenes raíz (`Dockerfile`, `Dockerfile.simple`, etc.)**  
    Legado Railway/H2. No usarlas. phpMyAdmin se eliminó del compose (superficie de ataque innecesaria).

16. **Claves `VITE_*`**  
    Van al bundle del navegador por diseño. No son secretos de servidor, pero la imagen frontend las contiene. Restringir la de Maps por HTTP referrer. Nunca poner `JWT_SECRET` ni passwords en el frontend.

---

## Cómo ejecutar el proyecto completo con Docker

Requisitos: Docker Engine + Docker Compose v2, y un archivo `.env` en la raíz (no versionado).

```bash
cd /ruta/AgroGestion
cp .env.example .env
```

Completar **obligatorios** (mínimo 32 caracteres en JWT y clave de cifrado IA):

- `MYSQL_ROOT_PASSWORD`
- `DATABASE_USERNAME`
- `DATABASE_PASSWORD`
- `JWT_SECRET`
- `CHAT_IA_ENCRYPTION_KEY`

Luego:

```bash
powershell -File deploy/levantar-local.ps1
```

o:

```bash
docker compose up --build
```

URLs locales (puertos por defecto del compose; 3306/3000 suelen estar ocupados en Windows):

| Servicio | URL |
|----------|-----|
| Frontend | http://localhost:3001 |
| Backend API | http://localhost:8080 |
| Health | http://localhost:8080/api/health |
| Actuator | http://localhost:8080/actuator/health |
| MySQL (contenedor) | localhost:3307 |

La primera vez Flyway aplica todas las migraciones sobre MySQL vacío. El frontend espera a que el backend esté healthy.

Comandos útiles:

```bash
docker compose logs -f backend
docker compose ps
docker compose down          # conserva volúmenes (datos MySQL y EULA)
docker compose down -v       # borra datos (vuelve a cero)
```

Tras cambiar `VITE_API_BASE_URL` o `VITE_GOOGLE_MAPS_API_KEY`:

```bash
docker compose build frontend --no-cache
docker compose up -d frontend
```

### Construir imágenes por separado (sin Compose)

```bash
docker build -t agrogestion-backend:local ./agrogestion-backend

docker build -t agrogestion-frontend:local ./agrogestion-frontend \
  --build-arg VITE_API_BASE_URL=http://localhost:8080/api \
  --build-arg VITE_GOOGLE_MAPS_API_KEY=
```

No pasar secretos como `ARG` del backend. El backend solo recibe secretos en `docker run` / Compose `environment`.

---

## Variables de entorno

### Backend (runtime, nunca en la imagen)

| Variable | Obligatoria (prod) | Uso |
|----------|--------------------|-----|
| `SPRING_PROFILES_ACTIVE` | sí (`prod`) | Perfil |
| `PORT` | no (default 8080) | Puerto HTTP |
| `DATABASE_URL` | sí en host; Compose la arma sola | JDBC MySQL |
| `DATABASE_USERNAME` | sí | Usuario BD |
| `DATABASE_PASSWORD` | sí | Password BD |
| `MYSQL_ROOT_PASSWORD` | sí (solo Compose/MySQL) | Root del contenedor MySQL |
| `MYSQL_DATABASE` | no (`agroclouddb`) | Nombre de la BD |
| `JWT_SECRET` | sí (≥ 32 chars) | Firma HS256 |
| `CHAT_IA_ENCRYPTION_KEY` | sí (≥ 32 chars) | Cifrado BYOK Gemini |
| `CORS_ALLOWED_ORIGINS` | sí en prod | Orígenes del SPA |
| `FRONTEND_URL` | recomendada | Links de email |
| `SPRING_FLYWAY_ENABLED` | `true` en Compose / VPS nuevo | Migraciones al arrancar |
| `JWT_COOKIE_SECURE` | `false` en HTTP local; `true` en HTTPS | Cookie JWT |
| `JWT_COOKIE_SAME_SITE` | `Lax` local; `None` o `Lax` en prod | Cookie JWT |
| `EULA_STORAGE_PATH` | no | Directorio PDF EULA |
| `TZ` | no | Timezone del proceso |
| `MAIL_HOST` / `MAIL_*` | no | SMTP |
| `IA_GEMINI_API_KEY` | no | Chat IA de proyecto |
| `OPENWEATHER_API_KEY` | no | Clima |
| `JAVA_OPTS` | no | Memoria JVM |
| `FORWARD_HEADERS_STRATEGY` | no (`framework`) | Proxy inverso (X-Forwarded-*) |
| `MANAGEMENT_HEALTH_MAIL_ENABLED` | no (`false`) | Evita health DOWN sin SMTP |

### Frontend (build-time)

| Variable | Uso |
|----------|-----|
| `VITE_API_BASE_URL` | URL que el navegador usa para la API (ej. `http://localhost:8080/api`) |
| `VITE_GOOGLE_MAPS_API_KEY` | Maps en el browser (restringir por referrer) |

---

## Cambios posteriores para CI/CD y producción (VPS / Hetzner)

Implementado en el repo (operación en el servidor sigue siendo manual):

1. **GitHub Actions** — `.github/workflows/publicar-imagenes-docker.yml`: build/push GHCR (`:sha` y `:latest`), `VITE_API_BASE_URL=/api`, Trivy (CVE críticas).
2. **VPS** — scripts en `deploy/`, Caddy oculta Actuator/Swagger, Flyway configurable, backups con restore de prueba. Guía: [migracion-railway-a-vps-docker.md](migracion-railway-a-vps-docker.md) y [despliegue-hetzner.md](despliegue-hetzner.md).
3. **SPA** — sin fallback a Railway; producción Docker usa `/api` (mismo origen).

Deuda pendiente (no bloquea el VPS): config runtime del frontend, unificar CORS Spring, `application-dev.properties` para `iniciar-proyecto.bat`.

---

## Estrategia de imágenes

```
[backend]  Maven 17  →  JRE Alpine 17  (JAR, usuario agrocloud, puerto 8080)
[frontend] Node 22   →  nginx 1.27     (solo dist/, puerto 80)
[mysql]    mysql:8.0                   (utf8mb4, UTC)
```

Las tres piezas son independientes. Compose las une en una red bridge. En el VPS se pueden publicar las dos imágenes de aplicación y usar MySQL gestionado o el mismo contenedor con volumen.
