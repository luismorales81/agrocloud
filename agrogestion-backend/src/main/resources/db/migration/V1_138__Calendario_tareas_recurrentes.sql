-- SPEC: tareas recurrentes del calendario + cumplimiento por ocurrencia (usuario)

CREATE TABLE IF NOT EXISTS calendario_serie_tarea_recurrente (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    titulo VARCHAR(200) NOT NULL,
    descripcion VARCHAR(1000) NULL,
    fecha_inicio DATE NOT NULL COMMENT 'Primera ocurrencia y ancla de repetición',
    fecha_fin DATE NULL COMMENT 'NULL = sin fin explícito; expansión acotada al rango de consulta',
    tipo_repeticion VARCHAR(20) NOT NULL COMMENT 'DIARIA, SEMANAL, MENSUAL, ANUAL',
    activo TINYINT(1) NOT NULL DEFAULT 1,
    fecha_creacion TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_cal_serie_tarea_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id) ON DELETE CASCADE,
    INDEX idx_cal_serie_usuario_activo (usuario_id, activo),
    INDEX idx_cal_serie_fechas (fecha_inicio, fecha_fin)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS calendario_cumplimiento_serie_tarea (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    serie_id BIGINT NOT NULL,
    fecha_ocurrencia DATE NOT NULL,
    cumplida TINYINT(1) NOT NULL DEFAULT 1,
    fecha_marcado TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_cal_cumpl_serie FOREIGN KEY (serie_id) REFERENCES calendario_serie_tarea_recurrente (id) ON DELETE CASCADE,
    UNIQUE KEY uk_serie_fecha_ocurrencia (serie_id, fecha_ocurrencia),
    INDEX idx_cal_cumpl_fecha (fecha_ocurrencia)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
