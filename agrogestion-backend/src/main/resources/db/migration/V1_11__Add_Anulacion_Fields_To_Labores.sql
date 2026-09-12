-- Migración V1.11: Campos de anulación en labores (defensiva)

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
    IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = @tabla_labores AND COLUMN_NAME = 'motivo_anulacion') > 0,
        'SELECT 1',
        CONCAT('ALTER TABLE ', @tabla_labores, ' ADD COLUMN motivo_anulacion VARCHAR(1000) NULL, ADD COLUMN fecha_anulacion TIMESTAMP NULL, ADD COLUMN usuario_anulacion_id BIGINT NULL')
    )
);
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = IF(@tabla_labores IS NULL
    OR (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS WHERE CONSTRAINT_SCHEMA = @esquema AND TABLE_NAME = @tabla_labores AND CONSTRAINT_NAME = 'fk_labores_usuario_anulacion') > 0,
    'SELECT 1',
    CONCAT('ALTER TABLE ', @tabla_labores, ' ADD CONSTRAINT fk_labores_usuario_anulacion FOREIGN KEY (usuario_anulacion_id) REFERENCES usuarios(id) ON DELETE SET NULL')
);
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = IF(@tabla_labores IS NULL OR (SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = @esquema AND table_name = @tabla_labores AND index_name = 'idx_labores_usuario_anulacion') > 0,
    'SELECT 1', CONCAT('CREATE INDEX idx_labores_usuario_anulacion ON ', @tabla_labores, '(usuario_anulacion_id)'));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = IF(@tabla_labores IS NULL OR (SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = @esquema AND table_name = @tabla_labores AND index_name = 'idx_labores_fecha_anulacion') > 0,
    'SELECT 1', CONCAT('CREATE INDEX idx_labores_fecha_anulacion ON ', @tabla_labores, '(fecha_anulacion)'));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = IF(@tabla_labores IS NULL, 'SELECT 1',
    CONCAT('ALTER TABLE ', @tabla_labores, ' MODIFY COLUMN estado VARCHAR(50) DEFAULT ''PLANIFICADA''')
);
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SELECT 'Migración V1_11 anulación en labores aplicada (modo defensivo)' AS mensaje;
