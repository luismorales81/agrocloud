# -*- coding: utf-8 -*-
"""Genera las 10 corridas de CSV de manga (1.csv a 5.csv)."""
from pathlib import Path

RAIZ = Path(__file__).resolve().parent

EID = [f"982 00012345670{i}" for i in range(1, 9)]
VID = [f"AG-MANGA-0{i}" for i in range(1, 9)]
EID9 = "982 000123456709"
VID9 = "AG-MANGA-09"
INTRUSO = "982 000999111222"

PESO1 = [286.5, 274.0, 301.2, 268.8, 292.4, 279.1, 310.0, 255.6]
PESO2 = [297.5, 285.0, 312.2, 279.8, 303.4, 290.1, 321.0, 266.6]


def linea_fabrica(i, fecha, hora, peso=None):
    base = f"{EID[i]},{VID[i]}"
    if peso is None:
        return f"{base},{fecha},{hora}"
    return f"{base},{peso},{fecha},{hora}"


def ident(i, fecha, hora, extra=""):
    mid = f",{extra}" if extra else ""
    return f"{EID[i]},{VID[i]}{mid},{fecha},{hora}"


def escribir(carpeta, nombre, encabezado, filas):
    dest = RAIZ / carpeta
    dest.mkdir(parents=True, exist_ok=True)
    (dest / nombre).write_text(encabezado + "\n" + "\n".join(filas) + "\n", encoding="utf-8")


def ocho_fabrica(fecha, hora, pesos=None):
    if pesos is None:
        return [linea_fabrica(i, fecha, hora) for i in range(8)]
    return [linea_fabrica(i, fecha, hora, pesos[i]) for i in range(8)]


def ocho_ident(fecha, hora, extras=None):
    filas = []
    for i in range(8):
        extra = extras[i] if extras else ""
        filas.append(ident(i, fecha, hora, extra))
    return filas


# --- 01 fabrica ---
c = "corrida-01-fabrica"
escribir(c, "1-enrolar.csv", "EID,VID,Date,Time", [
    linea_fabrica(i, "12/09/2026", f"08:14:{11+i:02d}") for i in range(8)
])
escribir(c, "2-pesar.csv", "EID,VID,Weight,Date,Time", [
    linea_fabrica(i, "12/09/2026", f"09:02:{11+i:02d}", PESO1[i]) for i in range(8)
])
escribir(c, "3-sanidad.csv", "EID,VID,Date,Time", [
    linea_fabrica(i, "12/09/2026", f"10:21:{11+i:02d}") for i in range(8)
])
escribir(c, "4-contar.csv", "EID", EID[:6] + [INTRUSO])
escribir(c, "5-baja.csv", "EID,VID,Date,Time", [
    linea_fabrica(6, "12/09/2026", "16:40:02"),
    linea_fabrica(7, "12/09/2026", "16:40:19"),
])
(RAIZ / c / "README.md").write_text(
    "# Corrida 01 — Fabrica Data Link\n\nPerfil: ninguno.\n\n"
    "1 Enrolar · 2 Pesar · 3 Aplicar · 4 Contar (faltan 07-08, intruso) · 5 Baja 07-08\n",
    encoding="utf-8",
)

# --- 02 identificador ---
c = "corrida-02-identificador"
h = "Identificador,TagVisual,Dia,Instante"
escribir(c, "1-enrolar.csv", h, [ident(i, "16/09/2026", f"07:40:{11+i:02d}") for i in range(8)])
escribir(c, "2-sanidad.csv", "Identificador,TagVisual,Nota,Dia,Instante",
         [ident(i, "16/09/2026", f"10:21:{11+i:02d}", "ok") for i in range(8)])
