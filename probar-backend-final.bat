@echo off
echo ========================================
echo PRUEBA FINAL BACKEND AGROGESTION
echo ========================================

echo.
echo 1. Deteniendo procesos Java existentes...
taskkill /F /IM java.exe 2>nul
timeout /t 3 /nobreak >nul

echo.
echo 2. Compilando proyecto...
call mvnw clean compile -q
if %errorlevel% neq 0 (
    echo ❌ ERROR: Fallo en compilacion
    pause
    exit /b 1
)
echo ✅ Compilacion exitosa

echo.
echo 3. Iniciando backend...
start "Backend AgroGestion" cmd /k "mvnw spring-boot:run"

echo.
echo 4. Esperando 20 segundos para que inicie...
timeout /t 20 /nobreak >nul

echo.
echo 5. Probando endpoints...
echo.
echo Probando health endpoint:
curl -s http://localhost:8080/api/v1/health
echo.
echo.
echo Probando swagger:
curl -s http://localhost:8080/swagger-ui.html | findstr "title"
echo.
echo.
echo Probando endpoint de empresas:
curl -s http://localhost:8080/api/v1/empresas

echo.
echo ========================================
echo Si ves respuestas arriba, el backend esta funcionando
echo ========================================
pause
