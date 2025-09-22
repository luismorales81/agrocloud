@echo off
echo ========================================
echo   Verificando y Limpiando Tabla Labores
echo ========================================

echo 1. Verificando estructura actual...
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p123456 -e "USE agrocloud; SOURCE verificar-y-eliminar-columnas-labores.sql;"

echo.
echo 2. Verificacion completada
echo.
echo Presiona cualquier tecla para continuar...
pause

