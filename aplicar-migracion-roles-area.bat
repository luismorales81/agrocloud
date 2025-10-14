@echo off
echo ========================================
echo Aplicando Migración: Roles por Área
echo ========================================
echo.

echo Este script actualizará los roles a la nueva estructura:
echo - PRODUCTOR + ASESOR + TECNICO → JEFE_CAMPO
echo - CONTADOR → JEFE_FINANCIERO
echo - LECTURA → CONSULTOR_EXTERNO
echo.

pause

"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p agrocloud < migrar-roles-por-area.sql

if %errorlevel% equ 0 (
    echo.
    echo ✅ Migración completada exitosamente
    echo.
) else (
    echo.
    echo ❌ Error al aplicar la migración
    echo.
)

pause
