-- =============================================================================
-- Módulo AVICOLA_PONEDORAS — esquema inicial (tablas propias prefijo avicola_ponedoras_)
-- Multiempresa: empresa_id en todas las tablas; FK a galpón donde aplica.
-- Insumo CORE: cultivo_insumos (FK solo en consumo / evento opcional).
-- Idempotente: CREATE TABLE IF NOT EXISTS + ajuste company_modules.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- avicola_ponedoras_galpon: unidad productiva (equivalente a "lote" del módulo)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS avicola_ponedoras_galpon (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id              BIGINT       NOT NULL COMMENT 'Empresa propietaria',
    nombre                  VARCHAR(120) NOT NULL COMMENT 'Identificación del galpón o nave',
    fecha_inicio_ciclo      DATE         NOT NULL COMMENT 'Inicio del ciclo de postura en este galpón',
    cantidad_aves_inicial   INT          NOT NULL COMMENT 'Aves al inicio del ciclo',
    cantidad_aves_actual    INT          NULL COMMENT 'Stock vivo estimado (opcional; negocio en aplicación)',
    estado                  VARCHAR(30)  NOT NULL DEFAULT 'ACTIVO' COMMENT 'ACTIVO | CERRADO',
    fecha_cierre            DATE         NULL COMMENT 'Fecha de cierre o liquidación del galpón',
    observaciones           TEXT         NULL,
    created_at              TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP    NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_ponedoras_galpon_empresa (empresa_id),
    INDEX idx_ponedoras_galpon_estado (empresa_id, estado)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Galpones / unidades productivas de ponedoras por empresa';

-- -----------------------------------------------------------------------------
-- avicola_ponedoras_postura: registro de producción de huevos por categoría (día o semana)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS avicola_ponedoras_postura (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    galpon_id        BIGINT       NOT NULL COMMENT 'Galpón productor',
    empresa_id       BIGINT       NOT NULL COMMENT 'Empresa',
    periodo_tipo     VARCHAR(15)  NOT NULL COMMENT 'DIARIO | SEMANAL',
    fecha_periodo    DATE         NOT NULL COMMENT 'Fecha del día o inicio de semana según periodo_tipo',
    categoria_huevo  VARCHAR(40)  NOT NULL COMMENT 'Categoría comercial o de calidad (A, B, extra, etc.)',
    cantidad         INT          NOT NULL COMMENT 'Cantidad de huevos en la categoría',
    observaciones    TEXT         NULL,
    created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP    NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_ponedoras_postura_galpon FOREIGN KEY (galpon_id) REFERENCES avicola_ponedoras_galpon (id) ON DELETE RESTRICT,
    INDEX idx_ponedoras_postura_galpon (galpon_id, empresa_id),
    INDEX idx_ponedoras_postura_empresa (empresa_id),
    INDEX idx_ponedoras_postura_fecha (empresa_id, fecha_periodo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Producción de huevos por galpón, periodo y categoría';

-- -----------------------------------------------------------------------------
-- avicola_ponedoras_muerte: mortalidad en el galpón
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS avicola_ponedoras_muerte (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    galpon_id     BIGINT       NOT NULL,
    empresa_id    BIGINT       NOT NULL,
    fecha         DATE         NOT NULL,
    cantidad      INT          NOT NULL COMMENT 'Número de aves muertas',
    causa         VARCHAR(150) NULL,
    observaciones TEXT         NULL,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ponedoras_muerte_galpon FOREIGN KEY (galpon_id) REFERENCES avicola_ponedoras_galpon (id) ON DELETE RESTRICT,
    INDEX idx_ponedoras_muerte_galpon (galpon_id, empresa_id),
    INDEX idx_ponedoras_muerte_empresa (empresa_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Mortalidad por galpón';

-- -----------------------------------------------------------------------------
-- avicola_ponedoras_consumo: consumo de alimento (insumo CORE cultivo_insumos)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS avicola_ponedoras_consumo (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    galpon_id     BIGINT        NOT NULL,
    empresa_id    BIGINT        NOT NULL,
    insumo_id     BIGINT        NOT NULL COMMENT 'Insumo compartido (CORE cultivo_insumos)',
    fecha         DATE          NOT NULL,
    cantidad      DECIMAL(12,3) NOT NULL COMMENT 'Cantidad en unidad del insumo',
    tipo          VARCHAR(20)   NOT NULL DEFAULT 'MANUAL' COMMENT 'MANUAL | AUTOMATICO',
    observaciones TEXT          NULL,
    created_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ponedoras_consumo_galpon FOREIGN KEY (galpon_id) REFERENCES avicola_ponedoras_galpon (id) ON DELETE RESTRICT,
    CONSTRAINT fk_ponedoras_consumo_insumo FOREIGN KEY (insumo_id) REFERENCES cultivo_insumos (id) ON DELETE RESTRICT,
    INDEX idx_ponedoras_consumo_galpon (galpon_id, empresa_id),
    INDEX idx_ponedoras_consumo_empresa (empresa_id),
    INDEX idx_ponedoras_consumo_insumo (insumo_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Consumos de alimento; movimiento de stock vía servicio con origen AVICOLA_PONEDORAS';

-- -----------------------------------------------------------------------------
-- avicola_ponedoras_evento_sanitario
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS avicola_ponedoras_evento_sanitario (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    galpon_id     BIGINT        NOT NULL,
    empresa_id    BIGINT        NOT NULL,
    fecha         DATE          NOT NULL,
    tipo          VARCHAR(80)   NOT NULL COMMENT 'VACUNACION | TRATAMIENTO | DIAGNOSTICO | OTRO',
    descripcion   TEXT          NULL,
    insumo_id     BIGINT        NULL COMMENT 'Medicamento u insumo CORE si aplica',
    dosis         DECIMAL(10,3) NULL,
    observaciones TEXT          NULL,
    created_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ponedoras_sanitario_galpon FOREIGN KEY (galpon_id) REFERENCES avicola_ponedoras_galpon (id) ON DELETE RESTRICT,
    CONSTRAINT fk_ponedoras_sanitario_insumo FOREIGN KEY (insumo_id) REFERENCES cultivo_insumos (id) ON DELETE SET NULL,
    INDEX idx_ponedoras_sanitario_galpon (galpon_id, empresa_id),
    INDEX idx_ponedoras_sanitario_empresa (empresa_id),
    INDEX idx_ponedoras_sanitario_insumo (insumo_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Eventos sanitarios del galpón';

-- -----------------------------------------------------------------------------
-- avicola_ponedoras_venta_huevos
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS avicola_ponedoras_venta_huevos (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    galpon_id        BIGINT        NOT NULL COMMENT 'Galpón origen (trazabilidad)',
    empresa_id       BIGINT        NOT NULL,
    fecha            DATE          NOT NULL,
    cantidad_huevos  INT           NOT NULL COMMENT 'Cantidad de huevos vendidos (unidad física acordada en negocio)',
    precio_unitario  DECIMAL(12,4) NULL,
    total            DECIMAL(14,2) NULL,
    comprador        VARCHAR(180)  NULL,
    observaciones    TEXT          NULL,
    created_at       TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ponedoras_venta_huevos_galpon FOREIGN KEY (galpon_id) REFERENCES avicola_ponedoras_galpon (id) ON DELETE RESTRICT,
    INDEX idx_ponedoras_venta_huevos_galpon (galpon_id, empresa_id),
    INDEX idx_ponedoras_venta_huevos_empresa (empresa_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Ventas de huevos asociadas al galpón';

-- -----------------------------------------------------------------------------
-- avicola_ponedoras_descarte_aves: liquidación del galpón (baja de aves)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS avicola_ponedoras_descarte_aves (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    galpon_id     BIGINT       NOT NULL,
    empresa_id    BIGINT       NOT NULL,
    fecha         DATE         NOT NULL,
    cantidad      INT          NOT NULL COMMENT 'Aves descartadas o liquidadas',
    motivo        VARCHAR(200) NOT NULL COMMENT 'Fin de ciclo, faena, venta pie, etc.',
    observaciones TEXT         NULL,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ponedoras_descarte_galpon FOREIGN KEY (galpon_id) REFERENCES avicola_ponedoras_galpon (id) ON DELETE RESTRICT,
    INDEX idx_ponedoras_descarte_galpon (galpon_id, empresa_id),
    INDEX idx_ponedoras_descarte_empresa (empresa_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Descarte o liquidación de aves al cierre de galpón';

-- -----------------------------------------------------------------------------
-- Módulo deshabilitado por defecto en company_modules (tabla real: company_modules)
-- Tras V1_140 puede existir fila con enabled = TRUE: se fuerza a FALSE.
-- -----------------------------------------------------------------------------
UPDATE company_modules cm
INNER JOIN modules m ON cm.module_id = m.id AND m.code = 'AVICOLA_PONEDORAS'
SET cm.enabled = FALSE;

INSERT INTO company_modules (company_id, module_id, enabled)
SELECT e.id, m.id, FALSE
FROM empresas e
INNER JOIN modules m ON m.code = 'AVICOLA_PONEDORAS'
WHERE NOT EXISTS (
    SELECT 1 FROM company_modules cm2
    WHERE cm2.company_id = e.id AND cm2.module_id = m.id
);
