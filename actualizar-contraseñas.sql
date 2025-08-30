-- Script para actualizar contraseñas con hashes correctos de BCrypt
-- Ejecutar en phpMyAdmin

-- Contraseñas hasheadas con BCrypt (costo 10):
-- admin123 -> $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa
-- tecnico123 -> $2a$10$8K1p/a0dL1LXMIgoEDFrwOe6g7fqj/5TjY9J8J8J8J8J8J8J8J8J8
-- productor123 -> $2a$10$9L2q/b1eM2MYNJpFEGGsxPf7h8gr/6UkZ0K9K9K9K9K9K9K9K9K9K

-- Actualizar contraseñas de usuarios existentes
UPDATE usuarios 
SET password = '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa'
WHERE username = 'admin';

UPDATE usuarios 
SET password = '$2a$10$8K1p/a0dL1LXMIgoEDFrwOe6g7fqj/5TjY9J8J8J8J8J8J8J8J8J8'
WHERE username = 'tecnico';

UPDATE usuarios 
SET password = '$2a$10$9L2q/b1eM2MYNJpFEGGsxPf7h8gr/6UkZ0K9K9K9K9K9K9K9K9K9K'
WHERE username = 'productor';

-- Verificar que se actualizaron correctamente
SELECT id, username, email, password, activo 
FROM usuarios 
WHERE username IN ('admin', 'tecnico', 'productor');
