-- Script simple para agregar campos de recuperación de contraseña
-- Compatible con MariaDB y MySQL

-- Verificar qué base de datos estamos usando
SELECT DATABASE() as current_database;

-- Mostrar todas las tablas en la base de datos actual
SHOW TABLES;

-- Verificar si existe la tabla usuarios
SELECT COUNT(*) as usuarios_exists 
FROM INFORMATION_SCHEMA.TABLES 
WHERE TABLE_SCHEMA = DATABASE() 
AND TABLE_NAME = 'usuarios';

-- Verificar si existe la tabla users
SELECT COUNT(*) as users_exists 
FROM INFORMATION_SCHEMA.TABLES 
WHERE TABLE_SCHEMA = DATABASE() 
AND TABLE_NAME = 'users';

-- Si la tabla se llama 'usuarios', ejecutar estos comandos:
-- (Descomenta las líneas que correspondan a tu tabla)

-- Para tabla 'usuarios':
/*
ALTER TABLE usuarios 
ADD COLUMN reset_password_token VARCHAR(255) NULL,
ADD COLUMN reset_password_token_expiry DATETIME NULL,
ADD COLUMN email_verified BOOLEAN DEFAULT FALSE,
ADD COLUMN verification_token VARCHAR(255) NULL;

CREATE INDEX idx_usuarios_reset_token ON usuarios(reset_password_token);
CREATE INDEX idx_usuarios_verification_token ON usuarios(verification_token);
CREATE INDEX idx_usuarios_email_verified ON usuarios(email_verified);

UPDATE usuarios SET email_verified = TRUE WHERE email IS NOT NULL;
*/

-- Para tabla 'users':
/*
ALTER TABLE users 
ADD COLUMN reset_password_token VARCHAR(255) NULL,
ADD COLUMN reset_password_token_expiry DATETIME NULL,
ADD COLUMN email_verified BOOLEAN DEFAULT FALSE,
ADD COLUMN verification_token VARCHAR(255) NULL;

CREATE INDEX idx_users_reset_token ON users(reset_password_token);
CREATE INDEX idx_users_verification_token ON users(verification_token);
CREATE INDEX idx_users_email_verified ON users(email_verified);

UPDATE users SET email_verified = TRUE WHERE email IS NOT NULL;
*/

-- Verificar la estructura de la tabla después de los cambios
-- (Descomenta la línea que corresponda a tu tabla)

-- Para tabla 'usuarios':
-- DESCRIBE usuarios;

-- Para tabla 'users':
-- DESCRIBE users;
