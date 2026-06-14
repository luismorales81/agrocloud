-- Registra AVICOLA_CARNE y AVICOLA_PONEDORAS (módulos separados; sin conflicto entre sí ni con AVICOLA_HUEVOS / AVICOLA_CRIANZA).
-- Habilita ambos en la tabla company_modules (JPA: entidad CompanyModule) para cada empresa que aún no tenga esa fila.
-- Son dos module_id distintos: luego cada uno puede deshabilitarse por separado (una empresa puede quedar solo carne, solo ponedoras, ambos o ninguno).

INSERT INTO modules (name, code, description, active)
SELECT 'Avícola carne', 'AVICOLA_CARNE',
       'Carne avícola: lotes, pesadas, mortalidad, ventas, consumos y sanidad (API /api/avicola-carne).', TRUE
WHERE NOT EXISTS (SELECT 1 FROM modules WHERE code = 'AVICOLA_CARNE');

INSERT INTO modules (name, code, description, active)
SELECT 'Avícola ponedoras', 'AVICOLA_PONEDORAS',
       'Explotaciones de ponedoras / recría (módulo independiente de carne y de huevos).', TRUE
WHERE NOT EXISTS (SELECT 1 FROM modules WHERE code = 'AVICOLA_PONEDORAS');

INSERT INTO company_modules (company_id, module_id, enabled)
SELECT e.id, m.id, TRUE
FROM empresas e
INNER JOIN modules m ON m.code IN ('AVICOLA_CARNE', 'AVICOLA_PONEDORAS')
WHERE NOT EXISTS (
    SELECT 1 FROM company_modules cm
    WHERE cm.company_id = e.id AND cm.module_id = m.id
);
