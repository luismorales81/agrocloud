-- Verificar estructura actual de la tabla labores
DESCRIBE labores;

-- Verificar si existen las columnas empresa_id y user_id
SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE, COLUMN_DEFAULT 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'labores' 
AND TABLE_SCHEMA = DATABASE()
AND COLUMN_NAME IN ('empresa_id', 'user_id', 'usuario_id');

-- Ver datos actuales para verificar qué columnas están siendo usadas
SELECT * FROM labores ORDER BY id DESC LIMIT 3;

-- Eliminar columnas innecesarias
-- (Las columnas empresa_id y user_id están vacías y no se usan)

-- Eliminar columna empresa_id
ALTER TABLE labores DROP COLUMN empresa_id;

-- Eliminar columna user_id (mantener usuario_id que es la correcta)
ALTER TABLE labores DROP COLUMN user_id;

-- Verificar estructura final
DESCRIBE labores;

-- Verificar que los datos siguen intactos
SELECT id, tipo_labor, nombre, descripcion, fecha_inicio, fecha_fin, estado, costo_total, lote_id, usuario_id 
FROM labores 
ORDER BY id DESC 
LIMIT 3;

