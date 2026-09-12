# Paso a paso: AgroGestion en un VPS Hetzner

Fecha: 2026-08-17

Este documento asume que ya tenés un servidor Cloud de Hetzner (Ubuntu 24.04 o 22.04) y que el proyecto se despliega con `docker-compose.prod.yml` (Caddy + TLS, MySQL no expuesto).

Procedimiento de cutover (piloto, dump Railway, backups, GHCR): [migracion-railway-a-vps-docker.md](migracion-railway-a-vps-docker.md).

**No uses `docker-compose.yml` (el local) en el VPS:** publica MySQL en 3306 y cookies inseguras.

---

## 0. En tu PC, antes de tocar el servidor

1. Anotá la **IPv4** del servidor (consola Hetzner → servidor → Networking).
2. Definí el **dominio** (ej. `agrocloud.com.ar`). Let's Encrypt no emite certificado para una IP suelta.
3. En el DNS del dominio, creá:
   - `A`  `@` → IPv4 del VPS
   - `A`  `www` → misma IPv4
4. En Hetzner: **Firewall** inbound TCP **22, 80, 443**. Nada más (sobre todo no 3306 ni 8080).
5. Si el repo es privado, tené a mano un **deploy key** o un token de clone.
6. Generá secretos **nuevos** para producción (no copies los de desarrollo):

```bash
openssl rand -base64 48
```

Usá una cadena distinta para `JWT_SECRET`, `CHAT_IA_ENCRYPTION_KEY`, `MYSQL_ROOT_PASSWORD` y `DATABASE_PASSWORD`. JWT y cifrado IA: mínimo 32 caracteres.

---

## 1. Primer acceso SSH

Desde Windows (PowerShell):

```powershell
ssh root@TU_IPV4
```

Si Hetzner te dio contraseña la primera vez, cambiala enseguida:

```bash
passwd
```

Mejor: en la consola Hetzner pegá tu clave pública (`~/.ssh/id_ed25519.pub`) y entrá solo con clave.

---

## 2. Actualizar el sistema e instalar Docker

```bash
apt update && apt upgrade -y
apt install -y ca-certificates curl git ufw fail2ban

install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
chmod a644 /etc/apt/keyrings/docker.asc
. /etc/os-release
echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu $VERSION_CODENAME stable" > /etc/apt/sources.list.d/docker.list
apt update
apt install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin
systemctl enable --now docker
docker version
docker compose version
```

---

## 3. Cortafuegos en el propio servidor

El firewall de Hetzner no reemplaza `ufw` del VPS.

```bash
cd /opt/agrogestion
chmod +x deploy/*.sh
sudo ./deploy/instalar-cortafuegos.sh
```

No abras 3306 ni 8080.

---

## 4. Copiar el proyecto al VPS

### Opción A — Git (recomendada)

```bash
mkdir -p /opt
cd /opt
git clone https://github.com/TU_USUARIO/AgroGestion.git agrogestion
cd /opt/agrogestion
```

Si el repo es privado, usá SSH:

```bash
# En el VPS:
ssh-keygen -t ed25519 -f /root/.ssh/github_deploy -N ""
cat /root/.ssh/github_deploy.pub
```

Pegá esa clave pública en GitHub → Settings → Deploy keys (solo lectura). Luego:

```bash
GIT_SSH_COMMAND="ssh -i /root/.ssh/github_deploy" git clone git@github.com:TU_USUARIO/AgroGestion.git /opt/agrogestion
```

**Importante:** tenés que haber hecho commit y push de Dockerfiles, `docker-compose.prod.yml` y `deploy/Caddyfile` desde tu PC.

### Opción B — Copiar desde tu PC (si aún no está en GitHub)

En PowerShell, desde la carpeta del proyecto:

```powershell
scp -r . root@TU_IPV4:/opt/agrogestion
```

No subas `.env`, `node_modules`, `target` ni dumps de BD.

---

## 5. Crear el `.env` de producción **en el servidor**

```bash
cd /opt/agrogestion
cp .env.example .env
nano .env
```

Valores típicos (reemplazá dominio y secretos):

