-- Migración para asociar cultivos a lotes existentes (defensiva / idempotente)
-- Omite pasos si no hay lotes con cultivo_actual o faltan columnas.

SET @esquema = DATABASE();

SET @tiene_cultivo_id = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'cultivo_lotes' AND COLUMN_NAME = 'cultivo_id'
);
SET @tiene_cultivo_actual = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'cultivo_lotes' AND COLUMN_NAME = 'cultivo_actual'
);
SET @tiene_empresa_cultivo = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'cultivo_cultivos' AND COLUMN_NAME = 'empresa_id'
);
SET @tiene_empresa_campo = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'cultivo_campos' AND COLUMN_NAME = 'empresa_id'
);
SET @tiene_tipo_cultivo_id = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'cultivo_lotes' AND COLUMN_NAME = 'tipo_cultivo_id'
);
SET @tiene_tabla_tipos = (
    SELECT COUNT(*) FROM information_schema.tables
    WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'cultivo_tipos_cultivo'
);

SET @hay_lotes_pendientes = IF(
    @tiene_cultivo_id = 0 OR @tiene_cultivo_actual = 0,
    0,
    (SELECT COUNT(*) FROM cultivo_lotes
     WHERE cultivo_actual IS NOT NULL AND cultivo_actual != '' AND cultivo_id IS NULL)
);

-- Paso 1
SET @sql = IF(
    @hay_lotes_pendientes = 0,
    'SELECT 1',
    IF(
        @tiene_empresa_cultivo > 0 AND @tiene_empresa_campo > 0,
        'UPDATE cultivo_lotes l INNER JOIN cultivo_campos campo ON campo.id = l.campo_id INNER JOIN empresas e ON e.id = campo.empresa_id INNER JOIN cultivo_cultivos c ON c.nombre COLLATE utf8mb4_unicode_ci = l.cultivo_actual COLLATE utf8mb4_unicode_ci AND (c.empresa_id = e.id OR c.empresa_id IS NULL) SET l.cultivo_id = c.id WHERE l.cultivo_actual IS NOT NULL AND l.cultivo_actual != '''' AND l.cultivo_id IS NULL',
        'UPDATE cultivo_lotes l INNER JOIN cultivo_cultivos c ON c.nombre COLLATE utf8mb4_unicode_ci = l.cultivo_actual COLLATE utf8mb4_unicode_ci SET l.cultivo_id = c.id WHERE l.cultivo_actual IS NOT NULL AND l.cultivo_actual != '''' AND l.cultivo_id IS NULL'
    )
);
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @hay_lotes_pendientes = IF(
    @tiene_cultivo_id = 0 OR @tiene_cultivo_actual = 0,
    0,
    (SELECT COUNT(*) FROM cultivo_lotes
     WHERE cultivo_actual IS NOT NULL AND cultivo_actual != '' AND cultivo_id IS NULL)
);

-- Paso 2
SET @sql = IF(
    @hay_lotes_pendientes = 0,
    'SELECT 1',
    IF(
        @tiene_empresa_cultivo > 0 AND @tiene_empresa_campo > 0,
        'UPDATE cultivo_lotes l INNER JOIN (SELECT l2.id AS lote_id, MIN(c.id) AS cultivo_elegido_id FROM cultivo_lotes l2 INNER JOIN cultivo_campos campo ON campo.id = l2.campo_id INNER JOIN empresas e ON e.id = campo.empresa_id INNER JOIN cultivo_cultivos c ON c.nombre COLLATE utf8mb4_unicode_ci LIKE CONCAT(''%'', l2.cultivo_actual, ''%'') COLLATE utf8mb4_unicode_ci AND (c.empresa_id = e.id OR c.empresa_id IS NULL) WHERE l2.cultivo_actual IS NOT NULL AND l2.cultivo_actual != '''' AND l2.cultivo_id IS NULL GROUP BY l2.id) coincidencias ON coincidencias.lote_id = l.id SET l.cultivo_id = coincidencias.cultivo_elegido_id',
        'UPDATE cultivo_lotes l INNER JOIN (SELECT l2.id AS lote_id, MIN(c.id) AS cultivo_elegido_id FROM cultivo_lotes l2 INNER JOIN cultivo_cultivos c ON c.nombre COLLATE utf8mb4_unicode_ci LIKE CONCAT(''%'', l2.cultivo_actual, ''%'') COLLATE utf8mb4_unicode_ci WHERE l2.cultivo_actual IS NOT NULL AND l2.cultivo_actual != '''' AND l2.cultivo_id IS NULL GROUP BY l2.id) coincidencias ON coincidencias.lote_id = l.id SET l.cultivo_id = coincidencias.cultivo_elegido_id'
    )
);
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @hay_tipos_pendientes = IF(
    @tiene_cultivo_id = 0 OR @tiene_tipo_cultivo_id = 0 OR @tiene_tabla_tipos = 0,
    0,
    (SELECT COUNT(*) FROM cultivo_lotes WHERE cultivo_id IS NOT NULL AND tipo_cultivo_id IS NULL)
);

-- Paso 3
SET @sql = IF(
    @hay_tipos_pendientes = 0,
    'SELECT 1',
    'UPDATE cultivo_lotes l INNER JOIN cultivo_cultivos c ON c.id = l.cultivo_id INNER JOIN cultivo_tipos_cultivo tc ON tc.nombre COLLATE utf8mb4_unicode_ci = c.nombre COLLATE utf8mb4_unicode_ci SET l.tipo_cultivo_id = tc.id WHERE l.cultivo_id IS NOT NULL AND l.tipo_cultivo_id IS NULL AND tc.activo = true'
);
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SELECT 'Migración V1_135 asociar cultivos a lotes aplicada (modo defensivo)' AS mensaje;
