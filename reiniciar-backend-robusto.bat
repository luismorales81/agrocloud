@echo off
echo ========================================
echo REINICIANDO BACKEND - VERSION ROBUSTA
echo ========================================
echo.

cd agrogestion-backend

echo [1] Deteniendo procesos en puerto 8080...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8080 ^| findstr LISTENING') do (
    echo Matando proceso %%a
    taskkill /PID %%a /F >nul 2>&1
)

echo [2] Esperando 3 segundos...
timeout /t 3 /nobreak >nul

echo [3] Limpiando proyecto...
call mvnw.cmd clean

echo [4] Compilando proyecto...
call mvnw.cmd compile

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo ✅ Compilacion exitosa!
    echo ========================================
    echo.
    echo [5] Iniciando servidor...
    echo.
    echo Espera a ver el mensaje: "Started AgroCloudBackendApplication"
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











