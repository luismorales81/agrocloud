@echo off
cls
color 0E
echo ============================================================
echo  MIGRACION DE BASE DE DATOS - TESTING
echo  AgroGestion v2.0
echo ============================================================
echo.
echo  *** ADVERTENCIA ***
echo.
echo Este script ELIMINARA todas las tablas existentes
echo y las recreara desde cero.
echo.
echo  TODOS LOS DATOS SE PERDERAN!
echo.
echo Creara:
echo   - Estructura de tablas (19 tablas)
echo   - Roles del sistema (13 roles)
echo   - Cultivos base (14 cultivos)
echo   - Usuarios de prueba (5 usuarios)
echo   - Datos de ejemplo (campos, lotes, insumos)
echo.
echo ============================================================
echo.
set /p CONFIRM=Estas SEGURO de continuar? (escribe SI para confirmar): 

if /i NOT "%CONFIRM%"=="SI" (
    echo.
    echo Operacion cancelada.
    color
    pause
    exit /b
)

color 07
echo.
echo Continuando con la migracion...
echo.
pause

echo.
echo Ejecutando migracion...
echo.

mysql -u root -p agrogestion_db < database-migration-testing-completo.sql

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ============================================================
    echo  MIGRACION COMPLETADA EXITOSAMENTE
    echo ============================================================
    echo.
    echo Credenciales de Testing:
    echo.
    echo Admin:          admin.testing@agrogestion.com / password123
    echo Jefe Campo:     jefe.campo@agrogestion.com / password123
    echo Jefe Financiero: jefe.financiero@agrogestion.com / password123
    echo Operario:       operario.test@agrogestion.com / password123
    echo Consultor:      consultor.test@agrogestion.com / password123
    echo.
    echo ============================================================
) else (
    echo.
    echo ============================================================
    echo  ERROR EN LA MIGRACION
    echo ============================================================
    echo.
    echo Verifica:
    echo - MySQL esta corriendo
    echo - La contraseña es correcta
    echo - Los archivos SQL estan en el directorio actual
    echo.
)

pause

