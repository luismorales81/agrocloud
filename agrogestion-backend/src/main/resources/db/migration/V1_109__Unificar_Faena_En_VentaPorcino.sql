-- ============================================================================
-- MIGRACIÓN: Unificar Faena en VentaPorcino
-- Versión: V1_109
-- Fecha: 2025-01-XX
-- ============================================================================
-- Esta migración:
-- 1. Agrega FAENA al enum tipo en porcinos_ventas_porcinos
-- 2. Agrega columnas opcionales para campos específicos de faena
-- 3. Migra datos de porcinos_faena a porcinos_ventas_porcinos
-- 4. Mantiene la tabla porcinos_faena por compatibilidad (se puede eliminar después)

-- Paso 1: Modificar el enum tipo para incluir FAENA
ALTER TABLE porcinos_ventas_porcinos 
MODIFY COLUMN tipo ENUM('ENGORDE', 'REPRODUCTOR', 'FAENA') NOT NULL;

-- Paso 2: Agregar columnas opcionales para campos específicos de faena
-- Verificar si las columnas ya existen antes de agregarlas
SET @dbname = DATABASE();
SET @tablename = 'porcinos_ventas_porcinos';
SET @preparedStatement = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
     WHERE TABLE_SCHEMA = @dbname 
     AND TABLE_NAME = @tablename 
     AND COLUMN_NAME = 'peso_envio') > 0,
    'SELECT 1',
    'ALTER TABLE porcinos_ventas_porcinos ADD COLUMN peso_envio DECIMAL(10,2) NULL COMMENT ''Peso de envío (solo para faenas)'''
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

SET @preparedStatement = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
     WHERE TABLE_SCHEMA = @dbname 
     AND TABLE_NAME = @tablename 
     AND COLUMN_NAME = 'peso_faena') > 0,
    'SELECT 1',
    'ALTER TABLE porcinos_ventas_porcinos ADD COLUMN peso_faena DECIMAL(10,2) NULL COMMENT ''Peso después de faena (solo para faenas)'''
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

SET @preparedStatement = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
     WHERE TABLE_SCHEMA = @dbname 
     AND TABLE_NAME = @tablename 
     AND COLUMN_NAME = 'rendimiento') > 0,
    'SELECT 1',
    'ALTER TABLE porcinos_ventas_porcinos ADD COLUMN rendimiento DECIMAL(5,2) NULL COMMENT ''Rendimiento de faena en porcentaje (solo para faenas)'''
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

SET @preparedStatement = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
     WHERE TABLE_SCHEMA = @dbname 
     AND TABLE_NAME = @tablename 
     AND COLUMN_NAME = 'fecha_envio') > 0,
    'SELECT 1',
    'ALTER TABLE porcinos_ventas_porcinos ADD COLUMN fecha_envio DATE NULL COMMENT ''Fecha de envío al matadero (solo para faenas)'''
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

SET @preparedStatement = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
     WHERE TABLE_SCHEMA = @dbname 
     AND TABLE_NAME = @tablename 
     AND COLUMN_NAME = 'fecha_faena') > 0,
    'SELECT 1',
    'ALTER TABLE porcinos_ventas_porcinos ADD COLUMN fecha_faena DATE NULL COMMENT ''Fecha de faena (solo para faenas)'''
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- Paso 3: Migrar datos de porcinos_faena a porcinos_ventas_porcinos
-- Nota: Se insertan como tipo FAENA, usando fecha_envio como fecha principal
-- Solo migrar si la tabla porcinos_faena existe y tiene datos
SET @faena_table_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES 
    WHERE TABLE_SCHEMA = @dbname 
    AND TABLE_NAME = 'porcinos_faena');

SET @preparedStatement = (SELECT IF(
    @faena_table_exists > 0,
    CONCAT('INSERT INTO ', @tablename, ' (
        tipo,
        fecha,
        cantidad,
        peso_promedio,
        precio_kg,
        ingreso_total,
        recria_id,
        observaciones,
        empresa_id,
        usuario_id,
        fecha_creacion,
        activo,
        peso_envio,
        peso_faena,
        rendimiento,
        fecha_envio,
        fecha_faena
    )
    SELECT 
        ''FAENA'' AS tipo,
        fecha_envio AS fecha,
        cantidad_animales AS cantidad,
        CASE 
            WHEN cantidad_animales > 0 THEN peso_envio / cantidad_animales
            ELSE 0
        END AS peso_promedio,
        precio_kg,
        ingreso_total,
        recria_id,
        observaciones,
        empresa_id,
        usuario_id,
        fecha_creacion,
        TRUE AS activo,
        peso_envio,
        peso_faena,
        rendimiento,
        fecha_envio,
        fecha_faena
    FROM porcinos_faena
    WHERE NOT EXISTS (
        SELECT 1 FROM ', @tablename, ' vp
        WHERE vp.tipo = ''FAENA''
        AND vp.recria_id = porcinos_faena.recria_id
        AND vp.fecha = porcinos_faena.fecha_envio
        AND vp.cantidad = porcinos_faena.cantidad_animales
    )'),
    'SELECT 1'
));
PREPARE migrateData FROM @preparedStatement;
EXECUTE migrateData;
DEALLOCATE PREPARE migrateData;

-- Paso 4: Agregar índices para mejorar consultas (solo si no existen)
SET @preparedStatement = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
     WHERE TABLE_SCHEMA = @dbname 
     AND TABLE_NAME = @tablename 
     AND INDEX_NAME = 'idx_ventas_fecha_envio') > 0,
    'SELECT 1',
    'CREATE INDEX idx_ventas_fecha_envio ON porcinos_ventas_porcinos(fecha_envio)'
));
PREPARE createIndexIfNotExists FROM @preparedStatement;
EXECUTE createIndexIfNotExists;
DEALLOCATE PREPARE createIndexIfNotExists;

SET @preparedStatement = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
     WHERE TABLE_SCHEMA = @dbname 
     AND TABLE_NAME = @tablename 
     AND INDEX_NAME = 'idx_ventas_tipo_fecha') > 0,
    'SELECT 1',
    'CREATE INDEX idx_ventas_tipo_fecha ON porcinos_ventas_porcinos(tipo, fecha)'
));
PREPARE createIndexIfNotExists FROM @preparedStatement;
EXECUTE createIndexIfNotExists;
DEALLOCATE PREPARE createIndexIfNotExists;

-- Nota: La tabla porcinos_faena se mantiene por compatibilidad con datos históricos
-- Se puede eliminar en una migración futura después de verificar que todo funciona correctamente

