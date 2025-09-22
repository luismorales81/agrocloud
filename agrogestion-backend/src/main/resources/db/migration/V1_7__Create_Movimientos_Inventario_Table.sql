-- Crear tabla movimientos_inventario para auditoría de movimientos de stock
CREATE TABLE IF NOT EXISTS movimientos_inventario (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    insumo_id BIGINT NOT NULL,
    labor_id BIGINT,
    tipo_movimiento ENUM('ENTRADA', 'SALIDA', 'AJUSTE') NOT NULL,
    cantidad DECIMAL(10,2) NOT NULL,
    motivo VARCHAR(255),
    fecha_movimiento TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    usuario_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (insumo_id) REFERENCES insumos(id) ON DELETE CASCADE,
    FOREIGN KEY (labor_id) REFERENCES labores(id) ON DELETE SET NULL,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE SET NULL,
    
    -- Índices para mejorar el rendimiento
    INDEX idx_movimientos_insumo (insumo_id),
    INDEX idx_movimientos_labor (labor_id),
    INDEX idx_movimientos_fecha (fecha_movimiento),
    INDEX idx_movimientos_tipo (tipo_movimiento)
);

-- Comentarios para documentar la tabla
ALTER TABLE movimientos_inventario COMMENT = 'Tabla que registra todos los movimientos de inventario de insumos para auditoría';
