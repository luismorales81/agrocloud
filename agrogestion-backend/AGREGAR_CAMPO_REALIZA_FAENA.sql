-- ============================================================================
-- SCRIPT: Agregar campo realiza_faena a parámetros del establecimiento
-- ============================================================================
-- Este script agrega la columna realiza_faena a la tabla 
-- porcinos_parametros_establecimiento_porcinos para configurar si el 
-- establecimiento realiza faenas o solo ventas normales.
-- ============================================================================

USE agrocloud;

-- Agregar columna realiza_faena si no existe
-- Verificar si la columna ya existe antes de agregarla
SET @col_exists = (
    SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = 'agrocloud' 
    AND TABLE_NAME = 'porcinos_parametros_establecimiento_porcinos'
    AND COLUMN_NAME = 'realiza_faena'
);

SET @sql = IF(@col_exists = 0,
    'ALTER TABLE porcinos_parametros_establecimiento_porcinos ADD COLUMN realiza_faena BOOLEAN NOT NULL DEFAULT FALSE COMMENT ''Indica si el establecimiento realiza faenas (ventas vía matadero)''',
    'SELECT ''La columna realiza_faena ya existe'' AS mensaje'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Mostrar confirmación
SELECT '✓ Columna realiza_faena agregada exitosamente' AS resultado;

-- NOTA: Por defecto todos los establecimientos tienen realiza_faena = FALSE
-- Los establecimientos que necesiten realizar faenas deben configurar este parámetro
-- desde la pantalla de Configuraciones del módulo de Porcinos
