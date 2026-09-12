# Guarda las imagenes locales en un tar para copiar al VPS (sin recompilar Maven alli).
# Uso: powershell -File deploy/exportar-imagenes-local.ps1
$ErrorActionPreference = "Stop"
$raiz = Split-Path -Parent $PSScriptRoot
if (-not (Test-Path (Join-Path $raiz "docker-compose.yml"))) {
  $raiz = Get-Location
}
Set-Location $raiz

$salida = Join-Path $raiz "database-dumps"
New-Item -ItemType Directory -Force -Path $salida | Out-Null
$archivo = Join-Path $salida "agrogestion-imagenes-local.tar"

Write-Host "Exportando agrogestion-backend:local y agrogestion-frontend:local..."
docker save -o $archivo agrogestion-backend:local agrogestion-frontend:local mysql:8.0
Get-Item $archivo | Format-List FullName, Length

Write-Host "En el VPS, despues de copiar el tar:"
Write-Host "  docker load -i agrogestion-imagenes-local.tar"
Write-Host "Retag para compose de produccion (ejemplo):"
Write-Host "  docker tag agrogestion-backend:local agrogestion-backend:prod"
Write-Host "  docker tag agrogestion-frontend:local agrogestion-frontend:prod"
Write-Host "El frontend local ya usa VITE_API_BASE_URL=/api (nginx proxifica al backend)."
Write-Host "En el VPS con Caddy se puede usar la misma imagen de frontend."
Write-Host "MySQL del tar es la imagen vacia, no tus datos."
