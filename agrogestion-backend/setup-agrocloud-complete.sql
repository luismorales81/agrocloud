-- ========================================
-- SCRIPT COMPLETO PARA AGROCLOUD
-- Recrear toda la base de datos desde cero
-- ========================================

-- Eliminar la base de datos si existe y recrearla
DROP DATABASE IF EXISTS agrocloud;
CREATE DATABASE agrocloud CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Usar la base de datos
USE agrocloud;

-- ========================================
-- CREACIÓN DE TABLAS DEL SISTEMA
-- ========================================

-- Crear tabla de roles
CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Crear tabla de usuarios
CREATE TABLE usuarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    phone VARCHAR(20),
    activo BOOLEAN DEFAULT TRUE,
    parent_user_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (parent_user_id) REFERENCES usuarios(id)
);

-- Crear tabla de relación usuarios-roles
CREATE TABLE usuarios_roles (
    usuario_id BIGINT NOT NULL,
    rol_id BIGINT NOT NULL,
    PRIMARY KEY (usuario_id, rol_id),
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    FOREIGN KEY (rol_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- Crear tabla de campos (actualizada para coincidir con Field.java)
CREATE TABLE campos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(255),
    ubicacion VARCHAR(200) NOT NULL,
    area_hectareas DECIMAL(10,2) NOT NULL,
    tipo_suelo VARCHAR(100),
    estado VARCHAR(100) DEFAULT 'ACTIVO',
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    poligono TEXT,
    coordenadas JSON,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    user_id BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES usuarios(id)
);

-- Crear tabla de lotes (actualizada para coincidir con Plot.java)
CREATE TABLE lotes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(255),
    area_hectareas DECIMAL(10,2) NOT NULL,
    estado VARCHAR(100) DEFAULT 'DISPONIBLE',
    tipo_suelo VARCHAR(100),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    campo_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    FOREIGN KEY (campo_id) REFERENCES campos(id),
    FOREIGN KEY (user_id) REFERENCES usuarios(id)
);

-- Crear tabla de insumos (actualizada para coincidir con Insumo.java)
CREATE TABLE insumos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(200) NOT NULL,
    descripcion VARCHAR(500),
    tipo ENUM('FERTILIZANTE', 'HERBICIDA', 'FUNGICIDA', 'INSECTICIDA', 'SEMILLA', 'COMBUSTIBLE', 'LUBRICANTE', 'REPUESTO', 'HERRAMIENTA', 'OTROS') NOT NULL,
    unidad VARCHAR(50) NOT NULL,
    precio_unitario DECIMAL(15,2) NOT NULL,
    stock_minimo DECIMAL(10,2) NOT NULL DEFAULT 0,
    stock_actual DECIMAL(10,2) NOT NULL DEFAULT 0,
    proveedor VARCHAR(200),
    fecha_vencimiento DATE,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    user_id BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES usuarios(id)
);

-- Crear tabla de maquinaria (actualizada para coincidir con Maquinaria.java)
CREATE TABLE maquinaria (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(255),
    tipo ENUM('TRACTOR', 'COSECHADORA', 'SEMBRADORA', 'PULVERIZADORA', 'ARADO', 'RASTRA', 'OTROS') NOT NULL,
    marca VARCHAR(100),
    modelo VARCHAR(100),
    año_fabricacion INT,
    numero_serie VARCHAR(100),
    estado ENUM('OPERATIVA', 'MANTENIMIENTO', 'REPARACION', 'FUERA_SERVICIO') DEFAULT 'OPERATIVA',
    horas_trabajo DECIMAL(10,2) DEFAULT 0,
    costo_adquisicion DECIMAL(15,2),
    valor_actual DECIMAL(15,2),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    user_id BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES usuarios(id)
);

