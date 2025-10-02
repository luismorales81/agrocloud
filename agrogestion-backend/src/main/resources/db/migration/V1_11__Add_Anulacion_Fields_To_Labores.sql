-- Migración para agregar campos de auditoría de anulación a la tabla labores
-- Permite registrar quién, cuándo y por qué se anuló una labor

-- 1. Agregar campos de auditoría de anulación
ALTER TABLE labores
ADD COLUMN motivo_anulacion VARCHAR(1000) NULL COMMENT 'Justificación de la anulación de la labor',
ADD COLUMN fecha_anulacion TIMESTAMP NULL COMMENT 'Fecha y hora en que se anuló la labor',
ADD COLUMN usuario_anulacion_id BIGINT NULL COMMENT 'Usuario que realizó la anulación';

-- 2. Agregar foreign key para el usuario que realizó la anulación
ALTER TABLE labores
ADD CONSTRAINT fk_labores_usuario_anulacion 
FOREIGN KEY (usuario_anulacion_id) REFERENCES usuarios(id) ON DELETE SET NULL;

-- 3. Crear índice para consultas por usuario que anuló
CREATE INDEX idx_labores_usuario_anulacion ON labores(usuario_anulacion_id);

-- 4. Crear índice para consultas por fecha de anulación
CREATE INDEX idx_labores_fecha_anulacion ON labores(fecha_anulacion);

-- 5. Actualizar el check constraint del enum estado si existe (MySQL 8.0.16+)
-- Nota: En versiones anteriores de MySQL, los ENUM se manejan directamente en la columna
-- Esta alteración agrega 'ANULADA' como nuevo estado válido
ALTER TABLE labores 
MODIFY COLUMN estado ENUM('PLANIFICADA', 'EN_PROGRESO', 'COMPLETADA', 'CANCELADA', 'ANULADA') 
DEFAULT 'PLANIFICADA'
COMMENT 'Estado actual de la labor: PLANIFICADA (no ejecutada), EN_PROGRESO (en ejecución), COMPLETADA (finalizada), CANCELADA (cancelada antes de ejecutar), ANULADA (anulada después de ejecutar)';

-- 6. Comentarios para documentar el propósito de los nuevos campos
ALTER TABLE labores 
COMMENT = 'Tabla de labores agrícolas con sistema de anulación y auditoría completo. Incluye gestión de estados y trazabilidad de cambios.';
