-- Script de migración para agregar columnas de polígono y coordenadas
-- Ejecutar en la base de datos MySQL existente (agroclouddb)

USE agroclouddb;

-- Verificar si las columnas ya existen antes de agregarlas
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
     WHERE TABLE_SCHEMA = 'agroclouddb' 
     AND TABLE_NAME = 'campos' 
     AND COLUMN_NAME = 'poligono') = 0,
    'ALTER TABLE campos ADD COLUMN poligono TEXT NULL COMMENT ''Polígono del campo en formato GeoJSON'';',
    'SELECT ''Columna poligono ya existe'' as message;'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
     WHERE TABLE_SCHEMA = 'agroclouddb' 
     AND TABLE_NAME = 'campos' 
     AND COLUMN_NAME = 'coordenadas') = 0,
    'ALTER TABLE campos ADD COLUMN coordenadas JSON NULL COMMENT ''Coordenadas del campo como array de puntos lat/lng'';',
    'SELECT ''Columna coordenadas ya existe'' as message;'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Verificar si las columnas estado y activo existen, si no las agregamos
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
     WHERE TABLE_SCHEMA = 'agroclouddb' 
     AND TABLE_NAME = 'campos' 
     AND COLUMN_NAME = 'estado') = 0,
    'ALTER TABLE campos ADD COLUMN estado VARCHAR(100) DEFAULT ''ACTIVO'';',
    'SELECT ''Columna estado ya existe'' as message;'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
     WHERE TABLE_SCHEMA = 'agroclouddb' 
     AND TABLE_NAME = 'campos' 
     AND COLUMN_NAME = 'activo') = 0,
    'ALTER TABLE campos ADD COLUMN activo BOOLEAN DEFAULT TRUE;',
    'SELECT ''Columna activo ya existe'' as message;'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Verificar si la columna user_id existe, si no la agregamos
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
     WHERE TABLE_SCHEMA = 'agroclouddb' 
     AND TABLE_NAME = 'campos' 
     AND COLUMN_NAME = 'user_id') = 0,
    'ALTER TABLE campos ADD COLUMN user_id BIGINT, ADD FOREIGN KEY (user_id) REFERENCES usuarios(id) ON DELETE SET NULL;',
    'SELECT ''Columna user_id ya existe'' as message;'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Agregar índices si no existen
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
     WHERE TABLE_SCHEMA = 'agroclouddb' 
     AND TABLE_NAME = 'campos' 
     AND INDEX_NAME = 'idx_campos_poligono') = 0,
    'CREATE INDEX idx_campos_poligono ON campos(poligono(100));',
    'SELECT ''Índice idx_campos_poligono ya existe'' as message;'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
     WHERE TABLE_SCHEMA = 'agroclouddb' 
     AND TABLE_NAME = 'campos' 
     AND INDEX_NAME = 'idx_campos_coordenadas') = 0,
         'CREATE INDEX idx_campos_coordenadas ON campos(coordenadas(100));',
    'SELECT ''Índice idx_campos_coordenadas ya existe'' as message;'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Mostrar el estado final de la tabla campos
SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    IS_NULLABLE,
    COLUMN_DEFAULT,
    COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'agroclouddb' 
AND TABLE_NAME = 'campos'
ORDER BY ORDINAL_POSITION;

-- Comentario sobre la migración
-- Esta migración agrega soporte para almacenar la geometría de los campos
-- permitiendo dibujar polígonos en Google Maps y almacenar las coordenadas
-- para uso en otros sistemas de gestión agrícola.
