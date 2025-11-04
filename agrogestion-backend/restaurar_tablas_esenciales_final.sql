-- Script para restaurar tablas esenciales del sistema
-- que fueron eliminadas por error

USE agrocloud;

-- Crear tabla usuarios_empresas_roles
CREATE TABLE IF NOT EXISTS usuarios_empresas_roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    empresa_id BIGINT NOT NULL,
    rol_id BIGINT NOT NULL,
    activo BOOLEAN DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE,
    FOREIGN KEY (rol_id) REFERENCES roles(id) ON DELETE CASCADE,
    UNIQUE KEY unique_usuario_empresa_rol (usuario_id, empresa_id, rol_id)
);

-- Crear tabla roles_permisos
CREATE TABLE IF NOT EXISTS roles_permisos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rol_id BIGINT NOT NULL,
    permiso_id BIGINT NOT NULL,
    activo BOOLEAN DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (rol_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (permiso_id) REFERENCES permisos(id) ON DELETE CASCADE,
    UNIQUE KEY unique_rol_permiso (rol_id, permiso_id)
);

-- Crear tabla usuarios_roles
CREATE TABLE IF NOT EXISTS usuarios_roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    rol_id BIGINT NOT NULL,
    activo BOOLEAN DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    FOREIGN KEY (rol_id) REFERENCES roles(id) ON DELETE CASCADE,
    UNIQUE KEY unique_usuario_rol (usuario_id, rol_id)
);

-- Crear tabla permisos
CREATE TABLE IF NOT EXISTS permisos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    descripcion TEXT,
    activo BOOLEAN DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Insertar datos básicos de prueba
INSERT IGNORE INTO roles (name, descripcion) VALUES
('ADMIN', 'Administrador del sistema'),
('USUARIO', 'Usuario estándar'),
('GERENTE', 'Gerente de empresa');

INSERT IGNORE INTO permisos (nombre, descripcion) VALUES
('READ_USUARIOS', 'Leer usuarios'),
('WRITE_USUARIOS', 'Crear/editar usuarios'),
('DELETE_USUARIOS', 'Eliminar usuarios'),
('READ_EMPRESAS', 'Leer empresas'),
('WRITE_EMPRESAS', 'Crear/editar empresas'),
('READ_AGROQUIMICOS', 'Leer agroquímicos'),
('WRITE_AGROQUIMICOS', 'Crear/editar agroquímicos');

-- Verificar que las tablas se crearon correctamente
SHOW TABLES;
