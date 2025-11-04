@echo off
echo ========================================
echo    EJECUTANDO MIGRACION DE AGROQUIMICOS
echo ========================================
echo.

echo Ejecutando script SQL para agregar campos de agroquímicos...
echo.

REM Intentar conectar a MySQL y ejecutar el script
mysql -u root -p -e "source ejecutar_migracion_agroquimicos.sql"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo    MIGRACION COMPLETADA EXITOSAMENTE
    echo ========================================
    echo.
    echo Las siguientes columnas se agregaron a la tabla insumos:
    echo - principio_activo
    echo - concentracion  
    echo - clase_quimica
    echo - categoria_toxicologica
    echo - periodo_carencia_dias
    echo - dosis_minima_por_ha
    echo - dosis_maxima_por_ha
    echo - unidad_dosis
    echo.
    echo Se crearon las siguientes tablas:
    echo - dosis_insumos
    echo - condiciones_aplicacion
    echo - historial_modificaciones_dosis
    echo.
    echo El backend ahora deberia funcionar correctamente.
) else (
    echo.
    echo ========================================
    echo    ERROR EN LA MIGRACION
    echo ========================================
    echo.
    echo No se pudo ejecutar la migración. Verifique:
    echo 1. Que MySQL esté ejecutándose
    echo 2. Que las credenciales sean correctas
    echo 3. Que la base de datos exista
    echo.
    echo Puede ejecutar el script manualmente:
    echo mysql -u root -p nombre_base_datos ^< ejecutar_migracion_agroquimicos.sql
)

echo.
pause
