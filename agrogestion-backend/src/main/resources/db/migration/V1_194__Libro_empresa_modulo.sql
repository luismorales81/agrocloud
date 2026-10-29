-- Libro único de la empresa: origen del módulo e imputación, sin tablas nuevas.

SET @esquema = DATABASE();

SET @sql = (
    SELECT IF(
        NOT EXISTS (SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'cultivo_egresos')
        OR EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'cultivo_egresos' AND COLUMN_NAME = 'empresa_id'),
        'SELECT 1',
        'ALTER TABLE cultivo_egresos ADD COLUMN empresa_id BIGINT NULL, ADD COLUMN modulo_origen VARCHAR(40) NULL, ADD COLUMN imputacion VARCHAR(20) NULL, ADD COLUMN tipo_referencia VARCHAR(40) NULL, ADD COLUMN referencia_hecho_id BIGINT NULL'
    )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(
        NOT EXISTS (SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'cultivo_ingresos')
        OR EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'cultivo_ingresos' AND COLUMN_NAME = 'empresa_id'),
        'SELECT 1',
        'ALTER TABLE cultivo_ingresos ADD COLUMN empresa_id BIGINT NULL, ADD COLUMN modulo_origen VARCHAR(40) NULL, ADD COLUMN imputacion VARCHAR(20) NULL, ADD COLUMN tipo_referencia VARCHAR(40) NULL, ADD COLUMN referencia_hecho_id BIGINT NULL'
    )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(
        NOT EXISTS (SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'lecheria_venta_leche')
        OR EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'lecheria_venta_leche' AND COLUMN_NAME = 'ingreso_id'),
        'SELECT 1',
        'ALTER TABLE lecheria_venta_leche ADD COLUMN ingreso_id BIGINT NULL'
    )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE cultivo_egresos e
INNER JOIN (
    SELECT usuario_id, MIN(empresa_id) AS empresa_id
    FROM usuario_empresas
    GROUP BY usuario_id
) ue ON ue.usuario_id = e.user_id
SET e.empresa_id = ue.empresa_id,
    e.modulo_origen = 'CULTIVOS',
    e.imputacion = IF(e.tipo IN ('INSUMO', 'INSUMOS'), 'COMPARTIDA', 'MODULO')
WHERE e.empresa_id IS NULL;

UPDATE cultivo_ingresos i
INNER JOIN (
    SELECT usuario_id, MIN(empresa_id) AS empresa_id
    FROM usuario_empresas
    GROUP BY usuario_id
) ue ON ue.usuario_id = i.user_id
SET i.empresa_id = ue.empresa_id,
    i.modulo_origen = 'CULTIVOS',
    i.imputacion = 'MODULO'
WHERE i.empresa_id IS NULL;
