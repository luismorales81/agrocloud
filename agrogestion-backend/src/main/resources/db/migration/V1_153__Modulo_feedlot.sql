-- Módulo Feedlot (engorde bovino a corral) — tablas feedlot_* + registro en modules

INSERT INTO modules (name, code, description, active)
SELECT 'Engorde a corral (Feedlot)', 'FEEDLOT',
       'Engorde bovino a corral: lotes, pesadas, alimento, sanidad, faena y closeout.',
       TRUE
WHERE NOT EXISTS (SELECT 1 FROM modules WHERE code = 'FEEDLOT');

CREATE TABLE IF NOT EXISTS feedlot_establecimiento (
    id                       BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id               BIGINT       NOT NULL,
    nombre                   VARCHAR(150) NOT NULL,
    ubicacion                VARCHAR(255) NULL,
    capacidad_total_cabezas  INT          NULL,
    activo                   BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at               TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at               TIMESTAMP    NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_feedlot_est_empresa (empresa_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS feedlot_corral (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    establecimiento_id BIGINT       NOT NULL,
    nombre             VARCHAR(120) NOT NULL,
    capacidad_cabezas  INT          NULL,
    estado             VARCHAR(20)  NOT NULL DEFAULT 'DISPONIBLE',
    activo             BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP    NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_feedlot_corral_est FOREIGN KEY (establecimiento_id) REFERENCES feedlot_establecimiento (id) ON DELETE RESTRICT,
    INDEX idx_feedlot_corral_est (establecimiento_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS feedlot_categoria (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id  BIGINT       NOT NULL,
    nombre      VARCHAR(120) NOT NULL,
    activo      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_feedlot_cat_empresa (empresa_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS feedlot_raza (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id  BIGINT       NOT NULL,
    nombre      VARCHAR(120) NOT NULL,
    activo      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_feedlot_raza_empresa (empresa_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS feedlot_motivo_muerte (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id  BIGINT       NOT NULL,
    nombre      VARCHAR(120) NOT NULL,
    activo      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_feedlot_motivo_muerte_empresa (empresa_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS feedlot_proveedor_origen (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id  BIGINT       NOT NULL,
    nombre      VARCHAR(150) NOT NULL,
    tipo        VARCHAR(30)  NOT NULL DEFAULT 'PROPIETARIO',
    activo      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_feedlot_prov_empresa (empresa_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS feedlot_lote (
    id                       BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id               BIGINT         NOT NULL,
    corral_id                BIGINT         NOT NULL,
    campana_id               BIGINT         NOT NULL,
    nombre                   VARCHAR(120)   NOT NULL,
    categoria_id             BIGINT         NULL,
    raza_id                  BIGINT         NULL,
    proveedor_id             BIGINT         NULL,
    tipo_tenencia            VARCHAR(20)    NOT NULL DEFAULT 'PROPIO',
    fecha_ingreso            DATE           NOT NULL,
    fecha_cierre             DATE           NULL,
    cabezas_inicial          INT            NOT NULL,
    cabezas_actuales         INT            NOT NULL,
    peso_promedio_ingreso_kg DECIMAL(10, 2) NOT NULL,
    precio_compra_kg         DECIMAL(12, 2) NULL,
    costo_hoteleria_dia      DECIMAL(12, 2) NULL,
    estado                   VARCHAR(20)    NOT NULL DEFAULT 'ACTIVO',
    observaciones            TEXT           NULL,
    created_at               TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at               TIMESTAMP      NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_feedlot_lote_corral FOREIGN KEY (corral_id) REFERENCES feedlot_corral (id) ON DELETE RESTRICT,
    CONSTRAINT fk_feedlot_lote_campana FOREIGN KEY (campana_id) REFERENCES core_campanas (id) ON DELETE RESTRICT,
    CONSTRAINT fk_feedlot_lote_categoria FOREIGN KEY (categoria_id) REFERENCES feedlot_categoria (id) ON DELETE SET NULL,
    CONSTRAINT fk_feedlot_lote_raza FOREIGN KEY (raza_id) REFERENCES feedlot_raza (id) ON DELETE SET NULL,
    CONSTRAINT fk_feedlot_lote_proveedor FOREIGN KEY (proveedor_id) REFERENCES feedlot_proveedor_origen (id) ON DELETE SET NULL,
    INDEX idx_feedlot_lote_empresa_estado (empresa_id, estado),
    INDEX idx_feedlot_lote_campana (campana_id),
    INDEX idx_feedlot_lote_corral (corral_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS feedlot_pesada (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    lote_id             BIGINT         NOT NULL,
    empresa_id          BIGINT         NOT NULL,
    fecha               DATE           NOT NULL,
    peso_promedio_kg    DECIMAL(10, 2) NOT NULL,
    cabezas_muestreadas INT            NULL,
    observaciones       TEXT           NULL,
    created_at          TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_feedlot_pesada_lote FOREIGN KEY (lote_id) REFERENCES feedlot_lote (id) ON DELETE RESTRICT,
    INDEX idx_feedlot_pesada_lote (lote_id, fecha)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS feedlot_consumo (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    lote_id       BIGINT         NOT NULL,
    empresa_id    BIGINT         NOT NULL,
    campana_id    BIGINT         NOT NULL,
    insumo_id     BIGINT         NOT NULL,
    fecha         DATE           NOT NULL,
    cantidad_kg   DECIMAL(12, 3) NOT NULL,
    materia_seca_pct DECIMAL(5, 2) NULL,
    observaciones TEXT           NULL,
    created_at    TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP      NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_feedlot_consumo_lote FOREIGN KEY (lote_id) REFERENCES feedlot_lote (id) ON DELETE RESTRICT,
    CONSTRAINT fk_feedlot_consumo_campana FOREIGN KEY (campana_id) REFERENCES core_campanas (id) ON DELETE RESTRICT,
    INDEX idx_feedlot_consumo_lote (lote_id, fecha),
    INDEX idx_feedlot_consumo_insumo (insumo_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS feedlot_muerte (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    lote_id       BIGINT NOT NULL,
    empresa_id    BIGINT NOT NULL,
    fecha         DATE   NOT NULL,
    cabezas       INT    NOT NULL,
    motivo_id     BIGINT NULL,
    observaciones TEXT   NULL,
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_feedlot_muerte_lote FOREIGN KEY (lote_id) REFERENCES feedlot_lote (id) ON DELETE RESTRICT,
    CONSTRAINT fk_feedlot_muerte_motivo FOREIGN KEY (motivo_id) REFERENCES feedlot_motivo_muerte (id) ON DELETE SET NULL,
    INDEX idx_feedlot_muerte_lote (lote_id, fecha)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS feedlot_evento_sanitario (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    lote_id       BIGINT       NOT NULL,
    empresa_id    BIGINT       NOT NULL,
    fecha         DATE         NOT NULL,
    tipo          VARCHAR(30)  NOT NULL,
    descripcion   TEXT         NULL,
    insumo_id     BIGINT       NULL,
    dias_retiro   INT          NULL,
    observaciones TEXT         NULL,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_feedlot_san_lote FOREIGN KEY (lote_id) REFERENCES feedlot_lote (id) ON DELETE RESTRICT,
    INDEX idx_feedlot_san_lote (lote_id, fecha)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS feedlot_venta (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    lote_id           BIGINT         NOT NULL,
    empresa_id        BIGINT         NOT NULL,
    campana_id        BIGINT         NOT NULL,
    fecha             DATE           NOT NULL,
    tipo              VARCHAR(30)    NOT NULL,
    cabezas           INT            NOT NULL,
    peso_promedio_kg  DECIMAL(10, 2) NULL,
    precio_kg         DECIMAL(12, 2) NULL,
    total             DECIMAL(14, 2) NULL,
    comprador         VARCHAR(200)   NULL,
    ingreso_id        BIGINT         NULL,
    observaciones     TEXT           NULL,
    created_at        TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_feedlot_venta_lote FOREIGN KEY (lote_id) REFERENCES feedlot_lote (id) ON DELETE RESTRICT,
    CONSTRAINT fk_feedlot_venta_campana FOREIGN KEY (campana_id) REFERENCES core_campanas (id) ON DELETE RESTRICT,
    INDEX idx_feedlot_venta_lote (lote_id, fecha)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS feedlot_ajuste_plantel (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    lote_id         BIGINT NOT NULL,
    empresa_id      BIGINT NOT NULL,
    usuario_id      BIGINT NOT NULL,
    fecha           DATE   NOT NULL,
    cabezas_antes   INT    NOT NULL,
    cabezas_despues INT    NOT NULL,
    motivo          TEXT   NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_feedlot_ajuste_lote FOREIGN KEY (lote_id) REFERENCES feedlot_lote (id) ON DELETE RESTRICT,
    CONSTRAINT fk_feedlot_ajuste_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id) ON DELETE RESTRICT,
    INDEX idx_feedlot_ajuste_lote (lote_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
