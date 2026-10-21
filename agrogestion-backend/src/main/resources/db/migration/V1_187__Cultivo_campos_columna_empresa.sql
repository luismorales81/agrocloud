-- La entidad Field mapea empresa_id. El baseline de campos no la tenía.
SET @esquema = DATABASE();

SET @sql = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = @esquema
              AND TABLE_NAME = 'cultivo_campos'
              AND COLUMN_NAME = 'empresa_id'
        ),
        'SELECT 1',
        'ALTER TABLE cultivo_campos ADD COLUMN empresa_id BIGINT NULL'
    )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @tiene_usuario_empresas = (
    SELECT COUNT(*) FROM information_schema.tables
    WHERE table_schema = @esquema AND table_name = 'usuario_empresas'
);

SET @sql = IF(
    @tiene_usuario_empresas = 0,
    'SELECT 1',
    'UPDATE cultivo_campos c
     SET c.empresa_id = (
         SELECT ue.empresa_id FROM usuario_empresas ue
         WHERE ue.usuario_id = c.user_id
         LIMIT 1
     )
     WHERE c.empresa_id IS NULL AND c.user_id IS NOT NULL'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @tiene_roles = (
    SELECT COUNT(*) FROM information_schema.tables
    WHERE table_schema = @esquema AND table_name = 'usuarios_empresas_roles'
);

SET @sql = IF(
    @tiene_roles = 0,
    'SELECT 1',
    'UPDATE cultivo_campos c
     SET c.empresa_id = (
         SELECT ucr.empresa_id FROM usuarios_empresas_roles ucr
         WHERE ucr.usuario_id = c.user_id
         LIMIT 1
     )
     WHERE c.empresa_id IS NULL AND c.user_id IS NOT NULL'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.statistics
     WHERE table_schema = @esquema AND table_name = 'cultivo_campos' AND index_name = 'idx_cultivo_campos_empresa') > 0,
    'SELECT 1',
    'CREATE INDEX idx_cultivo_campos_empresa ON cultivo_campos (empresa_id)'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
