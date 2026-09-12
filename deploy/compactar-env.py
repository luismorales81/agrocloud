# Recupera .env si Asegurar-Linea lo partió en un carácter por línea. No imprime valores.
from pathlib import Path
import re

ruta = Path(__file__).resolve().parents[1] / ".env"
if not ruta.exists():
    print("no hay .env")
    raise SystemExit(0)

texto = ruta.read_text(encoding="utf-8", errors="replace")
lineas = texto.splitlines()
promedio = (sum(len(l) for l in lineas[:200]) / max(1, min(200, len(lineas)))) if lineas else 0
if len(lineas) > 400 and promedio < 4:
    crudo = "".join(lineas)
    texto = re.sub(r"(?<!^)([A-Za-z_][A-Za-z0-9_]*=)", r"\n\1", crudo)
    lineas = texto.splitlines()

vistas = {}
orden = []
comentarios_cabeza = []
en_cabeza = True
for linea in lineas:
    s = linea.strip("\ufeff")
    if s and not s.startswith("#") and "=" in s:
        en_cabeza = False
        clave, _, valor = s.partition("=")
        if clave not in vistas:
            orden.append(clave)
            vistas[clave] = valor
        elif not str(vistas[clave]).strip() and valor.strip():
            vistas[clave] = valor
    elif en_cabeza and (s.startswith("#") or s == ""):
        comentarios_cabeza.append(s)

salida = comentarios_cabeza + [f"{k}={vistas[k]}" for k in orden]
ruta.write_text("\n".join(salida).rstrip() + "\n", encoding="utf-8")
print(f"env_compactado lineas={len(salida)} claves={len(orden)} bytes={ruta.stat().st_size}")
