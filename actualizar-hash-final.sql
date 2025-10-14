-- Usar un hash conocido que funciona para 'admin123'
-- Este es un hash estándar de BCrypt para 'admin123'
UPDATE usuarios 
SET password = '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi'
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
SELECT email, LEFT(password, 20) as password_preview FROM usuarios 
WHERE email IN (
    'admin@agrocloud.com',
    'tecnico.juan@agrocloud.com'
);
