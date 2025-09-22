-- Migración V1.4: Crear tabla historial_cosechas
-- Fecha: 2024-01-XX
-- Descripción: Crear tabla para el historial de cosechas con todas las relaciones necesarias

-- Crear tabla historial_cosechas
CREATE TABLE historial_cosechas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    lote_id BIGINT NOT NULL,
    cultivo_id BIGINT NOT NULL,
    fecha_siembra DATE NOT NULL,
    fecha_cosecha DATE NOT NULL,
    superficie_hectareas DECIMAL(10,2) NOT NULL,
    cantidad_cosechada DECIMAL(10,2) NOT NULL,
    unidad_cosecha VARCHAR(10) NOT NULL,
    rendimiento_real DECIMAL(10,2),
    rendimiento_esperado DECIMAL(10,2),
    humedad_cosecha DECIMAL(5,2),
    variedad_semilla VARCHAR(100),
    observaciones TEXT,
    estado_suelo VARCHAR(50) DEFAULT 'BUENO',
    requiere_descanso BOOLEAN DEFAULT FALSE,
    dias_descanso_recomendados INT DEFAULT 0,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    usuario_id BIGINT NOT NULL,
    
    -- Restricciones de integridad
    CONSTRAINT fk_historial_cosechas_lote 
        FOREIGN KEY (lote_id) REFERENCES lotes(id) ON DELETE CASCADE,
    CONSTRAINT fk_historial_cosechas_cultivo 
        FOREIGN KEY (cultivo_id) REFERENCES cultivos(id) ON DELETE CASCADE,
    CONSTRAINT fk_historial_cosechas_usuario 
        FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    
    -- Restricciones de validación
    CONSTRAINT chk_historial_superficie_positiva 
        CHECK (superficie_hectareas > 0),
    CONSTRAINT chk_historial_cantidad_positiva 
        CHECK (cantidad_cosechada > 0),
    CONSTRAINT chk_historial_fecha_siembra_antes_cosecha 
        CHECK (fecha_siembra <= fecha_cosecha),
    CONSTRAINT chk_historial_humedad_valida 
        CHECK (humedad_cosecha >= 0 AND humedad_cosecha <= 100),
    CONSTRAINT chk_historial_dias_descanso_positivos 
        CHECK (dias_descanso_recomendados >= 0),
    CONSTRAINT chk_historial_estado_suelo_valido 
        CHECK (estado_suelo IN ('BUENO', 'DESCANSANDO', 'AGOTADO'))
);

-- Crear índices para optimizar consultas
CREATE INDEX idx_historial_cosechas_lote_id ON historial_cosechas(lote_id);
CREATE INDEX idx_historial_cosechas_cultivo_id ON historial_cosechas(cultivo_id);
CREATE INDEX idx_historial_cosechas_usuario_id ON historial_cosechas(usuario_id);
CREATE INDEX idx_historial_cosechas_fecha_cosecha ON historial_cosechas(fecha_cosecha);
CREATE INDEX idx_historial_cosechas_estado_suelo ON historial_cosechas(estado_suelo);
CREATE INDEX idx_historial_cosechas_requiere_descanso ON historial_cosechas(requiere_descanso);

-- Crear índice compuesto para consultas frecuentes
CREATE INDEX idx_historial_cosechas_lote_fecha ON historial_cosechas(lote_id, fecha_cosecha DESC);
CREATE INDEX idx_historial_cosechas_usuario_fecha ON historial_cosechas(usuario_id, fecha_cosecha DESC);

-- Comentarios en la tabla y columnas
ALTER TABLE historial_cosechas COMMENT = 'Historial completo de cosechas por lote para análisis y planificación';

ALTER TABLE historial_cosechas 
    MODIFY COLUMN id BIGINT AUTO_INCREMENT COMMENT 'Identificador único del registro de historial',
    MODIFY COLUMN lote_id BIGINT NOT NULL COMMENT 'Referencia al lote donde se realizó la cosecha',
    MODIFY COLUMN cultivo_id BIGINT NOT NULL COMMENT 'Referencia al cultivo cosechado',
    MODIFY COLUMN fecha_siembra DATE NOT NULL COMMENT 'Fecha de siembra del cultivo',
    MODIFY COLUMN fecha_cosecha DATE NOT NULL COMMENT 'Fecha de cosecha del cultivo',
    MODIFY COLUMN superficie_hectareas DECIMAL(10,2) NOT NULL COMMENT 'Superficie cosechada en hectáreas',
    MODIFY COLUMN cantidad_cosechada DECIMAL(10,2) NOT NULL COMMENT 'Cantidad total cosechada',
    MODIFY COLUMN unidad_cosecha VARCHAR(10) NOT NULL COMMENT 'Unidad de medida de la cosecha (kg, tn, qq)',
    MODIFY COLUMN rendimiento_real DECIMAL(10,2) COMMENT 'Rendimiento real obtenido en kg/ha',
    MODIFY COLUMN rendimiento_esperado DECIMAL(10,2) COMMENT 'Rendimiento esperado en kg/ha',
    MODIFY COLUMN humedad_cosecha DECIMAL(5,2) COMMENT 'Humedad del grano al momento de la cosecha (%)',
    MODIFY COLUMN variedad_semilla VARCHAR(100) COMMENT 'Variedad de semilla utilizada',
    MODIFY COLUMN observaciones TEXT COMMENT 'Observaciones adicionales sobre la cosecha',
    MODIFY COLUMN estado_suelo VARCHAR(50) DEFAULT 'BUENO' COMMENT 'Estado del suelo después de la cosecha',
    MODIFY COLUMN requiere_descanso BOOLEAN DEFAULT FALSE COMMENT 'Indica si el lote requiere período de descanso',
    MODIFY COLUMN dias_descanso_recomendados INT DEFAULT 0 COMMENT 'Días de descanso recomendados para el lote',
    MODIFY COLUMN fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Fecha de creación del registro',
    MODIFY COLUMN fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Fecha de última actualización',
    MODIFY COLUMN usuario_id BIGINT NOT NULL COMMENT 'Usuario que registró la cosecha';

