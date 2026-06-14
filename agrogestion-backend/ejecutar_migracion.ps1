# Script PowerShell para ejecutar la migración de renombrado de tablas
# Ejecutar con: .\ejecutar_migracion.ps1

Write-Host "Buscando MySQL..." -ForegroundColor Yellow

# Rutas comunes donde puede estar MySQL
$mysqlPaths = @(
    "C:\xampp\mysql\bin\mysql.exe",
    "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe",
    "C:\Program Files (x86)\MySQL\MySQL Server 8.0\bin\mysql.exe",
    "C:\wamp64\bin\mysql\mysql8.0.xx\bin\mysql.exe"
)

$mysqlPath = $null
foreach ($path in $mysqlPaths) {
    if (Test-Path $path) {
        $mysqlPath = $path
        Write-Host "MySQL encontrado en: $path" -ForegroundColor Green
        break
    }
}

if ($null -eq $mysqlPath) {
    Write-Host "`nMySQL no encontrado en las rutas comunes." -ForegroundColor Red
    Write-Host "Por favor ejecuta el script SQL manualmente:" -ForegroundColor Yellow
    Write-Host "  1. Abre MySQL Workbench o phpMyAdmin" -ForegroundColor Cyan
    Write-Host "  2. Conéctate a la base de datos 'agrocloud'" -ForegroundColor Cyan
    Write-Host "  3. Ejecuta el archivo: ejecutar_renombrado_tablas.sql" -ForegroundColor Cyan
    Write-Host "`nO desde la línea de comandos de MySQL:" -ForegroundColor Yellow
    Write-Host "  mysql -u root -p123456 agrocloud < ejecutar_renombrado_tablas.sql" -ForegroundColor Cyan
    exit 1
}

Write-Host "`nEjecutando migración..." -ForegroundColor Yellow

try {
    Get-Content ejecutar_renombrado_tablas.sql | & $mysqlPath -u root -p123456 agrocloud
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "`n¡Migración completada exitosamente!" -ForegroundColor Green
        Write-Host "Todas las tablas han sido renombradas con los prefijos correspondientes." -ForegroundColor Green
    } else {
        Write-Host "`nError al ejecutar la migración. Código de salida: $LASTEXITCODE" -ForegroundColor Red
        Write-Host "Por favor revisa los errores y ejecuta el script manualmente." -ForegroundColor Yellow
    }
} catch {
    Write-Host "`nError al ejecutar MySQL: $_" -ForegroundColor Red
    Write-Host "Por favor ejecuta el script manualmente." -ForegroundColor Yellow
}



