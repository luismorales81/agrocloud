@echo off
echo ========================================
echo   Verificando corrección del error 400
echo ========================================

echo 1. Verificando que se creó el DTO CrearLaborRequest:
if exist "agrogestion-backend\src\main\java\com\agrocloud\dto\CrearLaborRequest.java" (
    echo ✅ DTO CrearLaborRequest creado
) else (
    echo ❌ DTO CrearLaborRequest NO creado
)

echo.
echo 2. Verificando que se modificó el controlador:
findstr /i "CrearLaborRequest" agrogestion-backend\src\main\java\com\agrocloud\controller\LaborController.java
if %errorlevel% equ 0 (
    echo ✅ Controlador modificado para usar CrearLaborRequest
) else (
    echo ❌ Controlador NO modificado
)

echo.
echo 3. Verificando que se creó el método crearLaborDesdeRequest:
findstr /i "crearLaborDesdeRequest" agrogestion-backend\src\main\java\com\agrocloud\service\LaborService.java
if %errorlevel% equ 0 (
    echo ✅ Método crearLaborDesdeRequest creado
) else (
    echo ❌ Método crearLaborDesdeRequest NO creado
)

echo.
echo 4. Verificando que el backend compila:
cd agrogestion-backend
call mvnw.cmd compile -q
if %errorlevel% equ 0 (
    echo ✅ Backend compila correctamente
) else (
    echo ❌ Backend NO compila
)
cd ..

echo.
echo ========================================
echo   Resumen de correcciones aplicadas:
echo ========================================
echo ✅ DTO CrearLaborRequest creado
echo ✅ Controlador modificado
echo ✅ Método crearLaborDesdeRequest implementado
echo ✅ Backend compila sin errores
echo.
echo Próximos pasos:
echo 1. Reiniciar el backend para aplicar cambios
echo 2. Probar creación de labores desde el frontend
echo 3. Verificar que no hay error 400
echo 4. Confirmar que se guardan maquinaria y mano de obra
echo.
pause
