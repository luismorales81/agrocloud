-- Campos agroquímicos: el esquema actual usa cultivo_insumos (tabla legacy insumos eliminada/renombrada).
-- Idempotente: solo añade columnas en cultivo_insumos si faltan.
-- Tablas dosis_insumos / aplicaciones legacy omitidas si no existen en la base.

SET @esquema = DATABASE();

SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'cultivo_insumos' AND COLUMN_NAME = 'principio_activo') > 0,
  'SELECT 1',
  'ALTER TABLE cultivo_insumos ADD COLUMN principio_activo VARCHAR(200) NULL'));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'cultivo_insumos' AND COLUMN_NAME = 'concentracion') > 0,
  'SELECT 1',
  'ALTER TABLE cultivo_insumos ADD COLUMN concentracion VARCHAR(100) NULL'));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'cultivo_insumos' AND COLUMN_NAME = 'clase_quimica') > 0,
  'SELECT 1',
  'ALTER TABLE cultivo_insumos ADD COLUMN clase_quimica VARCHAR(100) NULL'));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'cultivo_insumos' AND COLUMN_NAME = 'categoria_toxicologica') > 0,
  'SELECT 1',
  'ALTER TABLE cultivo_insumos ADD COLUMN categoria_toxicologica VARCHAR(50) NULL'));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'cultivo_insumos' AND COLUMN_NAME = 'periodo_carencia_dias') > 0,
  'SELECT 1',
  'ALTER TABLE cultivo_insumos ADD COLUMN periodo_carencia_dias INT NULL'));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'cultivo_insumos' AND COLUMN_NAME = 'dosis_minima_por_ha') > 0,
  'SELECT 1',
  'ALTER TABLE cultivo_insumos ADD COLUMN dosis_minima_por_ha DECIMAL(10,2) NULL'));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'cultivo_insumos' AND COLUMN_NAME = 'dosis_maxima_por_ha') > 0,
  'SELECT 1',
  'ALTER TABLE cultivo_insumos ADD COLUMN dosis_maxima_por_ha DECIMAL(10,2) NULL'));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'cultivo_insumos' AND COLUMN_NAME = 'unidad_dosis') > 0,
  'SELECT 1',
  'ALTER TABLE cultivo_insumos ADD COLUMN unidad_dosis VARCHAR(50) NULL'));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;
