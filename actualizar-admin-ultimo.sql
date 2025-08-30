-- Actualizar usuario admin con hash BCrypt correcto (último intento)
UPDATE usuarios 
SET password = '$2a$10$tSWJYqxf8LxvInLZyJxv/uPoSEPyYPANjWeOIJ0BYl8tZ1p8T21z2'
WHERE username = 'admin';

-- Verificar
SELECT id, username, email, password, activo 
FROM usuarios 
WHERE username = 'admin';
