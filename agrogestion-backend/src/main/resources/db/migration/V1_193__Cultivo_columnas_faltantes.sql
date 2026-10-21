-- Completa cultivo_cultivos con las columnas que la entidad ya persiste.

SET @esquema = DATABASE();

SET @sql = (
    SELECT IF(
        NOT EXISTS (SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'cultivo_cultivos')
        OR EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'cultivo_cultivos' AND COLUMN_NAME = 'estado'),
        'SELECT 1',
        'ALTER TABLE cultivo_cultivos ADD COLUMN estado VARCHAR(50) NOT NULL DEFAULT ''ACTIVO'''
    )
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(
        NOT EXISTS (SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'cultivo_cultivos')
        OR EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'cultivo_cultivos' AND COLUMN_NAME = 'rendimiento_esperado'),
        'SELECT 1',
        'ALTER TABLE cultivo_cultivos ADD COLUMN rendimiento_esperado DECIMAL(10,2) NULL'
    )
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(
        NOT EXISTS (SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'cultivo_cultivos')
        OR EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'cultivo_cultivos' AND COLUMN_NAME = 'unidad_rendimiento'),
        'SELECT 1',
        'ALTER TABLE cultivo_cultivos ADD COLUMN unidad_rendimiento VARCHAR(50) NULL'
    )
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(
        NOT EXISTS (SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'cultivo_cultivos')
        OR EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'cultivo_cultivos' AND COLUMN_NAME = 'precio_por_tonelada'),
        'SELECT 1',
        'ALTER TABLE cultivo_cultivos ADD COLUMN precio_por_tonelada DECIMAL(10,2) NULL'
    )
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(
        NOT EXISTS (SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'cultivo_cultivos')
        OR EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'cultivo_cultivos' AND COLUMN_NAME = 'usuario_id'),
        'SELECT 1',
        'ALTER TABLE cultivo_cultivos ADD COLUMN usuario_id BIGINT NULL'
    )
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
