-- Feedlot v2: padrón individual y sesión de manga (bastón electrónico)

CREATE TABLE IF NOT EXISTS feedlot_animal (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id         BIGINT        NOT NULL,
    lote_id            BIGINT        NOT NULL,
    eid                VARCHAR(15)   NULL,
    caravana_visual    VARCHAR(40)   NULL,
    sexo               VARCHAR(20)   NULL,
    categoria_id       BIGINT        NULL,
    fecha_alta         DATE          NOT NULL,
    estado             VARCHAR(20)   NOT NULL DEFAULT 'ACTIVO',
    fecha_baja         DATE          NULL,
    peso_ingreso_kg    DECIMAL(10, 2) NULL,
    observaciones      TEXT          NULL,
    created_at         TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP     NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_feedlot_animal_lote FOREIGN KEY (lote_id) REFERENCES feedlot_lote (id) ON DELETE RESTRICT,
    CONSTRAINT fk_feedlot_animal_cat FOREIGN KEY (categoria_id) REFERENCES feedlot_categoria (id) ON DELETE SET NULL,
    UNIQUE KEY uk_feedlot_animal_eid (empresa_id, eid),
    UNIQUE KEY uk_feedlot_animal_visual (empresa_id, caravana_visual),
    INDEX idx_feedlot_animal_lote_estado (lote_id, estado),
    INDEX idx_feedlot_animal_empresa (empresa_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS feedlot_sesion_manga (
    id                        BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id                BIGINT         NOT NULL,
    lote_id                   BIGINT         NOT NULL,
    tipo                      VARCHAR(20)    NOT NULL,
    fecha                     DATE           NOT NULL,
    estado                    VARCHAR(20)    NOT NULL DEFAULT 'BORRADOR',
    nombre_archivo            VARCHAR(255)   NULL,
    perfil_mapeo_json         TEXT           NULL,
    delimitador               VARCHAR(8)     NULL,
    filas_ok                  INT            NOT NULL DEFAULT 0,
    filas_error               INT            NOT NULL DEFAULT 0,
    detalle_errores           TEXT           NULL,
    usuario_id                BIGINT         NULL,
    movimiento_inventario_ref BIGINT         NULL,
    ajustar_plantel           BOOLEAN        NOT NULL DEFAULT FALSE,
    tipo_evento               VARCHAR(30)    NULL,
    insumo_id                 BIGINT         NULL,
    dosis_por_animal          DECIMAL(12, 4) NULL,
    unidad_dosis              VARCHAR(20)    NULL,
    cantidad_total            DECIMAL(14, 4) NULL,
    dias_retiro               INT            NULL,
    aplicador                 VARCHAR(30)    NULL,
    aplicador_nombre          VARCHAR(150)   NULL,
    origen_insumo             VARCHAR(30)    NULL,
    descripcion               TEXT           NULL,
    subtipo_baja              VARCHAR(20)    NULL,
    motivo_muerte_id          BIGINT         NULL,
    precio_kg                 DECIMAL(12, 2) NULL,
    created_at                TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                TIMESTAMP      NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_feedlot_sesion_lote FOREIGN KEY (lote_id) REFERENCES feedlot_lote (id) ON DELETE RESTRICT,
    INDEX idx_feedlot_sesion_lote (lote_id, fecha),
    INDEX idx_feedlot_sesion_empresa_estado (empresa_id, estado)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS feedlot_sesion_manga_fila (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    sesion_id          BIGINT         NOT NULL,
    empresa_id         BIGINT         NOT NULL,
    numero_linea       INT            NOT NULL,
    eid_crudo          VARCHAR(64)    NULL,
    eid_normalizado    VARCHAR(15)    NULL,
    caravana_visual    VARCHAR(40)    NULL,
    peso_kg            DECIMAL(10, 2) NULL,
    fecha_hora_lectura DATETIME       NULL,
    animal_id          BIGINT         NULL,
    resultado          VARCHAR(20)    NOT NULL,
    mensaje            VARCHAR(500)   NULL,
    CONSTRAINT fk_feedlot_fila_sesion FOREIGN KEY (sesion_id) REFERENCES feedlot_sesion_manga (id) ON DELETE CASCADE,
    CONSTRAINT fk_feedlot_fila_animal FOREIGN KEY (animal_id) REFERENCES feedlot_animal (id) ON DELETE SET NULL,
    INDEX idx_feedlot_fila_sesion (sesion_id),
    INDEX idx_feedlot_fila_resultado (sesion_id, resultado)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS feedlot_pesada_animal (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id BIGINT         NOT NULL,
    animal_id  BIGINT         NOT NULL,
    lote_id    BIGINT         NOT NULL,
    sesion_id  BIGINT         NULL,
    fecha      DATE           NOT NULL,
    peso_kg    DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_feedlot_pa_animal FOREIGN KEY (animal_id) REFERENCES feedlot_animal (id) ON DELETE RESTRICT,
    CONSTRAINT fk_feedlot_pa_lote FOREIGN KEY (lote_id) REFERENCES feedlot_lote (id) ON DELETE RESTRICT,
    CONSTRAINT fk_feedlot_pa_sesion FOREIGN KEY (sesion_id) REFERENCES feedlot_sesion_manga (id) ON DELETE SET NULL,
    INDEX idx_feedlot_pa_animal (animal_id, fecha),
    INDEX idx_feedlot_pa_sesion (sesion_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

ALTER TABLE feedlot_evento_sanitario
    ADD COLUMN animal_id BIGINT NULL,
    ADD COLUMN sesion_id BIGINT NULL,
    ADD COLUMN aplicador VARCHAR(30) NULL,
    ADD COLUMN origen_insumo VARCHAR(30) NULL;

ALTER TABLE feedlot_evento_sanitario
    ADD CONSTRAINT fk_feedlot_san_animal FOREIGN KEY (animal_id) REFERENCES feedlot_animal (id) ON DELETE SET NULL,
    ADD CONSTRAINT fk_feedlot_san_sesion FOREIGN KEY (sesion_id) REFERENCES feedlot_sesion_manga (id) ON DELETE SET NULL;

ALTER TABLE feedlot_muerte
    ADD COLUMN animal_id BIGINT NULL,
    ADD COLUMN sesion_id BIGINT NULL;

ALTER TABLE feedlot_muerte
    ADD CONSTRAINT fk_feedlot_muerte_animal FOREIGN KEY (animal_id) REFERENCES feedlot_animal (id) ON DELETE SET NULL,
    ADD CONSTRAINT fk_feedlot_muerte_sesion FOREIGN KEY (sesion_id) REFERENCES feedlot_sesion_manga (id) ON DELETE SET NULL;

ALTER TABLE feedlot_venta
    ADD COLUMN animal_id BIGINT NULL,
    ADD COLUMN sesion_id BIGINT NULL;

ALTER TABLE feedlot_venta
    ADD CONSTRAINT fk_feedlot_venta_animal FOREIGN KEY (animal_id) REFERENCES feedlot_animal (id) ON DELETE SET NULL,
    ADD CONSTRAINT fk_feedlot_venta_sesion FOREIGN KEY (sesion_id) REFERENCES feedlot_sesion_manga (id) ON DELETE SET NULL;

ALTER TABLE feedlot_pesada
    ADD COLUMN sesion_id BIGINT NULL;

ALTER TABLE feedlot_pesada
    ADD CONSTRAINT fk_feedlot_pesada_sesion FOREIGN KEY (sesion_id) REFERENCES feedlot_sesion_manga (id) ON DELETE SET NULL;
