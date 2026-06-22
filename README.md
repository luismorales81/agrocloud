# AgroGestion / AgroCloud

Backend Spring Boot + Frontend React (AgroGestion).

---

## Deploy en Cloud

El backend está preparado para desplegarse en **Render**, **Railway** o **Fly.io**.

### Variables necesarias

Configurar en el panel del proveedor (o en un archivo `.env` local, sin subirlo al repo). Ver `.env.example` en la raíz.

| Variable | Descripción |
|----------|-------------|
| `SPRING_PROFILES_ACTIVE` | Perfil de Spring. En cloud usar `prod`. |
| `PORT` | Puerto HTTP (suele inyectarlo el proveedor). |
| `DATABASE_URL` | URL JDBC de la base de datos. |
| `DATABASE_USERNAME` | Usuario de la BD. |
| `DATABASE_PASSWORD` | Contraseña de la BD. |
| `JWT_SECRET` | Clave para firmar tokens JWT. |
| `CORS_ALLOWED_ORIGINS` | Orígenes permitidos (URL del frontend). |
| `FRONTEND_URL` | URL pública del frontend. |
| `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_FROM` | Configuración SMTP. |

### Cómo buildar con Docker

**Dockerfile de producción:** usar únicamente `agrogestion-backend/Dockerfile`. Los demás Dockerfiles en el repo están marcados como legacy (no fijan perfil ni credenciales en la imagen).

Desde la carpeta del backend:

```bash
cd agrogestion-backend
docker build -t agrocloud-backend .
```

El perfil activo se define con la variable de entorno `SPRING_PROFILES_ACTIVE` (no va fijado en la imagen). Ejemplo para ejecutar en local con perfil prod:

```bash
docker run --rm -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DATABASE_URL=jdbc:mysql://host:3306/agrocloud \
  -e DATABASE_USERNAME=usuario \
  -e DATABASE_PASSWORD=contraseña \
  -e JWT_SECRET=tu-clave-secreta \
  -e CORS_ALLOWED_ORIGINS=http://localhost:3000 \
  agrocloud-backend
```

### Cómo correr en local con perfil prod

```bash
cd agrogestion-backend
export SPRING_PROFILES_ACTIVE=prod
export DATABASE_URL=jdbc:mysql://localhost:3306/agrocloud
export DATABASE_USERNAME=root
export DATABASE_PASSWORD=tu_password
export JWT_SECRET=tu_clave_secreta
export CORS_ALLOWED_ORIGINS=http://localhost:3000
export FRONTEND_URL=http://localhost:3000
mvn spring-boot:run
```

O usar un archivo `.env` (no subirlo al repo) y cargarlo con `set -a && source .env && set +a` (bash) antes de `mvn spring-boot:run`.

### Cómo desplegar en Render

1. Conectar el repositorio a Render.
2. Tipo de servicio: **Web Service**.
3. Build: **Docker**; ruta del Dockerfile: `agrogestion-backend/Dockerfile` (o configurar root de build en `agrogestion-backend`).
4. Definir todas las variables de entorno desde el panel (igual que en `.env.example`).
5. Añadir base de datos MySQL (Render ofrece PostgreSQL; si usas MySQL externo, configurar `DATABASE_URL` y credenciales).

### Cómo desplegar en Railway

1. Conectar el repo en Railway.
2. Añadir servicio desde **Dockerfile**; ruta: `agrogestion-backend/Dockerfile` (contexto: `agrogestion-backend`).
3. Añadir recurso MySQL si procede; Railway inyecta variables tipo `DATABASE_URL` (o `MYSQL_URL`). Ajustar nombre de variables si el perfil usa `SPRING_DATASOURCE_*` (perfil `railway-mysql`).
4. Definir `SPRING_PROFILES_ACTIVE=prod` (o `railway-mysql`) y el resto de variables en la pestaña Variables.

### Backup de configuración

Antes del deploy se hizo una copia de la configuración anterior en `backup/pre-deploy-config/` (application*.properties, Dockerfiles, docker-compose.yml).
