@echo off
echo ========================================
echo DIAGNOSTICO BACKEND AGROGESTION
echo ========================================

echo.
echo 1. Verificando MySQL...
netstat -an | findstr :3306
if %errorlevel% neq 0 (
    echo ❌ MySQL no esta ejecutandose
    pause
    exit /b 1
) else (
    echo ✅ MySQL esta ejecutandose
)

echo.
echo 2. Compilando proyecto...
call mvnw clean compile -q
if %errorlevel% neq 0 (
    echo ❌ Error en compilacion
    pause
    exit /b 1
) else (
    echo ✅ Compilacion exitosa
)

echo.
echo 3. Iniciando backend con logs detallados...
echo Presiona Ctrl+C para detener
echo.
call mvnw spring-boot:run
