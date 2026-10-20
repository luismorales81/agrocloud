-- La entidad Insumo mapea activo, pero el baseline de insumos/cultivo_insumos no tenía esa columna.
SET @esquema = DATABASE();

SET @sql = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = @esquema
              AND TABLE_NAME = 'cultivo_insumos'
              AND COLUMN_NAME = 'activo'
        ),
        'SELECT 1',
        'ALTER TABLE cultivo_insumos ADD COLUMN activo BOOLEAN NOT NULL DEFAULT TRUE'
    )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
