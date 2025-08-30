-- =====================================================
-- SCRIPT SIMPLIFICADO PARA CONFIGURAR AGROCLOUDBD
-- Ejecutar en phpMyAdmin: http://localhost/phpmyadmin
-- =====================================================

-- Crear la base de datos si no existe
CREATE DATABASE IF NOT EXISTS agroclouddb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Usar la base de datos
USE agroclouddb;

-- Crear usuario si no existe
CREATE USER IF NOT EXISTS 'agrocloudbd'@'localhost' IDENTIFIED BY 'Jones1212';
GRANT ALL PRIVILEGES ON agroclouddb.* TO 'agrocloudbd'@'localhost';
FLUSH PRIVILEGES;

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
    ano_fabricacion INT,
    estado VARCHAR(50) DEFAULT 'OPERATIVA',
    descripcion TEXT,
    usuario_id BIGINT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE SET NULL
);

-- Crear tabla de labores
CREATE TABLE IF NOT EXISTS labores (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    tipo VARCHAR(50) NOT NULL,
    descripcion TEXT,
    lote_id BIGINT,
    fecha_inicio DATE,
    fecha_fin DATE,
    estado VARCHAR(50) DEFAULT 'PLANIFICADA',
    usuario_id BIGINT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (lote_id) REFERENCES lotes(id) ON DELETE CASCADE,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE SET NULL
);

-- Insertar roles por defecto
INSERT IGNORE INTO roles (nombre, descripcion) VALUES
('ADMINISTRADOR', 'Administrador del sistema con acceso completo'),
('OPERARIO', 'Operario con acceso limitado a operaciones básicas'),
('INGENIERO_AGRONOMO', 'Ingeniero agrónomo con acceso técnico'),
('INVITADO', 'Usuario invitado con acceso de solo lectura');

-- Insertar usuario administrador por defecto
-- Password: admin123 (encriptado con BCrypt)
INSERT IGNORE INTO usuarios (nombre_usuario, email, password, nombre, apellido, activo) VALUES
('admin', 'admin@agrocloud.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'Administrador', 'Sistema', TRUE);

-- Asignar rol de administrador al usuario admin
INSERT IGNORE INTO usuario_roles (usuario_id, rol_id) 
SELECT u.id, r.id 
FROM usuarios u, roles r 
WHERE u.email = 'admin@agrocloud.com' AND r.nombre = 'ADMINISTRADOR';

-- Mensaje de confirmación
SELECT 'Base de datos agroclouddb configurada exitosamente' as mensaje;
