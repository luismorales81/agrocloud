@echo off
echo ========================================
echo Compilando Backend Limpiamente
echo ========================================
echo.

cd agrogestion-backend

echo [1/3] Limpiando target...
rd /s /q target 2>nul
rd /s /q .mvn 2>nul

echo.
echo [2/3] Compilando proyecto...
call mvnw.cmd clean install -DskipTests

if %errorlevel% neq 0 (
    echo.
    echo ❌ Error en compilación
    echo.
    pause
    exit /b 1
)

echo.
echo [3/3] Iniciando backend...
echo.
echo ✅ Compilación exitosa
echo ✅ Iniciando aplicación...
echo.

call mvnw.cmd spring-boot:run

pause




