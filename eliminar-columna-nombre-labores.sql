-- Eliminar columna nombre de la tabla labores
ALTER TABLE labores DROP COLUMN nombre;

-- Verificar estructura final
DESCRIBE labores;

-- Ver datos actuales
SELECT * FROM labores ORDER BY id DESC LIMIT 3;
