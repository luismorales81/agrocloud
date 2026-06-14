-- ============================================================================
-- SCRIPT: Crear Parámetros de Establecimiento para Empresa Demo
-- ============================================================================
-- Este script crea los parámetros del establecimiento para la empresa demo
-- con faena habilitada para poder probar la funcionalidad
-- ============================================================================

USE agrocloud;

-- Crear parámetros para la empresa demo (id=1) si no existen
INSERT INTO porcinos_parametros_establecimiento_porcinos (
    nombre_establecimiento,
    unidad_manejo,
    realiza_faena,
    empresa_id,
    fecha_creacion
)
SELECT 
    'Establecimiento Demo' as nombre_establecimiento,
    'LOTES' as unidad_manejo,
    TRUE as realiza_faena, -- ✅ Faena habilitada
    1 as empresa_id, -- ID de "AgroCloud Demo"
    NOW() as fecha_creacion
WHERE NOT EXISTS (
    SELECT 1 FROM porcinos_parametros_establecimiento_porcinos 
    WHERE empresa_id = 1
);

-- También crear para otras empresas si no existen (con faena deshabilitada por defecto)
INSERT INTO porcinos_parametros_establecimiento_porcinos (
    nombre_establecimiento,
    unidad_manejo,
    realiza_faena,
    empresa_id,
    fecha_creacion
)
SELECT 
    CONCAT('Establecimiento - ', e.nombre) as nombre_establecimiento,
    'LOTES' as unidad_manejo,
    FALSE as realiza_faena, -- Por defecto sin faena
    e.id as empresa_id,
    NOW() as fecha_creacion
FROM empresas e
WHERE e.id NOT IN (
    SELECT empresa_id FROM porcinos_parametros_establecimiento_porcinos WHERE empresa_id IS NOT NULL
)
AND e.id > 1; -- Excluir la empresa demo ya que la creamos arriba

-- Mostrar resultado
SELECT 
    pep.id,
    pep.empresa_id,
    e.nombre as empresa_nombre,
    pep.nombre_establecimiento,
    pep.realiza_faena,
    pep.unidad_manejo
FROM porcinos_parametros_establecimiento_porcinos pep
LEFT JOIN empresas e ON pep.empresa_id = e.id
ORDER BY pep.empresa_id;

SELECT '✓ Parámetros de establecimiento creados. Empresa Demo (id=1) tiene faena habilitada.' AS resultado;
