@echo off
echo ========================================
echo   Verificando roles en base de datos
echo ========================================

"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p123456 -e "
USE agrocloud;

-- 1. Verificar roles en la tabla 'roles'
SELECT 'ROLES EN TABLA ROLES:' as consulta;
SELECT id, name as nombre, descripcion, activo, fecha_creacion 
FROM roles 
ORDER BY id;

-- 2. Verificar roles en la tabla 'usuarios_empresas_roles'
SELECT 'ROLES EN USUARIOS_EMPRESAS_ROLES:' as consulta;
SELECT DISTINCT r.name as rol_nombre, COUNT(*) as cantidad_usuarios
FROM usuarios_empresas_roles ucr
JOIN roles r ON ucr.rol_id = r.id
GROUP BY r.name
ORDER BY r.name;

-- 3. Verificar roles en la tabla 'usuarios' (sistema legacy)
SELECT 'ROLES EN USUARIOS (LEGACY):' as consulta;
SELECT DISTINCT r.name as rol_nombre, COUNT(*) as cantidad_usuarios
FROM usuarios u
JOIN usuarios_roles ur ON u.id = ur.usuario_id
JOIN roles r ON ur.rol_id = r.id
GROUP BY r.name
ORDER BY r.name;

-- 4. Verificar estructura de la tabla roles
SELECT 'ESTRUCTURA TABLA ROLES:' as consulta;
DESCRIBE roles;

-- 5. Verificar si existe tabla usuarios_roles (sistema legacy)
SELECT 'TABLA USUARIOS_ROLES (LEGACY):' as consulta;
SHOW TABLES LIKE 'usuarios_roles';

-- 6. Verificar permisos asociados a roles
SELECT 'PERMISOS POR ROL:' as consulta;
SELECT r.name as rol_nombre, p.name as permiso_nombre, p.descripcion
FROM roles r
LEFT JOIN role_permissions rp ON r.id = rp.rol_id
LEFT JOIN permissions p ON rp.permission_id = p.id
ORDER BY r.name, p.name;
"

echo.
echo ========================================
echo   Verificacion completada
echo ========================================
pause
