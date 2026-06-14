-- ============================================================================
-- Spec: Concurrencia optimista en Recria (campo version).
-- ============================================================================

SET @dbname = DATABASE();
SET @tablename = 'porcinos_recria';
SET @columnname = 'version';

SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
   WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = @columnname) > 0,
  'SELECT 1',
  'ALTER TABLE porcinos_recria ADD COLUMN version BIGINT NULL DEFAULT 0'
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE porcinos_recria SET version = 0 WHERE version IS NULL;
