-- Script simple para renombrar tablas
-- Ejecuta cada comando individualmente, ignorando errores si la tabla no existe

USE agrocloud;

-- ============================================================================
-- MÓDULO CULTIVO
-- ============================================================================

-- Ejecutar cada RENAME TABLE individualmente (ignorar errores si la tabla no existe)
SET @continue_on_error = 1;

-- Campos
SET @sql = 'RENAME TABLE campos TO cultivo_campos';
SET @continue_on_error = 1;
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Lotes  
SET @sql = 'RENAME TABLE lotes TO cultivo_lotes';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Cultivos
SET @sql = 'RENAME TABLE cultivos TO cultivo_cultivos';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Labores
SET @sql = 'RENAME TABLE labores TO cultivo_labores';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Labor Maquinaria
SET @sql = 'RENAME TABLE labor_maquinaria TO cultivo_labor_maquinaria';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Labor Insumos
SET @sql = 'RENAME TABLE labor_insumos TO cultivo_labor_insumos';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Labor Mano Obra
SET @sql = 'RENAME TABLE labor_mano_obra TO cultivo_labor_mano_obra';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Cosechas (puede no existir - se omite si no existe)
-- SET @sql = 'RENAME TABLE cosechas TO cultivo_cosechas';
-- PREPARE stmt FROM @sql;
-- EXECUTE stmt;
-- DEALLOCATE PREPARE stmt;

-- Historial Cosechas
SET @sql = 'RENAME TABLE historial_cosechas TO cultivo_historial_cosechas';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Insumos
SET @sql = 'RENAME TABLE insumos TO cultivo_insumos';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Dosis Insumos
SET @sql = 'RENAME TABLE dosis_insumos TO cultivo_dosis_insumos';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Aplicaciones Agroquimicas
SET @sql = 'RENAME TABLE aplicaciones_agroquimicas TO cultivo_aplicaciones_agroquimicas';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Dosis Aplicacion
SET @sql = 'RENAME TABLE dosis_aplicacion TO cultivo_dosis_aplicacion';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Inventario Granos
SET @sql = 'RENAME TABLE inventario_granos TO cultivo_inventario_granos';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Movimientos Inventario Granos
SET @sql = 'RENAME TABLE movimientos_inventario_granos TO cultivo_movimientos_inventario_granos';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Movimientos Inventario
SET @sql = 'RENAME TABLE movimientos_inventario TO cultivo_movimientos_inventario';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Maquinaria
SET @sql = 'RENAME TABLE maquinaria TO cultivo_maquinaria';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Mantenimientos Maquinaria
SET @sql = 'RENAME TABLE mantenimientos_maquinaria TO cultivo_mantenimientos_maquinaria';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Alquiler Maquinaria
SET @sql = 'RENAME TABLE alquiler_maquinaria TO cultivo_alquiler_maquinaria';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Ingresos
SET @sql = 'RENAME TABLE ingresos TO cultivo_ingresos';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Egresos
SET @sql = 'RENAME TABLE egresos TO cultivo_egresos';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ============================================================================
-- MÓDULO PORCINOS
-- ============================================================================

-- Madres
SET @sql = 'RENAME TABLE madres TO porcinos_madres';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Padrillos
SET @sql = 'RENAME TABLE padrillos TO porcinos_padrillos';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Servicios
SET @sql = 'RENAME TABLE servicios TO porcinos_servicios';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Gestacion
SET @sql = 'RENAME TABLE gestacion TO porcinos_gestacion';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Chequeos Gestacion
SET @sql = 'RENAME TABLE chequeos_gestacion TO porcinos_chequeos_gestacion';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Partos
SET @sql = 'RENAME TABLE partos TO porcinos_partos';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Destetes
SET @sql = 'RENAME TABLE destetes TO porcinos_destetes';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Recria
SET @sql = 'RENAME TABLE recria TO porcinos_recria';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Movimientos Etapas
SET @sql = 'RENAME TABLE movimientos_etapas TO porcinos_movimientos_etapas';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Registros Peso
SET @sql = 'RENAME TABLE registros_peso TO porcinos_registros_peso';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Muertes Lactancia
SET @sql = 'RENAME TABLE muertes_lactancia TO porcinos_muertes_lactancia';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Muertes Recria
SET @sql = 'RENAME TABLE muertes_recria TO porcinos_muertes_recria';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Muertes Madres
SET @sql = 'RENAME TABLE muertes_madres TO porcinos_muertes_madres';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Consumos Alimento
SET @sql = 'RENAME TABLE consumos_alimento TO porcinos_consumos_alimento';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Ventas Porcinos
SET @sql = 'RENAME TABLE ventas_porcinos TO porcinos_ventas_porcinos';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Faena
SET @sql = 'RENAME TABLE faena TO porcinos_faena';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Lechones NN
SET @sql = 'RENAME TABLE lechones_nn TO porcinos_lechones_nn';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Transferencias Lechones
SET @sql = 'RENAME TABLE transferencias_lechones TO porcinos_transferencias_lechones';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Stock Alimento
SET @sql = 'RENAME TABLE stock_alimento TO porcinos_stock_alimento';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Historial Estados Madres
SET @sql = 'RENAME TABLE historial_estados_madres TO porcinos_historial_estados_madres';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Razas Porcinos
SET @sql = 'RENAME TABLE razas_porcinos TO porcinos_razas_porcinos';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Tipos Alimento Porcinos
SET @sql = 'RENAME TABLE tipos_alimento_porcinos TO porcinos_tipos_alimento_porcinos';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Tipos Servicio Porcinos
SET @sql = 'RENAME TABLE tipos_servicio_porcinos TO porcinos_tipos_servicio_porcinos';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Causas Mortalidad Porcinos
SET @sql = 'RENAME TABLE causas_mortalidad_porcinos TO porcinos_causas_mortalidad_porcinos';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Motivos Baja Porcinos
SET @sql = 'RENAME TABLE motivos_baja_porcinos TO porcinos_motivos_baja_porcinos';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Esquemas Sanitarios Porcinos
SET @sql = 'RENAME TABLE esquemas_sanitarios_porcinos TO porcinos_esquemas_sanitarios_porcinos';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Tipos Corral Porcinos
SET @sql = 'RENAME TABLE tipos_corral_porcinos TO porcinos_tipos_corral_porcinos';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Parametros Establecimiento Porcinos
SET @sql = 'RENAME TABLE parametros_establecimiento_porcinos TO porcinos_parametros_establecimiento_porcinos';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Parametros Productivos Porcinos
SET @sql = 'RENAME TABLE parametros_productivos_porcinos TO porcinos_parametros_productivos_porcinos';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Datos Economicos Porcinos
SET @sql = 'RENAME TABLE datos_economicos_porcinos TO porcinos_datos_economicos_porcinos';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Configuraciones Porcinos
SET @sql = 'RENAME TABLE configuraciones_porcinos TO porcinos_configuraciones_porcinos';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT 'Migración completada' AS resultado;



