-- Script SQL para crear las tablas del sistema multiempresa
-- Sistema de gestión de usuarios, empresas, roles y permisos

-- Crear base de datos si no existe
-- CREATE DATABASE IF NOT EXISTS agrocloud_multitenant;
-- USE agrocloud_multitenant;

-- Tabla de usuarios
CREATE TABLE IF NOT EXISTS usuarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_activo (activo)
);

-- Tabla de empresas
CREATE TABLE IF NOT EXISTS empresas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    cuit VARCHAR(13) NOT NULL UNIQUE,
    description VARCHAR(255),
    direccion VARCHAR(100),
    ciudad VARCHAR(50),
    provincia VARCHAR(50),
    codigo_postal VARCHAR(20),
    telefono VARCHAR(20),
    email VARCHAR(100),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_nombre (nombre),
    INDEX idx_cuit (cuit),
    INDEX idx_activo (activo)
);

-- Tabla de roles
CREATE TABLE IF NOT EXISTS roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_nombre (nombre),
    INDEX idx_activo (activo)
);

-- Tabla de permisos
CREATE TABLE IF NOT EXISTS permisos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    modulo VARCHAR(50),
    accion VARCHAR(50),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_nombre (nombre),
    INDEX idx_modulo (modulo),
    INDEX idx_accion (accion),
    INDEX idx_activo (activo)
);

-- Tabla intermedia: usuarios_empresas_roles
CREATE TABLE IF NOT EXISTS usuarios_empresas_roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    empresa_id BIGINT NOT NULL,
    rol_id BIGINT NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    UNIQUE KEY uk_usuario_empresa_rol (usuario_id, empresa_id, rol_id),
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE,
    FOREIGN KEY (rol_id) REFERENCES roles(id) ON DELETE CASCADE,
    
    INDEX idx_usuario (usuario_id),
    INDEX idx_empresa (empresa_id),
    INDEX idx_rol (rol_id),
    INDEX idx_activo (activo)
);

-- Tabla intermedia: roles_permisos
CREATE TABLE IF NOT EXISTS roles_permisos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rol_id BIGINT NOT NULL,
    permiso_id BIGINT NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    UNIQUE KEY uk_rol_permiso (rol_id, permiso_id),
    FOREIGN KEY (rol_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (permiso_id) REFERENCES permisos(id) ON DELETE CASCADE,
    
    INDEX idx_rol (rol_id),
    INDEX idx_permiso (permiso_id),
    INDEX idx_activo (activo)
);

-- Insertar roles básicos del sistema (estructura corregida)
INSERT INTO roles (nombre, description) VALUES
('SUPERADMIN', 'Super administrador con control total del sistema'),
('ADMIN_EMPRESA', 'Administrador de empresa con acceso completo a la empresa'),
('PRODUCTOR', 'Productor agrícola'),
('ASESOR', 'Asesor técnico'),
('CONTADOR', 'Contador de la empresa'),
('TECNICO', 'Técnico agrícola'),
('OPERARIO', 'Operario de campo'),
('LECTURA', 'Usuario con permisos de solo lectura'),
('USUARIO_REGISTRADO', 'Usuario registrado básico')
ON DUPLICATE KEY UPDATE description = VALUES(description);

-- Insertar permisos básicos del sistema
INSERT INTO permisos (nombre, description, modulo, accion) VALUES
-- Permisos de gestión de usuarios
('USER_READ', 'Ver usuarios', 'USUARIOS', 'READ'),
('USER_CREATE', 'Crear usuarios', 'USUARIOS', 'CREATE'),
('USER_UPDATE', 'Actualizar usuarios', 'USUARIOS', 'UPDATE'),
('USER_DELETE', 'Eliminar usuarios', 'USUARIOS', 'DELETE'),

-- Permisos de gestión de empresas
('COMPANY_READ', 'Ver empresas', 'EMPRESAS', 'READ'),
('COMPANY_CREATE', 'Crear empresas', 'EMPRESAS', 'CREATE'),
('COMPANY_UPDATE', 'Actualizar empresas', 'EMPRESAS', 'UPDATE'),
('COMPANY_DELETE', 'Eliminar empresas', 'EMPRESAS', 'DELETE'),

