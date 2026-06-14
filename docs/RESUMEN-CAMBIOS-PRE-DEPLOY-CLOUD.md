# Resumen de cambios – Preparación deploy cloud

**Rama:** `pre-deploy-cloud` (crear manualmente si no existe: `git checkout -b pre-deploy-cloud`)  
**Objetivo:** Dejar el proyecto listo para Render, Railway o Fly.io sin credenciales en repo y con configuración estándar de cloud.

---

## 1. Backup

- **Carpeta:** `backup/pre-deploy-config/`
- **Contenido:** Copia de la configuración tal como estaba antes de los cambios:
  - `application.properties`, `application-default.properties`, `application-prod.properties`
  - `application-railway.properties`, `application-railway-mysql.properties`, `application-railway-h2.properties`
  - `application-agrocloud.properties`, `application-dev.properties`, `application-mysql.properties`
  - `application-test.properties`, `application-simple.properties`
  - `Dockerfile-raiz`, `Dockerfile-backend`
  - `docker-compose.yml`
  - `README.md` (descripción del backup)

---

## 2. Fase 1 – Limpieza de credenciales

**Estado:** Ya aplicado en el código actual.

- **application.properties:** Base de datos y correo solo con variables de entorno (`${DATABASE_URL}`, `${DATABASE_USERNAME}`, `${DATABASE_PASSWORD}`, `${MAIL_USERNAME}`, `${MAIL_PASSWORD}`). Sin valores por defecto inseguros.
- **application-default.properties:** Misma idea; comentario indicando definir variables en `.env`.
- **application-railway.properties:** `DB_USERNAME` y `DB_PASSWORD` sin valores por defecto.
- **application-railway-h2.properties:** Usa `${DB_USERNAME}` y `${DB_PASSWORD}` (para H2 en memoria definir en entorno, p. ej. `DB_USERNAME=sa`, `DB_PASSWORD=`).
- **application-prod.properties:** Sin cambios; ya usaba solo variables de entorno.

No quedan contraseñas ni usuarios fijos en los properties.

---

## 3. Fase 2 – Puerto compatible con cloud

**Estado:** Ya aplicado.

- **application-prod.properties:** `server.port=${PORT:8080}` (compatible con Render/Railway/Fly.io).
- **application-railway.properties** y **application-railway-mysql.properties:** `server.port=${PORT:8080}`.

Todos los perfiles usables en cloud usan `PORT`.

---

## 4. Fase 3 – Perfil de producción limpio

**Estado:** Ya aplicado.

- **application-prod.properties:**
  - `spring.jpa.hibernate.ddl-auto=validate`
  - `spring.jpa.show-sql=false`
  - Logging en nivel INFO
  - Sin valores por defecto para credenciales; solo variables de entorno.

---

## 5. Fase 4 – Dockerfile profesional

**Cambios realizados:**

- **agrogestion-backend/Dockerfile (producción):**
  - Sigue siendo el único recomendado para producción.
  - No fija `SPRING_PROFILES_ACTIVE`; se define en el entorno.
  - Multi-stage, EXPOSE 8080, HEALTHCHECK a `/actuator/health`, sin credenciales en la imagen.

- **Dockerfile (raíz):**
  - Sigue marcado como **LEGACY**.
  - **Cambio:** `ENTRYPOINT` deja de fijar `-Dspring.profiles.active=railway-h2`. Ahora usa `java ${JAVA_OPTS:-} -jar app.jar`, de modo que el perfil lo define la variable de entorno `SPRING_PROFILES_ACTIVE`.

- **agrogestion-backend/Dockerfile.simple:**
  - Sigue marcado como **LEGACY**.
  - **Cambio:** Se eliminó `ENV SPRING_PROFILES_ACTIVE=railway-mysql` para no fijar perfil en la imagen.

- **Resto de Dockerfiles** (Dockerfile.simple en raíz, Dockerfile.railway-mysql, Dockerfile.simple-app, Dockerfile.debug): ya tenían cabecera LEGACY; sin cambios de contenido.

---

## 6. Fase 5 – .env.example

**Estado:** Ya existía en la raíz y está completo.

- Incluye: `SPRING_PROFILES_ACTIVE`, `PORT`, `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`, `JWT_SECRET`, `CORS_ALLOWED_ORIGINS`, `FRONTEND_URL`, `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_FROM`.
- Sin valores reales; solo plantilla para copiar a `.env` o configurar en el panel del proveedor.

---

## 7. Fase 6 – README Deploy

**Estado:** Ya existía la sección "Deploy en Cloud" en `README.md`.

- Variables necesarias.
- Cómo buildar con Docker (usar `agrogestion-backend/Dockerfile`).
- Cómo correr en local con perfil prod.
- Cómo desplegar en Render y en Railway.
- Mención del backup en `backup/pre-deploy-config/`.

---

## 8. Verificación de compilación

Ejecutar en la máquina local:

```bash
cd agrogestion-backend
mvn clean package -DskipTests
```

Se compila el JAR del backend sin ejecutar tests. Si el build termina en **BUILD SUCCESS**, el proyecto compila correctamente con la configuración actual.

---

## Diff resumido de archivos modificados

| Archivo | Cambio |
|---------|--------|
| `Dockerfile` (raíz) | ENTRYPOINT sin perfil fijo; usa `java ${JAVA_OPTS:-} -jar app.jar` para que el perfil venga de `SPRING_PROFILES_ACTIVE`. |
| `agrogestion-backend/Dockerfile.simple` | Eliminado `ENV SPRING_PROFILES_ACTIVE=railway-mysql`. |
| `backup/pre-deploy-config/*` | Nuevos: copia de todos los application*.properties, Dockerfile-raiz, Dockerfile-backend, docker-compose.yml, README. |

Los `application*.properties` en `agrogestion-backend/src/main/resources/` ya estaban sin credenciales hardcodeadas y con puerto `${PORT:8080}` en perfiles productivos; no se modificaron en esta tanda.

---

## Próximos pasos recomendados

1. Crear la rama (si no existe): `git checkout -b pre-deploy-cloud`
2. Confirmar compilación: `cd agrogestion-backend && mvn clean package -DskipTests`
3. En el proveedor cloud, definir todas las variables listadas en `.env.example` (sobre todo `SPRING_PROFILES_ACTIVE=prod`, `DATABASE_*`, `JWT_SECRET`, `CORS_ALLOWED_ORIGINS`).
4. Desplegar usando **solo** el Dockerfile `agrogestion-backend/Dockerfile` (contexto de build: `agrogestion-backend`).
