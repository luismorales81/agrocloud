-- V1_10: Agregar empresa_id a lotes (defensiva)

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

SET @existe_empresas = (
    SELECT COUNT(*) FROM information_schema.tables t
    WHERE t.table_schema = @esquema AND t.table_name = 'empresas'
);

SET @sql = IF(@tabla_lotes IS NULL, 'SELECT 1',
    IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = @tabla_lotes AND COLUMN_NAME = 'empresa_id') > 0,
        'SELECT 1',
        CONCAT('ALTER TABLE ', @tabla_lotes, ' ADD COLUMN empresa_id BIGINT NULL COMMENT ''ID de la empresa propietaria del lote''')
    )
);
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = IF(@tabla_lotes IS NULL OR (SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = @esquema AND table_name = @tabla_lotes AND index_name = 'idx_lotes_empresa') > 0,
    'SELECT 1', CONCAT('CREATE INDEX idx_lotes_empresa ON ', @tabla_lotes, '(empresa_id)'));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = IF(@tabla_lotes IS NULL OR @existe_empresas = 0
    OR (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS WHERE CONSTRAINT_SCHEMA = @esquema AND TABLE_NAME = @tabla_lotes AND CONSTRAINT_NAME = 'fk_lotes_empresa') > 0,
    'SELECT 1',
    CONCAT('ALTER TABLE ', @tabla_lotes, ' ADD CONSTRAINT fk_lotes_empresa FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE SET NULL')
);
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- Actualizar lotes solo si existen columnas user_id y tabla usuarios_empresas_roles
SET @tiene_user_id = IF(@tabla_lotes IS NULL, 0,
    (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = @tabla_lotes AND COLUMN_NAME = 'user_id'));
SET @tiene_ucr = (
    SELECT COUNT(*) FROM information_schema.tables t
    WHERE t.table_schema = @esquema AND t.table_name = 'usuarios_empresas_roles'
);

SET @sql = IF(@tabla_lotes IS NULL OR @tiene_user_id = 0 OR @tiene_ucr = 0, 'SELECT 1',
    CONCAT('UPDATE ', @tabla_lotes, ' l SET empresa_id = (SELECT ucr.empresa_id FROM usuarios_empresas_roles ucr WHERE ucr.usuario_id = l.user_id LIMIT 1) WHERE l.empresa_id IS NULL')
);
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SELECT 'Migración V1_10 empresa en lotes aplicada (modo defensivo)' AS mensaje;
