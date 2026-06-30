-- Módulo Lechería multi-especie — tablas lecheria_* + registro en modules

INSERT INTO modules (name, code, description, active)
SELECT 'Lechería', 'LECHERIA',
       'Gestión lechera multi-especie: animales, lactancia, ordeñe, reproducción, sanidad y ventas.',
       TRUE
WHERE NOT EXISTS (SELECT 1 FROM modules WHERE code = 'LECHERIA');

CREATE TABLE IF NOT EXISTS lecheria_establecimiento (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id          BIGINT       NOT NULL,
    nombre              VARCHAR(150) NOT NULL,
    ubicacion           VARCHAR(255) NULL,
    coordenadas         TEXT         NULL,
    capacidad_animales  INT          NULL,
    activo              BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP    NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_lecheria_est_empresa (empresa_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS lecheria_rodeo (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    establecimiento_id  BIGINT       NOT NULL,
    empresa_id          BIGINT       NOT NULL,
    nombre              VARCHAR(120) NOT NULL,
    especie             VARCHAR(20)  NOT NULL,
    activo              BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP    NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_lecheria_rodeo_est FOREIGN KEY (establecimiento_id) REFERENCES lecheria_establecimiento (id) ON DELETE RESTRICT,
    INDEX idx_lecheria_rodeo_empresa (empresa_id),
    INDEX idx_lecheria_rodeo_est (establecimiento_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS lecheria_parametro_especie (
    especie                 VARCHAR(20)  NOT NULL PRIMARY KEY,
    dias_gestacion          INT          NOT NULL,
    ordenees_dia            INT          NOT NULL DEFAULT 2,
    umbral_rcs_alerta       BIGINT       NOT NULL,
    dim_objetivo_secado     INT          NOT NULL,
    nombre_visible          VARCHAR(80)  NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO lecheria_parametro_especie (especie, dias_gestacion, ordenees_dia, umbral_rcs_alerta, dim_objetivo_secado, nombre_visible)
VALUES
    ('BOVINO', 280, 2, 200000, 305, 'Bovino'),
    ('BUFALO', 310, 2, 200000, 305, 'Búfalo'),
    ('CAPRINO', 150, 2, 500000, 240, 'Caprino'),
    ('OVINO', 147, 2, 500000, 220, 'Ovino'),
    ('CAMELIDO', 345, 1, 300000, 300, 'Camélido')
ON DUPLICATE KEY UPDATE nombre_visible = VALUES(nombre_visible);

CREATE TABLE IF NOT EXISTS lecheria_raza (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id  BIGINT       NOT NULL,
    especie     VARCHAR(20)  NOT NULL,
    nombre      VARCHAR(120) NOT NULL,
    activo      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_lecheria_raza_empresa (empresa_id),
    INDEX idx_lecheria_raza_especie (empresa_id, especie)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS lecheria_motivo_baja (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id  BIGINT       NOT NULL,
    nombre      VARCHAR(120) NOT NULL,
    activo      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_lecheria_motivo_baja_empresa (empresa_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS lecheria_animal (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id          BIGINT       NOT NULL,
    identificacion      VARCHAR(100) NOT NULL,
    especie             VARCHAR(20)  NOT NULL,
    sexo                VARCHAR(10)  NOT NULL,
    estado              VARCHAR(20)  NOT NULL DEFAULT 'VAQUILLONA',
    raza_id             BIGINT       NULL,
    rodeo_id            BIGINT       NULL,
    campana_id          BIGINT       NULL,
    fecha_nacimiento    DATE         NULL,
    fecha_ingreso       DATE         NOT NULL,
    fecha_baja          DATE         NULL,
    activo              BOOLEAN      NOT NULL DEFAULT TRUE,
    observaciones       TEXT         NULL,
    created_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP    NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_lecheria_animal_raza FOREIGN KEY (raza_id) REFERENCES lecheria_raza (id) ON DELETE SET NULL,
    CONSTRAINT fk_lecheria_animal_rodeo FOREIGN KEY (rodeo_id) REFERENCES lecheria_rodeo (id) ON DELETE SET NULL,
    CONSTRAINT fk_lecheria_animal_campana FOREIGN KEY (campana_id) REFERENCES core_campanas (id) ON DELETE SET NULL,
    UNIQUE KEY uk_lecheria_animal_ident (empresa_id, identificacion),
    INDEX idx_lecheria_animal_empresa_estado (empresa_id, estado),
    INDEX idx_lecheria_animal_rodeo (rodeo_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS lecheria_lactancia (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    animal_id       BIGINT   NOT NULL,
    empresa_id      BIGINT   NOT NULL,
    numero_lactancia INT     NOT NULL DEFAULT 1,
    fecha_parto     DATE     NOT NULL,
    fecha_secado    DATE     NULL,
    activa          BOOLEAN  NOT NULL DEFAULT TRUE,
    observaciones   TEXT     NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_lecheria_lact_animal FOREIGN KEY (animal_id) REFERENCES lecheria_animal (id) ON DELETE RESTRICT,
    INDEX idx_lecheria_lact_animal (animal_id, activa),
    INDEX idx_lecheria_lact_empresa (empresa_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS lecheria_registro_ordene (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    animal_id           BIGINT         NOT NULL,
    lactancia_id        BIGINT         NOT NULL,
    empresa_id          BIGINT         NOT NULL,
    fecha               DATE           NOT NULL,
    turno               VARCHAR(10)    NOT NULL,
    litros              DECIMAL(10, 3) NOT NULL,
    grasa_pct           DECIMAL(5, 2)  NULL,
    proteina_pct        DECIMAL(5, 2)  NULL,
    rcs                 BIGINT         NULL,
    temperatura_ambiente DECIMAL(5, 2) NULL,
    humedad_ambiente    DECIMAL(5, 2)  NULL,
    observaciones       TEXT           NULL,
    created_at          TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_lecheria_ord_animal FOREIGN KEY (animal_id) REFERENCES lecheria_animal (id) ON DELETE RESTRICT,
    CONSTRAINT fk_lecheria_ord_lact FOREIGN KEY (lactancia_id) REFERENCES lecheria_lactancia (id) ON DELETE RESTRICT,
    UNIQUE KEY uk_lecheria_ord_turno (animal_id, fecha, turno),
    INDEX idx_lecheria_ord_fecha (empresa_id, fecha)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS lecheria_evento_reproductivo (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    animal_id       BIGINT       NOT NULL,
    empresa_id      BIGINT       NOT NULL,
    tipo            VARCHAR(20)  NOT NULL,
    fecha           DATE         NOT NULL,
    resultado       VARCHAR(30)  NULL,
    toro_pajuela    VARCHAR(150) NULL,
    fecha_prevista_parto DATE    NULL,
    observaciones   TEXT         NULL,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_lecheria_repro_animal FOREIGN KEY (animal_id) REFERENCES lecheria_animal (id) ON DELETE RESTRICT,
    INDEX idx_lecheria_repro_animal (animal_id, fecha)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS lecheria_evento_sanitario (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    animal_id       BIGINT         NOT NULL,
    empresa_id      BIGINT         NOT NULL,
    tipo            VARCHAR(20)    NOT NULL,
    fecha           DATE           NOT NULL,
    descripcion     TEXT           NULL,
    insumo_id       BIGINT         NULL,
    cantidad        DECIMAL(12, 3) NULL,
    dias_retiro     INT            NULL,
    created_at      TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_lecheria_san_animal FOREIGN KEY (animal_id) REFERENCES lecheria_animal (id) ON DELETE RESTRICT,
    INDEX idx_lecheria_san_animal (animal_id, fecha)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS lecheria_score_corporal (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    animal_id   BIGINT        NOT NULL,
    empresa_id  BIGINT        NOT NULL,
    fecha       DATE          NOT NULL,
    valor       DECIMAL(3, 1) NOT NULL,
    observaciones TEXT        NULL,
    created_at  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_lecheria_ecc_animal FOREIGN KEY (animal_id) REFERENCES lecheria_animal (id) ON DELETE RESTRICT,
    INDEX idx_lecheria_ecc_animal (animal_id, fecha)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS lecheria_consumo (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    rodeo_id      BIGINT         NOT NULL,
    empresa_id    BIGINT         NOT NULL,
    campana_id    BIGINT         NOT NULL,
    insumo_id     BIGINT         NOT NULL,
    fecha         DATE           NOT NULL,
    cantidad_kg   DECIMAL(12, 3) NOT NULL,
    observaciones TEXT           NULL,
    created_at    TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_lecheria_cons_rodeo FOREIGN KEY (rodeo_id) REFERENCES lecheria_rodeo (id) ON DELETE RESTRICT,
    CONSTRAINT fk_lecheria_cons_campana FOREIGN KEY (campana_id) REFERENCES core_campanas (id) ON DELETE RESTRICT,
    INDEX idx_lecheria_cons_rodeo (rodeo_id, fecha)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS lecheria_venta_leche (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id    BIGINT         NOT NULL,
    campana_id    BIGINT         NOT NULL,
    fecha         DATE           NOT NULL,
    litros        DECIMAL(12, 3) NOT NULL,
    precio_litro  DECIMAL(12, 4) NULL,
    total         DECIMAL(14, 2) NULL,
    comprador     VARCHAR(150)   NULL,
    observaciones TEXT           NULL,
    created_at    TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_lecheria_venta_campana FOREIGN KEY (campana_id) REFERENCES core_campanas (id) ON DELETE RESTRICT,
    INDEX idx_lecheria_venta_empresa (empresa_id, fecha)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS lecheria_baja_animal (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    animal_id   BIGINT NOT NULL,
    empresa_id  BIGINT NOT NULL,
    fecha       DATE   NOT NULL,
    motivo_id   BIGINT NULL,
    observaciones TEXT NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_lecheria_baja_animal FOREIGN KEY (animal_id) REFERENCES lecheria_animal (id) ON DELETE RESTRICT,
    CONSTRAINT fk_lecheria_baja_motivo FOREIGN KEY (motivo_id) REFERENCES lecheria_motivo_baja (id) ON DELETE SET NULL,
    INDEX idx_lecheria_baja_animal (animal_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
