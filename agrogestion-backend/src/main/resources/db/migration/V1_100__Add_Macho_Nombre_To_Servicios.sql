-- Agregar campo macho_nombre a porcinos_servicios (idempotente si ya existe)

SET @esquema = DATABASE();

SET @consulta = IF(
    (SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_servicios') = 0,
    'SELECT 1',
    IF(
        (SELECT COUNT(*) FROM information_schema.COLUMNS
         WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'porcinos_servicios' AND COLUMN_NAME = 'macho_nombre') > 0,
        'SELECT 1',
        'ALTER TABLE porcinos_servicios ADD COLUMN macho_nombre VARCHAR(255) NULL AFTER macho_id'
    )
);
PREPARE sentencia FROM @consulta;
EXECUTE sentencia;
DEALLOCATE PREPARE sentencia;

SET @consulta = IF(
    (SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_servicios') = 0,
    'SELECT 1',
    IF(
        (SELECT COUNT(*) FROM information_schema.statistics
         WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'porcinos_servicios' AND INDEX_NAME = 'idx_servicios_macho_nombre') > 0,
        'SELECT 1',
        'CREATE INDEX idx_servicios_macho_nombre ON porcinos_servicios(macho_nombre)'
    )
);
PREPARE sentencia FROM @consulta;
EXECUTE sentencia;
DEALLOCATE PREPARE sentencia;
