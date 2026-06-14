-- ============================================================================
-- MIGRACIÓN: Sistema de Consumo Diario Automático de Alimento
-- Versión: V1_112
-- Fecha: 2025-01-XX
-- Descripción: Crea las tablas para consumo diario automático, calendario,
--              movimientos de stock y derrames/pérdidas.
-- ============================================================================

-- ============================================================================
-- TABLA: porcinos_dias_alimentacion
-- Descripción: Registro diario del calendario de alimentación con estado
-- ============================================================================
CREATE TABLE IF NOT EXISTS porcinos_dias_alimentacion (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fecha DATE NOT NULL,
    empresa_id BIGINT NOT NULL,
    
    -- Estado del día
    estado ENUM('PENDIENTE', 'CONFIRMADO', 'CON_CORRECCIONES') NOT NULL DEFAULT 'PENDIENTE',
    
    -- Confirmación
    confirmado_por_id BIGINT NULL,
    fecha_confirmacion DATETIME NULL,
    observaciones_confirmacion TEXT,
    
    -- Resumen del día (calculado)
    total_lotes_atendidos INT DEFAULT 0,
    total_animales_atendidos INT DEFAULT 0,
    total_recetas_usadas INT DEFAULT 0,
    total_insumos_consumidos INT DEFAULT 0,
    
    -- Alertas del día
    tiene_alertas_stock_insuficiente BOOLEAN DEFAULT FALSE,
    cantidad_alertas INT DEFAULT 0,
    
    -- Auditoría
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    UNIQUE KEY uk_dia_empresa (empresa_id, fecha),
    FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE,
    FOREIGN KEY (confirmado_por_id) REFERENCES usuarios(id) ON DELETE SET NULL,
    
    INDEX idx_fecha (fecha),
    INDEX idx_estado (estado),
    INDEX idx_empresa_fecha (empresa_id, fecha)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- TABLA: porcinos_consumos_diarios_automaticos
-- Descripción: Consumo diario generado automáticamente para un lote/recría
-- ============================================================================
CREATE TABLE IF NOT EXISTS porcinos_consumos_diarios_automaticos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dia_alimentacion_id BIGINT NOT NULL,
    recria_id BIGINT NULL,
    lote_id BIGINT NULL,
    madre_id BIGINT NULL COMMENT 'Para madres gestantes/lactantes',
    
    -- Contexto del consumo
    etapa_alimentacion VARCHAR(50) NOT NULL COMMENT 'GESTACION, LACTANCIA, F1, F2, F3, F4, DESARROLLO, TERMINACION',
    cantidad_animales INT NOT NULL,
    receta_id BIGINT NOT NULL COMMENT 'FK a insumos_compuestos',
    
    -- Consumo calculado
    cantidad_receta_total DECIMAL(10,2) NOT NULL COMMENT 'Total de receta consumida (kg)',
    cantidad_diaria_por_animal DECIMAL(10,2) NOT NULL COMMENT 'Cantidad por animal según receta',
    
    -- Estado
    procesado BOOLEAN DEFAULT FALSE COMMENT 'Si ya se descontó stock',
    fecha_procesamiento DATETIME NULL,
    
    -- Auditoría
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (dia_alimentacion_id) REFERENCES porcinos_dias_alimentacion(id) ON DELETE CASCADE,
    FOREIGN KEY (recria_id) REFERENCES porcinos_recria(id) ON DELETE SET NULL,
    FOREIGN KEY (lote_id) REFERENCES cultivo_lotes(id) ON DELETE SET NULL,
    FOREIGN KEY (madre_id) REFERENCES porcinos_madres(id) ON DELETE SET NULL,
    FOREIGN KEY (receta_id) REFERENCES insumos_compuestos(id) ON DELETE RESTRICT,
    
    INDEX idx_dia (dia_alimentacion_id),
    INDEX idx_recria (recria_id),
    INDEX idx_lote (lote_id),
    INDEX idx_fecha_procesamiento (fecha_procesamiento)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- TABLA: porcinos_consumos_diarios_detalle
-- Descripción: Desglose detallado de insumos consumidos (componentes de receta)
-- ============================================================================
CREATE TABLE IF NOT EXISTS porcinos_consumos_diarios_detalle (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    consumo_diario_id BIGINT NOT NULL,
    
    -- Insumo consumido (puede ser Insumo, Cultivo, o InsumoCompuesto)
    insumo_id BIGINT NULL COMMENT 'FK a cultivo_insumos',
    cultivo_id BIGINT NULL COMMENT 'FK a cultivos (grano propio)',
    insumo_compuesto_id BIGINT NULL COMMENT 'FK a insumos_compuestos (sub-receta)',
    
    tipo_componente ENUM('INSUMO', 'GRANO_PROPIO', 'INSUMO_COMPUESTO') NOT NULL COMMENT 'Mantiene consistencia con ComponenteInsumoCompuesto',
    
    -- Cantidad consumida
    cantidad_requerida DECIMAL(10,2) NOT NULL COMMENT 'Cantidad total requerida (kg)',
    cantidad_disponible DECIMAL(10,2) NOT NULL COMMENT 'Stock disponible al momento del consumo',
    cantidad_descontada DECIMAL(10,2) NOT NULL COMMENT 'Cantidad realmente descontada',
    stock_resultante DECIMAL(10,2) NOT NULL COMMENT 'Stock después del descuento (puede ser negativo)',
    deficit DECIMAL(10,2) DEFAULT 0.00 COMMENT 'Déficit si stock insuficiente',
    
    -- Alertas
    tiene_deficit BOOLEAN DEFAULT FALSE,
    porcentaje_cobertura DECIMAL(5,2) DEFAULT 100.00 COMMENT '% del requerimiento cubierto',
    
    -- Auditoría
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (consumo_diario_id) REFERENCES porcinos_consumos_diarios_automaticos(id) ON DELETE CASCADE,
    FOREIGN KEY (insumo_id) REFERENCES cultivo_insumos(id) ON DELETE SET NULL,
    FOREIGN KEY (cultivo_id) REFERENCES cultivo_cultivos(id) ON DELETE SET NULL,
    FOREIGN KEY (insumo_compuesto_id) REFERENCES insumos_compuestos(id) ON DELETE SET NULL,
    
    INDEX idx_consumo (consumo_diario_id),
    INDEX idx_deficit (tiene_deficit)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- TABLA: porcinos_derrames_perdidas
-- Descripción: Registro de derrames/pérdidas/accidentes independientes
-- NOTA: Se crea ANTES de porcinos_movimientos_stock porque movimientos referencia derrames
-- ============================================================================
CREATE TABLE IF NOT EXISTS porcinos_derrames_perdidas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id BIGINT NOT NULL,
    
    -- Tipo de pérdida
    tipo ENUM('DERRAME', 'PERDIDA', 'ACCIDENTE', 'OTRO') NOT NULL,
    fecha DATE NOT NULL,
    dia_alimentacion_id BIGINT NULL COMMENT 'Día asociado (solo referencia, no modifica consumo)',
    
    -- Insumo afectado
    insumo_id BIGINT NULL,
    cultivo_id BIGINT NULL,
    insumo_compuesto_id BIGINT NULL,
    tipo_insumo ENUM('INSUMO', 'GRANO_PROPIO', 'INSUMO_COMPUESTO') NOT NULL COMMENT 'CULTIVO se llama GRANO_PROPIO para consistencia',
    
    -- Detalles (obligatorios)
    cantidad DECIMAL(10,2) NOT NULL,
    motivo VARCHAR(200) NOT NULL COMMENT 'Obligatorio',
    observaciones TEXT NOT NULL COMMENT 'Obligatorio',
    ubicacion VARCHAR(200),
    lote_id BIGINT NULL,
    
    -- Usuario responsable (obligatorio)
    usuario_id BIGINT NOT NULL,
    
    -- Auditoría
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE,
    FOREIGN KEY (dia_alimentacion_id) REFERENCES porcinos_dias_alimentacion(id) ON DELETE SET NULL,
    FOREIGN KEY (insumo_id) REFERENCES cultivo_insumos(id) ON DELETE SET NULL,
    FOREIGN KEY (cultivo_id) REFERENCES cultivo_cultivos(id) ON DELETE SET NULL,
    FOREIGN KEY (insumo_compuesto_id) REFERENCES insumos_compuestos(id) ON DELETE SET NULL,
    FOREIGN KEY (lote_id) REFERENCES cultivo_lotes(id) ON DELETE SET NULL,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE RESTRICT,
    
    INDEX idx_fecha (fecha),
    INDEX idx_tipo (tipo),
    INDEX idx_empresa_fecha (empresa_id, fecha)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- TABLA: porcinos_movimientos_stock
-- Descripción: Trazabilidad completa de todos los movimientos de stock
-- NOTA: Se crea DESPUÉS de porcinos_derrames_perdidas porque referencia derrames
-- ============================================================================
CREATE TABLE IF NOT EXISTS porcinos_movimientos_stock (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id BIGINT NOT NULL,
    
    -- Origen del movimiento
    tipo_movimiento ENUM(
        'CONSUMO_AUTOMATICO', 
        'DERRAME', 
        'PERDIDA', 
        'AJUSTE_INVENTARIO',
        'INGRESO',
        'TRANSFERENCIA'
    ) NOT NULL,
    
    -- Referencia al origen
    consumo_diario_id BIGINT NULL COMMENT 'Si es consumo automático',
    derrame_id BIGINT NULL COMMENT 'Si es derrame',
    dia_alimentacion_id BIGINT NULL COMMENT 'Para consumo automático',
    fecha_movimiento DATE NOT NULL,
    
    -- Insumo afectado
    insumo_id BIGINT NULL,
    cultivo_id BIGINT NULL,
    insumo_compuesto_id BIGINT NULL,
    tipo_insumo ENUM('INSUMO', 'GRANO_PROPIO', 'INSUMO_COMPUESTO') NOT NULL COMMENT 'CULTIVO se llama GRANO_PROPIO para consistencia',
    
    -- Detalles del movimiento
    cantidad DECIMAL(10,2) NOT NULL,
    stock_anterior DECIMAL(10,2) NOT NULL,
    stock_posterior DECIMAL(10,2) NOT NULL COMMENT 'Puede ser negativo solo si es CONSUMO_AUTOMATICO',
    permite_negativo BOOLEAN DEFAULT FALSE COMMENT 'Solo true para CONSUMO_AUTOMATICO',
    
    -- Información adicional
    motivo VARCHAR(200),
    observaciones TEXT,
    lote_id BIGINT NULL,
    recria_id BIGINT NULL,
    
    -- Usuario responsable
    usuario_id BIGINT NULL,
    
    -- Auditoría
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE,
    FOREIGN KEY (consumo_diario_id) REFERENCES porcinos_consumos_diarios_automaticos(id) ON DELETE SET NULL,
    FOREIGN KEY (derrame_id) REFERENCES porcinos_derrames_perdidas(id) ON DELETE SET NULL,
    FOREIGN KEY (dia_alimentacion_id) REFERENCES porcinos_dias_alimentacion(id) ON DELETE SET NULL,
    FOREIGN KEY (insumo_id) REFERENCES cultivo_insumos(id) ON DELETE SET NULL,
    FOREIGN KEY (cultivo_id) REFERENCES cultivo_cultivos(id) ON DELETE SET NULL,
    FOREIGN KEY (insumo_compuesto_id) REFERENCES insumos_compuestos(id) ON DELETE SET NULL,
    FOREIGN KEY (lote_id) REFERENCES cultivo_lotes(id) ON DELETE SET NULL,
    FOREIGN KEY (recria_id) REFERENCES porcinos_recria(id) ON DELETE SET NULL,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE SET NULL,
    
    INDEX idx_fecha (fecha_movimiento),
    INDEX idx_tipo (tipo_movimiento),
    INDEX idx_insumo (tipo_insumo, insumo_id, cultivo_id, insumo_compuesto_id),
    INDEX idx_empresa_fecha (empresa_id, fecha_movimiento)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- MODIFICACIÓN: Agregar campo permite_stock_negativo a cultivo_insumos (idempotente)
-- ============================================================================
SET @esquema = DATABASE();
SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'cultivo_insumos' AND COLUMN_NAME = 'permite_stock_negativo') > 0,
  'SELECT 1',
  'ALTER TABLE cultivo_insumos ADD COLUMN permite_stock_negativo BOOLEAN DEFAULT FALSE COMMENT ''Permite stock negativo solo para consumo automático'' AFTER stock_actual'
));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;
