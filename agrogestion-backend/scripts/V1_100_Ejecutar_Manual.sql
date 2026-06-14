-- ============================================================================
-- MIGRACIÓN V1_100: Agregar campo macho_nombre a la tabla porcinos_servicios
-- ============================================================================
-- Este script puede ejecutarse manualmente en MySQL
-- Ejecutar desde la línea de comandos:
-- mysql -u root -p123456 agrocloud < scripts/V1_100_Ejecutar_Manual.sql
-- O ejecutar directamente en MySQL:
-- USE agrocloud;
-- SOURCE scripts/V1_100_Ejecutar_Manual.sql;

USE agrocloud;

-- Agregar campo macho_nombre a la tabla servicios para inseminación externa
ALTER TABLE porcinos_servicios 
ADD COLUMN macho_nombre VARCHAR(255) NULL AFTER macho_id;

-- Agregar índice para búsquedas por nombre de macho
CREATE INDEX idx_servicios_macho_nombre ON porcinos_servicios(macho_nombre);

-- Verificar que el campo se agregó correctamente
SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    IS_NULLABLE,
    COLUMN_DEFAULT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'agrocloud'
  AND TABLE_NAME = 'porcinos_servicios'
  AND COLUMN_NAME = 'macho_nombre';

SELECT '✅ Migración V1_100 ejecutada correctamente' AS resultado;

