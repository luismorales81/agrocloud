#!/usr/bin/env bash
# ufw: solo SSH, HTTP y HTTPS. No abrir 3306 ni 8080.
set -euo pipefail

if [[ "$(id -u)" -ne 0 ]]; then
  echo "Ejecutá como root (sudo)." >&2
  exit 1
fi

ufw default deny incoming
ufw default allow outgoing
ufw allow OpenSSH
ufw allow 80/tcp
ufw allow 443/tcp
ufw --force enable
ufw status verbose
echo "Cortafuegos listo. No abras 3306 ni 8080."
