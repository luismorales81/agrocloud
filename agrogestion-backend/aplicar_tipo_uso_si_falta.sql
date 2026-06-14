-- Ejecutar este script si GET /api/calendario/eventos devuelve 500 por falta de la columna tipo_uso.
-- Añade la columna tipo_uso a cultivo_lotes solo si no existe (MySQL 5.7+).

SET @dbname = DATABASE();
SET @tablename = 'cultivo_lotes';
SET @columnname = 'tipo_uso';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
   WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = @columnname) > 0,
  'SELECT 1',
  CONCAT(
    'ALTER TABLE ', @tablename, ' ',
    'ADD COLUMN ', @columnname, ' VARCHAR(20) NULL DEFAULT ''CULTIVO'' ',
    'COMMENT ''CULTIVO = lote de cultivo, PORCINO = corral/lote de porcinos'';'
  )
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- Índice (ignorar error si ya existe)
SET @idx = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'cultivo_lotes' AND INDEX_NAME = 'idx_cultivo_lotes_tipo_uso');
SET @sql = IF(@idx = 0, 'CREATE INDEX idx_cultivo_lotes_tipo_uso ON cultivo_lotes(tipo_uso)', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Marcar como PORCINO los lotes usados en recría
UPDATE cultivo_lotes cl
INNER JOIN (SELECT DISTINCT lote_id FROM porcinos_recria WHERE lote_id IS NOT NULL) r ON cl.id = r.lote_id
SET cl.tipo_uso = 'PORCINO';

SELECT 'Columna tipo_uso aplicada correctamente.' AS resultado;
