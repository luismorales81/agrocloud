@echo off
REM Agrega columnas version y liberado_para_siembra a cultivo_lotes si no existen
set MYSQL="C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
%MYSQL% -u root -p123456 agrocloud < "%~dp0agregar-columnas-cultivo-lotes.sql"
if %ERRORLEVEL% equ 0 (echo OK: Columnas agregadas o ya existian.) else (echo Error: %ERRORLEVEL%)
pause
