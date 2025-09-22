@echo off
echo ========================================
echo   Eliminando Columna costo_por_hora
echo ========================================

echo 1. Eliminando columna costo_por_hora de labor_mano_obra...
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p123456 -e "USE agrocloud; SOURCE eliminar-columna-costo-por-hora.sql;"

echo.
echo 2. Eliminacion completada
echo.
echo Presiona cualquier tecla para continuar...
pause
