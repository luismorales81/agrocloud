-- Eliminar columna costo_por_hora de la tabla labor_mano_obra
ALTER TABLE labor_mano_obra DROP COLUMN costo_por_hora;

-- Verificar estructura final
DESCRIBE labor_mano_obra;

-- Ver datos actuales
SELECT * FROM labor_mano_obra LIMIT 5;
