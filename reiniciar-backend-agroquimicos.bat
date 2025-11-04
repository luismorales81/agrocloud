@echo off
echo ========================================
echo Reiniciando backend para reconocer nuevas entidades
echo ========================================
echo.

cd agrogestion-backend

echo Compilando proyecto...
call mvnw.cmd clean compile

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo ✅ Compilacion exitosa!
    echo ========================================
    echo.
    echo Iniciando servidor...
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











