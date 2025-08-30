@echo off
echo ========================================
echo   Verificando Estructura de Tablas
echo ========================================
echo.

echo 1. Abriendo phpMyAdmin...
echo    Por favor ejecuta el siguiente SQL en phpMyAdmin:
echo.
echo    - Ve a http://localhost/phpmyadmin
echo    - Selecciona la base de datos 'agrocloud'
echo    - Ve a la pestaña 'SQL'
echo    - Copia y pega el contenido del archivo 'verificar-estructura.sql'
echo    - Haz clic en 'Continuar'
echo.

echo 2. Este script hará lo siguiente:
echo    - Mostrará la estructura de la tabla usuarios
echo    - Mostrará la estructura de la tabla roles
echo    - Mostrará la estructura de la tabla usuarios_roles
echo    - Contará los registros en cada tabla
echo.

echo 3. Con esta información podremos crear el script correcto.
echo.

pause
