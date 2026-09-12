-- Habilita el módulo Porcinos (code: pigs) para todas las empresas.
-- Corrige 402 MODULE_NOT_ENABLED en /api/v1/porcinos/planes-recria y endpoints con @RequiresModule("pigs").
-- Idempotente.

INSERT INTO modules (name, code, description, active)
SELECT 'Porcinos', 'pigs', 'Módulo de gestión de porcinos', TRUE
WHERE NOT EXISTS (SELECT 1 FROM modules WHERE code = 'pigs');

UPDATE modules SET active = TRUE WHERE code = 'pigs';

INSERT INTO company_modules (company_id, module_id, enabled)
SELECT e.id, m.id, TRUE
FROM empresas e
INNER JOIN modules m ON m.code = 'pigs'
WHERE NOT EXISTS (
    SELECT 1 FROM company_modules cm
    WHERE cm.company_id = e.id AND cm.module_id = m.id
);

UPDATE company_modules cm
INNER JOIN modules m ON m.id = cm.module_id
SET cm.enabled = TRUE
WHERE m.code = 'pigs';
