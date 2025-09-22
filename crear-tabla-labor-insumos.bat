@echo off
echo ========================================
echo   Creando tabla labor_insumos
echo ========================================

echo 1. Conectando a la base de datos MySQL...
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p123456 -e "USE agrocloud; SOURCE agrogestion-backend/src/main/resources/db/migration/V1_6__Create_Labor_Insumos_Table.sql;"

echo.
echo 2. Verificando que la tabla se creo correctamente...
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p123456 -e "USE agrocloud; DESCRIBE labor_insumos;"

echo.
echo ========================================
echo   Tabla labor_insumos creada exitosamente
echo ========================================
pause
