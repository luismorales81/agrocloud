-- Eliminar columnas innecesarias de la tabla labor_maquinaria
-- Eliminar columna tipo
ALTER TABLE labor_maquinaria DROP COLUMN tipo;

-- Eliminar columna horas_uso
ALTER TABLE labor_maquinaria DROP COLUMN horas_uso;

-- Eliminar columna kilometros_recorridos
ALTER TABLE labor_maquinaria DROP COLUMN kilometros_recorridos;

-- Verificar estructura final
DESCRIBE labor_maquinaria;

-- Ver datos actuales
SELECT * FROM labor_maquinaria LIMIT 5;
