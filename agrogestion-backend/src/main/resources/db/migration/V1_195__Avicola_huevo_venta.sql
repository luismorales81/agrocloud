CREATE TABLE IF NOT EXISTS avicola_huevo_venta (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    lote_id BIGINT NOT NULL,
    empresa_id BIGINT NOT NULL,
    fecha DATE NOT NULL,
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(12, 4) NULL,
    total DECIMAL(14, 2) NULL,
    comprador VARCHAR(180) NULL,
    observaciones TEXT NULL,
    campana_id BIGINT NULL,
    ingreso_id BIGINT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_avicola_huevo_venta_lote (lote_id, empresa_id),
    CONSTRAINT fk_avicola_huevo_venta_lote FOREIGN KEY (lote_id) REFERENCES avicola_huevo_lote (id)
);
