# Script PowerShell para verificar y corregir el rol de Luis Operario

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Verificando y corrigiendo rol de Luis" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$mysqlPath = "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
$dbHost = "localhost"
$dbPort = "3306"
$dbUser = "root"
$dbPassword = "123456"
$dbName = "agrocloud"

Write-Host "Configuracion de MySQL:" -ForegroundColor Yellow
Write-Host "- Host: $dbHost"
Write-Host "- Puerto: $dbPort"
Write-Host "- Base de datos: $dbName"
Write-Host "- Usuario: $dbUser"
Write-Host ""

# Verificar que MySQL existe
if (-not (Test-Path $mysqlPath)) {
    Write-Host "ERROR: No se encontro MySQL en la ruta especificada" -ForegroundColor Red
    Write-Host "Ruta: $mysqlPath" -ForegroundColor Red
    pause
    exit 1
}

Write-Host "Ejecutando verificacion y correccion..." -ForegroundColor Green
Write-Host ""

# Ejecutar el script SQL
$sqlScript = Get-Content "verificar-y-corregir-rol-luis.sql" -Raw
$sqlScript | & $mysqlPath -h $dbHost -P $dbPort -u $dbUser -p$dbPassword $dbName

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Green
    Write-Host "VERIFICACION Y CORRECCION COMPLETADA" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "Proximos pasos:" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "1. Cerrar sesion en el frontend si Luis esta logueado"
    Write-Host "2. Limpiar cache del navegador (Ctrl+Shift+Delete)"
    Write-Host "3. Volver a iniciar sesion con:"
    Write-Host "   Email: operario.luis@agrocloud.com"
    Write-Host "   Contrasena: admin123"
    Write-Host "4. Verificar que ahora diga 'Rol: Operario'"
    Write-Host ""
} else {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Red
    Write-Host "ERROR AL EJECUTAR EL SCRIPT" -ForegroundColor Red
    Write-Host "========================================" -ForegroundColor Red
    Write-Host ""
}

pause


