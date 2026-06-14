-- Spec SDD: Labor como fuente de verdad. Añadir fecha_realizacion (cuando se marca realizada).
-- La fecha planificada se sigue usando desde fecha_inicio.

-- Añadir columna fecha_realizacion (spec SDD: labor como fuente de verdad)
ALTER TABLE cultivo_labores
ADD COLUMN fecha_realizacion DATE NULL COMMENT 'Fecha en que se realizó la labor (estado COMPLETADA)' AFTER fecha_fin;
