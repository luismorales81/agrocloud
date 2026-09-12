-- Índice compuesto para listados frecuentes de labores por lote (defensivo)

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
    OR (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = @tabla_labores AND COLUMN_NAME = 'activo') = 0,
    IF(
        @tabla_labores IS NULL,
        'SELECT 1',
        CONCAT('ALTER TABLE ', @tabla_labores, ' ADD COLUMN activo BOOLEAN NOT NULL DEFAULT TRUE')
    ),
    'SELECT 1'
);
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = IF(
    @tabla_labores IS NULL
    OR (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = @tabla_labores AND COLUMN_NAME = 'lote_id') = 0
    OR (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = @tabla_labores AND COLUMN_NAME = 'fecha_inicio') = 0,
    'SELECT 1',
    IF(
        (SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = @esquema AND table_name = @tabla_labores AND index_name = 'idx_cultivo_labores_lote_activo_fecha') > 0,
        'SELECT 1',
        CONCAT('CREATE INDEX idx_cultivo_labores_lote_activo_fecha ON ', @tabla_labores, ' (lote_id, activo, fecha_inicio)')
    )
);
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SELECT 'Migración V1_147 índice labores aplicada (modo defensivo)' AS mensaje;
