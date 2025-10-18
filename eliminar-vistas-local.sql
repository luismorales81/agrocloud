-- Eliminar vistas problemáticas de la base de datos local
USE agrocloud;

DROP VIEW IF EXISTS vista_historial_cosechas_completo;
DROP VIEW IF EXISTS vista_balance_mensual;
DROP VIEW IF EXISTS vista_lotes_con_cultivo;
DROP VIEW IF EXISTS vista_labores_detalladas;
DROP VIEW IF EXISTS vista_inventario_granos_actual;
DROP VIEW IF EXISTS vista_insumos_stock_bajo;
DROP VIEW IF EXISTS vista_maquinaria_mantenimiento;
DROP VIEW IF EXISTS vista_usuarios_empresas;
DROP VIEW IF EXISTS vista_cosechas_por_cultivo;
DROP VIEW IF EXISTS vista_labores_por_tipo;
DROP VIEW IF EXISTS vista_ingresos_egresos_mensual;
DROP VIEW IF EXISTS vista_campos_con_lotes;
DROP VIEW IF EXISTS vista_rendimiento_por_lote;
DROP VIEW IF EXISTS vista_costos_por_labor;
DROP VIEW IF EXISTS vista_inventario_valor_total;

SELECT 'Vistas eliminadas exitosamente' AS mensaje;

