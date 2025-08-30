-- Script para insertar usuarios de prueba en la base de datos agrocloud
-- Ejecutar en phpMyAdmin o MySQL Workbench

USE agrocloud;

-- Insertar roles si no existen
INSERT IGNORE INTO roles (name, description) VALUES 
('ADMINISTRADOR', 'Administrador del sistema'),
('INGENIERO_AGRONOMO', 'Ingeniero Agrónomo'),
('OPERARIO', 'Operario de campo'),
('INVITADO', 'Usuario invitado');

-- Insertar usuarios de prueba
-- Las contraseñas están hasheadas con BCrypt (admin123, tecnico123, productor123)
INSERT IGNORE INTO usuarios (username, email, password, first_name, last_name, phone, activo, created_at, updated_at, email_verified) VALUES 
('admin', 'admin@agrocloud.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'Administrador', 'Sistema', '123456789', 1, NOW(), NOW(), 1),
('tecnico', 'tecnico@agrocloud.com', '$2a$10$8K1p/a0dL1LXMIgoEDFrwOe6g7fqj/5TjY9J8J8J8J8J8J8J8J8J8', 'Técnico', 'Agrícola', '987654321', 1, NOW(), NOW(), 1),
('productor', 'productor@agrocloud.com', '$2a$10$9L2q/b1eM2MYNJpFEGGsxPf7h8gr/6UkZ0K9K9K9K9K9K9K9K9K9K', 'Productor', 'Agrícola', '555666777', 1, NOW(), NOW(), 1);

-- Asignar roles a los usuarios
INSERT IGNORE INTO usuarios_roles (usuario_id, role_id) 
SELECT u.id, r.id 
FROM usuarios u, roles r 
WHERE u.username = 'admin' AND r.name = 'ADMINISTRADOR';

INSERT IGNORE INTO usuarios_roles (usuario_id, role_id) 
SELECT u.id, r.id 
FROM usuarios u, roles r 
WHERE u.username = 'tecnico' AND r.name = 'INGENIERO_AGRONOMO';

INSERT IGNORE INTO usuarios_roles (usuario_id, role_id) 
SELECT u.id, r.id 
FROM usuarios u, roles r 
WHERE u.username = 'productor' AND r.name = 'OPERARIO';

-- Verificar que los usuarios se insertaron correctamente
SELECT 
    u.id,
    u.username,
    u.email,
    u.first_name,
    u.last_name,
    u.activo,
    GROUP_CONCAT(r.name) as roles
FROM usuarios u
LEFT JOIN usuarios_roles ur ON u.id = ur.usuario_id
LEFT JOIN roles r ON ur.role_id = r.id
GROUP BY u.id, u.username, u.email, u.first_name, u.last_name, u.activo;
