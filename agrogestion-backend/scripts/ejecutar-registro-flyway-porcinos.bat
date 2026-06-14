@echo off
REM Registrar en Flyway las migraciones porcinos 1.120, 1.128-1.131 (ejecutadas a mano)
set MYSQL="C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
%MYSQL% -u root -p123456 agrocloud < "%~dp0registrar-flyway-porcinos-120-131.sql"
if %ERRORLEVEL% equ 0 (echo OK: Flyway registrado.) else (echo Error: %ERRORLEVEL%)
pause
