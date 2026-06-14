-- Script seguro para ejecutar el renombrado de tablas
-- Solo renombra las tablas que existen
-- Ejecutar este script en MySQL Workbench o desde la línea de comandos de MySQL

USE agrocloud;

-- ============================================================================
-- MÓDULO CULTIVO
-- ============================================================================

-- Verificar y renombrar tablas del módulo Cultivo (solo si existen)
SET @sql = '';
SELECT IF(COUNT(*) > 0, 'RENAME TABLE campos TO cultivo_campos;', '') INTO @sql FROM information_schema.tables WHERE table_schema = 'agrocloud' AND table_name = 'campos';
SET @sql = CONCAT(@sql, IF(CHAR_LENGTH(@sql) > 0, ' ', ''), IF((SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'agrocloud' AND table_name = 'lotes') > 0, 'RENAME TABLE lotes TO cultivo_lotes;', ''));
SET @sql = CONCAT(@sql, IF(CHAR_LENGTH(@sql) > 0, ' ', ''), IF((SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'agrocloud' AND table_name = 'cultivos') > 0, 'RENAME TABLE cultivos TO cultivo_cultivos;', ''));
SET @sql = CONCAT(@sql, IF(CHAR_LENGTH(@sql) > 0, ' ', ''), IF((SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'agrocloud' AND table_name = 'labores') > 0, 'RENAME TABLE labores TO cultivo_labores;', ''));

