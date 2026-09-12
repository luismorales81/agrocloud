#!/usr/bin/env bash
# Dump de un MySQL remoto (p. ej. Railway) hacia un archivo local. No imprime la contraseña.
# Variables: ORIGEN_MYSQL_HOST PORT USER PASSWORD DATABASE
set -euo pipefail

ORIGEN_MYSQL_HOST="${ORIGEN_MYSQL_HOST:?}"
ORIGEN_MYSQL_PORT="${ORIGEN_MYSQL_PORT:-3306}"
ORIGEN_MYSQL_USER="${ORIGEN_MYSQL_USER:?}"
ORIGEN_MYSQL_PASSWORD="${ORIGEN_MYSQL_PASSWORD:?}"
ORIGEN_MYSQL_DATABASE="${ORIGEN_MYSQL_DATABASE:-agroclouddb}"

STAMP="$(date +%Y%m%d-%H%M%S)"
DESTINO="${DESTINO_DUMP:-./database-dumps/origen-${ORIGEN_MYSQL_DATABASE}-${STAMP}.sql.gz}"
mkdir -p "$(dirname "$DESTINO")"

echo "Volcando ${ORIGEN_MYSQL_DATABASE} @ ${ORIGEN_MYSQL_HOST}:${ORIGEN_MYSQL_PORT} → ${DESTINO}"

docker run --rm mysql:8.0 \
  mysqldump \
  --host="$ORIGEN_MYSQL_HOST" \
  --port="$ORIGEN_MYSQL_PORT" \
  --user="$ORIGEN_MYSQL_USER" \
  --password="$ORIGEN_MYSQL_PASSWORD" \
  --single-transaction \
  --routines \
  --triggers \
  --set-gtid-purged=OFF \
  "$ORIGEN_MYSQL_DATABASE" \
  | gzip > "$DESTINO"

ls -lh "$DESTINO"
echo "Listo. Revisá que el gzip no esté vacío y guardá una copia fuera de este disco."
