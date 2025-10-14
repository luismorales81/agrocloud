@echo off
chcp 65001 > nul
echo ========================================
echo   Sincronizando Roles en Base de Datos
echo ========================================
echo.

echo Ejecutando script SQL...
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p123456 -D agrocloud < sincronizar-roles.sql

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ✅ Roles sincronizados correctamente
    echo.
    echo Roles ACTIVOS en el sistema:
    echo   1. SUPERADMIN
    echo   2. ADMINISTRADOR
    echo   3. PRODUCTOR
    echo   4. ASESOR
    echo   5. CONTADOR
    echo   6. TECNICO
    echo   7. OPERARIO
    echo   8. LECTURA
    echo   9. INVITADO
    echo.
    echo Roles DESACTIVADOS:
    echo   - ADMIN (duplicado de ADMINISTRADOR)
    echo   - USUARIO_REGISTRADO (sin función definida)
    echo.
) else (
    echo.
    echo ❌ Error al sincronizar roles
    echo.
)

pause
