@echo off
echo ========================================
echo REINICIO COMPLETO CON CAMBIOS DE ROLES
echo ========================================
echo.

echo [1/5] Deteniendo servicios existentes...
taskkill /F /IM node.exe 2>nul
taskkill /F /IM java.exe 2>nul
timeout /t 3 >nul

echo.
echo [2/5] Limpiando caches...
cd agrogestion-frontend
if exist node_modules\.cache rd /s /q node_modules\.cache
if exist build rd /s /q build
cd ..

echo.
echo [3/5] Compilando Backend...
cd agrogestion-backend
call mvn clean compile
cd ..

echo.
echo [4/5] Iniciando Backend...
start "Backend AgroGestion" cmd /k "cd agrogestion-backend && mvn spring-boot:run"
echo ✅ Backend iniciando... Espera 30 segundos
timeout /t 30 >nul

echo.
echo [5/5] Iniciando Frontend...
start "Frontend AgroGestion" cmd /k "cd agrogestion-frontend && npm start"

echo.
echo ========================================
echo ✅ SERVICIOS INICIADOS
echo ========================================
echo.
echo IMPORTANTE - SIGUE ESTOS PASOS:
echo.
echo 1. Espera a que el backend muestre: "Started AgroGestionApplication"
echo 2. Espera a que el frontend muestre: "webpack compiled successfully"
echo 3. Ve al navegador y presiona Ctrl+Shift+Delete
echo 4. Borra TODO (cookies, cache, historial)
echo 5. Recarga la pagina (F5)
echo 6. Inicia sesion de nuevo
echo.
echo ========================================
pause




