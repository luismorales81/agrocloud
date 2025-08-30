@echo off
echo ========================================
echo   VERIFICAR TABLAS EN LA BASE DE DATOS
echo ========================================
echo.
echo Ejecuta este script en phpMyAdmin para verificar si las tablas existen:
echo.
echo 1. Abre: http://localhost/phpmyadmin
echo 2. Inicia sesion con:
echo    - Usuario: agrocloudbd
echo    - Contraseña: Jones1212
echo 3. Selecciona la base de datos: agrocloud
echo 4. Ve a la pestaña "SQL"
echo 5. Copia y pega el siguiente script:
echo.
echo ========================================
echo SCRIPT DE VERIFICACION:
echo ========================================
echo.
type verificar-tablas.sql
echo.
echo ========================================
echo INSTRUCCIONES:
echo ========================================
echo 1. Ejecuta el script
echo 2. Si las tablas NO existen, ejecuta los scripts de creacion
echo 3. Si las tablas existen, el problema es otro
echo.
pause
