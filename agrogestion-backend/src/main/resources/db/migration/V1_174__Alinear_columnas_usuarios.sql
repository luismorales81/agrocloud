-- Columnas de usuarios que espera la entidad User y no estaban en V1_0.

SET @esquema = DATABASE();

SET @c = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@esquema AND TABLE_NAME='usuarios' AND COLUMN_NAME='creado_por_id');
SET @sql = IF(@c=0, 'ALTER TABLE usuarios ADD COLUMN creado_por_id BIGINT NULL', 'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @c = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@esquema AND TABLE_NAME='usuarios' AND COLUMN_NAME='parent_user_id');
SET @sql = IF(@c=0, 'ALTER TABLE usuarios ADD COLUMN parent_user_id BIGINT NULL', 'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @c = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@esquema AND TABLE_NAME='usuarios' AND COLUMN_NAME='phone');
SET @sql = IF(@c=0, 'ALTER TABLE usuarios ADD COLUMN phone VARCHAR(20) NULL', 'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @c = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@esquema AND TABLE_NAME='usuarios' AND COLUMN_NAME='email_verified');
SET @sql = IF(@c=0, 'ALTER TABLE usuarios ADD COLUMN email_verified BOOLEAN NOT NULL DEFAULT FALSE', 'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @c = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@esquema AND TABLE_NAME='usuarios' AND COLUMN_NAME='estado');
SET @sql = IF(@c=0, 'ALTER TABLE usuarios ADD COLUMN estado VARCHAR(50) NULL DEFAULT ''ACTIVO''', 'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @c = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@esquema AND TABLE_NAME='usuarios' AND COLUMN_NAME='verification_token');
SET @sql = IF(@c=0, 'ALTER TABLE usuarios ADD COLUMN verification_token VARCHAR(255) NULL', 'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @c = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@esquema AND TABLE_NAME='usuarios' AND COLUMN_NAME='reset_password_token');
SET @sql = IF(@c=0, 'ALTER TABLE usuarios ADD COLUMN reset_password_token VARCHAR(255) NULL', 'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @c = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@esquema AND TABLE_NAME='usuarios' AND COLUMN_NAME='reset_password_token_expiry');
SET @sql = IF(@c=0, 'ALTER TABLE usuarios ADD COLUMN reset_password_token_expiry DATETIME NULL', 'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @c = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@esquema AND TABLE_NAME='empresas' AND COLUMN_NAME='creado_por');
SET @sql = IF(@c=0, 'ALTER TABLE empresas ADD COLUMN creado_por BIGINT NULL', 'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @c = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@esquema AND TABLE_NAME='usuario_empresas' AND COLUMN_NAME='creado_por');
SET @sql = IF(@c=0, 'ALTER TABLE usuario_empresas ADD COLUMN creado_por BIGINT NULL', 'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

