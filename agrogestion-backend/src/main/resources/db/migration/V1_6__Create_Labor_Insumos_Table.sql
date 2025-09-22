-- Crear tabla labor_insumos para almacenar los insumos utilizados en las labores
CREATE TABLE IF NOT EXISTS labor_insumos (
    id_labor_insumo BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_labor BIGINT NOT NULL,
    id_insumo BIGINT NOT NULL,
    cantidad_usada DECIMAL(10,2) NOT NULL,
    cantidad_planificada DECIMAL(10,2) NOT NULL,
    costo_unitario DECIMAL(12,2) NOT NULL,
    costo_total DECIMAL(12,2) NOT NULL,
    observaciones VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (id_labor) REFERENCES labores(id) ON DELETE CASCADE,
    FOREIGN KEY (id_insumo) REFERENCES insumos(id) ON DELETE CASCADE,
    
    -- Índices para mejorar el rendimiento
    INDEX idx_labor_insumos_labor (id_labor),
    INDEX idx_labor_insumos_insumo (id_insumo),
    INDEX idx_labor_insumos_created_at (created_at)
);

-- Comentarios para documentar la tabla
ALTER TABLE labor_insumos COMMENT = 'Tabla que almacena los insumos utilizados en cada labor agrícola';
