-- Lechería v1.5: import control lechero, closeout por rodeo, export SENASA borrador

CREATE TABLE IF NOT EXISTS lecheria_import_control_lechero (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id      BIGINT       NOT NULL,
    nombre_archivo  VARCHAR(255) NOT NULL,
    filas_procesadas INT         NOT NULL DEFAULT 0,
    filas_error     INT          NOT NULL DEFAULT 0,
    detalle_errores TEXT         NULL,
    usuario_id      BIGINT       NULL,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_lecheria_import_empresa (empresa_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS lecheria_closeout_rodeo (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    rodeo_id            BIGINT         NOT NULL,
    empresa_id          BIGINT         NOT NULL,
    campana_id          BIGINT         NOT NULL,
    fecha_cierre        DATE           NOT NULL,
    litros_totales      DECIMAL(14, 3) NULL,
    costo_alimentacion  DECIMAL(14, 2) NULL,
    ingresos_leche      DECIMAL(14, 2) NULL,
    margen              DECIMAL(14, 2) NULL,
    observaciones       TEXT           NULL,
    created_at          TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_lecheria_closeout_rodeo FOREIGN KEY (rodeo_id) REFERENCES lecheria_rodeo (id) ON DELETE RESTRICT,
    CONSTRAINT fk_lecheria_closeout_campana FOREIGN KEY (campana_id) REFERENCES core_campanas (id) ON DELETE RESTRICT,
    INDEX idx_lecheria_closeout_rodeo (rodeo_id, campana_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS lecheria_movimiento_senasa (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id      BIGINT       NOT NULL,
    animal_id       BIGINT       NULL,
    tipo_movimiento VARCHAR(30)  NOT NULL,
    fecha           DATE         NOT NULL,
    datos_json      TEXT         NULL,
    exportado       BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_lecheria_senasa_animal FOREIGN KEY (animal_id) REFERENCES lecheria_animal (id) ON DELETE SET NULL,
    INDEX idx_lecheria_senasa_empresa (empresa_id, exportado)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
