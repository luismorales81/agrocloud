-- ============================================================================
-- MIGRACIÓN: Crear tablas de muertes para el módulo de Porcinos
-- Versión: V1_27
-- Fecha: 2025-01-XX
-- ============================================================================

-- Tabla: muertes_madres
CREATE TABLE IF NOT EXISTS muertes_madres (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    madre_id BIGINT NOT NULL,
    fecha DATE NOT NULL,
    causa ENUM('ENFERMEDAD', 'APLASTAMIENTO', 'ACCIDENTE', 'METRITIS', 'PROLAPSO', 'CAIDA', 'GOLPE', 'TORSION', 'INFECCION', 'EDAD', 'DESCONOCIDA') NOT NULL,
    etapa_momento ENUM('CACHORRA', 'GESTACION', 'LACTANCIA', 'ADULTA') NOT NULL,
    observaciones TEXT,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    empresa_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (madre_id) REFERENCES madres(id) ON DELETE CASCADE,
    FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    
    INDEX idx_muertes_madres_madre (madre_id),
    INDEX idx_muertes_madres_fecha (fecha),
    INDEX idx_muertes_madres_causa (causa),
    INDEX idx_muertes_madres_empresa (empresa_id),
    INDEX idx_muertes_madres_activo (activo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Actualizar tabla muertes_lactancia para que coincida con el modelo completo
SET @dbname = DATABASE();
SET @tablename = 'muertes_lactancia';

-- Agregar columna madre_id si no existe
SET @columnname = 'madre_id';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (TABLE_SCHEMA = @dbname)
      AND (TABLE_NAME = @tablename)
      AND (COLUMN_NAME = @columnname)
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' BIGINT NOT NULL AFTER id')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- Agregar columna etapa si no existe
SET @columnname = 'etapa';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (TABLE_SCHEMA = @dbname)
      AND (TABLE_NAME = @tablename)
      AND (COLUMN_NAME = @columnname)
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' ENUM(\'NACIMIENTO\', \'LACTANCIA\') NOT NULL DEFAULT \'LACTANCIA\' AFTER fecha')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- Modificar columna causa
ALTER TABLE muertes_lactancia 
MODIFY COLUMN causa ENUM('APLASTAMIENTO', 'DIARREA_NEONATAL', 'HIPOTERMIA', 'INANICION', 'DEFORMIDAD', 'INFECCION', 'DIARREA_POSDESTETE', 'DESCONOCIDA') NOT NULL;

-- Agregar columna empresa_id si no existe
SET @columnname = 'empresa_id';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (TABLE_SCHEMA = @dbname)
      AND (TABLE_NAME = @tablename)
      AND (COLUMN_NAME = @columnname)
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' BIGINT NOT NULL AFTER observaciones')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- Agregar columna usuario_id si no existe
SET @columnname = 'usuario_id';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (TABLE_SCHEMA = @dbname)
      AND (TABLE_NAME = @tablename)
      AND (COLUMN_NAME = @columnname)
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' BIGINT NOT NULL AFTER empresa_id')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- Agregar columna activo si no existe
SET @columnname = 'activo';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (TABLE_SCHEMA = @dbname)
      AND (TABLE_NAME = @tablename)
      AND (COLUMN_NAME = @columnname)
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' BOOLEAN NOT NULL DEFAULT TRUE AFTER usuario_id')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- Agregar foreign keys si no existen
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
    WHERE
      (TABLE_SCHEMA = DATABASE())
      AND (TABLE_NAME = 'muertes_lactancia')
      AND (CONSTRAINT_NAME = 'fk_muertes_lactancia_madre')
  ) > 0,
  'SELECT 1',
  'ALTER TABLE muertes_lactancia ADD CONSTRAINT fk_muertes_lactancia_madre FOREIGN KEY (madre_id) REFERENCES madres(id) ON DELETE CASCADE'
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
    WHERE
      (TABLE_SCHEMA = DATABASE())
      AND (TABLE_NAME = 'muertes_lactancia')
      AND (CONSTRAINT_NAME = 'fk_muertes_lactancia_empresa')
  ) > 0,
  'SELECT 1',
  'ALTER TABLE muertes_lactancia ADD CONSTRAINT fk_muertes_lactancia_empresa FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE'
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
    WHERE
      (TABLE_SCHEMA = DATABASE())
      AND (TABLE_NAME = 'muertes_lactancia')
      AND (CONSTRAINT_NAME = 'fk_muertes_lactancia_usuario')
  ) > 0,
  'SELECT 1',
  'ALTER TABLE muertes_lactancia ADD CONSTRAINT fk_muertes_lactancia_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE'
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- Agregar índices si no existen (MySQL no soporta CREATE INDEX IF NOT EXISTS en todas las versiones)
SET @sql = IF((SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = @dbname AND table_name = 'muertes_lactancia' AND index_name = 'idx_muertes_lactancia_madre') > 0, 'SELECT 1', 'CREATE INDEX idx_muertes_lactancia_madre ON muertes_lactancia(madre_id)');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = IF((SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = @dbname AND table_name = 'muertes_lactancia' AND index_name = 'idx_muertes_lactancia_etapa') > 0, 'SELECT 1', 'CREATE INDEX idx_muertes_lactancia_etapa ON muertes_lactancia(etapa)');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = IF((SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = @dbname AND table_name = 'muertes_lactancia' AND index_name = 'idx_muertes_lactancia_empresa') > 0, 'SELECT 1', 'CREATE INDEX idx_muertes_lactancia_empresa ON muertes_lactancia(empresa_id)');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = IF((SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = @dbname AND table_name = 'muertes_lactancia' AND index_name = 'idx_muertes_lactancia_activo') > 0, 'SELECT 1', 'CREATE INDEX idx_muertes_lactancia_activo ON muertes_lactancia(activo)');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- Renombrar tabla muertes_lactancia a muertes_lechones (si no existe ya)
-- Nota: En MySQL no se puede renombrar directamente, así que creamos la nueva y migramos datos si es necesario
-- Por ahora mantenemos muertes_lactancia pero la entidad se llamará MuerteLechon

