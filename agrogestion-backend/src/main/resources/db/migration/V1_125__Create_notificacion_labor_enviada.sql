-- Spec SDD: notificaciones derivadas de labores (comportamiento, no entidad Reminder).
-- Tabla solo para no duplicar envíos (auditoría de envío), no es un recordatorio.

CREATE TABLE notificacion_labor_enviada (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    labor_id BIGINT NOT NULL,
    tipo VARCHAR(20) NOT NULL COMMENT '24H_ANTES | MISMO_DIA',
    fecha_envio DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_labor_tipo_fecha (labor_id, tipo, fecha_envio),
    CONSTRAINT fk_notif_labor FOREIGN KEY (labor_id) REFERENCES cultivo_labores(id) ON DELETE CASCADE
);

CREATE INDEX idx_notif_labor_fecha ON notificacion_labor_enviada(labor_id, tipo);
