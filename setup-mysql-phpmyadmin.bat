@echo off
echo ========================================
echo    Configurando MySQL con phpMyAdmin
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
echo 2. Abriendo phpMyAdmin...
echo    URL: http://localhost/phpmyadmin
echo.
echo 3. Pasos para configurar la base de datos:
echo    a) Abre http://localhost/phpmyadmin en tu navegador
echo    b) Inicia sesión con usuario 'root' (sin contraseña por defecto)
echo    c) Ve a la pestaña 'SQL'
echo    d) Copia y pega el contenido del archivo 'setup-mysql-simple.sql'
echo    e) Haz clic en 'Continuar' para ejecutar el script
echo.
echo 4. Credenciales que se crearán:
echo    - Base de datos: agroclouddb
echo    - Usuario: agrocloudbd
echo    - Contraseña: Jones1212
echo.

start http://localhost/phpmyadmin

echo.
echo ========================================
echo    Configuración Manual Completada
echo ========================================
echo.
echo Una vez que hayas ejecutado el script SQL en phpMyAdmin,
echo puedes ejecutar el sistema con MySQL usando:
echo    start-project-mysql.bat
echo.
pause
