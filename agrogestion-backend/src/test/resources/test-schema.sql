-- ========================================
-- ESQUEMA DE BASE DE DATOS PARA PRUEBAS
-- ========================================
-- Script para crear la estructura de base de datos para pruebas automatizadas

-- Crear base de datos de pruebas si no existe
CREATE DATABASE IF NOT EXISTS agrocloud_test CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE agrocloud_test;

-- Crear tabla de roles
CREATE TABLE IF NOT EXISTS roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE,
    descripcion VARCHAR(255),
    activo BOOLEAN DEFAULT TRUE,
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

-- Crear tabla de empresas
CREATE TABLE IF NOT EXISTS empresas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(200) NOT NULL,
    descripcion TEXT,
    direccion VARCHAR(500),
    telefono VARCHAR(20),
    email VARCHAR(100),
    estado VARCHAR(50) DEFAULT 'ACTIVO',
    activo BOOLEAN DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Crear tabla de relación usuario-empresa-rol
CREATE TABLE IF NOT EXISTS usuario_empresa_roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    empresa_id BIGINT NOT NULL,
    rol_nombre VARCHAR(50) NOT NULL,
    estado VARCHAR(50) DEFAULT 'ACTIVO',
    fecha_asignacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE
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
    usuario_id BIGINT,
    empresa_id BIGINT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE SET NULL,
    FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE SET NULL
);

-- Crear tabla de lotes
CREATE TABLE IF NOT EXISTS lotes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(255),
    area_hectareas DECIMAL(10,2) NOT NULL,
    estado VARCHAR(100) DEFAULT 'DISPONIBLE',
    fecha_ultimo_cambio_estado TIMESTAMP,
    motivo_cambio_estado VARCHAR(500),
    tipo_suelo VARCHAR(100),
    activo BOOLEAN DEFAULT TRUE,
    campo_id BIGINT,
    usuario_id BIGINT NOT NULL,
    empresa_id BIGINT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (campo_id) REFERENCES campos(id) ON DELETE SET NULL,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE SET NULL
);

-- Crear tabla de cultivos
CREATE TABLE IF NOT EXISTS cultivos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    tipo VARCHAR(50) NOT NULL,
    variedad VARCHAR(100) NOT NULL,
    ciclo_dias INTEGER,
    rendimiento_esperado DECIMAL(10,2),
    unidad_rendimiento VARCHAR(50),
    precio_por_tonelada DECIMAL(10,2),
    descripcion TEXT,
    estado VARCHAR(50) DEFAULT 'ACTIVO',
    activo BOOLEAN DEFAULT TRUE,
    usuario_id BIGINT,
    empresa_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE SET NULL,
    FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE SET NULL
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
    activo BOOLEAN DEFAULT TRUE,
    usuario_id BIGINT,
    empresa_id BIGINT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE SET NULL,
    FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE SET NULL
);

-- Crear tabla de maquinaria
CREATE TABLE IF NOT EXISTS maquinaria (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    tipo VARCHAR(50) NOT NULL,
    marca VARCHAR(50),
    modelo VARCHAR(50),
    año INTEGER,
    descripcion TEXT,
    estado VARCHAR(50) DEFAULT 'DISPONIBLE',
    costo_por_hora DECIMAL(10,2),
    activo BOOLEAN DEFAULT TRUE,
    usuario_id BIGINT,
    empresa_id BIGINT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE SET NULL,
    FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE SET NULL
);

-- Crear tabla de labores
CREATE TABLE IF NOT EXISTS labores (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tipo_labor VARCHAR(50) NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE,
    descripcion TEXT,
    costo_total DECIMAL(10,2),
    estado VARCHAR(50) DEFAULT 'PLANIFICADA',
    observaciones VARCHAR(1000),
    responsable VARCHAR(255),
    activo BOOLEAN DEFAULT TRUE,
    lote_id BIGINT,
    usuario_id BIGINT NOT NULL,
    empresa_id BIGINT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (lote_id) REFERENCES lotes(id) ON DELETE SET NULL,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE SET NULL
);

-- Crear tabla de labor_insumos
CREATE TABLE IF NOT EXISTS labor_insumos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    labor_id BIGINT NOT NULL,
    insumo_id BIGINT NOT NULL,
    cantidad_usada DECIMAL(10,2) NOT NULL,
    costo_unitario DECIMAL(10,2),
    costo_total DECIMAL(10,2),
    FOREIGN KEY (labor_id) REFERENCES labores(id) ON DELETE CASCADE,
    FOREIGN KEY (insumo_id) REFERENCES insumos(id) ON DELETE CASCADE
);

