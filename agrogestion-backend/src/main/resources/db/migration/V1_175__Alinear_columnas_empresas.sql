-- Columnas de empresas que espera la entidad Empresa.

SET @esquema = DATABASE();

SET @c = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@esquema AND TABLE_NAME='empresas' AND COLUMN_NAME='descripcion');
SET @sql = IF(@c=0, 'ALTER TABLE empresas ADD COLUMN descripcion TEXT NULL', 'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @c = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@esquema AND TABLE_NAME='empresas' AND COLUMN_NAME='fecha_inicio_trial');
SET @sql = IF(@c=0, 'ALTER TABLE empresas ADD COLUMN fecha_inicio_trial DATE NULL', 'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @c = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@esquema AND TABLE_NAME='empresas' AND COLUMN_NAME='fecha_fin_trial');
SET @sql = IF(@c=0, 'ALTER TABLE empresas ADD COLUMN fecha_fin_trial DATE NULL', 'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;
