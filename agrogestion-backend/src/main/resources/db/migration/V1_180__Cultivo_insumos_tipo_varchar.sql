-- El wizard guarda tipos (COADYUVANTE, OTRO, etc.) que no cabían en el ENUM de MySQL
-- ni en el enum Java; eso rompía GET /api/insumos al hidratar la entidad.
SET @esquema = DATABASE();

UPDATE cultivo_insumos SET tipo = 'OTROS' WHERE tipo IN ('OTRO', '');

SET @sql = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'cultivo_insumos' AND COLUMN_NAME = 'tipo'
        ),
        'ALTER TABLE cultivo_insumos MODIFY COLUMN tipo VARCHAR(50) NOT NULL',
        'SELECT 1'
    )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
