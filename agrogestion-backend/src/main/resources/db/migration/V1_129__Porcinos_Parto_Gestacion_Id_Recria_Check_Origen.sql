-- ============================================================================
-- Spec: Parto.gestacion_id, CHECK cantidad_animales >= 0, actualizar origen NULL.
-- ============================================================================

-- Parto: columna gestacion_id (nullable)
SET @dbname = DATABASE();
SET @tablename = 'porcinos_partos';
SET @columnname = 'gestacion_id';

SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
   WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = @columnname) > 0,
  'SELECT 1',
  'ALTER TABLE porcinos_partos ADD COLUMN gestacion_id BIGINT NULL AFTER madre_id'
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Índice para consultas por gestacion_id
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
   WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 'porcinos_partos' AND INDEX_NAME = 'idx_partos_gestacion') > 0,
  'SELECT 1',
  'CREATE INDEX idx_partos_gestacion ON porcinos_partos(gestacion_id)'
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Recría: corregir negativos antes del CHECK (invariante de spec)
UPDATE porcinos_recria SET cantidad_animales = 0 WHERE cantidad_animales < 0;
ALTER TABLE porcinos_recria ADD CONSTRAINT chk_recria_cantidad_animales CHECK (cantidad_animales >= 0);

-- Recría: actualizar origen NULL a EXTERNO (datos existentes)
UPDATE porcinos_recria SET origen = 'EXTERNO' WHERE origen IS NULL;
