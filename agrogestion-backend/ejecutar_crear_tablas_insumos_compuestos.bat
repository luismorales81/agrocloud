@echo off
echo ============================================================================
echo EJECUTANDO SCRIPT PARA CREAR TABLAS DE INSUMOS COMPUESTOS
echo ============================================================================
echo.

REM Ruta de MySQL en XAMPP (ajusta según tu instalación)
set MYSQL_PATH=C:\xampp\mysql\bin\mysql.exe

REM Verificar si existe MySQL en la ruta estándar de XAMPP
if not exist "%MYSQL_PATH%" (
    echo ERROR: No se encontró MySQL en la ruta estándar de XAMPP.
    echo Por favor, ajusta la variable MYSQL_PATH en este archivo .bat
    echo o ejecuta el script SQL manualmente desde phpMyAdmin.
    echo.
    pause
    exit /b 1
)

echo Ejecutando script SQL...
echo.

"%MYSQL_PATH%" -u root -p123456 agrocloud < "%~dp0CREAR_TABLAS_INSUMOS_COMPUESTOS.sql"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ============================================================================
    echo ✓ TABLAS CREADAS EXITOSAMENTE
    echo ============================================================================
    echo.
    echo Las siguientes tablas han sido creadas:
    echo   - insumos_compuestos
    echo   - componentes_insumo_compuesto
    echo   - porcinos_recetas_alimentacion_etapa
    echo.
    echo Ahora puedes reiniciar la aplicación Spring Boot.
    echo.
) else (
    echo.
    echo ============================================================================
    echo ✗ ERROR AL EJECUTAR EL SCRIPT
    echo ============================================================================
    echo.
    echo Por favor, verifica:
    echo   1. Que MySQL esté ejecutándose en XAMPP
    echo   2. Que la base de datos 'agrocloud' exista
    echo   3. Que el usuario 'root' tenga la contraseña '123456'
    echo   4. Que la ruta de MySQL sea correcta
    echo.
    echo Alternativamente, puedes ejecutar el script manualmente desde phpMyAdmin:
    echo   1. Abre http://localhost/phpmyadmin
    echo   2. Selecciona la base de datos 'agrocloud'
    echo   3. Ve a la pestaña "SQL"
    echo   4. Copia y pega el contenido de CREAR_TABLAS_INSUMOS_COMPUESTOS.sql
    echo   5. Haz clic en "Ejecutar"
    echo.
)

pause

