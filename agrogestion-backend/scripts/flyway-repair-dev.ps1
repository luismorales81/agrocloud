# Flyway repair en BD de desarrollo (agrocloud)
#
# USO: solo cuando Flyway reporta checksum mismatch en migraciones ya aplicadas.
# Hacer backup de la BD antes de ejecutar.
#
# PowerShell:
#   cd agrogestion-backend
#   .\scripts\flyway-repair-dev.ps1
#
# Variables opcionales:
#   $env:FLYWAY_URL = 'jdbc:mysql://localhost:3306/agrocloud?...'
#   $env:FLYWAY_USER = 'root'
#   $env:FLYWAY_PASSWORD = 'tu_password'

param(
    [string]$Url = $env:FLYWAY_URL,
    [string]$User = $env:FLYWAY_USER,
    [string]$Password = $env:FLYWAY_PASSWORD
)

if (-not $Url) {
    $Url = 'jdbc:mysql://localhost:3306/agrocloud?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC'
}
if (-not $User) { $User = 'root' }
if (-not $Password) {
    Write-Host 'Definí FLYWAY_PASSWORD o pasá -Password' -ForegroundColor Red
    exit 1
}

Write-Host '=== Flyway repair (desarrollo) ===' -ForegroundColor Cyan
Write-Host "URL: $Url"
Write-Host ''
Write-Host 'ADVERTENCIA: esto actualiza flyway_schema_history sin revertir cambios SQL.' -ForegroundColor Yellow
Write-Host 'Asegurate de tener backup de la base antes de continuar.' -ForegroundColor Yellow
$confirm = Read-Host 'Escribí REPAIR para continuar'
if ($confirm -ne 'REPAIR') {
    Write-Host 'Cancelado.'
    exit 0
}

$env:FLYWAY_URL = $Url
$env:FLYWAY_USER = $User
$env:FLYWAY_PASSWORD = $Password

mvn flyway:repair "-Dflyway.outOfOrder=true"
if ($LASTEXITCODE -eq 0) {
    Write-Host 'Repair completado. Ejecutá flyway:info para verificar.' -ForegroundColor Green
} else {
    Write-Host 'Repair falló.' -ForegroundColor Red
    exit $LASTEXITCODE
}
