-- ============================================================================
-- MIGRACIÓN: Crear tablas del módulo de Porcinos
-- Versión: V1_25
-- Fecha: 2025-01-XX
-- ============================================================================

-- Tabla: madres (Unidades productivas)
CREATE TABLE IF NOT EXISTS madres (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    identificacion VARCHAR(100) NOT NULL UNIQUE,
    fecha_nacimiento DATE NOT NULL,
    cantidad_tetas INT NOT NULL DEFAULT 14,
    origen ENUM('EXTERNA', 'INTERNA') NOT NULL DEFAULT 'EXTERNA',
    estado_actual ENUM('CACHORRA', 'ADULTA', 'GESTACION', 'LACTANCIA', 'RECRIA', 'DESCARTE') NOT NULL DEFAULT 'CACHORRA',
    fecha_ingreso_granja DATE NOT NULL,
    observaciones TEXT,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    empresa_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    
    INDEX idx_madres_empresa (empresa_id),
    INDEX idx_madres_estado (estado_actual),
    INDEX idx_madres_identificacion (identificacion),
    INDEX idx_madres_fecha_ingreso (fecha_ingreso_granja),
    INDEX idx_madres_activo (activo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: historial_estados_madres
CREATE TABLE IF NOT EXISTS historial_estados_madres (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    madre_id BIGINT NOT NULL,
    estado ENUM('CACHORRA', 'ADULTA', 'GESTACION', 'LACTANCIA', 'RECRIA', 'DESCARTE') NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE,
    observaciones TEXT,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (madre_id) REFERENCES madres(id) ON DELETE CASCADE,
    INDEX idx_historial_madre (madre_id),
    INDEX idx_historial_fecha_inicio (fecha_inicio)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: servicios (Monta natural e IA)
CREATE TABLE IF NOT EXISTS servicios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    madre_id BIGINT NOT NULL,
    tipo ENUM('MONTA_NATURAL', 'IA') NOT NULL,
    fecha_servicio DATE NOT NULL,
    macho_id BIGINT,
    origen_semen ENUM('INTERNO', 'EXTERNO'),
    numero_intento INT NOT NULL DEFAULT 1,
    estado_servicio ENUM('PENDIENTE_CONTROL', 'FALLIDO', 'PREÑEZ_CONFIRMADA') NOT NULL DEFAULT 'PENDIENTE_CONTROL',
    fecha_control_celo DATE NOT NULL,
    observaciones TEXT,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    empresa_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (madre_id) REFERENCES madres(id) ON DELETE CASCADE,
    FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    
    INDEX idx_servicios_madre (madre_id),
    INDEX idx_servicios_estado (estado_servicio),
    INDEX idx_servicios_fecha_control (fecha_control_celo),
    INDEX idx_servicios_empresa (empresa_id),
    INDEX idx_servicios_activo (activo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: gestacion
CREATE TABLE IF NOT EXISTS gestacion (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    madre_id BIGINT NOT NULL,
    servicio_id BIGINT,
    fecha_inicio DATE NOT NULL,
    fecha_probable_parto DATE NOT NULL,
    estado ENUM('ACTIVA', 'FINALIZADA') NOT NULL DEFAULT 'ACTIVA',
    observaciones TEXT,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    empresa_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (madre_id) REFERENCES madres(id) ON DELETE CASCADE,
    FOREIGN KEY (servicio_id) REFERENCES servicios(id) ON DELETE SET NULL,
    FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    
    INDEX idx_gestacion_madre (madre_id),
    INDEX idx_gestacion_estado (estado),
    INDEX idx_gestacion_fecha_parto (fecha_probable_parto),
    INDEX idx_gestacion_empresa (empresa_id),
    INDEX idx_gestacion_activo (activo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: partos
CREATE TABLE IF NOT EXISTS partos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    madre_id BIGINT NOT NULL,
    fecha_inicio DATETIME NOT NULL,
    fecha_fin DATETIME,
    nacidos_vivos INT NOT NULL DEFAULT 0,
    nacidos_muertos INT NOT NULL DEFAULT 0,
    momias INT NOT NULL DEFAULT 0,
    total_nacidos INT NOT NULL,
    observaciones TEXT,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    empresa_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (madre_id) REFERENCES madres(id) ON DELETE CASCADE,
    FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    
    INDEX idx_partos_madre (madre_id),
    INDEX idx_partos_fecha (fecha_inicio),
    INDEX idx_partos_empresa (empresa_id),
    INDEX idx_partos_activo (activo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: muertes_lactancia
CREATE TABLE IF NOT EXISTS muertes_lactancia (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    parto_id BIGINT NOT NULL,
    fecha DATE NOT NULL,
    causa ENUM('APLASTAMIENTO', 'DIARREA', 'MALFORMACION', 'DEBILIDAD', 'OTRA') NOT NULL,
    cantidad INT NOT NULL DEFAULT 1,
    observaciones TEXT,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (parto_id) REFERENCES partos(id) ON DELETE CASCADE,
    INDEX idx_muertes_lactancia_parto (parto_id),
    INDEX idx_muertes_lactancia_fecha (fecha)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: recria
CREATE TABLE IF NOT EXISTS recria (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    lote_id BIGINT NOT NULL,
    fecha_ingreso DATE NOT NULL,
    peso_individual DECIMAL(10,2),
    peso_promedio DECIMAL(10,2) NOT NULL,
    cantidad_animales INT NOT NULL DEFAULT 1,
    sexo ENUM('MACHO', 'HEMBRA') NOT NULL,
    fecha_salida DATE,
    destino ENUM('VENTA', 'FUTURA_MADRE', 'ENGORDE'),
    observaciones TEXT,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    empresa_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (lote_id) REFERENCES lotes(id) ON DELETE CASCADE,
    FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    
    INDEX idx_recria_lote (lote_id),
    INDEX idx_recria_fecha_ingreso (fecha_ingreso),
    INDEX idx_recria_empresa (empresa_id),
    INDEX idx_recria_activo (activo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: muertes_recria
CREATE TABLE IF NOT EXISTS muertes_recria (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    recria_id BIGINT NOT NULL,
    fecha DATE NOT NULL,
    causa ENUM('APLASTAMIENTO', 'DIARREA', 'MALFORMACION', 'DEBILIDAD', 'OTRA') NOT NULL,
    cantidad INT NOT NULL DEFAULT 1,
    observaciones TEXT,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (recria_id) REFERENCES recria(id) ON DELETE CASCADE,
    INDEX idx_muertes_recria_recria (recria_id),
    INDEX idx_muertes_recria_fecha (fecha)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: formulas_alimentacion
CREATE TABLE IF NOT EXISTS formulas_alimentacion (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    etapa ENUM('GESTACION', 'LACTANCIA', 'F1', 'F2', 'F3', 'F4', 'DESARROLLO', 'TERMINACION') NOT NULL,
    nombre VARCHAR(200) NOT NULL,
    cantidad_recomendada DECIMAL(10,2) NOT NULL,
    unidad_medida VARCHAR(50) NOT NULL DEFAULT 'kg',
    observaciones TEXT,
    activa BOOLEAN NOT NULL DEFAULT TRUE,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    empresa_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    
    INDEX idx_formulas_etapa (etapa),
    INDEX idx_formulas_activa (activa),
    INDEX idx_formulas_empresa (empresa_id),
    INDEX idx_formulas_activo (activo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: insumos_formula
CREATE TABLE IF NOT EXISTS insumos_formula (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    formula_id BIGINT NOT NULL,
    insumo_id BIGINT NOT NULL,
    cantidad DECIMAL(10,2) NOT NULL,
    unidad_medida VARCHAR(50) NOT NULL DEFAULT 'kg',
    porcentaje DECIMAL(5,2),
    tipo ENUM('INSUMO', 'NUCLEO', 'VITAMINA', 'ANTIBIOTICO') NOT NULL DEFAULT 'INSUMO',
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (formula_id) REFERENCES formulas_alimentacion(id) ON DELETE CASCADE,
    FOREIGN KEY (insumo_id) REFERENCES insumos(id) ON DELETE CASCADE,
    
    INDEX idx_insumos_formula_formula (formula_id),
    INDEX idx_insumos_formula_insumo (insumo_id),
    INDEX idx_insumos_formula_tipo (tipo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: consumos_alimentacion
CREATE TABLE IF NOT EXISTS consumos_alimentacion (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fecha DATE NOT NULL,
    lote_id BIGINT,
    madre_id BIGINT,
    formula_id BIGINT NOT NULL,
    cantidad DECIMAL(10,2) NOT NULL,
    unidad_medida VARCHAR(50) NOT NULL DEFAULT 'kg',
    observaciones TEXT,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    empresa_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (lote_id) REFERENCES lotes(id) ON DELETE SET NULL,
    FOREIGN KEY (madre_id) REFERENCES madres(id) ON DELETE SET NULL,
    FOREIGN KEY (formula_id) REFERENCES formulas_alimentacion(id) ON DELETE CASCADE,
    FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    
    INDEX idx_consumos_fecha (fecha),
    INDEX idx_consumos_lote (lote_id),
    INDEX idx_consumos_madre (madre_id),
    INDEX idx_consumos_formula (formula_id),
    INDEX idx_consumos_empresa (empresa_id),
    INDEX idx_consumos_activo (activo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

