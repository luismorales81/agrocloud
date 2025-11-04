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
set DB_PASS=123456

echo Conectando a MySQL...
echo Base de datos: %DB_NAME%
echo.

REM Intentar diferentes rutas de MySQL
set MYSQL_PATH=

REM Ruta 1: XAMPP
if exist "C:\xampp\mysql\bin\mysql.exe" (
    set MYSQL_PATH=C:\xampp\mysql\bin\mysql.exe
    goto :ejecutar
)

REM Ruta 2: XAMPP alternativa
if exist "C:\Program Files\xampp\mysql\bin\mysql.exe" (
    set MYSQL_PATH=C:\Program Files\xampp\mysql\bin\mysql.exe
    goto :ejecutar
)

REM Ruta 3: MySQL instalado directamente
if exist "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" (
    set MYSQL_PATH=C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe
    goto :ejecutar
)

REM Ruta 4: MySQL Server 8.1
if exist "C:\Program Files\MySQL\MySQL Server 8.1\bin\mysql.exe" (
    set MYSQL_PATH=C:\Program Files\MySQL\MySQL Server 8.1\bin\mysql.exe
    goto :ejecutar
)

REM Ruta 5: MySQL Server 5.7
if exist "C:\Program Files\MySQL\MySQL Server 5.7\bin\mysql.exe" (
    set MYSQL_PATH=C:\Program Files\MySQL\MySQL Server 5.7\bin\mysql.exe
    goto :ejecutar
)

echo.
echo ========================================
echo ERROR: No se encontro MySQL
echo ========================================
echo.
echo Por favor, asegurate de que MySQL este instalado en una de estas ubicaciones:
echo - C:\xampp\mysql\bin\
echo - C:\Program Files\xampp\mysql\bin\
echo - C:\Program Files\MySQL\MySQL Server 8.0\bin\
echo - C:\Program Files\MySQL\MySQL Server 8.1\bin\
echo - C:\Program Files\MySQL\MySQL Server 5.7\bin\
echo.
pause
exit /b 1

:ejecutar
echo MySQL encontrado en: %MYSQL_PATH%
echo.

REM Ejecutar el script SQL
"%MYSQL_PATH%" -h %DB_HOST% -P %DB_PORT% -u %DB_USER% -p%DB_PASS% %DB_NAME% < agrogestion-backend\src\main\resources\db\migration\create_tablas_aplicaciones_agroquimicas.sql

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

