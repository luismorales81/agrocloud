@echo off
echo ========================================
echo    Backend AgroCloud con MySQL
echo ========================================
echo.

echo 1. Verificando que MySQL esté ejecutándose...
netstat -ano | findstr ":3306" > nul
if %errorlevel% neq 0 (
    echo ❌ ERROR: MySQL no está ejecutándose en el puerto 3306
    echo    Asegúrate de que XAMPP esté iniciado y MySQL esté corriendo
    pause
    exit /b 1
)
echo ✅ MySQL está ejecutándose en el puerto 3306

echo.
echo 2. Iniciando Backend con perfil MySQL...
cd agrogestion-backend
call mvnw spring-boot:run -Dspring.profiles.active=mysql

echo.
echo Backend iniciado con MySQL en: http://localhost:8080
pause
