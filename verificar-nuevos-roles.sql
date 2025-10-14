-- =====================================================
-- Script de Verificación: Nuevos Roles por Área
-- =====================================================

USE agrocloud;

SELECT '========================================' as '';
SELECT '📋 VERIFICACIÓN DE ROLES' as '';
SELECT '========================================' as '';

-- 1. Mostrar todos los roles activos
SELECT '' as '';
SELECT '✅ ROLES ACTIVOS:' as '';
SELECT 
    id,
    name as ROL,
    descripcion as DESCRIPCION,
    activo as ACTIVO
FROM roles 
WHERE activo = 1
ORDER BY 
    CASE name
        WHEN 'SUPERADMIN' THEN 1
        WHEN 'ADMINISTRADOR' THEN 2
        WHEN 'JEFE_CAMPO' THEN 3
        WHEN 'JEFE_FINANCIERO' THEN 4
        WHEN 'OPERARIO' THEN 5
        WHEN 'CONSULTOR_EXTERNO' THEN 6
        ELSE 99
    END;

-- 2. Mostrar roles deprecados
SELECT '' as '';
SELECT '❌ ROLES DEPRECADOS:' as '';
SELECT 
    id,
    name as ROL,
    descripcion as DESCRIPCION,
    activo as ACTIVO
FROM roles 
WHERE activo = 0 AND name IN ('PRODUCTOR', 'ASESOR', 'TECNICO', 'CONTADOR', 'LECTURA');

-- 3. Distribución de usuarios por rol
SELECT '' as '';
SELECT '📊 DISTRIBUCIÓN DE USUARIOS POR ROL:' as '';
SELECT 
    r.name as ROL,
    COUNT(*) as CANTIDAD_USUARIOS,
    GROUP_CONCAT(DISTINCT e.nombre SEPARATOR ', ') as EMPRESAS
FROM usuarios_empresas_roles uer
INNER JOIN roles r ON uer.rol_id = r.id
INNER JOIN empresas e ON uer.empresa_id = e.id
GROUP BY r.name
ORDER BY 
    CASE r.name
        WHEN 'ADMINISTRADOR' THEN 1
        WHEN 'JEFE_CAMPO' THEN 2
        WHEN 'JEFE_FINANCIERO' THEN 3
        WHEN 'OPERARIO' THEN 4
        WHEN 'CONSULTOR_EXTERNO' THEN 5
        ELSE 99
    END;

-- 4. Verificar si hay usuarios con roles antiguos (deberían ser 0)
SELECT '' as '';
SELECT '⚠️ USUARIOS CON ROLES ANTIGUOS (debería ser 0):' as '';
SELECT 
    u.email as EMAIL,
    e.nombre as EMPRESA,
    r.name as ROL_ANTIGUO,
    '→ Migrar a nuevo rol' as ACCION
FROM usuarios_empresas_roles uer
INNER JOIN usuarios u ON uer.usuario_id = u.id
INNER JOIN empresas e ON uer.empresa_id = e.id
INNER JOIN roles r ON uer.rol_id = r.id
WHERE r.name IN ('PRODUCTOR', 'ASESOR', 'TECNICO', 'CONTADOR', 'LECTURA');

-- 5. Resumen final
SELECT '' as '';
SELECT '========================================' as '';
SELECT '📝 RESUMEN' as '';
SELECT '========================================' as '';
SELECT 
    CONCAT('Total de roles activos: ', COUNT(*)) as INFO
FROM roles 
WHERE activo = 1;

SELECT 
    CONCAT('Total de roles deprecados: ', COUNT(*)) as INFO
FROM roles 
WHERE activo = 0;

SELECT 
    CONCAT('Total de usuarios en empresas: ', COUNT(*)) as INFO
FROM usuarios_empresas_roles;

SELECT '========================================' as '';

