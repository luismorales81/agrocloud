-- Migración V1.8: Actualizar tabla lotes con campos de estado (defensiva)
-- Solo aplica si existe la tabla legacy `lotes` o `cultivo_lotes`.

SET @esquema = DATABASE();

SET @tabla_lotes = (
    SELECT CASE
        WHEN EXISTS (SELECT 1 FROM information_schema.tables t WHERE t.table_schema = @esquema AND t.table_name = 'cultivo_lotes')
            THEN 'cultivo_lotes'
        WHEN EXISTS (SELECT 1 FROM information_schema.tables t WHERE t.table_schema = @esquema AND t.table_name = 'lotes')
            THEN 'lotes'
        ELSE NULL
    END
);

-- 1. Agregar columnas nuevas si no existen
SET @sql = IF(@tabla_lotes IS NULL, 'SELECT 1',
    IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = @tabla_lotes AND COLUMN_NAME = 'fecha_ultimo_cambio_estado') > 0,
        'SELECT 1',
        CONCAT('ALTER TABLE ', @tabla_lotes, ' ADD COLUMN fecha_ultimo_cambio_estado TIMESTAMP NULL, ADD COLUMN motivo_cambio_estado VARCHAR(255) NULL, ADD COLUMN fecha_cosecha_real DATE NULL, ADD COLUMN rendimiento_esperado DECIMAL(10,2) NULL, ADD COLUMN rendimiento_real DECIMAL(10,2) NULL')
    )
);
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- 2. Ampliar columna estado
SET @sql = IF(@tabla_lotes IS NULL, 'SELECT 1',
    CONCAT('ALTER TABLE ', @tabla_lotes, ' MODIFY COLUMN estado VARCHAR(50) NOT NULL DEFAULT ''DISPONIBLE''')
);
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- 3. Actualizar valores legacy
SET @sql = IF(@tabla_lotes IS NULL, 'SELECT 1',
    CONCAT('UPDATE ', @tabla_lotes, ' SET estado = CASE WHEN estado = ''DISPONIBLE'' THEN ''DISPONIBLE'' WHEN estado = ''OCUPADO'' THEN ''SEMBRADO'' WHEN estado = ''EN_DESCANSO'' THEN ''EN_DESCANSO'' ELSE ''DISPONIBLE'' END')
);
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- 4. Índices (solo si no existen)
SET @sql = IF(@tabla_lotes IS NULL OR (SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = @esquema AND table_name = @tabla_lotes AND index_name = 'idx_lotes_estado') > 0,
    'SELECT 1', CONCAT('CREATE INDEX idx_lotes_estado ON ', @tabla_lotes, '(estado)'));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = IF(@tabla_lotes IS NULL OR (SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = @esquema AND table_name = @tabla_lotes AND index_name = 'idx_lotes_fecha_cambio_estado') > 0,
    'SELECT 1', CONCAT('CREATE INDEX idx_lotes_fecha_cambio_estado ON ', @tabla_lotes, '(fecha_ultimo_cambio_estado)'));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = IF(@tabla_lotes IS NULL OR (SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = @esquema AND table_name = @tabla_lotes AND index_name = 'idx_lotes_fecha_siembra') > 0,
    'SELECT 1', CONCAT('CREATE INDEX idx_lotes_fecha_siembra ON ', @tabla_lotes, '(fecha_siembra)'));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = IF(@tabla_lotes IS NULL OR (SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = @esquema AND table_name = @tabla_lotes AND index_name = 'idx_lotes_fecha_cosecha_esperada') > 0,
    'SELECT 1', CONCAT('CREATE INDEX idx_lotes_fecha_cosecha_esperada ON ', @tabla_lotes, '(fecha_cosecha_esperada)'));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SELECT 'Migración V1_8 lotes aplicada (modo defensivo)' AS mensaje;
