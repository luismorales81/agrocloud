CREATE TABLE IF NOT EXISTS feedlot_perfil_lectura_manga (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id         BIGINT        NOT NULL,
    nombre             VARCHAR(120)  NOT NULL,
    tipo               VARCHAR(20)   NOT NULL,
    predeterminado     BOOLEAN       NOT NULL DEFAULT FALSE,
    mapeo_alias_json   TEXT          NOT NULL,
    insumo_id          BIGINT        NULL,
    created_at         TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP     NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_feedlot_perfil_lectura_nombre (empresa_id, nombre),
    INDEX idx_feedlot_perfil_lectura_tipo (empresa_id, tipo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

ALTER TABLE feedlot_sesion_manga
    ADD COLUMN perfil_lectura_id BIGINT NULL AFTER perfil_mapeo_json;
