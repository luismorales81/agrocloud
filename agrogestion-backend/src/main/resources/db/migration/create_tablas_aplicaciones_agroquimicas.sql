-- ============================================
-- Script de creación de tablas para el módulo de aplicaciones de agroquímicos
-- ============================================

-- Tabla: dosis_aplicacion
-- Almacena las dosis sugeridas de aplicación de insumos según el tipo de aplicación
CREATE TABLE IF NOT EXISTS dosis_aplicacion (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    insumo_id BIGINT NOT NULL,
    tipo_aplicacion VARCHAR(20) NOT NULL,
    dosis_por_ha DECIMAL(10, 2) NOT NULL,
    unidad_medida VARCHAR(50),
    descripcion VARCHAR(500),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (insumo_id) REFERENCES insumos(id) ON DELETE CASCADE,
    INDEX idx_dosis_insumo (insumo_id),
    INDEX idx_dosis_tipo (tipo_aplicacion),
    INDEX idx_dosis_activo (activo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: aplicaciones_agroquimicas
-- Almacena las aplicaciones de agroquímicos realizadas en las labores (tareas)
CREATE TABLE IF NOT EXISTS aplicaciones_agroquimicas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    labor_id BIGINT NOT NULL,
    insumo_id BIGINT NOT NULL,
    tipo_aplicacion VARCHAR(20) NOT NULL,
    cantidad_total_aplicar DECIMAL(10, 2) NOT NULL,
    dosis_aplicada_por_ha DECIMAL(10, 2),
    superficie_aplicada_ha DECIMAL(10, 2),
    unidad_medida VARCHAR(50),
    observaciones VARCHAR(1000),
    fecha_aplicacion TIMESTAMP,
    fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    
    FOREIGN KEY (labor_id) REFERENCES labores(id) ON DELETE CASCADE,
    FOREIGN KEY (insumo_id) REFERENCES insumos(id) ON DELETE RESTRICT,
    INDEX idx_aplicacion_labor (labor_id),
    INDEX idx_aplicacion_insumo (insumo_id),
    INDEX idx_aplicacion_tipo (tipo_aplicacion),
    INDEX idx_aplicacion_fecha (fecha_aplicacion),
    INDEX idx_aplicacion_activo (activo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Datos de ejemplo (opcional)
-- ============================================

-- Ejemplo: Dosis sugeridas para un herbicida
-- INSERT INTO dosis_aplicacion (insumo_id, tipo_aplicacion, dosis_por_ha, unidad_medida, descripcion) 
-- VALUES (1, 'FOLIAR', 2.0, 'litros', 'Aplicación foliar para control de malezas');

-- ============================================
-- Comentarios sobre las tablas
-- ============================================

-- dosis_aplicacion:
-- - Permite configurar dosis sugeridas para cada insumo según el tipo de aplicación
-- - Un insumo puede tener múltiples dosis (una por cada tipo de aplicación)
-- - Facilita el cálculo automático de cantidades en las aplicaciones

-- aplicaciones_agroquimicas:
-- - Registra cada aplicación de agroquímico realizada en una labor
-- - Almacena la cantidad total aplicada y la dosis utilizada
-- - Permite hacer seguimiento del uso de insumos
-- - Al eliminar una aplicación, se restaura el stock del insumo

