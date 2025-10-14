@echo off
cls
echo ========================================
echo  INICIO LIMPIO - AGROCLOUD
echo ========================================
echo.

echo [PASO 1/5] Deteniendo procesos existentes...
taskkill /F /IM node.exe 2>nul
taskkill /F /IM java.exe 2>nul
timeout /t 2 >nul

echo.
echo [PASO 2/5] Limpiando carpeta target del backend...
cd agrogestion-backend
if exist target rd /s /q target
cd ..

echo.
echo [PASO 3/5] Compilando backend...
cd agrogestion-backend
echo   Esto puede tardar 1-2 minutos...
call mvnw.cmd clean compile -DskipTests
if %errorlevel% neq 0 (
    echo.
    echo ❌ ERROR: No se pudo compilar el backend
    pause
    exit /b 1
)
cd ..

echo.
echo [PASO 4/5] Iniciando BACKEND en ventana separada...
start "Backend - AgroCloud" cmd /k "cd agrogestion-backend && mvnw.cmd spring-boot:run"
echo   ⏳ Esperando 20 segundos para que el backend inicie...
timeout /t 20 >nul

echo.
echo [PASO 5/5] Iniciando FRONTEND en ventana separada...
start "Frontend - AgroCloud" cmd /k "cd agrogestion-frontend && npm start"

echo.
echo ========================================
echo ✅ TODO INICIADO
echo ========================================
echo.
echo 📋 INSTRUCCIONES:
echo.
echo 1. Ventana "Backend - AgroCloud":
echo    Espera ver: "Started AgroGestionApplication"
echo.
echo 2. Ventana "Frontend - AgroCloud":
echo    Espera ver: "VITE vX.X.X  ready in XXX ms"
echo.
echo 3. Abre NAVEGADOR DE INCOGNITO (Ctrl+Shift+N)
echo.
echo 4. Ve a: http://localhost:5173
echo    (NOTA: Vite usa puerto 5173, NO 3000)
echo.
echo 5. Inicia sesion con:
echo    Email: tecnico.juan@agrocloud.com
echo    Password: admin123
echo.
echo 6. Verifica que diga: "Rol: JEFE_CAMPO"
echo.
echo ========================================
pause




