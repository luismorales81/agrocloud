# Repara el estado de Flyway tras fallo de V1_164 (Porcinos v2 migrar datos).
# Uso: .\scripts\reparar-flyway-porcinos-v2.ps1
# Luego reiniciar la aplicación.

param(
    [string] $Url = "",
    [string] $User = "",
    [string] $Password = ""
)

if ([string]::IsNullOrWhiteSpace($Url)) { $Url = $env:DATABASE_URL }
if ([string]::IsNullOrWhiteSpace($Url)) { $Url = "jdbc:mysql://localhost:3306/agrocloud" }
if ($Url -match '^jdbc:mysql://[^?]+') { $Url = $Matches[0] }
if ([string]::IsNullOrWhiteSpace($User)) { $User = if ($env:DATABASE_USERNAME) { $env:DATABASE_USERNAME } else { "root" } }
if ([string]::IsNullOrWhiteSpace($Password)) { $Password = if ($env:DATABASE_PASSWORD) { $env:DATABASE_PASSWORD } else { "123456" } }

$ErrorActionPreference = "Stop"
Set-Location (Join-Path $PSScriptRoot "..")

Write-Host "Reparando Flyway Porcinos v2 en: $Url" -ForegroundColor Cyan

$env:FLYWAY_URL = $Url
$env:FLYWAY_USER = $User
$env:FLYWAY_PASSWORD = $Password

# Eliminar registro de migración fallida (si existe)
$sqlLimpieza = @"
DELETE FROM flyway_schema_history WHERE version = '1.164' AND success = 0;
"@

$mysqlCmd = Get-Command mysql -ErrorAction SilentlyContinue
if ($mysqlCmd) {
    $dbName = if ($Url -match '/([^/?]+)$') { $Matches[1] } else { "agrocloud" }
    Write-Host "Eliminando entrada fallida 1.164 en flyway_schema_history..." -ForegroundColor Yellow
    echo $sqlLimpieza | & mysql -u $User "-p$Password" $dbName
} else {
    Write-Host "mysql CLI no encontrado. Ejecutá manualmente en la BD:" -ForegroundColor Yellow
    Write-Host $sqlLimpieza
}

Write-Host "Ejecutando flyway:repair..." -ForegroundColor Cyan
& mvn -q flyway:repair

Write-Host "Ejecutando flyway:migrate..." -ForegroundColor Cyan
& mvn -q flyway:migrate

Write-Host "Listo. Reiniciá la aplicación." -ForegroundColor Green
