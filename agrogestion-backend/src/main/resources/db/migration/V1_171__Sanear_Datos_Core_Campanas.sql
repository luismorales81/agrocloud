-- ============================================================================
-- V1_171: Sanear datos inválidos en core_campanas (evita 500 al listar)
-- ============================================================================

UPDATE core_campanas
SET estado = 'BORRADOR'
WHERE estado IS NULL
   OR TRIM(estado) = ''
   OR estado NOT IN ('BORRADOR', 'ACTIVA', 'CERRADA');

UPDATE core_campanas
SET es_default = 0
WHERE es_default IS NULL;

UPDATE core_campanas
SET created_at = COALESCE(created_at, NOW())
WHERE created_at IS NULL;

SELECT 'V1_171: core_campanas saneada' AS mensaje;
