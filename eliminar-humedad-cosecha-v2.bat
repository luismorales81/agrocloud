@echo off
echo ========================================
echo   Eliminando humedad de cosecha (v2)
echo ========================================

echo 1. Eliminando columna humedad_cosecha...
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p123456 -e "USE agrocloud; ALTER TABLE historial_cosechas DROP COLUMN humedad_cosecha;"

echo.
echo 2. Verificando que la columna fue eliminada:
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p123456 -e "USE agrocloud; DESCRIBE historial_cosechas;" | findstr humedad

echo.
echo 3. Verificando estructura actualizada:
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p123456 -e "USE agrocloud; DESCRIBE historial_cosechas;"

echo.
echo ========================================
echo   Migración completada
echo ========================================
pause
