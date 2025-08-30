-- Script para crear la tabla de ingresos (VERSIÓN CORREGIDA)
-- Ejecutar este script en la base de datos MySQL

-- Primero verificamos si las tablas referenciadas existen
-- Si no existen, las creamos primero

-- Crear tabla users si no existe
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nombre VARCHAR(100),
    apellido VARCHAR(100),
    activo BOOLEAN DEFAULT TRUE,
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Crear tabla campos si no existe
CREATE TABLE IF NOT EXISTS campos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    ubicacion VARCHAR(255),
    area_total DECIMAL(10,2),
    usuario_id BIGINT NOT NULL,
    activo BOOLEAN DEFAULT TRUE,
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (usuario_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Crear tabla lotes si no existe
CREATE TABLE IF NOT EXISTS lotes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(255),
    area_hectareas DECIMAL(10,2) NOT NULL,
    estado VARCHAR(100) DEFAULT 'DISPONIBLE',
    tipo_suelo VARCHAR(100),
    activo BOOLEAN DEFAULT TRUE,
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    campo_id BIGINT,
    user_id BIGINT NOT NULL,
    FOREIGN KEY (campo_id) REFERENCES campos(id) ON DELETE SET NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Ahora creamos la tabla de ingresos
CREATE TABLE IF NOT EXISTS ingresos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    concepto VARCHAR(200) NOT NULL,
    descripcion VARCHAR(500),
    tipo_ingreso ENUM('VENTA_CULTIVO', 'VENTA_ANIMAL', 'SERVICIOS_AGRICOLAS', 'SUBSIDIOS', 'OTROS_INGRESOS') NOT NULL,
    fecha_ingreso DATE NOT NULL,
    monto DECIMAL(15,2) NOT NULL,
    unidad_medida VARCHAR(100),
    cantidad DECIMAL(10,2),
    cliente_comprador VARCHAR(200),
    estado ENUM('REGISTRADO', 'CONFIRMADO', 'CANCELADO') DEFAULT 'REGISTRADO',
    observaciones VARCHAR(1000),
    lote_id BIGINT,
    usuario_id BIGINT NOT NULL,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (lote_id) REFERENCES lotes(id) ON DELETE SET NULL,
    FOREIGN KEY (usuario_id) REFERENCES users(id) ON DELETE CASCADE,
    
    INDEX idx_ingresos_usuario (usuario_id),
    INDEX idx_ingresos_fecha (fecha_ingreso),
    INDEX idx_ingresos_tipo (tipo_ingreso),
    INDEX idx_ingresos_estado (estado),
    INDEX idx_ingresos_lote (lote_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insertar un usuario de ejemplo si no existe
INSERT IGNORE INTO users (id, username, email, password, nombre, apellido) VALUES 
(1, 'admin', 'admin@agrocloud.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'Administrador', 'Sistema');

-- Insertar un campo de ejemplo si no existe
INSERT IGNORE INTO campos (id, nombre, descripcion, ubicacion, area_total, usuario_id) VALUES 
(1, 'Campo Principal', 'Campo principal de la finca', 'Ruta 8, Km 45', 100.00, 1);

-- Insertar un lote de ejemplo si no existe
INSERT IGNORE INTO lotes (id, nombre, descripcion, area_hectareas, estado, tipo_suelo, campo_id, user_id) VALUES 
(1, 'Lote A', 'Lote principal para cultivos', 25.00, 'DISPONIBLE', 'Franco arcilloso', 1, 1);

-- Insertar algunos datos de ejemplo en ingresos
INSERT INTO ingresos (concepto, descripcion, tipo_ingreso, fecha_ingreso, monto, unidad_medida, cantidad, cliente_comprador, estado, lote_id, usuario_id) VALUES
('Venta de Soja', 'Venta de soja de la cosecha 2024', 'VENTA_CULTIVO', '2024-03-15', 150000.00, 'toneladas', 50.00, 'Cooperativa San Martín', 'CONFIRMADO', 1, 1),
('Venta de Maíz', 'Venta de maíz de la cosecha 2024', 'VENTA_CULTIVO', '2024-04-20', 80000.00, 'toneladas', 40.00, 'Molino Central', 'CONFIRMADO', 1, 1),
('Servicio de Siembra', 'Servicio de siembra para terceros', 'SERVICIOS_AGRICOLAS', '2024-02-10', 25000.00, 'hectáreas', 100.00, 'Estancia La Esperanza', 'CONFIRMADO', NULL, 1),
('Subsidio PROAGRO', 'Subsidio del programa PROAGRO', 'SUBSIDIOS', '2024-01-15', 45000.00, NULL, NULL, 'Gobierno Nacional', 'CONFIRMADO', NULL, 1),
('Venta de Ganado', 'Venta de novillos', 'VENTA_ANIMAL', '2024-05-10', 120000.00, 'cabezas', 20.00, 'Frigorífico del Sur', 'REGISTRADO', NULL, 1);

-- Verificar que todo se creó correctamente
SELECT 'Tabla ingresos creada exitosamente' as mensaje;
SELECT COUNT(*) as total_ingresos FROM ingresos;
