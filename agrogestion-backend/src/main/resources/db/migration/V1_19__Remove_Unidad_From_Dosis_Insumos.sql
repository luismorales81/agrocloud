-- Eliminar columna unidad de dosis_insumos (defensiva)

SET @esquema = DATABASE();

SET @tabla_dosis = (
    SELECT CASE
        WHEN EXISTS (SELECT 1 FROM information_schema.tables t WHERE t.table_schema = @esquema AND t.table_name = 'cultivo_dosis_insumos')
            THEN 'cultivo_dosis_insumos'
        WHEN EXISTS (SELECT 1 FROM information_schema.tables t WHERE t.table_schema = @esquema AND t.table_name = 'dosis_insumos')
            THEN 'dosis_insumos'
        ELSE NULL
    END
);

SET @sql = IF(
    @tabla_dosis IS NULL
    OR (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = @tabla_dosis AND COLUMN_NAME = 'unidad') = 0,
    'SELECT 1',
    CONCAT('ALTER TABLE ', @tabla_dosis, ' DROP COLUMN unidad')
);
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SELECT 'Migración V1_19 dosis_insumos aplicada (modo defensivo)' AS mensaje;
