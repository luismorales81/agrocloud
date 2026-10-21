-- La entidad HistorialCosecha persiste costo_total_produccion y las dosis
-- consultan tablas que el esquema en ejecución no tenía.

SET @esquema = DATABASE();

SET @sql = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = @esquema
              AND TABLE_NAME = 'cultivo_historial_cosechas'
              AND COLUMN_NAME = 'costo_total_produccion'
        ),
        'SELECT 1',
        'ALTER TABLE cultivo_historial_cosechas ADD COLUMN costo_total_produccion DECIMAL(15,2) NULL DEFAULT 0'
    )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS dosis_aplicacion (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    insumo_id BIGINT NOT NULL,
    tipo_aplicacion VARCHAR(20) NOT NULL,
    dosis_por_ha DECIMAL(10, 2) NOT NULL,
    unidad_medida VARCHAR(50) NULL,
    descripcion VARCHAR(500) NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP NULL DEFAULT NULL,
    INDEX idx_dosis_aplicacion_insumo (insumo_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET @sql = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.TABLE_CONSTRAINTS
            WHERE TABLE_SCHEMA = @esquema
              AND TABLE_NAME = 'dosis_aplicacion'
              AND CONSTRAINT_NAME = 'fk_dosis_aplicacion_insumo'
        )
        OR NOT EXISTS (
            SELECT 1 FROM information_schema.TABLES
            WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'cultivo_insumos'
        ),
        'SELECT 1',
        'ALTER TABLE dosis_aplicacion ADD CONSTRAINT fk_dosis_aplicacion_insumo FOREIGN KEY (insumo_id) REFERENCES cultivo_insumos(id) ON DELETE CASCADE'
    )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS cultivo_dosis_insumos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tipo_aplicacion VARCHAR(50) NOT NULL,
    forma_aplicacion VARCHAR(50) NOT NULL,
    dosis_recomendada_por_ha DOUBLE NOT NULL,
    insumo_id BIGINT NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP NULL DEFAULT NULL,
    INDEX idx_cultivo_dosis_insumos_insumo (insumo_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET @sql = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.TABLE_CONSTRAINTS
            WHERE TABLE_SCHEMA = @esquema
              AND TABLE_NAME = 'cultivo_dosis_insumos'
              AND CONSTRAINT_NAME = 'fk_cultivo_dosis_insumos_insumo'
        )
        OR NOT EXISTS (
            SELECT 1 FROM information_schema.TABLES
            WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'cultivo_insumos'
        ),
        'SELECT 1',
        'ALTER TABLE cultivo_dosis_insumos ADD CONSTRAINT fk_cultivo_dosis_insumos_insumo FOREIGN KEY (insumo_id) REFERENCES cultivo_insumos(id) ON DELETE CASCADE'
    )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
