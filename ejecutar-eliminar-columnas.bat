@echo off
echo ========================================
echo   Eliminando Columnas de Labor Maquinaria
echo ========================================

echo 1. Eliminando columnas tipo, horas_uso y kilometros_recorridos...
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p123456 -e "USE agrocloud; SOURCE eliminar-columnas-labor-maquinaria.sql;"

echo.
echo 2. Eliminacion completada
echo.
echo Presiona cualquier tecla para continuar...
pause
