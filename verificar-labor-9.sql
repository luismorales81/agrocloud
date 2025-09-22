-- Verificar datos de la labor ID 9
SELECT 'LABOR 9' as tipo, id, tipo_labor, descripcion, fecha_inicio, estado, costo_total 
FROM labores 
WHERE id = 9;

SELECT 'MAQUINARIA LABOR 9' as tipo, id_labor_maquinaria, id_labor, descripcion, costo 
FROM labor_maquinaria 
WHERE id_labor = 9;

SELECT 'MANO OBRA LABOR 9' as tipo, id_labor_mano_obra, id_labor, descripcion, costo_total 
FROM labor_mano_obra 
WHERE id_labor = 9;
