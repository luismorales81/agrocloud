-- V1_105__Create_Configuracion_Estados_Lotes.sql
-- Migración para crear sistema de configuración de estados y tareas por tipo de cultivo

-- Tabla de tipos de cultivo (catálogo global)
CREATE TABLE IF NOT EXISTS cultivo_tipos_cultivo (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE COMMENT 'Nombre del tipo de cultivo (Soja, Maíz, Trigo, etc.)',
    descripcion VARCHAR(255) COMMENT 'Descripción del tipo de cultivo',
    es_plantilla BOOLEAN DEFAULT TRUE COMMENT 'Indica si es una plantilla global o personalizada',
    activo BOOLEAN DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_tipos_cultivo_activo (activo),
    INDEX idx_tipos_cultivo_plantilla (es_plantilla)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla de estados por tipo de cultivo
CREATE TABLE IF NOT EXISTS cultivo_estados_lote (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tipo_cultivo_id BIGINT COMMENT 'ID del tipo de cultivo (NULL si es personalizado por empresa)',
    empresa_id BIGINT COMMENT 'ID de la empresa (NULL si es plantilla global, NOT NULL si es personalizado)',
    nombre VARCHAR(100) NOT NULL COMMENT 'Nombre del estado (ej: Disponible, Sembrado, En Crecimiento)',
    descripcion VARCHAR(255) COMMENT 'Descripción del estado',
    color VARCHAR(20) DEFAULT '#10b981' COMMENT 'Color para la UI en formato hexadecimal',
    icono VARCHAR(10) COMMENT 'Emoji o icono para representar el estado',
    orden INT NOT NULL COMMENT 'Orden en el flujo de estados',
    es_estado_inicial BOOLEAN DEFAULT FALSE COMMENT 'Indica si es un estado inicial del ciclo',
    es_estado_final BOOLEAN DEFAULT FALSE COMMENT 'Indica si es un estado final del ciclo',
    activo BOOLEAN DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (tipo_cultivo_id) REFERENCES cultivo_tipos_cultivo(id) ON DELETE CASCADE,
    FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE,
    UNIQUE KEY uk_tipo_empresa_nombre (tipo_cultivo_id, empresa_id, nombre),
    INDEX idx_estados_tipo_cultivo (tipo_cultivo_id),
    INDEX idx_estados_empresa (empresa_id),
    INDEX idx_estados_activo (activo),
    INDEX idx_estados_orden (orden)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla de transiciones permitidas entre estados
CREATE TABLE IF NOT EXISTS cultivo_transiciones_estado (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tipo_cultivo_id BIGINT COMMENT 'ID del tipo de cultivo (NULL si es personalizado)',
    empresa_id BIGINT COMMENT 'ID de la empresa (NULL si es plantilla, NOT NULL si es personalizado)',
    estado_origen_id BIGINT NOT NULL COMMENT 'ID del estado origen',
    estado_destino_id BIGINT NOT NULL COMMENT 'ID del estado destino',
    requiere_motivo BOOLEAN DEFAULT FALSE COMMENT 'Indica si esta transición requiere motivo',
    activo BOOLEAN DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (tipo_cultivo_id) REFERENCES cultivo_tipos_cultivo(id) ON DELETE CASCADE,
    FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE,
    FOREIGN KEY (estado_origen_id) REFERENCES cultivo_estados_lote(id) ON DELETE CASCADE,
    FOREIGN KEY (estado_destino_id) REFERENCES cultivo_estados_lote(id) ON DELETE CASCADE,
    UNIQUE KEY uk_transicion (empresa_id, estado_origen_id, estado_destino_id),
    INDEX idx_transiciones_tipo_cultivo (tipo_cultivo_id),
    INDEX idx_transiciones_empresa (empresa_id),
    INDEX idx_transiciones_origen (estado_origen_id),
    INDEX idx_transiciones_destino (estado_destino_id),
    INDEX idx_transiciones_activo (activo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla de tareas/labores permitidas por estado
CREATE TABLE IF NOT EXISTS cultivo_tareas_por_estado (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tipo_cultivo_id BIGINT COMMENT 'ID del tipo de cultivo (NULL si es personalizado)',
    empresa_id BIGINT COMMENT 'ID de la empresa (NULL si es plantilla, NOT NULL si es personalizado)',
    estado_id BIGINT NOT NULL COMMENT 'ID del estado',
    tipo_labor VARCHAR(50) NOT NULL COMMENT 'Tipo de labor (SIEMBRA, FERTILIZACION, RIEGO, etc.)',
    nombre_tarea VARCHAR(100) NOT NULL COMMENT 'Nombre amigable de la tarea',
    descripcion VARCHAR(255) COMMENT 'Descripción de la tarea',
    es_obligatoria BOOLEAN DEFAULT FALSE COMMENT 'Indica si la tarea es obligatoria en este estado',
    orden INT DEFAULT 0 COMMENT 'Orden de visualización de la tarea',
    activo BOOLEAN DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (tipo_cultivo_id) REFERENCES cultivo_tipos_cultivo(id) ON DELETE CASCADE,
    FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE,
    FOREIGN KEY (estado_id) REFERENCES cultivo_estados_lote(id) ON DELETE CASCADE,
    UNIQUE KEY uk_estado_tarea (estado_id, tipo_labor),
    INDEX idx_tareas_tipo_cultivo (tipo_cultivo_id),
    INDEX idx_tareas_empresa (empresa_id),
    INDEX idx_tareas_estado (estado_id),
    INDEX idx_tareas_activo (activo),
    INDEX idx_tareas_orden (orden)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insertar tipos de cultivo predeterminados (plantillas globales; idempotente si ya existen)
INSERT IGNORE INTO cultivo_tipos_cultivo (nombre, descripcion, es_plantilla, activo) VALUES
('Soja', 'Cultivo de soja', TRUE, TRUE),
('Maíz', 'Cultivo de maíz', TRUE, TRUE),
('Trigo', 'Cultivo de trigo', TRUE, TRUE),
('Girasol', 'Cultivo de girasol', TRUE, TRUE),
('Sorgo', 'Cultivo de sorgo', TRUE, TRUE);

-- Insertar estados predeterminados para Soja (plantilla global)
INSERT IGNORE INTO cultivo_estados_lote (tipo_cultivo_id, empresa_id, nombre, descripcion, color, icono, orden, es_estado_inicial, es_estado_final, activo)
SELECT 
    tc.id,
    NULL,
    estados.nombre,
    estados.descripcion,
    estados.color,
    estados.icono,
    estados.orden,
    estados.es_estado_inicial,
    estados.es_estado_final,
    TRUE
FROM (
    SELECT 1 as orden, 'Disponible' as nombre, 'Lote disponible para comenzar un nuevo ciclo' as descripcion, '#10b981' as color, '🟢' as icono, TRUE as es_estado_inicial, FALSE as es_estado_final
    UNION ALL SELECT 2, 'Preparado', 'Lote preparado y listo para siembra', '#22c55e', '🟡', FALSE, FALSE
    UNION ALL SELECT 3, 'Sembrado', 'Cultivo sembrado y en desarrollo inicial', '#3b82f6', '🔵', FALSE, FALSE
    UNION ALL SELECT 4, 'Emergencia', 'Cultivo en etapa de emergencia', '#8b5cf6', '🌱', FALSE, FALSE
    UNION ALL SELECT 5, 'R3', 'Inicio de llenado de granos (R3)', '#f59e0b', '🌾', FALSE, FALSE
    UNION ALL SELECT 6, 'R6', 'Llenado completo de granos (R6)', '#ef4444', '🌽', FALSE, FALSE
    UNION ALL SELECT 7, 'Listo para Cosecha', 'Cultivo listo para ser cosechado', '#f97316', '📦', FALSE, FALSE
    UNION ALL SELECT 8, 'Cosechado', 'Cosecha completada', '#6b7280', '✅', FALSE, TRUE
) estados
CROSS JOIN cultivo_tipos_cultivo tc
WHERE tc.nombre = 'Soja';

-- Insertar transiciones predeterminadas para Soja
INSERT IGNORE INTO cultivo_transiciones_estado (tipo_cultivo_id, empresa_id, estado_origen_id, estado_destino_id, requiere_motivo, activo)
SELECT 
    tc.id,
    NULL,
    eo.id,
    ed.id,
    FALSE,
    TRUE
FROM cultivo_tipos_cultivo tc
CROSS JOIN cultivo_estados_lote eo
CROSS JOIN cultivo_estados_lote ed
WHERE tc.nombre = 'Soja'
  AND eo.tipo_cultivo_id = tc.id
  AND ed.tipo_cultivo_id = tc.id
  AND (
    (eo.nombre = 'Disponible' AND ed.nombre = 'Preparado') OR
    (eo.nombre = 'Preparado' AND ed.nombre = 'Sembrado') OR
    (eo.nombre = 'Sembrado' AND ed.nombre = 'Emergencia') OR
    (eo.nombre = 'Emergencia' AND ed.nombre = 'R3') OR
    (eo.nombre = 'R3' AND ed.nombre = 'R6') OR
    (eo.nombre = 'R6' AND ed.nombre = 'Listo para Cosecha') OR
    (eo.nombre = 'Listo para Cosecha' AND ed.nombre = 'Cosechado') OR
    (eo.nombre = 'Cosechado' AND ed.nombre = 'Disponible')
  );

-- Insertar tareas predeterminadas para estados de Soja
INSERT IGNORE INTO cultivo_tareas_por_estado (tipo_cultivo_id, empresa_id, estado_id, tipo_labor, nombre_tarea, descripcion, es_obligatoria, orden, activo)
SELECT 
    tc.id,
    NULL,
    e.id,
    tareas.tipo_labor,
    tareas.nombre_tarea,
    tareas.descripcion,
    tareas.es_obligatoria,
    tareas.orden,
    TRUE
FROM cultivo_tipos_cultivo tc
CROSS JOIN cultivo_estados_lote e
CROSS JOIN (
    SELECT 'Disponible' as estado, 'MANTENIMIENTO' as tipo_labor, 'Arado' as nombre_tarea, 'Preparación profunda del suelo' as descripcion, FALSE as es_obligatoria, 1 as orden
    UNION ALL SELECT 'Disponible', 'MANTENIMIENTO', 'Rastra', 'Nivelación del suelo', FALSE, 2
    UNION ALL SELECT 'Preparado', 'SIEMBRA', 'Siembra', 'Plantación del cultivo', TRUE, 1
    UNION ALL SELECT 'Sembrado', 'RIEGO', 'Riego', 'Aplicación de agua', FALSE, 1
    UNION ALL SELECT 'Sembrado', 'FERTILIZACION', 'Fertilización', 'Aplicación de nutrientes', FALSE, 2
    UNION ALL SELECT 'Emergencia', 'RIEGO', 'Riego', 'Aplicación de agua', FALSE, 1
    UNION ALL SELECT 'Emergencia', 'CONTROL_MALEZAS', 'Control de Malezas', 'Aplicación de herbicidas', FALSE, 2
    UNION ALL SELECT 'R3', 'FERTILIZACION', 'Fertilización', 'Aplicación de nutrientes', FALSE, 1
    UNION ALL SELECT 'R3', 'RIEGO', 'Riego', 'Aplicación de agua', FALSE, 2
    UNION ALL SELECT 'R6', 'RIEGO', 'Riego', 'Aplicación de agua', FALSE, 1
    UNION ALL SELECT 'R6', 'CONTROL_PLAGAS', 'Control de Plagas', 'Aplicación de insecticidas', FALSE, 2
    UNION ALL SELECT 'Listo para Cosecha', 'COSECHA', 'Cosecha', 'Recolección del cultivo', TRUE, 1
    UNION ALL SELECT 'Cosechado', 'MANTENIMIENTO', 'Arado', 'Preparación para nuevo ciclo', FALSE, 1
) tareas
WHERE tc.nombre = 'Soja'
  AND e.tipo_cultivo_id = tc.id
  AND e.nombre = tareas.estado;

