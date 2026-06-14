-- Renombrar tablas del módulo Porcinos
USE agrocloud;

RENAME TABLE madres TO porcinos_madres;
RENAME TABLE padrillos TO porcinos_padrillos;
RENAME TABLE servicios TO porcinos_servicios;
RENAME TABLE gestacion TO porcinos_gestacion;
RENAME TABLE chequeos_gestacion TO porcinos_chequeos_gestacion;
RENAME TABLE partos TO porcinos_partos;
RENAME TABLE destetes TO porcinos_destetes;
RENAME TABLE recria TO porcinos_recria;
RENAME TABLE movimientos_etapas TO porcinos_movimientos_etapas;
RENAME TABLE registros_peso TO porcinos_registros_peso;
RENAME TABLE muertes_lactancia TO porcinos_muertes_lactancia;
RENAME TABLE muertes_recria TO porcinos_muertes_recria;
RENAME TABLE muertes_madres TO porcinos_muertes_madres;
RENAME TABLE consumos_alimento TO porcinos_consumos_alimento;
RENAME TABLE ventas_porcinos TO porcinos_ventas_porcinos;
RENAME TABLE faena TO porcinos_faena;
RENAME TABLE lechones_nn TO porcinos_lechones_nn;
RENAME TABLE transferencias_lechones TO porcinos_transferencias_lechones;
RENAME TABLE stock_alimento TO porcinos_stock_alimento;
RENAME TABLE historial_estados_madres TO porcinos_historial_estados_madres;
RENAME TABLE razas_porcinos TO porcinos_razas_porcinos;
RENAME TABLE tipos_alimento_porcinos TO porcinos_tipos_alimento_porcinos;
RENAME TABLE tipos_servicio_porcinos TO porcinos_tipos_servicio_porcinos;
RENAME TABLE causas_mortalidad_porcinos TO porcinos_causas_mortalidad_porcinos;
RENAME TABLE motivos_baja_porcinos TO porcinos_motivos_baja_porcinos;
RENAME TABLE esquemas_sanitarios_porcinos TO porcinos_esquemas_sanitarios_porcinos;
RENAME TABLE tipos_corral_porcinos TO porcinos_tipos_corral_porcinos;
RENAME TABLE parametros_establecimiento_porcinos TO porcinos_parametros_establecimiento_porcinos;
RENAME TABLE parametros_productivos_porcinos TO porcinos_parametros_productivos_porcinos;
RENAME TABLE datos_economicos_porcinos TO porcinos_datos_economicos_porcinos;
RENAME TABLE configuraciones_porcinos TO porcinos_configuraciones_porcinos;



