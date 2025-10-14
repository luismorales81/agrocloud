-- Test para verificar hashes de contraseñas
USE agrocloud;

-- Ver todos los usuarios y sus hashes
SELECT 
    id,
    email,
    first_name,
    last_name,
    activo,
    password as hash_completo
FROM usuarios 
WHERE email IN (
    'admin@agrocloud.com',
    'admin.campo@agrocloud.com', 
    'tecnico.juan@agrocloud.com',
    'admin.empresa@agrocloud.com'
)
ORDER BY id;

-- Nota: Los hashes BCrypt deberían verse así:
-- $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa = "admin123"
-- $2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi = DIFERENTE

-- Si tecnico.juan tiene el mismo hash que admin.campo, ambos deberían tener la misma contraseña

