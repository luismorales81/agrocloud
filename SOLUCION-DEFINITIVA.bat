@echo off
cls
echo ========================================
echo SOLUCION DEFINITIVA - ROLES
echo ========================================
echo.
echo PROBLEMA: El backend sigue devolviendo roles antiguos
echo CAUSA: El backend no cargo los cambios de RolEmpresa.java
echo.
pause

echo.
echo [1/6] Deteniendo TODOS los servicios...
taskkill /F /IM java.exe 2>nul
taskkill /F /IM node.exe 2>nul
timeout /t 3 >nul

echo.
echo [2/6] Limpiando completamente el backend...
cd agrogestion-backend
if exist target rd /s /q target
if exist .mvn rd /s /q .mvn
cd ..

echo.
echo [3/6] Compilando backend COMPLETO (con install)...
echo   Esto tarda 2-3 minutos, es NORMAL
cd agrogestion-backend
call mvnw.cmd clean install -DskipTests
if %errorlevel% neq 0 (
    echo.
    echo ❌ ERROR: Compilacion fallo
    pause
    exit /b 1
)
cd ..

echo.
echo ✅ Backend compilado exitosamente
echo.

echo [4/6] Iniciando backend...
start "Backend - AgroCloud" cmd /k "cd agrogestion-backend && mvnw.cmd spring-boot:run"
echo   Esperando 30 segundos...
timeout /t 30 >nul

echo.
echo [5/6] Verificando que el backend inicio...
netstat -ano | findstr ":8080" | findstr "LISTENING"
if %errorlevel% neq 0 (
    echo ❌ Backend NO esta corriendo
    echo    Revisa la ventana "Backend - AgroCloud" para ver errores
    pause
    exit /b 1
)
echo ✅ Backend corriendo en puerto 8080

echo.
echo [6/6] Iniciando frontend...
start "Frontend - AgroCloud" cmd /k "cd agrogestion-frontend && npm start"

echo.
echo ========================================
echo ✅ COMPLETADO
echo ========================================
echo.
echo PASOS FINALES:
echo.
echo 1. Ve a la ventana "Frontend - AgroCloud"
echo    Espera ver: "VITE vX.X.X  ready in XXX ms"
echo.
echo 2. Abre navegador DE INCOGNITO (Ctrl+Shift+N)
echo.
echo 3. Ve a: http://localhost:5173
echo.
echo 4. Inicia sesion:
echo    Email: tecnico.juan@agrocloud.com
echo    Pass: admin123
echo.
echo 5. DEBE decir: "Rol: JEFE_CAMPO"
echo    (NO "TECNICO")
echo.
echo Si SIGUE diciendo "TECNICO":
echo - Revisa la ventana del backend
echo - Busca errores en el inicio
echo - Copia el error y avisame
echo.
echo ========================================
pause




