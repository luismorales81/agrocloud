@echo off
cls
echo ========================================
echo REINICIAR BACKEND CON MAPEO DE ROLES
echo ========================================
echo.
echo Se corrigio EmpresaController.java para mapear:
echo   TECNICO → JEFE_CAMPO
echo   CONTADOR → JEFE_FINANCIERO
echo   LECTURA → CONSULTOR_EXTERNO
echo.
pause

echo.
echo [1/4] Deteniendo backend actual...
taskkill /F /IM java.exe 2>nul
timeout /t 3 >nul

echo.
echo [2/4] Limpiando target...
cd agrogestion-backend
if exist target rd /s /q target
cd ..

echo.
echo [3/4] Compilando con cambios...
cd agrogestion-backend
echo   Compilando... (tarda 1-2 minutos)
call mvnw.cmd clean install -DskipTests

if %errorlevel% neq 0 (
    echo.
    echo ❌ ERROR en compilacion
    pause
    exit /b 1
)

echo.
echo ✅ Compilacion exitosa
cd ..

echo.
echo [4/4] Iniciando backend con cambios...
cd agrogestion-backend
echo.
echo ========================================
echo Backend iniciando...
echo ========================================
echo.
echo Espera ver: "Started AgroGestionApplication"
echo.
call mvnw.cmd spring-boot:run

pause



