-- =============================================================================
-- Módulo Avícola — esquema inicial (SPEC docs/specs/SPEC-MODULO-AVICOLA.md)
-- Idempotente: CREATE TABLE IF NOT EXISTS + INSERT módulo con ON DUPLICATE KEY
-- =============================================================================

-- -----------------------------------------------------------------------------
-- avicola_establecimiento: explotaciones avícolas por empresa (independiente de cultivos)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS avicola_establecimiento (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id    BIGINT       NOT NULL COMMENT 'Empresa propietaria',
    nombre        VARCHAR(150) NOT NULL COMMENT 'Nombre de la explotación o galpón',
    observaciones TEXT         NULL,
    activo        BOOLEAN      NOT NULL DEFAULT TRUE COMMENT 'Permite ocultar establecimientos sin borrar historial',
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP    NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_avicola_establecimiento_empresa (empresa_id),
    INDEX idx_avicola_establecimiento_activo (empresa_id, activo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Establecimientos del módulo avícola por empresa';

-- -----------------------------------------------------------------------------
-- avicola_raza: catálogo de razas o líneas genéticas configurable por empresa
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS avicola_raza (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id    BIGINT       NOT NULL COMMENT 'Empresa propietaria del catálogo',
    nombre        VARCHAR(120) NOT NULL COMMENT 'Nombre de la raza o línea',
    activo        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP    NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_avicola_raza_empresa (empresa_id),
    INDEX idx_avicola_raza_activo (empresa_id, activo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Razas o líneas avícolas configurables por empresa';

-- -----------------------------------------------------------------------------
-- avicola_lote: lote productivo de aves
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS avicola_lote (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id              BIGINT       NOT NULL COMMENT 'Empresa propietaria (multiempresa)',
    establecimiento_id      BIGINT       NOT NULL COMMENT 'FK a explotación avícola',
    raza_id                 BIGINT       NOT NULL COMMENT 'FK al catálogo de razas de la empresa',
    nombre                  VARCHAR(100) NOT NULL COMMENT 'Nombre o código del lote',
    especie                 VARCHAR(50)  NOT NULL COMMENT 'Clasificación comercial (enum aplicación)',
    origen                  VARCHAR(30)  NOT NULL DEFAULT 'EXTERNO' COMMENT 'EXTERNO | PROPIO',
    fecha_ingreso           DATE         NOT NULL COMMENT 'Fecha de ingreso del lote',
    cantidad_inicial        INT          NOT NULL COMMENT 'Aves al inicio del ciclo',
    cantidad_animales       INT          NOT NULL COMMENT 'Stock según ventas; muertes no restan aquí',
    peso_promedio_ingreso   DECIMAL(8,3) NULL COMMENT 'Peso promedio al ingreso (kg)',
    estado                  VARCHAR(30)  NOT NULL DEFAULT 'ACTIVO' COMMENT 'ACTIVO | CERRADO',
    fecha_salida            DATE         NULL COMMENT 'Fecha de cierre del lote',
    observaciones           TEXT         NULL,
    created_at              TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP    NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_avicola_lote_establecimiento FOREIGN KEY (establecimiento_id) REFERENCES avicola_establecimiento (id) ON DELETE RESTRICT,
    CONSTRAINT fk_avicola_lote_raza FOREIGN KEY (raza_id) REFERENCES avicola_raza (id) ON DELETE RESTRICT,
    INDEX idx_avicola_lote_empresa (empresa_id),
    INDEX idx_avicola_lote_estado (empresa_id, estado),
    INDEX idx_avicola_lote_establecimiento (establecimiento_id),
    INDEX idx_avicola_lote_raza (raza_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Lotes de aves: ciclo productivo y estado';

-- -----------------------------------------------------------------------------
-- avicola_pesada: pesadas para seguimiento de ganancia de peso
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS avicola_pesada (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    lote_id          BIGINT       NOT NULL COMMENT 'Lote pesado',
    empresa_id       BIGINT       NOT NULL COMMENT 'Empresa (índices y seguridad)',
    fecha            DATE         NOT NULL COMMENT 'Fecha de la pesada',
    peso_promedio    DECIMAL(8,3) NOT NULL COMMENT 'Peso promedio en kg',
    cantidad_pesada  INT          NULL COMMENT 'Aves incluidas en la muestra, si aplica',
    observaciones    TEXT         NULL,
    created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_avicola_pesada_lote FOREIGN KEY (lote_id) REFERENCES avicola_lote (id) ON DELETE RESTRICT,
    INDEX idx_avicola_pesada_lote (lote_id, empresa_id),
    INDEX idx_avicola_pesada_empresa (empresa_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Historial de pesadas por lote';

-- -----------------------------------------------------------------------------
-- avicola_muerte: mortalidad (no descuenta cantidad_animales en lote)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS avicola_muerte (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    lote_id       BIGINT    NOT NULL COMMENT 'Lote afectado',
    empresa_id    BIGINT    NOT NULL COMMENT 'Empresa',
    fecha         DATE      NOT NULL COMMENT 'Fecha del hecho',
    cantidad      INT       NOT NULL COMMENT 'Número de muertes',
    causa         VARCHAR(100) NULL COMMENT 'Causa declarada',
    observaciones TEXT      NULL,
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_avicola_muerte_lote FOREIGN KEY (lote_id) REFERENCES avicola_lote (id) ON DELETE RESTRICT,
    INDEX idx_avicola_muerte_lote (lote_id, empresa_id),
    INDEX idx_avicola_muerte_empresa (empresa_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Mortalidad por lote';

-- -----------------------------------------------------------------------------
-- avicola_venta: ventas y faena (descuentan cantidad_animales en aplicación)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS avicola_venta (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    lote_id          BIGINT        NOT NULL COMMENT 'Lote origen',
    empresa_id       BIGINT        NOT NULL COMMENT 'Empresa',
    fecha            DATE          NOT NULL COMMENT 'Fecha de venta o faena',
    tipo             VARCHAR(30)   NOT NULL COMMENT 'FAENA | VENTA_EN_PIE | DESCARTE',
    cantidad         INT           NOT NULL COMMENT 'Aves vendidas o faenadas',
    peso_promedio    DECIMAL(8,3)  NULL COMMENT 'Peso promedio kg',
    precio_unitario  DECIMAL(12,2) NULL,
    total            DECIMAL(14,2) NULL COMMENT 'Importe total de la operación',
    comprador        VARCHAR(150)  NULL,
    observaciones    TEXT          NULL,
    ingreso_id       BIGINT        NULL COMMENT 'ID en cultivo_ingresos si se generó ingreso (CORE)',
    created_at       TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_avicola_venta_lote FOREIGN KEY (lote_id) REFERENCES avicola_lote (id) ON DELETE RESTRICT,
    INDEX idx_avicola_venta_lote (lote_id, empresa_id),
    INDEX idx_avicola_venta_empresa (empresa_id),
    INDEX idx_avicola_venta_ingreso (ingreso_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Ventas y faena; vínculo opcional a ingreso económico';

-- -----------------------------------------------------------------------------
-- avicola_consumo: consumo de insumo (CORE); movimiento de inventario vía servicio
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS avicola_consumo (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    lote_id       BIGINT        NOT NULL COMMENT 'Lote consumidor',
    empresa_id    BIGINT        NOT NULL COMMENT 'Empresa',
    insumo_id     BIGINT        NOT NULL COMMENT 'Insumo compartido (tabla cultivo_insumos, sin FK)',
    fecha         DATE          NOT NULL COMMENT 'Día del consumo',
    cantidad      DECIMAL(12,3) NOT NULL COMMENT 'Cantidad consumida (unidad del insumo)',
    tipo          VARCHAR(20)   NOT NULL DEFAULT 'MANUAL' COMMENT 'MANUAL | AUTOMATICO',
    observaciones TEXT          NULL,
    created_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_avicola_consumo_lote FOREIGN KEY (lote_id) REFERENCES avicola_lote (id) ON DELETE RESTRICT,
    INDEX idx_avicola_consumo_lote (lote_id, empresa_id),
    INDEX idx_avicola_consumo_empresa (empresa_id),
    INDEX idx_avicola_consumo_insumo (insumo_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Consumos de alimento registrados por lote';

-- -----------------------------------------------------------------------------
-- avicola_evento_sanitario: sanidad del lote
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS avicola_evento_sanitario (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    lote_id       BIGINT        NOT NULL COMMENT 'Lote afectado',
    empresa_id    BIGINT        NOT NULL COMMENT 'Empresa',
    fecha         DATE          NOT NULL COMMENT 'Fecha del evento',
    tipo          VARCHAR(80)   NOT NULL COMMENT 'VACUNACION | TRATAMIENTO | DIAGNOSTICO | OTRO',
    descripcion   TEXT          NULL,
    insumo_id     BIGINT        NULL COMMENT 'Medicamento u insumo asociado (sin FK al CORE)',
    dosis         DECIMAL(10,3) NULL,
    observaciones TEXT          NULL,
    created_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_avicola_sanitario_lote FOREIGN KEY (lote_id) REFERENCES avicola_lote (id) ON DELETE RESTRICT,
    INDEX idx_avicola_sanitario_lote (lote_id, empresa_id),
    INDEX idx_avicola_sanitario_empresa (empresa_id),
    INDEX idx_avicola_sanitario_insumo (insumo_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Eventos sanitarios del lote';

-- -----------------------------------------------------------------------------
-- Catálogo de módulos: código heredado AVICOLA (V1_137 renombra a AVICOLA_CRIANZA y agrega AVICOLA_HUEVOS)
-- -----------------------------------------------------------------------------
INSERT INTO modules (name, code, description, active)
VALUES (
    'Avícola',
    'AVICOLA',
    'Módulo de gestión avícola: lotes, pesadas, mortalidad, ventas, consumos y sanidad.',
    TRUE
)
ON DUPLICATE KEY UPDATE
    name        = VALUES(name),
    description = VALUES(description),
    active      = VALUES(active);
