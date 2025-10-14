-- Script para aplicar el sistema de inventario de granos
-- Ejecutar en MySQL Workbench antes de reiniciar el backend

USE agrocloud;

-- Las columnas en historial_cosechas ya existen, omitir ese paso

-- Crear tabla de inventario de granos
CREATE TABLE IF NOT EXISTS inventario_granos (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    cosecha_id BIGINT NOT NULL,
    cultivo_id BIGINT NOT NULL,
    lote_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    cantidad_inicial DECIMAL(10,2) NOT NULL,
    cantidad_disponible DECIMAL(10,2) NOT NULL,
    unidad_medida VARCHAR(10) NOT NULL,
    costo_produccion_total DECIMAL(15,2) NOT NULL DEFAULT 0,
    costo_unitario DECIMAL(10,2) NOT NULL DEFAULT 0,
    fecha_ingreso DATE NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'DISPONIBLE',
    variedad VARCHAR(100),
    calidad VARCHAR(50),
    ubicacion_almacenamiento VARCHAR(200),
    observaciones TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (cosecha_id) REFERENCES historial_cosechas(id) ON DELETE RESTRICT,
    FOREIGN KEY (cultivo_id) REFERENCES cultivos(id) ON DELETE RESTRICT,
    FOREIGN KEY (lote_id) REFERENCES lotes(id) ON DELETE RESTRICT,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE RESTRICT,
    INDEX idx_cultivo (cultivo_id),
    INDEX idx_lote (lote_id),
    INDEX idx_usuario (usuario_id),
    INDEX idx_estado (estado),
    INDEX idx_fecha_ingreso (fecha_ingreso)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Crear tabla de movimientos de inventario de granos
CREATE TABLE IF NOT EXISTS movimientos_inventario_granos (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    inventario_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    tipo_movimiento VARCHAR(30) NOT NULL,
    cantidad DECIMAL(10,2) NOT NULL,
    referencia_tipo VARCHAR(30),
    referencia_id BIGINT,
    precio_unitario DECIMAL(10,2),
    monto_total DECIMAL(15,2),
    cliente_destino VARCHAR(200),
    fecha_movimiento DATE NOT NULL,
    observaciones TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (inventario_id) REFERENCES inventario_granos(id) ON DELETE RESTRICT,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE RESTRICT,
    INDEX idx_inventario (inventario_id),
    INDEX idx_tipo (tipo_movimiento),
    INDEX idx_fecha (fecha_movimiento),
    INDEX idx_referencia (referencia_tipo, referencia_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Verificar tablas creadas
SHOW TABLES LIKE '%inventario%';

-- Verificar estructura
DESCRIBE inventario_granos;
DESCRIBE movimientos_inventario_granos;

SELECT 'Tablas de inventario de granos creadas exitosamente' as mensaje;

