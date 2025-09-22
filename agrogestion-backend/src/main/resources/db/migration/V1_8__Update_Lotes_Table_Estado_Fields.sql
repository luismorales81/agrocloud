-- Actualizar tabla lotes para incluir campos de estado y fechas de cultivo
-- Migración V1.8 - Actualización de campos de estado de lotes

-- 1. Agregar nuevas columnas para gestión de estados
ALTER TABLE lotes 
ADD COLUMN fecha_ultimo_cambio_estado TIMESTAMP NULL,
ADD COLUMN motivo_cambio_estado VARCHAR(255) NULL,
ADD COLUMN fecha_cosecha_real DATE NULL,
ADD COLUMN rendimiento_esperado DECIMAL(10,2) NULL,
ADD COLUMN rendimiento_real DECIMAL(10,2) NULL;

-- 2. Actualizar el campo estado para usar enum
-- Primero, cambiar el tipo de columna a VARCHAR más largo para el enum
ALTER TABLE lotes 
MODIFY COLUMN estado VARCHAR(50) NOT NULL DEFAULT 'DISPONIBLE';

-- 3. Actualizar registros existentes para usar los nuevos valores de enum
UPDATE lotes 
SET estado = CASE 
    WHEN estado = 'DISPONIBLE' THEN 'DISPONIBLE'
    WHEN estado = 'OCUPADO' THEN 'SEMBRADO'
    WHEN estado = 'EN_DESCANSO' THEN 'EN_DESCANSO'
    ELSE 'DISPONIBLE'
END;

-- 4. Agregar índices para mejorar el rendimiento de consultas por estado
CREATE INDEX idx_lotes_estado ON lotes(estado);
CREATE INDEX idx_lotes_fecha_cambio_estado ON lotes(fecha_ultimo_cambio_estado);
CREATE INDEX idx_lotes_fecha_siembra ON lotes(fecha_siembra);
CREATE INDEX idx_lotes_fecha_cosecha_esperada ON lotes(fecha_cosecha_esperada);

-- 5. Agregar comentarios para documentar las nuevas columnas
ALTER TABLE lotes 
MODIFY COLUMN estado VARCHAR(50) NOT NULL DEFAULT 'DISPONIBLE' 
COMMENT 'Estado actual del lote en el ciclo de cultivo';

ALTER TABLE lotes 
MODIFY COLUMN fecha_ultimo_cambio_estado TIMESTAMP NULL 
COMMENT 'Fecha y hora del último cambio de estado';

ALTER TABLE lotes 
MODIFY COLUMN motivo_cambio_estado VARCHAR(255) NULL 
COMMENT 'Motivo del último cambio de estado';

ALTER TABLE lotes 
MODIFY COLUMN fecha_cosecha_real DATE NULL 
COMMENT 'Fecha real de cosecha (cuando se completó)';

ALTER TABLE lotes 
MODIFY COLUMN rendimiento_esperado DECIMAL(10,2) NULL 
COMMENT 'Rendimiento esperado en toneladas por hectárea';

ALTER TABLE lotes 
MODIFY COLUMN rendimiento_real DECIMAL(10,2) NULL 
COMMENT 'Rendimiento real obtenido en toneladas por hectárea';

-- 6. Verificar que la migración se aplicó correctamente
SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    IS_NULLABLE,
    COLUMN_DEFAULT,
    COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'lotes' 
AND TABLE_SCHEMA = DATABASE()
AND COLUMN_NAME IN ('estado', 'fecha_ultimo_cambio_estado', 'motivo_cambio_estado', 
                   'fecha_cosecha_real', 'rendimiento_esperado', 'rendimiento_real')
ORDER BY ORDINAL_POSITION;
