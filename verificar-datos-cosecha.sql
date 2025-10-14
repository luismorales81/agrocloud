-- Script para verificar datos de cosecha y diagnosticar problemas
-- Ejecutar en MySQL

-- 1. Verificar estructura de la tabla historial_cosechas
DESCRIBE historial_cosechas;

-- 2. Verificar cultivos disponibles
SELECT id, nombre, variedad, empresa_id, activo 
FROM cultivos 
WHERE activo = 1 
ORDER BY id DESC 
LIMIT 10;

-- 3. Verificar lotes disponibles
SELECT id, nombre, cultivo_actual, estado, area_hectareas, activo 
FROM lotes 
WHERE activo = 1 
ORDER BY id DESC 
LIMIT 10;

-- 4. Verificar usuarios disponibles
SELECT id, username, email, activo 
FROM usuarios 
WHERE activo = 1 
ORDER BY id DESC 
LIMIT 10;

-- 5. Verificar registros en historial_cosechas
SELECT id, lote_id, cultivo_id, usuario_id, cantidad_cosechada, fecha_cosecha
FROM historial_cosechas 
ORDER BY id DESC 
LIMIT 10;

-- 6. Verificar constraints y foreign keys
SELECT 
    CONSTRAINT_NAME,
    TABLE_NAME,
    COLUMN_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM 
    INFORMATION_SCHEMA.KEY_COLUMN_USAGE 
WHERE 
    TABLE_SCHEMA = 'agroclouddb' 
    AND TABLE_NAME = 'historial_cosechas'
    AND REFERENCED_TABLE_NAME IS NOT NULL;

-- 7. Verificar si hay cultivos sin empresa_id
SELECT COUNT(*) as cultivos_sin_empresa 
FROM cultivos 
WHERE empresa_id IS NULL AND activo = 1;

-- 8. Verificar si hay lotes sin cultivo_actual
SELECT COUNT(*) as lotes_sin_cultivo 
FROM lotes 
WHERE (cultivo_actual IS NULL OR cultivo_actual = '') AND activo = 1;

