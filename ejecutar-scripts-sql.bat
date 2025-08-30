@echo off
echo ========================================
echo   Ejecutar Scripts SQL en phpMyAdmin
echo ========================================
echo.
echo 1. Abre tu navegador y ve a: http://localhost/phpmyadmin
echo 2. Inicia sesion con:
echo    - Usuario: agrocloudbd
echo    - Contraseña: Jones1212
echo 3. Selecciona la base de datos: agrocloud
echo 4. Ve a la pestaña "SQL"
echo 5. Copia y pega el contenido de los siguientes archivos:
echo.
echo ========================================
echo SCRIPT 1: create-ingresos-table-clean.sql
echo ========================================
echo.
type agrogestion-backend\src\main\resources\create-ingresos-table-clean.sql
echo.
echo ========================================
echo SCRIPT 2: create-egresos-table-simple.sql
echo ========================================
echo.
type agrogestion-backend\src\main\resources\create-egresos-table-simple.sql
echo.
echo ========================================
echo INSTRUCCIONES:
echo ========================================
echo 1. Ejecuta el primer script (ingresos)
echo 2. Ejecuta el segundo script (egresos)
echo 3. Verifica que ambas tablas se crearon
echo 4. Regresa a la aplicacion y prueba los formularios
echo.
pause
