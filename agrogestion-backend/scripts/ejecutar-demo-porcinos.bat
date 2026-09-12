@echo off
REM Carga datos demo del modulo Porcinos v2 (empresa AgroCloud Demo / admin)
set MYSQL="C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
set DB=agrocloud
set USER=root
set PASS=123456

echo Ejecutando DEMO_INSERTAR_PORCINOS_V2.sql ...
%MYSQL% -u %USER% -p%PASS% %DB% < "%~dp0DEMO_INSERTAR_PORCINOS_V2.sql"
if %ERRORLEVEL% equ 0 (
    echo OK: Datos demo Porcinos v2 cargados.
) else (
    echo Error al ejecutar script. Codigo: %ERRORLEVEL%
    exit /b %ERRORLEVEL%
)
