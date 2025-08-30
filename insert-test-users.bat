@echo off
echo ========================================
echo   Insertando Usuarios de Prueba
echo ========================================
echo.

echo 1. Abriendo phpMyAdmin...
echo    Por favor ejecuta el siguiente SQL en phpMyAdmin:
echo.
echo    - Ve a http://localhost/phpmyadmin
echo    - Selecciona la base de datos 'agrocloud'
echo    - Ve a la pestaña 'SQL'
echo    - Copia y pega el contenido del archivo 'insert-test-users.sql'
echo    - Haz clic en 'Continuar'
echo.

echo 2. O ejecuta directamente desde MySQL:
echo    mysql -u agrocloudbd -p agrocloud < insert-test-users.sql
echo.

echo 3. Usuarios que se crearán:
echo    - admin / admin123 (admin@agrocloud.com)
echo    - tecnico / tecnico123 (tecnico@agrocloud.com)
echo    - productor / productor123 (productor@agrocloud.com)
echo.

pause
