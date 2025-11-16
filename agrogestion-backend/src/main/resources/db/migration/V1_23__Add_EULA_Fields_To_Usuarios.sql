-- Agregar campos EULA a la tabla usuarios
ALTER TABLE usuarios 
ADD COLUMN eula_aceptado BOOLEAN DEFAULT FALSE NOT NULL,
ADD COLUMN eula_fecha_aceptacion DATETIME NULL,
ADD COLUMN eula_ip_address VARCHAR(45) NULL,
ADD COLUMN eula_user_agent VARCHAR(500) NULL,
ADD COLUMN eula_version VARCHAR(20) NULL,
ADD COLUMN eula_pdf_path VARCHAR(500) NULL;

-- Crear índice para búsquedas rápidas
CREATE INDEX idx_eula_aceptado ON usuarios(eula_aceptado);

