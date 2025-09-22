@echo off
echo ========================================
echo   Verificando Labor ID 9
echo ========================================

echo 1. Verificando labor ID 9...
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p123456 -e "USE agrocloud; SELECT 'LABOR 9' as tipo, id, tipo_labor, descripcion, fecha_inicio, estado, costo_total FROM labores WHERE id = 9;"

echo.
echo 2. Verificando maquinaria de labor ID 9...
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p123456 -e "USE agrocloud; SELECT 'MAQUINARIA LABOR 9' as tipo, id_labor_maquinaria, id_labor, descripcion, costo FROM labor_maquinaria WHERE id_labor = 9;"

echo.
echo 3. Verificando mano de obra de labor ID 9...
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p123456 -e "USE agrocloud; SELECT 'MANO OBRA LABOR 9' as tipo, id_labor_mano_obra, id_labor, descripcion, costo_total FROM labor_mano_obra WHERE id_labor = 9;"

echo.
echo Verificacion completada
echo.
echo Presiona cualquier tecla para continuar...
pause
