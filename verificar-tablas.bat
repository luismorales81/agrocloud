@echo off
echo ========================================
echo Verificando tablas creadas
echo ========================================
echo.

"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -h localhost -P 3306 -u root -p123456 agrocloud -e "SHOW TABLES LIKE '%aplicacion%';"

echo.
echo ========================================
echo Verificando estructura de dosis_aplicacion
echo ========================================
echo.

"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -h localhost -P 3306 -u root -p123456 agrocloud -e "DESCRIBE dosis_aplicacion;"

echo.
echo ========================================
echo Verificando estructura de aplicaciones_agroquimicas
echo ========================================
echo.

"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -h localhost -P 3306 -u root -p123456 agrocloud -e "DESCRIBE aplicaciones_agroquimicas;"

echo.
pause











