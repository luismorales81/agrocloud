-- ========================================
-- Script para configurar la base de datos MySQL de Railway para AgroGestion
-- Ejecutar este script en la consola de MySQL de Railway
-- Base de datos: railway
-- Usuario: root
-- ========================================

-- Usar la base de datos
USE railway;

-- ========================================
-- CREACIÓN DE TABLAS DEL SISTEMA
-- ========================================

-- Tabla de roles
CREATE TABLE IF NOT EXISTS roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Tabla de usuarios
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Tabla de relación usuarios-roles
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT,
    role_id BIGINT,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- Tabla de campos
CREATE TABLE IF NOT EXISTS fields (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    location VARCHAR(255),
    area_hectares DECIMAL(10,2),
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Tabla de lotes
CREATE TABLE IF NOT EXISTS plots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    field_id BIGINT,
    name VARCHAR(100) NOT NULL,
    area_hectares DECIMAL(10,2),
    soil_type VARCHAR(50),
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (field_id) REFERENCES fields(id) ON DELETE CASCADE
);

-- Tabla de cultivos
CREATE TABLE IF NOT EXISTS crops (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    plot_id BIGINT,
    name VARCHAR(100) NOT NULL,
    variety VARCHAR(100),
    planting_date DATE,
    expected_harvest_date DATE,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (plot_id) REFERENCES plots(id) ON DELETE CASCADE
);

-- Tabla de insumos
CREATE TABLE IF NOT EXISTS insumos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(50),
    description TEXT,
    unit VARCHAR(20),
    price_per_unit DECIMAL(10,2),
    stock_quantity DECIMAL(10,2) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Tabla de maquinaria