escribir(c, "3-contar.csv", h, [ident(i, "18/09/2026", f"09:00:{11+i:02d}") for i in range(6)])
escribir(c, "4-baja.csv", h, [ident(6, "18/09/2026", "16:40:02"), ident(7, "18/09/2026", "16:40:19")])
escribir(c, "5-enrolar-09.csv", h, [f"{EID9},{VID9},01/11/2026,09:15:02"])
(RAIZ / c / "README.md").write_text(
    "# Corrida 02 — Identificador bastón\n\nPerfil: Identificador bastón.\n\n"
    "1 Enrolar · 2 Aplicar (Nota se ignora) · 3 Contar faltan 07-08 · 4 Baja · 5 Enrolar 09\n",
    encoding="utf-8",
)

# --- 03 kilos ---
c = "corrida-03-kilos"
hk = "Identificador,TagVisual,Kilos,Dia,Instante"
escribir(c, "1-enrolar.csv", h, [ident(i, "16/09/2026", f"07:40:{11+i:02d}") for i in range(8)])
escribir(c, "2-pesar.csv", hk, [
    f"{EID[i]},{VID[i]},{PESO1[i]},16/09/2026,08:10:{11+i:02d}" for i in range(8)
])
escribir(c, "3-pesar-dia-10.csv", hk, [
    f"{EID[i]},{VID[i]},{PESO2[i]},26/09/2026,08:05:{11+i:02d}" for i in range(8)
])
escribir(c, "4-contar.csv", h, [ident(i, "18/09/2026", f"09:00:{11+i:02d}") for i in range(6)])
escribir(c, "5-baja.csv", h, [ident(6, "18/09/2026", "16:40:02"), ident(7, "18/09/2026", "16:40:19")])
(RAIZ / c / "README.md").write_text(
    "# Corrida 03 — Kilos cabezal\n\nPerfil 1 y 4-5: Identificador. Perfil 2 y 3: Kilos cabezal.\n\n"
    "1 Enrolar · 2 Pesar ingreso · 3 Pesar día 10 · 4 Contar · 5 Baja\n",
    encoding="utf-8",
)

# --- 04 caravana ---
c = "corrida-04-caravana-elec"
escribir(c, "1-enrolar.csv", h, [ident(i, "16/09/2026", f"07:40:{11+i:02d}") for i in range(8)])
escribir(c, "2-contar.csv", "CaravanaElec", EID)
escribir(c, "3-contar-faltantes.csv", "CaravanaElec", EID[:6] + [INTRUSO])
escribir(c, "4-contar-chico.csv", "CaravanaElec", EID[:4])
escribir(c, "5-baja.csv", h, [ident(6, "18/09/2026", "16:40:02"), ident(7, "18/09/2026", "16:40:19")])
(RAIZ / c / "README.md").write_text(
    "# Corrida 04 — CaravanaElec\n\nPerfil 1 y 5: Identificador. Perfil 2-4: CaravanaElec (Contar).\n\n"
    "1 Enrolar · 2 Contar 8 · 3 Contar faltantes+intruso · 4 Contar 01-04 · 5 Baja\n",
    encoding="utf-8",
)

# --- 05 peso vivo ---
c = "corrida-05-peso-vivo"
escribir(c, "1-enrolar.csv", "EID,VID,Date,Time", [
    linea_fabrica(i, "12/09/2026", f"08:14:{11+i:02d}") for i in range(8)
])
escribir(c, "2-pesar.csv", "EID,VID,Peso vivo,Date,Time", [
    f"{EID[i]},{VID[i]},{PESO1[i]},12/09/2026,09:02:{11+i:02d}" for i in range(8)
])
escribir(c, "3-pesar-dia-10.csv", "EID,VID,Peso vivo,Date,Time", [
    f"{EID[i]},{VID[i]},{PESO2[i]},22/09/2026,09:05:{11+i:02d}" for i in range(8)
])
escribir(c, "4-contar.csv", "EID,VID,Peso vivo,Date,Time", [
    f"{EID[i]},{VID[i]},{PESO1[i]},12/09/2026,09:02:{11+i:02d}" for i in range(8)
])
escribir(c, "5-baja.csv", "EID,VID,Date,Time", [
    linea_fabrica(6, "12/09/2026", "16:40:02"),
    linea_fabrica(7, "12/09/2026", "16:40:19"),
])
(RAIZ / c / "README.md").write_text(
    "# Corrida 05 — Peso vivo\n\nPerfil 1 y 5: ninguno. Perfil 2-4: Peso vivo (4 Contar: se ignora el kilo).\n\n"
    "1 Enrolar · 2 Pesar · 3 Pesar día 10 · 4 Contar · 5 Baja\n",
    encoding="utf-8",
)

