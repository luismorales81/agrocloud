-- Copiar nombres desde la tabla legacy `insumos` si todavía existe.
SET @esquema = DATABASE();
SET @tiene_vieja = (SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA=@esquema AND TABLE_NAME='insumos');
SET @tiene_nueva = (SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA=@esquema AND TABLE_NAME='cultivo_insumos');

SET @sql = IF(@tiene_vieja=0 OR @tiene_nueva=0, 'SELECT 1',
    'INSERT INTO cultivo_insumos (nombre, tipo, unidad_medida, precio_unitario, stock_minimo, stock_actual, activo, fecha_creacion, fecha_actualizacion) '
    'SELECT i.nombre, COALESCE(i.tipo, ''OTROS''), ''kg'', 1, 0, 0, TRUE, NOW(), NOW() FROM insumos i '
    'WHERE NOT EXISTS (SELECT 1 FROM cultivo_insumos c WHERE c.nombre = i.nombre)');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;
