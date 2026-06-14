-- V1_116: Añadir tipo_uso a cultivo_lotes (idempotente)

SET @esquema = DATABASE();

SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'cultivo_lotes' AND COLUMN_NAME = 'tipo_uso') > 0,
  'SELECT 1',
  'ALTER TABLE cultivo_lotes ADD COLUMN tipo_uso VARCHAR(20) NOT NULL DEFAULT ''CULTIVO'' COMMENT ''CULTIVO = lote de cultivo, PORCINO = corral/lote de porcinos'''
));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.STATISTICS
   WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'cultivo_lotes' AND INDEX_NAME = 'idx_cultivo_lotes_tipo_uso') > 0,
  'SELECT 1',
  'CREATE INDEX idx_cultivo_lotes_tipo_uso ON cultivo_lotes(tipo_uso)'));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

UPDATE cultivo_lotes cl
INNER JOIN (SELECT DISTINCT lote_id FROM porcinos_recria WHERE lote_id IS NOT NULL) r ON cl.id = r.lote_id
SET cl.tipo_uso = 'PORCINO';
