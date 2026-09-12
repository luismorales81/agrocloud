# Cutover Railway/Vercel → Docker en VPS

Los scripts viven en `deploy/`. Este documento es el procedimiento operativo. **No se puede completar el piloto ni el dump de Railway desde el repositorio**: hace falta SSH al VPS y credenciales de MySQL de origen.

## 1. Piloto (BD vacía)

En el VPS, con DNS `A` apuntando al servidor y `.env` creado (`chmod 600`):

```bash
cd /opt/agrogestion
sudo ./deploy/instalar-cortafuegos.sh
chmod +x deploy/*.sh
./deploy/levantar-produccion.sh
```

`levantar-produccion.sh` espera a que el backend esté healthy y corre `verificar-piloto.sh` (HTTPS, `/api/health`, login alcanzable, Actuator/Swagger 404).

Comprobación manual: abrir `https://$DOMINIO`, iniciar sesión y un CRUD de un módulo.

`VITE_API_BASE_URL` debe ser `/api` (ya es el default de `docker-compose.prod.yml`).

## 2. Ensayo de datos (Railway → MySQL del compose)

Desde una máquina que alcance el MySQL público de Railway (o un túnel):

```bash
export ORIGEN_MYSQL_HOST=...
export ORIGEN_MYSQL_PORT=3306
export ORIGEN_MYSQL_USER=...
export ORIGEN_MYSQL_PASSWORD=...
export ORIGEN_MYSQL_DATABASE=...
export DESTINO_DUMP=./database-dumps/railway.sql.gz
./deploy/volcar-origen-mysql.sh
```

En el VPS (piloto ya arriba):

```bash
# Flyway OFF mientras se valida el historial
# En .env: SPRING_FLYWAY_ENABLED=false
./deploy/restaurar-mysql-contenedor.sh /ruta/railway.sql.gz
./deploy/validar-historial-flyway.sh
```

- Código de salida `0`: historial alineado; podés encender Flyway.
- Código `1`: hay versiones de más o de menos; no enciendas Flyway.
- Código `2`: no existe `flyway_schema_history` (esquema a mano). Baseline con un DBA; `SPRING_FLYWAY_ENABLED=false`.

Después:

```bash
SPRING_FLYWAY_ENABLED=false docker compose -f docker-compose.prod.yml up -d backend
./deploy/verificar-piloto.sh
```

## 3. DNS / apagar Vercel y Railway

1. Confirmá piloto + restore + login.
2. El SPA Docker ya no usa fallback a Railway (`api.ts` usa `/api` en producción).
3. `vercel.json` todavía apunta a Railway por si el sitio Vercel sigue en el aire: **pausá o desconectá el proyecto Vercel** cuando el dominio sirva el VPS.
4. Railway: modo lectura / backup final, luego apagar el servicio cuando el VPS lleve 48–72 h estables.

## 4. Operación mínima

```bash
sudo ./deploy/instalar-respaldo-cron.sh
/usr/local/sbin/agrogestion-respaldo-mysql
./deploy/probar-restauracion-respaldo.sh
```

Copiá los `.sql.gz` fuera del VPS. Firewall: solo 22, 80, 443. Caddy bloquea Actuator y Swagger.

## 5. Imágenes en GHCR (sin Maven en el VPS)

El workflow `.github/workflows/publicar-imagenes-docker.yml` publica:

- `ghcr.io/<org>/agrogestion-backend:<sha>` y `:latest`
- `ghcr.io/<org>/agrogestion-frontend:<sha>` y `:latest` con `VITE_API_BASE_URL=/api`

En el `.env` del VPS:

```
IMAGEN_BACKEND=ghcr.io/<org>/agrogestion-backend:latest
IMAGEN_FRONTEND=ghcr.io/<org>/agrogestion-frontend:latest
GHCR_USUARIO=<github-user>
GHCR_TOKEN=<PAT con read:packages>
```

Paquetes privados: en GitHub → Package → package settings → Grant al repo o usá un PAT.

```bash
./deploy/actualizar-desde-registro.sh
```
