-- ============================================================================
-- MIGRACIÓN: Esquema v2 del módulo Porcinos
-- Versión: V1_163
-- SPEC: SPEC-MODULO-PORCINOS-V2.md / DISENO-TECNICO-MODULO-PORCINOS-V2.md
-- ============================================================================

-- --------------------------------------------------------------------------
-- Establecimiento y galpones
-- --------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS porcinos_establecimiento (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id          BIGINT       NOT NULL,
    nombre              VARCHAR(200) NOT NULL,
    ubicacion           VARCHAR(255) NULL,
    coordenadas         TEXT         NULL COMMENT 'JSON [{lat,lng}]',
    dias_gestacion      INT          NOT NULL DEFAULT 114,
    dias_lactancia      INT          NOT NULL DEFAULT 21,
    dias_entre_celos    INT          NOT NULL DEFAULT 21,
    faena_habilitada    BOOLEAN      NOT NULL DEFAULT TRUE,
    capacidad_cabezas   INT          NULL,
    activo              BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP    NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_porcinos_est_empresa FOREIGN KEY (empresa_id) REFERENCES empresas (id) ON DELETE RESTRICT,
    UNIQUE KEY uk_porcinos_est_empresa (empresa_id),
    INDEX idx_porcinos_est_empresa (empresa_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS porcinos_galpon (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    establecimiento_id  BIGINT       NOT NULL,
    nombre              VARCHAR(120) NOT NULL,
    capacidad_cabezas   INT          NULL,
    estado              VARCHAR(20)  NOT NULL DEFAULT 'DISPONIBLE' COMMENT 'DISPONIBLE|OCUPADO|INACTIVO',
    activo              BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP    NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_porcinos_galpon_est FOREIGN KEY (establecimiento_id) REFERENCES porcinos_establecimiento (id) ON DELETE RESTRICT,
    INDEX idx_porcinos_galpon_est (establecimiento_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------------------------
-- Catálogos
-- --------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS porcinos_raza (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id  BIGINT       NOT NULL,
    nombre      VARCHAR(120) NOT NULL,
    tipo        VARCHAR(20)  NULL COMMENT 'MADRE|PADRILLO|HIBRIDO',
    activo      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_porcinos_raza_empresa FOREIGN KEY (empresa_id) REFERENCES empresas (id) ON DELETE RESTRICT,
    UNIQUE KEY uk_porcinos_raza_empresa_nombre (empresa_id, nombre),
    INDEX idx_porcinos_raza_empresa (empresa_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS porcinos_motivo_baja (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id  BIGINT       NOT NULL,
    nombre      VARCHAR(120) NOT NULL,
    activo      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_porcinos_motivo_baja_empresa FOREIGN KEY (empresa_id) REFERENCES empresas (id) ON DELETE RESTRICT,
    UNIQUE KEY uk_porcinos_motivo_baja_empresa_nombre (empresa_id, nombre),
    INDEX idx_porcinos_motivo_baja_empresa (empresa_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS porcinos_causa_mortalidad (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id  BIGINT       NOT NULL,
    nombre      VARCHAR(120) NOT NULL,
    etapa       VARCHAR(20)  NULL COMMENT 'LACTANCIA|RECRIA|ENGORDE|GESTACION|GENERAL',
    activo      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_porcinos_causa_mort_empresa FOREIGN KEY (empresa_id) REFERENCES empresas (id) ON DELETE RESTRICT,
    INDEX idx_porcinos_causa_mort_empresa (empresa_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS porcinos_tipo_servicio (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id  BIGINT       NOT NULL,
    nombre      VARCHAR(120) NOT NULL,
    activo      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_porcinos_tipo_serv_empresa FOREIGN KEY (empresa_id) REFERENCES empresas (id) ON DELETE RESTRICT,
    UNIQUE KEY uk_porcinos_tipo_serv_empresa_nombre (empresa_id, nombre),
    INDEX idx_porcinos_tipo_serv_empresa (empresa_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------------------------
-- Reproducción
-- --------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS porcinos_madre (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id          BIGINT       NOT NULL,
    caravana            VARCHAR(100) NOT NULL,
    raza_id             BIGINT       NULL,
    galpon_id           BIGINT       NULL,
    estado              VARCHAR(20)  NOT NULL DEFAULT 'CACHORRA' COMMENT 'CACHORRA|ADULTA|GESTACION|LACTANCIA|BAJA',
    fecha_ingreso       DATE         NOT NULL,
    fecha_nacimiento    DATE         NULL,
    activo              BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP    NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_porcinos_madre_empresa FOREIGN KEY (empresa_id) REFERENCES empresas (id) ON DELETE RESTRICT,
    CONSTRAINT fk_porcinos_madre_raza FOREIGN KEY (raza_id) REFERENCES porcinos_raza (id) ON DELETE SET NULL,
    CONSTRAINT fk_porcinos_madre_galpon FOREIGN KEY (galpon_id) REFERENCES porcinos_galpon (id) ON DELETE SET NULL,
    UNIQUE KEY uk_porcinos_madre_caravana (empresa_id, caravana),
    INDEX idx_porcinos_madre_empresa (empresa_id),
    INDEX idx_porcinos_madre_estado (empresa_id, estado)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS porcinos_padrillo (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id  BIGINT       NOT NULL,
    nombre      VARCHAR(100) NOT NULL,
    raza_id     BIGINT       NULL,
    activo      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_porcinos_padrillo_empresa FOREIGN KEY (empresa_id) REFERENCES empresas (id) ON DELETE RESTRICT,
    CONSTRAINT fk_porcinos_padrillo_raza FOREIGN KEY (raza_id) REFERENCES porcinos_raza (id) ON DELETE SET NULL,
    UNIQUE KEY uk_porcinos_padrillo_nombre (empresa_id, nombre),
    INDEX idx_porcinos_padrillo_empresa (empresa_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS porcinos_servicio (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    madre_id            BIGINT   NOT NULL,
    padrillo_id         BIGINT   NULL,
    tipo_servicio_id    BIGINT   NULL,
    fecha               DATE     NOT NULL,
    observaciones       TEXT     NULL,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_porcinos_servicio_madre FOREIGN KEY (madre_id) REFERENCES porcinos_madre (id) ON DELETE RESTRICT,
    CONSTRAINT fk_porcinos_servicio_padrillo FOREIGN KEY (padrillo_id) REFERENCES porcinos_padrillo (id) ON DELETE SET NULL,
    CONSTRAINT fk_porcinos_servicio_tipo FOREIGN KEY (tipo_servicio_id) REFERENCES porcinos_tipo_servicio (id) ON DELETE SET NULL,
    INDEX idx_porcinos_servicio_madre (madre_id, fecha)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS porcinos_gestacion (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    madre_id                BIGINT   NOT NULL,
    servicio_id             BIGINT   NULL,
    fecha_inicio            DATE     NOT NULL,
    fecha_probable_parto    DATE     NOT NULL,
    estado                  VARCHAR(20) NOT NULL DEFAULT 'EN_CURSO' COMMENT 'EN_CURSO|FINALIZADA|ABORTO',
    activo                  BOOLEAN  NOT NULL DEFAULT TRUE,
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_porcinos_gestacion_madre FOREIGN KEY (madre_id) REFERENCES porcinos_madre (id) ON DELETE RESTRICT,
    CONSTRAINT fk_porcinos_gestacion_servicio FOREIGN KEY (servicio_id) REFERENCES porcinos_servicio (id) ON DELETE SET NULL,
    INDEX idx_porcinos_gestacion_madre (madre_id, estado, activo),
    INDEX idx_porcinos_gestacion_fecha_parto (fecha_probable_parto)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS porcinos_parto (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    gestacion_id            BIGINT         NOT NULL,
    madre_id                BIGINT         NOT NULL,
    fecha                   DATE           NOT NULL,
    nacidos_vivos           INT            NOT NULL DEFAULT 0,
    nacidos_muertos         INT            NOT NULL DEFAULT 0,
    momificados             INT            NOT NULL DEFAULT 0,
    temperatura_ambiente    DECIMAL(5, 2)  NULL,
    humedad_ambiente        DECIMAL(5, 2)  NULL,
    observaciones           TEXT           NULL,
    created_at              TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP      NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_porcinos_parto_gestacion FOREIGN KEY (gestacion_id) REFERENCES porcinos_gestacion (id) ON DELETE RESTRICT,
    CONSTRAINT fk_porcinos_parto_madre FOREIGN KEY (madre_id) REFERENCES porcinos_madre (id) ON DELETE RESTRICT,
    INDEX idx_porcinos_parto_madre (madre_id, fecha),
    INDEX idx_porcinos_parto_gestacion (gestacion_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Lote se crea antes que destete para permitir FK circular (destete.lote_id / lote.destete_id)
CREATE TABLE IF NOT EXISTS porcinos_lote (
    id                          BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id                  BIGINT         NOT NULL,
    galpon_id                   BIGINT         NULL,
    campana_id                  BIGINT         NOT NULL,
    nombre                      VARCHAR(120)   NOT NULL,
    origen                      VARCHAR(20)    NOT NULL DEFAULT 'EXTERNO' COMMENT 'DESTETE|EXTERNO',
    destete_id                  BIGINT         NULL,
    fecha_ingreso               DATE           NOT NULL,
    fecha_cierre                DATE           NULL,
    cabezas_inicial             INT            NOT NULL,
    cabezas_actuales            INT            NOT NULL,
    peso_promedio_ingreso_kg    DECIMAL(10, 2) NOT NULL,
    etapa                       VARCHAR(20)    NOT NULL DEFAULT 'RECRIA' COMMENT 'RECRIA|ENGORDE',
    estado                      VARCHAR(20)    NOT NULL DEFAULT 'ACTIVO' COMMENT 'ACTIVO|CERRADO',
    observaciones               TEXT           NULL,
    created_at                  TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                  TIMESTAMP      NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_porcinos_lote_empresa FOREIGN KEY (empresa_id) REFERENCES empresas (id) ON DELETE RESTRICT,
    CONSTRAINT fk_porcinos_lote_galpon FOREIGN KEY (galpon_id) REFERENCES porcinos_galpon (id) ON DELETE SET NULL,
    CONSTRAINT fk_porcinos_lote_campana FOREIGN KEY (campana_id) REFERENCES core_campanas (id) ON DELETE RESTRICT,
    INDEX idx_porcinos_lote_empresa (empresa_id),
    INDEX idx_porcinos_lote_empresa_estado (empresa_id, estado),
    INDEX idx_porcinos_lote_campana (campana_id),
    INDEX idx_porcinos_lote_galpon (galpon_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS porcinos_destete (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    parto_id                BIGINT         NOT NULL,
    fecha                   DATE           NOT NULL,
    cantidad_destetados     INT            NOT NULL,
    peso_promedio_kg        DECIMAL(10, 2) NOT NULL,
    lote_id                 BIGINT         NULL,
    created_at              TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP      NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_porcinos_destete_parto FOREIGN KEY (parto_id) REFERENCES porcinos_parto (id) ON DELETE RESTRICT,
    CONSTRAINT fk_porcinos_destete_lote FOREIGN KEY (lote_id) REFERENCES porcinos_lote (id) ON DELETE SET NULL,
    UNIQUE KEY uk_porcinos_destete_parto (parto_id),
    INDEX idx_porcinos_destete_lote (lote_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET @esquema = DATABASE();
SET @consulta = (SELECT IF(
    EXISTS (SELECT 1 FROM information_schema.table_constraints
            WHERE constraint_schema = @esquema AND table_name = 'porcinos_lote' AND constraint_name = 'fk_porcinos_lote_destete'),
    'SELECT 1',
    'ALTER TABLE porcinos_lote ADD CONSTRAINT fk_porcinos_lote_destete FOREIGN KEY (destete_id) REFERENCES porcinos_destete (id) ON DELETE SET NULL'));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

-- --------------------------------------------------------------------------
-- Operaciones de lote
-- --------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS porcinos_pesada (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    lote_id                 BIGINT         NOT NULL,
    empresa_id              BIGINT         NOT NULL,
    fecha                   DATE           NOT NULL,
    peso_promedio_kg        DECIMAL(10, 2) NOT NULL,
    cabezas_muestreadas     INT            NULL,
    temperatura_ambiente    DECIMAL(5, 2)  NULL,
    humedad_ambiente        DECIMAL(5, 2)  NULL,
    observaciones           TEXT           NULL,
    created_at              TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_porcinos_pesada_lote FOREIGN KEY (lote_id) REFERENCES porcinos_lote (id) ON DELETE RESTRICT,
    CONSTRAINT fk_porcinos_pesada_empresa FOREIGN KEY (empresa_id) REFERENCES empresas (id) ON DELETE RESTRICT,
    INDEX idx_porcinos_pesada_lote (lote_id),
    INDEX idx_porcinos_pesada_lote_fecha (lote_id, fecha),
    INDEX idx_porcinos_pesada_empresa (empresa_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS porcinos_muerte (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    lote_id                 BIGINT   NOT NULL,
    empresa_id              BIGINT   NOT NULL,
    fecha                   DATE     NOT NULL,
    cabezas                 INT      NOT NULL,
    causa_mortalidad_id     BIGINT   NULL,
    observaciones           TEXT     NULL,
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_porcinos_muerte_lote FOREIGN KEY (lote_id) REFERENCES porcinos_lote (id) ON DELETE RESTRICT,
    CONSTRAINT fk_porcinos_muerte_empresa FOREIGN KEY (empresa_id) REFERENCES empresas (id) ON DELETE RESTRICT,
    CONSTRAINT fk_porcinos_muerte_causa FOREIGN KEY (causa_mortalidad_id) REFERENCES porcinos_causa_mortalidad (id) ON DELETE SET NULL,
    INDEX idx_porcinos_muerte_lote (lote_id),
    INDEX idx_porcinos_muerte_lote_fecha (lote_id, fecha),
    INDEX idx_porcinos_muerte_empresa (empresa_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS porcinos_consumo (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    lote_id                 BIGINT         NOT NULL,
    empresa_id              BIGINT         NOT NULL,
    campana_id              BIGINT         NOT NULL,
    insumo_id               BIGINT         NOT NULL,
    fecha                   DATE           NOT NULL,
    cantidad_kg             DECIMAL(12, 3) NOT NULL,
    temperatura_ambiente    DECIMAL(5, 2)  NULL,
    humedad_ambiente        DECIMAL(5, 2)  NULL,
    observaciones           TEXT           NULL,
    created_at              TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP      NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_porcinos_consumo_lote FOREIGN KEY (lote_id) REFERENCES porcinos_lote (id) ON DELETE RESTRICT,
    CONSTRAINT fk_porcinos_consumo_empresa FOREIGN KEY (empresa_id) REFERENCES empresas (id) ON DELETE RESTRICT,
    CONSTRAINT fk_porcinos_consumo_campana FOREIGN KEY (campana_id) REFERENCES core_campanas (id) ON DELETE RESTRICT,
    CONSTRAINT fk_porcinos_consumo_insumo FOREIGN KEY (insumo_id) REFERENCES cultivo_insumos (id) ON DELETE RESTRICT,
    INDEX idx_porcinos_consumo_lote (lote_id),
    INDEX idx_porcinos_consumo_lote_fecha (lote_id, fecha),
    INDEX idx_porcinos_consumo_empresa (empresa_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS porcinos_evento_sanitario (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    lote_id         BIGINT         NOT NULL,
    empresa_id      BIGINT         NOT NULL,
    fecha           DATE           NOT NULL,
    tipo            VARCHAR(30)    NOT NULL,
    descripcion     TEXT           NULL,
    insumo_id       BIGINT         NULL,
    cantidad        DECIMAL(12, 3) NULL,
    dias_retiro     INT            NULL,
    observaciones   TEXT           NULL,
    created_at      TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_porcinos_san_lote FOREIGN KEY (lote_id) REFERENCES porcinos_lote (id) ON DELETE RESTRICT,
    CONSTRAINT fk_porcinos_san_empresa FOREIGN KEY (empresa_id) REFERENCES empresas (id) ON DELETE RESTRICT,
    CONSTRAINT fk_porcinos_san_insumo FOREIGN KEY (insumo_id) REFERENCES cultivo_insumos (id) ON DELETE SET NULL,
    INDEX idx_porcinos_san_lote (lote_id),
    INDEX idx_porcinos_san_lote_fecha (lote_id, fecha),
    INDEX idx_porcinos_san_empresa (empresa_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS porcinos_venta (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    lote_id             BIGINT         NOT NULL,
    empresa_id          BIGINT         NOT NULL,
    campana_id          BIGINT         NOT NULL,
    fecha               DATE           NOT NULL,
    tipo                VARCHAR(20)    NOT NULL COMMENT 'ENGORDE|REPRODUCTOR|FAENA',
    cabezas             INT            NOT NULL,
    peso_promedio_kg    DECIMAL(10, 2) NULL,
    precio_kg           DECIMAL(12, 2) NULL,
    total               DECIMAL(14, 2) NULL,
    comprador           VARCHAR(200)   NULL,
    observaciones       TEXT           NULL,
    created_at          TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP      NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_porcinos_venta_lote FOREIGN KEY (lote_id) REFERENCES porcinos_lote (id) ON DELETE RESTRICT,
    CONSTRAINT fk_porcinos_venta_empresa FOREIGN KEY (empresa_id) REFERENCES empresas (id) ON DELETE RESTRICT,
    CONSTRAINT fk_porcinos_venta_campana FOREIGN KEY (campana_id) REFERENCES core_campanas (id) ON DELETE RESTRICT,
    INDEX idx_porcinos_venta_lote (lote_id),
    INDEX idx_porcinos_venta_lote_fecha (lote_id, fecha),
    INDEX idx_porcinos_venta_empresa (empresa_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------------------------
-- Dietas
-- --------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS porcinos_dieta (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id  BIGINT       NOT NULL,
    nombre      VARCHAR(150) NOT NULL,
    activo      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_porcinos_dieta_empresa FOREIGN KEY (empresa_id) REFERENCES empresas (id) ON DELETE RESTRICT,
    UNIQUE KEY uk_porcinos_dieta_empresa_nombre (empresa_id, nombre),
    INDEX idx_porcinos_dieta_empresa (empresa_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS porcinos_dieta_fase (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    dieta_id            BIGINT         NOT NULL,
    nombre_fase         VARCHAR(120)   NOT NULL,
    dias_desde_ingreso  INT            NOT NULL DEFAULT 0,
    kg_cabeza_dia       DECIMAL(10, 3) NOT NULL,
    insumo_id           BIGINT         NULL,
    created_at          TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP      NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_porcinos_dieta_fase_dieta FOREIGN KEY (dieta_id) REFERENCES porcinos_dieta (id) ON DELETE CASCADE,
    CONSTRAINT fk_porcinos_dieta_fase_insumo FOREIGN KEY (insumo_id) REFERENCES cultivo_insumos (id) ON DELETE SET NULL,
    INDEX idx_porcinos_dieta_fase_dieta (dieta_id, dias_desde_ingreso)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
