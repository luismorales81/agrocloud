-- ============================================================================
-- SCRIPT PARA CREAR TABLAS DE INSUMOS COMPUESTOS
-- Ejecutar manualmente si la migración Flyway falló
-- ============================================================================

USE agrocloud;

-- ============================================================================
-- TABLA: insumos_compuestos
-- ============================================================================
CREATE TABLE IF NOT EXISTS insumos_compuestos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(200) NOT NULL,
    descripcion VARCHAR(500),
    tipo VARCHAR(50) NOT NULL COMMENT 'RACION, NUCLEO, MEZCLA, PREMEZCLA, OTRO',
    unidad_medida VARCHAR(50) NOT NULL DEFAULT 'kg',
    rendimiento DECIMAL(5,4) NOT NULL DEFAULT 1.0000 COMMENT 'Rendimiento de producción (ej: 0.95 = 95% por mermas)',
    costo_unitario_calculado DECIMAL(15,2) COMMENT 'Costo calculado automáticamente desde componentes',
    costo_unitario_manual DECIMAL(15,2) COMMENT 'Costo manual (opcional, sobreescribe el calculado)',
    stock_actual DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    stock_minimo DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    empresa_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_insumo_compuesto_empresa FOREIGN KEY (empresa_id) REFERENCES empresas(id),
    CONSTRAINT fk_insumo_compuesto_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
    
    INDEX idx_insumo_compuesto_empresa (empresa_id),
    INDEX idx_insumo_compuesto_activo (activo),
    INDEX idx_insumo_compuesto_tipo (tipo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- TABLA: componentes_insumo_compuesto
-- ============================================================================
CREATE TABLE IF NOT EXISTS componentes_insumo_compuesto (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    insumo_compuesto_id BIGINT NOT NULL,
    
    -- Un componente puede ser: Insumo, Cultivo (grano propio), u otro InsumoCompuesto
    insumo_id BIGINT NULL COMMENT 'Referencia a cultivo_insumos (núcleo, antibiótico, etc.)',
    cultivo_id BIGINT NULL COMMENT 'Referencia a cultivo_cultivos (maíz propio, soja propia)',
    insumo_compuesto_padre_id BIGINT NULL COMMENT 'Referencia a otro insumo compuesto (receta dentro de receta)',
    
    tipo_componente VARCHAR(50) NOT NULL COMMENT 'INSUMO, GRANO_PROPIO, INSUMO_COMPUESTO',
    porcentaje DECIMAL(5,2) NULL COMMENT 'Porcentaje en la mezcla (0-100)',
    cantidad_fija DECIMAL(10,2) NULL COMMENT 'Cantidad fija en unidad de medida',
    unidad_medida VARCHAR(50) DEFAULT 'kg',
    orden_mezcla INT DEFAULT 0 COMMENT 'Orden en que se agregan los componentes',
    observaciones VARCHAR(500),
    
    CONSTRAINT fk_componente_insumo_compuesto FOREIGN KEY (insumo_compuesto_id) 
        REFERENCES insumos_compuestos(id) ON DELETE CASCADE,
    CONSTRAINT fk_componente_insumo FOREIGN KEY (insumo_id) 
        REFERENCES cultivo_insumos(id) ON DELETE SET NULL,
    CONSTRAINT fk_componente_cultivo FOREIGN KEY (cultivo_id) 
        REFERENCES cultivo_cultivos(id) ON DELETE SET NULL,
    CONSTRAINT fk_componente_insumo_compuesto_padre FOREIGN KEY (insumo_compuesto_padre_id) 
        REFERENCES insumos_compuestos(id) ON DELETE SET NULL,
    
    -- Nota: Las validaciones de origen y cantidad se hacen a nivel de aplicación
    -- MySQL 8.0 no permite CHECK constraints con columnas usadas en foreign keys
    
    INDEX idx_componente_insumo_compuesto (insumo_compuesto_id),
    INDEX idx_componente_insumo (insumo_id),
    INDEX idx_componente_cultivo (cultivo_id),
    INDEX idx_componente_tipo (tipo_componente)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- TABLA: porcinos_recetas_alimentacion_etapa
-- ============================================================================
CREATE TABLE IF NOT EXISTS porcinos_recetas_alimentacion_etapa (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    insumo_compuesto_id BIGINT NOT NULL,
    etapa VARCHAR(50) NOT NULL COMMENT 'GESTACION, LACTANCIA, F1, F2, F3, F4, DESARROLLO, TERMINACION',
    cantidad_diaria_por_animal DECIMAL(10,2) NOT NULL COMMENT 'Cantidad diaria recomendada por animal (kg)',
    cantidad_diaria_minima DECIMAL(10,2) NULL COMMENT 'Cantidad mínima diaria (para rangos)',
    cantidad_diaria_maxima DECIMAL(10,2) NULL COMMENT 'Cantidad máxima diaria (para rangos)',
    peso_minimo_animal DECIMAL(10,2) NULL COMMENT 'Peso mínimo del animal para usar esta receta (kg)',
    peso_maximo_animal DECIMAL(10,2) NULL COMMENT 'Peso máximo del animal para usar esta receta (kg)',
    edad_minima_dias INT NULL COMMENT 'Edad mínima en días',
    edad_maxima_dias INT NULL COMMENT 'Edad máxima en días',
    es_por_defecto BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Indica si es la receta por defecto para la etapa',
    observaciones VARCHAR(500),
    empresa_id BIGINT NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    
    CONSTRAINT fk_receta_insumo_compuesto FOREIGN KEY (insumo_compuesto_id) 
        REFERENCES insumos_compuestos(id) ON DELETE CASCADE,
    CONSTRAINT fk_receta_empresa FOREIGN KEY (empresa_id) 
        REFERENCES empresas(id),
    
    INDEX idx_receta_insumo_compuesto (insumo_compuesto_id),
    INDEX idx_receta_empresa_etapa (empresa_id, etapa),
    INDEX idx_receta_activo (activo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SELECT '✓ Tablas de insumos compuestos creadas exitosamente' AS resultado;

