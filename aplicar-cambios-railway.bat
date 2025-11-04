@echo off
echo ========================================
echo APLICAR CAMBIOS A RAILWAY
echo ========================================
echo.

REM Configuración Railway
set RAILWAY_HOST=gondola.proxy.rlwy.net
set RAILWAY_PORT=54893
set RAILWAY_USER=root
set RAILWAY_PASS=WSoobrppUQbaPINdsRcoQVkUvtYKjmSe
set RAILWAY_DB=railway

REM Ruta MySQL
set MYSQL_PATH=C:\Program Files\MySQL\MySQL Server 8.0\bin
set MYSQL=%MYSQL_PATH%\mysql.exe

REM Verificar que MySQL esté disponible
if not exist "%MYSQL%" (
    echo ERROR: No se encuentra mysql.exe
    echo Por favor, verifica que MySQL esté instalado en: %MYSQL_PATH%
    pause
    exit /b 1
)

echo [1/3] Verificando conexión a Railway...
"%MYSQL%" -h %RAILWAY_HOST% -P %RAILWAY_PORT% -u %RAILWAY_USER% -p%RAILWAY_PASS% -D %RAILWAY_DB% -e "SELECT 1" >nul 2>&1
if errorlevel 1 (
    echo ERROR: No se pudo conectar a Railway
    echo Verifica las credenciales y que Railway esté corriendo
    pause
    exit /b 1
)
echo ✓ Conexión exitosa

echo.
echo [2/3] Aplicando cambios...
echo.
echo IMPORTANTE: Se aplicarán los siguientes cambios:
echo   - Crear tabla dosis_insumos
echo   - Crear tabla formas_aplicacion
echo   - Agregar columnas a tabla insumos (agroquímicos)
echo   - Modificar tabla labores (costo_total)
echo.
pause

echo.
echo Ejecutando script SQL...
"%MYSQL%" -h %RAILWAY_HOST% -P %RAILWAY_PORT% -u %RAILWAY_USER% -p%RAILWAY_PASS% -D %RAILWAY_DB% < aplicar_cambios_completos_railway.sql
if errorlevel 1 (
    echo.
    echo ERROR: Hubo un error al aplicar los cambios
    echo Revisa el archivo aplicar_cambios_completos_railway.sql
    pause
    exit /b 1
)

echo.
echo [3/3] Verificando cambios aplicados...
echo.

echo Verificando tablas creadas:
"%MYSQL%" -h %RAILWAY_HOST% -P %RAILWAY_PORT% -u %RAILWAY_USER% -p%RAILWAY_PASS% -D %RAILWAY_DB% -e "SHOW TABLES LIKE 'dosis_insumos';"
"%MYSQL%" -h %RAILWAY_HOST% -P %RAILWAY_PORT% -u %RAILWAY_USER% -p%RAILWAY_PASS% -D %RAILWAY_DB% -e "SHOW TABLES LIKE 'formas_aplicacion';"

echo.
echo Verificando columnas agregadas en insumos:
"%MYSQL%" -h %RAILWAY_HOST% -P %RAILWAY_PORT% -u %RAILWAY_USER% -p%RAILWAY_PASS% -D %RAILWAY_DB% -e "SHOW COLUMNS FROM insumos LIKE 'principio_activo';"

echo.
echo ========================================
echo CAMBIOS APLICADOS EXITOSAMENTE
echo ========================================
echo.
echo Revisa los resultados anteriores para confirmar que los cambios se aplicaron correctamente.
echo.
pause

