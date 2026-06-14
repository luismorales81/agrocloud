@echo off
echo ========================================
echo Ejecutando migracion V1_104
echo ========================================
echo.

"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p123456 agrocloud -e "ALTER TABLE porcinos_parametros_productivos_porcinos ADD COLUMN dias_tolerancia_vencimiento_gestacion INT NULL COMMENT 'Dias de tolerancia para el vencimiento de gestacion';" 2>&1
echo.

"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p123456 agrocloud -e "ALTER TABLE porcinos_parametros_productivos_porcinos ADD COLUMN dias_control_celo INT NULL COMMENT 'Dias de control de celo';" 2>&1
echo.

"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p123456 agrocloud -e "ALTER TABLE porcinos_parametros_productivos_porcinos ADD COLUMN dias_entre_celos INT NULL COMMENT 'Dias entre celos';" 2>&1
echo.

"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p123456 agrocloud -e "ALTER TABLE porcinos_parametros_productivos_porcinos ADD COLUMN dias_pasaje_maternidad INT NULL COMMENT 'Dias antes del parto para pasaje a maternidad';" 2>&1
echo.

"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p123456 agrocloud -e "ALTER TABLE porcinos_parametros_productivos_porcinos ADD COLUMN peso_promedio_nacimiento DECIMAL(10,2) NULL COMMENT 'Peso promedio al nacer (kg)';" 2>&1
echo.

"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p123456 agrocloud -e "ALTER TABLE porcinos_parametros_productivos_porcinos ADD COLUMN peso_destete_objetivo DECIMAL(10,2) NULL COMMENT 'Peso objetivo al destete (kg)';" 2>&1
echo.

"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p123456 agrocloud -e "ALTER TABLE porcinos_parametros_productivos_porcinos ADD COLUMN peso_venta_objetivo DECIMAL(10,2) NULL COMMENT 'Peso objetivo para venta (kg)';" 2>&1
echo.

"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p123456 agrocloud -e "ALTER TABLE porcinos_parametros_productivos_porcinos ADD COLUMN lechones_vivos_parto_objetivo DECIMAL(5,2) NULL COMMENT 'Lechones vivos por parto objetivo';" 2>&1
echo.

"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p123456 agrocloud -e "ALTER TABLE porcinos_parametros_productivos_porcinos ADD COLUMN lechones_destetados_objetivo DECIMAL(5,2) NULL COMMENT 'Lechones destetados objetivo';" 2>&1
echo.

"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p123456 agrocloud -e "ALTER TABLE porcinos_parametros_productivos_porcinos ADD COLUMN partos_madre_anio_objetivo DECIMAL(5,2) NULL COMMENT 'Partos por madre por ano objetivo';" 2>&1
echo.

echo ========================================
echo Verificando columnas...
echo ========================================
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p123456 agrocloud -e "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = 'agrocloud' AND TABLE_NAME = 'porcinos_parametros_productivos_porcinos' AND COLUMN_NAME IN ('dias_control_celo', 'dias_entre_celos', 'dias_pasaje_maternidad', 'peso_promedio_nacimiento', 'peso_destete_objetivo', 'peso_venta_objetivo', 'lechones_vivos_parto_objetivo', 'lechones_destetados_objetivo', 'partos_madre_anio_objetivo', 'dias_tolerancia_vencimiento_gestacion') ORDER BY COLUMN_NAME;"
echo.
echo Migracion completada!
pause















