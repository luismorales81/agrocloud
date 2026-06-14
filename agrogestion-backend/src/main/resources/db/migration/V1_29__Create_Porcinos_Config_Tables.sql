-- ============================================================================
-- MIGRACIÓN: Crear tablas de Configuración y funcionalidades faltantes del módulo Porcinos
-- Versión: V1_29
-- Fecha: 2025-01-XX
-- ============================================================================

-- Tabla: configuraciones_porcinos (Parámetros configurables del módulo)
CREATE TABLE IF NOT EXISTS configuraciones_porcinos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    clave VARCHAR(100) NOT NULL UNIQUE,
    valor VARCHAR(500) NOT NULL,
    tipo ENUM('NUMERO', 'TEXTO', 'BOOLEAN', 'FECHA') NOT NULL DEFAULT 'TEXTO',
    descripcion TEXT,
    categoria VARCHAR(50) NOT NULL DEFAULT 'GENERAL',
    empresa_id BIGINT NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE,
    
    INDEX idx_config_empresa (empresa_id),
    INDEX idx_config_clave (clave),
    INDEX idx_config_categoria (categoria)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: causas_muerte_personalizadas (Causas de muerte configurables)
CREATE TABLE IF NOT EXISTS causas_muerte_personalizadas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    etapa ENUM('LACTANCIA', 'RECRIA', 'ENGORDE', 'GESTACION', 'GENERAL') NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    empresa_id BIGINT NOT NULL,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE,
    
    INDEX idx_causas_empresa (empresa_id),
    INDEX idx_causas_etapa (etapa)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: padrillos
