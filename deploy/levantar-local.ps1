# Levantar AgroGestion en Docker Desktop (PC). No uses docker-compose.prod.yml aquí.
# Uso:  powershell -File deploy/levantar-local.ps1
$ErrorActionPreference = "Stop"
$raiz = Split-Path -Parent $PSScriptRoot
if (-not (Test-Path (Join-Path $raiz "docker-compose.yml"))) {
  $raiz = Get-Location
}
Set-Location $raiz

function Nuevo-Secreto {
  $bytes = [byte[]]::new(36)
  $rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
  $rng.GetBytes($bytes)
  $rng.Dispose()
  return [Convert]::ToBase64String($bytes)
}

function Asegurar-Linea([string]$archivo, [string]$clave, [string]$valorSiVacio) {
  $lineas = [System.Collections.Generic.List[string]]::new()
  $lineas.AddRange([string[]][System.IO.File]::ReadAllLines($archivo))
  $encontrada = $false
  for ($i = 0; $i -lt $lineas.Count; $i++) {
    $linea = $lineas[$i]
    if ($linea -match "^${clave}=") {
      $encontrada = $true
      $actual = $linea.Substring($clave.Length + 1)
      if ([string]::IsNullOrWhiteSpace($actual)) { $lineas[$i] = "${clave}=$valorSiVacio" }
    }
  }
  if (-not $encontrada) {
    $lineas.Add("${clave}=$valorSiVacio")
  }
  $utf8 = New-Object System.Text.UTF8Encoding $false
  [System.IO.File]::WriteAllLines($archivo, $lineas.ToArray(), $utf8)
}

function Forzar-Linea([string]$archivo, [string]$clave, [string]$valor) {
  $lineas = [System.Collections.Generic.List[string]]::new()
  $lineas.AddRange([string[]][System.IO.File]::ReadAllLines($archivo))
  $encontrada = $false
  for ($i = 0; $i -lt $lineas.Count; $i++) {
    if ($lineas[$i] -match "^${clave}=") {
      $lineas[$i] = "${clave}=$valor"
      $encontrada = $true
    }
  }
  if (-not $encontrada) { $lineas.Add("${clave}=$valor") }
  $utf8 = New-Object System.Text.UTF8Encoding $false
  [System.IO.File]::WriteAllLines($archivo, $lineas.ToArray(), $utf8)
}

$envFile = Join-Path $raiz ".env"
if (-not (Test-Path $envFile)) {
  Copy-Item (Join-Path $raiz ".env.example") $envFile
  Write-Host "Se creo .env a partir de .env.example"
}

& (Join-Path $PSScriptRoot "compactar-env.ps1")

if ((Get-Item $envFile).Length -gt 80000) {
  Write-Host "AVISO: .env sigue superando 80KB. No recrees contenedores hasta compactarlo (deploy/compactar-env.ps1)."
}

Asegurar-Linea $envFile "MYSQL_DATABASE" "agroclouddb"
Asegurar-Linea $envFile "MYSQL_ROOT_PASSWORD" (Nuevo-Secreto)
Asegurar-Linea $envFile "DATABASE_USERNAME" "agrocloud"
Asegurar-Linea $envFile "DATABASE_PASSWORD" (Nuevo-Secreto)
Asegurar-Linea $envFile "JWT_SECRET" (Nuevo-Secreto)
Asegurar-Linea $envFile "CHAT_IA_ENCRYPTION_KEY" (Nuevo-Secreto)
Asegurar-Linea $envFile "PUERTO_MYSQL_HOST" "3307"
Asegurar-Linea $envFile "PUERTO_BACKEND_HOST" "8080"
Asegurar-Linea $envFile "PUERTO_FRONTEND_HOST" "3001"
Asegurar-Linea $envFile "CORS_ALLOWED_ORIGINS" "http://localhost:3001"
Asegurar-Linea $envFile "FRONTEND_URL" "http://localhost:3001"
Forzar-Linea $envFile "VITE_API_BASE_URL" "/api"

Write-Host "Construyendo y levantando MySQL + backend + frontend (primera vez: varios minutos)..."
docker compose up --build -d

Write-Host "Esperando health del backend (primera vez Flyway, hasta 6 minutos)..."
$ok = $false
for ($i = 1; $i -le 72; $i++) {
  try {
    $estado = docker inspect --format "{{.State.Health.Status}}" agrogestion-backend 2>$null
    if ($estado -eq "healthy") { $ok = $true; break }
  } catch { }
  Start-Sleep -Seconds 5
}
if (-not $ok) {
  Write-Host "El backend no quedo healthy. Ultimos logs:"
  docker compose logs --tail 80 backend
  exit 1
}

Write-Host ""
Write-Host "Stack local listo:"
Write-Host "  Frontend  http://localhost:3001"
Write-Host "  API (mismo origen)  http://localhost:3001/api/health"
Write-Host "  API directa         http://localhost:8080/api/health"
Write-Host "  MySQL     localhost:3307 (no usa el 3306 de tu PC)"
Write-Host "  Login local: admin@localhost / AgrocloudLocal1"
Write-Host ""
Write-Host "Imagenes: agrogestion-backend:local  agrogestion-frontend:local"
Write-Host "Para llevarlas al VPS: powershell -File deploy/exportar-imagenes-local.ps1"
