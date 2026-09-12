-- ============================================================================
-- V1_170: Ocultar módulo duplicado AVICOLA_PONEDORAS del selector
-- La postura/ponedoras queda unificada en AVICOLA_HUEVOS (avicola-huevos en frontend).
-- Las rutas legacy /avicola-ponedoras siguen disponibles si ya estaban en uso.
-- ============================================================================

UPDATE company_modules cm
INNER JOIN modules m ON m.id = cm.module_id
SET cm.enabled = FALSE
WHERE m.code = 'AVICOLA_PONEDORAS';

SELECT 'V1_170: AVICOLA_PONEDORAS deshabilitado en selector por empresa' AS mensaje;
