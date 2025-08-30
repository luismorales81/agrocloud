-- Script para actualizar contraseñas con hashes BCrypt correctos
-- Ejecutar en phpMyAdmin

-- Contraseñas hasheadas con BCrypt (costo 10) - Generadas por el backend:
-- admin123 -> $2a$10$7i.UgzFVlaJKHQQom0HpD.yOjOVhKuqCWygkQjGgVKiWGvqF9L.d2
-- tecnico123 -> $2a$10$e5A0CFbpdGM8GxaHjyt/qekU301eIdVtZt66e2BjaAJP/xjtt4NkG
-- productor123 -> $2a$10$QJ/UPn6RADwl/HPk/Ivn5.oXGj4.HNayEZoySuKBsRXz2w.2NiPqu

-- Actualizar contraseñas de usuarios existentes
UPDATE usuarios 
SET password = '$2a$10$7i.UgzFVlaJKHQQom0HpD.yOjOVhKuqCWygkQjGgVKiWGvqF9L.d2'
WHERE username = 'admin';

UPDATE usuarios 
SET password = '$2a$10$e5A0CFbpdGM8GxaHjyt/qekU301eIdVtZt66e2BjaAJP/xjtt4NkG'
WHERE username = 'tecnico';

UPDATE usuarios 
SET password = '$2a$10$QJ/UPn6RADwl/HPk/Ivn5.oXGj4.HNayEZoySuKBsRXz2w.2NiPqu'
WHERE username = 'productor';

-- Verificar que se actualizaron correctamente
SELECT id, username, email, password, activo 
FROM usuarios 
WHERE username IN ('admin', 'tecnico', 'productor');
