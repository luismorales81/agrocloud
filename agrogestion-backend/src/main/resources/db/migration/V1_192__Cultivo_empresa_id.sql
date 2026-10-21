-- La entidad Cultivo persiste empresa_id. Un esquema solo Flyway no tenía esa columna.

SET @esquema = DATABASE();

SET @sql = (
    SELECT IF(
        NOT EXISTS (
            SELECT 1 FROM information_schema.TABLES
            WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'cultivo_cultivos'
        ) OR EXISTS (
            SELECT 1 FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = @esquema
              AND TABLE_NAME = 'cultivo_cultivos'
              AND COLUMN_NAME = 'empresa_id'
        ),
        'SELECT 1',
        'ALTER TABLE cultivo_cultivos ADD COLUMN empresa_id BIGINT NULL'
    )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(
        NOT EXISTS (
            SELECT 1 FROM information_schema.TABLES
            WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'cultivo_maquinaria'
        ) OR EXISTS (
            SELECT 1 FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = @esquema
              AND TABLE_NAME = 'cultivo_maquinaria'
              AND COLUMN_NAME = 'user_id'
        ),
        'SELECT 1',
        'ALTER TABLE cultivo_maquinaria ADD COLUMN user_id BIGINT NULL'
    )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
