-- Script flexible para agregar campos de recuperación de contraseña
-- Este script detecta automáticamente si la tabla se llama 'usuarios' o 'users'

-- Verificar qué base de datos estamos usando
SELECT DATABASE() as current_database;

-- Mostrar todas las tablas en la base de datos actual
SHOW TABLES;

-- Verificar si existe la tabla usuarios o users
SELECT TABLE_NAME 
FROM INFORMATION_SCHEMA.TABLES 
WHERE TABLE_SCHEMA = DATABASE() 
AND (TABLE_NAME = 'usuarios' OR TABLE_NAME = 'users');

-- Crear una variable para almacenar el nombre de la tabla
SET @table_name = (
    SELECT TABLE_NAME 
    FROM INFORMATION_SCHEMA.TABLES 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND (TABLE_NAME = 'usuarios' OR TABLE_NAME = 'users')
    LIMIT 1
);

-- Mostrar qué tabla se encontró
SELECT @table_name as found_table;

-- Si se encontró una tabla, proceder con la actualización
SET @sql = CONCAT('
    ALTER TABLE ', @table_name, ' 
    ADD COLUMN reset_password_token VARCHAR(255) NULL,
    ADD COLUMN reset_password_token_expiry DATETIME NULL,
    ADD COLUMN email_verified BOOLEAN DEFAULT FALSE,
    ADD COLUMN verification_token VARCHAR(255) NULL
');

-- Ejecutar el comando dinámico
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Crear índices
SET @sql = CONCAT('CREATE INDEX idx_', @table_name, '_reset_token ON ', @table_name, '(reset_password_token)');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = CONCAT('CREATE INDEX idx_', @table_name, '_verification_token ON ', @table_name, '(verification_token)');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = CONCAT('CREATE INDEX idx_', @table_name, '_email_verified ON ', @table_name, '(email_verified)');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Actualizar usuarios existentes
SET @sql = CONCAT('UPDATE ', @table_name, ' SET email_verified = TRUE WHERE email IS NOT NULL');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Verificar que los cambios se aplicaron correctamente
SET @sql = CONCAT('
    SELECT 
        COLUMN_NAME, 
        DATA_TYPE, 
        IS_NULLABLE, 
        COLUMN_DEFAULT
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = ''', @table_name, ''' 
    AND COLUMN_NAME IN (''reset_password_token'', ''reset_password_token_expiry'', ''email_verified'', ''verification_token'')
');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Mostrar la estructura actualizada de la tabla
SET @sql = CONCAT('DESCRIBE ', @table_name);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
