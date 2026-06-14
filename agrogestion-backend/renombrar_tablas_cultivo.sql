-- Renombrar tablas del módulo Cultivo
USE agrocloud;

RENAME TABLE campos TO cultivo_campos;
RENAME TABLE lotes TO cultivo_lotes;
RENAME TABLE cultivos TO cultivo_cultivos;
RENAME TABLE labores TO cultivo_labores;
RENAME TABLE labor_maquinaria TO cultivo_labor_maquinaria;
RENAME TABLE labor_insumos TO cultivo_labor_insumos;
RENAME TABLE labor_mano_obra TO cultivo_labor_mano_obra;
RENAME TABLE historial_cosechas TO cultivo_historial_cosechas;
RENAME TABLE insumos TO cultivo_insumos;
RENAME TABLE dosis_insumos TO cultivo_dosis_insumos;
RENAME TABLE aplicaciones_agroquimicas TO cultivo_aplicaciones_agroquimicas;
RENAME TABLE dosis_aplicacion TO cultivo_dosis_aplicacion;
RENAME TABLE inventario_granos TO cultivo_inventario_granos;
RENAME TABLE movimientos_inventario_granos TO cultivo_movimientos_inventario_granos;
RENAME TABLE movimientos_inventario TO cultivo_movimientos_inventario;
RENAME TABLE maquinaria TO cultivo_maquinaria;
RENAME TABLE mantenimientos_maquinaria TO cultivo_mantenimientos_maquinaria;
RENAME TABLE alquiler_maquinaria TO cultivo_alquiler_maquinaria;
RENAME TABLE ingresos TO cultivo_ingresos;
RENAME TABLE egresos TO cultivo_egresos;



