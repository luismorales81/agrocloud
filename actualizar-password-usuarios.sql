-- ========================================
-- Actualizar contraseñas de usuarios de prueba
-- ========================================
-- Contraseña para todos: admin123
-- Hash BCrypt: $2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi

USE agrocloud;

-- Actualizar usuario tecnico.juan con contraseña correcta
UPDATE usuarios 
SET password = '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi'
WHERE email = 'tecnico.juan@agrocloud.com';

-- Actualizar admin.campo con contraseña correcta
UPDATE usuarios 
SET password = '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi'
WHERE email = 'admin.campo@agrocloud.com';

-- Actualizar admin.empresa con contraseña correcta
UPDATE usuarios 
SET password = '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi'
WHERE email = 'admin.empresa@agrocloud.com';

-- Actualizar todos los demás usuarios de prueba que puedan existir
UPDATE usuarios 
SET password = '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi'
WHERE email IN (
    'asesor.maria@agrocloud.com',
    'productor.pedro@agrocloud.com',
    'operario.luis@agrocloud.com',
    'invitado.ana@agrocloud.com'
);

-- Verificar cambios
SELECT 
    email, 
    first_name, 
    last_name,
    activo,
    SUBSTRING(password, 1, 30) as hash_verificacion
FROM usuarios 
WHERE email LIKE '%@agrocloud.com'
ORDER BY id;

SELECT '✅ Contraseñas actualizadas. Todos los usuarios ahora usan: admin123' AS resultado;

