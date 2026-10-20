$ErrorActionPreference = "Stop"
$raiz = $PSScriptRoot
$EID = 1..8 | ForEach-Object { "982 00012345670$_" }
$VID = 1..8 | ForEach-Object { "AG-MANGA-0$_" }
$EID9 = "982 000123456709"
$VID9 = "AG-MANGA-09"
$INTRUSO = "982 000999111222"
$PESO1 = @(286.5, 274.0, 301.2, 268.8, 292.4, 279.1, 310.0, 255.6)
$PESO2 = @(297.5, 285.0, 312.2, 279.8, 303.4, 290.1, 321.0, 266.6)

function Escribir($carpeta, $nombre, $encabezado, $filas) {
    $dest = Join-Path $raiz $carpeta
    New-Item -ItemType Directory -Force -Path $dest | Out-Null
    $cuerpo = ($filas -join "`n")
    $utf8 = New-Object System.Text.UTF8Encoding $false
    [System.IO.File]::WriteAllText((Join-Path $dest $nombre), ($encabezado + "`n" + $cuerpo + "`n"), $utf8)
}

function Fabrica($i, $fecha, $hora, $peso) {
    if ($null -eq $peso) { return "$($EID[$i]),$($VID[$i]),$fecha,$hora" }
    return "$($EID[$i]),$($VID[$i]),$peso,$fecha,$hora"
}

function Ident($i, $fecha, $hora, $extra) {
    if ($extra) { return "$($EID[$i]),$($VID[$i]),$extra,$fecha,$hora" }
    return "$($EID[$i]),$($VID[$i]),$fecha,$hora"
}

function OchoFabrica($fecha, $horaPref, $pesos) {
    0..7 | ForEach-Object {
        $h = "{0}{1:D2}" -f $horaPref, (11 + $_)
        if ($pesos) { Fabrica $_ $fecha $h $pesos[$_] } else { Fabrica $_ $fecha $h $null }
    }
}

function OchoIdent($fecha, $horaPref, $extra) {
    0..7 | ForEach-Object {
        $h = "{0}{1:D2}" -f $horaPref, (11 + $_)
        Ident $_ $fecha $h $extra
    }
}

$h = "Identificador,TagVisual,Dia,Instante"
$hk = "Identificador,TagVisual,Kilos,Dia,Instante"

# 01
$c = "corrida-01-fabrica"
Escribir $c "1-enrolar.csv" "EID,VID,Date,Time" (OchoFabrica "12/09/2026" "08:14:" $null)
Escribir $c "2-pesar.csv" "EID,VID,Weight,Date,Time" (OchoFabrica "12/09/2026" "09:02:" $PESO1)
Escribir $c "3-sanidad.csv" "EID,VID,Date,Time" (OchoFabrica "12/09/2026" "10:21:" $null)
Escribir $c "4-contar.csv" "EID" ($EID[0..5] + $INTRUSO)
Escribir $c "5-baja.csv" "EID,VID,Date,Time" @((Fabrica 6 "12/09/2026" "16:40:02" $null), (Fabrica 7 "12/09/2026" "16:40:19" $null))
Set-Content (Join-Path (Join-Path $raiz $c) "README.md") -Encoding UTF8 -Value @"
# Corrida 01 — Fabrica Data Link

Perfil: ninguno.

1 Enrolar · 2 Pesar · 3 Aplicar · 4 Contar (faltan 07-08, intruso) · 5 Baja 07-08
"@

# 02
$c = "corrida-02-identificador"
Escribir $c "1-enrolar.csv" $h (OchoIdent "16/09/2026" "07:40:" $null)
Escribir $c "2-sanidad.csv" "Identificador,TagVisual,Nota,Dia,Instante" (OchoIdent "16/09/2026" "10:21:" "ok")
Escribir $c "3-contar.csv" $h (0..5 | ForEach-Object { Ident $_ "18/09/2026" ("09:00:{0:D2}" -f (11 + $_)) $null })
Escribir $c "4-baja.csv" $h @((Ident 6 "18/09/2026" "16:40:02" $null), (Ident 7 "18/09/2026" "16:40:19" $null))
Escribir $c "5-enrolar-09.csv" $h @("$EID9,$VID9,01/11/2026,09:15:02")
Set-Content (Join-Path (Join-Path $raiz $c) "README.md") -Encoding UTF8 -Value @"
# Corrida 02 — Identificador bastón

