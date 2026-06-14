-- ============================================================================
-- MIGRACIÓN: Renombrar tablas según módulo (cultivo_, porcinos_)
-- Versión: V1_99
-- Idempotente: si el renombrado ya se aplicó (destino existe), se omite.
-- ============================================================================

SET @esquema = DATABASE();

-- ============================================================================
-- MÓDULO CULTIVO
-- ============================================================================

SET @consulta = (SELECT IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'cultivo_campos'), 'SELECT 1', IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'campos'), 'RENAME TABLE campos TO cultivo_campos', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'cultivo_lotes'), 'SELECT 1', IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'lotes'), 'RENAME TABLE lotes TO cultivo_lotes', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'cultivo_cultivos'), 'SELECT 1', IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'cultivos'), 'RENAME TABLE cultivos TO cultivo_cultivos', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'cultivo_labores'), 'SELECT 1', IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'labores'), 'RENAME TABLE labores TO cultivo_labores', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'cultivo_labor_maquinaria'), 'SELECT 1', IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'labor_maquinaria'), 'RENAME TABLE labor_maquinaria TO cultivo_labor_maquinaria', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'cultivo_labor_insumos'), 'SELECT 1', IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'labor_insumos'), 'RENAME TABLE labor_insumos TO cultivo_labor_insumos', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'cultivo_labor_mano_obra'), 'SELECT 1', IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'labor_mano_obra'), 'RENAME TABLE labor_mano_obra TO cultivo_labor_mano_obra', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'cultivo_cosechas'), 'SELECT 1', IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'cosechas'), 'RENAME TABLE cosechas TO cultivo_cosechas', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'cultivo_historial_cosechas'), 'SELECT 1', IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'historial_cosechas'), 'RENAME TABLE historial_cosechas TO cultivo_historial_cosechas', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'cultivo_insumos'), 'SELECT 1', IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'insumos'), 'RENAME TABLE insumos TO cultivo_insumos', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'cultivo_dosis_insumos'), 'SELECT 1', IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'dosis_insumos'), 'RENAME TABLE dosis_insumos TO cultivo_dosis_insumos', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'cultivo_aplicaciones_agroquimicas'), 'SELECT 1', IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'aplicaciones_agroquimicas'), 'RENAME TABLE aplicaciones_agroquimicas TO cultivo_aplicaciones_agroquimicas', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'cultivo_dosis_aplicacion'), 'SELECT 1', IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'dosis_aplicacion'), 'RENAME TABLE dosis_aplicacion TO cultivo_dosis_aplicacion', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'cultivo_inventario_granos'), 'SELECT 1', IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'inventario_granos'), 'RENAME TABLE inventario_granos TO cultivo_inventario_granos', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'cultivo_movimientos_inventario_granos'), 'SELECT 1', IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'movimientos_inventario_granos'), 'RENAME TABLE movimientos_inventario_granos TO cultivo_movimientos_inventario_granos', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'cultivo_movimientos_inventario'), 'SELECT 1', IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'movimientos_inventario'), 'RENAME TABLE movimientos_inventario TO cultivo_movimientos_inventario', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'cultivo_maquinaria'), 'SELECT 1', IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'maquinaria'), 'RENAME TABLE maquinaria TO cultivo_maquinaria', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'cultivo_mantenimientos_maquinaria'), 'SELECT 1', IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'mantenimientos_maquinaria'), 'RENAME TABLE mantenimientos_maquinaria TO cultivo_mantenimientos_maquinaria', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'cultivo_alquiler_maquinaria'), 'SELECT 1', IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'alquiler_maquinaria'), 'RENAME TABLE alquiler_maquinaria TO cultivo_alquiler_maquinaria', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'cultivo_ingresos'), 'SELECT 1', IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'ingresos'), 'RENAME TABLE ingresos TO cultivo_ingresos', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'cultivo_egresos'), 'SELECT 1', IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'egresos'), 'RENAME TABLE egresos TO cultivo_egresos', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

-- ============================================================================
-- MÓDULO PORCINOS
-- ============================================================================

