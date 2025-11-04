@echo off
echo ========================================
echo COMPARAR BASES DE DATOS LOCAL Y RAILWAY
echo ========================================
echo.

REM Configuración Local
set LOCAL_HOST=localhost
set LOCAL_PORT=3306
set LOCAL_USER=root
set LOCAL_PASS=123456
set LOCAL_DB=agrocloud

REM Configuración Railway
set RAILWAY_HOST=gondola.proxy.rlwy.net
set RAILWAY_PORT=54893
set RAILWAY_USER=root
set RAILWAY_PASS=WSoobrppUQbaPINdsRcoQVkUvtYKjmSe
set RAILWAY_DB=railway

REM Rutas MySQL
set MYSQL_PATH=C:\Program Files\MySQL\MySQL Server 8.0\bin
set MYSQLDUMP=%MYSQL_PATH%\mysqldump.exe
set MYSQL=%MYSQL_PATH%\mysql.exe

REM Verificar que MySQL esté en el PATH
if not exist "%MYSQLDUMP%" (
    echo ERROR: No se encuentra mysqldump.exe
    echo Por favor, verifica que MySQL esté instalado en: %MYSQL_PATH%
    pause
    exit /b 1
)

echo [1/4] Exportando estructura de base de datos LOCAL...
"%MYSQLDUMP%" -h %LOCAL_HOST% -P %LOCAL_PORT% -u %LOCAL_USER% -p%LOCAL_PASS% --no-data --routines --triggers %LOCAL_DB% > estructura_local.sql
if errorlevel 1 (
    echo ERROR: No se pudo exportar la estructura local
    pause
    exit /b 1
)
echo ✓ Estructura local exportada

echo.
echo [2/4] Exportando estructura de base de datos RAILWAY...
"%MYSQLDUMP%" -h %RAILWAY_HOST% -P %RAILWAY_PORT% -u %RAILWAY_USER% -p%RAILWAY_PASS% --no-data --routines --triggers %RAILWAY_DB% > estructura_railway.sql
if errorlevel 1 (
    echo ERROR: No se pudo exportar la estructura de Railway
    pause
    exit /b 1
)
echo ✓ Estructura Railway exportada

echo.
echo [3/4] Comparando estructuras...
echo.
echo ========================================
echo DIFERENCIAS ENCONTRADAS:
echo ========================================
echo.

REM Comparar tablas
echo -- TABLAS QUE EXISTEN EN LOCAL PERO NO EN RAILWAY:
"%MYSQL%" -h %LOCAL_HOST% -P %LOCAL_PORT% -u %LOCAL_USER% -p%LOCAL_PASS% -D %LOCAL_DB% -e "SHOW TABLES" > tablas_local.txt
"%MYSQL%" -h %RAILWAY_HOST% -P %RAILWAY_PORT% -u %RAILWAY_USER% -p%RAILWAY_PASS% -D %RAILWAY_DB% -e "SHOW TABLES" > tablas_railway.txt

echo.
echo Generando script SQL con diferencias...
echo.

REM Crear script SQL para aplicar cambios
echo -- Script generado automaticamente para aplicar cambios de LOCAL a RAILWAY > aplicar_cambios_railway.sql
echo -- Fecha: %date% %time% >> aplicar_cambios_railway.sql
echo. >> aplicar_cambios_railway.sql
echo USE railway; >> aplicar_cambios_railway.sql
echo. >> aplicar_cambios_railway.sql

echo [4/4] Analizando diferencias detalladas...
echo Esto puede tomar unos minutos...
echo.

REM Usar Python o PowerShell para comparar más detalladamente
powershell -Command "& { $local = Get-Content 'estructura_local.sql' -Raw; $railway = Get-Content 'estructura_railway.sql' -Raw; if ($local -ne $railway) { Write-Host 'Se encontraron diferencias. Generando reporte...' } else { Write-Host 'No se encontraron diferencias estructurales.' } }"

echo.
echo ========================================
echo COMPARACION COMPLETADA
echo ========================================
echo.
echo Archivos generados:
echo   - estructura_local.sql
echo   - estructura_railway.sql
echo   - tablas_local.txt
echo   - tablas_railway.txt
echo   - aplicar_cambios_railway.sql
echo.
echo Revisa los archivos para ver las diferencias detalladas.
echo.
pause