-- Actualizar tabla muertes_recria para que coincida con el modelo completo
SET @tablename = 'muertes_recria';

-- Modificar columna causa
ALTER TABLE muertes_recria 
MODIFY COLUMN causa ENUM('NEUMONIA', 'DIARREA', 'GOLPE', 'ACCIDENTE', 'CANIBALISMO', 'APLASTAMIENTO', 'INANICION', 'DEBILIDAD', 'INTOXICACION', 'GOLPE_CALOR', 'DESCONOCIDA') NOT NULL;

-- Agregar columna peso_promedio si no existe
SET @columnname = 'peso_promedio';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (TABLE_SCHEMA = @dbname)
      AND (TABLE_NAME = @tablename)
      AND (COLUMN_NAME = @columnname)
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' DECIMAL(10,2) AFTER cantidad')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- Agregar columna empresa_id si no existe
SET @columnname = 'empresa_id';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (TABLE_SCHEMA = @dbname)
      AND (TABLE_NAME = @tablename)
      AND (COLUMN_NAME = @columnname)
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' BIGINT NOT NULL AFTER observaciones')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- Agregar columna usuario_id si no existe
SET @columnname = 'usuario_id';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (TABLE_SCHEMA = @dbname)
      AND (TABLE_NAME = @tablename)
      AND (COLUMN_NAME = @columnname)
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' BIGINT NOT NULL AFTER empresa_id')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- Agregar columna activo si no existe
SET @columnname = 'activo';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (TABLE_SCHEMA = @dbname)
      AND (TABLE_NAME = @tablename)
      AND (COLUMN_NAME = @columnname)
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' BOOLEAN NOT NULL DEFAULT TRUE AFTER usuario_id')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- Agregar foreign keys si no existen
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
    WHERE
      (TABLE_SCHEMA = DATABASE())
      AND (TABLE_NAME = 'muertes_recria')
      AND (CONSTRAINT_NAME = 'fk_muertes_recria_empresa')
  ) > 0,
  'SELECT 1',
  'ALTER TABLE muertes_recria ADD CONSTRAINT fk_muertes_recria_empresa FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE'
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
    WHERE
      (TABLE_SCHEMA = DATABASE())
      AND (TABLE_NAME = 'muertes_recria')
      AND (CONSTRAINT_NAME = 'fk_muertes_recria_usuario')
  ) > 0,
  'SELECT 1',
  'ALTER TABLE muertes_recria ADD CONSTRAINT fk_muertes_recria_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE'
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- Agregar índices si no existen
SET @sql = IF((SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = @dbname AND table_name = 'muertes_recria' AND index_name = 'idx_muertes_recria_causa') > 0, 'SELECT 1', 'CREATE INDEX idx_muertes_recria_causa ON muertes_recria(causa)');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = IF((SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = @dbname AND table_name = 'muertes_recria' AND index_name = 'idx_muertes_recria_empresa') > 0, 'SELECT 1', 'CREATE INDEX idx_muertes_recria_empresa ON muertes_recria(empresa_id)');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = IF((SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = @dbname AND table_name = 'muertes_recria' AND index_name = 'idx_muertes_recria_activo') > 0, 'SELECT 1', 'CREATE INDEX idx_muertes_recria_activo ON muertes_recria(activo)');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

