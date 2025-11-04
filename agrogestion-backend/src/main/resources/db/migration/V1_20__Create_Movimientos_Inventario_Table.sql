-- Crear tabla movimientos_inventario para registrar movimientos de inventario de insumos
CREATE TABLE IF NOT EXISTS movimientos_inventario (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    insumo_id BIGINT NOT NULL,
    labor_id BIGINT DEFAULT NULL COMMENT 'ID de la labor relacionada (si aplica)',
    tipo_movimiento ENUM('ENTRADA', 'SALIDA', 'AJUSTE') NOT NULL,
    cantidad DECIMAL(10,2) NOT NULL,
    motivo VARCHAR(255) DEFAULT NULL COMMENT 'Motivo del movimiento',
    fecha_movimiento TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    usuario_id BIGINT DEFAULT NULL COMMENT 'Usuario que realizó el movimiento',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (insumo_id) REFERENCES insumos(id) ON DELETE CASCADE,
    FOREIGN KEY (labor_id) REFERENCES labores(id) ON DELETE SET NULL,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE SET NULL,
    
    -- Índices para mejorar el rendimiento
    INDEX idx_movimientos_insumo (insumo_id),
    INDEX idx_movimientos_labor (labor_id),
    INDEX idx_movimientos_tipo (tipo_movimiento),
    INDEX idx_movimientos_fecha (fecha_movimiento),
    INDEX idx_movimientos_usuario (usuario_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Registro de movimientos de inventario de insumos';

