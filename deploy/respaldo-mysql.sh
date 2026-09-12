#!/usr/bin/env bash
# Dump diario del MySQL del stack. Instalar en cron.daily o systemd timer.
set -euo pipefail

ROOT="${AGROGESTION_ROOT:-/opt/agrogestion}"
DESTINO_DIR="${AGROGESTION_BACKUPS:-/opt/backups}"
RETENCION_DIAS="${RETENCION_DIAS:-14}"

if [[ -f "$ROOT/.env" ]]; then
  set -a
  # shellcheck disable=SC1091
  source "$ROOT/.env"
  set +a
fi

DATABASE_USERNAME="${DATABASE_USERNAME:?}"
DATABASE_PASSWORD="${DATABASE_PASSWORD:?}"
MYSQL_DATABASE="${MYSQL_DATABASE:-agroclouddb}"

mkdir -p "$DESTINO_DIR"
STAMP="$(date +%F-%H%M)"
ARCHIVO="${DESTINO_DIR}/agroclouddb-${STAMP}.sql.gz"

docker exec agrogestion-mysql \
  mysqldump -u"$DATABASE_USERNAME" -p"$DATABASE_PASSWORD" \
  --single-transaction --routines --triggers \
  "$MYSQL_DATABASE" | gzip > "$ARCHIVO"

find "$DESTINO_DIR" -name 'agroclouddb-*.sql.gz' -mtime "+${RETENCION_DIAS}" -delete
ls -lh "$ARCHIVO"
echo "Copiá este archivo fuera del VPS (Storage Box u otro disco)."
