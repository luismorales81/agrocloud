-- ============================================================================
-- SCRIPT PARA ELIMINAR CONFIGURACIONES REDUNDANTES
-- Estas configuraciones están duplicadas en Parámetros Productivos
-- y deben eliminarse de Configuraciones Generales
-- ============================================================================
-- 
-- Configuraciones a eliminar (duplicadas en Parámetros Productivos):
-- - DIAS_GESTACION (usa ParametrosProductivos.diasPromedioGestacion)
-- - DIAS_LACTANCIA (usa ParametrosProductivos.diasLactancia)
-- - DIAS_ENTRE_CELOS (usa ParametrosProductivos.diasEntreCelos)
-- - DIAS_CONTROL_CELO (usa ParametrosProductivos.diasControlCelo)
--
-- Configuraciones a MANTENER:
-- - DIAS_CACHORRA (únicamente usada en Configuraciones Generales en MadreService)
-- ============================================================================

USE agrocloud;

-- Verificar configuraciones existentes antes de eliminar
SELECT 
    'CONFIGURACIONES ANTES DE ELIMINAR' AS estado,
    clave,
    valor,
    categoria,
    empresa_id,
    activo
FROM porcinos_configuraciones_porcinos
WHERE clave IN ('DIAS_GESTACION', 'DIAS_LACTANCIA', 'DIAS_ENTRE_CELOS', 'DIAS_CONTROL_CELO', 'DIAS_CACHORRA')
ORDER BY empresa_id, clave;

-- Eliminar configuraciones redundantes (marcar como inactivas en lugar de eliminar físicamente)
-- NOTA: Estas configuraciones están duplicadas en Parámetros Productivos y deben eliminarse
UPDATE porcinos_configuraciones_porcinos
SET activo = 0,
    fecha_actualizacion = NOW()
WHERE clave IN ('DIAS_GESTACION', 'DIAS_LACTANCIA', 'DIAS_ENTRE_CELOS', 'DIAS_CONTROL_CELO')
  AND activo = 1;

-- Verificar resultado
SELECT 
    'CONFIGURACIONES DESPUÉS DE ELIMINAR' AS estado,
    clave,
    valor,
    categoria,
    empresa_id,
    activo
FROM porcinos_configuraciones_porcinos
WHERE clave IN ('DIAS_GESTACION', 'DIAS_LACTANCIA', 'DIAS_ENTRE_CELOS', 'DIAS_CONTROL_CELO', 'DIAS_CACHORRA')
ORDER BY empresa_id, clave;

-- Verificar que DIAS_CACHORRA sigue activa (debe mantenerse porque se usa en MadreService)
SELECT 
    'VERIFICACIÓN: DIAS_CACHORRA DEBE ESTAR ACTIVA' AS verificacion,
    clave,
    valor,
    empresa_id,
    activo
FROM porcinos_configuraciones_porcinos
WHERE clave = 'DIAS_CACHORRA'
  AND activo = 1;

-- Resumen por empresa
SELECT 
    'RESUMEN: Configuraciones eliminadas por empresa (marcadas como inactivas)' AS mensaje,
    empresa_id,
    clave,
    COUNT(*) AS cantidad_eliminadas
FROM porcinos_configuraciones_porcinos
WHERE clave IN ('DIAS_GESTACION', 'DIAS_LACTANCIA', 'DIAS_ENTRE_CELOS', 'DIAS_CONTROL_CELO')
  AND activo = 0
GROUP BY empresa_id, clave
ORDER BY empresa_id, clave;
