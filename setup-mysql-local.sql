-- ========================================
-- Script para configurar la base de datos MySQL local para AgroGestion
-- Ejecutar este script en phpMyAdmin o en la línea de comandos de MySQL
-- Usuario: agrocloudbd
-- Contraseña: Jones1212
-- Puerto: 3306
-- ========================================

-- Crear la base de datos
CREATE DATABASE IF NOT EXISTS agrocloud CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Usar la base de datos
USE agrocloud;

-- ========================================
-- CREACIÓN DE TABLAS DEL SISTEMA
-- ========================================

-- Crear tabla de roles
CREATE TABLE IF NOT EXISTS roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Crear tabla de usuarios
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    phone VARCHAR(20),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Crear tabla de relación usuario-rol
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT,
    role_id BIGINT,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- Crear tabla de campos
CREATE TABLE IF NOT EXISTS fields (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    location VARCHAR(255),
    area_hectares DECIMAL(10,2),
    description TEXT,
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES users(id)
);

-- Crear tabla de lotes
CREATE TABLE IF NOT EXISTS plots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    field_id BIGINT NOT NULL,
    area_hectares DECIMAL(10,2),
    soil_type VARCHAR(50),
    description TEXT,
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (field_id) REFERENCES fields(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(id)
);

-- Crear tabla de cultivos
CREATE TABLE IF NOT EXISTS crops (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    variety VARCHAR(100),
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Crear tabla de insumos
CREATE TABLE IF NOT EXISTS insumos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    unit VARCHAR(20),
    stock_quantity DECIMAL(10,2) DEFAULT 0,
    min_stock DECIMAL(10,2) DEFAULT 0,
    price_per_unit DECIMAL(10,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Crear tabla de maquinaria
CREATE TABLE IF NOT EXISTS maquinaria (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(50),
    model VARCHAR(100),
    year INTEGER,
    description TEXT,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Crear tabla de labores
CREATE TABLE IF NOT EXISTS labores (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    plot_id BIGINT NOT NULL,
    crop_id BIGINT,
    labor_type VARCHAR(50) NOT NULL,
    description TEXT,
    date DATE NOT NULL,
    hours_worked DECIMAL(5,2),
    cost DECIMAL(10,2),
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (plot_id) REFERENCES plots(id) ON DELETE CASCADE,
    FOREIGN KEY (crop_id) REFERENCES crops(id),
    FOREIGN KEY (created_by) REFERENCES users(id)
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
INSERT INTO user_roles (user_id, role_id) 
SELECT u.id, r.id FROM users u, roles r 
WHERE u.username = 'admin' AND r.name = 'ADMIN'
ON DUPLICATE KEY UPDATE user_id = user_id;

INSERT INTO user_roles (user_id, role_id) 
SELECT u.id, r.id FROM users u, roles r 
WHERE u.username = 'tecnico' AND r.name = 'TECNICO'
ON DUPLICATE KEY UPDATE user_id = user_id;

INSERT INTO user_roles (user_id, role_id) 
SELECT u.id, r.id FROM users u, roles r 
WHERE u.username = 'productor' AND r.name = 'PRODUCTOR'
ON DUPLICATE KEY UPDATE user_id = user_id;

-- Insertar campos de ejemplo
INSERT INTO fields (name, location, area_hectares, description, created_by) VALUES 
('Campo Norte', 'Ruta 5 km 120, Provincia de Buenos Aires', 50.5, 'Campo principal de producción con riego por aspersión', 1),
('Campo Sur', 'Ruta 8 km 45, Provincia de Buenos Aires', 30.2, 'Campo secundario con suelo franco-arcilloso', 1),
('Campo Este', 'Ruta 2 km 180, Provincia de Buenos Aires', 25.8, 'Campo para cultivos de verano', 1)
ON DUPLICATE KEY UPDATE location = VALUES(location);

-- Insertar lotes de ejemplo
INSERT INTO plots (name, field_id, area_hectares, soil_type, description, created_by) VALUES 
('Lote A1', 1, 10.5, 'Franco', 'Lote principal para soja con excelente drenaje', 1),
('Lote A2', 1, 8.2, 'Franco-arcilloso', 'Lote para maíz con riego complementario', 1),
('Lote A3', 1, 12.0, 'Arcilloso', 'Lote para trigo con buena retención de humedad', 1),
('Lote B1', 2, 15.0, 'Arcilloso', 'Lote para trigo con suelo fértil', 1),
('Lote B2', 2, 8.5, 'Franco', 'Lote para girasol', 1),
('Lote C1', 3, 12.8, 'Franco-arcilloso', 'Lote para soja de segunda', 1)
ON DUPLICATE KEY UPDATE area_hectares = VALUES(area_hectares);

-- Insertar cultivos de ejemplo
INSERT INTO crops (name, variety, description) VALUES 
('Soja', 'DM 53i54', 'Soja de ciclo corto, resistente a sequía'),
('Maíz', 'DK 72-10', 'Maíz híbrido de alto rendimiento'),
('Trigo', 'Baguette 19', 'Trigo pan de calidad panadera'),
('Girasol', 'Paraíso 33', 'Girasol confitero de alto contenido oleico'),
('Sorgo', 'DK 46-15', 'Sorgo granífero para alimentación animal')
ON DUPLICATE KEY UPDATE variety = VALUES(variety);

-- Insertar insumos de ejemplo
INSERT INTO insumos (name, description, unit, stock_quantity, min_stock, price_per_unit) VALUES 
('Urea', 'Fertilizante nitrogenado 46-0-0', 'kg', 1000.0, 100.0, 0.8),
('Fosfato Diamónico', 'Fertilizante 18-46-0', 'kg', 800.0, 80.0, 1.2),
('Glifosato', 'Herbicida no selectivo', 'L', 50.0, 10.0, 5.5),
('Semillas Soja', 'Semillas certificadas de soja', 'kg', 500.0, 50.0, 2.5),
('Semillas Maíz', 'Semillas híbridas de maíz', 'kg', 300.0, 30.0, 3.8),
('Fungicida', 'Fungicida sistémico para cereales', 'L', 25.0, 5.0, 8.5),
('Insecticida', 'Insecticida para control de plagas', 'L', 40.0, 8.0, 6.2)
ON DUPLICATE KEY UPDATE stock_quantity = VALUES(stock_quantity);

-- Insertar maquinaria de ejemplo
INSERT INTO maquinaria (name, type, model, year, description) VALUES 
('Tractor John Deere', 'Tractor', '5075E', 2020, 'Tractor de 75 HP con cabina climatizada'),
('Sembradora', 'Sembradora', 'MaxEmerge 5', 2019, 'Sembradora de 5 surcos con dosificador neumático'),
('Pulverizadora', 'Pulverizadora', 'Jacto 600L', 2021, 'Pulverizadora de 600L con barras hidráulicas'),
('Cosechadora', 'Cosechadora', 'New Holland CR', 2018, 'Cosechadora de 5.5 metros de ancho'),
('Arado', 'Arado', 'Rastra de discos', 2017, 'Rastra de discos de 3 metros')
ON DUPLICATE KEY UPDATE year = VALUES(year);

-- Insertar labores de ejemplo
INSERT INTO labores (plot_id, crop_id, labor_type, description, date, hours_worked, cost, created_by) VALUES 
(1, 1, 'Siembra', 'Siembra de soja con sembradora de 5 surcos', '2024-11-15', 8.0, 1200.0, 1),
(2, 2, 'Fertilización', 'Aplicación de urea en maíz', '2024-11-20', 4.0, 800.0, 1),
(3, 3, 'Pulverización', 'Control de malezas en trigo', '2024-11-25', 6.0, 900.0, 1),
(4, 3, 'Siembra', 'Siembra de trigo con sembradora', '2024-05-10', 10.0, 1500.0, 1),
(5, 4, 'Cosecha', 'Cosecha de girasol', '2024-03-15', 12.0, 2000.0, 1)
ON DUPLICATE KEY UPDATE cost = VALUES(cost);

-- ========================================
-- CREAR ÍNDICES PARA OPTIMIZAR RENDIMIENTO
-- ========================================

-- Índices para usuarios
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_active ON users(is_active);

-- Índices para campos
CREATE INDEX idx_fields_name ON fields(name);
CREATE INDEX idx_fields_created_by ON fields(created_by);

-- Índices para lotes
CREATE INDEX idx_plots_name ON plots(name);
CREATE INDEX idx_plots_field_id ON plots(field_id);
CREATE INDEX idx_plots_created_by ON plots(created_by);

-- Índices para cultivos
CREATE INDEX idx_crops_name ON crops(name);
CREATE INDEX idx_crops_variety ON crops(variety);

-- Índices para insumos
CREATE INDEX idx_insumos_name ON insumos(name);
CREATE INDEX idx_insumos_stock ON insumos(stock_quantity);

-- Índices para maquinaria
CREATE INDEX idx_maquinaria_name ON maquinaria(name);
CREATE INDEX idx_maquinaria_type ON maquinaria(type);
CREATE INDEX idx_maquinaria_status ON maquinaria(status);

-- Índices para labores
CREATE INDEX idx_labores_plot_id ON labores(plot_id);
CREATE INDEX idx_labores_crop_id ON labores(crop_id);
CREATE INDEX idx_labores_date ON labores(date);
CREATE INDEX idx_labores_type ON labores(labor_type);
CREATE INDEX idx_labores_created_by ON labores(created_by);

-- ========================================
-- VERIFICACIÓN Y CONFIRMACIÓN
-- ========================================

-- Mostrar resumen de la configuración
SELECT 'Base de datos agrocloud configurada exitosamente' AS mensaje;

-- Mostrar estadísticas de las tablas creadas
SELECT 
    'Roles' AS tabla, COUNT(*) AS registros FROM roles
UNION ALL
SELECT 'Usuarios', COUNT(*) FROM users
UNION ALL
SELECT 'Campos', COUNT(*) FROM fields
UNION ALL
SELECT 'Lotes', COUNT(*) FROM plots
UNION ALL
SELECT 'Cultivos', COUNT(*) FROM crops
UNION ALL
SELECT 'Insumos', COUNT(*) FROM insumos
UNION ALL
SELECT 'Maquinaria', COUNT(*) FROM maquinaria
UNION ALL
SELECT 'Labores', COUNT(*) FROM labores;

-- Mostrar usuarios creados
SELECT username, email, first_name, last_name FROM users;

-- Mostrar roles disponibles
SELECT name, description FROM roles;
