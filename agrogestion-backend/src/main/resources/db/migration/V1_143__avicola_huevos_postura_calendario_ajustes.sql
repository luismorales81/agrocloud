-- Producción diaria ampliada (tamaños, rotos, total, clima), ajustes de plantel, calendario huevos

-- ---------------------------------------------------------------------------
-- avicola_huevo_produccion_diaria: columnas nuevas
-- ---------------------------------------------------------------------------
ALTER TABLE avicola_huevo_produccion_diaria
    ADD COLUMN huevos_tam1 INT NOT NULL DEFAULT 0 AFTER cantidad_huevos,
    ADD COLUMN huevos_tam2 INT NOT NULL DEFAULT 0 AFTER huevos_tam1,
    ADD COLUMN huevos_tam3 INT NOT NULL DEFAULT 0 AFTER huevos_tam2,
    ADD COLUMN huevos_tam4 INT NOT NULL DEFAULT 0 AFTER huevos_tam3,
    ADD COLUMN huevos_rotos INT NOT NULL DEFAULT 0 AFTER huevos_tam4,
    ADD COLUMN total_huevos_dia INT NOT NULL DEFAULT 0 AFTER huevos_rotos,
    ADD COLUMN temperatura_dia DECIMAL(5, 2) NULL COMMENT 'Temperatura ambiente registrada (°C)' AFTER total_huevos_dia,
    ADD COLUMN humedad_dia DECIMAL(6, 2) NULL COMMENT 'Humedad relativa registrada (%)' AFTER temperatura_dia;

-- Datos existentes: todo en tam1 y total = cantidad_huevos
UPDATE avicola_huevo_produccion_diaria
SET huevos_tam1 = cantidad_huevos,
    total_huevos_dia = cantidad_huevos
WHERE total_huevos_dia = 0 AND cantidad_huevos > 0;

-- ---------------------------------------------------------------------------
-- Historial de ajustes de cantidad de aves (con motivo)
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS avicola_huevo_ajuste_plantel (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    lote_id BIGINT NOT NULL,
    empresa_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    fecha_hora TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    cantidad_aves_anterior INT NOT NULL,
    cantidad_aves_nueva INT NOT NULL,
    motivo VARCHAR(500) NOT NULL,
    CONSTRAINT fk_ajuste_plantel_lote FOREIGN KEY (lote_id) REFERENCES avicola_huevo_lote (id) ON DELETE RESTRICT,
    CONSTRAINT fk_ajuste_plantel_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id) ON DELETE RESTRICT,
    INDEX idx_ajuste_plantel_lote (lote_id, empresa_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Ajustes auditados de plantel en lotes de postura';

-- ---------------------------------------------------------------------------
-- Recordatorios vinculados a lote de huevos (calendario módulo huevos)
-- ---------------------------------------------------------------------------
ALTER TABLE recordatorios
    ADD COLUMN lote_avicola_huevo_id BIGINT NULL AFTER madre_id,
    ADD CONSTRAINT fk_recordatorio_lote_avicola_huevo FOREIGN KEY (lote_avicola_huevo_id) REFERENCES avicola_huevo_lote (id) ON DELETE SET NULL,
    ADD INDEX idx_recordatorio_lote_avicola_huevo (lote_avicola_huevo_id);

-- ---------------------------------------------------------------------------
-- Tareas recurrentes: ámbito (GENERAL vs calendario módulo huevos)
-- ---------------------------------------------------------------------------
ALTER TABLE calendario_serie_tarea_recurrente
    ADD COLUMN ambito_calendario VARCHAR(40) NOT NULL DEFAULT 'GENERAL' COMMENT 'GENERAL | AVICOLA_HUEVOS' AFTER tipo_repeticion,
    ADD INDEX idx_cal_serie_ambito (usuario_id, ambito_calendario, activo);
