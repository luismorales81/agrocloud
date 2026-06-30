@echo off
REM Habilita modulo AVICOLA_CRIANZA para empresa AgroCloud Demo (usuario operativo: admin)
REM Ajustar MYSQL, usuario y base segun entorno local.

set MYSQL="C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
set DB=agrocloud
set USER=root
set PASS=123456

echo Ejecutando HABILITAR_AVICOLA_CRIANZA_EMPRESA_DEMO.sql ...
%MYSQL% -u %USER% -p%PASS% %DB% < "%~dp0HABILITAR_AVICOLA_CRIANZA_EMPRESA_DEMO.sql"
if %ERRORLEVEL% equ 0 (
    echo OK: Modulo AVICOLA_CRIANZA habilitado con datos demo.
) else (
    echo Error al ejecutar script. Codigo: %ERRORLEVEL%
)
pause
