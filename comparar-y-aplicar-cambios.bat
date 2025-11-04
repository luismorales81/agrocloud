@echo off
setlocal enabledelayedexpansion

echo ========================================
echo COMPARAR Y APLICAR CAMBIOS A RAILWAY
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

if not exist "%MYSQL%" (
    echo ERROR: No se encuentra mysql.exe
    pause
    exit /b 1
)

echo [1/6] Exportando estructura completa de LOCAL...
"%MYSQLDUMP%" -h %LOCAL_HOST% -P %LOCAL_PORT% -u %LOCAL_USER% -p%LOCAL_PASS% --no-data --routines --triggers --skip-comments --skip-add-drop-table --skip-tz-utc %LOCAL_DB% > estructura_local_completa.sql 2>nul
if errorlevel 1 (
    echo ERROR: No se pudo exportar la estructura local
    pause
    exit /b 1
)
echo ✓ Estructura local exportada

echo.
echo [2/6] Exportando estructura completa de RAILWAY...
"%MYSQLDUMP%" -h %RAILWAY_HOST% -P %RAILWAY_PORT% -u %RAILWAY_USER% -p%RAILWAY_PASS% --no-data --routines --triggers --skip-comments --skip-add-drop-table --skip-tz-utc %RAILWAY_DB% > estructura_railway_completa.sql 2>nul
if errorlevel 1 (
    echo ERROR: No se pudo exportar la estructura de Railway
    pause
    exit /b 1
)
echo ✓ Estructura Railway exportada

echo.
echo [3/6] Obteniendo listas de tablas...
"%MYSQL%" -h %LOCAL_HOST% -P %LOCAL_PORT% -u %LOCAL_USER% -p%LOCAL_PASS% -D %LOCAL_DB% -N -e "SHOW TABLES" > tablas_local.txt 2>nul
"%MYSQL%" -h %RAILWAY_HOST% -P %RAILWAY_PORT% -u %RAILWAY_USER% -p%RAILWAY_PASS% -D %RAILWAY_DB% -N -e "SHOW TABLES" > tablas_railway.txt 2>nul
echo ✓ Listas de tablas obtenidas

echo.
echo [4/6] Comparando tablas...
echo.

REM Crear script SQL para aplicar cambios
echo -- Script generado automaticamente para aplicar cambios de LOCAL a RAILWAY > aplicar_cambios_railway.sql
echo -- Fecha: %date% %time% >> aplicar_cambios_railway.sql
echo -- Base de datos origen: %LOCAL_DB% >> aplicar_cambios_railway.sql
echo -- Base de datos destino: %RAILWAY_DB% >> aplicar_cambios_railway.sql
echo. >> aplicar_cambios_railway.sql
echo USE railway; >> aplicar_cambios_railway.sql
echo. >> aplicar_cambios_railway.sql

echo -- ======================================== >> aplicar_cambios_railway.sql
echo -- TABLAS QUE FALTAN EN RAILWAY >> aplicar_cambios_railway.sql
echo -- ======================================== >> aplicar_cambios_railway.sql
echo. >> aplicar_cambios_railway.sql

REM Comparar tablas y generar CREATE TABLE para las que faltan
set /a tablas_faltantes=0
for /f "delims=" %%t in (tablas_local.txt) do (
    findstr /c:"%%t" tablas_railway.txt >nul
    if errorlevel 1 (
        echo   Tabla faltante: %%t
        set /a tablas_faltantes+=1
        echo -- Exportar estructura de tabla: %%t >> aplicar_cambios_railway.sql
        "%MYSQLDUMP%" -h %LOCAL_HOST% -P %LOCAL_PORT% -u %LOCAL_USER% -p%LOCAL_PASS% --no-data --skip-comments --skip-tz-utc %LOCAL_DB% %%t >> aplicar_cambios_railway.sql 2>nul
        echo. >> aplicar_cambios_railway.sql
    )
)

