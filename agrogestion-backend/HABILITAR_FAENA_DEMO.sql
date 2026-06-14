-- ============================================================================
-- SCRIPT: Habilitar Faena para Empresas Demo
-- ============================================================================
-- Este script habilita la funcionalidad de faenas para todas las empresas
-- o para empresas específicas del entorno demo
-- ============================================================================

USE agrocloud;

-- Habilitar faena para todas las empresas que tienen parámetros configurados
UPDATE porcinos_parametros_establecimiento_porcinos 
SET realiza_faena = TRUE 
WHERE empresa_id IS NOT NULL;

-- Mostrar resultado
SELECT 
    pep.id,
    pep.empresa_id,
    e.nombre as empresa_nombre,
    pep.nombre_establecimiento,
    pep.realiza_faena
FROM porcinos_parametros_establecimiento_porcinos pep
LEFT JOIN empresas e ON pep.empresa_id = e.id;

SELECT '✓ Faena habilitada para todas las empresas' AS resultado;
