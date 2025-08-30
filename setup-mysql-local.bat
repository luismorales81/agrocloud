@echo off
echo ========================================
echo    Configurando MySQL Local (XAMPP)
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
echo 2. Configurando base de datos agroclouddb...
echo    - Base de datos: agroclouddb
echo    - Usuario: agrocloudbd
echo    - Contraseña: Jones1212

echo.
echo 3. Ejecutando script SQL...
mysql -u root -p -e "source agrogestion-backend/src/main/resources/setup-agroclouddb-complete.sql"

if %errorlevel% equ 0 (
    echo ✅ Base de datos configurada exitosamente
) else (
    echo ❌ Error configurando la base de datos
    echo    Verifica que tengas acceso a MySQL como root
)

echo.
echo ========================================
echo    Configuración MySQL Completada
echo ========================================
echo.
echo Credenciales de conexión:
echo - Host: localhost:3306
echo - Base de datos: agroclouddb
echo - Usuario: agrocloudbd
echo - Contraseña: Jones1212
echo.
pause