if %tablas_faltantes% equ 0 (
    echo   No hay tablas faltantes
    echo -- No hay tablas faltantes >> aplicar_cambios_railway.sql
)

echo. >> aplicar_cambios_railway.sql
echo -- ======================================== >> aplicar_cambios_railway.sql
echo -- MODIFICACIONES A TABLAS EXISTENTES >> aplicar_cambios_railway.sql
echo -- ======================================== >> aplicar_cambios_railway.sql
echo. >> aplicar_cambios_railway.sql
echo -- NOTA: Revisa manualmente las diferencias entre las estructuras >> aplicar_cambios_railway.sql
echo -- de las tablas existentes usando los archivos: >> aplicar_cambios_railway.sql
echo --   - estructura_local_completa.sql >> aplicar_cambios_railway.sql
echo --   - estructura_railway_completa.sql >> aplicar_cambios_railway.sql
echo. >> aplicar_cambios_railway.sql

echo.
echo [5/6] Generando resumen de diferencias...
echo.
echo ========================================
echo RESUMEN DE DIFERENCIAS
echo ========================================
echo.
echo Tablas en LOCAL: 
for /f %%t in (tablas_local.txt) do @echo   - %%t
echo.
echo Tablas en RAILWAY: 
for /f %%t in (tablas_railway.txt) do @echo   - %%t
echo.
echo Tablas faltantes en RAILWAY: %tablas_faltantes%
echo.

echo [6/6] Verificando cambios en columnas de tablas existentes...
echo.
echo Comparando estructuras de tablas comunes...

REM Para cada tabla común, comparar columnas
for /f "delims=" %%t in (tablas_local.txt) do (
    findstr /c:"%%t" tablas_railway.txt >nul
    if not errorlevel 1 (
        echo   Comparando tabla: %%t...
        
        REM Exportar estructura de columna de local
        "%MYSQL%" -h %LOCAL_HOST% -P %LOCAL_PORT% -u %LOCAL_USER% -p%LOCAL_PASS% -D %LOCAL_DB% -N -e "DESCRIBE `%%t`" > temp_local_%%t.txt 2>nul
        
        REM Exportar estructura de columna de railway
        "%MYSQL%" -h %RAILWAY_HOST% -P %RAILWAY_PORT% -u %RAILWAY_USER% -p%RAILWAY_PASS% -D %RAILWAY_DB% -N -e "DESCRIBE `%%t`" > temp_railway_%%t.txt 2>nul
        
        REM Comparar (esto es básico, se puede mejorar)
        fc temp_local_%%t.txt temp_railway_%%t.txt >nul 2>nul
        if errorlevel 1 (
            echo     [DIFERENCIAS DETECTADAS] - Revisa manualmente
            echo -- Tabla %%t tiene diferencias en columnas >> aplicar_cambios_railway.sql
            echo -- Revisa estructura_local_completa.sql y estructura_railway_completa.sql >> aplicar_cambios_railway.sql
            echo. >> aplicar_cambios_railway.sql
        )
        
        del temp_local_%%t.txt temp_railway_%%t.txt 2>nul
    )
)

echo.
echo ========================================
echo COMPARACION COMPLETADA
echo ========================================
echo.
echo Archivos generados:
echo   - estructura_local_completa.sql
echo   - estructura_railway_completa.sql
echo   - tablas_local.txt
echo   - tablas_railway.txt
echo   - aplicar_cambios_railway.sql
echo.
echo SIGUIENTES PASOS:
echo   1. Revisa aplicar_cambios_railway.sql
echo   2. Verifica que las tablas faltantes sean correctas
echo   3. Compara manualmente las estructuras de tablas existentes
echo   4. Aplica los cambios a Railway usando:
echo      mysql -h %RAILWAY_HOST% -P %RAILWAY_PORT% -u %RAILWAY_USER% -p%RAILWAY_PASS% %RAILWAY_DB% ^< aplicar_cambios_railway.sql
echo.
pause

