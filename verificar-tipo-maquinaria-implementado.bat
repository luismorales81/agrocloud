@echo off
echo ========================================
echo   Verificando Tipo Maquinaria Implementado
echo ========================================

echo 1. Cambios realizados en el backend:
echo    - Agregada columna tipo_maquinaria a la tabla labor_maquinaria
echo    - Creado enum TipoMaquinaria (PROPIA, ALQUILADA)
echo    - Actualizada entidad LaborMaquinaria con el nuevo campo
echo    - Modificado LaborService para determinar tipo automáticamente

echo.
echo 2. Lógica implementada:
echo    - Si tiene proveedor: se guarda como ALQUILADA
echo    - Si NO tiene proveedor: se guarda como PROPIA

echo.
echo 3. Cambios en el frontend:
echo    - Modificada transformación de maquinaria para enviar proveedor correctamente
echo    - El backend determina automáticamente el tipo basado en el proveedor

echo.
echo 4. Verificando estructura actual de la tabla...
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p123456 -e "USE agrocloud; DESCRIBE labor_maquinaria;"

echo.
echo 5. Mostrando datos actuales...
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p123456 -e "USE agrocloud; SELECT id_labor_maquinaria, id_labor, descripcion, tipo_maquinaria, proveedor, costo FROM labor_maquinaria ORDER BY id_labor_maquinaria DESC LIMIT 5;"

echo.
echo 6. Instrucciones para probar:
echo    - Recarga la página del frontend (F5)
echo    - Crea una nueva labor
echo    - Agrega maquinaria PROPIA (sin proveedor)
echo    - Agrega maquinaria ALQUILADA (con proveedor)
echo    - Guarda la labor
echo    - Verifica en la base de datos que se guarde correctamente

echo.
echo Verificacion completada
echo.
echo Presiona cualquier tecla para continuar...
pause