CREATE TABLE IF NOT EXISTS maquinaria (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(50),
    model VARCHAR(100),
    year INTEGER,
    description TEXT,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Tabla de labores
CREATE TABLE IF NOT EXISTS labores (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    plot_id BIGINT,
    type VARCHAR(50) NOT NULL,
    description TEXT,
    date DATE,
    hours_worked DECIMAL(5,2),
    cost DECIMAL(10,2),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (plot_id) REFERENCES plots(id) ON DELETE CASCADE
);

-- ========================================
-- INSERTAR DATOS INICIALES DEL SISTEMA
-- ========================================

-- Insertar roles del sistema
INSERT INTO roles (name, description) VALUES 
('ADMIN', 'Administrador del sistema con acceso completo'),
('TECNICO', 'Técnico agrícola con acceso a gestión de cultivos y labores'),
('PRODUCTOR', 'Productor agrícola con acceso básico al sistema')
ON DUPLICATE KEY UPDATE description = VALUES(description);

-- Insertar usuario administrador (password: admin123)
INSERT INTO users (username, email, password, first_name, last_name) VALUES 
('admin', 'admin@agrocloud.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Admin', 'Sistema'),
('tecnico', 'tecnico@agrocloud.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Técnico', 'Agrícola'),
('productor', 'productor@agrocloud.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Productor', 'Agrícola')
ON DUPLICATE KEY UPDATE email = VALUES(email);

-- Asignar roles a usuarios
INSERT INTO user_roles (user_id, role_id) VALUES 
((SELECT id FROM users WHERE username = 'admin'), (SELECT id FROM roles WHERE name = 'ADMIN')),
((SELECT id FROM users WHERE username = 'tecnico'), (SELECT id FROM roles WHERE name = 'TECNICO')),
((SELECT id FROM users WHERE username = 'productor'), (SELECT id FROM roles WHERE name = 'PRODUCTOR'))
ON DUPLICATE KEY UPDATE user_id = VALUES(user_id);

-- Insertar campos de ejemplo
INSERT INTO fields (name, location, area_hectares, description) VALUES 
('Campo Principal', 'Zona Norte - Coordenadas: -34.6037, -58.3816', 150.50, 'Campo principal de la finca con suelo fértil'),
('Campo Secundario', 'Zona Sur - Coordenadas: -34.6040, -58.3820', 75.25, 'Campo secundario para cultivos de rotación'),
('Campo Experimental', 'Zona Este - Coordenadas: -34.6035, -58.3818', 25.00, 'Campo para pruebas y experimentos agrícolas')
ON DUPLICATE KEY UPDATE description = VALUES(description);

-- Insertar lotes de ejemplo
INSERT INTO plots (field_id, name, area_hectares, soil_type, description) VALUES 
((SELECT id FROM fields WHERE name = 'Campo Principal'), 'Lote A1', 25.00, 'Franco-arcilloso', 'Lote principal para soja'),
((SELECT id FROM fields WHERE name = 'Campo Principal'), 'Lote A2', 30.00, 'Franco-arcilloso', 'Lote para maíz'),
((SELECT id FROM fields WHERE name = 'Campo Secundario'), 'Lote B1', 20.00, 'Franco-limoso', 'Lote para trigo'),
((SELECT id FROM fields WHERE name = 'Campo Experimental'), 'Lote C1', 10.00, 'Franco-arenoso', 'Lote experimental')
ON DUPLICATE KEY UPDATE description = VALUES(description);

-- Insertar cultivos de ejemplo
INSERT INTO crops (plot_id, name, variety, planting_date, expected_harvest_date, status, notes) VALUES 
((SELECT id FROM plots WHERE name = 'Lote A1'), 'Soja', 'DM 53i54', '2024-11-15', '2025-04-15', 'ACTIVE', 'Cultivo principal de soja'),
((SELECT id FROM plots WHERE name = 'Lote A2'), 'Maíz', 'DK 72-10', '2024-10-01', '2025-03-15', 'ACTIVE', 'Maíz de segunda'),
((SELECT id FROM plots WHERE name = 'Lote B1'), 'Trigo', 'Baguette 19', '2024-06-15', '2024-12-15', 'ACTIVE', 'Trigo de invierno')
ON DUPLICATE KEY UPDATE notes = VALUES(notes);

-- Insertar insumos de ejemplo
INSERT INTO insumos (name, type, description, unit, price_per_unit, stock_quantity) VALUES 
('Glifosato 48%', 'Herbicida', 'Herbicida sistémico para control de malezas', 'Litro', 15.50, 100.00),
('Urea 46%', 'Fertilizante', 'Fertilizante nitrogenado', 'Kg', 2.80, 5000.00),
('Semilla Soja RR', 'Semilla', 'Semilla de soja resistente a glifosato', 'Kg', 8.50, 2000.00),
('Fungicida Tebuconazole', 'Fungicida', 'Fungicida para control de enfermedades', 'Litro', 25.00, 50.00),
('Insecticida Lambda', 'Insecticida', 'Insecticida para control de plagas', 'Litro', 18.00, 75.00)
ON DUPLICATE KEY UPDATE stock_quantity = VALUES(stock_quantity);

-- Insertar maquinaria de ejemplo
INSERT INTO maquinaria (name, type, model, year, description, status) VALUES 
('Tractor Principal', 'Tractor', 'John Deere 5075E', 2020, 'Tractor principal para labores agrícolas', 'ACTIVE'),
('Sembradora', 'Sembradora', 'Massey Ferguson 290', 2019, 'Sembradora de precisión', 'ACTIVE'),
('Pulverizadora', 'Pulverizadora', 'Jacto 2000', 2021, 'Pulverizadora autopropulsada', 'ACTIVE'),
('Cosechadora', 'Cosechadora', 'New Holland CR', 2018, 'Cosechadora de granos', 'ACTIVE')
ON DUPLICATE KEY UPDATE status = VALUES(status);

-- Insertar labores de ejemplo
INSERT INTO labores (plot_id, type, description, date, hours_worked, cost, notes) VALUES 
((SELECT id FROM plots WHERE name = 'Lote A1'), 'Siembra', 'Siembra de soja', '2024-11-15', 8.00, 1200.00, 'Siembra exitosa'),
((SELECT id FROM plots WHERE name = 'Lote A2'), 'Aplicación', 'Aplicación de herbicida', '2024-10-05', 4.00, 800.00, 'Control de malezas'),
((SELECT id FROM plots WHERE name = 'Lote B1'), 'Fertilización', 'Aplicación de urea', '2024-06-20', 6.00, 1500.00, 'Fertilización nitrogenada')
ON DUPLICATE KEY UPDATE notes = VALUES(notes);

-- ========================================
-- CREAR ÍNDICES PARA OPTIMIZACIÓN
-- ========================================

-- Índices para mejorar el rendimiento
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_plots_field_id ON plots(field_id);
CREATE INDEX idx_crops_plot_id ON crops(plot_id);
CREATE INDEX idx_labores_plot_id ON labores(plot_id);
CREATE INDEX idx_crops_status ON crops(status);
CREATE INDEX idx_maquinaria_status ON maquinaria(status);

-- ========================================
-- VERIFICACIÓN DE DATOS INSERTADOS
-- ========================================

-- Verificar datos insertados
SELECT 'Roles creados:' as info, COUNT(*) as cantidad FROM roles;
SELECT 'Usuarios creados:' as info, COUNT(*) as cantidad FROM users;
SELECT 'Campos creados:' as info, COUNT(*) as cantidad FROM fields;
SELECT 'Lotes creados:' as info, COUNT(*) as cantidad FROM plots;
SELECT 'Cultivos creados:' as info, COUNT(*) as cantidad FROM crops;
SELECT 'Insumos creados:' as info, COUNT(*) as cantidad FROM insumos;
SELECT 'Maquinaria creada:' as info, COUNT(*) as cantidad FROM maquinaria;
SELECT 'Labores creadas:' as info, COUNT(*) as cantidad FROM labores;

-- Mostrar usuarios con sus roles
SELECT u.username, u.email, GROUP_CONCAT(r.name) as roles
FROM users u
LEFT JOIN user_roles ur ON u.id = ur.user_id
LEFT JOIN roles r ON ur.role_id = r.id
GROUP BY u.id, u.username, u.email;

-- ========================================
-- SCRIPT COMPLETADO EXITOSAMENTE
-- ========================================
