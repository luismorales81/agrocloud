@echo off
echo ========================================
echo INSERTAR INSUMOS Y RECETAS PARA PORCINOS
echo ========================================
echo.
echo Este script inserta los insumos basicos y recetas de alimentacion
echo para todas las etapas del ciclo productivo porcino.
echo.
echo Base de datos: agrocloud
echo Usuario: root
echo.
pause

cd /d "%~dp0"

echo.
echo Ejecutando script SQL...
echo.

Get-Content "INSERTAR_INSUMOS_Y_RECETAS_PORCINOS.sql" | & "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p123456 agrocloud

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo INSUMOS Y RECETAS INSERTADOS EXITOSAMENTE
    echo ========================================
) else (
    echo.
    echo ========================================
    echo ERROR AL EJECUTAR EL SCRIPT
    echo ========================================
)

pause
