-- Script para agregar campos de recuperación de contraseña a la tabla usuarios
-- Ejecutar este script en la base de datos para habilitar la funcionalidad de recuperación de contraseña

-- Primero, verificar qué base de datos estamos usando
SELECT DATABASE() as current_database;

-- Mostrar todas las bases de datos disponibles
SHOW DATABASES;

-- Mostrar todas las tablas en la base de datos actual
SHOW TABLES;

-- Verificar si existe la tabla usuarios o users
SELECT TABLE_NAME 
FROM INFORMATION_SCHEMA.TABLES 
WHERE TABLE_SCHEMA = DATABASE() 
AND (TABLE_NAME = 'usuarios' OR TABLE_NAME = 'users');

-- Si la tabla se llama 'users' en lugar de 'usuarios', usar este comando:
-- ALTER TABLE users 
-- ADD COLUMN reset_password_token VARCHAR(255) NULL,
-- ADD COLUMN reset_password_token_expiry DATETIME NULL,
-- ADD COLUMN email_verified BOOLEAN DEFAULT FALSE,
-- ADD COLUMN verification_token VARCHAR(255) NULL;

-- Si la tabla se llama 'usuarios', usar este comando:
ALTER TABLE usuarios 
ADD COLUMN reset_password_token VARCHAR(255) NULL,
ADD COLUMN reset_password_token_expiry DATETIME NULL,
ADD COLUMN email_verified BOOLEAN DEFAULT FALSE,
ADD COLUMN verification_token VARCHAR(255) NULL;

-- Crear índices para mejorar el rendimiento de las consultas
-- (Ajustar el nombre de la tabla según corresponda)
CREATE INDEX idx_usuarios_reset_token ON usuarios(reset_password_token);
CREATE INDEX idx_usuarios_verification_token ON usuarios(verification_token);
CREATE INDEX idx_usuarios_email_verified ON usuarios(email_verified);

-- Actualizar usuarios existentes para marcar sus emails como verificados
UPDATE usuarios SET email_verified = TRUE WHERE email IS NOT NULL;

-- Verificar que los cambios se aplicaron correctamente
SELECT 
    COLUMN_NAME, 
    DATA_TYPE, 
    IS_NULLABLE, 
    COLUMN_DEFAULT
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
AND TABLE_NAME = 'usuarios' 
AND COLUMN_NAME IN ('reset_password_token', 'reset_password_token_expiry', 'email_verified', 'verification_token');

-- Mostrar la estructura actualizada de la tabla
DESCRIBE usuarios;
