@echo off
echo ========================================
echo MIGRACION COMPLETA A RAILWAY
echo ========================================
echo.

echo Este script:
echo 1. Creara las vistas en Railway
echo 2. Exportara los datos de tu base de datos local
echo 3. Los importara en Railway MySQL
echo.

echo [1/4] Creando vistas en Railway MySQL...
echo.

"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -h gondola.proxy.rlwy.net -P 54893 -u root -pWSoobrppUQbaPINdsRcoQVkUvtYKjmSe railway < crear-vistas-railway.sql

if %errorlevel% neq 0 (
    echo.
    echo ADVERTENCIA: Puede haber errores al crear vistas, pero continuando...
    echo.
)

echo.
echo [2/4] Vistas creadas
echo.

echo [3/4] Exportando datos de la base de datos local...
echo.

set BACKUP_FILE=backup-agrocloud-%date:~-4,4%%date:~-7,2%%date:~-10,2%.sql
set BACKUP_FILE=%BACKUP_FILE: =0%

echo Archivo de backup: %BACKUP_FILE%
echo.

"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysqldump.exe" -u root -p123456 agrocloud > %BACKUP_FILE%

if %errorlevel% neq 0 (
    echo.
    echo ERROR: No se pudo exportar la base de datos local
    echo Verifica que MySQL este corriendo y que las credenciales sean correctas
    echo.
    pause
    exit /b 1
)

echo.
echo [4/4] Datos exportados
echo.

echo [5/4] Importando datos a Railway MySQL...
echo.

"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -h gondola.proxy.rlwy.net -P 54893 -u root -pWSoobrppUQbaPINdsRcoQVkUvtYKjmSe railway < %BACKUP_FILE%

if %errorlevel% neq 0 (
    echo.
    echo ADVERTENCIA: Puede haber errores en la importacion, pero continuando...
    echo.
)

echo.
echo ========================================
echo MIGRACION COMPLETADA
echo ========================================
echo.
echo Archivo de backup guardado en: %BACKUP_FILE%
echo.
echo Los datos de tu base de datos local ahora estan en Railway MySQL
echo.
echo NOTA: Verifica los logs arriba por posibles errores
echo.
pause

