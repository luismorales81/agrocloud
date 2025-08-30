-- Script de inicialización para MySQL
-- Crear la base de datos si no existe
CREATE DATABASE IF NOT EXISTS agroclouddb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Usar la base de datos
USE agroclouddb;

-- Crear tabla de roles
CREATE TABLE IF NOT EXISTS roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE,
    descripcion VARCHAR(255),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Crear tabla de usuarios
CREATE TABLE IF NOT EXISTS usuarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre_usuario VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    telefono VARCHAR(20),
    activo BOOLEAN DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Crear tabla de relación usuario-rol
CREATE TABLE IF NOT EXISTS usuario_roles (
    usuario_id BIGINT NOT NULL,
    rol_id BIGINT NOT NULL,
    PRIMARY KEY (usuario_id, rol_id),
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    FOREIGN KEY (rol_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- Crear tabla de campos
CREATE TABLE IF NOT EXISTS campos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    ubicacion VARCHAR(255),
    area_hectareas DECIMAL(10,2),
    tipo_suelo VARCHAR(50),
    descripcion TEXT,
    poligono TEXT COMMENT 'Polígono del campo en formato GeoJSON',
    coordenadas JSON COMMENT 'Coordenadas del campo como array de puntos lat/lng',
    estado VARCHAR(100) DEFAULT 'ACTIVO',
    activo BOOLEAN DEFAULT TRUE,
    user_id BIGINT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES usuarios(id) ON DELETE SET NULL
);

-- Crear tabla de lotes
CREATE TABLE IF NOT EXISTS lotes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    campo_id BIGINT NOT NULL,
    area_hectareas DECIMAL(10,2),
    cultivo_actual VARCHAR(100),
    estado VARCHAR(50) DEFAULT 'DISPONIBLE',
    fecha_siembra DATE,
    fecha_cosecha_esperada DATE,
    descripcion TEXT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (campo_id) REFERENCES campos(id) ON DELETE CASCADE
);

-- Crear tabla de insumos
CREATE TABLE IF NOT EXISTS insumos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    tipo VARCHAR(50) NOT NULL,
    descripcion TEXT,
    unidad_medida VARCHAR(20),
    precio_unitario DECIMAL(10,2),
    stock_disponible DECIMAL(10,2) DEFAULT 0,
    stock_minimo DECIMAL(10,2) DEFAULT 0,
    proveedor VARCHAR(100),
    usuario_id BIGINT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE SET NULL
);

-- Crear tabla de maquinaria
CREATE TABLE IF NOT EXISTS maquinaria (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    tipo VARCHAR(50) NOT NULL,
    marca VARCHAR(50),
    modelo VARCHAR(50),
    anio INTEGER,
    estado VARCHAR(50) DEFAULT 'OPERATIVA',
    horas_uso DECIMAL(10,2) DEFAULT 0,
    costo_por_hora DECIMAL(10,2),
    descripcion TEXT,
    usuario_id BIGINT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE SET NULL
);

-- Crear tabla de labores
CREATE TABLE IF NOT EXISTS labores (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    lote_id BIGINT NOT NULL,
    tipo_labor VARCHAR(50) NOT NULL,
    descripcion TEXT,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE,
    estado VARCHAR(50) DEFAULT 'PLANIFICADA',
    costo_total DECIMAL(10,2),
    observaciones TEXT,
    usuario_id BIGINT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (lote_id) REFERENCES lotes(id) ON DELETE CASCADE,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE SET NULL
);

-- Crear tabla de mantenimiento de maquinaria
CREATE TABLE IF NOT EXISTS mantenimiento_maquinaria (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    maquinaria_id BIGINT NOT NULL,
    tipo_mantenimiento VARCHAR(50) NOT NULL,
    descripcion TEXT,
    fecha_mantenimiento DATE NOT NULL,
    costo DECIMAL(10,2),
    proximo_mantenimiento DATE,
    observaciones TEXT,
    usuario_id BIGINT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (maquinaria_id) REFERENCES maquinaria(id) ON DELETE CASCADE,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE SET NULL
);

-- Insertar roles básicos
INSERT IGNORE INTO roles (nombre, descripcion) VALUES 
('ADMIN', 'Administrador del sistema'),
('USUARIO', 'Usuario estándar'),
('GERENTE', 'Gerente de campo');

-- Insertar usuario administrador por defecto (password: admin123)
INSERT IGNORE INTO usuarios (nombre_usuario, email, password, nombre, apellido) VALUES 
('admin', 'admin@agrocloud.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'Administrador', 'Sistema');

-- Asignar rol de administrador al usuario admin
INSERT IGNORE INTO usuario_roles (usuario_id, rol_id) 
SELECT u.id, r.id FROM usuarios u, roles r 
WHERE u.nombre_usuario = 'admin' AND r.nombre = 'ADMIN';
