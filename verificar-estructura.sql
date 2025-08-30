-- Script para verificar la estructura de las tablas
-- Ejecutar en phpMyAdmin

-- 1. Verificar estructura de la tabla usuarios
DESCRIBE usuarios;

-- 2. Verificar estructura de la tabla roles
DESCRIBE roles;

-- 3. Verificar estructura de la tabla usuarios_roles
DESCRIBE usuarios_roles;

-- 4. Verificar si las tablas tienen datos
SELECT 'usuarios' as tabla, COUNT(*) as cantidad FROM usuarios
UNION ALL
SELECT 'roles' as tabla, COUNT(*) as cantidad FROM roles
UNION ALL
SELECT 'usuarios_roles' as tabla, COUNT(*) as cantidad FROM usuarios_roles;
