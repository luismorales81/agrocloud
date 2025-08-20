-- =====================================================
-- CREACIÓN DE TABLA CULTIVOS
-- =====================================================

USE agrogestion;

-- Crear tabla cultivos
CREATE TABLE IF NOT EXISTS cultivos (
    id INT(11) AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL COMMENT 'Nombre del cultivo (ej: Soja, Maíz, Trigo)',
    variedad VARCHAR(100) NOT NULL COMMENT 'Variedad específica del cultivo',
    ciclo_dias INT(11) NOT NULL COMMENT 'Duración del ciclo en días',
    rendimiento_esperado DECIMAL(10,2) NOT NULL COMMENT 'Rendimiento esperado por hectárea',
    unidad_rendimiento VARCHAR(20) NOT NULL DEFAULT 'kg/ha' COMMENT 'Unidad de rendimiento (kg/ha, tn/ha, qq/ha)',
    descripcion TEXT COMMENT 'Descripción y características del cultivo',
    estado ENUM('activo', 'inactivo') NOT NULL DEFAULT 'activo' COMMENT 'Estado del cultivo',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Fecha de creación del registro',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Fecha de última actualización',
    
    -- Índices para mejorar rendimiento
    INDEX idx_nombre (nombre),
    INDEX idx_variedad (variedad),
    INDEX idx_estado (estado),
    INDEX idx_ciclo_dias (ciclo_dias),
    
    -- Constraints
    CONSTRAINT chk_ciclo_dias CHECK (ciclo_dias > 0),
    CONSTRAINT chk_rendimiento CHECK (rendimiento_esperado >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Tabla para registrar cultivos agrícolas disponibles';

-- Insertar datos de ejemplo
INSERT INTO cultivos (nombre, variedad, ciclo_dias, rendimiento_esperado, unidad_rendimiento, descripcion, estado) VALUES
('Soja', 'DM 53i54', 120, 3500.00, 'kg/ha', 'Soja de ciclo corto, resistente a sequía y enfermedades. Ideal para siembra de segunda.', 'activo'),
('Soja', 'DM 58i60', 130, 3800.00, 'kg/ha', 'Soja de ciclo medio, alto potencial de rendimiento. Adaptada a diferentes condiciones de suelo.', 'activo'),
('Maíz', 'DK 72-10', 140, 12.50, 'tn/ha', 'Maíz híbrido de alto rendimiento, adaptado a diferentes suelos. Resistente a enfermedades foliares.', 'activo'),
('Maíz', 'DK 79-10', 150, 14.00, 'tn/ha', 'Maíz híbrido de ciclo largo, máximo potencial de rendimiento. Requiere buena fertilización.', 'activo'),
('Trigo', 'Klein Pantera', 180, 4500.00, 'kg/ha', 'Trigo de calidad panadera, resistente a royas. Ciclo largo, alto contenido proteico.', 'activo'),
('Trigo', 'Klein Guerrero', 170, 4200.00, 'kg/ha', 'Trigo de ciclo medio, buena calidad panadera. Adaptado a siembra directa.', 'activo'),
('Girasol', 'Paraíso 33', 110, 2.50, 'tn/ha', 'Girasol confitero de alto contenido oleico. Ciclo corto, resistente a sequía.', 'activo'),
('Girasol', 'Paraíso 39', 115, 2.80, 'tn/ha', 'Girasol oleico de alto rendimiento. Adaptado a diferentes condiciones climáticas.', 'activo'),
('Sorgo', 'DK 54-00', 100, 8.00, 'tn/ha', 'Sorgo granífero de ciclo corto. Ideal para rotación con soja.', 'activo'),
('Cebada', 'Quilmes Ayelén', 160, 5500.00, 'kg/ha', 'Cebada cervecera de alta calidad. Ciclo medio, resistente a enfermedades.', 'activo'),
('Arroz', 'Gurí INTA', 140, 8.50, 'tn/ha', 'Arroz de alto rendimiento. Adaptado a riego por inundación.', 'inactivo'),
('Algodón', 'DP 1646 B2XF', 180, 2.20, 'tn/ha', 'Algodón transgénico resistente a insectos. Ciclo largo, requiere riego.', 'inactivo');

-- Verificar la creación
SELECT '✅ Tabla cultivos creada exitosamente' AS mensaje;

-- Mostrar estructura de la tabla
DESCRIBE cultivos;

-- Mostrar datos de ejemplo
SELECT 
    id,
    nombre,
    variedad,
    ciclo_dias,
    rendimiento_esperado,
    unidad_rendimiento,
    estado,
    created_at
FROM cultivos
ORDER BY nombre, variedad;
