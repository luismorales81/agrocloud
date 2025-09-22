@echo off
echo ========================================
echo   Verificando tabla labor_insumos
echo ========================================

echo 1. Estructura de la tabla:
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p123456 -e "USE agrocloud; SHOW CREATE TABLE labor_insumos;"

echo.
echo 2. Verificando claves foráneas:
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p123456 -e "USE agrocloud; SELECT TABLE_NAME, COLUMN_NAME, CONSTRAINT_NAME, REFERENCED_TABLE_NAME, REFERENCED_COLUMN_NAME FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE WHERE TABLE_SCHEMA = 'agrocloud' AND TABLE_NAME = 'labor_insumos' AND REFERENCED_TABLE_NAME IS NOT NULL;"

echo.
echo 3. Verificando índices:
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p123456 -e "USE agrocloud; SHOW INDEX FROM labor_insumos;"

echo.
echo ========================================
echo   Verificación completada
echo ========================================
pause
