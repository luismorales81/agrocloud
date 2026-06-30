-- Ubicación y clima: pesadas crianza, ambiente diario ponedoras, coordenadas feedlot, clima lectura comedero

ALTER TABLE avicola_pesada
    ADD COLUMN temperatura_ambiente DECIMAL(5, 2) NULL,
    ADD COLUMN humedad_ambiente DECIMAL(6, 2) NULL;

CREATE TABLE IF NOT EXISTS avicola_ponedoras_ambiente_diario (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    galpon_id BIGINT NOT NULL,
    empresa_id BIGINT NOT NULL,
    fecha DATE NOT NULL,
    temperatura_dia DECIMAL(5, 2) NULL,
    humedad_dia DECIMAL(6, 2) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_ponedoras_ambiente_galpon FOREIGN KEY (galpon_id) REFERENCES avicola_ponedoras_galpon (id),
    CONSTRAINT uk_ponedoras_ambiente_galpon_fecha UNIQUE (galpon_id, fecha)
);

CREATE INDEX idx_ponedoras_ambiente_empresa_fecha ON avicola_ponedoras_ambiente_diario (empresa_id, fecha);

ALTER TABLE feedlot_establecimiento
    ADD COLUMN coordenadas TEXT NULL;

ALTER TABLE feedlot_lectura_comedero
    ADD COLUMN temperatura_dia DECIMAL(5, 2) NULL,
    ADD COLUMN humedad_dia DECIMAL(6, 2) NULL;
