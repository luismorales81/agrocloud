-- ============================================================================
-- MIGRACIÓN: Eliminar categorías RACION_* de TipoAlimentoPorcino
-- Versión: V1_111
-- Si la tabla ya no existe (p. ej. eliminada en V1_114), no hace nada (Flyway out-of-order).
-- ============================================================================

SET @esquema = DATABASE();

SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.TABLES
   WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'porcinos_tipos_alimento_porcinos') = 0,
  'SELECT 1',
  'UPDATE porcinos_tipos_alimento_porcinos SET categoria = ''OTRO'', activo = 0, fecha_actualizacion = NOW() WHERE categoria IN (''RACION_INICIADOR'', ''RACION_TERMINADOR'', ''RACION_GESTACION'', ''RACION_LACTANCIA'')'
));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.TABLES
   WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'porcinos_tipos_alimento_porcinos') = 0,
  'SELECT 1',
  'ALTER TABLE porcinos_tipos_alimento_porcinos MODIFY COLUMN categoria ENUM(''BALANCEADO'', ''GRANO_PROPIO'', ''OTRO'') NOT NULL COMMENT ''Categorías: BALANCEADO, GRANO_PROPIO, OTRO. RACION_* eliminadas (InsumoCompuesto).'''
));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;