-- Permisos de gestión de roles
('ROLE_READ', 'Ver roles', 'ROLES', 'READ'),
('ROLE_CREATE', 'Crear roles', 'ROLES', 'CREATE'),
('ROLE_UPDATE', 'Actualizar roles', 'ROLES', 'UPDATE'),
('ROLE_DELETE', 'Eliminar roles', 'ROLES', 'DELETE'),

-- Permisos de gestión de permisos
('PERMISSION_READ', 'Ver permisos', 'PERMISOS', 'READ'),
('PERMISSION_CREATE', 'Crear permisos', 'PERMISOS', 'CREATE'),
('PERMISSION_UPDATE', 'Actualizar permisos', 'PERMISOS', 'UPDATE'),
('PERMISSION_DELETE', 'Eliminar permisos', 'PERMISOS', 'DELETE'),

-- Permisos de campos y lotes
('FIELD_READ', 'Ver campos', 'CAMPOS', 'READ'),
('FIELD_CREATE', 'Crear campos', 'CAMPOS', 'CREATE'),
('FIELD_UPDATE', 'Actualizar campos', 'CAMPOS', 'UPDATE'),
('FIELD_DELETE', 'Eliminar campos', 'CAMPOS', 'DELETE'),

('PLOT_READ', 'Ver lotes', 'LOTES', 'READ'),
('PLOT_CREATE', 'Crear lotes', 'LOTES', 'CREATE'),
('PLOT_UPDATE', 'Actualizar lotes', 'LOTES', 'UPDATE'),
('PLOT_DELETE', 'Eliminar lotes', 'LOTES', 'DELETE'),

-- Permisos de cultivos y cosechas
('CROP_READ', 'Ver cultivos', 'CULTIVOS', 'READ'),
('CROP_CREATE', 'Crear cultivos', 'CULTIVOS', 'CREATE'),
('CROP_UPDATE', 'Actualizar cultivos', 'CULTIVOS', 'UPDATE'),
('CROP_DELETE', 'Eliminar cultivos', 'CULTIVOS', 'DELETE'),

('HARVEST_READ', 'Ver cosechas', 'COSECHAS', 'READ'),
('HARVEST_CREATE', 'Crear cosechas', 'COSECHAS', 'CREATE'),
('HARVEST_UPDATE', 'Actualizar cosechas', 'COSECHAS', 'UPDATE'),
('HARVEST_DELETE', 'Eliminar cosechas', 'COSECHAS', 'DELETE'),

-- Permisos de insumos y maquinaria
('INPUT_READ', 'Ver insumos', 'INSUMOS', 'READ'),
('INPUT_CREATE', 'Crear insumos', 'INSUMOS', 'CREATE'),
('INPUT_UPDATE', 'Actualizar insumos', 'INSUMOS', 'UPDATE'),
('INPUT_DELETE', 'Eliminar insumos', 'INSUMOS', 'DELETE'),

('MACHINERY_READ', 'Ver maquinaria', 'MAQUINARIA', 'READ'),
('MACHINERY_CREATE', 'Crear maquinaria', 'MAQUINARIA', 'CREATE'),
('MACHINERY_UPDATE', 'Actualizar maquinaria', 'MAQUINARIA', 'UPDATE'),
('MACHINERY_DELETE', 'Eliminar maquinaria', 'MAQUINARIA', 'DELETE'),

-- Permisos de labores
('LABOR_READ', 'Ver labores', 'LABORES', 'READ'),
('LABOR_CREATE', 'Crear labores', 'LABORES', 'CREATE'),
('LABOR_UPDATE', 'Actualizar labores', 'LABORES', 'UPDATE'),
('LABOR_DELETE', 'Eliminar labores', 'LABORES', 'DELETE'),

-- Permisos financieros
('FINANCE_READ', 'Ver finanzas', 'FINANZAS', 'READ'),
('FINANCE_CREATE', 'Crear registros financieros', 'FINANZAS', 'CREATE'),
('FINANCE_UPDATE', 'Actualizar registros financieros', 'FINANZAS', 'UPDATE'),
('FINANCE_DELETE', 'Eliminar registros financieros', 'FINANZAS', 'DELETE'),

