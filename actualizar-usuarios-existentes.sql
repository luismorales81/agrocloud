-- Actualizar todos los usuarios existentes con el hash correcto para 'admin123'
-- Este hash fue generado por el sistema y funciona correctamente
UPDATE usuarios 
SET password = '$2a$10$tfobtlu6.o5QdOum9zzHPO21o9ui713jQiBE5d/7QmAkOJZS91be2'
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
    'tecnico.juan@agrocloud.com',
    'admin.empresa@agrocloud.com'
);
