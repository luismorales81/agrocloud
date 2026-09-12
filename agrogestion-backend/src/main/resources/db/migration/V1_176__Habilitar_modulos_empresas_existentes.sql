-- Habilita módulos de plataforma para empresas creadas después de V1_168
-- (p. ej. Empresa local Docker). Idempotente.

INSERT INTO company_modules (company_id, module_id, enabled)
SELECT e.id, m.id, TRUE
FROM empresas e
INNER JOIN modules m ON m.code IN (
    'CULTIVOS', 'PORCINOS', 'AVICOLA_CRIANZA', 'AVICOLA_HUEVOS',
    'FEEDLOT', 'LECHERIA'
)
WHERE NOT EXISTS (
    SELECT 1 FROM company_modules cm
    WHERE cm.company_id = e.id AND cm.module_id = m.id
);

UPDATE company_modules cm
INNER JOIN modules m ON m.id = cm.module_id
SET cm.enabled = TRUE
WHERE m.code IN (
    'CULTIVOS', 'CROPS', 'PORCINOS', 'pigs',
    'AVICOLA_CRIANZA', 'AVICOLA_HUEVOS', 'AVICOLA_CARNE',
    'FEEDLOT', 'LECHERIA'
);
