@echo off
echo ========================================
echo   Probando funcionalidad de estados de lotes
echo ========================================

echo 1. Verificando estructura de la tabla lotes:
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p123456 -e "USE agrocloud; DESCRIBE lotes;"

echo.
echo 2. Verificando datos actuales de lotes:
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p123456 -e "USE agrocloud; SELECT id, nombre, estado, fecha_ultimo_cambio_estado, motivo_cambio_estado, cultivo_actual, fecha_siembra, fecha_cosecha_esperada FROM lotes LIMIT 5;"

echo.
echo 3. Verificando tablas relacionadas:
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p123456 -e "USE agrocloud; SHOW TABLES LIKE '%labor%';"

echo.
echo 4. Verificando tablas de inventario:
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p123456 -e "USE agrocloud; SHOW TABLES LIKE '%inventario%'; SHOW TABLES LIKE '%insumo%';"

echo.
echo 5. Verificando datos de labores existentes:
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p123456 -e "USE agrocloud; SELECT l.id, l.descripcion, l.tipo_labor, l.estado, p.nombre as lote_nombre, p.estado as lote_estado FROM labores l JOIN lotes p ON l.lote_id = p.id ORDER BY l.id DESC LIMIT 5;"

echo.
echo ========================================
echo   Verificación completada
echo ========================================
echo.
echo Próximos pasos:
echo 1. Iniciar el backend: .\iniciar-proyecto.bat
echo 2. Probar endpoints de estados de lotes
echo 3. Crear labor de siembra con confirmación
echo 4. Verificar cambios de estado
echo.
pause
