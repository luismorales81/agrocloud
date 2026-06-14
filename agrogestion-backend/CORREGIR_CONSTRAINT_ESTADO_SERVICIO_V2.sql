-- ============================================================================
-- SCRIPT PARA CORREGIR CONSTRAINT chk_estado_servicio (VERSIÓN 2)
-- ============================================================================
-- Problema: La restricción tiene caracteres corruptos (PRE??EZ_CONFIRMADA)
-- Solución: Usar valor hexadecimal para la Ñ
-- ============================================================================

USE agrocloud;

-- Eliminar la restricción existente
SET @constraint_name = 'chk_estado_servicio';
SET @sql = CONCAT('ALTER TABLE porcinos_servicios DROP CONSTRAINT ', @constraint_name);
SET @sql = IF((SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS 
    WHERE CONSTRAINT_SCHEMA = 'agrocloud' 
    AND TABLE_NAME = 'porcinos_servicios' 
    AND CONSTRAINT_NAME = @constraint_name) > 0, @sql, 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Crear la restricción usando CONCAT para construir el valor con Ñ
-- La Ñ en UTF-8 es 0xC391 (hex) o podemos usar CHAR(209) + CHAR(145) para UTF-8
SET @valor_prenez = CONCAT('PRE', CHAR(0xC3, 0x91 USING utf8mb4), 'EZ_CONFIRMADA');
SET @sql = CONCAT('ALTER TABLE porcinos_servicios ADD CONSTRAINT chk_estado_servicio CHECK (estado_servicio IN (''PENDIENTE_CONTROL'', ''FALLIDO'', ''', @valor_prenez, '''))');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Verificar
SELECT '✓ Constraint corregida' AS resultado;
