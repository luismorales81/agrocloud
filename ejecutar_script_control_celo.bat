@echo off
REM Script para ejecutar SCRIPT_PRUEBA_CONTROL_CELO.sql
REM Ajusta las credenciales según tu configuración

echo ========================================
echo Ejecutando Script de Prueba Control Celo
echo ========================================
echo.

REM Ajusta estos valores según tu configuración
SET DB_NAME=agrocloud
SET DB_USER=root
SET DB_PASS=-p123456

cd agrogestion-backend

REM Ejecutar el script SQL
mysql -u %DB_USER% %DB_PASS% %DB_NAME% < SCRIPT_PRUEBA_CONTROL_CELO.sql

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo Script ejecutado exitosamente
    echo ========================================
) else (
    echo.
    echo ========================================
    echo Error al ejecutar el script
    echo Verifica las credenciales de MySQL
    echo ========================================
)

pause
