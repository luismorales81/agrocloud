-- V1_106__Add_Estado_Configurado_To_Plot.sql
-- Migración para agregar soporte de estados configurados a los lotes (idempotente)

SET @esquema = DATABASE();

SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'cultivo_lotes' AND COLUMN_NAME = 'estado_configurado_id') > 0,
  'SELECT 1',
  'ALTER TABLE cultivo_lotes ADD COLUMN estado_configurado_id BIGINT NULL COMMENT ''ID del estado configurado (opcional, si es NULL usa el enum estado)'''));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
   WHERE CONSTRAINT_SCHEMA = @esquema AND TABLE_NAME = 'cultivo_lotes' AND CONSTRAINT_NAME = 'fk_lote_estado_configurado') > 0,
  'SELECT 1',
  'ALTER TABLE cultivo_lotes ADD CONSTRAINT fk_lote_estado_configurado FOREIGN KEY (estado_configurado_id) REFERENCES cultivo_estados_lote(id) ON DELETE SET NULL'));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.STATISTICS
   WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'cultivo_lotes' AND INDEX_NAME = 'idx_lotes_estado_configurado') > 0,
  'SELECT 1',
  'CREATE INDEX idx_lotes_estado_configurado ON cultivo_lotes(estado_configurado_id)'));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;
