@echo off
cls
echo ============================================================
echo  BACKUP DE BASE DE DATOS
echo  Antes de Migrar
echo ============================================================
echo.

set DB_NAME=agrogestion_db
set BACKUP_DIR=backups
set TIMESTAMP=%date:~-4%%date:~3,2%%date:~0,2%_%time:~0,2%%time:~3,2%%time:~6,2%
set TIMESTAMP=%TIMESTAMP: =0%
set BACKUP_FILE=%BACKUP_DIR%\backup_pre_migracion_%TIMESTAMP%.sql

if not exist %BACKUP_DIR% mkdir %BACKUP_DIR%

echo Generando backup de la base de datos...
echo.
echo Base de datos: %DB_NAME%
echo Archivo destino: %BACKUP_FILE%
echo.

mysqldump -u root -p --single-transaction --quick --lock-tables=false %DB_NAME% > %BACKUP_FILE%

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ============================================================
    echo  BACKUP COMPLETADO EXITOSAMENTE
    echo ============================================================
    echo.
    echo Archivo guardado en: %BACKUP_FILE%
    echo.
    echo Para restaurar este backup:
    echo   mysql -u root -p %DB_NAME% ^< %BACKUP_FILE%
    echo.
    echo Ahora puedes ejecutar la migracion de forma segura.
    echo.
    echo ============================================================
) else (
    echo.
    echo ============================================================
    echo  ERROR AL CREAR BACKUP
    echo ============================================================
    echo.
    echo Verifica:
    echo - MySQL esta corriendo
    echo - La base de datos existe
    echo - La contraseña es correcta
    echo.
    echo NO CONTINUAR CON LA MIGRACION SIN UN BACKUP EXITOSO
    echo.
)

pause

