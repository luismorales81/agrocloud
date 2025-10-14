@echo off
echo ========================================
echo   Corrigiendo tabla INGRESOS
echo ========================================
echo.
echo Este script corregira la columna 'tipo' a 'tipo_ingreso'
echo.
pause

echo Ejecutando script SQL...
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root agrocloud < corregir-tabla-ingresos.sql

if %errorlevel% equ 0 (
    echo.
    echo ========================================
    echo   EXITO: Tabla corregida
    echo ========================================
) else (
    echo.
    echo ========================================
    echo   ERROR: Revisa el mensaje anterior
    echo ========================================
)

echo.
pause

