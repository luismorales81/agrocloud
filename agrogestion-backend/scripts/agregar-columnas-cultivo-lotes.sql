-- Agrega columnas faltantes en cultivo_lotes y cultivo_labores para alinear la BD con las entidades.
-- Ejecutar si aparecen "Unknown column liberado_para_siembra" o "Unknown column fecha_realizacion".
-- Uso: mysql -u root -p123456 agrocloud < scripts/agregar-columnas-cultivo-lotes.sql

USE agrocloud;

SET @db = DATABASE();

-- ========== cultivo_lotes ==========
-- version (V1_126)
SET @col = 'version';
SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
   WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'cultivo_lotes' AND COLUMN_NAME = @col) > 0,
  'SELECT 1',
  'ALTER TABLE cultivo_lotes ADD COLUMN version BIGINT NOT NULL DEFAULT 0'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- liberado_para_siembra (V1_127)
SET @col = 'liberado_para_siembra';
SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
   WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'cultivo_lotes' AND COLUMN_NAME = @col) > 0,
  'SELECT 1',
  'ALTER TABLE cultivo_lotes ADD COLUMN liberado_para_siembra BOOLEAN NOT NULL DEFAULT false'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ========== cultivo_labores ==========
-- cultivo_id (V1_124)
SET @col = 'cultivo_id';
SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
   WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'cultivo_labores' AND COLUMN_NAME = @col) > 0,
  'SELECT 1',
  'ALTER TABLE cultivo_labores ADD COLUMN cultivo_id BIGINT NULL AFTER lote_id'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @fk_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'cultivo_labores' AND CONSTRAINT_NAME = 'fk_cultivo_labores_cultivo');
SET @sql = (SELECT IF(@fk_exists > 0, 'SELECT 1',
  'ALTER TABLE cultivo_labores ADD CONSTRAINT fk_cultivo_labores_cultivo FOREIGN KEY (cultivo_id) REFERENCES cultivo_cultivos(id) ON DELETE SET NULL'));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'cultivo_labores' AND INDEX_NAME = 'idx_cultivo_labores_cultivo_id');
SET @sql = (SELECT IF(@idx_exists > 0, 'SELECT 1', 'CREATE INDEX idx_cultivo_labores_cultivo_id ON cultivo_labores(cultivo_id)'));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- fecha_realizacion (V1_123)
SET @col = 'fecha_realizacion';
SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
   WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'cultivo_labores' AND COLUMN_NAME = @col) > 0,
  'SELECT 1',
  'ALTER TABLE cultivo_labores ADD COLUMN fecha_realizacion DATE NULL AFTER fecha_fin'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- version en cultivo_labores (V1_126)
SET @col = 'version';
SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
   WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'cultivo_labores' AND COLUMN_NAME = @col) > 0,
  'SELECT 1',
  'ALTER TABLE cultivo_labores ADD COLUMN version BIGINT NOT NULL DEFAULT 0'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ========== Registrar en Flyway ==========
INSERT INTO flyway_schema_history (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success)
SELECT (SELECT COALESCE(MAX(installed_rank), 0) + 1 FROM flyway_schema_history f2), '1.123', 'Add fecha realizacion to cultivo labores', 'SQL', 'V1_123__Add_fecha_realizacion_to_cultivo_labores.sql', NULL, 'root', NOW(), 0, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM flyway_schema_history f WHERE f.version = '1.123');

INSERT INTO flyway_schema_history (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success)
SELECT (SELECT COALESCE(MAX(installed_rank), 0) + 1 FROM flyway_schema_history f2), '1.124', 'Add cultivo id to cultivo labores', 'SQL', 'V1_124__Add_cultivo_id_to_cultivo_labores.sql', NULL, 'root', NOW(), 0, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM flyway_schema_history f WHERE f.version = '1.124');

INSERT INTO flyway_schema_history (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success)
SELECT (SELECT COALESCE(MAX(installed_rank), 0) + 1 FROM flyway_schema_history f2), '1.126', 'Add version Plot Labor', 'SQL', 'V1_126__Add_version_Plot_Labor.sql', NULL, 'root', NOW(), 0, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM flyway_schema_history f WHERE f.version = '1.126');

INSERT INTO flyway_schema_history (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success)
SELECT (SELECT COALESCE(MAX(installed_rank), 0) + 1 FROM flyway_schema_history f2), '1.127', 'Add liberado para siembra to Plot', 'SQL', 'V1_127__Add_liberado_para_siembra_to_Plot.sql', NULL, 'root', NOW(), 0, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM flyway_schema_history f WHERE f.version = '1.127');

SELECT 'Columnas cultivo_lotes y cultivo_labores verificadas.' AS resultado;
