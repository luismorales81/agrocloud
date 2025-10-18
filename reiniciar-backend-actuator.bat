@echo off
echo ========================================
echo REINICIAR BACKEND - ACTUATOR FIX
echo ========================================
echo.

echo [1/2] Deteniendo procesos Java existentes...
taskkill /F /IM java.exe 2>nul
timeout /t 3 /nobreak >nul

echo.
echo [2/2] Iniciando backend...
echo.
echo NOTA: Espera a que veas "Started AgroCloudApplication"
echo Luego presiona Ctrl+C para detener y probar el health check
echo.

cd agrogestion-backend
call mvnw.cmd spring-boot:run

