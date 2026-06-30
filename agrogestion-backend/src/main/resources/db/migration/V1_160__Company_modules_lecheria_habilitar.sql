-- Habilita LECHERIA para todas las empresas que aún no lo tienen en company_modules.

INSERT INTO modules (name, code, description, active)
SELECT 'Lechería', 'LECHERIA',
       'Gestión lechera multi-especie: animales, lactancia, ordeñe, reproducción, sanidad y ventas.',
       TRUE
WHERE NOT EXISTS (SELECT 1 FROM modules WHERE code = 'LECHERIA');

INSERT INTO company_modules (company_id, module_id, enabled)
SELECT e.id, m.id, FALSE
FROM empresas e
CROSS JOIN modules m
WHERE m.code = 'LECHERIA'
  AND NOT EXISTS (
    SELECT 1 FROM company_modules cm
    WHERE cm.company_id = e.id AND cm.module_id = m.id
  );
