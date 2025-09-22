@echo off
echo ========================================
echo    AgroCloud - Iniciando Proyecto
echo ========================================
echo.

echo 1. Verificando Java...
java -version
if %errorlevel% neq 0 (
    echo ERROR: Java no esta instalado
    pause
    exit /b 1
)
echo Java OK

echo.
echo 2. Iniciando Backend...
cd agrogestion-backend
start "Backend AgroCloud" cmd /k "mvnw.cmd spring-boot:run"
cd ..

echo.
echo 3. Esperando 15 segundos...
timeout /t 15 /nobreak > nul

echo.
echo 4. Iniciando Frontend...
cd agrogestion-frontend
start "Frontend AgroCloud" cmd /k "npm run dev"
cd ..

echo.
echo ========================================
echo Servicios iniciados:
echo ========================================
echo Frontend: http://localhost:3000
echo Backend:  http://localhost:8080
echo.
echo Presiona cualquier tecla para cerrar...
pause

