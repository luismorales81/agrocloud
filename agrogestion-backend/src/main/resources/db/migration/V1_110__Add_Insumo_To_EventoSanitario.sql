-- ============================================================================
-- MIGRACIÓN: Agregar campo insumo_id a EventoSanitario
-- Versión: V1_110
-- Fecha: 2025-01-XX
-- ============================================================================
-- Esta migración agrega el campo insumo_id a porcinos_eventos_sanitarios
-- para poder asociar vacunas/medicamentos (Insumo) con eventos sanitarios
-- y permitir el descuento automático de stock

-- Verificar si la columna ya existe antes de agregarla
SET @dbname = DATABASE();
SET @tablename = 'porcinos_eventos_sanitarios';
SET @preparedStatement = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = @dbname
     AND TABLE_NAME = @tablename
     AND COLUMN_NAME = 'insumo_id') > 0,
    'SELECT 1',
    'ALTER TABLE porcinos_eventos_sanitarios 
     ADD COLUMN insumo_id BIGINT NULL COMMENT ''ID del insumo (vacuna/medicamento) usado'',
     ADD INDEX idx_evento_sanitario_insumo (insumo_id),
     ADD FOREIGN KEY (insumo_id) REFERENCES cultivo_insumos(id) ON DELETE SET NULL'
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

