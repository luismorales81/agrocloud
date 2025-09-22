@echo off
echo ========================================
echo   Creando tablas de inventario e insumos
echo ========================================

echo 1. Creando tabla movimientos_inventario...
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p123456 -e "USE agrocloud; SOURCE agrogestion-backend/src/main/resources/db/migration/V1_7__Create_Movimientos_Inventario_Table.sql;"

echo.
echo 2. Verificando que las tablas se crearon correctamente...
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p123456 -e "USE agrocloud; SHOW TABLES LIKE '%inventario%'; SHOW TABLES LIKE '%insumo%';"

echo.
echo 3. Verificando estructura de movimientos_inventario:
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p123456 -e "USE agrocloud; DESCRIBE movimientos_inventario;"

echo.
echo 4. Verificando estructura de labor_insumos:
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p123456 -e "USE agrocloud; DESCRIBE labor_insumos;"

echo.
echo ========================================
echo   Tablas creadas exitosamente
echo ========================================
pause
