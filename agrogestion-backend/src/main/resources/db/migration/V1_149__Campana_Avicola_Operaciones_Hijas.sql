-- campana_id en operaciones avícolas hijas (consumos y ventas)

SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'avicola_huevo_consumo' AND column_name = 'campana_id') = 0, 'ALTER TABLE avicola_huevo_consumo ADD COLUMN campana_id BIGINT NULL', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'avicola_ponedoras_consumo' AND column_name = 'campana_id') = 0, 'ALTER TABLE avicola_ponedoras_consumo ADD COLUMN campana_id BIGINT NULL', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'avicola_ponedoras_venta_huevos' AND column_name = 'campana_id') = 0, 'ALTER TABLE avicola_ponedoras_venta_huevos ADD COLUMN campana_id BIGINT NULL', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'avicola_consumo' AND column_name = 'campana_id') = 0, 'ALTER TABLE avicola_consumo ADD COLUMN campana_id BIGINT NULL', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'avicola_venta' AND column_name = 'campana_id') = 0, 'ALTER TABLE avicola_venta ADD COLUMN campana_id BIGINT NULL', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
