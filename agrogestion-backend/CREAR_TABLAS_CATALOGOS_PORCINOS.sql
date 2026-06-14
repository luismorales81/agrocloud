-- ============================================================================
-- SCRIPT PARA CREAR TABLAS DE CATÁLOGOS FALTANTES DEL MÓDULO PORCINOS
-- ============================================================================

USE agrocloud;

-- ============================================================================
-- TABLA: porcinos_tipos_evento_sanitario
-- ============================================================================
CREATE TABLE IF NOT EXISTS porcinos_tipos_evento_sanitario (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    categoria VARCHAR(50) NOT NULL COMMENT 'VACUNACION, DESPARASITACION, ANTIBIOTICO, VITAMINA, TRATAMIENTO, CONTROL, OTRO',
    requiere_fecha_retiro BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Si requiere fecha de retiro (medicamentos)',
    dias_retiro_defecto INT NULL COMMENT 'Días de retiro por defecto',
    requiere_lote_medicamento BOOLEAN NOT NULL DEFAULT FALSE,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    empresa_id BIGINT NOT NULL,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE,
    
    INDEX idx_tipo_evento_sanitario_empresa (empresa_id),
    INDEX idx_tipo_evento_sanitario_categoria (categoria),
    INDEX idx_tipo_evento_sanitario_activo (activo),
    UNIQUE KEY uk_tipo_evento_sanitario_empresa_nombre (empresa_id, nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- TABLA: porcinos_proveedores_genetica
-- ============================================================================
CREATE TABLE IF NOT EXISTS porcinos_proveedores_genetica (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(200) NOT NULL,
    razon_social VARCHAR(200),
    contacto VARCHAR(200),
    telefono VARCHAR(50),
    email VARCHAR(100),
    direccion TEXT,
    descripcion TEXT,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    empresa_id BIGINT NOT NULL,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE,
    
    INDEX idx_proveedor_genetica_empresa (empresa_id),
    INDEX idx_proveedor_genetica_activo (activo),
    UNIQUE KEY uk_proveedor_genetica_empresa_nombre (empresa_id, nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- TABLA: porcinos_tipos_parto
-- ============================================================================
CREATE TABLE IF NOT EXISTS porcinos_tipos_parto (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    requiere_intervencion BOOLEAN NOT NULL DEFAULT FALSE,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    empresa_id BIGINT NOT NULL,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE,
    
    INDEX idx_tipo_parto_empresa (empresa_id),
    INDEX idx_tipo_parto_activo (activo),
    UNIQUE KEY uk_tipo_parto_empresa_nombre (empresa_id, nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- TABLA: porcinos_causas_nacidos_muertos
-- ============================================================================
CREATE TABLE IF NOT EXISTS porcinos_causas_nacidos_muertos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    empresa_id BIGINT NOT NULL,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE,
    
    INDEX idx_causa_nacido_muerto_empresa (empresa_id),
    INDEX idx_causa_nacido_muerto_activo (activo),
    UNIQUE KEY uk_causa_nacido_muerto_empresa_nombre (empresa_id, nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- TABLA: porcinos_causas_momificados
-- ============================================================================
CREATE TABLE IF NOT EXISTS porcinos_causas_momificados (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    empresa_id BIGINT NOT NULL,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE,
    
    INDEX idx_causa_momificado_empresa (empresa_id),
    INDEX idx_causa_momificado_activo (activo),
    UNIQUE KEY uk_causa_momificado_empresa_nombre (empresa_id, nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SELECT '✓ Tablas de catálogos creadas exitosamente' AS resultado;

