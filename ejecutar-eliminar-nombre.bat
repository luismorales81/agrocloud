@echo off
echo ========================================
echo   Eliminando Columna nombre de labores
echo ========================================

echo 1. Eliminando columna nombre de labores...
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p123456 -e "USE agrocloud; SOURCE eliminar-columna-nombre-labores.sql;"

echo.
echo 2. Eliminacion completada
echo.
echo Presiona cualquier tecla para continuar...
pause
