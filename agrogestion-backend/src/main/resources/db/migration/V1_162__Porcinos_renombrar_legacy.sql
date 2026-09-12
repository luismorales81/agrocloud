-- ============================================================================
-- MIGRACIÓN: Renombrar tablas legacy del módulo Porcinos → porcinos_legacy_*
-- Versión: V1_162
-- Descripción: Libera el prefijo porcinos_* para el esquema v2.
-- Idempotente: omite tablas ya renombradas o inexistentes.
-- ============================================================================

SET @esquema = DATABASE();

SET FOREIGN_KEY_CHECKS = 0;

-- Catálogos y configuración
SET @consulta = (SELECT IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_legacy_razas_porcinos'), 'SELECT 1',
    IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_razas_porcinos'),
       'RENAME TABLE porcinos_razas_porcinos TO porcinos_legacy_razas_porcinos', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_legacy_tipos_servicio_porcinos'), 'SELECT 1',
    IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_tipos_servicio_porcinos'),
       'RENAME TABLE porcinos_tipos_servicio_porcinos TO porcinos_legacy_tipos_servicio_porcinos', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_legacy_causas_mortalidad_porcinos'), 'SELECT 1',
    IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_causas_mortalidad_porcinos'),
       'RENAME TABLE porcinos_causas_mortalidad_porcinos TO porcinos_legacy_causas_mortalidad_porcinos', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_legacy_motivos_baja_porcinos'), 'SELECT 1',
    IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_motivos_baja_porcinos'),
       'RENAME TABLE porcinos_motivos_baja_porcinos TO porcinos_legacy_motivos_baja_porcinos', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_legacy_esquemas_sanitarios_porcinos'), 'SELECT 1',
    IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_esquemas_sanitarios_porcinos'),
       'RENAME TABLE porcinos_esquemas_sanitarios_porcinos TO porcinos_legacy_esquemas_sanitarios_porcinos', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_legacy_tipos_alimento_porcinos'), 'SELECT 1',
    IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_tipos_alimento_porcinos'),
       'RENAME TABLE porcinos_tipos_alimento_porcinos TO porcinos_legacy_tipos_alimento_porcinos', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_legacy_tipos_corral_porcinos'), 'SELECT 1',
    IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_tipos_corral_porcinos'),
       'RENAME TABLE porcinos_tipos_corral_porcinos TO porcinos_legacy_tipos_corral_porcinos', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_legacy_parametros_establecimiento_porcinos'), 'SELECT 1',
    IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_parametros_establecimiento_porcinos'),
       'RENAME TABLE porcinos_parametros_establecimiento_porcinos TO porcinos_legacy_parametros_establecimiento_porcinos', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_legacy_parametros_productivos_porcinos'), 'SELECT 1',
    IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_parametros_productivos_porcinos'),
       'RENAME TABLE porcinos_parametros_productivos_porcinos TO porcinos_legacy_parametros_productivos_porcinos', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_legacy_datos_economicos_porcinos'), 'SELECT 1',
    IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_datos_economicos_porcinos'),
       'RENAME TABLE porcinos_datos_economicos_porcinos TO porcinos_legacy_datos_economicos_porcinos', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_legacy_configuraciones_porcinos'), 'SELECT 1',
    IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_configuraciones_porcinos'),
       'RENAME TABLE porcinos_configuraciones_porcinos TO porcinos_legacy_configuraciones_porcinos', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_legacy_tipos_evento_sanitario'), 'SELECT 1',
    IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_tipos_evento_sanitario'),
       'RENAME TABLE porcinos_tipos_evento_sanitario TO porcinos_legacy_tipos_evento_sanitario', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_legacy_proveedores_genetica'), 'SELECT 1',
    IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_proveedores_genetica'),
       'RENAME TABLE porcinos_proveedores_genetica TO porcinos_legacy_proveedores_genetica', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_legacy_tipos_parto'), 'SELECT 1',
    IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_tipos_parto'),
       'RENAME TABLE porcinos_tipos_parto TO porcinos_legacy_tipos_parto', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_legacy_causas_nacidos_muertos'), 'SELECT 1',
    IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_causas_nacidos_muertos'),
       'RENAME TABLE porcinos_causas_nacidos_muertos TO porcinos_legacy_causas_nacidos_muertos', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_legacy_causas_momificados'), 'SELECT 1',
    IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_causas_momificados'),
       'RENAME TABLE porcinos_causas_momificados TO porcinos_legacy_causas_momificados', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_legacy_ubicaciones_internas'), 'SELECT 1',
    IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_ubicaciones_internas'),
       'RENAME TABLE porcinos_ubicaciones_internas TO porcinos_legacy_ubicaciones_internas', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_legacy_recetas_alimentacion_etapa'), 'SELECT 1',
    IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_recetas_alimentacion_etapa'),
       'RENAME TABLE porcinos_recetas_alimentacion_etapa TO porcinos_legacy_recetas_alimentacion_etapa', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

-- Plantillas wizard IA
SET @consulta = (SELECT IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_legacy_plan_recria_recordatorio'), 'SELECT 1',
    IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_plan_recria_recordatorio'),
       'RENAME TABLE porcinos_plan_recria_recordatorio TO porcinos_legacy_plan_recria_recordatorio', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_legacy_plan_recria_receta_sugerencia'), 'SELECT 1',
    IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_plan_recria_receta_sugerencia'),
       'RENAME TABLE porcinos_plan_recria_receta_sugerencia TO porcinos_legacy_plan_recria_receta_sugerencia', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_legacy_plan_recria_etapa'), 'SELECT 1',
    IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_plan_recria_etapa'),
       'RENAME TABLE porcinos_plan_recria_etapa TO porcinos_legacy_plan_recria_etapa', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_legacy_plan_recria'), 'SELECT 1',
    IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_plan_recria'),
       'RENAME TABLE porcinos_plan_recria TO porcinos_legacy_plan_recria', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

-- Consumo diario automático
SET @consulta = (SELECT IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_legacy_movimientos_stock'), 'SELECT 1',
    IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_movimientos_stock'),
       'RENAME TABLE porcinos_movimientos_stock TO porcinos_legacy_movimientos_stock', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_legacy_derrames_perdidas'), 'SELECT 1',
    IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_derrames_perdidas'),
       'RENAME TABLE porcinos_derrames_perdidas TO porcinos_legacy_derrames_perdidas', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_legacy_consumos_diarios_detalle'), 'SELECT 1',
    IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_consumos_diarios_detalle'),
       'RENAME TABLE porcinos_consumos_diarios_detalle TO porcinos_legacy_consumos_diarios_detalle', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_legacy_consumos_diarios_automaticos'), 'SELECT 1',
    IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_consumos_diarios_automaticos'),
       'RENAME TABLE porcinos_consumos_diarios_automaticos TO porcinos_legacy_consumos_diarios_automaticos', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_legacy_dias_alimentacion'), 'SELECT 1',
    IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_dias_alimentacion'),
       'RENAME TABLE porcinos_dias_alimentacion TO porcinos_legacy_dias_alimentacion', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

-- Operaciones (hijas antes que padres cuando sea posible; FK_CHECKS=0 permite cualquier orden)
SET @consulta = (SELECT IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_legacy_partos_nacidos_muertos_causas'), 'SELECT 1',
    IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_partos_nacidos_muertos_causas'),
       'RENAME TABLE porcinos_partos_nacidos_muertos_causas TO porcinos_legacy_partos_nacidos_muertos_causas', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_legacy_partos_momificados_causas'), 'SELECT 1',
    IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_partos_momificados_causas'),
       'RENAME TABLE porcinos_partos_momificados_causas TO porcinos_legacy_partos_momificados_causas', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_legacy_transferencias_corral'), 'SELECT 1',
    IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_transferencias_corral'),
       'RENAME TABLE porcinos_transferencias_corral TO porcinos_legacy_transferencias_corral', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_legacy_reabsorciones'), 'SELECT 1',
    IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_reabsorciones'),
       'RENAME TABLE porcinos_reabsorciones TO porcinos_legacy_reabsorciones', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_legacy_eventos_sanitarios'), 'SELECT 1',
    IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_eventos_sanitarios'),
       'RENAME TABLE porcinos_eventos_sanitarios TO porcinos_legacy_eventos_sanitarios', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_legacy_registros_peso'), 'SELECT 1',
    IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_registros_peso'),
       'RENAME TABLE porcinos_registros_peso TO porcinos_legacy_registros_peso', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_legacy_muertes_recria'), 'SELECT 1',
    IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_muertes_recria'),
       'RENAME TABLE porcinos_muertes_recria TO porcinos_legacy_muertes_recria', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_legacy_muertes_lactancia'), 'SELECT 1',
    IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_muertes_lactancia'),
       'RENAME TABLE porcinos_muertes_lactancia TO porcinos_legacy_muertes_lactancia', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_legacy_muertes_madres'), 'SELECT 1',
    IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_muertes_madres'),
       'RENAME TABLE porcinos_muertes_madres TO porcinos_legacy_muertes_madres', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_legacy_movimientos_etapas'), 'SELECT 1',
    IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_movimientos_etapas'),
       'RENAME TABLE porcinos_movimientos_etapas TO porcinos_legacy_movimientos_etapas', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_legacy_consumos_alimento'), 'SELECT 1',
    IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_consumos_alimento'),
       'RENAME TABLE porcinos_consumos_alimento TO porcinos_legacy_consumos_alimento', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_legacy_ventas_porcinos'), 'SELECT 1',
    IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_ventas_porcinos'),
       'RENAME TABLE porcinos_ventas_porcinos TO porcinos_legacy_ventas_porcinos', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_legacy_faena'), 'SELECT 1',
    IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_faena'),
       'RENAME TABLE porcinos_faena TO porcinos_legacy_faena', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_legacy_transferencias_lechones'), 'SELECT 1',
    IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_transferencias_lechones'),
       'RENAME TABLE porcinos_transferencias_lechones TO porcinos_legacy_transferencias_lechones', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_legacy_lechones_nn'), 'SELECT 1',
    IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_lechones_nn'),
       'RENAME TABLE porcinos_lechones_nn TO porcinos_legacy_lechones_nn', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_legacy_stock_alimento'), 'SELECT 1',
    IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_stock_alimento'),
       'RENAME TABLE porcinos_stock_alimento TO porcinos_legacy_stock_alimento', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_legacy_historial_estados_madres'), 'SELECT 1',
    IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_historial_estados_madres'),
       'RENAME TABLE porcinos_historial_estados_madres TO porcinos_legacy_historial_estados_madres', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_legacy_chequeos_gestacion'), 'SELECT 1',
    IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_chequeos_gestacion'),
       'RENAME TABLE porcinos_chequeos_gestacion TO porcinos_legacy_chequeos_gestacion', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_legacy_destetes'), 'SELECT 1',
    IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_destetes'),
       'RENAME TABLE porcinos_destetes TO porcinos_legacy_destetes', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_legacy_partos'), 'SELECT 1',
    IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_partos'),
       'RENAME TABLE porcinos_partos TO porcinos_legacy_partos', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_legacy_gestacion'), 'SELECT 1',
    IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_gestacion'),
       'RENAME TABLE porcinos_gestacion TO porcinos_legacy_gestacion', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_legacy_servicios'), 'SELECT 1',
    IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_servicios'),
       'RENAME TABLE porcinos_servicios TO porcinos_legacy_servicios', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_legacy_recria'), 'SELECT 1',
    IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_recria'),
       'RENAME TABLE porcinos_recria TO porcinos_legacy_recria', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_legacy_padrillos'), 'SELECT 1',
    IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_padrillos'),
       'RENAME TABLE porcinos_padrillos TO porcinos_legacy_padrillos', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_legacy_madres'), 'SELECT 1',
    IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_madres'),
       'RENAME TABLE porcinos_madres TO porcinos_legacy_madres', 'SELECT 1')));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET FOREIGN_KEY_CHECKS = 1;
