#!/usr/bin/env bash
# Primer arranque o rebuild en el VPS (BD vacía: Flyway ON).
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

if [[ ! -f .env ]]; then
  echo "Falta .env. Copiá .env.example, completá secretos y chmod 600 .env" >&2
  exit 1
fi

chmod 600 .env || true

set -a
# shellcheck disable=SC1091
source .env
set +a

faltantes=()
for v in MYSQL_ROOT_PASSWORD DATABASE_USERNAME DATABASE_PASSWORD JWT_SECRET CHAT_IA_ENCRYPTION_KEY DOMINIO CADDY_EMAIL CORS_ALLOWED_ORIGINS FRONTEND_URL; do
  if [[ -z "${!v:-}" ]]; then
    faltantes+=("$v")
  fi
done
if [[ ${#faltantes[@]} -gt 0 ]]; then
  echo "Faltan variables en .env: ${faltantes[*]}" >&2
  exit 1
fi

export SPRING_FLYWAY_ENABLED="${SPRING_FLYWAY_ENABLED:-true}"
export VITE_API_BASE_URL="${VITE_API_BASE_URL:-/api}"

echo "Levantando stack de producción (build local si no hay IMAGEN_* de GHCR)..."
docker compose -f docker-compose.prod.yml up --build -d
docker compose -f docker-compose.prod.yml ps

echo "Esperando health del backend (hasta 4 minutos)..."
ok=0
for i in $(seq 1 48); do
  if docker inspect --format='{{.State.Health.Status}}' agrogestion-backend 2>/dev/null | grep -q healthy; then
    ok=1
    break
  fi
  sleep 5
done
if [[ "$ok" -ne 1 ]]; then
  echo "El backend no quedó healthy. Logs:" >&2
  docker compose -f docker-compose.prod.yml logs --tail=80 backend
  exit 1
fi

"$ROOT/deploy/verificar-piloto.sh"
