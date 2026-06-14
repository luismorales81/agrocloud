# Ejecuta la migración V1_120: agregar columna origen a porcinos_recria
# Uso: .\ejecutar_migracion_V1_120.ps1

$ErrorActionPreference = "Stop"
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $scriptDir

$mysqlPaths = @(
    "C:\xampp\mysql\bin\mysql.exe",
    "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe",
    "C:\Program Files (x86)\MySQL\MySQL Server 8.0\bin\mysql.exe",
    "mysql.exe"
)

$mysqlPath = $null
foreach ($path in $mysqlPaths) {
    if (Get-Command $path -ErrorAction SilentlyContinue) { $mysqlPath = $path; break }
    if (Test-Path $path) { $mysqlPath = $path; break }
}

if (-not $mysqlPath) {
    Write-Host "MySQL no encontrado. Ejecuta manualmente en tu cliente SQL:" -ForegroundColor Yellow
    Write-Host "  src\main\resources\db\migration\V1_120__Add_Origen_To_Porcinos_Recria.sql" -ForegroundColor Cyan
    exit 1
}

$sqlFile = Join-Path $scriptDir "src\main\resources\db\migration\V1_120__Add_Origen_To_Porcinos_Recria.sql"
if (-not (Test-Path $sqlFile)) {
    Write-Host "No se encuentra el archivo de migración: $sqlFile" -ForegroundColor Red
    exit 1
}

Write-Host "Ejecutando migración V1_120 (origen en porcinos_recria)..." -ForegroundColor Cyan
try {
    Get-Content $sqlFile -Raw | & $mysqlPath -u root -p123456 -h localhost agrocloud 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "Migración V1_120 completada." -ForegroundColor Green
    } else {
        Write-Host "Revisa el mensaje anterior. Si la columna ya existe, no hay problema." -ForegroundColor Yellow
    }
} catch {
    Write-Host "Error: $_" -ForegroundColor Red
    exit 1
}
