-- Habilita AVICOLA_CRIANZA y AVICOLA_HUEVOS para todas las empresas que aún no los tienen en company_modules.
-- Así el front (/api/modules/available) puede mostrar los módulos sin paso manual por empresa.
-- Idempotente.

INSERT INTO company_modules (company_id, module_id, enabled)
SELECT e.id, m.id, TRUE
FROM empresas e
INNER JOIN modules m ON m.code IN ('AVICOLA_CRIANZA', 'AVICOLA_HUEVOS')
WHERE NOT EXISTS (
    SELECT 1 FROM company_modules cm
    WHERE cm.company_id = e.id AND cm.module_id = m.id
);
