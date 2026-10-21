-- Alinea un esquema creado solo con Flyway con las columnas y tablas que ya usan las entidades.

SET @esquema = DATABASE();

SET @sql = (
    SELECT IF(
        NOT EXISTS (
            SELECT 1 FROM information_schema.TABLES
            WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'cultivo_cultivos'
        ) OR EXISTS (
            SELECT 1 FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = @esquema
              AND TABLE_NAME = 'cultivo_cultivos'
              AND COLUMN_NAME = 'ciclo_dias'
        ),
        'SELECT 1',
        'ALTER TABLE cultivo_cultivos ADD COLUMN ciclo_dias INT NULL'
    )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(
        NOT EXISTS (
            SELECT 1 FROM information_schema.TABLES
            WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'cultivo_maquinaria'
        ) OR EXISTS (
            SELECT 1 FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = @esquema
              AND TABLE_NAME = 'cultivo_maquinaria'
              AND COLUMN_NAME = 'empresa_id'
        ),
        'SELECT 1',
        'ALTER TABLE cultivo_maquinaria ADD COLUMN empresa_id BIGINT NULL'
    )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS cultivo_ingresos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    concepto VARCHAR(200) NOT NULL,
    descripcion TEXT NULL,
    tipo_ingreso VARCHAR(50) NOT NULL,
    monto DECIMAL(15, 2) NOT NULL,
    cantidad DECIMAL(10, 2) NULL,
    unidad_medida VARCHAR(50) NULL,
    fecha DATE NOT NULL,
    cliente_comprador VARCHAR(200) NULL,
    observaciones TEXT NULL,
    estado VARCHAR(50) NOT NULL,
    lote_id BIGINT NULL,
    user_id BIGINT NOT NULL,
    fecha_creacion DATETIME NULL,
    fecha_actualizacion DATETIME NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    campana_id BIGINT NULL
);

CREATE TABLE IF NOT EXISTS cultivo_egresos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    concepto VARCHAR(200) NULL,
    descripcion TEXT NULL,
    tipo VARCHAR(50) NOT NULL,
    referencia_id BIGINT NULL,
    cantidad DECIMAL(10, 2) NULL,
    unidad_medida VARCHAR(50) NULL,
    costo_unitario DECIMAL(10, 2) NULL,
    costo_total DECIMAL(10, 2) NOT NULL,
    monto DECIMAL(15, 2) NOT NULL,
    fecha DATE NOT NULL,
    proveedor VARCHAR(200) NULL,
    observaciones TEXT NULL,
    estado VARCHAR(50) NOT NULL,
    lote_id BIGINT NULL,
    user_id BIGINT NOT NULL,
    fecha_creacion DATETIME NULL,
    fecha_actualizacion DATETIME NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    campana_id BIGINT NULL
);
