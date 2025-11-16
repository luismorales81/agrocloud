@echo off
REM ========================================
REM SCRIPT PARA CONECTARSE A MYSQL EN RAILWAY
REM ========================================
REM 
REM INSTRUCCIONES:
REM 1. Obtén los datos de Railway (ver DATOS-CONEXION-MYSQL-RAILWAY.md)
REM 2. Reemplaza los valores entre corchetes [] con tus datos reales
REM 3. Guarda el archivo
REM 4. Ejecuta este script
REM
REM ========================================

echo ========================================
echo CONECTANDO A MYSQL EN RAILWAY
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

REM Mostrar información de conexión
echo.
echo Conectando usando URL PÚBLICA de Railway
echo.

REM ========================================
REM OPCIÓN 1: Conectarse interactivamente
REM ========================================
echo Conectando a MySQL en Railway...
echo Host: %HOST%
echo Puerto: %PORT%
echo Base de datos: %DATABASE%
echo Usuario: %USERNAME%
echo.
echo Ingresando a MySQL (se pedirá la contraseña)...
echo.

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
    echo.
    pause
    exit /b 1
)

echo MySQL encontrado en: %MYSQL_PATH%
echo.

"%MYSQL_PATH%" -h %HOST% -P %PORT% -u %USERNAME% -p %DATABASE%

REM ========================================
REM OPCIÓN 2: Ejecutar un script SQL
REM ========================================
REM Si quieres ejecutar un script SQL automáticamente,
REM descomenta las siguientes líneas y ajusta la ruta del archivo:

REM echo.
REM echo Ejecutando script SQL...
REM mysql -h %HOST% -P %PORT% -u %USERNAME% -p%PASSWORD% %DATABASE% < estructura_local_completa.sql
REM echo.
REM echo Script ejecutado exitosamente!

echo.
echo ========================================
echo Conexión finalizada
echo ========================================
pause

