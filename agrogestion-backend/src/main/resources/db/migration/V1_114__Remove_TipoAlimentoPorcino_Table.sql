-- ============================================================================
-- ELIMINAR TABLA tipos_alimento_porcinos (DEPRECADA)
-- ============================================================================
-- Esta migración elimina la tabla tipos_alimento_porcinos porque es redundante
-- con el sistema de recetas (InsumoCompuesto) e insumos básicos (Insumo).
-- 
-- RAZÓN DE ELIMINACIÓN:
-- - Las recetas (raciones) ahora se gestionan completamente en InsumoCompuesto (tipo RACION)
--   que se asocia a etapas mediante RecetaAlimentacionPorEtapa
-- - Los balanceados comerciales pueden gestionarse como Insumo (tabla cultivo_insumos)
-- - Los granos propios se gestionan mediante Cultivo + InventarioGrano
-- - TipoAlimentoPorcino solo existía como catálogo sin integración funcional
--   en el sistema de consumo actual (ConsumoDiarioAutomatico)

-- Eliminar tabla (si existe)
DROP TABLE IF EXISTS porcinos_tipos_alimento_porcinos;
DROP TABLE IF EXISTS tipos_alimento_porcinos;