Perfil: Identificador bastón.

1 Enrolar · 2 Aplicar (Nota se ignora) · 3 Contar faltan 07-08 · 4 Baja · 5 Enrolar 09
"@

# 03
$c = "corrida-03-kilos"
Escribir $c "1-enrolar.csv" $h (OchoIdent "16/09/2026" "07:40:" $null)
Escribir $c "2-pesar.csv" $hk (0..7 | ForEach-Object { "$($EID[$_]),$($VID[$_]),$($PESO1[$_]),16/09/2026,08:10:{0:D2}" -f (11 + $_) })
Escribir $c "3-pesar-dia-10.csv" $hk (0..7 | ForEach-Object { "$($EID[$_]),$($VID[$_]),$($PESO2[$_]),26/09/2026,08:05:{0:D2}" -f (11 + $_) })
Escribir $c "4-contar.csv" $h (0..5 | ForEach-Object { Ident $_ "18/09/2026" ("09:00:{0:D2}" -f (11 + $_)) $null })
Escribir $c "5-baja.csv" $h @((Ident 6 "18/09/2026" "16:40:02" $null), (Ident 7 "18/09/2026" "16:40:19" $null))
Set-Content (Join-Path (Join-Path $raiz $c) "README.md") -Encoding UTF8 -Value @"
# Corrida 03 — Kilos cabezal

Perfil 1 y 4-5: Identificador. Perfil 2 y 3: Kilos cabezal.

1 Enrolar · 2 Pesar ingreso · 3 Pesar día 10 · 4 Contar · 5 Baja
"@

# 04
$c = "corrida-04-caravana-elec"
Escribir $c "1-enrolar.csv" $h (OchoIdent "16/09/2026" "07:40:" $null)
Escribir $c "2-contar.csv" "CaravanaElec" $EID
Escribir $c "3-contar-faltantes.csv" "CaravanaElec" ($EID[0..5] + $INTRUSO)
Escribir $c "4-contar-chico.csv" "CaravanaElec" $EID[0..3]
Escribir $c "5-baja.csv" $h @((Ident 6 "18/09/2026" "16:40:02" $null), (Ident 7 "18/09/2026" "16:40:19" $null))
Set-Content (Join-Path (Join-Path $raiz $c) "README.md") -Encoding UTF8 -Value @"
# Corrida 04 — CaravanaElec

Perfil 1 y 5: Identificador. Perfil 2-4: CaravanaElec (Contar).

1 Enrolar · 2 Contar 8 · 3 Contar faltantes+intruso · 4 Contar 01-04 · 5 Baja
"@

# 05
$c = "corrida-05-peso-vivo"
Escribir $c "1-enrolar.csv" "EID,VID,Date,Time" (OchoFabrica "12/09/2026" "08:14:" $null)
Escribir $c "2-pesar.csv" "EID,VID,Peso vivo,Date,Time" (0..7 | ForEach-Object { "$($EID[$_]),$($VID[$_]),$($PESO1[$_]),12/09/2026,09:02:{0:D2}" -f (11 + $_) })
Escribir $c "3-pesar-dia-10.csv" "EID,VID,Peso vivo,Date,Time" (0..7 | ForEach-Object { "$($EID[$_]),$($VID[$_]),$($PESO2[$_]),22/09/2026,09:05:{0:D2}" -f (11 + $_) })
Escribir $c "4-contar.csv" "EID,VID,Peso vivo,Date,Time" (0..7 | ForEach-Object { "$($EID[$_]),$($VID[$_]),$($PESO1[$_]),12/09/2026,09:02:{0:D2}" -f (11 + $_) })
Escribir $c "5-baja.csv" "EID,VID,Date,Time" @((Fabrica 6 "12/09/2026" "16:40:02" $null), (Fabrica 7 "12/09/2026" "16:40:19" $null))
Set-Content (Join-Path (Join-Path $raiz $c) "README.md") -Encoding UTF8 -Value @"
# Corrida 05 — Peso vivo