# --- 06 bloqueos ---
c = "corrida-06-bloqueos"
escribir(c, "1-enrolar.csv", "EID,VID,Date,Time", [
    linea_fabrica(i, "12/09/2026", f"08:14:{11+i:02d}") for i in range(8)
])
escribir(c, "2-duplicados.csv", "EID,VID,Date,Time", [
    linea_fabrica(0, "12/09/2026", "08:14:02"),
    linea_fabrica(1, "12/09/2026", "08:14:18"),
    linea_fabrica(0, "12/09/2026", "08:16:40"),
])
escribir(c, "3-pesar-fuera-rango.csv", "EID,VID,Weight,Date,Time", [
    linea_fabrica(0, "12/09/2026", "09:10:00", 20.0),
    linea_fabrica(1, "12/09/2026", "09:10:15", 2000.0),
    linea_fabrica(2, "12/09/2026", "09:10:30", 280.0),
])
escribir(c, "4-contar.csv", "EID", EID[:6] + [INTRUSO])
escribir(c, "5-eid-excel.csv", "EID", ["9.82E+14", "9.82E+14", "9.82E+14"])
(RAIZ / c / "README.md").write_text(
    "# Corrida 06 — Bloqueos\n\nPerfil: ninguno.\n\n"
    "1 Enrolar · 2 Duplicados (queda la última) · 3 Pesar 20/2000 kg error · 4 Contar · 5 EID Excel bloquea\n",
    encoding="utf-8",
)

# --- 07 aftosa ---
c = "corrida-07-aftosa"
escribir(c, "1-enrolar.csv", h, [ident(i, "16/09/2026", f"07:40:{11+i:02d}") for i in range(8)])
escribir(c, "2-pesar.csv", hk, [
    f"{EID[i]},{VID[i]},{PESO1[i]},16/09/2026,08:10:{11+i:02d}" for i in range(8)
])
escribir(c, "3-sanidad.csv", "Identificador,TagVisual,Nota,Dia,Instante",
         [ident(i, "16/09/2026", f"10:21:{11+i:02d}", "ok") for i in range(8)])
escribir(c, "4-sanidad-aftosa.csv",
         "Identificador,TagVisual,Aftosa,Dosage,Batch,WHP,ESI,Dia,Instante",
         [f"{EID[i]},{VID[i]},Si,2.0,Lote-A,07/10/2026,14/10/2026,16/09/2026,11:05:{11+i:02d}" for i in range(8)])
escribir(c, "5-contar.csv", h, [ident(i, "18/09/2026", f"09:00:{11+i:02d}") for i in range(8)])
(RAIZ / c / "README.md").write_text(
    "# Corrida 07 — Aftosa cabezal\n\nPerfil 1,3,4,5: Identificador. Perfil 2: Kilos. Dosage/WHP se ignoran.\n\n"
    "1 Enrolar · 2 Pesar · 3 Aplicar nota · 4 Aplicar aftosa · 5 Contar 8\n",
    encoding="utf-8",
)

# --- 08 punto y coma ---
c = "corrida-08-punto-y-coma"
filas_py = [f"{EID[i]};{VID[i]};{PESO1[i]};16/09/2026;08:30:{11+i:02d}" for i in range(8)]
escribir(c, "1-enrolar.csv", "Identificador;TagVisual;Dia;Instante",
         [f"{EID[i]};{VID[i]};16/09/2026;07:40:{11+i:02d}" for i in range(8)])
