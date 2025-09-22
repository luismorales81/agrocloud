@echo off
echo ========================================
echo   Agregando Columna Tipo Maquinaria
echo ========================================

echo 1. Ejecutando script SQL para agregar columna tipo_maquinaria...
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p123456 < agregar-columna-tipo-maquinaria.sql

echo.
echo 2. Verificando que la columna se agregó correctamente...
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p123456 -e "USE agrocloud; DESCRIBE labor_maquinaria;"

echo.
echo 3. Mostrando datos actuales con la nueva columna...
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p123456 -e "USE agrocloud; SELECT id_labor_maquinaria, id_labor, descripcion, tipo_maquinaria, proveedor, costo FROM labor_maquinaria ORDER BY id_labor_maquinaria DESC LIMIT 5;"

echo.
echo Script ejecutado correctamente
echo.
echo Presiona cualquier tecla para continuar...
pause


