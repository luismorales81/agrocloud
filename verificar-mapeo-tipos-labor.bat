@echo off
echo ========================================
echo   Verificando Mapeo de Tipos de Labor
echo ========================================

echo 1. Tipos de labor en el combo del frontend:
echo    - siembra
echo    - fertilizacion  
echo    - cosecha
echo    - riego
echo    - pulverizacion
echo    - arado
echo    - rastra
echo    - desmalezado
echo    - aplicacion_herbicida
echo    - aplicacion_insecticida
echo    - monitoreo
echo    - otro

echo.
echo 2. Mapeo actualizado en mapTipoLaborToBackend:
echo    - siembra -> SIEMBRA
echo    - fertilizacion -> FERTILIZACION
echo    - cosecha -> COSECHA
echo    - riego -> RIEGO
echo    - pulverizacion -> MANTENIMIENTO
echo    - arado -> MANTENIMIENTO
echo    - rastra -> MANTENIMIENTO
echo    - desmalezado -> CONTROL_MALEZAS
echo    - aplicacion_herbicida -> CONTROL_MALEZAS
echo    - aplicacion_insecticida -> CONTROL_PLAGAS
echo    - monitoreo -> ANALISIS_SUELO
echo    - otro -> OTROS

echo.
echo 3. Verificando labor ID 9 en la base de datos...
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p123456 -e "USE agrocloud; SELECT id, tipo_labor, descripcion FROM labores WHERE id = 9;"

echo.
echo 4. Instrucciones para probar:
echo    - Recarga la página del frontend (F5)
echo    - Edita la labor ID 9
echo    - Cambia el tipo de labor a cualquier opción del combo
echo    - Guarda los cambios
echo    - Verifica que el tipo se guarde correctamente

echo.
echo Verificacion completada
echo.
echo Presiona cualquier tecla para continuar...
pause








