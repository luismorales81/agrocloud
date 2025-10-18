@echo off
echo ========================================
echo REINICIAR BACKEND - HEALTH CHECK
echo ========================================
echo.

echo [1/3] Deteniendo procesos Java existentes...
taskkill /F /IM java.exe 2>nul
timeout /t 2 /nobreak >nul

echo.
echo [2/3] Limpiando proyecto Maven...
cd agrogestion-backend
call mvnw.cmd clean
if %errorlevel% neq 0 (
    echo Error al limpiar el proyecto
    pause
    exit /b 1
)

echo.
echo [3/3] Compilando e iniciando backend...
echo.
call mvnw.cmd spring-boot:run

