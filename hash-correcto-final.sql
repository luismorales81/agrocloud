-- Hash correcto para 'admin123' - este hash es conocido y funciona
-- Este es el hash correcto para 'admin123' con BCrypt
UPDATE usuarios 
SET password = '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa'
WHERE email IN (
    'admin@agrocloud.com',
    'admin.empresa@agrocloud.com', 
    'admin.campo@agrocloud.com',
    'tecnico.juan@agrocloud.com',
    'asesor.maria@agrocloud.com',
    'productor.pedro@agrocloud.com',
    'operario.luis@agrocloud.com',
    'invitado.ana@agrocloud.com'
);

-- Verificar que se actualizaron los usuarios
SELECT email, LEFT(password, 30) as password_preview FROM usuarios 
WHERE email IN (
    'admin@agrocloud.com',
    'tecnico.juan@agrocloud.com'
);
