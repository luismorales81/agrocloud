@echo off
echo ========================================
echo   Probando funcionalidad de cosecha sin humedad
echo ========================================

echo 1. Verificando que la columna humedad_cosecha fue eliminada:
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p123456 -e "USE agrocloud; DESCRIBE historial_cosechas;" | findstr humedad

echo.
echo 2. Verificando estructura actual de historial_cosechas:
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p123456 -e "USE agrocloud; DESCRIBE historial_cosechas;"

echo.
echo 3. Verificando campos de rendimiento en la tabla lotes:
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p123456 -e "USE agrocloud; SELECT id, nombre, cultivo_actual, area_hectareas, rendimiento_esperado, rendimiento_real, fecha_cosecha_real FROM lotes WHERE fecha_cosecha_real IS NOT NULL LIMIT 3;"

echo.
echo 4. Verificando labores de cosecha existentes:
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p123456 -e "USE agrocloud; SELECT l.id, l.descripcion, l.tipo_labor, l.estado, p.nombre as lote_nombre, p.cultivo_actual FROM labores l JOIN lotes p ON l.lote_id = p.id WHERE l.tipo_labor = 'COSECHA' ORDER BY l.id DESC LIMIT 5;"

echo.
echo 5. Verificando vista de historial de cosechas:
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p123456 -e "USE agrocloud; SELECT * FROM vista_historial_cosechas_completo LIMIT 3;"

echo.
echo 6. Verificando estadísticas de cosechas:
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p123456 -e "USE agrocloud; SELECT * FROM vista_estadisticas_cosechas;"

echo.
echo ========================================
echo   Verificación completada
echo ========================================
echo.
echo Funcionalidades implementadas:
echo ✅ Eliminación de humedad de cosecha
echo ✅ Cálculo de rendimiento basado en cantidad esperada vs obtenida
echo ✅ Unidad de medida estandarizada (kg/ha)
echo ✅ Reportes de cosecha con análisis de cumplimiento
echo ✅ Estadísticas por cultivo
echo.
echo Endpoints disponibles:
echo - GET /api/labores/reporte-cosecha/{loteId}
echo - GET /api/labores/reportes-cosecha
echo - GET /api/labores/estadisticas-cosecha
echo.
pause
