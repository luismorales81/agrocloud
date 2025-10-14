-- =====================================================
-- Script de Migración: Roles por Área
-- Actualiza roles antiguos a nueva estructura
-- =====================================================

USE agrocloud;

-- =====================================================
-- 1. AGREGAR NUEVOS ROLES
-- =====================================================

INSERT INTO roles (name, descripcion, activo, fecha_creacion)
SELECT 'JEFE_CAMPO', 'Jefe de Campo', 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'JEFE_CAMPO');

INSERT INTO roles (name, descripcion, activo, fecha_creacion)
SELECT 'JEFE_FINANCIERO', 'Jefe Financiero', 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'JEFE_FINANCIERO');

INSERT INTO roles (name, descripcion, activo, fecha_creacion)
SELECT 'CONSULTOR_EXTERNO', 'Consultor Externo', 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'CONSULTOR_EXTERNO');

-- =====================================================
-- 2. MIGRAR USUARIOS A NUEVOS ROLES
-- =====================================================

-- Obtener IDs de roles
SET @id_jefe_campo = (SELECT id FROM roles WHERE name = 'JEFE_CAMPO');
SET @id_jefe_financiero = (SELECT id FROM roles WHERE name = 'JEFE_FINANCIERO');
SET @id_consultor_externo = (SELECT id FROM roles WHERE name = 'CONSULTOR_EXTERNO');

SET @id_productor = (SELECT id FROM roles WHERE name = 'PRODUCTOR');
SET @id_asesor = (SELECT id FROM roles WHERE name = 'ASESOR');
SET @id_tecnico = (SELECT id FROM roles WHERE name = 'TECNICO');
SET @id_contador = (SELECT id FROM roles WHERE name = 'CONTADOR');
SET @id_lectura = (SELECT id FROM roles WHERE name = 'LECTURA');

-- Migrar PRODUCTOR → JEFE_CAMPO
UPDATE usuarios_empresas_roles uer
INNER JOIN roles r ON uer.rol_id = r.id
SET uer.rol_id = @id_jefe_campo
WHERE r.name = 'PRODUCTOR';

-- Migrar ASESOR → JEFE_CAMPO
UPDATE usuarios_empresas_roles uer
INNER JOIN roles r ON uer.rol_id = r.id
SET uer.rol_id = @id_jefe_campo
WHERE r.name = 'ASESOR';

-- Migrar TECNICO → JEFE_CAMPO
UPDATE usuarios_empresas_roles uer
INNER JOIN roles r ON uer.rol_id = r.id
SET uer.rol_id = @id_jefe_campo
WHERE r.name = 'TECNICO';

-- Migrar CONTADOR → JEFE_FINANCIERO
UPDATE usuarios_empresas_roles uer
INNER JOIN roles r ON uer.rol_id = r.id
SET uer.rol_id = @id_jefe_financiero
WHERE r.name = 'CONTADOR';

-- Migrar LECTURA → CONSULTOR_EXTERNO
UPDATE usuarios_empresas_roles uer
INNER JOIN roles r ON uer.rol_id = r.id
SET uer.rol_id = @id_consultor_externo
WHERE r.name = 'LECTURA';

-- =====================================================
-- 3. MARCAR ROLES ANTIGUOS COMO DEPRECADOS
-- =====================================================

UPDATE roles 
SET descripcion = CONCAT(descripcion, ' (DEPRECADO - usar JEFE_CAMPO)'),
    activo = 0
WHERE name IN ('PRODUCTOR', 'ASESOR', 'TECNICO');

UPDATE roles 
SET descripcion = CONCAT(descripcion, ' (DEPRECADO - usar JEFE_FINANCIERO)'),
    activo = 0
WHERE name = 'CONTADOR';

UPDATE roles 
SET descripcion = CONCAT(descripcion, ' (DEPRECADO - usar CONSULTOR_EXTERNO)'),
    activo = 0
WHERE name = 'LECTURA';

-- =====================================================
-- 4. ACTUALIZAR ROLES EN USUARIOS GLOBALES (si existe usuarios_roles)
-- =====================================================

-- Solo si existe la tabla usuarios_roles (para SUPERADMIN)
UPDATE usuarios_roles ur
INNER JOIN roles r_old ON ur.rol_id = r_old.id
SET ur.rol_id = @id_jefe_campo
WHERE r_old.name IN ('PRODUCTOR', 'ASESOR', 'TECNICO')
AND EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'usuarios_roles');

UPDATE usuarios_roles ur
INNER JOIN roles r_old ON ur.rol_id = r_old.id
SET ur.rol_id = @id_jefe_financiero
WHERE r_old.name = 'CONTADOR'
AND EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'usuarios_roles');

UPDATE usuarios_roles ur
INNER JOIN roles r_old ON ur.rol_id = r_old.id
SET ur.rol_id = @id_consultor_externo
WHERE r_old.name = 'LECTURA'
AND EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'usuarios_roles');

-- =====================================================
-- 5. VERIFICACIÓN
-- =====================================================

SELECT '=== ROLES ACTIVOS ===' as Seccion;
SELECT 
    id,
    name,
    descripcion,
    activo,
    CASE 
        WHEN activo = 1 THEN '✅ ACTIVO'
        ELSE '❌ DEPRECADO'
    END as estado
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

SELECT '=== DISTRIBUCIÓN DE USUARIOS POR ROL ===' as Seccion;
SELECT 
    r.name as rol,
    COUNT(*) as cantidad_usuarios
FROM usuarios_empresas_roles uer
INNER JOIN roles r ON uer.rol_id = r.id
GROUP BY r.name
ORDER BY cantidad_usuarios DESC;

SELECT '=== ROLES DEPRECADOS (si hay usuarios asignados) ===' as Seccion;
SELECT 
    r.name as rol,
    COUNT(*) as cantidad_usuarios
FROM usuarios_empresas_roles uer
INNER JOIN roles r ON uer.rol_id = r.id
WHERE r.name IN ('PRODUCTOR', 'ASESOR', 'TECNICO', 'CONTADOR', 'LECTURA')
GROUP BY r.name;

-- =====================================================
-- FIN DE MIGRACIÓN
-- =====================================================

