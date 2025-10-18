@echo off
echo ========================================
echo REINICIAR BACKEND - RAPIDO
echo ========================================
echo.

echo [1/2] Deteniendo procesos Java existentes...
taskkill /F /IM java.exe 2>nul
if %errorlevel% equ 0 (
    echo Backend detenido exitosamente
) else (
    echo No se encontraron procesos Java corriendo
)
timeout /t 2 /nobreak >nul

echo.
echo [2/2] Iniciando backend...
echo.
echo NOTA: El backend se iniciara en segundo plano.
echo Para ver los logs, abre otra terminal y ejecuta:
echo   cd agrogestion-backend
echo   call mvnw.cmd spring-boot:run
echo.
echo Esperando 10 segundos para que el backend inicie...
echo.

cd agrogestion-backend
start /B call mvnw.cmd spring-boot:run

timeout /t 10 /nobreak

echo.
echo ========================================
echo BACKEND REINICIADO
echo ========================================
echo.
echo Probando health check...
echo.

curl http://localhost:8080/actuator/health

echo.
echo.
echo Si ves {"status":"UP"}, el health check esta funcionando!
echo.
pause

