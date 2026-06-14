-- Script completo para agregar todas las columnas faltantes
USE agrocloud;

-- Verificar y agregar cada columna
SET @db = 'agrocloud';
SET @table = 'porcinos_parametros_productivos_porcinos';

-- dias_tolerancia_vencimiento_gestacion
SET @col = 'dias_tolerancia_vencimiento_gestacion';
SET @sql = IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = @table AND COLUMN_NAME = @col) = 0,
    CONCAT('ALTER TABLE ', @table, ' ADD COLUMN ', @col, ' INT NULL COMMENT ''Días de tolerancia para el vencimiento de gestación'';'),
    'SELECT ''Columna ya existe'';'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- dias_control_celo
SET @col = 'dias_control_celo';
SET @sql = IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = @table AND COLUMN_NAME = @col) = 0,
    CONCAT('ALTER TABLE ', @table, ' ADD COLUMN ', @col, ' INT NULL COMMENT ''Días de control de celo'';'),
    'SELECT ''Columna ya existe'';'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- dias_entre_celos
SET @col = 'dias_entre_celos';
SET @sql = IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = @table AND COLUMN_NAME = @col) = 0,
    CONCAT('ALTER TABLE ', @table, ' ADD COLUMN ', @col, ' INT NULL COMMENT ''Días entre celos'';'),
    'SELECT ''Columna ya existe'';'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- dias_pasaje_maternidad
SET @col = 'dias_pasaje_maternidad';
SET @sql = IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = @table AND COLUMN_NAME = @col) = 0,
    CONCAT('ALTER TABLE ', @table, ' ADD COLUMN ', @col, ' INT NULL COMMENT ''Días antes del parto para pasaje a maternidad'';'),
    'SELECT ''Columna ya existe'';'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- peso_promedio_nacimiento
SET @col = 'peso_promedio_nacimiento';
SET @sql = IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = @table AND COLUMN_NAME = @col) = 0,
    CONCAT('ALTER TABLE ', @table, ' ADD COLUMN ', @col, ' DECIMAL(10,2) NULL COMMENT ''Peso promedio al nacer (kg)'';'),
    'SELECT ''Columna ya existe'';'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- peso_destete_objetivo
SET @col = 'peso_destete_objetivo';
SET @sql = IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = @table AND COLUMN_NAME = @col) = 0,
    CONCAT('ALTER TABLE ', @table, ' ADD COLUMN ', @col, ' DECIMAL(10,2) NULL COMMENT ''Peso objetivo al destete (kg)'';'),
    'SELECT ''Columna ya existe'';'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- peso_venta_objetivo
SET @col = 'peso_venta_objetivo';
SET @sql = IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = @table AND COLUMN_NAME = @col) = 0,
    CONCAT('ALTER TABLE ', @table, ' ADD COLUMN ', @col, ' DECIMAL(10,2) NULL COMMENT ''Peso objetivo para venta (kg)'';'),
    'SELECT ''Columna ya existe'';'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- lechones_vivos_parto_objetivo
SET @col = 'lechones_vivos_parto_objetivo';
SET @sql = IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = @table AND COLUMN_NAME = @col) = 0,
    CONCAT('ALTER TABLE ', @table, ' ADD COLUMN ', @col, ' DECIMAL(5,2) NULL COMMENT ''Lechones vivos por parto objetivo'';'),
    'SELECT ''Columna ya existe'';'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- lechones_destetados_objetivo
SET @col = 'lechones_destetados_objetivo';
SET @sql = IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = @table AND COLUMN_NAME = @col) = 0,
    CONCAT('ALTER TABLE ', @table, ' ADD COLUMN ', @col, ' DECIMAL(5,2) NULL COMMENT ''Lechones destetados objetivo'';'),
    'SELECT ''Columna ya existe'';'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- partos_madre_anio_objetivo
SET @col = 'partos_madre_anio_objetivo';
SET @sql = IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = @table AND COLUMN_NAME = @col) = 0,
    CONCAT('ALTER TABLE ', @table, ' ADD COLUMN ', @col, ' DECIMAL(5,2) NULL COMMENT ''Partos por madre por año objetivo'';'),
    'SELECT ''Columna ya existe'';'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Verificar resultado final
SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    IS_NULLABLE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = @db 
  AND TABLE_NAME = @table
  AND COLUMN_NAME IN (
    'dias_tolerancia_vencimiento_gestacion',
    'dias_control_celo',
    'dias_entre_celos',
    'dias_pasaje_maternidad',
    'peso_promedio_nacimiento',
    'peso_destete_objetivo',
    'peso_venta_objetivo',
    'lechones_vivos_parto_objetivo',
    'lechones_destetados_objetivo',
    'partos_madre_anio_objetivo'
  )
ORDER BY COLUMN_NAME;















