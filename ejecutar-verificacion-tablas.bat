@echo off
echo ========================================
echo   Verificando Tablas de Labor
echo ========================================

echo 1. Verificando tablas existentes...
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p123456 -e "USE agrocloud; SOURCE verificar-tablas-labor.sql;"

echo.
echo 2. Verificacion completada
echo.
echo Presiona cualquier tecla para continuar...
pause
