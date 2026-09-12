-- V1_9: Agregar responsable a labores (defensiva)

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

SET @sql = IF(@tabla_labores IS NULL, 'SELECT 1',
    IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = @tabla_labores AND COLUMN_NAME = 'responsable') > 0,
        'SELECT 1',
        CONCAT('ALTER TABLE ', @tabla_labores, ' ADD COLUMN responsable VARCHAR(255) NULL COMMENT ''Nombre del responsable de la labor''')
    )
);
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = IF(@tabla_labores IS NULL OR (SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = @esquema AND table_name = @tabla_labores AND index_name = 'idx_labores_responsable') > 0,
    'SELECT 1', CONCAT('CREATE INDEX idx_labores_responsable ON ', @tabla_labores, '(responsable)'));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SELECT 'Migración V1_9 responsable en labores aplicada (modo defensivo)' AS mensaje;
