-- Trazabilidad: origen del movimiento (CULTIVOS, PORCINOS) y ID de referencia (idempotente)

SET @esquema = DATABASE();

SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'cultivo_movimientos_inventario' AND COLUMN_NAME = 'origen') > 0,
  'SELECT 1',
  'ALTER TABLE cultivo_movimientos_inventario ADD COLUMN origen VARCHAR(50) NULL'));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'cultivo_movimientos_inventario' AND COLUMN_NAME = 'referencia_id') > 0,
  'SELECT 1',
  'ALTER TABLE cultivo_movimientos_inventario ADD COLUMN referencia_id BIGINT NULL'));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;
