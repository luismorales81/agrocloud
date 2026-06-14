-- ============================================================================
-- Migración: Campos para registro histórico de peso por lote (recría/engorde)
-- - porcinos_registros_peso: metodo (BALANZA, MUESTREO, ESTIMADO), etapa_al_momento
-- - porcinos_recria: peso_inicial_kg, fecha_ultima_pesada, fecha_inicio_etapa
-- ============================================================================

SET @dbname = DATABASE();

-- porcinos_registros_peso: metodo
SET @tablename = 'porcinos_registros_peso';
SET @columnname = 'metodo';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = @columnname) > 0,
  'SELECT 1',
  'ALTER TABLE porcinos_registros_peso ADD COLUMN metodo VARCHAR(20) NOT NULL DEFAULT ''BALANZA'' AFTER cantidad_animales'
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- porcinos_registros_peso: etapa_al_momento
SET @columnname = 'etapa_al_momento';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = @columnname) > 0,
  'SELECT 1',
  'ALTER TABLE porcinos_registros_peso ADD COLUMN etapa_al_momento VARCHAR(20) NULL AFTER metodo'
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- porcinos_recria: peso_inicial_kg
SET @tablename = 'porcinos_recria';
SET @columnname = 'peso_inicial_kg';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = @columnname) > 0,
  'SELECT 1',
  'ALTER TABLE porcinos_recria ADD COLUMN peso_inicial_kg DECIMAL(10,2) NULL AFTER peso_promedio'
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- porcinos_recria: fecha_ultima_pesada
SET @columnname = 'fecha_ultima_pesada';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = @columnname) > 0,
  'SELECT 1',
  'ALTER TABLE porcinos_recria ADD COLUMN fecha_ultima_pesada DATE NULL AFTER peso_inicial_kg'
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- porcinos_recria: fecha_inicio_etapa
SET @columnname = 'fecha_inicio_etapa';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = @columnname) > 0,
  'SELECT 1',
  'ALTER TABLE porcinos_recria ADD COLUMN fecha_inicio_etapa DATE NULL AFTER fecha_ultima_pesada'
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
