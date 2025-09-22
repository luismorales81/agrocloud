-- Verificar estructura actual de la tabla labores
DESCRIBE labores;

-- Ver datos actuales
SELECT * FROM labores ORDER BY id DESC LIMIT 5;

-- Verificar si existen columnas innecesarias
SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE, COLUMN_DEFAULT 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'labores' 
AND TABLE_SCHEMA = DATABASE();

-- Si existe una columna 'empresa' que no se usa, removerla
-- ALTER TABLE labores DROP COLUMN IF EXISTS empresa;

-- Verificar que la columna usuario_id esté presente y sea correcta
-- (Esta columna es necesaria según la entidad Labor)

-- Verificar que la columna lote_id esté presente
-- (Esta columna es necesaria para la relación con Plot)

-- Mostrar estructura final
DESCRIBE labores;


