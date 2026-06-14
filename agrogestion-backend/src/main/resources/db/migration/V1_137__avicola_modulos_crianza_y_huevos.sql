-- =============================================================================
-- Dos módulos avícolas: crianza (tablas avicola_* existentes) y producción de huevos (avicola_huevo_*)
-- Renombra el código de módulo AVICOLA → AVICOLA_CRIANZA; registra AVICOLA_HUEVOS
-- =============================================================================

-- Catálogo modules: crianza (reemplaza código heredado AVICOLA si aún existe)
UPDATE modules
SET code = 'AVICOLA_CRIANZA',
    name = 'Avícola crianza',
    description = 'Parrilleros y crianza: lotes, pesadas, mortalidad, ventas, consumos y sanidad.'
WHERE code = 'AVICOLA';

INSERT INTO modules (name, code, description, active)
SELECT 'Avícola crianza', 'AVICOLA_CRIANZA', 'Parrilleros y crianza: lotes, pesadas, mortalidad, ventas, consumos y sanidad.', TRUE
WHERE NOT EXISTS (SELECT 1 FROM modules WHERE code = 'AVICOLA_CRIANZA');

INSERT INTO modules (name, code, description, active)
VALUES (
    'Avícola producción de huevos',
    'AVICOLA_HUEVOS',
    'Postura: lotes de puesta, producción diaria de huevos, consumos y sanidad.',
    TRUE
)
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    description = VALUES(description),
    active = VALUES(active);

-- ----------------------------------------------------------------------------- 
-- Módulo huevos: establecimientos y razas propios (sin FK a tablas de crianza)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS avicola_huevo_establecimiento (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id    BIGINT       NOT NULL,
    nombre        VARCHAR(150) NOT NULL,
    observaciones TEXT         NULL,
    activo        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP    NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_avicola_huevo_est_empresa (empresa_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Establecimientos del módulo avícola huevos';

CREATE TABLE IF NOT EXISTS avicola_huevo_raza (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id    BIGINT       NOT NULL,
    nombre        VARCHAR(120) NOT NULL,
    activo        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP    NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_avicola_huevo_raza_empresa (empresa_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Razas/líneas de puesta por empresa';

CREATE TABLE IF NOT EXISTS avicola_huevo_lote (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id              BIGINT       NOT NULL,
    establecimiento_id      BIGINT       NOT NULL,
    raza_id                 BIGINT       NOT NULL,
    nombre                  VARCHAR(100) NOT NULL,
    fecha_inicio            DATE         NOT NULL,
    cantidad_aves_inicial   INT          NOT NULL,
    cantidad_aves_actual    INT          NOT NULL,
    estado                  VARCHAR(30)  NOT NULL DEFAULT 'ACTIVO',
    fecha_cierre            DATE         NULL,
    observaciones           TEXT         NULL,
    created_at              TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP    NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_avicola_huevo_lote_est FOREIGN KEY (establecimiento_id) REFERENCES avicola_huevo_establecimiento (id) ON DELETE RESTRICT,
    CONSTRAINT fk_avicola_huevo_lote_raza FOREIGN KEY (raza_id) REFERENCES avicola_huevo_raza (id) ON DELETE RESTRICT,
    INDEX idx_avicola_huevo_lote_empresa (empresa_id),
    INDEX idx_avicola_huevo_lote_estado (empresa_id, estado)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Lote de postura (producción de huevos)';

CREATE TABLE IF NOT EXISTS avicola_huevo_produccion_diaria (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    lote_id          BIGINT       NOT NULL,
    empresa_id       BIGINT       NOT NULL,
    fecha            DATE         NOT NULL,
    cantidad_huevos  INT          NOT NULL,
    observaciones    TEXT         NULL,
    created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_avicola_huevo_prod_lote FOREIGN KEY (lote_id) REFERENCES avicola_huevo_lote (id) ON DELETE RESTRICT,
    INDEX idx_avicola_huevo_prod_lote (lote_id, empresa_id),
    INDEX idx_avicola_huevo_prod_empresa (empresa_id),
    UNIQUE KEY uk_avicola_huevo_prod_lote_fecha (lote_id, fecha)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Producción diaria de huevos por lote';

CREATE TABLE IF NOT EXISTS avicola_huevo_consumo (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    lote_id       BIGINT        NOT NULL,
    empresa_id    BIGINT        NOT NULL,
    insumo_id     BIGINT        NOT NULL,
    fecha         DATE          NOT NULL,
    cantidad      DECIMAL(12,3) NOT NULL,
    tipo          VARCHAR(20)   NOT NULL DEFAULT 'MANUAL',
    observaciones TEXT          NULL,
    created_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_avicola_huevo_cons_lote FOREIGN KEY (lote_id) REFERENCES avicola_huevo_lote (id) ON DELETE RESTRICT,
    INDEX idx_avicola_huevo_cons_lote (lote_id, empresa_id),
    INDEX idx_avicola_huevo_cons_insumo (insumo_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Consumo de insumos módulo huevos (movimiento inventario vía servicio)';

CREATE TABLE IF NOT EXISTS avicola_huevo_evento_sanitario (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    lote_id       BIGINT        NOT NULL,
    empresa_id    BIGINT        NOT NULL,
    fecha         DATE          NOT NULL,
    tipo          VARCHAR(80)   NOT NULL,
    descripcion   TEXT          NULL,
    insumo_id     BIGINT        NULL,
    dosis         DECIMAL(10,3) NULL,
    observaciones TEXT          NULL,
    created_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_avicola_huevo_san_lote FOREIGN KEY (lote_id) REFERENCES avicola_huevo_lote (id) ON DELETE RESTRICT,
    INDEX idx_avicola_huevo_san_lote (lote_id, empresa_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Sanidad en lotes de postura';
