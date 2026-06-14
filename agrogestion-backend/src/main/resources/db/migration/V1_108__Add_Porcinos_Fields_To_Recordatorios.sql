-- Agregar campos para módulo porcinos a la tabla recordatorios (idempotente)

SET @esquema = DATABASE();

SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'recordatorios' AND COLUMN_NAME = 'servicio_id') > 0,
  'SELECT 1',
  'ALTER TABLE recordatorios ADD COLUMN servicio_id BIGINT DEFAULT NULL COMMENT ''ID del servicio relacionado (módulo porcinos)'''));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'recordatorios' AND COLUMN_NAME = 'gestacion_id') > 0,
  'SELECT 1',
  'ALTER TABLE recordatorios ADD COLUMN gestacion_id BIGINT DEFAULT NULL COMMENT ''ID de la gestación relacionada (módulo porcinos)'''));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'recordatorios' AND COLUMN_NAME = 'parto_id') > 0,
  'SELECT 1',
  'ALTER TABLE recordatorios ADD COLUMN parto_id BIGINT DEFAULT NULL COMMENT ''ID del parto relacionado (módulo porcinos)'''));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'recordatorios' AND COLUMN_NAME = 'madre_id') > 0,
  'SELECT 1',
  'ALTER TABLE recordatorios ADD COLUMN madre_id BIGINT DEFAULT NULL COMMENT ''ID de la madre relacionada (módulo porcinos)'''));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.STATISTICS
   WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'recordatorios' AND INDEX_NAME = 'idx_servicio_id') > 0,
  'SELECT 1',
  'CREATE INDEX idx_servicio_id ON recordatorios(servicio_id)'));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.STATISTICS
   WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'recordatorios' AND INDEX_NAME = 'idx_gestacion_id') > 0,
  'SELECT 1',
  'CREATE INDEX idx_gestacion_id ON recordatorios(gestacion_id)'));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.STATISTICS
   WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'recordatorios' AND INDEX_NAME = 'idx_parto_id') > 0,
  'SELECT 1',
  'CREATE INDEX idx_parto_id ON recordatorios(parto_id)'));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.STATISTICS
   WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'recordatorios' AND INDEX_NAME = 'idx_madre_id') > 0,
  'SELECT 1',
  'CREATE INDEX idx_madre_id ON recordatorios(madre_id)'));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'recordatorios' AND COLUMN_NAME = 'tipo'
     AND COLUMN_TYPE LIKE '%PARTO%') > 0,
  'SELECT 1',
  'ALTER TABLE recordatorios MODIFY COLUMN tipo ENUM(''GENERAL'',''LABOR'',''COSECHA'',''MANTENIMIENTO'',''INSUMO'',''REUNION'',''PARTO'',''REPRODUCCION'',''SANIDAD'',''ALIMENTACION'',''OTRO'') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT ''GENERAL'''));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;
