-- Feedlot v1.5: dietas, bunk score, configuración closeout, dieta en lote

CREATE TABLE IF NOT EXISTS feedlot_configuracion_empresa (
    empresa_id       BIGINT      NOT NULL PRIMARY KEY,
    metodo_closeout  VARCHAR(20) NOT NULL DEFAULT 'DEADS_IN',
    created_at       TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP   NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS feedlot_dieta (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id  BIGINT       NOT NULL,
    nombre      VARCHAR(150) NOT NULL,
    activo      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_feedlot_dieta_empresa (empresa_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS feedlot_dieta_fase (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    dieta_id           BIGINT         NOT NULL,
    nombre_fase        VARCHAR(120)   NOT NULL,
    dias_desde_ingreso INT            NOT NULL DEFAULT 0,
    kg_ms_cabeza_dia   DECIMAL(10, 3) NOT NULL,
    insumo_id          BIGINT         NULL,
    created_at         TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP      NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_feedlot_dieta_fase_dieta FOREIGN KEY (dieta_id) REFERENCES feedlot_dieta (id) ON DELETE CASCADE,
    INDEX idx_feedlot_dieta_fase_dieta (dieta_id, dias_desde_ingreso)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS feedlot_lectura_comedero (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    lote_id         BIGINT         NOT NULL,
    empresa_id      BIGINT         NOT NULL,
    fecha           DATE           NOT NULL,
    bunk_score      VARCHAR(20)    NOT NULL,
    kg_entregados   DECIMAL(12, 3) NULL,
    observaciones   TEXT           NULL,
    created_at      TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP      NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_feedlot_lectura_lote FOREIGN KEY (lote_id) REFERENCES feedlot_lote (id) ON DELETE RESTRICT,
    UNIQUE KEY uk_feedlot_lectura_lote_fecha (lote_id, fecha),
    INDEX idx_feedlot_lectura_empresa (empresa_id, fecha)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

ALTER TABLE feedlot_lote
    ADD COLUMN dieta_id BIGINT NULL AFTER observaciones;

ALTER TABLE feedlot_lote
    ADD CONSTRAINT fk_feedlot_lote_dieta FOREIGN KEY (dieta_id) REFERENCES feedlot_dieta (id) ON DELETE SET NULL;
