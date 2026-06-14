-- ============================================================================
-- SCRIPT PARA CREAR LA TABLA DE EVENTOS SANITARIOS
-- Esta tabla almacena los eventos sanitarios registrados (vacunaciones, tratamientos, etc.)
-- ============================================================================

USE agrocloud;

-- TABLA: porcinos_eventos_sanitarios
CREATE TABLE IF NOT EXISTS porcinos_eventos_sanitarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tipo_evento_sanitario_id BIGINT NOT NULL,
    fecha DATE NOT NULL,
    tipo_entidad ENUM('MADRE', 'PADRILLO', 'LOTE') NOT NULL,
    entidad_id BIGINT NOT NULL,
    dosis DECIMAL(10,2),
    unidad_dosis VARCHAR(50) DEFAULT 'ml',
    lote_medicamento VARCHAR(100),
    profesional_responsable VARCHAR(200),
    fecha_retiro DATE,
    retiro_cumplido BOOLEAN NOT NULL DEFAULT FALSE,
    observaciones TEXT,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    empresa_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (tipo_evento_sanitario_id) REFERENCES porcinos_tipos_evento_sanitario(id) ON DELETE RESTRICT,
    FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE RESTRICT,
    
    INDEX idx_eventos_sanitarios_empresa (empresa_id),
    INDEX idx_eventos_sanitarios_tipo_entidad (tipo_entidad, entidad_id),
    INDEX idx_eventos_sanitarios_fecha (fecha),
    INDEX idx_eventos_sanitarios_activo (activo),
    INDEX idx_eventos_sanitarios_retiro (fecha_retiro, retiro_cumplido),
    INDEX idx_eventos_sanitarios_tipo_evento (tipo_evento_sanitario_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SELECT '✓ Tabla porcinos_eventos_sanitarios creada exitosamente' AS resultado;














