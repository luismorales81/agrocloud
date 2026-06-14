@echo off
REM Script para ejecutar la actualización de nombres de lotes de recría
REM Ajusta estos valores según tu configuración
SET DB_NAME=agrocloud
SET DB_USER=root
SET DB_PASS=-p123456

echo ========================================
echo Actualizando nombres de lotes de recría
echo ========================================
echo.

cd /d "%~dp0"

REM Ejecutar el script SQL
Get-Content "ACTUALIZAR_NOMBRES_LOTES_RECRIA.sql" | & "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u %DB_USER% %DB_PASS% %DB_NAME%

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo Actualización completada exitosamente
    echo ========================================
) else (
    echo.
    echo ========================================
    echo ERROR: La actualización falló
    echo ========================================
    pause
)
