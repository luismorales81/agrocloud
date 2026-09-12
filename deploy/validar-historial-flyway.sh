#!/usr/bin/env bash
# Compara flyway_schema_history del MySQL del contenedor con los SQL del repo.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
MIGRACIONES="$ROOT/agrogestion-backend/src/main/resources/db/migration"

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

if [[ ! -d "$MIGRACIONES" ]]; then
  echo "No está el directorio de migraciones: ${MIGRACIONES}" >&2
  exit 1
fi

mapfile -t archivos < <(find "$MIGRACIONES" -maxdepth 1 -type f -name 'V*.sql' | sort)
versiones_repo=()
for f in "${archivos[@]}"; do
  base="$(basename "$f")"
  ver="${base%%__*}"
  ver="${ver#V}"
  versiones_repo+=("$ver")
done

echo "Migraciones en el repo: ${#versiones_repo[@]}"

existe="$(docker exec agrogestion-mysql mysql -N -u"$DATABASE_USERNAME" -p"$DATABASE_PASSWORD" \
  -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='${MYSQL_DATABASE}' AND table_name='flyway_schema_history';" 2>/dev/null || echo 0)"

if [[ "${existe//$'\r'/}" != "1" ]]; then
  echo "No hay tabla flyway_schema_history en ${MYSQL_DATABASE}."
  echo "La BD se migró a mano (típico de Railway) o el restore está incompleto."
  echo "NO actives SPRING_FLYWAY_ENABLED=true hasta baseline/repair con un DBA."
  exit 2
fi

mapfile -t versiones_bd < <(docker exec agrogestion-mysql mysql -N -u"$DATABASE_USERNAME" -p"$DATABASE_PASSWORD" \
  "$MYSQL_DATABASE" -e "SELECT version FROM flyway_schema_history WHERE success=1 ORDER BY installed_rank;" 2>/dev/null)

echo "Versiones aplicadas (success=1): ${#versiones_bd[@]}"

declare -A en_repo
for v in "${versiones_repo[@]}"; do
  en_repo["$v"]=1
done
declare -A en_bd
for v in "${versiones_bd[@]}"; do
  v="${v//$'\r'/}"
  en_bd["$v"]=1
done

solo_repo=()
for v in "${versiones_repo[@]}"; do
  if [[ -z "${en_bd[$v]:-}" ]]; then
    solo_repo+=("$v")
  fi
done
solo_bd=()
for v in "${versiones_bd[@]}"; do
  v="${v//$'\r'/}"
  if [[ -z "${en_repo[$v]:-}" ]]; then
    solo_bd+=("$v")
  fi
done

echo
if [[ ${#solo_repo[@]} -eq 0 && ${#solo_bd[@]} -eq 0 ]]; then
  echo "Historial alineado con el repositorio. Flyway ON es razonable en el próximo arranque."
  exit 0
fi

if [[ ${#solo_repo[@]} -gt 0 ]]; then
  echo "En el repo y no en la BD (Flyway las aplicaría al encender):"
  printf '  %s\n' "${solo_repo[@]}"
fi
if [[ ${#solo_bd[@]} -gt 0 ]]; then
  echo "En la BD y no en el repo (riesgo de validate/checksum):"
  printf '  %s\n' "${solo_bd[@]}"
fi
echo
echo "No actives Flyway hasta resolver diferencias (baseline, repair o migraciones faltantes)."
exit 1
