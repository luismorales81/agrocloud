@echo off
echo ========================================
echo    AgroCloud - Estado de Servicios
echo ========================================
echo.

echo 1. Verificando MySQL (puerto 3306)...
netstat -ano | findstr ":3306" > nul
if %errorlevel% equ 0 (
    echo    ✅ MySQL está ejecutándose
) else (
    echo    ❌ MySQL NO está ejecutándose
    echo       Inicia XAMPP y activa MySQL
)

echo.
echo 2. Verificando Backend (puerto 8080)...
netstat -ano | findstr ":8080" > nul
if %errorlevel% equ 0 (
    echo    ✅ Backend está ejecutándose
    echo    🔗 URL: http://localhost:8080
) else (
    echo    ❌ Backend NO está ejecutándose
)

echo.
echo 3. Verificando Frontend (puerto 3000)...
netstat -ano | findstr ":3000" > nul
if %errorlevel% equ 0 (
    echo    ✅ Frontend está ejecutándose
    echo    🔗 URL: http://localhost:3000
) else (
    echo    ❌ Frontend NO está ejecutándose
)

echo.
echo 4. Verificando conectividad del Backend...
curl -s http://localhost:8080/api/health > nul 2>&1
if %errorlevel% equ 0 (
    echo    ✅ Backend responde correctamente
) else (
    echo    ⚠️  Backend no responde (puede estar iniciando)
)

echo.
echo ========================================
echo 📊 Resumen del Estado:
echo ========================================
echo.

if exist "temp_status.txt" del "temp_status.txt"

echo Estado de Servicios > temp_status.txt
echo ==================== >> temp_status.txt
echo. >> temp_status.txt

netstat -ano | findstr ":3306" > nul && echo "MySQL:     ✅ ACTIVO" >> temp_status.txt || echo "MySQL:     ❌ INACTIVO" >> temp_status.txt
netstat -ano | findstr ":8080" > nul && echo "Backend:   ✅ ACTIVO" >> temp_status.txt || echo "Backend:   ❌ INACTIVO" >> temp_status.txt
netstat -ano | findstr ":3000" > nul && echo "Frontend:  ✅ ACTIVO" >> temp_status.txt || echo "Frontend:  ❌ INACTIVO" >> temp_status.txt

type temp_status.txt
del temp_status.txt

echo.
echo ========================================
echo 🔧 Comandos útiles:
echo ========================================
echo.
echo Para iniciar todos los servicios:
echo    start-project-mysql.bat
echo.
echo Para detener todos los servicios:
echo    stop-project.bat
echo.
echo Para verificar la base de datos:
echo    http://localhost/phpmyadmin
echo.
echo Presiona cualquier tecla para cerrar...
pause