escribir(c, "2-pesar.csv", "Identificador;TagVisual;Kilos;Dia;Instante", filas_py)
escribir(c, "3-sanidad.csv", "Identificador;TagVisual;Dia;Instante",
         [f"{EID[i]};{VID[i]};16/09/2026;10:21:{11+i:02d}" for i in range(8)])
escribir(c, "4-contar.csv", "Identificador;TagVisual;Dia;Instante",
         [f"{EID[i]};{VID[i]};18/09/2026;09:00:{11+i:02d}" for i in range(6)])
escribir(c, "5-baja.csv", "Identificador;TagVisual;Dia;Instante", [
    f"{EID[6]};{VID[6]};18/09/2026;16:40:02",
    f"{EID[7]};{VID[7]};18/09/2026;16:40:19",
])
(RAIZ / c / "README.md").write_text(
    "# Corrida 08 — Punto y coma (Windows)\n\nPerfil: Identificador + Kilos. Delimitador `;`.\n\n"
    "1 Enrolar · 2 Pesar · 3 Aplicar · 4 Contar · 5 Baja\n",
    encoding="utf-8",
)

# --- 09 draft bcs ---
c = "corrida-09-draft-bcs"
escribir(c, "1-enrolar.csv", h, [ident(i, "16/09/2026", f"07:40:{11+i:02d}") for i in range(8)])
draft = [1, 1, 2, 1, 2, 1, 3, 1]
bcs = [3, 3, 4, 3, 3, 2, 4, 3]
escribir(c, "2-pesar.csv", "Identificador,TagVisual,Kilos,Draft,BCS,Dia,Instante", [
    f"{EID[i]},{VID[i]},{PESO1[i]},{draft[i]},{bcs[i]},16/09/2026,08:20:{11+i:02d}" for i in range(8)
])
escribir(c, "3-pesar-dia-10.csv", hk, [
    f"{EID[i]},{VID[i]},{PESO2[i]},26/09/2026,08:05:{11+i:02d}" for i in range(8)
])
escribir(c, "4-contar.csv", h, [ident(i, "18/09/2026", f"09:00:{11+i:02d}") for i in range(6)])
escribir(c, "5-baja.csv", h, [ident(6, "18/09/2026", "16:40:02"), ident(7, "18/09/2026", "16:40:19")])
(RAIZ / c / "README.md").write_text(
    "# Corrida 09 — Draft y BCS\n\nPerfil 1,4,5: Identificador. Perfil 2-3: Kilos (Draft/BCS ignorar).\n\n"
    "1 Enrolar · 2 Pesar extras · 3 Pesar día 10 · 4 Contar · 5 Baja\n",
    encoding="utf-8",
)

# --- 10 compra ---
c = "corrida-10-compra"
escribir(c, "1-enrolar.csv", h, [ident(i, "16/09/2026", f"07:40:{11+i:02d}") for i in range(8)])
escribir(c, "2-pesar.csv", hk, [
    f"{EID[i]},{VID[i]},{PESO1[i]},16/09/2026,08:10:{11+i:02d}" for i in range(8)
])
escribir(c, "3-enrolar-09.csv", h, [f"{EID9},{VID9},01/11/2026,09:15:02"])
escribir(c, "4-contar.csv", h, [ident(i, "01/11/2026", f"10:00:{11+i:02d}") for i in range(8)] + [
    f"{EID9},{VID9},01/11/2026,10:01:50"
])
escribir(c, "5-baja.csv", h, [ident(6, "02/11/2026", "16:40:02"), ident(7, "02/11/2026", "16:40:19")])
(RAIZ / c / "README.md").write_text(
    "# Corrida 10 — Compra y cierre\n\nPerfil: Identificador + Kilos.\n\n"
    "1 Enrolar 8 · 2 Pesar · 3 Enrolar 09 · 4 Contar 8+09 · 5 Baja 07-08\n",
    encoding="utf-8",
)

print("ok", RAIZ)
