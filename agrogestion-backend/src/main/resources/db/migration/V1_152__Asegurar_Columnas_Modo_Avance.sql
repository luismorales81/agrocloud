-- V1_152: Asegura columnas de V1_150 si la migración anterior no se aplicó (idempotente).

SET @db := DATABASE();

SET @tiene_dias := (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'cultivo_estados_lote' AND COLUMN_NAME = 'dias_minimos'
);
SET @sql_dias := IF(
    @tiene_dias = 0,
    'ALTER TABLE cultivo_estados_lote ADD COLUMN dias_minimos INT NULL COMMENT ''Días desde siembra para alcanzar este estado''',
    'SELECT 1'
);
PREPARE stmt_dias FROM @sql_dias;
EXECUTE stmt_dias;
DEALLOCATE PREPARE stmt_dias;

SET @tiene_modo := (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'cultivo_estados_lote' AND COLUMN_NAME = 'modo_avance'
);
SET @sql_modo := IF(
    @tiene_modo = 0,
    'ALTER TABLE cultivo_estados_lote ADD COLUMN modo_avance VARCHAR(20) NOT NULL DEFAULT ''MIXTO'' COMMENT ''EVENTO, TIEMPO, TAREAS o MIXTO''',
    'SELECT 1'
);
PREPARE stmt_modo FROM @sql_modo;
EXECUTE stmt_modo;
DEALLOCATE PREPARE stmt_modo;

UPDATE cultivo_estados_lote
SET modo_avance = 'MIXTO'
WHERE modo_avance IS NULL OR TRIM(modo_avance) = '';