SET @consulta = (SELECT IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_madres'), 'SELECT 1', IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'madres'), 'RENAME TABLE madres TO porcinos_madres', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_padrillos'), 'SELECT 1', IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'padrillos'), 'RENAME TABLE padrillos TO porcinos_padrillos', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_servicios'), 'SELECT 1', IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'servicios'), 'RENAME TABLE servicios TO porcinos_servicios', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_gestacion'), 'SELECT 1', IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'gestacion'), 'RENAME TABLE gestacion TO porcinos_gestacion', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_chequeos_gestacion'), 'SELECT 1', IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'chequeos_gestacion'), 'RENAME TABLE chequeos_gestacion TO porcinos_chequeos_gestacion', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_partos'), 'SELECT 1', IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'partos'), 'RENAME TABLE partos TO porcinos_partos', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_destetes'), 'SELECT 1', IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'destetes'), 'RENAME TABLE destetes TO porcinos_destetes', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_recria'), 'SELECT 1', IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'recria'), 'RENAME TABLE recria TO porcinos_recria', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_movimientos_etapas'), 'SELECT 1', IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'movimientos_etapas'), 'RENAME TABLE movimientos_etapas TO porcinos_movimientos_etapas', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_registros_peso'), 'SELECT 1', IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'registros_peso'), 'RENAME TABLE registros_peso TO porcinos_registros_peso', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_muertes_lactancia'), 'SELECT 1', IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'muertes_lactancia'), 'RENAME TABLE muertes_lactancia TO porcinos_muertes_lactancia', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_muertes_recria'), 'SELECT 1', IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'muertes_recria'), 'RENAME TABLE muertes_recria TO porcinos_muertes_recria', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_muertes_madres'), 'SELECT 1', IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'muertes_madres'), 'RENAME TABLE muertes_madres TO porcinos_muertes_madres', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_consumos_alimento'), 'SELECT 1', IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'consumos_alimento'), 'RENAME TABLE consumos_alimento TO porcinos_consumos_alimento', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_ventas_porcinos'), 'SELECT 1', IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'ventas_porcinos'), 'RENAME TABLE ventas_porcinos TO porcinos_ventas_porcinos', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_faena'), 'SELECT 1', IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'faena'), 'RENAME TABLE faena TO porcinos_faena', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_lechones_nn'), 'SELECT 1', IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'lechones_nn'), 'RENAME TABLE lechones_nn TO porcinos_lechones_nn', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_transferencias_lechones'), 'SELECT 1', IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'transferencias_lechones'), 'RENAME TABLE transferencias_lechones TO porcinos_transferencias_lechones', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_stock_alimento'), 'SELECT 1', IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'stock_alimento'), 'RENAME TABLE stock_alimento TO porcinos_stock_alimento', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_historial_estados_madres'), 'SELECT 1', IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'historial_estados_madres'), 'RENAME TABLE historial_estados_madres TO porcinos_historial_estados_madres', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_razas_porcinos'), 'SELECT 1', IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'razas_porcinos'), 'RENAME TABLE razas_porcinos TO porcinos_razas_porcinos', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_tipos_alimento_porcinos'), 'SELECT 1', IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'tipos_alimento_porcinos'), 'RENAME TABLE tipos_alimento_porcinos TO porcinos_tipos_alimento_porcinos', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_tipos_servicio_porcinos'), 'SELECT 1', IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'tipos_servicio_porcinos'), 'RENAME TABLE tipos_servicio_porcinos TO porcinos_tipos_servicio_porcinos', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_causas_mortalidad_porcinos'), 'SELECT 1', IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'causas_mortalidad_porcinos'), 'RENAME TABLE causas_mortalidad_porcinos TO porcinos_causas_mortalidad_porcinos', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_motivos_baja_porcinos'), 'SELECT 1', IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'motivos_baja_porcinos'), 'RENAME TABLE motivos_baja_porcinos TO porcinos_motivos_baja_porcinos', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_esquemas_sanitarios_porcinos'), 'SELECT 1', IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'esquemas_sanitarios_porcinos'), 'RENAME TABLE esquemas_sanitarios_porcinos TO porcinos_esquemas_sanitarios_porcinos', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_tipos_corral_porcinos'), 'SELECT 1', IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'tipos_corral_porcinos'), 'RENAME TABLE tipos_corral_porcinos TO porcinos_tipos_corral_porcinos', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_parametros_establecimiento_porcinos'), 'SELECT 1', IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'parametros_establecimiento_porcinos'), 'RENAME TABLE parametros_establecimiento_porcinos TO porcinos_parametros_establecimiento_porcinos', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_parametros_productivos_porcinos'), 'SELECT 1', IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'parametros_productivos_porcinos'), 'RENAME TABLE parametros_productivos_porcinos TO porcinos_parametros_productivos_porcinos', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_datos_economicos_porcinos'), 'SELECT 1', IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'datos_economicos_porcinos'), 'RENAME TABLE datos_economicos_porcinos TO porcinos_datos_economicos_porcinos', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_configuraciones_porcinos'), 'SELECT 1', IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'configuraciones_porcinos'), 'RENAME TABLE configuraciones_porcinos TO porcinos_configuraciones_porcinos', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;