-- Crear vista para consultas frecuentes del historial
CREATE VIEW vista_historial_cosechas_completo AS
SELECT 
    hc.id,
    hc.lote_id,
    l.nombre AS lote_nombre,
    hc.cultivo_id,
    c.nombre AS cultivo_nombre,
    c.variedad AS cultivo_variedad,
    hc.fecha_siembra,
    hc.fecha_cosecha,
    DATEDIFF(hc.fecha_cosecha, hc.fecha_siembra) AS dias_ciclo,
    hc.superficie_hectareas,
    hc.cantidad_cosechada,
    hc.unidad_cosecha,
    hc.rendimiento_real,
    hc.rendimiento_esperado,
    CASE 
        WHEN hc.rendimiento_esperado > 0 THEN 
            ROUND((hc.rendimiento_real / hc.rendimiento_esperado) * 100, 2)
        ELSE 0 
    END AS porcentaje_cumplimiento,
    hc.humedad_cosecha,
    hc.variedad_semilla,
    hc.observaciones,
    hc.estado_suelo,
    hc.requiere_descanso,
    hc.dias_descanso_recomendados,
    hc.fecha_creacion,
    hc.fecha_actualizacion,
    hc.usuario_id,
    u.email AS usuario_email
FROM historial_cosechas hc
JOIN lotes l ON hc.lote_id = l.id
JOIN cultivos c ON hc.cultivo_id = c.id
JOIN usuarios u ON hc.usuario_id = u.id;

-- Crear vista para estadísticas de rendimiento por cultivo
CREATE VIEW vista_estadisticas_rendimiento_cultivo AS
SELECT 
    c.id AS cultivo_id,
    c.nombre AS cultivo_nombre,
    COUNT(hc.id) AS total_cosechas,
    AVG(hc.rendimiento_real) AS rendimiento_promedio,
    MIN(hc.rendimiento_real) AS rendimiento_minimo,
    MAX(hc.rendimiento_real) AS rendimiento_maximo,
    AVG(CASE 
        WHEN hc.rendimiento_esperado > 0 THEN 
            (hc.rendimiento_real / hc.rendimiento_esperado) * 100
        ELSE 0 
    END) AS cumplimiento_promedio,
    SUM(hc.superficie_hectareas) AS superficie_total_cosechada,
    SUM(hc.cantidad_cosechada) AS cantidad_total_cosechada
FROM cultivos c
LEFT JOIN historial_cosechas hc ON c.id = hc.cultivo_id
GROUP BY c.id, c.nombre;

-- Crear vista para lotes que requieren descanso
CREATE VIEW vista_lotes_requieren_descanso AS
SELECT 
    l.id AS lote_id,
    l.nombre AS lote_nombre,
    hc.fecha_cosecha,
    hc.estado_suelo,
    hc.dias_descanso_recomendados,
    DATEDIFF(CURDATE(), hc.fecha_cosecha) AS dias_desde_cosecha,
    CASE 
        WHEN DATEDIFF(CURDATE(), hc.fecha_cosecha) >= hc.dias_descanso_recomendados THEN 'LISTO_PARA_SIEMBRA'
        ELSE 'EN_DESCANSO'
    END AS estado_lote
FROM lotes l
JOIN historial_cosechas hc ON l.id = hc.lote_id
WHERE hc.requiere_descanso = TRUE
AND hc.fecha_cosecha = (
    SELECT MAX(fecha_cosecha) 
    FROM historial_cosechas hc2 
    WHERE hc2.lote_id = l.id
);

-- Insertar datos de ejemplo para testing (opcional)
-- INSERT INTO historial_cosechas (lote_id, cultivo_id, fecha_siembra, fecha_cosecha, superficie_hectareas, cantidad_cosechada, unidad_cosecha, rendimiento_real, rendimiento_esperado, humedad_cosecha, variedad_semilla, observaciones, estado_suelo, requiere_descanso, dias_descanso_recomendados, usuario_id)
-- VALUES 
-- (1, 1, '2024-01-15', '2024-04-15', 10.5, 4200, 'kg', 4000, 4500, 14.5, 'Variedad A', 'Cosecha exitosa', 'BUENO', FALSE, 0, 1),
-- (2, 2, '2024-02-01', '2024-05-01', 8.0, 3200, 'kg', 4000, 3800, 13.2, 'Variedad B', 'Rendimiento superior al esperado', 'BUENO', FALSE, 0, 1);

-- Mensaje de confirmación
SELECT 'Tabla historial_cosechas creada exitosamente con todas sus relaciones y vistas' AS mensaje;