Perfil 1 y 5: ninguno. Perfil 2-4: Peso vivo (4 Contar: se ignora el kilo).

1 Enrolar · 2 Pesar · 3 Pesar día 10 · 4 Contar · 5 Baja
"@

# 06
$c = "corrida-06-bloqueos"
Escribir $c "1-enrolar.csv" "EID,VID,Date,Time" (OchoFabrica "12/09/2026" "08:14:" $null)
Escribir $c "2-duplicados.csv" "EID,VID,Date,Time" @((Fabrica 0 "12/09/2026" "08:14:02" $null), (Fabrica 1 "12/09/2026" "08:14:18" $null), (Fabrica 0 "12/09/2026" "08:16:40" $null))
Escribir $c "3-pesar-fuera-rango.csv" "EID,VID,Weight,Date,Time" @((Fabrica 0 "12/09/2026" "09:10:00" 20.0), (Fabrica 1 "12/09/2026" "09:10:15" 2000.0), (Fabrica 2 "12/09/2026" "09:10:30" 280.0))
Escribir $c "4-contar.csv" "EID" ($EID[0..5] + $INTRUSO)
Escribir $c "5-eid-excel.csv" "EID" @("9.82E+14", "9.82E+14", "9.82E+14")
Set-Content (Join-Path (Join-Path $raiz $c) "README.md") -Encoding UTF8 -Value @"
# Corrida 06 — Bloqueos

Perfil: ninguno.

1 Enrolar · 2 Duplicados (queda la última) · 3 Pesar 20/2000 kg error · 4 Contar · 5 EID Excel bloquea
"@

# 07
$c = "corrida-07-aftosa"
Escribir $c "1-enrolar.csv" $h (OchoIdent "16/09/2026" "07:40:" $null)
Escribir $c "2-pesar.csv" $hk (0..7 | ForEach-Object { "$($EID[$_]),$($VID[$_]),$($PESO1[$_]),16/09/2026,08:10:{0:D2}" -f (11 + $_) })
Escribir $c "3-sanidad.csv" "Identificador,TagVisual,Nota,Dia,Instante" (OchoIdent "16/09/2026" "10:21:" "ok")
Escribir $c "4-sanidad-aftosa.csv" "Identificador,TagVisual,Aftosa,Dosage,Batch,WHP,ESI,Dia,Instante" (0..7 | ForEach-Object { "$($EID[$_]),$($VID[$_]),Si,2.0,Lote-A,07/10/2026,14/10/2026,16/09/2026,11:05:{0:D2}" -f (11 + $_) })
Escribir $c "5-contar.csv" $h (OchoIdent "18/09/2026" "09:00:" $null)
Set-Content (Join-Path (Join-Path $raiz $c) "README.md") -Encoding UTF8 -Value @"
# Corrida 07 — Aftosa cabezal

Perfil 1,3,4,5: Identificador. Perfil 2: Kilos. Dosage/WHP se ignoran.

1 Enrolar · 2 Pesar · 3 Aplicar nota · 4 Aplicar aftosa · 5 Contar 8
"@

