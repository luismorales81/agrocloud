-- ============================================================================
-- SCRIPT PARA CORREGIR CONSTRAINT chk_estado_servicio
-- ============================================================================
-- Problema: La restricción tiene caracteres corruptos (PRE??EZ_CONFIRMADA)
-- Solución: Eliminar y recrear la restricción con el valor correcto
-- ============================================================================

USE agrocloud;

-- Eliminar la restricción existente (si existe)
SET @constraint_name = 'chk_estado_servicio';
SET @sql = CONCAT('ALTER TABLE porcinos_servicios DROP CONSTRAINT ', @constraint_name);
SET @sql = IF((SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS 
    WHERE CONSTRAINT_SCHEMA = 'agrocloud' 
    AND TABLE_NAME = 'porcinos_servicios' 
    AND CONSTRAINT_NAME = @constraint_name) > 0, @sql, 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Recrear la restricción con el valor correcto (PREÑEZ_CONFIRMADA con Ñ)
ALTER TABLE porcinos_servicios 
ADD CONSTRAINT chk_estado_servicio 
CHECK (estado_servicio IN ('PENDIENTE_CONTROL', 'FALLIDO', 'PREÑEZ_CONFIRMADA'));

-- Verificar que se creó correctamente
SELECT '✓ Constraint chk_estado_servicio corregida' AS resultado;

-- Verificar la estructura
SHOW CREATE TABLE porcinos_servicios;
