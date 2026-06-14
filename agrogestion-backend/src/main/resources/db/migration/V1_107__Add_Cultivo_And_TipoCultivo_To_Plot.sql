-- Migración: relaciones cultivo_id y tipo_cultivo_id en cultivo_lotes (idempotente)

SET @esquema = DATABASE();

SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'cultivo_lotes' AND COLUMN_NAME = 'cultivo_id') > 0,
  'SELECT 1',
  'ALTER TABLE cultivo_lotes ADD COLUMN cultivo_id BIGINT NULL'));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'cultivo_lotes' AND COLUMN_NAME = 'tipo_cultivo_id') > 0,
  'SELECT 1',
  'ALTER TABLE cultivo_lotes ADD COLUMN tipo_cultivo_id BIGINT NULL'));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
   WHERE CONSTRAINT_SCHEMA = @esquema AND TABLE_NAME = 'cultivo_lotes' AND CONSTRAINT_NAME = 'fk_plot_cultivo') > 0,
  'SELECT 1',
  'ALTER TABLE cultivo_lotes ADD CONSTRAINT fk_plot_cultivo FOREIGN KEY (cultivo_id) REFERENCES cultivo_cultivos(id) ON DELETE SET NULL'));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
   WHERE CONSTRAINT_SCHEMA = @esquema AND TABLE_NAME = 'cultivo_lotes' AND CONSTRAINT_NAME = 'fk_plot_tipo_cultivo') > 0,
  'SELECT 1',
  'ALTER TABLE cultivo_lotes ADD CONSTRAINT fk_plot_tipo_cultivo FOREIGN KEY (tipo_cultivo_id) REFERENCES cultivo_tipos_cultivo(id) ON DELETE SET NULL'));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.STATISTICS
   WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'cultivo_lotes' AND INDEX_NAME = 'idx_plot_cultivo_id') > 0,
  'SELECT 1',
  'CREATE INDEX idx_plot_cultivo_id ON cultivo_lotes(cultivo_id)'));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.STATISTICS
   WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'cultivo_lotes' AND INDEX_NAME = 'idx_plot_tipo_cultivo_id') > 0,
  'SELECT 1',
  'CREATE INDEX idx_plot_tipo_cultivo_id ON cultivo_lotes(tipo_cultivo_id)'));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;
