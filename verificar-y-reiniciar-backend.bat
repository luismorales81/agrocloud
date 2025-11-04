@echo off
echo ========================================
echo VERIFICANDO Y REINICIANDO BACKEND
echo ========================================

echo.
echo 1. Deteniendo procesos Java existentes...
taskkill /F /IM java.exe 2>nul
timeout /t 3 /nobreak >nul

echo.
echo 2. Compilando proyecto...
cd agrogestion-backend
call mvnw clean compile -q
if %errorlevel% neq 0 (
    echo ERROR: Fallo en la compilacion
    pause
    exit /b 1
)

echo.
echo 3. Iniciando backend...
start "Backend AgroGestion" cmd /k "mvnw spring-boot:run"

echo.
echo 4. Esperando que el backend inicie...
timeout /t 15 /nobreak >nul

echo.
echo 5. Verificando estado del backend...
curl -s http://localhost:8080/api/v1/health >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ BACKEND FUNCIONANDO CORRECTAMENTE
    echo URL: http://localhost:8080
    echo Swagger: http://localhost:8080/swagger-ui.html
) else (
    echo ❌ BACKEND NO RESPONDE
    echo Revisa los logs en la ventana del backend
)

echo.
echo ========================================
pause
