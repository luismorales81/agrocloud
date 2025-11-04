-- Migración para crear las tablas del sistema integral de agroquímicos
-- Integra agroquímicos, dosis por tipo de aplicación y aplicaciones con evaluación de desviaciones

-- Tabla de agroquímicos (extiende el concepto de insumos)
CREATE TABLE IF NOT EXISTS agroquimicos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    insumo_id BIGINT NOT NULL,
    principio_activo VARCHAR(200),
    concentracion VARCHAR(100),
    clase_quimica VARCHAR(100),
    modo_accion VARCHAR(200),
    periodo_carencia_dias INT,
    dosis_minima_por_ha DECIMAL(10,2),
    dosis_maxima_por_ha DECIMAL(10,2),
    unidad_dosis VARCHAR(50),
    observaciones TEXT,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (insumo_id) REFERENCES insumos(id) ON DELETE CASCADE,
    INDEX idx_agroquimicos_insumo (insumo_id),
    INDEX idx_agroquimicos_clase_quimica (clase_quimica),
    INDEX idx_agroquimicos_principio_activo (principio_activo),
    INDEX idx_agroquimicos_activo (activo)
);

-- Tabla de dosis por tipo de aplicación para agroquímicos
CREATE TABLE IF NOT EXISTS dosis_agroquimicos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    agroquimico_id BIGINT NOT NULL,
    tipo_aplicacion ENUM('FOLIAR', 'TERRESTRE', 'AEREA', 'PRECISION') NOT NULL,
    dosis_recomendada DECIMAL(10,2) NOT NULL,
    dosis_minima DECIMAL(10,2),
    dosis_maxima DECIMAL(10,2),
    unidad_medida VARCHAR(50),
    descripcion VARCHAR(500),
    condiciones_aplicacion TEXT,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (agroquimico_id) REFERENCES agroquimicos(id) ON DELETE CASCADE,
    UNIQUE KEY uk_dosis_agroquimico_tipo (agroquimico_id, tipo_aplicacion),
    INDEX idx_dosis_agroquimicos_agroquimico (agroquimico_id),
    INDEX idx_dosis_agroquimicos_tipo (tipo_aplicacion),
    INDEX idx_dosis_agroquimicos_activo (activo)
);

-- Tabla de aplicaciones de agroquímicos (integrada con labores)
CREATE TABLE IF NOT EXISTS aplicaciones_agroquimicos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    labor_id BIGINT NOT NULL,
    agroquimico_id BIGINT NOT NULL,
    tipo_aplicacion ENUM('FOLIAR', 'TERRESTRE', 'AEREA', 'PRECISION') NOT NULL,
    superficie_aplicada_ha DECIMAL(10,2) NOT NULL,
    dosis_aplicada_por_ha DECIMAL(10,2) NOT NULL,
    cantidad_total_aplicada DECIMAL(10,2) NOT NULL,
    dosis_recomendada_por_ha DECIMAL(10,2),
    desviacion_porcentual DECIMAL(5,2),
    unidad_medida VARCHAR(50),
    condiciones_climaticas VARCHAR(500),
    equipo_aplicacion VARCHAR(200),
    operador VARCHAR(200),
    observaciones TEXT,
    fecha_aplicacion TIMESTAMP,
    fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    
    FOREIGN KEY (labor_id) REFERENCES labores(id) ON DELETE CASCADE,
    FOREIGN KEY (agroquimico_id) REFERENCES agroquimicos(id) ON DELETE CASCADE,
    INDEX idx_aplicaciones_labor (labor_id),
    INDEX idx_aplicaciones_agroquimico (agroquimico_id),
    INDEX idx_aplicaciones_tipo (tipo_aplicacion),
    INDEX idx_aplicaciones_fecha (fecha_aplicacion),
    INDEX idx_aplicaciones_activo (activo)
);

