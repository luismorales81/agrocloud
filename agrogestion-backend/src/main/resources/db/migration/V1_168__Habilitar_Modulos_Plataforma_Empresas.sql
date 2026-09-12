-- ============================================================================
-- V1_168: Habilitar módulos de plataforma para todas las empresas
-- Corrige selector de módulos que solo muestra Cultivos y Porcinos cuando
-- company_modules tiene enabled=FALSE o faltan filas (feedlot, avícola, lechería).
-- Idempotente.
-- ============================================================================

-- Catálogo canónico (modules)
INSERT INTO modules (name, code, description, active)
SELECT 'Cultivos', 'CULTIVOS', 'Gestión de campos, lotes, cultivos y labores', TRUE
WHERE NOT EXISTS (SELECT 1 FROM modules WHERE code = 'CULTIVOS');

INSERT INTO modules (name, code, description, active)
SELECT 'Porcinos', 'PORCINOS', 'Gestión porcina v2', TRUE
WHERE NOT EXISTS (SELECT 1 FROM modules WHERE code = 'PORCINOS');

INSERT INTO modules (name, code, description, active)
SELECT 'Avícola crianza', 'AVICOLA_CRIANZA',
       'Parrilleros y crianza: lotes, pesadas, mortalidad, ventas, consumos y sanidad.', TRUE
WHERE NOT EXISTS (SELECT 1 FROM modules WHERE code = 'AVICOLA_CRIANZA');

INSERT INTO modules (name, code, description, active)
SELECT 'Avícola huevos', 'AVICOLA_HUEVOS',
       'Postura: lotes de puesta, producción diaria, consumos y sanidad.', TRUE
WHERE NOT EXISTS (SELECT 1 FROM modules WHERE code = 'AVICOLA_HUEVOS');

INSERT INTO modules (name, code, description, active)
SELECT 'Avícola ponedoras', 'AVICOLA_PONEDORAS',
       'Ponedoras y recría (módulo independiente de carne y huevos).', TRUE
WHERE NOT EXISTS (SELECT 1 FROM modules WHERE code = 'AVICOLA_PONEDORAS');

INSERT INTO modules (name, code, description, active)
SELECT 'Engorde a corral (Feedlot)', 'FEEDLOT',
       'Engorde bovino a corral: lotes, pesadas, alimento, sanidad, faena y closeout.', TRUE
WHERE NOT EXISTS (SELECT 1 FROM modules WHERE code = 'FEEDLOT');

INSERT INTO modules (name, code, description, active)
SELECT 'Lechería', 'LECHERIA',
       'Gestión lechera multi-especie: animales, lactancia, ordeñe, reproducción, sanidad y ventas.', TRUE
WHERE NOT EXISTS (SELECT 1 FROM modules WHERE code = 'LECHERIA');

UPDATE modules SET active = TRUE
WHERE code IN (
    'CULTIVOS', 'CROPS', 'PORCINOS', 'pigs',
    'AVICOLA_CRIANZA', 'AVICOLA_HUEVOS', 'AVICOLA_PONEDORAS', 'AVICOLA_CARNE',
    'FEEDLOT', 'LECHERIA'
);

-- Filas faltantes por empresa
INSERT INTO company_modules (company_id, module_id, enabled)
SELECT e.id, m.id, TRUE
FROM empresas e
INNER JOIN modules m ON m.code IN (
    'CULTIVOS', 'PORCINOS', 'AVICOLA_CRIANZA', 'AVICOLA_HUEVOS',
    'AVICOLA_PONEDORAS', 'FEEDLOT', 'LECHERIA'
)
WHERE NOT EXISTS (
    SELECT 1 FROM company_modules cm
    WHERE cm.company_id = e.id AND cm.module_id = m.id
);

-- Activar módulos de plataforma ya existentes (p. ej. ponedoras/lechería en FALSE)
UPDATE company_modules cm
INNER JOIN modules m ON m.id = cm.module_id
SET cm.enabled = TRUE
WHERE m.code IN (
    'CULTIVOS', 'CROPS', 'PORCINOS', 'pigs',
    'AVICOLA_CRIANZA', 'AVICOLA_HUEVOS', 'AVICOLA_PONEDORAS', 'AVICOLA_CARNE',
    'FEEDLOT', 'LECHERIA'
);

SELECT 'V1_168: módulos de plataforma habilitados por empresa' AS mensaje;
