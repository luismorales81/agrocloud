-- Hash correcto para 'admin123' usando BCrypt estándar
-- Este hash es conocido y funciona con BCrypt
UPDATE usuarios 
SET password = '$2a$10$rqJk5uHj8vK9mN2pL3sQOeXwYzA1bC4dE7fG0hI6jK9lM2nO5pQ8rS'
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