-- Crear tabla de labor_maquinaria
CREATE TABLE IF NOT EXISTS labor_maquinaria (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    labor_id BIGINT NOT NULL,
    maquinaria_id BIGINT NOT NULL,
    horas_uso DECIMAL(10,2) NOT NULL,
    costo_por_hora DECIMAL(10,2),
    costo_total DECIMAL(10,2),
    FOREIGN KEY (labor_id) REFERENCES labores(id) ON DELETE CASCADE,
    FOREIGN KEY (maquinaria_id) REFERENCES maquinaria(id) ON DELETE CASCADE
);

-- Crear tabla de labor_mano_obra
CREATE TABLE IF NOT EXISTS labor_mano_obra (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    labor_id BIGINT NOT NULL,
    descripcion VARCHAR(255) NOT NULL,
    horas_trabajadas DECIMAL(10,2) NOT NULL,
    costo_por_hora DECIMAL(10,2),
    costo_total DECIMAL(10,2),
    FOREIGN KEY (labor_id) REFERENCES labores(id) ON DELETE CASCADE
);

-- Crear tabla de ingresos
CREATE TABLE IF NOT EXISTS ingresos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    concepto VARCHAR(200) NOT NULL,
    descripcion VARCHAR(500),
    tipo_ingreso VARCHAR(50) NOT NULL,
    fecha_ingreso DATE NOT NULL,
    monto DECIMAL(15,2) NOT NULL,
    unidad_medida VARCHAR(100),
    cantidad DECIMAL(10,2),
    cliente_comprador VARCHAR(200),
    estado VARCHAR(50) DEFAULT 'REGISTRADO',
    observaciones VARCHAR(1000),
    lote_id BIGINT,
    usuario_id BIGINT NOT NULL,
    empresa_id BIGINT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (lote_id) REFERENCES lotes(id) ON DELETE SET NULL,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE SET NULL
);

-- Crear tabla de egresos
CREATE TABLE IF NOT EXISTS egresos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    concepto VARCHAR(200) NOT NULL,
    descripcion VARCHAR(500),
    tipo_egreso VARCHAR(50) NOT NULL,
    fecha_egreso DATE NOT NULL,
    monto DECIMAL(15,2) NOT NULL,
    proveedor VARCHAR(200),
    estado VARCHAR(50) DEFAULT 'PENDIENTE',
    observaciones VARCHAR(1000),
    lote_id BIGINT,
    usuario_id BIGINT NOT NULL,
    empresa_id BIGINT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (lote_id) REFERENCES lotes(id) ON DELETE SET NULL,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE SET NULL
);

-- Crear tabla de cosechas
CREATE TABLE IF NOT EXISTS cosechas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    lote_id BIGINT NOT NULL,
    cultivo_id BIGINT NOT NULL,
    fecha_cosecha DATE NOT NULL,
    cantidad_cosechada DECIMAL(10,2) NOT NULL,
    unidad_medida VARCHAR(50) NOT NULL,
    rendimiento DECIMAL(10,2),
    precio_por_unidad DECIMAL(10,2),
    valor_total DECIMAL(15,2),
    observaciones TEXT,
    activo BOOLEAN DEFAULT TRUE,
    usuario_id BIGINT NOT NULL,
    empresa_id BIGINT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (lote_id) REFERENCES lotes(id) ON DELETE CASCADE,
    FOREIGN KEY (cultivo_id) REFERENCES cultivos(id) ON DELETE CASCADE,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE SET NULL
);

-- Crear tabla de historial_cosechas
CREATE TABLE IF NOT EXISTS historial_cosechas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    lote_id BIGINT NOT NULL,
    cultivo_id BIGINT NOT NULL,
    fecha_cosecha DATE NOT NULL,
    cantidad_cosechada DECIMAL(10,2) NOT NULL,
    unidad_medida VARCHAR(50) NOT NULL,
    rendimiento DECIMAL(10,2),
    precio_por_unidad DECIMAL(10,2),
    valor_total DECIMAL(15,2),
    observaciones TEXT,
    usuario_id BIGINT NOT NULL,
    empresa_id BIGINT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (lote_id) REFERENCES lotes(id) ON DELETE CASCADE,
    FOREIGN KEY (cultivo_id) REFERENCES cultivos(id) ON DELETE CASCADE,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE SET NULL
);
