@echo off
echo ========================================
echo   Verificando e Insertando Usuarios
echo ========================================
echo.

echo 1. Abriendo phpMyAdmin...
echo    Por favor ejecuta el siguiente SQL en phpMyAdmin:
echo.
echo    - Ve a http://localhost/phpmyadmin
echo    - Selecciona la base de datos 'agrocloud'
echo    - Ve a la pestaña 'SQL'
echo    - Copia y pega el contenido del archivo 'verificar-y-insertar-usuarios.sql'
echo    - Haz clic en 'Continuar'
echo.

echo 2. Este script hará lo siguiente:
echo    - Verificará la estructura de las tablas
echo    - Insertará roles si no existen
echo    - Insertará usuarios de prueba si no existen
echo    - Asignará roles a los usuarios
echo    - Mostrará el resultado final
echo.

echo 3. Usuarios que se crearán:
echo    - admin / admin123 (admin@agrocloud.com)
echo    - tecnico / tecnico123 (tecnico@agrocloud.com)
echo    - productor / productor123 (productor@agrocloud.com)
echo.

echo 4. Después de ejecutar el SQL, reinicia el backend para que cargue los usuarios.
echo.

pause
