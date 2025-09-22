@echo off
echo ========================================
echo   Verificando Logs del Backend Directo
echo ========================================

echo 1. Verificando si el backend está ejecutándose...
netstat -an | findstr :8080

echo.
echo 2. Verificando logs del backend...
echo Buscando logs de "CREAR LABOR DEBUG" en el proceso...

echo.
echo 3. Verificando datos actuales en la base...
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p123456 -e "USE agrocloud; SELECT COUNT(*) as total_labores FROM labores; SELECT COUNT(*) as total_maquinaria FROM labor_maquinaria; SELECT COUNT(*) as total_mano_obra FROM labor_mano_obra;"

echo.
echo 4. Verificando la última labor creada...
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p123456 -e "USE agrocloud; SELECT id, tipo_labor, descripcion, costo_total FROM labores ORDER BY id DESC LIMIT 1;"

echo.
echo Verificacion completada
echo.
echo Presiona cualquier tecla para continuar...
pause
