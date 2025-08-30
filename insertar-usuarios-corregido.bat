@echo off
echo ========================================
echo   Insertando Usuarios (Script Corregido)
echo ========================================
echo.

echo 1. Abriendo phpMyAdmin...
echo    Por favor ejecuta el siguiente SQL en phpMyAdmin:
echo.
echo    - Ve a http://localhost/phpmyadmin
echo    - Selecciona la base de datos 'agrocloud'
echo    - Ve a la pestaña 'SQL'
echo    - Copia y pega el contenido del archivo 'insertar-usuarios-corregido.sql'
echo    - Haz clic en 'Continuar'
echo.

echo 2. Este script hará lo siguiente:
echo    - Insertará roles si no existen
echo    - Insertará usuarios de prueba si no existen
echo    - Asignará roles a los usuarios (con nombres de columnas correctos)
echo    - Mostrará el resultado final
echo.

echo 3. Usuarios que se crearán/asignarán:
echo    - admin / admin123 (admin@agrocloud.com) - ADMINISTRADOR
echo    - tecnico / tecnico123 (tecnico@agrocloud.com) - INGENIERO_AGRONOMO
echo    - productor / productor123 (productor@agrocloud.com) - OPERARIO
echo.

echo 4. Después de ejecutar el SQL, el sistema estará listo para usar.
echo.

pause
