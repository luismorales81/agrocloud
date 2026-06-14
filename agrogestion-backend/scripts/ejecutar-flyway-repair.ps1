# Actualiza checksums en flyway_schema_history para que coincidan con los .sql actuales del repo.
# Evita pasar la URL JDBC por la linea de comandos (los & rompen mvn.cmd en Windows).
# Uso (desde agrogestion-backend o con ruta al script):
#   .\scripts\ejecutar-flyway-repair.ps1
# Usa por defecto los mismos valores que application.properties (root / 123456 / agrocloud).
# Si usás .env, definí DATABASE_URL, DATABASE_USERNAME, DATABASE_PASSWORD antes de ejecutar
# (el script los toma si no pasás -Url/-User/-Password).
# Otra base explícita:
#   .\scripts\ejecutar-flyway-repair.ps1 -Url "jdbc:mysql://127.0.0.1:3306/agrocloud" -User "root" -Password "..."

param(
    [string] $Url = "",
    [string] $User = "",
    [string] $Password = ""
)

if ([string]::IsNullOrWhiteSpace($Url)) {
    $Url = $env:DATABASE_URL
}
if ([string]::IsNullOrWhiteSpace($Url)) {
    $Url = "jdbc:mysql://localhost:3306/agrocloud"
}
# Flyway CLI a veces falla con & en la URL; quedarse con host/puerto/base si viene JDBC completo
if ($Url -match '^jdbc:mysql://[^?]+') {
    $Url = $Matches[0]
}
if ([string]::IsNullOrWhiteSpace($User)) {
    $User = $env:DATABASE_USERNAME
}
if ([string]::IsNullOrWhiteSpace($User)) {
    $User = "root"
}
if ([string]::IsNullOrWhiteSpace($Password)) {
    $Password = $env:DATABASE_PASSWORD
}
if ([string]::IsNullOrWhiteSpace($Password)) {
    $Password = "123456"
}

$ErrorActionPreference = "Stop"
Set-Location (Join-Path $PSScriptRoot "..")

Write-Host "Flyway repair contra: $Url (usuario: $User)" -ForegroundColor Cyan

$anteriorUrl = $env:FLYWAY_URL
$anteriorUsuario = $env:FLYWAY_USER
$anteriorClave = $env:FLYWAY_PASSWORD

$env:FLYWAY_URL = $Url
$env:FLYWAY_USER = $User
$env:FLYWAY_PASSWORD = $Password

try {
    & mvn -q flyway:repair
}
finally {
    if ($null -ne $anteriorUrl) { $env:FLYWAY_URL = $anteriorUrl } else { Remove-Item Env:\FLYWAY_URL -ErrorAction SilentlyContinue }
    if ($null -ne $anteriorUsuario) { $env:FLYWAY_USER = $anteriorUsuario } else { Remove-Item Env:\FLYWAY_USER -ErrorAction SilentlyContinue }
    if ($null -ne $anteriorClave) { $env:FLYWAY_PASSWORD = $anteriorClave } else { Remove-Item Env:\FLYWAY_PASSWORD -ErrorAction SilentlyContinue }
}

Write-Host "Listo. Reinicia la aplicacion." -ForegroundColor Green
