@echo off
REM Habilita modulo LECHERIA + catalogos demo para AgroCloud Demo
REM Ajustar MYSQL, usuario y base segun entorno local.

set MYSQL="C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
set DB=agrocloud
set USER=root
set PASS=123456

echo Ejecutando HABILITAR_LECHERIA_EMPRESA_DEMO.sql ...
%MYSQL% -u %USER% -p%PASS% %DB% < "%~dp0HABILITAR_LECHERIA_EMPRESA_DEMO.sql"
if %ERRORLEVEL% neq 0 (
    echo Error en habilitacion. Codigo: %ERRORLEVEL%
    pause
    exit /b %ERRORLEVEL%
)

echo Ejecutando DEMO_INSERTAR_LECHERIA.sql ...
%MYSQL% -u %USER% -p%PASS% %DB% < "%~dp0DEMO_INSERTAR_LECHERIA.sql"
if %ERRORLEVEL% equ 0 (
    echo OK: Modulo LECHERIA con datos demo cargados.
) else (
    echo Error al cargar datos demo. Codigo: %ERRORLEVEL%
)
pause
