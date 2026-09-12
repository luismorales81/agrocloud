#!/usr/bin/env bash
# Restaura un dump .sql o .sql.gz en el contenedor agrogestion-mysql.
# Detiene el backend para evitar escrituras a medias.
# Tras un dump de Railway: SPRING_FLYWAY_ENABLED=false hasta validar historial.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
ARCHIVO="${1:?Uso: $0 ruta/al/dump.sql.gz}"

if [[ ! -f "$ARCHIVO" ]]; then
  echo "No existe ${ARCHIVO}" >&2
  exit 1
fi

cd "$ROOT"
if [[ -f .env ]]; then
  set -a
  # shellcheck disable=SC1091
  source .env
  set +a
fi

DATABASE_USERNAME="${DATABASE_USERNAME:?}"
DATABASE_PASSWORD="${DATABASE_PASSWORD:?}"
MYSQL_DATABASE="${MYSQL_DATABASE:-agroclouddb}"

echo "Deteniendo backend..."
docker compose -f docker-compose.prod.yml stop backend || true

echo "Restaurando ${ARCHIVO} en ${MYSQL_DATABASE}..."
if [[ "$ARCHIVO" == *.gz ]]; then
  gzip -dc "$ARCHIVO" | docker exec -i agrogestion-mysql \
    mysql -u"$DATABASE_USERNAME" -p"$DATABASE_PASSWORD" "$MYSQL_DATABASE"
else
  docker exec -i agrogestion-mysql \
    mysql -u"$DATABASE_USERNAME" -p"$DATABASE_PASSWORD" "$MYSQL_DATABASE" < "$ARCHIVO"
fi

echo "Restore terminado. Validá Flyway antes de volver a encender migraciones:"
echo "  ./deploy/validar-historial-flyway.sh"
echo "Si el historial coincide con el repo, podés arrancar el backend con SPRING_FLYWAY_ENABLED=false"
echo "hasta el primer release que agregue migraciones nuevas; si la BD nació en Railway a mano,"
echo "dejá Flyway en false o baselineá con un DBA."
echo
echo "Arranque sugerido (Flyway apagado):"
echo "  SPRING_FLYWAY_ENABLED=false docker compose -f docker-compose.prod.yml up -d backend"
