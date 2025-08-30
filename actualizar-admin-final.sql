-- Actualizar usuario admin con hash BCrypt correcto
UPDATE usuarios 
SET password = '$2a$10$pjJ6mQqqLOVSG4Hy79qM6eNm9ODpbIGU8yJEzb2PfmgHEr/1LD3Ne'
WHERE username = 'admin';

-- Verificar
SELECT id, username, email, password, activo 
FROM usuarios 
WHERE username = 'admin';
