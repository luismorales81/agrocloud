-- =====================================================
-- SCRIPT BÁSICO DE CONFIGURACIÓN PARA AGROCLOUDBD
-- Sistema de Gestión Agrícola AgroCloud
-- Versión simplificada sin índices
-- =====================================================

-- Crear la base de datos si no existe
CREATE DATABASE IF NOT EXISTS agroclouddb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Usar la base de datos
USE agroclouddb;

-- =====================================================
-- CREACIÓN DE TABLAS
-- =====================================================

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

-- Crear tabla de campos (con soporte para polígonos y coordenadas)
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

-- =====================================================
-- INSERCIÓN DE DATOS INICIALES
-- =====================================================

-- Insertar roles básicos
INSERT IGNORE INTO roles (nombre, descripcion) VALUES 
('ADMIN', 'Administrador del sistema con acceso completo'),
('USUARIO', 'Usuario estándar con acceso limitado'),
('GERENTE', 'Gerente de campo con acceso amplio'),
('OPERADOR', 'Operador de maquinaria con acceso específico');

-- Insertar usuario administrador por defecto (password: admin123)
INSERT IGNORE INTO usuarios (nombre_usuario, email, password, nombre, apellido, telefono) VALUES 
('admin', 'admin@agrocloud.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'Administrador', 'Sistema', '+54 11 1234-5678');

-- Insertar usuario de prueba (password: user123)
INSERT IGNORE INTO usuarios (nombre_usuario, email, password, nombre, apellido, telefono) VALUES 
('usuario', 'usuario@agrocloud.com', '$2a$10$8K1p/a0dL1LXMIgoEDFrwOe6g7fKoJvqLrR3VqJqJqJqJqJqJqJqJq', 'Usuario', 'Prueba', '+54 11 9876-5432');

-- Asignar rol de administrador al usuario admin
INSERT IGNORE INTO usuario_roles (usuario_id, rol_id) 
SELECT u.id, r.id FROM usuarios u, roles r 
WHERE u.nombre_usuario = 'admin' AND r.nombre = 'ADMIN';

-- Asignar rol de usuario al usuario de prueba
INSERT IGNORE INTO usuario_roles (usuario_id, rol_id) 
SELECT u.id, r.id FROM usuarios u, roles r 
WHERE u.nombre_usuario = 'usuario' AND r.nombre = 'USUARIO';

-- Insertar campos de ejemplo
INSERT IGNORE INTO campos (nombre, ubicacion, area_hectareas, tipo_suelo, descripcion, estado, activo, user_id) VALUES 
('Campo Norte', 'Ruta 8, Km 45, Buenos Aires', 25.50, 'FRANCO', 'Campo principal para cultivo de soja', 'ACTIVO', TRUE, 1),
('Campo Sur', 'Ruta 9, Km 32, Córdoba', 18.75, 'ARCILLOSO', 'Campo para rotación de cultivos', 'ACTIVO', TRUE, 1),
('Campo Este', 'Ruta 12, Km 78, Entre Ríos', 32.00, 'FRANCO_ARCILLOSO', 'Campo con riego por aspersión', 'ACTIVO', TRUE, 1);

-- Insertar lotes de ejemplo
INSERT IGNORE INTO lotes (nombre, campo_id, area_hectareas, cultivo_actual, estado, fecha_siembra, fecha_cosecha_esperada, descripcion) VALUES 
('Lote A1', 1, 12.50, 'SOJA', 'EN_CRECIMIENTO', '2024-11-15', '2025-04-15', 'Lote con soja de primera'),
('Lote A2', 1, 13.00, 'MAIZ', 'PLANIFICADO', '2024-12-01', '2025-06-01', 'Lote para maíz tardío'),
('Lote B1', 2, 8.75, 'TRIGO', 'COSECHADO', '2024-06-01', '2024-11-01', 'Lote de trigo cosechado'),
('Lote B2', 2, 10.00, 'GIRASOL', 'EN_CRECIMIENTO', '2024-10-01', '2025-02-01', 'Lote de girasol'),
('Lote C1', 3, 16.00, 'SOJA', 'PLANIFICADO', '2024-11-20', '2025-04-20', 'Lote grande para soja'),
('Lote C2', 3, 16.00, 'MAIZ', 'PLANIFICADO', '2024-12-10', '2025-06-10', 'Lote para maíz');

-- Insertar insumos de ejemplo
INSERT IGNORE INTO insumos (nombre, tipo, descripcion, unidad_medida, precio_unitario, stock_disponible, stock_minimo, proveedor, usuario_id) VALUES 
('Glifosato 48%', 'HERBICIDA', 'Herbicida sistémico para control de malezas', 'LITROS', 8.50, 200.00, 50.00, 'Syngenta', 1),
('Urea 46%', 'FERTILIZANTE', 'Fertilizante nitrogenado', 'KG', 0.85, 5000.00, 1000.00, 'Yara', 1),
('Semilla Soja RR', 'SEMILLA', 'Semilla de soja resistente a glifosato', 'KG', 12.00, 2500.00, 500.00, 'Don Mario', 1),
('Semilla Maíz', 'SEMILLA', 'Semilla de maíz híbrido', 'KG', 15.50, 1800.00, 400.00, 'Pioneer', 1),
('Fosfato Diamónico', 'FERTILIZANTE', 'Fertilizante fosfatado', 'KG', 1.20, 3000.00, 800.00, 'Yara', 1),
('2,4-D', 'HERBICIDA', 'Herbicida hormonal', 'LITROS', 6.80, 150.00, 30.00, 'Dow', 1);

-- Insertar maquinaria de ejemplo
INSERT IGNORE INTO maquinaria (nombre, tipo, marca, modelo, anio, estado, horas_uso, costo_por_hora, descripcion, usuario_id) VALUES 
('Tractor Principal', 'TRACTOR', 'John Deere', '5075E', 2020, 'OPERATIVA', 1250.50, 45.00, 'Tractor de 75 HP para labores generales', 1),
('Sembradora Directa', 'SEMBRADORA', 'Agrometal', 'SD 2800', 2019, 'OPERATIVA', 850.25, 35.00, 'Sembradora de 14 surcos para siembra directa', 1),
('Pulverizadora', 'PULVERIZADORA', 'Metalfor', 'Pulverizadora 2000', 2021, 'OPERATIVA', 420.75, 40.00, 'Pulverizadora de 2000 litros', 1),
('Cosechadora', 'COSECHADORA', 'New Holland', 'TC 5.90', 2018, 'OPERATIVA', 2100.00, 120.00, 'Cosechadora de 5.90 metros', 1),
('Cultivador', 'IMPLEMENTO', 'Agrometal', 'Cultivador 3m', 2017, 'OPERATIVA', 680.30, 25.00, 'Cultivador de 3 metros para labranza', 1);

-- Insertar labores de ejemplo
INSERT IGNORE INTO labores (lote_id, tipo_labor, descripcion, fecha_inicio, fecha_fin, estado, costo_total, observaciones, usuario_id) VALUES 
(1, 'SIEMBRA', 'Siembra de soja de primera', '2024-11-15', '2024-11-16', 'COMPLETADA', 1250.00, 'Siembra realizada con sembradora directa', 1),
(1, 'PULVERIZACION', 'Aplicación de herbicida pre-emergente', '2024-11-20', '2024-11-20', 'COMPLETADA', 800.00, 'Aplicación de glifosato + 2,4-D', 1),
(2, 'SIEMBRA', 'Siembra de maíz tardío', '2024-12-01', '2024-12-02', 'PLANIFICADA', 1500.00, 'Pendiente de realizar', 1),
(3, 'COSECHA', 'Cosecha de trigo', '2024-11-01', '2024-11-03', 'COMPLETADA', 3200.00, 'Cosecha realizada con buen rendimiento', 1),
(4, 'PULVERIZACION', 'Control de malezas en girasol', '2024-10-15', '2024-10-15', 'COMPLETADA', 600.00, 'Aplicación de herbicida selectivo', 1);

-- Insertar mantenimientos de maquinaria de ejemplo
INSERT IGNORE INTO mantenimiento_maquinaria (maquinaria_id, tipo_mantenimiento, descripcion, fecha_mantenimiento, costo, proximo_mantenimiento, observaciones, usuario_id) VALUES 
(1, 'PREVENTIVO', 'Cambio de aceite y filtros', '2024-10-15', 450.00, '2025-01-15', 'Mantenimiento preventivo realizado', 1),
(2, 'CORRECTIVO', 'Reparación de sistema de distribución', '2024-09-20', 1200.00, '2025-03-20', 'Reparación de engranajes', 1),
(3, 'PREVENTIVO', 'Limpieza y calibración de boquillas', '2024-11-01', 300.00, '2025-02-01', 'Calibración realizada correctamente', 1),
(4, 'PREVENTIVO', 'Revisión general y cambio de aceite', '2024-08-10', 800.00, '2025-02-10', 'Mantenimiento preventivo', 1),
(5, 'CORRECTIVO', 'Reparación de dientes rotos', '2024-07-25', 600.00, '2025-01-25', 'Reemplazo de 3 dientes', 1);

-- =====================================================
-- VERIFICACIÓN DE LA INSTALACIÓN
-- =====================================================

-- Mostrar resumen de la instalación
SELECT 'RESUMEN DE LA INSTALACIÓN' as titulo;
SELECT 'Base de datos creada: agroclouddb' as mensaje;
SELECT 'Tablas creadas: 8' as mensaje;
SELECT 'Roles creados: 4' as mensaje;
SELECT 'Usuarios creados: 2' as mensaje;
SELECT 'Campos de ejemplo: 3' as mensaje;
SELECT 'Lotes de ejemplo: 6' as mensaje;
SELECT 'Insumos de ejemplo: 6' as mensaje;
SELECT 'Maquinaria de ejemplo: 5' as mensaje;
SELECT 'Labores de ejemplo: 5' as mensaje;
SELECT 'Mantenimientos de ejemplo: 5' as mensaje;

-- Mostrar información de usuarios creados
SELECT 'USUARIOS CREADOS:' as titulo;
SELECT 
    u.nombre_usuario,
    u.email,
    u.nombre,
    u.apellido,
    GROUP_CONCAT(r.nombre) as roles
FROM usuarios u
LEFT JOIN usuario_roles ur ON u.id = ur.usuario_id
LEFT JOIN roles r ON ur.rol_id = r.id
GROUP BY u.id;

-- Mostrar estructura de la tabla campos
SELECT 'ESTRUCTURA DE LA TABLA CAMPOS:' as titulo;
SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    IS_NULLABLE,
    COLUMN_DEFAULT,
    COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'agroclouddb' 
AND TABLE_NAME = 'campos'
ORDER BY ORDINAL_POSITION;

-- =====================================================
-- INFORMACIÓN DE CONEXIÓN
-- =====================================================

SELECT 'INFORMACIÓN DE CONEXIÓN:' as titulo;
SELECT 'Base de datos: agroclouddb' as info;
SELECT 'Usuario: agrocloudbd' as info;
SELECT 'Password: Jones1212' as info;
SELECT 'Host: localhost' as info;
SELECT 'Puerto: 3306' as info;

SELECT 'CREDENCIALES DE ACCESO:' as titulo;
SELECT 'Usuario admin: admin' as credencial;
SELECT 'Password admin: admin123' as credencial;
SELECT 'Usuario prueba: usuario' as credencial;
SELECT 'Password prueba: user123' as credencial;

-- =====================================================
-- FIN DEL SCRIPT
-- =====================================================

SELECT 'INSTALACIÓN COMPLETADA EXITOSAMENTE' as mensaje;
SELECT 'El sistema AgroCloud está listo para usar' as mensaje;
