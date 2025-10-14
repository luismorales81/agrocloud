-- =====================================================
-- Verificación y Corrección: Rol de Luis Operario
-- =====================================================

USE agrocloud;

SELECT '========================================' as '';
SELECT '🔍 VERIFICACIÓN DEL USUARIO LUIS' as '';
SELECT '========================================' as '';

-- 1. Verificar datos del usuario Luis
SELECT '' as '';
SELECT '👤 DATOS DEL USUARIO:' as '';
SELECT 
    id,
    username,
    email,
    CONCAT(first_name, ' ', last_name) as nombre_completo,
    activo,
    estado
FROM usuarios 
WHERE email = 'operario.luis@agrocloud.com';

-- 2. Verificar rol en usuario_empresas (nueva estructura)
SELECT '' as '';
SELECT '🏢 ROL EN TABLA USUARIO_EMPRESAS:' as '';
SELECT 
    ue.id,
    u.email,
    CONCAT(u.first_name, ' ', u.last_name) as usuario,
    e.nombre as empresa,
    ue.rol as ROL_ACTUAL,
    CASE 
        WHEN ue.rol = 'OPERARIO' THEN '✅ CORRECTO'
        WHEN ue.rol = 'JEFE_CAMPO' THEN '❌ INCORRECTO (debería ser OPERARIO)'
        ELSE '❓ REVISAR'
    END as estado
FROM usuario_empresas ue
INNER JOIN usuarios u ON ue.usuario_id = u.id
INNER JOIN empresas e ON ue.empresa_id = e.id
WHERE u.email = 'operario.luis@agrocloud.com';

-- 3. Verificar rol en usuarios_empresas_roles (tabla de roles por empresa)
SELECT '' as '';
SELECT '🎭 ROL EN TABLA USUARIOS_EMPRESAS_ROLES:' as '';
SELECT 
    uer.id,
    u.email,
    e.nombre as empresa,
    r.name as ROL_ACTUAL,
    r.descripcion,
    CASE 
        WHEN r.name = 'OPERARIO' THEN '✅ CORRECTO'
        WHEN r.name = 'JEFE_CAMPO' THEN '❌ INCORRECTO (debería ser OPERARIO)'
        ELSE '❓ REVISAR'
    END as estado
FROM usuarios_empresas_roles uer
INNER JOIN usuarios u ON uer.usuario_id = u.id
INNER JOIN empresas e ON uer.empresa_id = e.id
INNER JOIN roles r ON uer.rol_id = r.id
WHERE u.email = 'operario.luis@agrocloud.com';

-- 4. Verificar todos los usuarios OPERARIO
SELECT '' as '';
SELECT '👷 TODOS LOS USUARIOS CON ROL OPERARIO:' as '';
SELECT 
    u.email,
    CONCAT(u.first_name, ' ', u.last_name) as nombre,
    e.nombre as empresa,
    ue.rol
FROM usuario_empresas ue
INNER JOIN usuarios u ON ue.usuario_id = u.id
INNER JOIN empresas e ON ue.empresa_id = e.id
WHERE ue.rol = 'OPERARIO';

SELECT '========================================' as '';
SELECT '🔧 CORRECCIÓN (si es necesario)' as '';
SELECT '========================================' as '';

-- 5. CORREGIR el rol en usuario_empresas
UPDATE usuario_empresas ue
INNER JOIN usuarios u ON ue.usuario_id = u.id
SET ue.rol = 'OPERARIO'
WHERE u.email = 'operario.luis@agrocloud.com'
AND ue.rol != 'OPERARIO';

-- 6. CORREGIR el rol en usuarios_empresas_roles
UPDATE usuarios_empresas_roles uer
INNER JOIN usuarios u ON uer.usuario_id = u.id
INNER JOIN roles r ON uer.rol_id = r.id
SET uer.rol_id = (SELECT id FROM roles WHERE name = 'OPERARIO' LIMIT 1)
WHERE u.email = 'operario.luis@agrocloud.com'
AND r.name != 'OPERARIO';

-- 7. Verificar corrección
SELECT '' as '';
SELECT '✅ VERIFICACIÓN POST-CORRECCIÓN:' as '';
SELECT 
    ue.id,
    u.email,
    e.nombre as empresa,
    ue.rol as ROL_EN_USUARIO_EMPRESAS,
    r.name as ROL_EN_USUARIOS_EMPRESAS_ROLES,
    CASE 
        WHEN ue.rol = 'OPERARIO' AND r.name = 'OPERARIO' THEN '✅ TODO CORRECTO'
        ELSE '⚠️ REVISAR'
    END as estado
FROM usuario_empresas ue
INNER JOIN usuarios u ON ue.usuario_id = u.id
INNER JOIN empresas e ON ue.empresa_id = e.id
LEFT JOIN usuarios_empresas_roles uer ON uer.usuario_id = u.id AND uer.empresa_id = e.id
LEFT JOIN roles r ON uer.rol_id = r.id
WHERE u.email = 'operario.luis@agrocloud.com';

SELECT '' as '';
SELECT '========================================' as '';
SELECT '🎯 RESUMEN' as '';
SELECT '========================================' as '';

SELECT 
    CASE 
        WHEN COUNT(*) > 0 THEN CONCAT('✅ Luis tiene el rol OPERARIO asignado correctamente (', COUNT(*), ' empresa(s))')
        ELSE '❌ Luis NO tiene el rol OPERARIO asignado'
    END as RESULTADO
FROM usuario_empresas ue
INNER JOIN usuarios u ON ue.usuario_id = u.id
WHERE u.email = 'operario.luis@agrocloud.com'
AND ue.rol = 'OPERARIO';

SELECT '========================================' as '';


