-- ========================================
-- SCRIPT PARA VERIFICAR ESTRUCTURA DE DATOS
-- Analizar roles, usuarios, empresas y datos
-- ========================================

USE agrocloud;

-- 1. Verificar roles existentes
SELECT 'ROLES EXISTENTES' as seccion;
SELECT id, name as nombre, description as descripcion FROM roles ORDER BY id;

-- 2. Verificar usuarios y sus roles globales
SELECT 'USUARIOS Y ROLES GLOBALES' as seccion;
SELECT 
    u.id,
    u.username,
    u.email,
    u.first_name,
    u.last_name,
    u.parent_user_id,
    GROUP_CONCAT(r.name) as roles_globales
FROM usuarios u
LEFT JOIN usuarios_roles ur ON u.id = ur.usuario_id
LEFT JOIN roles r ON ur.rol_id = r.id
GROUP BY u.id, u.username, u.email, u.first_name, u.last_name, u.parent_user_id
ORDER BY u.id;

-- 3. Verificar empresas
SELECT 'EMPRESAS' as seccion;
SELECT id, nombre, descripcion, estado FROM empresas ORDER BY id;

-- 4. Verificar usuarios-empresas-roles
SELECT 'USUARIOS-EMPRESAS-ROLES' as seccion;
SELECT 
    ucr.id,
    u.username as usuario,
    e.nombre as empresa,
    r.name as rol,
    ucr.activo
FROM usuarios_empresas_roles ucr
JOIN usuarios u ON ucr.usuario_id = u.id
JOIN empresas e ON ucr.empresa_id = e.id
JOIN roles r ON ucr.rol_id = r.id
ORDER BY u.username, e.nombre;

-- 5. Verificar campos y sus propietarios
SELECT 'CAMPOS Y PROPIETARIOS' as seccion;
SELECT 
    c.id,
    c.nombre,
    c.descripcion,
    c.area_hectareas,
    c.activo,
    u.username as propietario,
    e.nombre as empresa
FROM campos c
JOIN usuarios u ON c.user_id = u.id
LEFT JOIN usuarios_empresas_roles ucr ON u.id = ucr.usuario_id AND ucr.activo = 1
LEFT JOIN empresas e ON ucr.empresa_id = e.id
ORDER BY c.id;

-- 6. Verificar lotes y sus propietarios
SELECT 'LOTES Y PROPIETARIOS' as seccion;
SELECT 
    l.id,
    l.nombre,
    l.descripcion,
    l.area_hectareas,
    l.activo,
    u.username as propietario,
    e.nombre as empresa
FROM lotes l
JOIN usuarios u ON l.user_id = u.id
LEFT JOIN usuarios_empresas_roles ucr ON u.id = ucr.usuario_id AND ucr.activo = 1
LEFT JOIN empresas e ON ucr.empresa_id = e.id
ORDER BY l.id;

-- 7. Verificar cultivos y sus propietarios
SELECT 'CULTIVOS Y PROPIETARIOS' as seccion;
SELECT 
    c.id,
    c.nombre,
    c.descripcion,
    c.activo,
    u.username as propietario,
    e.nombre as empresa
FROM cultivos c
JOIN usuarios u ON c.usuario_id = u.id
LEFT JOIN usuarios_empresas_roles ucr ON u.id = ucr.usuario_id AND ucr.activo = 1
LEFT JOIN empresas e ON ucr.empresa_id = e.id
ORDER BY c.id;

-- 8. Verificar cosechas y sus propietarios
SELECT 'COSECHAS Y PROPIETARIOS' as seccion;
SELECT 
    c.id,
    c.cantidad_toneladas,
    c.precio_por_tonelada,
    c.activo,
    u.username as propietario,
    e.nombre as empresa
FROM cosechas c
JOIN usuarios u ON c.usuario_id = u.id
LEFT JOIN usuarios_empresas_roles ucr ON u.id = ucr.usuario_id AND ucr.activo = 1
LEFT JOIN empresas e ON ucr.empresa_id = e.id
ORDER BY c.id;

-- 9. Verificar insumos y sus propietarios
SELECT 'INSUMOS Y PROPIETARIOS' as seccion;
SELECT 
    i.id,
    i.nombre,
    i.descripcion,
    i.cantidad_disponible,
    i.activo,
    u.username as propietario,
    e.nombre as empresa
FROM insumos i
JOIN usuarios u ON i.user_id = u.id
LEFT JOIN usuarios_empresas_roles ucr ON u.id = ucr.usuario_id AND ucr.activo = 1
LEFT JOIN empresas e ON ucr.empresa_id = e.id
ORDER BY i.id;

-- 10. Resumen por empresa
SELECT 'RESUMEN POR EMPRESA' as seccion;
SELECT 
    e.nombre as empresa,
    COUNT(DISTINCT ucr.usuario_id) as total_usuarios,
    COUNT(DISTINCT c.id) as total_campos,
    COUNT(DISTINCT l.id) as total_lotes,
    COUNT(DISTINCT cult.id) as total_cultivos,
    COUNT(DISTINCT cos.id) as total_cosechas,
    COUNT(DISTINCT i.id) as total_insumos
FROM empresas e
LEFT JOIN usuarios_empresas_roles ucr ON e.id = ucr.empresa_id AND ucr.activo = 1
LEFT JOIN usuarios u ON ucr.usuario_id = u.id
LEFT JOIN campos c ON u.id = c.user_id AND c.activo = 1
LEFT JOIN lotes l ON u.id = l.user_id AND l.activo = 1
LEFT JOIN cultivos cult ON u.id = cult.usuario_id AND cult.activo = 1
LEFT JOIN cosechas cos ON u.id = cos.usuario_id AND cos.activo = 1
LEFT JOIN insumos i ON u.id = i.user_id AND i.activo = 1
GROUP BY e.id, e.nombre
ORDER BY e.nombre;
