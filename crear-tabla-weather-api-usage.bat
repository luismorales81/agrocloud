@echo off
echo ========================================
echo   Creando tabla Weather API Usage
echo ========================================

echo 1. Ejecutando script SQL...
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p123456 agrocloud < agrogestion-backend\src\main\resources\create-weather-api-usage-table.sql

echo.
echo 2. Verificando que la tabla se creó correctamente...
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p123456 agrocloud -e "DESCRIBE weather_api_usage;"

echo.
echo ========================================
echo Tabla weather_api_usage creada exitosamente
echo ========================================
echo.
echo Presiona cualquier tecla para continuar...
pause
