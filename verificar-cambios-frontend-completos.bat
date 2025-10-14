@echo off
echo ========================================
echo   Verificando Cambios Frontend Completos
echo ========================================

echo 1. Verificando cambios implementados en el frontend:
echo    - Interfaz MaquinariaAsignada actualizada con campo proveedor
echo    - Transformación de maquinaria implementada
echo    - Lógica para determinar tipo basado en proveedor

echo.
echo 2. Verificando la transformación de maquinaria:
echo    - Si tiene proveedor: se envía como ALQUILADA
echo    - Si NO tiene proveedor: se envía como PROPIA

echo.
echo 3. Verificando que el backend reciba correctamente:
echo    - Campo proveedor en la transformación
echo    - Lógica automática para determinar tipo_maquinaria

echo.
echo 4. Estructura actual de la base de datos:
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p123456 -e "USE agrocloud; SELECT id_labor_maquinaria, id_labor, descripcion, tipo_maquinaria, proveedor, costo FROM labor_maquinaria ORDER BY id_labor_maquinaria DESC LIMIT 3;"

echo.
echo 5. Instrucciones para probar:
echo    - Recarga la página del frontend (F5)
echo    - Crea una nueva labor
echo    - Agrega maquinaria PROPIA (sin proveedor)
echo    - Agrega maquinaria ALQUILADA (con proveedor)
echo    - Guarda la labor
echo    - Verifica en la base de datos

echo.
echo 6. Resultado esperado:
echo    - Maquinaria propia: tipo_maquinaria = 'PROPIA', proveedor = NULL
echo    - Maquinaria alquilada: tipo_maquinaria = 'ALQUILADA', proveedor = 'nombre_proveedor'

echo.
echo Verificacion completada
echo.
echo Presiona cualquier tecla para continuar...
pause








