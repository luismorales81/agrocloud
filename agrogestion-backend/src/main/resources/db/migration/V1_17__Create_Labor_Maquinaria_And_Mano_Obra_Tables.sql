-- Crear tabla labor_maquinaria para almacenar las maquinarias utilizadas en las labores
CREATE TABLE IF NOT EXISTS labor_maquinaria (
    id_labor_maquinaria BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_labor BIGINT NOT NULL,
    descripcion VARCHAR(255) NOT NULL COMMENT 'Descripción de la maquinaria (ej: Sembradora John Deere)',
    tipo_maquinaria ENUM('PROPIA', 'ALQUILADA') NOT NULL DEFAULT 'PROPIA',
    proveedor VARCHAR(255) DEFAULT NULL COMMENT 'Proveedor de alquiler (solo si tipo = ALQUILADA)',
    costo DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT 'Costo de uso/alquiler de la maquinaria',
    observaciones TEXT COMMENT 'Observaciones adicionales',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (id_labor) REFERENCES labores(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT chk_labor_maquinaria_costo CHECK (costo >= 0),
    
    -- Índices para mejorar el rendimiento
    INDEX idx_labor_maquinaria_labor (id_labor),
    INDEX idx_labor_maquinaria_proveedor (proveedor)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Registro de maquinarias utilizadas en cada labor';

-- Crear tabla labor_mano_obra para almacenar la mano de obra utilizada en las labores
CREATE TABLE IF NOT EXISTS labor_mano_obra (
    id_labor_mano_obra BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_labor BIGINT NOT NULL,
    descripcion VARCHAR(255) NOT NULL COMMENT 'Descripción de la mano de obra (ej: Cuadrilla de 5 jornaleros)',
    cantidad_personas INT NOT NULL DEFAULT 1 COMMENT 'Número de personas involucradas',
    proveedor VARCHAR(255) DEFAULT NULL COMMENT 'Proveedor/contratista (si es tercerizado)',
    costo_total DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT 'Costo total de la mano de obra',
    horas_trabajo DECIMAL(8,2) DEFAULT NULL COMMENT 'Horas totales de trabajo',
    observaciones TEXT COMMENT 'Observaciones adicionales',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (id_labor) REFERENCES labores(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT chk_labor_mano_obra_cantidad CHECK (cantidad_personas > 0),
    CONSTRAINT chk_labor_mano_obra_costo CHECK (costo_total >= 0),
    CONSTRAINT chk_labor_mano_obra_horas CHECK (horas_trabajo IS NULL OR horas_trabajo >= 0),
    
    -- Índices para mejorar el rendimiento
    INDEX idx_labor_mano_obra_labor (id_labor),
    INDEX idx_labor_mano_obra_proveedor (proveedor),
    INDEX idx_labor_mano_obra_cantidad (cantidad_personas)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Registro de mano de obra utilizada en cada labor';