-- Insertar datos de ejemplo para tipos de aplicación si no existen
INSERT IGNORE INTO tipos_aplicacion (nombre, descripcion) VALUES
('FOLIAR', 'Aplicación Foliar - Pulverización sobre las hojas'),
('TERRESTRE', 'Aplicación Terrestre - Aplicación directa al suelo'),
('AEREA', 'Aplicación Aérea - Pulverización desde avión o dron'),
('PRECISION', 'Aplicación de Precisión - Aplicación dirigida con GPS');

-- Crear vistas para facilitar consultas
CREATE OR REPLACE VIEW vista_agroquimicos_completos AS
SELECT 
    a.id,
    a.insumo_id,
    i.nombre as nombre_insumo,
    i.descripcion as descripcion_insumo,
    i.unidad_medida as unidad_medida_insumo,
    i.stock_actual,
    i.stock_minimo,
    i.precio_unitario,
    a.principio_activo,
    a.concentracion,
    a.clase_quimica,
    a.modo_accion,
    a.periodo_carencia_dias,
    a.dosis_minima_por_ha,
    a.dosis_maxima_por_ha,
    a.unidad_dosis,
    a.observaciones,
    a.activo,
    a.fecha_creacion,
    a.fecha_actualizacion
FROM agroquimicos a
JOIN insumos i ON a.insumo_id = i.id;

CREATE OR REPLACE VIEW vista_dosis_agroquimicos AS
SELECT 
    d.id,
    d.agroquimico_id,
    a.insumo_id,
    i.nombre as nombre_agroquimico,
    d.tipo_aplicacion,
    d.dosis_recomendada,
    d.dosis_minima,
    d.dosis_maxima,
    d.unidad_medida,
    d.descripcion,
    d.condiciones_aplicacion,
    d.activo,
    d.fecha_creacion
FROM dosis_agroquimicos d
JOIN agroquimicos a ON d.agroquimico_id = a.id
JOIN insumos i ON a.insumo_id = i.id;

CREATE OR REPLACE VIEW vista_aplicaciones_agroquimicos AS
SELECT 
    ap.id,
    ap.labor_id,
    l.nombre as nombre_labor,
    ap.agroquimico_id,
    i.nombre as nombre_agroquimico,
    ap.tipo_aplicacion,
    ap.superficie_aplicada_ha,
    ap.dosis_aplicada_por_ha,
    ap.cantidad_total_aplicada,
    ap.dosis_recomendada_por_ha,
    ap.desviacion_porcentual,
    CASE 
        WHEN ABS(ap.desviacion_porcentual) <= 5 THEN 'Óptima'
        WHEN ABS(ap.desviacion_porcentual) <= 10 THEN 'Aceptable'
        WHEN ABS(ap.desviacion_porcentual) <= 20 THEN 'Alta'
        ELSE 'Crítica'
    END as nivel_desviacion,
    ap.unidad_medida,
    ap.condiciones_climaticas,
    ap.equipo_aplicacion,
    ap.operador,
    ap.observaciones,
    ap.fecha_aplicacion,
    ap.fecha_registro,
    ap.activo
FROM aplicaciones_agroquimicos ap
JOIN labores l ON ap.labor_id = l.id
JOIN agroquimicos a ON ap.agroquimico_id = a.id
JOIN insumos i ON a.insumo_id = i.id;

-- Crear índices adicionales para optimizar consultas
CREATE INDEX idx_aplicaciones_desviacion ON aplicaciones_agroquimicos(desviacion_porcentual);
CREATE INDEX idx_aplicaciones_fecha_aplicacion ON aplicaciones_agroquimicos(fecha_aplicacion);
CREATE INDEX idx_agroquimicos_fecha_creacion ON agroquimicos(fecha_creacion);

-- Comentarios para documentación
ALTER TABLE agroquimicos COMMENT = 'Tabla de agroquímicos con información específica de productos químicos';
ALTER TABLE dosis_agroquimicos COMMENT = 'Dosis recomendadas por tipo de aplicación para cada agroquímico';
ALTER TABLE aplicaciones_agroquimicos COMMENT = 'Aplicaciones reales de agroquímicos con evaluación de desviaciones';









