-- Aumentar precisión de costo_total en labores (defensiva)

SET @esquema = DATABASE();

SET @tabla_labores = (
    SELECT CASE
        WHEN EXISTS (SELECT 1 FROM information_schema.tables t WHERE t.table_schema = @esquema AND t.table_name = 'cultivo_labores')
            THEN 'cultivo_labores'
        WHEN EXISTS (SELECT 1 FROM information_schema.tables t WHERE t.table_schema = @esquema AND t.table_name = 'labores')
            THEN 'labores'
        ELSE NULL
    END
);

SET @sql = IF(
    @tabla_labores IS NULL
    OR (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = @tabla_labores AND COLUMN_NAME = 'costo_total') = 0,
    'SELECT 1',
    CONCAT('ALTER TABLE ', @tabla_labores, ' MODIFY COLUMN costo_total DECIMAL(15,2) DEFAULT 0.00')
);
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SELECT 'Migración V1_18 costo_total labores aplicada (modo defensivo)' AS mensaje;
