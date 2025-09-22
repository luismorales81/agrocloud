@echo off
echo ========================================
echo   Verificando insumos en labores
echo ========================================

echo 1. Verificando que la tabla labor_insumos existe:
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p123456 -e "USE agrocloud; SHOW TABLES LIKE 'labor_insumos';"

echo.
echo 2. Verificando estructura de la tabla labor_insumos:
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p123456 -e "USE agrocloud; DESCRIBE labor_insumos;"

echo.
echo 3. Verificando datos en labor_insumos:
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p123456 -e "USE agrocloud; SELECT COUNT(*) as total_insumos FROM labor_insumos;"

echo.
echo 4. Verificando insumos por labor:
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p123456 -e "USE agrocloud; SELECT li.id_labor, l.descripcion as labor_descripcion, li.id_insumo, i.nombre as insumo_nombre, li.cantidad_usada, li.costo_total FROM labor_insumos li JOIN labores l ON li.id_labor = l.id JOIN insumos i ON li.id_insumo = i.id ORDER BY li.id_labor DESC LIMIT 10;"

echo.
echo 5. Verificando mano de obra con costos:
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p123456 -e "USE agrocloud; SELECT id_labor_mano_obra, id_labor, descripcion, cantidad_personas, costo_total, horas_trabajo FROM labor_mano_obra WHERE costo_total > 0 ORDER BY id_labor_mano_obra DESC LIMIT 10;"

echo.
echo ========================================
echo   Verificación completada
echo ========================================
pause
