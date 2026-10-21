-- La entidad Plot mapea activo, pero cultivo_lotes nunca tuvo esa columna.
SET @esquema = DATABASE();

SET @sql = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = @esquema
              AND TABLE_NAME = 'cultivo_lotes'
              AND COLUMN_NAME = 'activo'
        ),
        'SELECT 1',
        'ALTER TABLE cultivo_lotes ADD COLUMN activo BOOLEAN NOT NULL DEFAULT TRUE'
    )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
