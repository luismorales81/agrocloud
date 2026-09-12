# Compacta .env si un bucle anterior lo partió en un carácter por línea.
# No imprime valores. Uso: powershell -File deploy/compactar-env.ps1
$ErrorActionPreference = "Stop"
$raiz = Split-Path -Parent $PSScriptRoot
if (-not (Test-Path (Join-Path $raiz "docker-compose.yml"))) {
  $raiz = Get-Location
}
$archivo = Join-Path $raiz ".env"
if (-not (Test-Path $archivo)) {
  Write-Host "no hay .env"
  exit 0
}

$lineas = [string[]][System.IO.File]::ReadAllLines($archivo)
$muestra = [Math]::Min(200, $lineas.Length)
$promedio = 0.0
if ($muestra -gt 0) {
  $suma = 0
  for ($i = 0; $i -lt $muestra; $i++) { $suma += $lineas[$i].Length }
  $promedio = $suma / $muestra
}

$textoLineas = $lineas
if ($lineas.Length -gt 400 -and $promedio -lt 4) {
  $crudo = [string]::Join("", $lineas)
  $textoLineas = [regex]::Replace($crudo, '(?<!^)([A-Za-z_][A-Za-z0-9_]*=)', "`n`$1") -split "`n"
}

$vistas = [ordered]@{}
$comentarios = [System.Collections.Generic.List[string]]::new()
$enCabeza = $true
foreach ($linea in $textoLineas) {
  $s = $linea.TrimStart([char]0xFEFF)
  if ($s -and -not $s.StartsWith("#") -and $s.Contains("=")) {
    $enCabeza = $false
    $idx = $s.IndexOf("=")
    $clave = $s.Substring(0, $idx)
    $valor = $s.Substring($idx + 1)
    if (-not $vistas.Contains($clave)) {
      $vistas[$clave] = $valor
    } elseif ([string]::IsNullOrWhiteSpace([string]$vistas[$clave]) -and -not [string]::IsNullOrWhiteSpace($valor)) {
      $vistas[$clave] = $valor
    }
  } elseif ($enCabeza -and ($s.StartsWith("#") -or $s -eq "")) {
    $comentarios.Add($s) | Out-Null
  }
}

$salida = [System.Collections.Generic.List[string]]::new()
$salida.AddRange($comentarios)
foreach ($k in $vistas.Keys) {
  $salida.Add("$k=$($vistas[$k])") | Out-Null
}

$utf8 = New-Object System.Text.UTF8Encoding $false
[System.IO.File]::WriteAllLines($archivo, $salida.ToArray(), $utf8)
Write-Host "env_compactado claves=$($vistas.Count) bytes=$((Get-Item $archivo).Length)"
