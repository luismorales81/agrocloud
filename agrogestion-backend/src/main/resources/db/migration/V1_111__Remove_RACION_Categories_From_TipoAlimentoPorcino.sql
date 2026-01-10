-- ============================================================================
-- MIGRACIÓN: Eliminar categorías RACION_* de TipoAlimentoPorcino
-- Versión: V1_111
-- Fecha: 2025-01-XX
-- Descripción: Elimina las categorías RACION_INICIADOR, RACION_TERMINADOR,
--              RACION_GESTACION, RACION_LACTANCIA del ENUM de categoría porque
--              están solapadas con InsumoCompuesto (recetas).
--              Las recetas (raciones) ahora se gestionan completamente en 
--              InsumoCompuesto (tipo RACION) que se asocia a etapas mediante
--              RecetaAlimentacionPorEtapa.
-- ============================================================================

-- Primero, actualizar TODOS los registros (activos e inactivos) con categorías RACION_*
-- a 'OTRO' antes de modificar el ENUM, ya que MySQL no permite cambiar un ENUM
-- si hay registros con valores que se van a eliminar
UPDATE porcinos_tipos_alimento_porcinos
SET categoria = 'OTRO',
    activo = 0,
    fecha_actualizacion = NOW()
WHERE categoria IN ('RACION_INICIADOR', 'RACION_TERMINADOR', 'RACION_GESTACION', 'RACION_LACTANCIA');

-- Modificar el ENUM para eliminar las categorías RACION_*
-- NOTA: Ahora es seguro modificar el ENUM porque ya no hay registros con esas categorías
ALTER TABLE porcinos_tipos_alimento_porcinos
MODIFY COLUMN categoria ENUM('BALANCEADO', 'GRANO_PROPIO', 'OTRO') NOT NULL
COMMENT 'Categorías: BALANCEADO (balanceados comerciales), GRANO_PROPIO (granos propios), OTRO. Las categorías RACION_* fueron eliminadas porque están solapadas con InsumoCompuesto (recetas tipo RACION asociadas a etapas mediante RecetaAlimentacionPorEtapa)';
