@echo off
echo ========================================
echo Ejecutando script SQL para tablas de agroquimicos
echo ========================================
echo.

REM Configuración de la base de datos
set DB_HOST=localhost
set DB_PORT=3306
set DB_NAME=agrocloud
set DB_USER=root
set DB_PASS=

echo Conectando a MySQL...
echo Base de datos: %DB_NAME%
echo.

REM Ejecutar el script SQL
"C:\xampp\mysql\bin\mysql.exe" -h %DB_HOST% -P %DB_PORT% -u %DB_USER% %DB_NAME% < agrogestion-backend\src\main\resources\db\migration\create_tablas_aplicaciones_agroquimicas.sql

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo ✅ Tablas creadas exitosamente!
    echo ========================================
    echo.
    echo Tablas creadas:
    echo - dosis_aplicacion
    echo - aplicaciones_agroquimicas
    echo.
) else (
    echo.
    echo ========================================
    echo ❌ Error al ejecutar el script SQL
    echo ========================================
    echo.
)

pause











