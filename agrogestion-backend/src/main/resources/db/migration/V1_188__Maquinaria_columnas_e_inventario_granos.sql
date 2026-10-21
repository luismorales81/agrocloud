-- Alinea maquinaria con la entidad y devuelve el inventario de granos al nombre que usa Hibernate.
SET @esquema = DATABASE();

SET @sql = (SELECT IF(EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'cultivo_maquinaria' AND COLUMN_NAME = 'activo'), 'SELECT 1', 'ALTER TABLE cultivo_maquinaria ADD COLUMN activo BOOLEAN NOT NULL DEFAULT TRUE'));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql = (SELECT IF(EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'cultivo_maquinaria' AND COLUMN_NAME = 'anio_fabricacion'), 'SELECT 1', 'ALTER TABLE cultivo_maquinaria ADD COLUMN anio_fabricacion INT NULL'));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql = (SELECT IF(EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'cultivo_maquinaria' AND COLUMN_NAME = 'numero_serie'), 'SELECT 1', 'ALTER TABLE cultivo_maquinaria ADD COLUMN numero_serie VARCHAR(100) NULL'));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql = (SELECT IF(EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'cultivo_maquinaria' AND COLUMN_NAME = 'kilometros_uso'), 'SELECT 1', 'ALTER TABLE cultivo_maquinaria ADD COLUMN kilometros_uso DECIMAL(10,2) NULL DEFAULT 0'));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql = (SELECT IF(EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'cultivo_maquinaria' AND COLUMN_NAME = 'kilometros_mantenimiento_intervalo'), 'SELECT 1', 'ALTER TABLE cultivo_maquinaria ADD COLUMN kilometros_mantenimiento_intervalo INT NULL DEFAULT 5000'));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql = (SELECT IF(EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'cultivo_maquinaria' AND COLUMN_NAME = 'ultimo_mantenimiento_kilometros'), 'SELECT 1', 'ALTER TABLE cultivo_maquinaria ADD COLUMN ultimo_mantenimiento_kilometros DECIMAL(10,2) NULL DEFAULT 0'));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql = (SELECT IF(EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'cultivo_maquinaria' AND COLUMN_NAME = 'rendimiento_combustible'), 'SELECT 1', 'ALTER TABLE cultivo_maquinaria ADD COLUMN rendimiento_combustible DECIMAL(8,2) NULL DEFAULT 0'));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql = (SELECT IF(EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'cultivo_maquinaria' AND COLUMN_NAME = 'unidad_rendimiento'), 'SELECT 1', 'ALTER TABLE cultivo_maquinaria ADD COLUMN unidad_rendimiento VARCHAR(20) NULL DEFAULT ''km/L'''));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql = (SELECT IF(EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'cultivo_maquinaria' AND COLUMN_NAME = 'costo_combustible_por_litro'), 'SELECT 1', 'ALTER TABLE cultivo_maquinaria ADD COLUMN costo_combustible_por_litro DECIMAL(8,2) NULL DEFAULT 0'));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql = (SELECT IF(EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'cultivo_maquinaria' AND COLUMN_NAME = 'valor_actual'), 'SELECT 1', 'ALTER TABLE cultivo_maquinaria ADD COLUMN valor_actual DECIMAL(12,2) NULL DEFAULT 0'));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql = (SELECT IF(EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'cultivo_maquinaria' AND COLUMN_NAME = 'fecha_compra'), 'SELECT 1', 'ALTER TABLE cultivo_maquinaria ADD COLUMN fecha_compra DATE NULL'));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (SELECT IF(
    EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'cultivo_maquinaria' AND COLUMN_NAME = 'anio'),
    'UPDATE cultivo_maquinaria SET anio_fabricacion = anio WHERE anio_fabricacion IS NULL AND anio IS NOT NULL',
    'SELECT 1'));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (SELECT IF(
    EXISTS (SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'cultivo_inventario_granos')
    AND NOT EXISTS (SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'inventario_granos'),
    'RENAME TABLE cultivo_inventario_granos TO inventario_granos',
    'SELECT 1'));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (SELECT IF(
    EXISTS (SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'cultivo_movimientos_inventario_granos')
    AND NOT EXISTS (SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'movimientos_inventario_granos'),
    'RENAME TABLE cultivo_movimientos_inventario_granos TO movimientos_inventario_granos',
    'SELECT 1'));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

INSERT INTO inventario_granos (
    cosecha_id, cultivo_id, lote_id, usuario_id,
    cantidad_inicial, cantidad_disponible, unidad_medida,
    costo_produccion_total, costo_unitario, fecha_ingreso, estado,
    variedad, calidad, ubicacion_almacenamiento, observaciones, campana_id
)
SELECT 1, 4, 3, 1, 96.00, 96.00, 'tn', 4800000.00, 50000.00, '2025-11-18', 'DISPONIBLE',
       'ACA 602', 'Grado 2', 'Acopio La Banda, Santiago del Estero',
       'Trigo cosechado en el lote de Los Cardones.', 1
FROM DUAL
WHERE EXISTS (SELECT 1 FROM cultivo_historial_cosechas WHERE id = 1)
  AND NOT EXISTS (SELECT 1 FROM inventario_granos WHERE cosecha_id = 1);
