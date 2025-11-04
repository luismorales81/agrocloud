@echo off
echo ========================================
echo Reiniciando Backend
echo ========================================
echo.

cd agrogestion-backend

echo [1] Limpiando proyecto...
call mvnw.cmd clean

echo [2] Compilando proyecto...
call mvnw.cmd compile

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo ✅ Compilacion exitosa!
    echo ========================================
    echo.
    echo [3] Iniciando servidor...
    echo.
    call mvnw.cmd spring-boot:run
) else (
    echo.
    echo ========================================
    echo ❌ Error en la compilacion
    echo ========================================
    echo.
    pause
)

pause