-- Ejecutar comandos individuales para mejor manejo de errores
-- Campos
SELECT IF(COUNT(*) > 0, 'RENAME TABLE campos TO cultivo_campos;', 'SELECT "Tabla campos no existe, omitiendo";') INTO @sql FROM information_schema.tables WHERE table_schema = 'agrocloud' AND table_name = 'campos';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Lotes
SELECT IF(COUNT(*) > 0, 'RENAME TABLE lotes TO cultivo_lotes;', 'SELECT "Tabla lotes no existe, omitiendo";') INTO @sql FROM information_schema.tables WHERE table_schema = 'agrocloud' AND table_name = 'lotes';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Cultivos
SELECT IF(COUNT(*) > 0, 'RENAME TABLE cultivos TO cultivo_cultivos;', 'SELECT "Tabla cultivos no existe, omitiendo";') INTO @sql FROM information_schema.tables WHERE table_schema = 'agrocloud' AND table_name = 'cultivos';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Labores
SELECT IF(COUNT(*) > 0, 'RENAME TABLE labores TO cultivo_labores;', 'SELECT "Tabla labores no existe, omitiendo";') INTO @sql FROM information_schema.tables WHERE table_schema = 'agrocloud' AND table_name = 'labores';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Labor Maquinaria
SELECT IF(COUNT(*) > 0, 'RENAME TABLE labor_maquinaria TO cultivo_labor_maquinaria;', 'SELECT "Tabla labor_maquinaria no existe, omitiendo";') INTO @sql FROM information_schema.tables WHERE table_schema = 'agrocloud' AND table_name = 'labor_maquinaria';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Labor Insumos
SELECT IF(COUNT(*) > 0, 'RENAME TABLE labor_insumos TO cultivo_labor_insumos;', 'SELECT "Tabla labor_insumos no existe, omitiendo";') INTO @sql FROM information_schema.tables WHERE table_schema = 'agrocloud' AND table_name = 'labor_insumos';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Labor Mano Obra
SELECT IF(COUNT(*) > 0, 'RENAME TABLE labor_mano_obra TO cultivo_labor_mano_obra;', 'SELECT "Tabla labor_mano_obra no existe, omitiendo";') INTO @sql FROM information_schema.tables WHERE table_schema = 'agrocloud' AND table_name = 'labor_mano_obra';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Cosechas (puede no existir)
SELECT IF(COUNT(*) > 0, 'RENAME TABLE cosechas TO cultivo_cosechas;', 'SELECT "Tabla cosechas no existe, omitiendo";') INTO @sql FROM information_schema.tables WHERE table_schema = 'agrocloud' AND table_name = 'cosechas';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Historial Cosechas
SELECT IF(COUNT(*) > 0, 'RENAME TABLE historial_cosechas TO cultivo_historial_cosechas;', 'SELECT "Tabla historial_cosechas no existe, omitiendo";') INTO @sql FROM information_schema.tables WHERE table_schema = 'agrocloud' AND table_name = 'historial_cosechas';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Insumos
SELECT IF(COUNT(*) > 0, 'RENAME TABLE insumos TO cultivo_insumos;', 'SELECT "Tabla insumos no existe, omitiendo";') INTO @sql FROM information_schema.tables WHERE table_schema = 'agrocloud' AND table_name = 'insumos';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Dosis Insumos
SELECT IF(COUNT(*) > 0, 'RENAME TABLE dosis_insumos TO cultivo_dosis_insumos;', 'SELECT "Tabla dosis_insumos no existe, omitiendo";') INTO @sql FROM information_schema.tables WHERE table_schema = 'agrocloud' AND table_name = 'dosis_insumos';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Aplicaciones Agroquimicas
SELECT IF(COUNT(*) > 0, 'RENAME TABLE aplicaciones_agroquimicas TO cultivo_aplicaciones_agroquimicas;', 'SELECT "Tabla aplicaciones_agroquimicas no existe, omitiendo";') INTO @sql FROM information_schema.tables WHERE table_schema = 'agrocloud' AND table_name = 'aplicaciones_agroquimicas';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Dosis Aplicacion
SELECT IF(COUNT(*) > 0, 'RENAME TABLE dosis_aplicacion TO cultivo_dosis_aplicacion;', 'SELECT "Tabla dosis_aplicacion no existe, omitiendo";') INTO @sql FROM information_schema.tables WHERE table_schema = 'agrocloud' AND table_name = 'dosis_aplicacion';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Inventario Granos
SELECT IF(COUNT(*) > 0, 'RENAME TABLE inventario_granos TO cultivo_inventario_granos;', 'SELECT "Tabla inventario_granos no existe, omitiendo";') INTO @sql FROM information_schema.tables WHERE table_schema = 'agrocloud' AND table_name = 'inventario_granos';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Movimientos Inventario Granos
SELECT IF(COUNT(*) > 0, 'RENAME TABLE movimientos_inventario_granos TO cultivo_movimientos_inventario_granos;', 'SELECT "Tabla movimientos_inventario_granos no existe, omitiendo";') INTO @sql FROM information_schema.tables WHERE table_schema = 'agrocloud' AND table_name = 'movimientos_inventario_granos';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Movimientos Inventario
SELECT IF(COUNT(*) > 0, 'RENAME TABLE movimientos_inventario TO cultivo_movimientos_inventario;', 'SELECT "Tabla movimientos_inventario no existe, omitiendo";') INTO @sql FROM information_schema.tables WHERE table_schema = 'agrocloud' AND table_name = 'movimientos_inventario';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Maquinaria
SELECT IF(COUNT(*) > 0, 'RENAME TABLE maquinaria TO cultivo_maquinaria;', 'SELECT "Tabla maquinaria no existe, omitiendo";') INTO @sql FROM information_schema.tables WHERE table_schema = 'agrocloud' AND table_name = 'maquinaria';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Mantenimientos Maquinaria
SELECT IF(COUNT(*) > 0, 'RENAME TABLE mantenimientos_maquinaria TO cultivo_mantenimientos_maquinaria;', 'SELECT "Tabla mantenimientos_maquinaria no existe, omitiendo";') INTO @sql FROM information_schema.tables WHERE table_schema = 'agrocloud' AND table_name = 'mantenimientos_maquinaria';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Alquiler Maquinaria
SELECT IF(COUNT(*) > 0, 'RENAME TABLE alquiler_maquinaria TO cultivo_alquiler_maquinaria;', 'SELECT "Tabla alquiler_maquinaria no existe, omitiendo";') INTO @sql FROM information_schema.tables WHERE table_schema = 'agrocloud' AND table_name = 'alquiler_maquinaria';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Ingresos
SELECT IF(COUNT(*) > 0, 'RENAME TABLE ingresos TO cultivo_ingresos;', 'SELECT "Tabla ingresos no existe, omitiendo";') INTO @sql FROM information_schema.tables WHERE table_schema = 'agrocloud' AND table_name = 'ingresos';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Egresos
SELECT IF(COUNT(*) > 0, 'RENAME TABLE egresos TO cultivo_egresos;', 'SELECT "Tabla egresos no existe, omitiendo";') INTO @sql FROM information_schema.tables WHERE table_schema = 'agrocloud' AND table_name = 'egresos';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ============================================================================
-- MÓDULO PORCINOS
-- ============================================================================

