-- Actualizar solo el usuario admin
UPDATE usuarios 
SET password = '$2a$10$PqorFigHL8.mnf0rnde/res7Ko.khiipqsVyxaivqlDXBqs6fpM8m'
WHERE username = 'admin';

-- Verificar
SELECT id, username, email, password, activo 
FROM usuarios 
WHERE username = 'admin';
