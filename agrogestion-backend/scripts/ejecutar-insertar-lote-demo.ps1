# Inserta lote demo feedlot (AgroCloud Demo). QA: admin@agrocloud.com → /feedlot/lotes/{id}
# Ejecutar despues de ejecutar-habilitar-feedlot-demo.ps1
# Uso: .\ejecutar-insertar-lote-demo.ps1

param(
    [string]$DbHost = "localhost",
    [int]$DbPort = 3306,
    [string]$DbUser = "root",
    [string]$DbPass = "123456",
    [string]$DbName = "agrocloud"
)

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$SqlFile = Join-Path $ScriptDir "INSERTAR_LOTE_DEMO_FEEDLOT.sql"

$mysql = "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
if (-not (Test-Path $mysql)) {
    $mysql = Get-Command mysql -ErrorAction SilentlyContinue | Select-Object -ExpandProperty Source
}

if (-not $mysql) {
    Write-Error "No se encontro mysql.exe. Instale MySQL o agreguelo al PATH."
    exit 1
}

Write-Host "Ejecutando $SqlFile en $DbName@$DbHost ..."

$env:MYSQL_PWD = $DbPass
Get-Content -Raw -Encoding UTF8 $SqlFile | & $mysql -h $DbHost -P $DbPort -u $DbUser $DbName
$exitCode = $LASTEXITCODE
Remove-Item Env:MYSQL_PWD -ErrorAction SilentlyContinue

if ($exitCode -eq 0) {
    Write-Host "OK: Lote demo feedlot insertado." -ForegroundColor Green
} else {
    Write-Host "Error. Codigo: $exitCode" -ForegroundColor Red
}
exit $exitCode