# 08
$c = "corrida-08-punto-y-coma"
Escribir $c "1-enrolar.csv" "Identificador;TagVisual;Dia;Instante" (0..7 | ForEach-Object { "$($EID[$_]);$($VID[$_]);16/09/2026;07:40:{0:D2}" -f (11 + $_) })
Escribir $c "2-pesar.csv" "Identificador;TagVisual;Kilos;Dia;Instante" (0..7 | ForEach-Object { "$($EID[$_]);$($VID[$_]);$($PESO1[$_]);16/09/2026;08:30:{0:D2}" -f (11 + $_) })
Escribir $c "3-sanidad.csv" "Identificador;TagVisual;Dia;Instante" (0..7 | ForEach-Object { "$($EID[$_]);$($VID[$_]);16/09/2026;10:21:{0:D2}" -f (11 + $_) })
Escribir $c "4-contar.csv" "Identificador;TagVisual;Dia;Instante" (0..5 | ForEach-Object { "$($EID[$_]);$($VID[$_]);18/09/2026;09:00:{0:D2}" -f (11 + $_) })
Escribir $c "5-baja.csv" "Identificador;TagVisual;Dia;Instante" @("$($EID[6]);$($VID[6]);18/09/2026;16:40:02", "$($EID[7]);$($VID[7]);18/09/2026;16:40:19")
Set-Content (Join-Path (Join-Path $raiz $c) "README.md") -Encoding UTF8 -Value @"
# Corrida 08 — Punto y coma (Windows)

Perfil: Identificador + Kilos. Delimitador `;`.

1 Enrolar · 2 Pesar · 3 Aplicar · 4 Contar · 5 Baja
"@

# 09
$c = "corrida-09-draft-bcs"
$draft = @(1, 1, 2, 1, 2, 1, 3, 1)
$bcs = @(3, 3, 4, 3, 3, 2, 4, 3)
Escribir $c "1-enrolar.csv" $h (OchoIdent "16/09/2026" "07:40:" $null)
Escribir $c "2-pesar.csv" "Identificador,TagVisual,Kilos,Draft,BCS,Dia,Instante" (0..7 | ForEach-Object { "$($EID[$_]),$($VID[$_]),$($PESO1[$_]),$($draft[$_]),$($bcs[$_]),16/09/2026,08:20:{0:D2}" -f (11 + $_) })
Escribir $c "3-pesar-dia-10.csv" $hk (0..7 | ForEach-Object { "$($EID[$_]),$($VID[$_]),$($PESO2[$_]),26/09/2026,08:05:{0:D2}" -f (11 + $_) })
Escribir $c "4-contar.csv" $h (0..5 | ForEach-Object { Ident $_ "18/09/2026" ("09:00:{0:D2}" -f (11 + $_)) $null })
Escribir $c "5-baja.csv" $h @((Ident 6 "18/09/2026" "16:40:02" $null), (Ident 7 "18/09/2026" "16:40:19" $null))
Set-Content (Join-Path (Join-Path $raiz $c) "README.md") -Encoding UTF8 -Value @"
# Corrida 09 — Draft y BCS

Perfil 1,4,5: Identificador. Perfil 2-3: Kilos (Draft/BCS ignorar).

1 Enrolar · 2 Pesar extras · 3 Pesar día 10 · 4 Contar · 5 Baja
"@

# 10
$c = "corrida-10-compra"
Escribir $c "1-enrolar.csv" $h (OchoIdent "16/09/2026" "07:40:" $null)
Escribir $c "2-pesar.csv" $hk (0..7 | ForEach-Object { "$($EID[$_]),$($VID[$_]),$($PESO1[$_]),16/09/2026,08:10:{0:D2}" -f (11 + $_) })
Escribir $c "3-enrolar-09.csv" $h @("$EID9,$VID9,01/11/2026,09:15:02")
Escribir $c "4-contar.csv" $h ((0..7 | ForEach-Object { Ident $_ "01/11/2026" ("10:00:{0:D2}" -f (11 + $_)) $null }) + "$EID9,$VID9,01/11/2026,10:01:50")
Escribir $c "5-baja.csv" $h @((Ident 6 "02/11/2026" "16:40:02" $null), (Ident 7 "02/11/2026" "16:40:19" $null))
Set-Content (Join-Path (Join-Path $raiz $c) "README.md") -Encoding UTF8 -Value @"
# Corrida 10 — Compra y cierre

Perfil: Identificador + Kilos.

1 Enrolar 8 · 2 Pesar · 3 Enrolar 09 · 4 Contar 8+09 · 5 Baja 07-08
"@

Write-Host "ok $raiz"