-- Permisos de reportes
('REPORT_READ', 'Ver reportes', 'REPORTES', 'READ'),
('REPORT_CREATE', 'Crear reportes', 'REPORTES', 'CREATE'),
('REPORT_EXPORT', 'Exportar reportes', 'REPORTES', 'EXPORT')
ON DUPLICATE KEY UPDATE description = VALUES(description), modulo = VALUES(modulo), accion = VALUES(accion);

-- Asignar permisos a roles
-- SUPERADMIN: todos los permisos
INSERT INTO roles_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r, permisos p
WHERE r.nombre = 'SUPERADMIN'
ON DUPLICATE KEY UPDATE activo = TRUE;

-- ADMIN_EMPRESA: permisos de gestión de empresa
INSERT INTO roles_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r, permisos p
WHERE r.nombre = 'ADMIN_EMPRESA' 
AND p.nombre IN (
    'USER_READ', 'USER_CREATE', 'USER_UPDATE',
    'COMPANY_READ', 'COMPANY_UPDATE',
    'ROLE_READ',
    'FIELD_READ', 'FIELD_CREATE', 'FIELD_UPDATE', 'FIELD_DELETE',
    'PLOT_READ', 'PLOT_CREATE', 'PLOT_UPDATE', 'PLOT_DELETE',
    'CROP_READ', 'CROP_CREATE', 'CROP_UPDATE', 'CROP_DELETE',
    'HARVEST_READ', 'HARVEST_CREATE', 'HARVEST_UPDATE', 'HARVEST_DELETE',
    'INPUT_READ', 'INPUT_CREATE', 'INPUT_UPDATE', 'INPUT_DELETE',
    'MACHINERY_READ', 'MACHINERY_CREATE', 'MACHINERY_UPDATE', 'MACHINERY_DELETE',
    'LABOR_READ', 'LABOR_CREATE', 'LABOR_UPDATE', 'LABOR_DELETE',
    'FINANCE_READ', 'FINANCE_CREATE', 'FINANCE_UPDATE', 'FINANCE_DELETE',
    'REPORT_READ', 'REPORT_CREATE', 'REPORT_EXPORT'
)
ON DUPLICATE KEY UPDATE activo = TRUE;

-- PRODUCTOR: permisos de operación
INSERT INTO roles_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r, permisos p
WHERE r.nombre = 'PRODUCTOR' 
AND p.nombre IN (
    'FIELD_READ', 'PLOT_READ', 'PLOT_CREATE', 'PLOT_UPDATE',
    'CROP_READ', 'CROP_CREATE', 'CROP_UPDATE',
    'HARVEST_READ', 'HARVEST_CREATE', 'HARVEST_UPDATE',
    'INPUT_READ', 'INPUT_CREATE', 'INPUT_UPDATE',
    'MACHINERY_READ',
    'LABOR_READ', 'LABOR_CREATE', 'LABOR_UPDATE',
    'FINANCE_READ', 'FINANCE_CREATE', 'FINANCE_UPDATE',
    'REPORT_READ'
)
ON DUPLICATE KEY UPDATE activo = TRUE;

-- ASESOR: permisos de consulta y asesoramiento
INSERT INTO roles_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r, permisos p
WHERE r.nombre = 'ASESOR' 
AND p.nombre IN (
    'FIELD_READ', 'PLOT_READ',
    'CROP_READ', 'HARVEST_READ',
    'INPUT_READ', 'MACHINERY_READ',
    'LABOR_READ',
    'FINANCE_READ',
    'REPORT_READ', 'REPORT_CREATE', 'REPORT_EXPORT'
)
ON DUPLICATE KEY UPDATE activo = TRUE;

-- CONTADOR: permisos financieros
INSERT INTO roles_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r, permisos p
WHERE r.nombre = 'CONTADOR' 
AND p.nombre IN (
    'FINANCE_READ', 'FINANCE_CREATE', 'FINANCE_UPDATE', 'FINANCE_DELETE',
    'REPORT_READ', 'REPORT_CREATE', 'REPORT_EXPORT'
)
ON DUPLICATE KEY UPDATE activo = TRUE;

