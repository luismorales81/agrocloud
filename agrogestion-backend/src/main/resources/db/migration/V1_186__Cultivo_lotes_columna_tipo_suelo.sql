-- La entidad Plot mapea tipo_suelo. En el baseline esa columna está en campos, no en cultivo_lotes.
SET @esquema = DATABASE();

SET @sql = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = @esquema
              AND TABLE_NAME = 'cultivo_lotes'
              AND COLUMN_NAME = 'tipo_suelo'
        ),
        'SELECT 1',
        'ALTER TABLE cultivo_lotes ADD COLUMN tipo_suelo VARCHAR(100) NULL'
    )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
