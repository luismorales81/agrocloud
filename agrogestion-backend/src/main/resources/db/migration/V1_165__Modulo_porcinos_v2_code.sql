-- Código de módulo PORCINOS para API v2 (@RequiresModule("PORCINOS")).
-- El legacy usa code 'pigs' (V1_161). Idempotente.

INSERT INTO modules (name, code, description, active)
SELECT 'Porcinos', 'PORCINOS',
       'Gestión porcina v2: reproducción, lotes, dietas, ventas y reportes.',
       TRUE
WHERE NOT EXISTS (SELECT 1 FROM modules WHERE code = 'PORCINOS');

UPDATE modules SET active = TRUE WHERE code = 'PORCINOS';

-- Empresas con módulo legacy pigs → habilitar PORCINOS con el mismo estado
INSERT INTO company_modules (company_id, module_id, enabled)
SELECT cm.company_id, mp.id, cm.enabled
FROM company_modules cm
INNER JOIN modules m ON m.id = cm.module_id AND m.code = 'pigs'
INNER JOIN modules mp ON mp.code = 'PORCINOS'
WHERE NOT EXISTS (
    SELECT 1 FROM company_modules cm2
    WHERE cm2.company_id = cm.company_id AND cm2.module_id = mp.id
);

UPDATE company_modules cm_p
INNER JOIN modules mp ON mp.id = cm_p.module_id AND mp.code = 'PORCINOS'
INNER JOIN company_modules cm_g ON cm_g.company_id = cm_p.company_id
INNER JOIN modules m_g ON m_g.id = cm_g.module_id AND m_g.code = 'pigs'
SET cm_p.enabled = cm_g.enabled
WHERE cm_g.enabled = TRUE;

-- Resto de empresas sin fila PORCINOS
INSERT INTO company_modules (company_id, module_id, enabled)
SELECT e.id, m.id, TRUE
FROM empresas e
INNER JOIN modules m ON m.code = 'PORCINOS'
WHERE NOT EXISTS (
    SELECT 1 FROM company_modules cm
    WHERE cm.company_id = e.id AND cm.module_id = m.id
);
