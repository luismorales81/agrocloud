-- Columnas de rentabilidad del historial de cosechas que la entidad ya usa.

SET @esquema = DATABASE();

SET @sql = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = @esquema
              AND TABLE_NAME = 'cultivo_historial_cosechas'
              AND COLUMN_NAME = 'precio_venta_unitario'
        ),
        'SELECT 1',
        'ALTER TABLE cultivo_historial_cosechas ADD COLUMN precio_venta_unitario DECIMAL(10,2) NULL DEFAULT 0'
    )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = @esquema
              AND TABLE_NAME = 'cultivo_historial_cosechas'
              AND COLUMN_NAME = 'ingreso_total'
        ),
        'SELECT 1',
        'ALTER TABLE cultivo_historial_cosechas ADD COLUMN ingreso_total DECIMAL(15,2) NULL DEFAULT 0'
    )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
