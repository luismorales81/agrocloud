-- Eliminar el campo 'unidad' de la tabla dosis_insumos
-- La unidad ahora se deriva automáticamente del campo unidad_medida del insumo relacionado
-- Esto elimina redundancia y garantiza consistencia de datos

ALTER TABLE dosis_insumos 
DROP COLUMN unidad;