```
SPRING_PROFILES_ACTIVE=prod
TZ=America/Argentina/Buenos_Aires
MYSQL_DATABASE=agroclouddb
MYSQL_ROOT_PASSWORD=...generado...
DATABASE_USERNAME=agrocloud
DATABASE_PASSWORD=...generado...
JWT_SECRET=...generado min 32 chars...
CHAT_IA_ENCRYPTION_KEY=...generado min 32 chars...
DOMINIO=agrocloud.com.ar
CADDY_EMAIL=admin@agrocloud.com.ar
FRONTEND_URL=https://agrocloud.com.ar
CORS_ALLOWED_ORIGINS=https://agrocloud.com.ar,https://www.agrocloud.com.ar
VITE_API_BASE_URL=/api
VITE_GOOGLE_MAPS_API_KEY=...si usás mapas...
```

Permisos:

```bash
chmod 600 .env
```

El `.env` **no** se commitea. Si el DNS todavía no apunta al VPS, Caddy no podrá sacar el certificado: esperá a que el `A` resuelva (podés comprobar con `ping agrocloud.com.ar` desde tu PC).

---

## 6. Levantar el sistema

La primera vez tarda: Maven + npm + Flyway sobre MySQL vacío.

```bash
cd /opt/agrogestion
chmod +x deploy/*.sh
./deploy/levantar-produccion.sh
```

Equivalente manual:

```bash
cd /opt/agrogestion
docker compose -f docker-compose.prod.yml up --build -d
docker compose -f docker-compose.prod.yml ps
docker compose -f docker-compose.prod.yml logs -f backend
```

Cuando Actuator interno esté UP, Ctrl+C (los contenedores siguen). `levantar-produccion.sh` ya espera healthy y corre `deploy/verificar-piloto.sh`.

```bash
curl -I https://TU_DOMINIO
curl -s https://TU_DOMINIO/api/health
./deploy/verificar-piloto.sh
```

En el navegador: `https://TU_DOMINIO`.

Si Caddy falla por TLS: el dominio no apunta a esta IP, el firewall bloquea 80, o `DOMINIO` / `CADDY_EMAIL` están vacíos.

```bash
docker compose -f docker-compose.prod.yml logs caddy
```

---

## 7. Después del primer arranque

1. Creá el usuario administrador **dentro de la app** (no hay usuario mágico en Docker).
2. En Google Cloud Console, restringí la clave de Maps a `https://TU_DOMINIO/*`.
3. Completá SMTP en `.env` si querés reset de contraseña; luego `docker compose -f docker-compose.prod.yml up -d backend`.
4. Swagger y Actuator no se publican: Caddy responde 404 (ver `deploy/Caddyfile`).

---

## 8. Actualizar el código más adelante

Con build en el VPS:

```bash
cd /opt/agrogestion
git pull
docker compose -f docker-compose.prod.yml up --build -d
```

Con imágenes de GHCR (recomendado): `./deploy/actualizar-desde-registro.sh` (ver [migracion-railway-a-vps-docker.md](migracion-railway-a-vps-docker.md)).

Si cambiaste `VITE_API_BASE_URL` o la clave de Maps y compilás en el VPS:

```bash
docker compose -f docker-compose.prod.yml build frontend --no-cache
docker compose -f docker-compose.prod.yml up -d frontend caddy
```

---

## 9. Backup de MySQL

```bash
sudo ./deploy/instalar-respaldo-cron.sh
/usr/local/sbin/agrogestion-respaldo-mysql
./deploy/probar-restauracion-respaldo.sh
```

Guardá copias fuera del VPS (Storage Box de Hetzner o tu PC).

---

## 10. Comandos útiles

| Acción | Comando |
|--------|---------|
| Estado | `docker compose -f docker-compose.prod.yml ps` |
| Logs backend | `docker compose -f docker-compose.prod.yml logs -f backend` |
| Logs Caddy | `docker compose -f docker-compose.prod.yml logs -f caddy` |
| Parar | `docker compose -f docker-compose.prod.yml down` |
| Parar y borrar datos | `docker compose -f docker-compose.prod.yml down -v` (irreversible) |

---

## Qué no hacer

- No uses el `docker-compose.yml` local en el VPS.
- No abras 3306/8080 en Hetzner Firewall ni en `ufw`.
- No subas `.env` a GitHub.
- No actives Flyway contra una BD ya migrada a mano (Railway) sin revisar el historial (`./deploy/validar-historial-flyway.sh`). En un VPS **nuevo** (BD vacía) `SPRING_FLYWAY_ENABLED=true` es correcto.
- GitHub Actions publica imágenes en GHCR (`.github/workflows/publicar-imagenes-docker.yml`). En el VPS preferí `compose pull` antes que recompilar Maven.
