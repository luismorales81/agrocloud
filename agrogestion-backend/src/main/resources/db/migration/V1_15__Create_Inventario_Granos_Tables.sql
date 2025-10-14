-- Migration V1_15: Sistema de Inventario de Granos
-- Permite gestionar stock de granos cosechados y ventas

-- Tabla principal de inventario de granos
CREATE TABLE IF NOT EXISTS inventario_granos (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    
    -- Referencias
    cosecha_id BIGINT NOT NULL COMMENT 'Referencia a historial_cosechas',
    cultivo_id BIGINT NOT NULL,
    lote_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    
    -- Cantidades
    cantidad_inicial DECIMAL(10,2) NOT NULL COMMENT 'Cantidad inicial cosechada',
    cantidad_disponible DECIMAL(10,2) NOT NULL COMMENT 'Cantidad actual disponible',
    unidad_medida VARCHAR(10) NOT NULL COMMENT 'kg, ton, qq',
    
    -- Costos
    costo_produccion_total DECIMAL(15,2) NOT NULL DEFAULT 0 COMMENT 'Costo total de producción',
    costo_unitario DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT 'Costo por unidad (total/cantidad)',
    
    -- Fechas y estado
    fecha_ingreso DATE NOT NULL COMMENT 'Fecha de cosecha/ingreso',
    estado VARCHAR(20) NOT NULL DEFAULT 'DISPONIBLE' COMMENT 'DISPONIBLE, RESERVADO, AGOTADO',
    
    -- Información adicional
    variedad VARCHAR(100) COMMENT 'Variedad del cultivo',
    calidad VARCHAR(50) COMMENT 'Calidad del grano',
    ubicacion_almacenamiento VARCHAR(200) COMMENT 'Dónde está almacenado',
    observaciones TEXT,
    
    -- Auditoría
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Foreign Keys
    FOREIGN KEY (cosecha_id) REFERENCES historial_cosechas(id) ON DELETE RESTRICT,
    FOREIGN KEY (cultivo_id) REFERENCES cultivos(id) ON DELETE RESTRICT,
    FOREIGN KEY (lote_id) REFERENCES lotes(id) ON DELETE RESTRICT,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE RESTRICT,
    
    -- Índices para mejor performance
    INDEX idx_cultivo (cultivo_id),
    INDEX idx_lote (lote_id),
    INDEX idx_usuario (usuario_id),
    INDEX idx_estado (estado),
    INDEX idx_fecha_ingreso (fecha_ingreso)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Inventario de granos cosechados';

-- Tabla de movimientos de inventario de granos (separada de movimientos_inventario de insumos)
CREATE TABLE IF NOT EXISTS movimientos_inventario_granos (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    
    -- Referencias
    inventario_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    
    -- Tipo de movimiento
    tipo_movimiento VARCHAR(30) NOT NULL COMMENT 'ENTRADA_COSECHA, SALIDA_VENTA, AJUSTE_INVENTARIO, MERMA, USO_INTERNO',
    
    -- Cantidad
    cantidad DECIMAL(10,2) NOT NULL,
    
    -- Referencia externa (si aplica)
    referencia_tipo VARCHAR(30) COMMENT 'INGRESO, EGRESO, LABOR, null',
    referencia_id BIGINT COMMENT 'ID del registro relacionado',
    
    -- Información de la transacción
    precio_unitario DECIMAL(10,2) COMMENT 'Precio en caso de venta',
    monto_total DECIMAL(15,2) COMMENT 'Monto total de la transacción',
    
    -- Cliente/Destino
    cliente_destino VARCHAR(200) COMMENT 'Cliente o destino del movimiento',
    
    -- Fechas y estado
    fecha_movimiento DATE NOT NULL,
    
    -- Información adicional
    observaciones TEXT,
    
    -- Auditoría
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign Keys
    FOREIGN KEY (inventario_id) REFERENCES inventario_granos(id) ON DELETE RESTRICT,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE RESTRICT,
    
    -- Índices
    INDEX idx_inventario (inventario_id),
    INDEX idx_tipo (tipo_movimiento),
    INDEX idx_fecha (fecha_movimiento),
    INDEX idx_referencia (referencia_tipo, referencia_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Historial de movimientos de inventario';

-- Crear vista para stock actual por cultivo
CREATE OR REPLACE VIEW v_stock_actual_por_cultivo AS
SELECT 
    c.id as cultivo_id,
    c.nombre as cultivo_nombre,
    SUM(ig.cantidad_disponible) as cantidad_total,
    ig.unidad_medida,
    COUNT(DISTINCT ig.lote_id) as cantidad_lotes,
    AVG(ig.costo_unitario) as costo_promedio,
    SUM(ig.cantidad_disponible * ig.costo_unitario) as valor_inventario
FROM inventario_granos ig
INNER JOIN cultivos c ON ig.cultivo_id = c.id
WHERE ig.estado = 'DISPONIBLE' AND ig.cantidad_disponible > 0
GROUP BY c.id, c.nombre, ig.unidad_medida;

-- Crear vista para movimientos recientes de granos
CREATE OR REPLACE VIEW v_movimientos_granos_recientes AS
SELECT 
    m.id,
    m.fecha_movimiento,
    m.tipo_movimiento,
    c.nombre as cultivo,
    l.nombre as lote,
    m.cantidad,
    ig.unidad_medida,
    m.monto_total,
    m.cliente_destino,
    CONCAT(u.first_name, ' ', u.last_name) as usuario
FROM movimientos_inventario_granos m
INNER JOIN inventario_granos ig ON m.inventario_id = ig.id
INNER JOIN cultivos c ON ig.cultivo_id = c.id
INNER JOIN lotes l ON ig.lote_id = l.id
INNER JOIN usuarios u ON m.usuario_id = u.id
ORDER BY m.created_at DESC
LIMIT 50;

