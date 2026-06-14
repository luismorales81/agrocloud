@echo off
REM Ejecuta aplicar_tipo_uso_si_falta.sql contra MySQL (agrocloud)
REM Ajusta MYSQL_BIN si tu instalación está en otra ruta.

set DB=agrocloud
set USER=root
set PASS=123456
set SCRIPT=%~dp0aplicar_tipo_uso_si_falta.sql

REM Rutas habituales de MySQL en Windows (XAMPP, MySQL Server)
if exist "C:\xampp\mysql\bin\mysql.exe" set MYSQL_BIN=C:\xampp\mysql\bin\mysql.exe
if exist "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" set MYSQL_BIN=C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe
if exist "C:\Program Files\MySQL\MySQL Server 5.7\bin\mysql.exe" set MYSQL_BIN=C:\Program Files\MySQL\MySQL Server 5.7\bin\mysql.exe
if exist "C:\mysql\bin\mysql.exe" set MYSQL_BIN=C:\mysql\bin\mysql.exe

if not defined MYSQL_BIN (
  echo No se encontro mysql.exe. Ejecuta el script manualmente:
  echo   mysql -u %USER% -p %DB% ^< "%SCRIPT%"
  echo O desde MySQL Workbench / DBeaver: abre aplicar_tipo_uso_si_falta.sql y ejecutalo.
  exit /b 1
)

echo Ejecutando script contra %DB%...
"%MYSQL_BIN%" -u %USER% -p%PASS% %DB% < "%SCRIPT%"
if %ERRORLEVEL% neq 0 (
  echo Error al ejecutar. Verifica usuario, password y que la base %DB% exista.
  exit /b 1
)
echo Listo. Columna tipo_uso aplicada.
pause
