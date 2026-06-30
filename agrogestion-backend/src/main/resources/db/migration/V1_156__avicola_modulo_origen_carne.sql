-- Aislamiento de datos entre módulos avícola crianza y carne (SPEC v1.1).

ALTER TABLE avicola_establecimiento
    ADD COLUMN modulo_origen VARCHAR(40) NOT NULL DEFAULT 'AVICOLA_CRIANZA' COMMENT 'AVICOLA_CRIANZA | AVICOLA_CARNE';

ALTER TABLE avicola_raza
    ADD COLUMN modulo_origen VARCHAR(40) NOT NULL DEFAULT 'AVICOLA_CRIANZA' COMMENT 'AVICOLA_CRIANZA | AVICOLA_CARNE';

ALTER TABLE avicola_lote
    ADD COLUMN modulo_origen VARCHAR(40) NOT NULL DEFAULT 'AVICOLA_CRIANZA' COMMENT 'AVICOLA_CRIANZA | AVICOLA_CARNE';

CREATE INDEX idx_avicola_est_modulo ON avicola_establecimiento (empresa_id, modulo_origen);
CREATE INDEX idx_avicola_raza_modulo ON avicola_raza (empresa_id, modulo_origen);
CREATE INDEX idx_avicola_lote_modulo ON avicola_lote (empresa_id, modulo_origen, estado);
