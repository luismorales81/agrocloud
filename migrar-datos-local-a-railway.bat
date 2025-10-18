@echo off
echo ========================================
echo MIGRAR DATOS LOCAL A RAILWAY
echo ========================================
echo.

echo Este script exportara los datos de tu base de datos local
echo y los importara en Railway MySQL.
echo.

echo [1/4] Exportando datos de la base de datos local...
echo.

set BACKUP_FILE=backup-agrocloud-%date:~-4,4%%date:~-7,2%%date:~-10,2%-%time:~0,2%%time:~3,2%%time:~6,2%.sql
set BACKUP_FILE=%BACKUP_FILE: =0%

echo Archivo de backup: %BACKUP_FILE%
echo.

mysqldump -u root -p123456 agrocloud > %BACKUP_FILE%

if %errorlevel% neq 0 (
    echo.
    echo ERROR: No se pudo exportar la base de datos local
    echo Verifica que MySQL este corriendo y que las credenciales sean correctas
    echo.
    pause
    exit /b 1
)

echo.
echo [2/4] Datos exportados exitosamente
echo.

echo [3/4] Importando datos a Railway MySQL...
echo.

echo Ingresa los datos de Railway MySQL:
echo.

set /p RAILWAY_HOST="Host (ej: gondola.proxy.rlwy.net): "
set /p RAILWAY_PORT="Puerto (ej: 54893): "
set /p RAILWAY_USER="Usuario (ej: root): "
set /p RAILWAY_PASS="Contraseña: "
set /p RAILWAY_DB="Base de datos (ej: railway): "

echo.
echo Importando datos a Railway...
echo.

mysql -h %RAILWAY_HOST% -P %RAILWAY_PORT% -u %RAILWAY_USER% -p%RAILWAY_PASS% %RAILWAY_DB% < %BACKUP_FILE%

if %errorlevel% neq 0 (
    echo.
    echo ERROR: No se pudo importar los datos a Railway
    echo Verifica que los datos de conexion sean correctos
    echo.
    pause
    exit /b 1
)

echo.
echo [4/4] Datos importados exitosamente a Railway
echo.

echo ========================================
echo MIGRACION COMPLETADA
echo ========================================
echo.
echo Archivo de backup guardado en: %BACKUP_FILE%
echo.
echo Los datos de tu base de datos local ahora estan en Railway MySQL
echo.
pause