CREATE TABLE IF NOT EXISTS padrillos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    identificacion VARCHAR(100) NOT NULL UNIQUE,
    fecha_nacimiento DATE NOT NULL,
    origen ENUM('EXTERNA', 'INTERNA') NOT NULL DEFAULT 'EXTERNA',
    fecha_ingreso_granja DATE NOT NULL,
    fecha_baja DATE,
    motivo_baja VARCHAR(200),
    observaciones TEXT,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    empresa_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    
    INDEX idx_padrillos_empresa (empresa_id),
    INDEX idx_padrillos_identificacion (identificacion),
    INDEX idx_padrillos_activo (activo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: lechones_nn (Trazabilidad de lechones sin origen claro)
CREATE TABLE IF NOT EXISTS lechones_nn (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    identificacion VARCHAR(100),
    fecha_registro DATE NOT NULL,
    lote_id BIGINT NOT NULL,
    cantidad INT NOT NULL DEFAULT 1,
    peso_promedio DECIMAL(10,2),
    etapa_ingreso ENUM('DESTETE', 'RECRIA', 'F1', 'F2', 'F3', 'F4') NOT NULL,
    observaciones TEXT,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    empresa_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (lote_id) REFERENCES lotes(id) ON DELETE CASCADE,
    FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    
    INDEX idx_lechones_nn_empresa (empresa_id),
    INDEX idx_lechones_nn_lote (lote_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: transferencias_lechones (Transferencias de lechones entre madres)
CREATE TABLE IF NOT EXISTS transferencias_lechones (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    parto_origen_id BIGINT NOT NULL,
    madre_origen_id BIGINT NOT NULL,
    parto_destino_id BIGINT NOT NULL,
    madre_destino_id BIGINT NOT NULL,
    fecha_transferencia DATE NOT NULL,
    cantidad INT NOT NULL DEFAULT 1,
    motivo VARCHAR(200),
    observaciones TEXT,
    empresa_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (parto_origen_id) REFERENCES partos(id) ON DELETE CASCADE,
    FOREIGN KEY (madre_origen_id) REFERENCES madres(id) ON DELETE CASCADE,
    FOREIGN KEY (parto_destino_id) REFERENCES partos(id) ON DELETE CASCADE,
    FOREIGN KEY (madre_destino_id) REFERENCES madres(id) ON DELETE CASCADE,
    FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    
    INDEX idx_transferencias_empresa (empresa_id),
    INDEX idx_transferencias_origen (parto_origen_id),
    INDEX idx_transferencias_destino (parto_destino_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: registros_peso (Registro de pesos en recría/engorde)
CREATE TABLE IF NOT EXISTS registros_peso (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    recria_id BIGINT NOT NULL,
    fecha_pesaje DATE NOT NULL,
    peso_promedio DECIMAL(10,2) NOT NULL,
    cantidad_animales INT NOT NULL,
    observaciones TEXT,
    empresa_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (recria_id) REFERENCES recria(id) ON DELETE CASCADE,
    FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    
    INDEX idx_pesos_recria (recria_id),
    INDEX idx_pesos_fecha (fecha_pesaje),
    INDEX idx_pesos_empresa (empresa_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: movimientos_etapas (Movimientos entre etapas de recría)
CREATE TABLE IF NOT EXISTS movimientos_etapas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    recria_origen_id BIGINT NOT NULL,
    recria_destino_id BIGINT,
    etapa_origen ENUM('F1', 'F2', 'F3', 'F4', 'DESARROLLO', 'TERMINACION') NOT NULL,
    etapa_destino ENUM('F1', 'F2', 'F3', 'F4', 'DESARROLLO', 'TERMINACION') NOT NULL,
    fecha_movimiento DATE NOT NULL,
    cantidad_animales INT NOT NULL,
    peso_promedio DECIMAL(10,2),
    lote_destino_id BIGINT,
    observaciones TEXT,
    empresa_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (recria_origen_id) REFERENCES recria(id) ON DELETE CASCADE,
    FOREIGN KEY (recria_destino_id) REFERENCES recria(id) ON DELETE SET NULL,
    FOREIGN KEY (lote_destino_id) REFERENCES lotes(id) ON DELETE SET NULL,
    FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    
    INDEX idx_movimientos_empresa (empresa_id),
    INDEX idx_movimientos_origen (recria_origen_id),
    INDEX idx_movimientos_destino (recria_destino_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: faena (Registro de faena)
CREATE TABLE IF NOT EXISTS faena (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    recria_id BIGINT NOT NULL,
    fecha_envio DATE NOT NULL,
    fecha_faena DATE,
    peso_envio DECIMAL(10,2) NOT NULL,
    peso_faena DECIMAL(10,2),
    rendimiento DECIMAL(5,2),
    precio_kg DECIMAL(10,2),
    ingreso_total DECIMAL(12,2),
    cantidad_animales INT NOT NULL,
    observaciones TEXT,
    empresa_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (recria_id) REFERENCES recria(id) ON DELETE CASCADE,
    FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    
    INDEX idx_faena_recria (recria_id),
    INDEX idx_faena_fecha (fecha_envio),
    INDEX idx_faena_empresa (empresa_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: stock_alimento (Stock de alimento con descuento automático)
CREATE TABLE IF NOT EXISTS stock_alimento (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    insumo_id BIGINT,
    cultivo_id BIGINT,
    tipo ENUM('INSUMO', 'GRANO_PROPIO', 'SUBPRODUCTO') NOT NULL,
    nombre VARCHAR(200) NOT NULL,
    cantidad_disponible DECIMAL(10,2) NOT NULL DEFAULT 0,
    unidad_medida VARCHAR(50) NOT NULL DEFAULT 'kg',
    fecha_ultima_actualizacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    empresa_id BIGINT NOT NULL,
    
    FOREIGN KEY (insumo_id) REFERENCES insumos(id) ON DELETE SET NULL,
    FOREIGN KEY (cultivo_id) REFERENCES cultivos(id) ON DELETE SET NULL,
    FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE,
    
    INDEX idx_stock_empresa (empresa_id),
    INDEX idx_stock_tipo (tipo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: movimientos_stock_alimento (Historial de movimientos de stock)
CREATE TABLE IF NOT EXISTS movimientos_stock_alimento (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    stock_id BIGINT NOT NULL,
    tipo_movimiento ENUM('ENTRADA', 'SALIDA', 'AJUSTE') NOT NULL,
    cantidad DECIMAL(10,2) NOT NULL,
    motivo VARCHAR(200),
    relacionado_con ENUM('CONSUMO', 'COMPRA', 'PRODUCCION', 'VENTA', 'AJUSTE') NOT NULL,
    relacionado_id BIGINT,
    fecha_movimiento DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    empresa_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    
    FOREIGN KEY (stock_id) REFERENCES stock_alimento(id) ON DELETE CASCADE,
    FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    
    INDEX idx_movimientos_stock (stock_id),
    INDEX idx_movimientos_fecha (fecha_movimiento)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Actualizar tabla recria para agregar campo de etapa
SET @columnExists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE table_schema = DATABASE() 
    AND table_name = 'recria' 
    AND column_name = 'etapa');

SET @sql = IF(@columnExists = 0,
    'ALTER TABLE recria ADD COLUMN etapa ENUM(\'F1\', \'F2\', \'F3\', \'F4\', \'DESARROLLO\', \'TERMINACION\') AFTER sexo',
    'SELECT 1');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Insertar configuraciones por defecto (se hará por empresa en el servicio)
-- Días cachorra: 160
-- Días gestación: 115
-- Días lactancia: 21-28 (configurable)
-- Días entre celos: 21

