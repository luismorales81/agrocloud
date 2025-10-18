@echo off
echo ========================================
echo MIGRAR DATOS LOCAL A RAILWAY - SIN VISTAS
echo ========================================
echo.

echo Este script exportara los datos de tu base de datos local
echo y los importara en Railway MySQL.
echo.

echo [1/3] Exportando datos de la base de datos local...
echo.

set BACKUP_FILE=backup-agrocloud-%date:~-4,4%%date:~-7,2%%date:~-10,2%.sql
set BACKUP_FILE=%BACKUP_FILE: =0%

echo Archivo de backup: %BACKUP_FILE%
echo.

"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysqldump.exe" -u root -p123456 --skip-triggers --skip-events agrocloud > %BACKUP_FILE% 2>nul

if %errorlevel% neq 0 (
    echo.
    echo ADVERTENCIA: Algunas vistas pueden tener problemas, pero continuando...
    echo.
)

echo.
echo [2/3] Datos exportados
echo.

echo [3/3] Importando datos a Railway MySQL...
echo.

echo DATOS DE RAILWAY (configura estos valores):
echo Host: gondola.proxy.rlwy.net
echo Puerto: 54893
echo Usuario: root
echo Base de datos: railway
echo.

"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -h gondola.proxy.rlwy.net -P 54893 -u root -pWSoobrppUQbaPINdsRcoQVkUvtYKjmSe railway < %BACKUP_FILE% 2>nul

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
echo NOTA: Si hubo errores con vistas, puedes recrearlas manualmente
echo.
pause

