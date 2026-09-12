#!/usr/bin/env bash
# En el VPS: pull de imágenes GHCR y recreate sin recompilar Maven/Node.
# Requiere IMAGEN_BACKEND e IMAGEN_FRONTEND en .env y docker login a ghcr.io.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

if [[ ! -f .env ]]; then
  echo "Falta .env" >&2
  exit 1
fi
set -a
# shellcheck disable=SC1091
source .env
set +a

if [[ -z "${IMAGEN_BACKEND:-}" || -z "${IMAGEN_FRONTEND:-}" ]]; then
  echo "Definí IMAGEN_BACKEND e IMAGEN_FRONTEND (etiquetas ghcr.io/...). " >&2
  echo "Si todavía compilás en el VPS, usá ./deploy/levantar-produccion.sh" >&2
  exit 1
fi

if [[ -n "${GHCR_TOKEN:-}" && -n "${GHCR_USUARIO:-}" ]]; then
  echo "$GHCR_TOKEN" | docker login ghcr.io -u "$GHCR_USUARIO" --password-stdin
fi

docker compose -f docker-compose.prod.yml pull backend frontend
docker compose -f docker-compose.prod.yml up -d --no-build
docker compose -f docker-compose.prod.yml ps
"$ROOT/deploy/verificar-piloto.sh"
