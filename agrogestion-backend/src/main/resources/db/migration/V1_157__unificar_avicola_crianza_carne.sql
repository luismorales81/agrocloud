-- Unificación módulo avícola: AVICOLA_CARNE → AVICOLA_CRIANZA (único producto parrilleros)

UPDATE avicola_establecimiento
SET modulo_origen = 'AVICOLA_CRIANZA'
WHERE modulo_origen = 'AVICOLA_CARNE';

UPDATE avicola_raza
SET modulo_origen = 'AVICOLA_CRIANZA'
WHERE modulo_origen = 'AVICOLA_CARNE';

UPDATE avicola_lote
SET modulo_origen = 'AVICOLA_CRIANZA'
WHERE modulo_origen = 'AVICOLA_CARNE';

-- Habilitar crianza donde existía carne
INSERT INTO company_modules (company_id, module_id, enabled)
SELECT cm_carne.company_id, m_crianza.id, TRUE
FROM company_modules cm_carne
INNER JOIN modules m_carne ON m_carne.id = cm_carne.module_id AND m_carne.code = 'AVICOLA_CARNE'
INNER JOIN modules m_crianza ON m_crianza.code = 'AVICOLA_CRIANZA'
WHERE cm_carne.enabled = TRUE
  AND NOT EXISTS (
      SELECT 1 FROM company_modules cm2
      WHERE cm2.company_id = cm_carne.company_id AND cm2.module_id = m_crianza.id
  );

UPDATE company_modules cm
INNER JOIN modules m ON m.id = cm.module_id
SET cm.enabled = FALSE
WHERE m.code = 'AVICOLA_CARNE';

UPDATE modules
SET active = FALSE
WHERE code = 'AVICOLA_CARNE';

UPDATE modules
SET name = 'Avícola crianza',
    description = 'Crianza de pollos parrilleros: galpones, lotes, pesadas, faena, consumos y reportes.'
WHERE code = 'AVICOLA_CRIANZA';

-- Calendario: series que usaban ámbito carne pasan a crianza
UPDATE calendario_serie_tarea_recurrente
SET ambito_calendario = 'AVICOLA_CRIANZA'
WHERE ambito_calendario = 'AVICOLA_CARNE';
