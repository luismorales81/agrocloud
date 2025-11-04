-- Crear tabla dosis_insumos que relaciona insumos con sus dosis de aplicación
-- Esta tabla se usa para insumos que son agroquímicos (tipo: HERBICIDA, FUNGICIDA, INSECTICIDA)

CREATE TABLE IF NOT EXISTS dosis_insumos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    insumo_id BIGINT NOT NULL,
    tipo_aplicacion VARCHAR(20) NOT NULL,
    forma_aplicacion VARCHAR(20) NOT NULL,
    unidad VARCHAR(50) NOT NULL,
    dosis_recomendada_por_ha DOUBLE NOT NULL,
    activo BOOLEAN DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (insumo_id) REFERENCES insumos(id) ON DELETE CASCADE,
    INDEX idx_dosis_insumo (insumo_id),
    INDEX idx_dosis_tipo (tipo_aplicacion),
    INDEX idx_dosis_activo (activo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

