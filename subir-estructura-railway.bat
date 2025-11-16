@echo off
REM ========================================
REM SCRIPT PARA SUBIR ESTRUCTURA A MYSQL EN RAILWAY
REM ========================================
REM 
REM INSTRUCCIONES:
REM 1. Obtén los datos de Railway (ver DATOS-CONEXION-MYSQL-RAILWAY.md)
REM 2. Reemplaza los valores entre corchetes [] con tus datos reales
REM 3. Asegúrate de que el archivo SQL existe en la misma carpeta
REM 4. Ejecuta este script
REM
REM ========================================

echo ========================================
echo SUBIENDO ESTRUCTURA A MYSQL EN RAILWAY
echo ========================================
echo.

REM ========================================
REM CONFIGURACIÓN - REEMPLAZA ESTOS VALORES
REM ========================================
REM URL PÚBLICA de Railway para conexión desde tu PC
set HOST=ballast.proxy.rlwy.net
set PORT=41199
set DATABASE=railway
set USERNAME=root
set PASSWORD=OxwHQZQdvdAmCNBwEsdhDCmxzHbgJGpy

REM Archivo SQL a ejecutar (ajusta el nombre según corresponda)
set ARCHIVO_SQL=estructura_local_completa.sql

REM ========================================
REM VERIFICACIÓN DE CONFIGURACIÓN
REM ========================================
if "%HOST%"=="" (
    echo ERROR: No has configurado el HOST
    echo Por favor, edita este archivo y configura el HOST
    pause
    exit /b 1
)

if "%USERNAME%"=="" (
    echo ERROR: No has configurado el USERNAME
    echo Por favor, edita este archivo y configura el USERNAME
    pause
    exit /b 1
)

if "%PASSWORD%"=="" (
    echo ERROR: No has configurado la PASSWORD
    echo Por favor, edita este archivo y configura la PASSWORD
    pause
    exit /b 1
)

REM Mostrar información de conexión
echo.
echo Conectando usando URL PÚBLICA de Railway
echo.

REM ========================================
REM VERIFICACIÓN DE ARCHIVO SQL
REM ========================================
if not exist "%ARCHIVO_SQL%" (
    echo ERROR: No se encuentra el archivo: %ARCHIVO_SQL%
    echo.
    echo Archivos SQL disponibles en esta carpeta:
    dir /b *.sql
    echo.
    echo Por favor, ajusta la variable ARCHIVO_SQL en este script o coloca el archivo en esta carpeta.
    pause
    exit /b 1
)

REM ========================================
REM CONFIRMACIÓN ANTES DE EJECUTAR
REM ========================================
echo Configuración:
echo   Host: %HOST%
echo   Puerto: %PORT%
echo   Base de datos: %DATABASE%
echo   Usuario: %USERNAME%
echo   Archivo SQL: %ARCHIVO_SQL%
echo.
echo ADVERTENCIA: Este script modificará la base de datos en Railway.
echo.
set /p CONFIRMAR="¿Deseas continuar? (S/N): "
if /i not "%CONFIRMAR%"=="S" (
    echo Operación cancelada.
    pause
    exit /b 0
)

REM ========================================
REM BUSCAR MYSQL EN UBICACIONES COMUNES
REM ========================================
set MYSQL_PATH=
if exist "C:\xampp\mysql\bin\mysql.exe" (
    set "MYSQL_PATH=C:\xampp\mysql\bin\mysql.exe"
) else if exist "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" (
    set "MYSQL_PATH=C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
) else if exist "C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe" (
    set "MYSQL_PATH=C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe"
) else if exist "C:\Program Files (x86)\MySQL\MySQL Server 8.0\bin\mysql.exe" (
    set "MYSQL_PATH=C:\Program Files (x86)\MySQL\MySQL Server 8.0\bin\mysql.exe"
) else (
    REM Intentar usar mysql del PATH
    where mysql >nul 2>&1
    if %ERRORLEVEL% EQU 0 (
        set "MYSQL_PATH=mysql"
    )
)

if "%MYSQL_PATH%"=="" (
    echo.
    echo ERROR: No se encontró MySQL en el sistema
    echo.
    echo Por favor, instala MySQL o agrega MySQL al PATH del sistema.
    echo Ubicaciones comunes donde se busca:
    echo   - C:\xampp\mysql\bin\
    echo   - C:\Program Files\MySQL\MySQL Server 8.0\bin\
    echo   - C:\Program Files\MySQL\MySQL Server 8.4\bin\
    echo.
    echo O puedes ejecutar manualmente:
    echo   "[RUTA_A_MYSQL]\mysql.exe" -h %HOST% -P %PORT% -u %USERNAME% -p%PASSWORD% %DATABASE% ^< %ARCHIVO_SQL%
    echo.
    pause
    exit /b 1
)

echo MySQL encontrado en: %MYSQL_PATH%
echo.

REM ========================================
REM EJECUTAR SCRIPT SQL
REM ========================================
echo.
echo Conectando a MySQL en Railway...
echo Ejecutando script SQL: %ARCHIVO_SQL%
echo Esto puede tardar varios minutos dependiendo del tamaño del archivo...
echo.

"%MYSQL_PATH%" -h %HOST% -P %PORT% -u %USERNAME% -p%PASSWORD% %DATABASE% < %ARCHIVO_SQL%

REM ========================================
REM VERIFICACIÓN DE RESULTADO
REM ========================================
if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo ¡ÉXITO! Estructura subida correctamente
    echo ========================================
    echo.
    echo La estructura de la base de datos ha sido subida exitosamente a Railway.
    echo Puedes verificar en Railway Dashboard que las tablas se crearon correctamente.
) else (
    echo.
    echo ========================================
    echo ERROR al subir la estructura
    echo ========================================
    echo.
    echo Código de error: %ERRORLEVEL%
    echo.
    echo Posibles causas:
    echo   - Credenciales incorrectas
    echo   - Problemas de conexión
    echo   - Errores en el script SQL
    echo   - La base de datos no existe
    echo.
    echo Revisa los mensajes de error anteriores para más detalles.
)

echo.
pause