-- TECNICO: permisos técnicos
INSERT INTO roles_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r, permisos p
WHERE r.nombre = 'TECNICO' 
AND p.nombre IN (
    'FIELD_READ', 'PLOT_READ', 'PLOT_CREATE', 'PLOT_UPDATE',
    'CROP_READ', 'CROP_CREATE', 'CROP_UPDATE',
    'HARVEST_READ', 'HARVEST_CREATE', 'HARVEST_UPDATE',
    'INPUT_READ', 'INPUT_CREATE', 'INPUT_UPDATE',
    'MACHINERY_READ', 'MACHINERY_CREATE', 'MACHINERY_UPDATE',
    'LABOR_READ', 'LABOR_CREATE', 'LABOR_UPDATE',
    'REPORT_READ', 'REPORT_CREATE'
)
ON DUPLICATE KEY UPDATE activo = TRUE;

-- OPERARIO: permisos básicos de operación
INSERT INTO roles_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r, permisos p
WHERE r.nombre = 'OPERARIO' 
AND p.nombre IN (
    'FIELD_READ', 'PLOT_READ',
    'CROP_READ',
    'HARVEST_READ', 'HARVEST_CREATE', 'HARVEST_UPDATE',
    'INPUT_READ',
    'MACHINERY_READ',
    'LABOR_READ', 'LABOR_CREATE', 'LABOR_UPDATE'
)
ON DUPLICATE KEY UPDATE activo = TRUE;

-- LECTURA: solo permisos de lectura
INSERT INTO roles_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r, permisos p
WHERE r.nombre = 'LECTURA' 
AND p.nombre IN (
    'FIELD_READ', 'PLOT_READ',
    'CROP_READ', 'HARVEST_READ',
    'INPUT_READ', 'MACHINERY_READ',
    'LABOR_READ',
    'FINANCE_READ',
    'REPORT_READ'
)
ON DUPLICATE KEY UPDATE activo = TRUE;

-- USUARIO_REGISTRADO: permisos mínimos
INSERT INTO roles_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r, permisos p
WHERE r.nombre = 'USUARIO_REGISTRADO' 
AND p.nombre IN (
    'FIELD_READ', 'PLOT_READ',
    'CROP_READ', 'HARVEST_READ',
    'INPUT_READ', 'MACHINERY_READ',
    'LABOR_READ',
    'REPORT_READ'
)
ON DUPLICATE KEY UPDATE activo = TRUE;

-- Crear usuario administrador por defecto
INSERT INTO usuarios (username, email, password, activo) VALUES
('admin', 'admin@agrocloud.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', TRUE)
ON DUPLICATE KEY UPDATE email = VALUES(email);

-- Crear empresa por defecto
INSERT INTO empresas (nombre, cuit, description) VALUES
('AgroCloud Demo', '20-12345678-9', 'Empresa de demostración del sistema AgroCloud')
ON DUPLICATE KEY UPDATE nombre = VALUES(nombre);

-- Asignar rol de administrador al usuario admin en la empresa demo
INSERT INTO usuarios_empresas_roles (usuario_id, empresa_id, rol_id)
SELECT u.id, e.id, r.id
FROM usuarios u, empresas e, roles r
WHERE u.username = 'admin' 
AND e.nombre = 'AgroCloud Demo'
AND r.nombre = 'ADMIN_EMPRESA'
ON DUPLICATE KEY UPDATE activo = TRUE;

-- Mostrar resumen de la configuración
SELECT 'Configuración completada' as status;
SELECT COUNT(*) as total_usuarios FROM usuarios;
SELECT COUNT(*) as total_empresas FROM empresas;
SELECT COUNT(*) as total_roles FROM roles;
SELECT COUNT(*) as total_permisos FROM permisos;
SELECT COUNT(*) as total_asignaciones_roles FROM usuarios_empresas_roles;
SELECT COUNT(*) as total_asignaciones_permisos FROM roles_permisos;
