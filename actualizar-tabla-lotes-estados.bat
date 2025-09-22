@echo off
echo ========================================
echo   Actualizando tabla lotes con estados
echo ========================================

echo 1. Aplicando migración V1.8...
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p123456 -e "USE agrocloud; SOURCE agrogestion-backend/src/main/resources/db/migration/V1_8__Update_Lotes_Table_Estado_Fields.sql;"

echo.
echo 2. Verificando estructura actualizada de la tabla lotes:
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p123456 -e "USE agrocloud; DESCRIBE lotes;"

echo.
echo 3. Verificando datos existentes:
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p123456 -e "USE agrocloud; SELECT id, nombre, estado, fecha_ultimo_cambio_estado, motivo_cambio_estado FROM lotes LIMIT 5;"

echo.
echo 4. Verificando índices creados:
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p123456 -e "USE agrocloud; SHOW INDEX FROM lotes WHERE Key_name LIKE 'idx_lotes_%';"

echo.
echo ========================================
echo   Tabla lotes actualizada exitosamente
echo ========================================
pause
