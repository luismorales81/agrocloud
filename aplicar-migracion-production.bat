@echo off
cls
color 4F
echo ============================================================
echo  MIGRACION DE BASE DE DATOS - PRODUCTION
echo  AgroGestion v2.0
echo ============================================================
echo.
echo  *** ADVERTENCIA CRITICA ***
echo.
echo Este script ELIMINARA todas las tablas existentes
echo y las recreara desde cero.
echo.
echo  TODOS LOS DATOS DE PRODUCCION SE PERDERAN!
echo.
echo Si tienes datos importantes, haz un BACKUP PRIMERO:
echo   mysqldump -u root -p agrogestion_db ^> backup.sql
echo.
echo Este script creara:
echo   - Estructura de tablas (19 tablas)
echo   - Roles del sistema (13 roles)
echo   - Cultivos base (14 cultivos)
echo   - 1 usuario administrador (password temporal)
echo   - 1 empresa (debes actualizar los datos)
echo.
echo NO incluye datos de prueba.
echo.
echo ============================================================
echo.
set /p CONFIRM=Estas ABSOLUTAMENTE SEGURO? (escribe SI para continuar): 

if /i NOT "%CONFIRM%"=="SI" (
    echo.
    echo Operacion cancelada.
    pause
    exit /b
)

echo.
echo Ejecutando migracion de PRODUCCION...
echo.

mysql -u root -p agrogestion_db < database-migration-production-completo.sql

if %ERRORLEVEL% EQU 0 (
    color 0A
    echo.
    echo ============================================================
    echo  MIGRACION COMPLETADA EXITOSAMENTE
    echo ============================================================
    echo.
    echo CREDENCIALES TEMPORALES:
    echo   Email: admin@tudominio.com
    echo   Password: Admin2025!Temp
    echo.
    echo ============================================================
    echo  TAREAS PENDIENTES:
    echo ============================================================
    echo.
    echo 1. Cambiar la contraseña del administrador INMEDIATAMENTE
    echo 2. Actualizar el email del administrador
    echo 3. Actualizar los datos de la empresa
    echo 4. Configurar backups automaticos
    echo 5. Crear usuarios adicionales segun sea necesario
    echo.
    echo ============================================================
) else (
    echo.
    echo ============================================================
    echo  ERROR EN LA MIGRACION
    echo ============================================================
    echo.
)

color
pause

