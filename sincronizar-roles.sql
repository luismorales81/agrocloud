-- =====================================================
-- Script para sincronizar roles entre BD y sistema
-- =====================================================

USE agrocloud;

-- 1. DESACTIVAR roles que no se usan en el sistema
UPDATE roles SET activo = 0 WHERE name = 'ADMIN';
UPDATE roles SET activo = 0 WHERE name = 'USUARIO_REGISTRADO';

-- 2. AGREGAR roles faltantes que están en RolEmpresa enum
-- Verificar si CONTADOR existe
INSERT INTO roles (name, descripcion, activo, fecha_creacion)
SELECT 'CONTADOR', 'Contador', 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'CONTADOR');

-- Verificar si LECTURA existe
INSERT INTO roles (name, descripcion, activo, fecha_creacion)
SELECT 'LECTURA', 'Solo Lectura', 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'LECTURA');

-- 3. Actualizar descripciones para que coincidan con RolEmpresa
UPDATE roles SET descripcion = 'Super Administrador' WHERE name = 'SUPERADMIN';
UPDATE roles SET descripcion = 'Administrador de Empresa' WHERE name = 'ADMINISTRADOR';
UPDATE roles SET descripcion = 'Productor' WHERE name = 'PRODUCTOR';
UPDATE roles SET descripcion = 'Técnico' WHERE name = 'TECNICO';
UPDATE roles SET descripcion = 'Asesor/Ingeniero Agrónomo' WHERE name = 'ASESOR';
UPDATE roles SET descripcion = 'Operario' WHERE name = 'OPERARIO';
UPDATE roles SET descripcion = 'Usuario invitado con acceso limitado' WHERE name = 'INVITADO';

-- 4. Mostrar resultado final
SELECT 
    id,
    name,
    descripcion,
    activo,
    CASE 
        WHEN activo = 1 THEN '✅ ACTIVO'
        ELSE '❌ DESACTIVADO'
    END as estado
FROM roles 
ORDER BY 
    activo DESC,
    CASE name
        WHEN 'SUPERADMIN' THEN 1
        WHEN 'ADMINISTRADOR' THEN 2
        WHEN 'PRODUCTOR' THEN 3
        WHEN 'ASESOR' THEN 4
        WHEN 'CONTADOR' THEN 5
        WHEN 'TECNICO' THEN 6
        WHEN 'OPERARIO' THEN 7
        WHEN 'LECTURA' THEN 8
        WHEN 'INVITADO' THEN 9
        ELSE 99
    END;

-- 5. Verificar usuarios que tienen roles desactivados
SELECT 
    u.id,
    u.email,
    u.username,
    r.name as rol,
    r.activo as rol_activo
FROM usuarios u
INNER JOIN usuarios_roles ur ON u.id = ur.usuario_id
INNER JOIN roles r ON ur.rol_id = r.id
WHERE r.activo = 0;
