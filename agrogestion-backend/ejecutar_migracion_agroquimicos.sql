-- Script para ejecutar migración de agroquímicos
-- Ejecutar este script directamente en la base de datos

-- Agregar campos específicos para agroquímicos a la tabla insumos
ALTER TABLE insumos ADD COLUMN principio_activo VARCHAR(200);
ALTER TABLE insumos ADD COLUMN concentracion VARCHAR(100);
ALTER TABLE insumos ADD COLUMN clase_quimica VARCHAR(100);
ALTER TABLE insumos ADD COLUMN categoria_toxicologica VARCHAR(50);
ALTER TABLE insumos ADD COLUMN periodo_carencia_dias INT;
ALTER TABLE insumos ADD COLUMN dosis_minima_por_ha DECIMAL(10,2);
ALTER TABLE insumos ADD COLUMN dosis_maxima_por_ha DECIMAL(10,2);
ALTER TABLE insumos ADD COLUMN unidad_dosis VARCHAR(50);

-- Crear tabla de dosis por tipo de aplicación para insumos agroquímicos
CREATE TABLE IF NOT EXISTS dosis_insumos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    insumo_id BIGINT NOT NULL,
    tipo_aplicacion VARCHAR(20) NOT NULL,
    dosis_recomendada DECIMAL(10,2) NOT NULL,
    dosis_minima DECIMAL(10,2),
    dosis_maxima DECIMAL(10,2),
    unidad_medida VARCHAR(50),
    descripcion TEXT,
    condiciones_aplicacion TEXT,
    activo BOOLEAN DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (insumo_id) REFERENCES insumos(id) ON DELETE CASCADE,
    INDEX idx_dosis_insumos_insumo (insumo_id),
    INDEX idx_dosis_insumos_tipo (tipo_aplicacion),
    INDEX idx_dosis_insumos_activo (activo)
);

-- Agregar campos para cantidad esperada vs real en aplicaciones_agroquimicos
ALTER TABLE aplicaciones_agroquimicos ADD COLUMN cantidad_esperada DECIMAL(10,2);
ALTER TABLE aplicaciones_agroquimicos ADD COLUMN cantidad_real_usada DECIMAL(10,2);
ALTER TABLE aplicaciones_agroquimicos ADD COLUMN diferencia_cantidad DECIMAL(10,2);
ALTER TABLE aplicaciones_agroquimicos ADD COLUMN porcentaje_desviacion_cantidad DECIMAL(5,2);
ALTER TABLE aplicaciones_agroquimicos ADD COLUMN motivo_desviacion VARCHAR(500);
ALTER TABLE aplicaciones_agroquimicos ADD COLUMN dosis_sugerida_por_ha DECIMAL(10,2);
ALTER TABLE aplicaciones_agroquimicos ADD COLUMN dosis_modificada_por_usuario DECIMAL(10,2);
ALTER TABLE aplicaciones_agroquimicos ADD COLUMN usuario_modifico_dosis BOOLEAN DEFAULT FALSE;

-- Crear tabla para condiciones ambientales de aplicaciones
CREATE TABLE IF NOT EXISTS condiciones_aplicacion (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    aplicacion_id BIGINT NOT NULL,
    temperatura DECIMAL(5,2),
    humedad DECIMAL(5,2),
    velocidad_viento DECIMAL(5,2),
    direccion_viento VARCHAR(10),
    presion_atmosferica DECIMAL(7,2),
    observaciones TEXT,
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (aplicacion_id) REFERENCES aplicaciones_agroquimicos(id) ON DELETE CASCADE,
    INDEX idx_condiciones_aplicacion (aplicacion_id)
);

-- Crear tabla para historial de modificaciones de dosis
CREATE TABLE IF NOT EXISTS historial_modificaciones_dosis (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    aplicacion_id BIGINT NOT NULL,
    dosis_original DECIMAL(10,2),
    dosis_modificada DECIMAL(10,2),
    motivo_modificacion VARCHAR(500),
    usuario_id BIGINT,
    fecha_modificacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (aplicacion_id) REFERENCES aplicaciones_agroquimicos(id) ON DELETE CASCADE,
    INDEX idx_historial_aplicacion (aplicacion_id),
    INDEX idx_historial_usuario (usuario_id)
);

-- Crear índices para optimizar consultas
CREATE INDEX idx_insumos_tipo_agroquimico ON insumos(tipo) WHERE tipo IN ('HERBICIDA', 'FUNGICIDA', 'INSECTICIDA', 'FERTILIZANTE');
CREATE INDEX idx_aplicaciones_cantidad_esperada ON aplicaciones_agroquimicos(cantidad_esperada);
CREATE INDEX idx_aplicaciones_cantidad_real ON aplicaciones_agroquimicos(cantidad_real_usada);
CREATE INDEX idx_aplicaciones_desviacion ON aplicaciones_agroquimicos(porcentaje_desviacion_cantidad);

-- Mensaje de confirmación
SELECT 'Migración de agroquímicos ejecutada exitosamente' AS resultado;
