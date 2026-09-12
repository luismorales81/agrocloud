-- Agregar campos EULA a usuarios (defensiva)

SET @esquema = DATABASE();

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'usuarios' AND COLUMN_NAME = 'eula_aceptado') > 0,
    'SELECT 1',
    'ALTER TABLE usuarios ADD COLUMN eula_aceptado BOOLEAN DEFAULT FALSE NOT NULL'
);
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'usuarios' AND COLUMN_NAME = 'eula_fecha_aceptacion') > 0,
    'SELECT 1',
    'ALTER TABLE usuarios ADD COLUMN eula_fecha_aceptacion DATETIME NULL'
);
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'usuarios' AND COLUMN_NAME = 'eula_ip_address') > 0,
    'SELECT 1',
    'ALTER TABLE usuarios ADD COLUMN eula_ip_address VARCHAR(45) NULL'
);
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'usuarios' AND COLUMN_NAME = 'eula_user_agent') > 0,
    'SELECT 1',
    'ALTER TABLE usuarios ADD COLUMN eula_user_agent VARCHAR(500) NULL'
);
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'usuarios' AND COLUMN_NAME = 'eula_version') > 0,
    'SELECT 1',
    'ALTER TABLE usuarios ADD COLUMN eula_version VARCHAR(20) NULL'
);
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'usuarios' AND COLUMN_NAME = 'eula_pdf_path') > 0,
    'SELECT 1',
    'ALTER TABLE usuarios ADD COLUMN eula_pdf_path VARCHAR(500) NULL'
);
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = @esquema AND table_name = 'usuarios' AND index_name = 'idx_eula_aceptado') > 0,
    'SELECT 1',
    'CREATE INDEX idx_eula_aceptado ON usuarios(eula_aceptado)'
);
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SELECT 'Migración V1_23 campos EULA aplicada (modo defensivo)' AS mensaje;
