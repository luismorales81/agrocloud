@echo off
REM Habilita modulo FEEDLOT para empresa AgroCloud Demo (usuario operativo: admin@agrocloud.com)
REM Ajustar MYSQL, usuario y base segun entorno local.

set MYSQL="C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
set DB=agrocloud
set USER=root
set PASS=123456

echo Ejecutando HABILITAR_FEEDLOT_EMPRESA_DEMO.sql ...
%MYSQL% -u %USER% -p%PASS% %DB% < "%~dp0HABILITAR_FEEDLOT_EMPRESA_DEMO.sql"
if %ERRORLEVEL% equ 0 (
    echo OK: Modulo FEEDLOT habilitado para la empresa de prueba.
) else (
    echo Error al ejecutar script. Codigo: %ERRORLEVEL%
)
pause
