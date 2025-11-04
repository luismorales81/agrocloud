-- ========================================
-- CREAR TABLA DE FORMAS DE APLICACIÓN
-- ========================================
-- Esta tabla almacena las diferentes formas de aplicación de agroquímicos

USE agrocloud;

-- Crear tabla de formas de aplicación
CREATE TABLE IF NOT EXISTS formas_aplicacion (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    activo BOOLEAN DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Insertar las 5 formas de aplicación específicas
INSERT INTO formas_aplicacion (nombre, descripcion) VALUES
('Pulverización', 'Aplicación mediante pulverizador hidráulico o de mochila'),
('Fumigación', 'Aplicación mediante fumigador aéreo o terrestre'),
('Granulado', 'Aplicación de productos granulados al voleo'),
('Manual', 'Aplicación manual con equipos portátiles'),
('Precisión', 'Aplicación de precisión con GPS y tecnología avanzada');

-- Verificar inserción
SELECT * FROM formas_aplicacion WHERE activo = TRUE ORDER BY nombre;
