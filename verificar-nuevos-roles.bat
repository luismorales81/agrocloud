@echo off
echo ========================================
echo Verificando Nuevos Roles por Area
echo ========================================
echo.

"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p agrocloud < verificar-nuevos-roles.sql

echo.
pause
