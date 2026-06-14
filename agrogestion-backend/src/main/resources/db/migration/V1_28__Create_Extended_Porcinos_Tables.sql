-- ============================================================================
-- MIGRACIÓN: Crear tablas extendidas del módulo de Porcinos
-- Versión: V1_28
-- Fecha: 2025-01-XX
-- ============================================================================

SET @dbname = DATABASE();

-- Actualizar tabla gestacion con nuevos campos
SET @tablename = 'gestacion';

-- Agregar fecha_aborto si no existe
SET @columnname = 'fecha_aborto';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (TABLE_SCHEMA = @dbname)
      AND (TABLE_NAME = @tablename)
      AND (COLUMN_NAME = @columnname)
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' DATE AFTER estado')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- Agregar causa_aborto si no existe
SET @columnname = 'causa_aborto';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (TABLE_SCHEMA = @dbname)
      AND (TABLE_NAME = @tablename)
      AND (COLUMN_NAME = @columnname)
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' VARCHAR(200) AFTER fecha_aborto')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- Agregar fecha_sala_maternidad si no existe
SET @columnname = 'fecha_sala_maternidad';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (TABLE_SCHEMA = @dbname)
      AND (TABLE_NAME = @tablename)
      AND (COLUMN_NAME = @columnname)
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' DATE AFTER causa_aborto')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- Modificar estado para incluir EN_CURSO, ABORTO, FINALIZADA
ALTER TABLE gestacion 
MODIFY COLUMN estado ENUM('EN_CURSO', 'ABORTO', 'FINALIZADA', 'ACTIVA') NOT NULL DEFAULT 'EN_CURSO';

-- Tabla: chequeos_gestacion
CREATE TABLE IF NOT EXISTS chequeos_gestacion (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    gestacion_id BIGINT NOT NULL,
    fecha DATE NOT NULL,
    metodo ENUM('ECO', 'PALPACION', 'OBSERVACION') NOT NULL,
    resultado ENUM('POSITIVO', 'NEGATIVO') NOT NULL,
    observaciones TEXT,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    empresa_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (gestacion_id) REFERENCES gestacion(id) ON DELETE CASCADE,
    FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    
    INDEX idx_chequeos_gestacion (gestacion_id),
    INDEX idx_chequeos_fecha (fecha),
    INDEX idx_chequeos_resultado (resultado),
    INDEX idx_chequeos_empresa (empresa_id),
    INDEX idx_chequeos_activo (activo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Actualizar tabla partos con peso_promedio_nacimiento
SET @tablename = 'partos';
SET @columnname = 'peso_promedio_nacimiento';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (TABLE_SCHEMA = @dbname)
      AND (TABLE_NAME = @tablename)
      AND (COLUMN_NAME = @columnname)
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' DECIMAL(10,2) AFTER total_nacidos')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- Tabla: destetes
CREATE TABLE IF NOT EXISTS destetes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    parto_id BIGINT NOT NULL,
    fecha_destete DATE NOT NULL,
    cantidad_destetados INT NOT NULL,
    peso_promedio_destete DECIMAL(10,2) NOT NULL,
    dias_lactancia INT,
    observaciones TEXT,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    empresa_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (parto_id) REFERENCES partos(id) ON DELETE CASCADE,
    FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    
    INDEX idx_destetes_parto (parto_id),
    INDEX idx_destetes_fecha (fecha_destete),
    INDEX idx_destetes_empresa (empresa_id),
    INDEX idx_destetes_activo (activo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: adopciones (adopciones entre camadas)
CREATE TABLE IF NOT EXISTS adopciones (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    parto_origen_id BIGINT NOT NULL COMMENT 'Parto que entrega lechones',
    parto_destino_id BIGINT NOT NULL COMMENT 'Parto que recibe lechones',
    cantidad INT NOT NULL DEFAULT 1,
    fecha DATE NOT NULL,
    observaciones TEXT,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    empresa_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (parto_origen_id) REFERENCES partos(id) ON DELETE CASCADE,
    FOREIGN KEY (parto_destino_id) REFERENCES partos(id) ON DELETE CASCADE,
    FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    
    INDEX idx_adopciones_origen (parto_origen_id),
    INDEX idx_adopciones_destino (parto_destino_id),
    INDEX idx_adopciones_fecha (fecha),
    INDEX idx_adopciones_empresa (empresa_id),
    INDEX idx_adopciones_activo (activo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: consumos_alimento (nueva tabla para consumos de alimento)
CREATE TABLE IF NOT EXISTS consumos_alimento (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    categoria ENUM('MADRES', 'PADRILLOS', 'RECRIA', 'ENGORDE', 'LECHONES') NOT NULL,
    fecha DATE NOT NULL,
    cantidad_kg DECIMAL(10,2) NOT NULL,
    tipo_alimento ENUM('BALANCEADO', 'MAIZ', 'GRANO_PROPIO') NOT NULL,
    cultivo_relacionado_id BIGINT NULL COMMENT 'Si es grano propio, referencia al cultivo',
    lote_id BIGINT NULL COMMENT 'Lote de recría/engorde si aplica',
    madre_id BIGINT NULL COMMENT 'Madre específica si aplica',
    observaciones TEXT,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    empresa_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (cultivo_relacionado_id) REFERENCES cultivos(id) ON DELETE SET NULL,
    FOREIGN KEY (lote_id) REFERENCES lotes(id) ON DELETE SET NULL,
    FOREIGN KEY (madre_id) REFERENCES madres(id) ON DELETE SET NULL,
    FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    
    INDEX idx_consumos_alimento_categoria (categoria),
    INDEX idx_consumos_alimento_fecha (fecha),
    INDEX idx_consumos_alimento_tipo (tipo_alimento),
    INDEX idx_consumos_alimento_cultivo (cultivo_relacionado_id),
    INDEX idx_consumos_alimento_lote (lote_id),
    INDEX idx_consumos_alimento_madre (madre_id),
    INDEX idx_consumos_alimento_empresa (empresa_id),
    INDEX idx_consumos_alimento_activo (activo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: ventas_porcinos
CREATE TABLE IF NOT EXISTS ventas_porcinos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tipo ENUM('ENGORDE', 'REPRODUCTOR') NOT NULL,
    fecha DATE NOT NULL,
    cantidad INT NOT NULL,
    peso_promedio DECIMAL(10,2) NOT NULL,
    precio_kg DECIMAL(10,2) NOT NULL,
    ingreso_total DECIMAL(15,2) NOT NULL,
    lote_id BIGINT NULL COMMENT 'Lote de origen si aplica',
    recria_id BIGINT NULL COMMENT 'Recría de origen si aplica',
    cliente VARCHAR(200),
    observaciones TEXT,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    empresa_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (lote_id) REFERENCES lotes(id) ON DELETE SET NULL,
    FOREIGN KEY (recria_id) REFERENCES recria(id) ON DELETE SET NULL,
    FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    
    INDEX idx_ventas_tipo (tipo),
    INDEX idx_ventas_fecha (fecha),
    INDEX idx_ventas_lote (lote_id),
    INDEX idx_ventas_recria (recria_id),
    INDEX idx_ventas_empresa (empresa_id),
    INDEX idx_ventas_activo (activo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;







