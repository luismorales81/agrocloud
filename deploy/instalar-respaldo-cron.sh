#!/usr/bin/env bash
set -euo pipefail

if [[ "$(id -u)" -ne 0 ]]; then
  echo "Ejecutá como root." >&2
  exit 1
fi

ROOT="${AGROGESTION_ROOT:-/opt/agrogestion}"
install -d -m 755 /opt/backups
install -m 755 "$ROOT/deploy/respaldo-mysql.sh" /usr/local/sbin/agrogestion-respaldo-mysql
cat >/etc/cron.daily/agrogestion-mysql <<'EOF'
#!/bin/bash
export AGROGESTION_ROOT=/opt/agrogestion
export AGROGESTION_BACKUPS=/opt/backups
/usr/local/sbin/agrogestion-respaldo-mysql
EOF
chmod 755 /etc/cron.daily/agrogestion-mysql
echo "Cron diario instalado. Probá: /usr/local/sbin/agrogestion-respaldo-mysql"
echo "Luego: $ROOT/deploy/probar-restauracion-respaldo.sh"
