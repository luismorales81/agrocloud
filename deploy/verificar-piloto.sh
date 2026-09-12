#!/usr/bin/env bash
# Comprueba TLS, health público y que Actuator/Swagger no salgan a internet.
# Uso en el VPS (con .env) o: DOMINIO=midominio.com ./deploy/verificar-piloto.sh
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
if [[ -f "$ROOT/.env" ]]; then
  set -a
  # shellcheck disable=SC1091
  source "$ROOT/.env"
  set +a
fi

DOMINIO="${DOMINIO:?Definí DOMINIO en el entorno o en .env}"
PROTOCOLO="${PROTOCOLO:-https}"
BASE="${PROTOCOLO}://${DOMINIO}"
fallos=0

fallar() {
  echo "FALLO: $1" >&2
  fallos=$((fallos + 1))
}

ok() {
  echo "OK: $1"
}

codigo() {
  curl -sS -o /dev/null -w "%{http_code}" --max-time 20 "$@" || echo "000"
}

echo "Piloto Docker/VPS — ${BASE}"
echo

if [[ "$PROTOCOLO" == "https" ]]; then
  http_code="$(codigo -I "${BASE}" || true)"
  if [[ "$http_code" =~ ^(200|301|302|308)$ ]]; then
    ok "HTTPS responde (${http_code})"
  else
    fallar "HTTPS no responde (código ${http_code}). Revisá DNS, Caddy y puertos 80/443."
  fi

  redir="$(codigo -I "http://${DOMINIO}" || true)"
  if [[ "$redir" =~ ^(301|302|308)$ ]]; then
    ok "HTTP redirige a HTTPS (${redir})"
  else
    echo "AVISO: HTTP no redirige (código ${redir}). Let's Encrypt necesita el 80 abierto."
  fi
fi

salud="$(curl -sS --max-time 20 "${BASE}/api/health" || true)"
if echo "$salud" | grep -qiE 'ok|up|status'; then
  ok "/api/health responde"
else
  # algunos health devuelven JSON corto o 200 vacío
  code_salud="$(codigo "${BASE}/api/health")"
  if [[ "$code_salud" == "200" ]]; then
    ok "/api/health HTTP 200"
  else
    fallar "/api/health no está UP (código ${code_salud}, cuerpo: ${salud:0:200})"
  fi
fi

login_code="$(codigo -X POST "${BASE}/api/auth/login" -H "Content-Type: application/json" -d '{}')"
if [[ "$login_code" =~ ^(400|401|403|415|422)$ ]]; then
  ok "POST /api/auth/login alcanza el backend (${login_code})"
elif [[ "$login_code" == "200" ]]; then
  ok "POST /api/auth/login HTTP 200 (revisá si el body vacío debería rechazarse)"
else
  fallar "POST /api/auth/login no llega al backend (código ${login_code})"
fi

for ruta in \
  /actuator/health \
  /actuator/metrics \
  /swagger-ui.html \
  /swagger-ui/index.html \
  /v3/api-docs \
  /api/actuator/health \
  /api/swagger-ui.html \
  /api/v3/api-docs
do
  c="$(codigo "${BASE}${ruta}")"
  if [[ "$c" =~ ^(404|403)$ ]]; then
    ok "Ruta interna oculta ${ruta} (${c})"
  else
    fallar "Ruta ${ruta} no debería ser pública (código ${c})"
  fi
done

echo
if [[ "$fallos" -gt 0 ]]; then
  echo "Piloto: ${fallos} comprobación(es) fallida(s)."
  exit 1
fi
echo "Piloto: todas las comprobaciones automáticas pasaron."
echo "Pendiente manual: login en el navegador y un CRUD de un módulo."
exit 0
