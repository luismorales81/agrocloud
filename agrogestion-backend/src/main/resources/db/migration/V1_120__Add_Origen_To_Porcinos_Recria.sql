-- ============================================================================
-- Migración: Campo de origen para diferenciar recrías por destete vs. ingreso externo
-- Tabla: porcinos_recria
-- Campo: origen (DESTETE | EXTERNO)
-- ============================================================================

SET @dbname = DATABASE();
SET @tablename = 'porcinos_recria';
SET @columnname = 'origen';

SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*)
   FROM INFORMATION_SCHEMA.COLUMNS
   WHERE TABLE_SCHEMA = @dbname
     AND TABLE_NAME = @tablename
     AND COLUMN_NAME = @columnname) > 0,
  'SELECT 1',
  'ALTER TABLE porcinos_recria ADD COLUMN origen VARCHAR(20) NULL AFTER fecha_salida'
));

PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

