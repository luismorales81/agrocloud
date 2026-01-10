-- ============================================================================
-- SCRIPT PARA ELIMINAR TIPOS DE ALIMENTO CON CATEGORÍAS RACION_*
-- Estas categorías están solapadas con InsumoCompuesto (recetas)
-- ============================================================================
-- 
-- Las recetas (raciones) ahora se gestionan completamente en InsumoCompuesto
-- que permite:
-- - Definir componentes detallados (insumos, granos, otras recetas)
-- - Control de stock y preparación
-- - Asociación a etapas específicas (RecetaAlimentacionPorEtapa)
-- - Cálculo automático de costos
--
-- Las categorías RACION_* de TipoAlimentoPorcino están duplicadas y no se usan
-- funcionalmente en el código (ConsumoAlimento usa un enum simple, no referencia
-- a TipoAlimentoPorcino).
-- ============================================================================

USE agrocloud;

-- Verificar tipos de alimento con categorías RACION_* antes de eliminar
SELECT 
    'TIPOS DE ALIMENTO ANTES DE ELIMINAR' AS estado,
    id,
    nombre,
    categoria,
    porcentaje_proteina,
    precio_kg,
    empresa_id,
    activo
FROM porcinos_tipos_alimento_porcinos
WHERE categoria IN ('RACION_INICIADOR', 'RACION_TERMINADOR', 'RACION_GESTACION', 'RACION_LACTANCIA')
ORDER BY empresa_id, categoria, nombre;

-- Eliminar tipos de alimento con categorías RACION_* (marcar como inactivos)
-- NOTA: Estas categorías están solapadas con InsumoCompuesto (recetas)
UPDATE porcinos_tipos_alimento_porcinos
SET activo = 0,
    fecha_actualizacion = NOW()
WHERE categoria IN ('RACION_INICIADOR', 'RACION_TERMINADOR', 'RACION_GESTACION', 'RACION_LACTANCIA')
  AND activo = 1;

-- Verificar resultado
SELECT 
    'TIPOS DE ALIMENTO DESPUÉS DE ELIMINAR (solo deben quedar BALANCEADO, GRANO_PROPIO, OTRO)' AS estado,
    categoria,
    COUNT(*) AS cantidad_activos
FROM porcinos_tipos_alimento_porcinos
WHERE activo = 1
GROUP BY categoria
ORDER BY categoria;

-- Verificar tipos de alimento activos restantes
SELECT 
    'TIPOS DE ALIMENTO ACTIVOS FINALES' AS estado,
    id,
    nombre,
    categoria,
    porcentaje_proteina,
    precio_kg,
    empresa_id
FROM porcinos_tipos_alimento_porcinos
WHERE activo = 1
ORDER BY empresa_id, categoria, nombre;

-- Resumen de tipos eliminados por categoría
SELECT 
    'RESUMEN: Tipos de alimento eliminados por categoría' AS mensaje,
    categoria,
    COUNT(*) AS cantidad_eliminados
FROM porcinos_tipos_alimento_porcinos
WHERE categoria IN ('RACION_INICIADOR', 'RACION_TERMINADOR', 'RACION_GESTACION', 'RACION_LACTANCIA')
  AND activo = 0
GROUP BY categoria
ORDER BY categoria;

-- Verificar que no hay referencias a estos tipos (por si acaso)
-- NOTA: ConsumoAlimento usa un enum simple, no referencia a TipoAlimentoPorcino,
-- así que no debería haber problemas de integridad referencial

SELECT 
    'VERIFICACIÓN: No debería haber referencias funcionales a estos tipos' AS verificacion,
    'ConsumoAlimento.tipoAlimento es un enum simple (BALANCEADO, MAIZ, GRANO_PROPIO), no referencia a TipoAlimentoPorcino' AS estado;
