-- Composición de razas del lote avícola (sin cantidad por raza).
-- La cantidad sigue en el lote. raza_id histórico se conserva y se copia a la composición.

CREATE TABLE IF NOT EXISTS avicola_lote_raza (
    lote_id BIGINT NOT NULL,
    raza_id BIGINT NOT NULL,
    PRIMARY KEY (lote_id, raza_id),
    CONSTRAINT fk_avicola_lote_raza_lote FOREIGN KEY (lote_id) REFERENCES avicola_lote (id),
    CONSTRAINT fk_avicola_lote_raza_raza FOREIGN KEY (raza_id) REFERENCES avicola_raza (id)
);

INSERT INTO avicola_lote_raza (lote_id, raza_id)
SELECT l.id, l.raza_id
FROM avicola_lote l
WHERE l.raza_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM avicola_lote_raza c WHERE c.lote_id = l.id AND c.raza_id = l.raza_id
  );

CREATE TABLE IF NOT EXISTS avicola_huevo_lote_raza (
    lote_id BIGINT NOT NULL,
    raza_id BIGINT NOT NULL,
    PRIMARY KEY (lote_id, raza_id),
    CONSTRAINT fk_avicola_huevo_lote_raza_lote FOREIGN KEY (lote_id) REFERENCES avicola_huevo_lote (id),
    CONSTRAINT fk_avicola_huevo_lote_raza_raza FOREIGN KEY (raza_id) REFERENCES avicola_huevo_raza (id)
);

INSERT INTO avicola_huevo_lote_raza (lote_id, raza_id)
SELECT l.id, l.raza_id
FROM avicola_huevo_lote l
WHERE l.raza_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM avicola_huevo_lote_raza c WHERE c.lote_id = l.id AND c.raza_id = l.raza_id
  );

CREATE TABLE IF NOT EXISTS avicola_ponedoras_raza (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id BIGINT NOT NULL,
    nombre VARCHAR(120) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_ponedoras_raza_empresa_nombre (empresa_id, nombre),
    INDEX idx_ponedoras_raza_empresa (empresa_id)
);

CREATE TABLE IF NOT EXISTS avicola_ponedoras_galpon_raza (
    galpon_id BIGINT NOT NULL,
    raza_id BIGINT NOT NULL,
    PRIMARY KEY (galpon_id, raza_id),
    CONSTRAINT fk_ponedoras_galpon_raza_galpon FOREIGN KEY (galpon_id) REFERENCES avicola_ponedoras_galpon (id),
    CONSTRAINT fk_ponedoras_galpon_raza_raza FOREIGN KEY (raza_id) REFERENCES avicola_ponedoras_raza (id)
);

INSERT INTO avicola_ponedoras_raza (empresa_id, nombre, activo)
SELECT g.empresa_id, MIN(TRIM(g.raza)), TRUE
FROM avicola_ponedoras_galpon g
WHERE TRIM(g.raza) <> ''
GROUP BY g.empresa_id, LOWER(TRIM(g.raza));

INSERT INTO avicola_ponedoras_galpon_raza (galpon_id, raza_id)
SELECT g.id, r.id
FROM avicola_ponedoras_galpon g
INNER JOIN avicola_ponedoras_raza r
    ON r.empresa_id = g.empresa_id
   AND LOWER(r.nombre) = LOWER(TRIM(g.raza))
WHERE TRIM(g.raza) <> ''
  AND NOT EXISTS (
      SELECT 1 FROM avicola_ponedoras_galpon_raza c WHERE c.galpon_id = g.id AND c.raza_id = r.id
  );

UPDATE avicola_ponedoras_galpon g
INNER JOIN avicola_ponedoras_galpon_raza gr ON gr.galpon_id = g.id
INNER JOIN avicola_ponedoras_raza r ON r.id = gr.raza_id
SET g.raza = r.nombre
WHERE TRIM(g.raza) <> '';
