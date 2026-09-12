# Docker en el PC, despues al VPS

El compose de **esta maquina** es `docker-compose.yml` (no `docker-compose.prod.yml`).
Caddy y TLS solo van en el VPS.

## Por que no un solo contenedor

Tres imagenes (MySQL, Spring, nginx) se construyen aca y se pueden copiar o republicar.
En el VPS se agregan Caddy y otro `.env`.

## Levantar en Docker Desktop

En PowerShell, desde la raiz del repo:

```powershell
powershell -File deploy/levantar-local.ps1
```

El script completa secretos vacios en `.env` (no lo subas a git) y hace `docker compose up --build -d`.

Puertos por defecto (evitan choque con MySQL/npm del Windows):

| Servicio | URL |
|----------|-----|
| Frontend | http://localhost:3001 |
| Backend | http://localhost:8080 |
| MySQL del contenedor | localhost:3307 |

Usuario de prueba (solo compose local, BD vacía): `admin@localhost` / `AgrocloudLocal1`.
El navegador habla con la API en el **mismo origen** (`http://localhost:3001/api`), nginx la reenvía al backend. El puerto 8080 sigue abierto para depurar.
Si `.env` se infló (un carácter por línea), `levantar-local.ps1` corre `deploy/compactar-env.ps1`. También podés ejecutarlo a mano. Un `.env` enorme puede desincronizar contraseñas con el volumen de MySQL.
En el VPS usá `docker-compose.prod.yml`: no pasa `APP_CREAR_USUARIO_LOCAL`, así no se crea ese admin.

El MySQL nativo del PC (3306) no se toca. El frontend que escuche en 3000 tampoco.

## Llevar al VPS

1. Probar login y un CRUD en http://localhost:3001.
2. Opcion A — reconstruir en el servidor: `git pull` y `./deploy/levantar-produccion.sh` (Maven/Node en el VPS).
3. Opcion B — `deploy/exportar-imagenes-local.ps1` y `docker load` en el VPS (el frontend local ya lleva `/api`).
4. Opcion C (mejor a medio plazo): workflow GHCR y `./deploy/actualizar-desde-registro.sh`.

Datos: el volumen `mysql_datos` de Docker Desktop no viaja con `docker save`. Para datos reales, dump/restore (`deploy/volcar-origen-mysql.sh`).
