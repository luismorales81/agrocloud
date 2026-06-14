-- ============================================================================
-- SCRIPT: Ejecutar Migración V1_106 Manualmente
-- ============================================================================
-- Este script agrega la columna estado_configurado_id a la tabla cultivo_lotes
-- 
-- IMPORTANTE: Asegúrate de que la migración V1_105 se haya ejecutado primero
-- (debe existir la tabla cultivo_estados_lote)
-- 
-- Ejecutar: mysql -u root -p agrocloud < scripts/V1_106_Ejecutar_Manual.sql
-- ============================================================================

USE agrocloud;

-- Verificar que existe la tabla cultivo_estados_lote
SELECT COUNT(*) as existe_tabla_estados 
FROM information_schema.tables 
WHERE table_schema = 'agrocloud' 
AND table_name = 'cultivo_estados_lote';

-- Verificar si la columna ya existe
SELECT COUNT(*) as existe_columna
FROM information_schema.columns 
WHERE table_schema = 'agrocloud' 
AND table_name = 'cultivo_lotes' 
AND column_name = 'estado_configurado_id';

-- Agregar columna para estado configurado (opcional, mantiene compatibilidad con enum)
ALTER TABLE cultivo_lotes 
ADD COLUMN IF NOT EXISTS estado_configurado_id BIGINT NULL COMMENT 'ID del estado configurado (opcional, si es NULL usa el enum estado)';

-- Agregar foreign key (si no existe)
-- Nota: MySQL no soporta IF NOT EXISTS para constraints, así que verificamos primero
SET @constraint_exists = (
    SELECT COUNT(*) 
    FROM information_schema.table_constraints 
    WHERE constraint_schema = 'agrocloud' 
    AND table_name = 'cultivo_lotes' 
    AND constraint_name = 'fk_lote_estado_configurado'
);

SET @sql = IF(@constraint_exists = 0,
    'ALTER TABLE cultivo_lotes ADD CONSTRAINT fk_lote_estado_configurado FOREIGN KEY (estado_configurado_id) REFERENCES cultivo_estados_lote(id) ON DELETE SET NULL',
    'SELECT "Constraint fk_lote_estado_configurado already exists" as message'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Crear índice para búsquedas (si no existe)
CREATE INDEX IF NOT EXISTS idx_lotes_estado_configurado ON cultivo_lotes(estado_configurado_id);

-- Verificar que se creó correctamente
DESCRIBE cultivo_lotes;

-- Mostrar mensaje de éxito
SELECT 'Migración V1_106 ejecutada exitosamente' as resultado;