-- Madres
SELECT IF(COUNT(*) > 0, 'RENAME TABLE madres TO porcinos_madres;', 'SELECT "Tabla madres no existe, omitiendo";') INTO @sql FROM information_schema.tables WHERE table_schema = 'agrocloud' AND table_name = 'madres';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Padrillos
SELECT IF(COUNT(*) > 0, 'RENAME TABLE padrillos TO porcinos_padrillos;', 'SELECT "Tabla padrillos no existe, omitiendo";') INTO @sql FROM information_schema.tables WHERE table_schema = 'agrocloud' AND table_name = 'padrillos';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Servicios
SELECT IF(COUNT(*) > 0, 'RENAME TABLE servicios TO porcinos_servicios;', 'SELECT "Tabla servicios no existe, omitiendo";') INTO @sql FROM information_schema.tables WHERE table_schema = 'agrocloud' AND table_name = 'servicios';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Gestacion
SELECT IF(COUNT(*) > 0, 'RENAME TABLE gestacion TO porcinos_gestacion;', 'SELECT "Tabla gestacion no existe, omitiendo";') INTO @sql FROM information_schema.tables WHERE table_schema = 'agrocloud' AND table_name = 'gestacion';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Chequeos Gestacion
SELECT IF(COUNT(*) > 0, 'RENAME TABLE chequeos_gestacion TO porcinos_chequeos_gestacion;', 'SELECT "Tabla chequeos_gestacion no existe, omitiendo";') INTO @sql FROM information_schema.tables WHERE table_schema = 'agrocloud' AND table_name = 'chequeos_gestacion';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Partos
SELECT IF(COUNT(*) > 0, 'RENAME TABLE partos TO porcinos_partos;', 'SELECT "Tabla partos no existe, omitiendo";') INTO @sql FROM information_schema.tables WHERE table_schema = 'agrocloud' AND table_name = 'partos';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Destetes
SELECT IF(COUNT(*) > 0, 'RENAME TABLE destetes TO porcinos_destetes;', 'SELECT "Tabla destetes no existe, omitiendo";') INTO @sql FROM information_schema.tables WHERE table_schema = 'agrocloud' AND table_name = 'destetes';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Recria
SELECT IF(COUNT(*) > 0, 'RENAME TABLE recria TO porcinos_recria;', 'SELECT "Tabla recria no existe, omitiendo";') INTO @sql FROM information_schema.tables WHERE table_schema = 'agrocloud' AND table_name = 'recria';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Movimientos Etapas
SELECT IF(COUNT(*) > 0, 'RENAME TABLE movimientos_etapas TO porcinos_movimientos_etapas;', 'SELECT "Tabla movimientos_etapas no existe, omitiendo";') INTO @sql FROM information_schema.tables WHERE table_schema = 'agrocloud' AND table_name = 'movimientos_etapas';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Registros Peso
SELECT IF(COUNT(*) > 0, 'RENAME TABLE registros_peso TO porcinos_registros_peso;', 'SELECT "Tabla registros_peso no existe, omitiendo";') INTO @sql FROM information_schema.tables WHERE table_schema = 'agrocloud' AND table_name = 'registros_peso';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Muertes Lactancia
SELECT IF(COUNT(*) > 0, 'RENAME TABLE muertes_lactancia TO porcinos_muertes_lactancia;', 'SELECT "Tabla muertes_lactancia no existe, omitiendo";') INTO @sql FROM information_schema.tables WHERE table_schema = 'agrocloud' AND table_name = 'muertes_lactancia';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Muertes Recria
SELECT IF(COUNT(*) > 0, 'RENAME TABLE muertes_recria TO porcinos_muertes_recria;', 'SELECT "Tabla muertes_recria no existe, omitiendo";') INTO @sql FROM information_schema.tables WHERE table_schema = 'agrocloud' AND table_name = 'muertes_recria';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Muertes Madres
SELECT IF(COUNT(*) > 0, 'RENAME TABLE muertes_madres TO porcinos_muertes_madres;', 'SELECT "Tabla muertes_madres no existe, omitiendo";') INTO @sql FROM information_schema.tables WHERE table_schema = 'agrocloud' AND table_name = 'muertes_madres';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Consumos Alimento
SELECT IF(COUNT(*) > 0, 'RENAME TABLE consumos_alimento TO porcinos_consumos_alimento;', 'SELECT "Tabla consumos_alimento no existe, omitiendo";') INTO @sql FROM information_schema.tables WHERE table_schema = 'agrocloud' AND table_name = 'consumos_alimento';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Ventas Porcinos
SELECT IF(COUNT(*) > 0, 'RENAME TABLE ventas_porcinos TO porcinos_ventas_porcinos;', 'SELECT "Tabla ventas_porcinos no existe, omitiendo";') INTO @sql FROM information_schema.tables WHERE table_schema = 'agrocloud' AND table_name = 'ventas_porcinos';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Faena
SELECT IF(COUNT(*) > 0, 'RENAME TABLE faena TO porcinos_faena;', 'SELECT "Tabla faena no existe, omitiendo";') INTO @sql FROM information_schema.tables WHERE table_schema = 'agrocloud' AND table_name = 'faena';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Lechones NN
SELECT IF(COUNT(*) > 0, 'RENAME TABLE lechones_nn TO porcinos_lechones_nn;', 'SELECT "Tabla lechones_nn no existe, omitiendo";') INTO @sql FROM information_schema.tables WHERE table_schema = 'agrocloud' AND table_name = 'lechones_nn';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Transferencias Lechones
SELECT IF(COUNT(*) > 0, 'RENAME TABLE transferencias_lechones TO porcinos_transferencias_lechones;', 'SELECT "Tabla transferencias_lechones no existe, omitiendo";') INTO @sql FROM information_schema.tables WHERE table_schema = 'agrocloud' AND table_name = 'transferencias_lechones';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Stock Alimento
SELECT IF(COUNT(*) > 0, 'RENAME TABLE stock_alimento TO porcinos_stock_alimento;', 'SELECT "Tabla stock_alimento no existe, omitiendo";') INTO @sql FROM information_schema.tables WHERE table_schema = 'agrocloud' AND table_name = 'stock_alimento';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Historial Estados Madres
SELECT IF(COUNT(*) > 0, 'RENAME TABLE historial_estados_madres TO porcinos_historial_estados_madres;', 'SELECT "Tabla historial_estados_madres no existe, omitiendo";') INTO @sql FROM information_schema.tables WHERE table_schema = 'agrocloud' AND table_name = 'historial_estados_madres';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Razas Porcinos
SELECT IF(COUNT(*) > 0, 'RENAME TABLE razas_porcinos TO porcinos_razas_porcinos;', 'SELECT "Tabla razas_porcinos no existe, omitiendo";') INTO @sql FROM information_schema.tables WHERE table_schema = 'agrocloud' AND table_name = 'razas_porcinos';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Tipos Alimento Porcinos
SELECT IF(COUNT(*) > 0, 'RENAME TABLE tipos_alimento_porcinos TO porcinos_tipos_alimento_porcinos;', 'SELECT "Tabla tipos_alimento_porcinos no existe, omitiendo";') INTO @sql FROM information_schema.tables WHERE table_schema = 'agrocloud' AND table_name = 'tipos_alimento_porcinos';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Tipos Servicio Porcinos
SELECT IF(COUNT(*) > 0, 'RENAME TABLE tipos_servicio_porcinos TO porcinos_tipos_servicio_porcinos;', 'SELECT "Tabla tipos_servicio_porcinos no existe, omitiendo";') INTO @sql FROM information_schema.tables WHERE table_schema = 'agrocloud' AND table_name = 'tipos_servicio_porcinos';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Causas Mortalidad Porcinos
SELECT IF(COUNT(*) > 0, 'RENAME TABLE causas_mortalidad_porcinos TO porcinos_causas_mortalidad_porcinos;', 'SELECT "Tabla causas_mortalidad_porcinos no existe, omitiendo";') INTO @sql FROM information_schema.tables WHERE table_schema = 'agrocloud' AND table_name = 'causas_mortalidad_porcinos';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Motivos Baja Porcinos
SELECT IF(COUNT(*) > 0, 'RENAME TABLE motivos_baja_porcinos TO porcinos_motivos_baja_porcinos;', 'SELECT "Tabla motivos_baja_porcinos no existe, omitiendo";') INTO @sql FROM information_schema.tables WHERE table_schema = 'agrocloud' AND table_name = 'motivos_baja_porcinos';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Esquemas Sanitarios Porcinos
SELECT IF(COUNT(*) > 0, 'RENAME TABLE esquemas_sanitarios_porcinos TO porcinos_esquemas_sanitarios_porcinos;', 'SELECT "Tabla esquemas_sanitarios_porcinos no existe, omitiendo";') INTO @sql FROM information_schema.tables WHERE table_schema = 'agrocloud' AND table_name = 'esquemas_sanitarios_porcinos';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Tipos Corral Porcinos
SELECT IF(COUNT(*) > 0, 'RENAME TABLE tipos_corral_porcinos TO porcinos_tipos_corral_porcinos;', 'SELECT "Tabla tipos_corral_porcinos no existe, omitiendo";') INTO @sql FROM information_schema.tables WHERE table_schema = 'agrocloud' AND table_name = 'tipos_corral_porcinos';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Parametros Establecimiento Porcinos
SELECT IF(COUNT(*) > 0, 'RENAME TABLE parametros_establecimiento_porcinos TO porcinos_parametros_establecimiento_porcinos;', 'SELECT "Tabla parametros_establecimiento_porcinos no existe, omitiendo";') INTO @sql FROM information_schema.tables WHERE table_schema = 'agrocloud' AND table_name = 'parametros_establecimiento_porcinos';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Parametros Productivos Porcinos
SELECT IF(COUNT(*) > 0, 'RENAME TABLE parametros_productivos_porcinos TO porcinos_parametros_productivos_porcinos;', 'SELECT "Tabla parametros_productivos_porcinos no existe, omitiendo";') INTO @sql FROM information_schema.tables WHERE table_schema = 'agrocloud' AND table_name = 'parametros_productivos_porcinos';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Datos Economicos Porcinos
SELECT IF(COUNT(*) > 0, 'RENAME TABLE datos_economicos_porcinos TO porcinos_datos_economicos_porcinos;', 'SELECT "Tabla datos_economicos_porcinos no existe, omitiendo";') INTO @sql FROM information_schema.tables WHERE table_schema = 'agrocloud' AND table_name = 'datos_economicos_porcinos';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Configuraciones Porcinos
SELECT IF(COUNT(*) > 0, 'RENAME TABLE configuraciones_porcinos TO porcinos_configuraciones_porcinos;', 'SELECT "Tabla configuraciones_porcinos no existe, omitiendo";') INTO @sql FROM information_schema.tables WHERE table_schema = 'agrocloud' AND table_name = 'configuraciones_porcinos';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT 'Migración completada exitosamente' AS resultado;



