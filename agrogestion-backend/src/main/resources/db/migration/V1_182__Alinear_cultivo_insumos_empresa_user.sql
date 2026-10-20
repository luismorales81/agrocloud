-- Alinear cultivo_insumos con la entidad Insumo (baseline V1_0 no tenía empresa_id ni user_id).
SET @esquema = DATABASE();

SET @c = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@esquema AND TABLE_NAME='cultivo_insumos' AND COLUMN_NAME='activo');
SET @sql = IF(@c=0, 'ALTER TABLE cultivo_insumos ADD COLUMN activo BOOLEAN NOT NULL DEFAULT TRUE', 'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @c = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@esquema AND TABLE_NAME='cultivo_insumos' AND COLUMN_NAME='user_id');
SET @sql = IF(@c=0, 'ALTER TABLE cultivo_insumos ADD COLUMN user_id BIGINT NULL', 'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @tiene_usuario_id = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@esquema AND TABLE_NAME='cultivo_insumos' AND COLUMN_NAME='usuario_id');
SET @sql = IF(@tiene_usuario_id=0, 'SELECT 1',
    'UPDATE cultivo_insumos SET user_id = usuario_id WHERE user_id IS NULL AND usuario_id IS NOT NULL');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @c = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@esquema AND TABLE_NAME='cultivo_insumos' AND COLUMN_NAME='empresa_id');
SET @sql = IF(@c=0, 'ALTER TABLE cultivo_insumos ADD COLUMN empresa_id BIGINT NULL', 'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @tiene_ue = (SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA=@esquema AND TABLE_NAME='usuario_empresas');
SET @sql = IF(@tiene_ue=0, 'SELECT 1',
    'UPDATE cultivo_insumos i SET empresa_id = (SELECT ue.empresa_id FROM usuario_empresas ue WHERE ue.usuario_id = i.user_id ORDER BY ue.id LIMIT 1) WHERE i.empresa_id IS NULL AND i.user_id IS NOT NULL');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @tiene_emp_en_user = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@esquema AND TABLE_NAME='usuarios' AND COLUMN_NAME='empresa_id');
SET @sql = IF(@tiene_emp_en_user=0, 'SELECT 1',
    'UPDATE cultivo_insumos i SET empresa_id = (SELECT u.empresa_id FROM usuarios u WHERE u.id = i.user_id) WHERE i.empresa_id IS NULL AND i.user_id IS NOT NULL');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

UPDATE cultivo_insumos
SET empresa_id = (SELECT MIN(id) FROM empresas)
WHERE empresa_id IS NULL
  AND EXISTS (SELECT 1 FROM empresas);

SET @c = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@esquema AND TABLE_NAME='cultivo_insumos' AND COLUMN_NAME='fecha_vencimiento');
SET @sql = IF(@c=0, 'ALTER TABLE cultivo_insumos ADD COLUMN fecha_vencimiento DATE NULL', 'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @c = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@esquema AND TABLE_NAME='cultivo_insumos' AND COLUMN_NAME='permite_stock_negativo');
SET @sql = IF(@c=0, 'ALTER TABLE cultivo_insumos ADD COLUMN permite_stock_negativo BOOLEAN DEFAULT FALSE', 'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;
