-- Script simplificado para crear la tabla de ingresos
-- Este script no depende de otras tablas existentes

-- Crear tabla de ingresos sin claves foráneas
CREATE TABLE IF NOT EXISTS ingresos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    concepto VARCHAR(200) NOT NULL,
    descripcion VARCHAR(500),
    tipo_ingreso ENUM('VENTA_CULTIVO', 'VENTA_ANIMAL', 'SERVICIOS_AGRICOLAS', 'SUBSIDIOS', 'OTROS_INGRESOS') NOT NULL,
    fecha_ingreso DATE NOT NULL,
    monto DECIMAL(15,2) NOT NULL,
    unidad_medida VARCHAR(100),
    cantidad DECIMAL(10,2),
    cliente_comprador VARCHAR(200),
    estado ENUM('REGISTRADO', 'CONFIRMADO', 'CANCELADO') DEFAULT 'REGISTRADO',
    observaciones VARCHAR(1000),
    lote_id BIGINT,
    usuario_id BIGINT NOT NULL,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_ingresos_usuario (usuario_id),
    INDEX idx_ingresos_fecha (fecha_ingreso),
    INDEX idx_ingresos_tipo (tipo_ingreso),
    INDEX idx_ingresos_estado (estado),
    INDEX idx_ingresos_lote (lote_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insertar datos de ejemplo
INSERT INTO ingresos (concepto, descripcion, tipo_ingreso, fecha_ingreso, monto, unidad_medida, cantidad, cliente_comprador, estado, lote_id, usuario_id) VALUES
('Venta de Soja', 'Venta de soja de la cosecha 2024', 'VENTA_CULTIVO', '2024-03-15', 150000.00, 'toneladas', 50.00, 'Cooperativa San Martín', 'CONFIRMADO', 1, 1),
('Venta de Maíz', 'Venta de maíz de la cosecha 2024', 'VENTA_CULTIVO', '2024-04-20', 80000.00, 'toneladas', 40.00, 'Molino Central', 'CONFIRMADO', 1, 1),
('Servicio de Siembra', 'Servicio de siembra para terceros', 'SERVICIOS_AGRICOLAS', '2024-02-10', 25000.00, 'hectáreas', 100.00, 'Estancia La Esperanza', 'CONFIRMADO', NULL, 1),
('Subsidio PROAGRO', 'Subsidio del programa PROAGRO', 'SUBSIDIOS', '2024-01-15', 45000.00, NULL, NULL, 'Gobierno Nacional', 'CONFIRMADO', NULL, 1),
('Venta de Ganado', 'Venta de novillos', 'VENTA_ANIMAL', '2024-05-10', 120000.00, 'cabezas', 20.00, 'Frigorífico del Sur', 'REGISTRADO', NULL, 1);

-- Verificar que se creó correctamente
SELECT 'Tabla ingresos creada exitosamente' as mensaje;
SELECT COUNT(*) as total_ingresos FROM ingresos;
