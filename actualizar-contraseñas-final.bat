@echo off
echo ========================================
echo   Actualizando Contraseñas (Final)
echo ========================================
echo.

echo 1. Abriendo phpMyAdmin...
echo    Por favor ejecuta el siguiente SQL en phpMyAdmin:
echo.
echo    - Ve a http://localhost/phpmyadmin
echo    - Selecciona la base de datos 'agrocloud'
echo    - Ve a la pestaña 'SQL'
echo    - Copia y pega el contenido del archivo 'actualizar-contraseñas-final.sql'
echo    - Haz clic en 'Continuar'
echo.

echo 2. Este script hará lo siguiente:
echo    - Actualizará las contraseñas con hashes BCrypt correctos generados por el backend
echo    - Verificará que se actualizaron correctamente
echo.

echo 3. Contraseñas que se actualizarán:
echo    - admin / admin123
echo    - tecnico / tecnico123
echo    - productor / productor123
echo.

echo 4. Después de ejecutar el SQL, el login debería funcionar correctamente.
echo.

pause
