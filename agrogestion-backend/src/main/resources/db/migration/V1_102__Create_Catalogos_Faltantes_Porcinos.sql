-- ============================================================================
-- MIGRACIÓN: Crear catálogos faltantes del módulo Porcinos
-- Versión: V1_102
-- Fecha: 2025-12-05
-- Descripción: Catálogos adicionales requeridos: Tipos de Evento Sanitario,
--               Proveedores de Genética, Tipos de Parto, Causas de Nacidos Muertos,
--               Causas de Momificados
-- ============================================================================

-- ============================================================================
-- TABLA: porcinos_tipos_evento_sanitario
-- Descripción: Tipos de eventos sanitarios configurables (vacunación, desparasitación, etc.)
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
-- Descripción: Proveedores de genética y empresas de semen
-- ============================================================================
CREATE TABLE IF NOT EXISTS porcinos_proveedores_genetica (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(200) NOT NULL,
    razon_social VARCHAR(200),
    contacto VARCHAR(200),
    telefono VARCHAR(50),
    email VARCHAR(100),
    direccion VARCHAR(500),
    descripcion TEXT,
    tipo VARCHAR(50) NOT NULL COMMENT 'CENTRO_IA, EMPRESA_SEMEN, GRANJA_GENETICA, PROVEEDOR_VERRACOS, OTRO',
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    empresa_id BIGINT NOT NULL,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE,
    
    INDEX idx_proveedor_genetica_empresa (empresa_id),
    INDEX idx_proveedor_genetica_tipo (tipo),
    INDEX idx_proveedor_genetica_activo (activo),
    UNIQUE KEY uk_proveedor_genetica_empresa_nombre (empresa_id, nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- TABLA: porcinos_tipos_parto
-- Descripción: Tipos de parto configurables (normal, asistido, distocia, etc.)
-- ============================================================================
CREATE TABLE IF NOT EXISTS porcinos_tipos_parto (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    requiere_intervencion BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Si requiere intervención veterinaria',
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
-- Descripción: Causas configurables de nacidos muertos
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
-- Descripción: Causas configurables de momificados
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

-- ============================================================================
-- TABLA: porcinos_ubicaciones_internas
-- Descripción: Ubicaciones internas con estructura jerárquica (Galpón → Sala → Corral)
-- ============================================================================
CREATE TABLE IF NOT EXISTS porcinos_ubicaciones_internas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(200) NOT NULL,
    codigo VARCHAR(50) COMMENT 'Código único de la ubicación',
    nivel VARCHAR(50) NOT NULL COMMENT 'GALPON, SALA, CORRAL',
    ubicacion_padre_id BIGINT NULL COMMENT 'FK a otra ubicación (para jerarquía)',
    tipo_ubicacion VARCHAR(50) COMMENT 'MATERNIDAD, GESTACION, RECRIA, ENGORDE, AISLAMIENTO, GENERAL',
    capacidad_maxima INT COMMENT 'Capacidad máxima de animales',
    descripcion TEXT,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    empresa_id BIGINT NOT NULL,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE,
    FOREIGN KEY (ubicacion_padre_id) REFERENCES porcinos_ubicaciones_internas(id) ON DELETE SET NULL,
    
    INDEX idx_ubicacion_interna_empresa (empresa_id),
    INDEX idx_ubicacion_interna_nivel (nivel),
    INDEX idx_ubicacion_interna_tipo (tipo_ubicacion),
    INDEX idx_ubicacion_interna_padre (ubicacion_padre_id),
    INDEX idx_ubicacion_interna_activo (activo)
    -- Nota: La validación de unicidad de codigo (cuando no es NULL) se maneja a nivel de aplicación
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- TABLA: porcinos_eventos_sanitarios
-- Descripción: Registro de eventos sanitarios (tratamientos, vacunas, etc.)
--              Puede estar asociado a Madre, Padrillo o Lote (Recria)
-- ============================================================================
CREATE TABLE IF NOT EXISTS porcinos_eventos_sanitarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tipo_evento_sanitario_id BIGINT NOT NULL,
    fecha DATE NOT NULL,
    tipo_entidad VARCHAR(50) NOT NULL COMMENT 'MADRE, PADRILLO, LOTE',
    entidad_id BIGINT NOT NULL COMMENT 'ID de la entidad (Madre, Padrillo o Recria)',
    dosis DECIMAL(10,2),
    unidad_dosis VARCHAR(50) DEFAULT 'ml',
    lote_medicamento VARCHAR(100),
    profesional_responsable VARCHAR(200),
    fecha_retiro DATE COMMENT 'Fecha de retiro del medicamento',
    retiro_cumplido BOOLEAN NOT NULL DEFAULT FALSE,
    observaciones TEXT,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    empresa_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (tipo_evento_sanitario_id) REFERENCES porcinos_tipos_evento_sanitario(id),
    FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
    
    INDEX idx_evento_sanitario_empresa (empresa_id),
    INDEX idx_evento_sanitario_tipo_entidad (tipo_entidad, entidad_id),
    INDEX idx_evento_sanitario_fecha (fecha),
    INDEX idx_evento_sanitario_fecha_retiro (fecha_retiro),
    INDEX idx_evento_sanitario_activo (activo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- TABLA: porcinos_reabsorciones
-- Descripción: Registro de reabsorciones embrionarias
-- ============================================================================
CREATE TABLE IF NOT EXISTS porcinos_reabsorciones (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    gestacion_id BIGINT NOT NULL,
    fecha_deteccion DATE NOT NULL,
    dias_gestacion INT COMMENT 'Días de gestación al momento de la reabsorción',
    cantidad_embriones INT COMMENT 'Cantidad de embriones reabsorbidos',
    causa_probable VARCHAR(200),
    observaciones TEXT,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    empresa_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (gestacion_id) REFERENCES porcinos_gestacion(id) ON DELETE CASCADE,
    FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
    
    INDEX idx_reabsorcion_gestacion (gestacion_id),
    INDEX idx_reabsorcion_empresa (empresa_id),
    INDEX idx_reabsorcion_fecha (fecha_deteccion)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- TABLA: porcinos_transferencias_corral
-- Descripción: Registro de transferencias de animales entre corrales/ubicaciones
-- ============================================================================
CREATE TABLE IF NOT EXISTS porcinos_transferencias_corral (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    recria_id BIGINT NOT NULL,
    ubicacion_origen_id BIGINT NULL,
    ubicacion_destino_id BIGINT NOT NULL,
    fecha DATE NOT NULL,
    cantidad_animales INT NOT NULL,
    motivo VARCHAR(200),
    observaciones TEXT,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    empresa_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (recria_id) REFERENCES porcinos_recria(id) ON DELETE CASCADE,
    FOREIGN KEY (ubicacion_origen_id) REFERENCES porcinos_ubicaciones_internas(id) ON DELETE SET NULL,
    FOREIGN KEY (ubicacion_destino_id) REFERENCES porcinos_ubicaciones_internas(id),
    FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
    
    INDEX idx_transferencia_recria (recria_id),
    INDEX idx_transferencia_origen (ubicacion_origen_id),
    INDEX idx_transferencia_destino (ubicacion_destino_id),
    INDEX idx_transferencia_fecha (fecha),
    INDEX idx_transferencia_empresa (empresa_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


















