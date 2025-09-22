@echo off
echo ========================================
echo   Verificando Logs del Backend
echo ========================================

echo 1. Verificando si el backend está ejecutándose...
netstat -an | findstr :8080

echo.
echo 2. Verificando datos en la base de datos...
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p123456 -e "USE agrocloud; SELECT COUNT(*) as total_labores FROM labores; SELECT COUNT(*) as total_maquinaria FROM labor_maquinaria; SELECT COUNT(*) as total_mano_obra FROM labor_mano_obra;"

echo.
echo 3. Verificando las últimas labores creadas...
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p123456 -e "USE agrocloud; SELECT id, tipo_labor, descripcion, fecha_inicio, estado, costo_total FROM labores ORDER BY id DESC LIMIT 3;"

echo.
echo 4. Verificando maquinaria de las últimas labores...
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p123456 -e "USE agrocloud; SELECT lm.id_labor_maquinaria, lm.id_labor, lm.descripcion, lm.costo FROM labor_maquinaria lm ORDER BY lm.id_labor_maquinaria DESC LIMIT 5;"

echo.
echo 5. Verificando mano de obra de las últimas labores...
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p123456 -e "USE agrocloud; SELECT lmo.id_labor_mano_obra, lmo.id_labor, lmo.descripcion, lmo.costo_total FROM labor_mano_obra lmo ORDER BY lmo.id_labor_mano_obra DESC LIMIT 5;"

echo.
echo Verificacion completada
echo.
echo Presiona cualquier tecla para continuar...
pause