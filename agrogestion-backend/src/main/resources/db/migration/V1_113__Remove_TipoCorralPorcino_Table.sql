-- ============================================================================
-- ELIMINAR TABLA tipos_corral_porcinos (DEPRECADA)
-- ============================================================================
-- Esta migración elimina la tabla tipos_corral_porcinos porque estaba en 
-- superposición con la estructura de UbicacionInterna (porcinos_ubicaciones_internas).
-- El sistema usa UbicacionInterna (estructura jerárquica: Galpón → Sala → Corral)
-- que tiene FK en Madre, Padrillo y Recria, mientras que TipoCorralPorcino no se usa.

-- Eliminar tabla (si existe)
DROP TABLE IF EXISTS porcinos_tipos_corral_porcinos;
DROP TABLE IF EXISTS tipos_corral_porcinos;