-- Crear tabla de labores (actualizada para coincidir con Labor.java)
CREATE TABLE labores (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tipo_labor ENUM('SIEMBRA', 'FERTILIZACION', 'RIEGO', 'COSECHA', 'MANTENIMIENTO', 'PODA', 'CONTROL_PLAGAS', 'CONTROL_MALEZAS', 'ANALISIS_SUELO', 'OTROS') NOT NULL,
    nombre VARCHAR(200) NOT NULL,
    descripcion VARCHAR(500),
    fecha_labor DATE NOT NULL,
    area_hectareas DECIMAL(10,2),
    costo DECIMAL(15,2),
    estado ENUM('PLANIFICADA', 'EN_PROGRESO', 'COMPLETADA', 'CANCELADA') DEFAULT 'PLANIFICADA',
    observaciones VARCHAR(1000),
    lote_id BIGINT,
    maquinaria_id BIGINT,
    usuario_id BIGINT NOT NULL,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (lote_id) REFERENCES lotes(id),
    FOREIGN KEY (maquinaria_id) REFERENCES maquinaria(id),
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

-- Crear tabla de mantenimientos de maquinaria (actualizada para coincidir con MantenimientoMaquinaria.java)
CREATE TABLE mantenimientos_maquinaria (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    maquinaria_id BIGINT NOT NULL,
    tipo_mantenimiento ENUM('PREVENTIVO', 'CORRECTIVO', 'REPARACION', 'INSPECCION') NOT NULL,
    descripcion VARCHAR(500) NOT NULL,
    fecha_programada DATE NOT NULL,
    fecha_realizada DATE,
    horas_maquina DECIMAL(10,2),
    costo DECIMAL(15,2),
    estado ENUM('PROGRAMADO', 'EN_PROGRESO', 'COMPLETADO', 'CANCELADO') DEFAULT 'PROGRAMADO',
    observaciones VARCHAR(1000),
    proximo_mantenimiento DATE,
    horas_proximo_mantenimiento DECIMAL(10,2),
    usuario_id BIGINT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (maquinaria_id) REFERENCES maquinaria(id),
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

-- ========================================
-- INSERTAR DATOS INICIALES
-- ========================================

-- Insertar roles por defecto
INSERT INTO roles (name, description) VALUES
('ADMINISTRADOR', 'Acceso completo al sistema'),
('OPERARIO', 'Acceso limitado a operaciones básicas'),
('INGENIERO_AGRONOMO', 'Acceso técnico y de planificación'),
('INVITADO', 'Acceso de solo lectura');

-- Insertar usuario administrador
INSERT INTO usuarios (username, email, password, first_name, last_name, phone, activo) VALUES
('admin', 'admin@agrocloud.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Administrador', 'Sistema', '123456789', TRUE);

-- Asignar rol de administrador al usuario admin
INSERT INTO usuarios_roles (usuario_id, rol_id) VALUES
(1, 1);

-- Insertar campos de ejemplo (actualizados para coincidir con la nueva estructura)
INSERT INTO campos (nombre, descripcion, ubicacion, area_hectareas, tipo_suelo, estado, activo, user_id) VALUES
('Campo Norte', 'Campo principal para cultivos de verano', 'Ruta 9, Km 45', 150.50, 'Franco arcilloso', 'ACTIVO', TRUE, 1),
('Campo Sur', 'Campo para cultivos de invierno', 'Ruta 9, Km 47', 200.00, 'Franco arenoso', 'ACTIVO', TRUE, 1),
('Campo Este', 'Campo para rotación de cultivos', 'Ruta 11, Km 23', 75.25, 'Arcilloso', 'ACTIVO', TRUE, 1);

-- Insertar lotes de ejemplo
INSERT INTO lotes (nombre, descripcion, area_hectareas, estado, activo, campo_id, user_id) VALUES
('Lote A1', 'Lote principal para cultivos de soja', 25.50, 'DISPONIBLE', TRUE, 1, 1),
('Lote A2', 'Lote para cultivos de maíz', 30.00, 'DISPONIBLE', TRUE, 1, 1),
('Lote B1', 'Lote para rotación de cultivos', 40.00, 'DISPONIBLE', TRUE, 2, 1),
('Lote B2', 'Lote para cultivos de girasol', 35.00, 'DISPONIBLE', TRUE, 2, 1),
('Lote C1', 'Lote para cultivos de sorgo', 20.00, 'DISPONIBLE', TRUE, 3, 1);

-- Insertar insumos de ejemplo (actualizados para coincidir con la nueva estructura)
INSERT INTO insumos (nombre, descripcion, tipo, unidad, precio_unitario, stock_minimo, stock_actual, proveedor, activo, user_id) VALUES
('Fertilizante NPK', 'Fertilizante balanceado 15-15-15', 'FERTILIZANTE', 'kg', 2.50, 100.00, 500.00, 'AgroSupply S.A.', TRUE, 1),
('Herbicida Glifosato', 'Herbicida no selectivo', 'HERBICIDA', 'L', 8.00, 20.00, 100.00, 'Química Agrícola', TRUE, 1),
('Semilla de Soja', 'Semilla certificada de soja', 'SEMILLA', 'kg', 3.20, 500.00, 2000.00, 'Semillas Premium', TRUE, 1),
('Semilla de Maíz', 'Semilla híbrida de maíz', 'SEMILLA', 'kg', 4.50, 300.00, 1500.00, 'Semillas Premium', TRUE, 1),
('Fungicida', 'Fungicida preventivo', 'FUNGICIDA', 'L', 12.00, 10.00, 50.00, 'AgroSupply S.A.', TRUE, 1);

-- Insertar maquinaria de ejemplo (actualizados para coincidir con la nueva estructura)
INSERT INTO maquinaria (nombre, descripcion, tipo, marca, modelo, año_fabricacion, numero_serie, estado, horas_trabajo, costo_adquisicion, valor_actual, activo, user_id) VALUES
('Tractor John Deere 5075E', 'Tractor de 75 HP para labores generales', 'TRACTOR', 'John Deere', '5075E', 2020, 'JD-5075E-001', 'OPERATIVA', 1200.00, 150000.00, 145000.00, TRUE, 1),
('Sembradora de Precisión', 'Sembradora de 12 líneas', 'SEMBRADORA', 'Agrometal', 'SP-12', 2019, 'AG-SP12-001', 'OPERATIVA', 400.00, 80000.00, 78000.00, TRUE, 1),
('Pulverizadora', 'Pulverizadora de 600L', 'PULVERIZADORA', 'Metalfor', 'P-600', 2021, 'MF-P600-001', 'OPERATIVA', 600.00, 50000.00, 48000.00, TRUE, 1),
('Cosechadora', 'Cosechadora de 6 metros', 'COSECHADORA', 'New Holland', 'CR-6', 2018, 'NH-CR6-001', 'OPERATIVA', 1800.00, 250000.00, 245000.00, TRUE, 1);

-- Insertar labores de ejemplo (actualizados para coincidir con la nueva estructura)
INSERT INTO labores (tipo_labor, nombre, descripcion, fecha_labor, area_hectareas, costo, estado, observaciones, lote_id, maquinaria_id, usuario_id) VALUES
('SIEMBRA', 'Siembra de soja', 'Siembra de soja en lote A1', '2024-08-25', 25.50, 140.00, 'COMPLETADA', 'Siembra de soja en lote A1', 1, 2, 1),
('FERTILIZACION', 'Aplicación de fertilizante NPK', 'Aplicación de fertilizante NPK', '2024-08-26', 25.50, 90.00, 'PLANIFICADA', 'Aplicación de fertilizante NPK', 1, 1, 1),
('CONTROL_MALEZAS', 'Control de malezas', 'Control de malezas', '2024-08-27', 30.00, 120.00, 'EN_PROGRESO', 'Control de malezas', 2, 3, 1);

-- Insertar mantenimientos de ejemplo
INSERT INTO mantenimientos_maquinaria (maquinaria_id, tipo_mantenimiento, descripcion, fecha_programada, fecha_realizada, horas_maquina, costo, estado, observaciones, proximo_mantenimiento, horas_proximo_mantenimiento, usuario_id) VALUES
(1, 'PREVENTIVO', 'Cambio de aceite y filtros', '2024-08-20', NULL, 2.5, 150.00, 'PROGRAMADO', NULL, '2024-09-20', 10.0, 1),
(2, 'CORRECTIVO', 'Reparación de sistema hidráulico', '2024-08-15', NULL, 1.0, 300.00, 'PROGRAMADO', NULL, '2024-09-15', 0.5, 1);

-- ========================================
-- VERIFICACIÓN FINAL
-- ========================================

-- Verificar que todas las tablas fueron creadas
SELECT 'Tablas creadas:' as info;
SHOW TABLES;

-- Verificar datos insertados
SELECT 'Roles creados:' as info;
SELECT * FROM roles;

SELECT 'Usuarios creados:' as info;
SELECT id, username, email, first_name, last_name, activo FROM usuarios;

SELECT 'Campos creados:' as info;
SELECT * FROM campos;

SELECT 'Lotes creados:' as info;
SELECT * FROM lotes;

SELECT 'Insumos creados:' as info;
SELECT * FROM insumos;

SELECT 'Maquinaria creada:' as info;
SELECT * FROM maquinaria;

SELECT 'Labores creadas:' as info;
SELECT * FROM labores;

SELECT 'Mantenimientos creados:' as info;
SELECT * FROM mantenimientos_maquinaria;

-- Verificar restricciones de foreign key
SELECT 'Verificando restricciones de foreign key...' as info;
SELECT 
    TABLE_NAME,
    COLUMN_NAME,
    CONSTRAINT_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE 
WHERE REFERENCED_TABLE_SCHEMA = 'agrocloud'
ORDER BY TABLE_NAME, COLUMN_NAME;

SELECT 'Base de datos AgroCloud creada exitosamente!' as mensaje;
