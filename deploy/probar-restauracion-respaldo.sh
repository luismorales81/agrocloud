#!/usr/bin/env bash
# Restaura el último dump en un contenedor MySQL temporal y comprueba que abre.
# No toca agrogestion-mysql de producción.
set -euo pipefail

DUMP="${1:-}"
DESTINO_DIR="${AGROGESTION_BACKUPS:-/opt/backups}"

if [[ -z "$DUMP" ]]; then
  DUMP="$(ls -1t "$DESTINO_DIR"/agroclouddb-*.sql.gz 2>/dev/null | head -1 || true)"
fi
if [[ -z "$DUMP" || ! -f "$DUMP" ]]; then
  echo "No hay dump. Pasá la ruta: $0 /opt/backups/agroclouddb-....sql.gz" >&2
  exit 1
fi

NOMBRE="agrogestion-mysql-prueba-restore"
CLAVE="prueba-restore-local"
docker rm -f "$NOMBRE" >/dev/null 2>&1 || true
docker run -d --name "$NOMBRE" \
  -e MYSQL_ROOT_PASSWORD="$CLAVE" \
  -e MYSQL_DATABASE=agroclouddb \
  mysql:8.0 >/dev/null

echo "Esperando MySQL temporal..."
for i in $(seq 1 40); do
  if docker exec "$NOMBRE" mysqladmin ping -h 127.0.0.1 -uroot -p"$CLAVE" --silent 2>/dev/null; then
    break
  fi
  sleep 3
done

echo "Restaurando ${DUMP}..."
gzip -dc "$DUMP" | docker exec -i "$NOMBRE" mysql -uroot -p"$CLAVE"

TABLAS="$(docker exec "$NOMBRE" mysql -N -uroot -p"$CLAVE" -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='agroclouddb';")"
echo "Tablas en agroclouddb: ${TABLAS}"
docker rm -f "$NOMBRE" >/dev/null
if [[ "${TABLAS//$'\r'/}" -lt 1 ]]; then
  echo "Restore de prueba FALLÓ (0 tablas)." >&2
  exit 1
fi
echo "Restore de prueba OK. El dump es usable."
