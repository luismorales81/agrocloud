-- Crear tabla movimientos_inventario_granos para registrar movimientos de inventario de granos
CREATE TABLE IF NOT EXISTS movimientos_inventario_granos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    inventario_id BIGINT NOT NULL COMMENT 'ID del inventario de granos relacionado',
    usuario_id BIGINT DEFAULT NULL COMMENT 'Usuario que realizó el movimiento',
    tipo_movimiento VARCHAR(30) NOT NULL COMMENT 'Tipo de movimiento: ENTRADA, SALIDA, AJUSTE, etc.',
    cantidad DECIMAL(10,2) NOT NULL COMMENT 'Cantidad del movimiento',
    referencia_tipo VARCHAR(30) DEFAULT NULL COMMENT 'Tipo de referencia: COSECHA, VENTA, COMPRA, etc.',
    referencia_id BIGINT DEFAULT NULL COMMENT 'ID de la referencia relacionada',
    precio_unitario DECIMAL(10,2) DEFAULT NULL COMMENT 'Precio unitario del grano',
    monto_total DECIMAL(15,2) DEFAULT NULL COMMENT 'Monto total del movimiento',
    cliente_destino VARCHAR(200) DEFAULT NULL COMMENT 'Cliente destino (si aplica para ventas)',
    fecha_movimiento DATE NOT NULL COMMENT 'Fecha del movimiento',
    observaciones TEXT DEFAULT NULL COMMENT 'Observaciones adicionales',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (inventario_id) REFERENCES inventario_granos(id) ON DELETE CASCADE,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE SET NULL,
    
    -- Índices para mejorar el rendimiento
    INDEX idx_movimientos_granos_inventario (inventario_id),
    INDEX idx_movimientos_granos_usuario (usuario_id),
    INDEX idx_movimientos_granos_tipo (tipo_movimiento),
    INDEX idx_movimientos_granos_referencia (referencia_tipo, referencia_id),
    INDEX idx_movimientos_granos_fecha (fecha_movimiento)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Registro de movimientos de inventario de granos';

