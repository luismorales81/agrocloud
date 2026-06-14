-- ========================================
-- DATOS INICIALES PARA MÓDULOS
-- ========================================
-- Script para insertar datos iniciales del sistema de módulos
-- Ejecutar después de create-modules-system.sql

USE agrocloud;

-- Asegurar que el módulo de cultivos existe y está activo
INSERT INTO modules (name, code, description, active) VALUES
('Cultivos', 'crops', 'Módulo de gestión de cultivos, siembras y cosechas', TRUE)
ON DUPLICATE KEY UPDATE active=TRUE;

-- Habilitar el módulo de cultivos para todas las empresas existentes
INSERT INTO company_modules (company_id, module_id, enabled)
SELECT e.id, m.id, TRUE
FROM empresas e
CROSS JOIN modules m
WHERE m.code = 'crops'
AND NOT EXISTS (
    SELECT 1 FROM company_modules cm 
    WHERE cm.company_id = e.id AND cm.module_id = m.id
);

-- Crear o actualizar roles básicos
INSERT INTO roles (name, descripcion, activo) VALUES
('ADMIN', 'Administrador con acceso completo a todos los módulos', TRUE),
('OPERATOR', 'Operador con permisos de lectura y escritura', TRUE),
('VIEWER', 'Visualizador con permisos de solo lectura', TRUE)
ON DUPLICATE KEY UPDATE descripcion=VALUES(descripcion), activo=TRUE;

-- Asignar permisos básicos a los roles sobre el módulo de cultivos
INSERT INTO role_permissions (role_id, module_id, read_permission, write_permission, manage_permission)
SELECT r.id, m.id, 
    TRUE,  -- Todos pueden leer
    CASE WHEN r.name = 'ADMIN' THEN TRUE WHEN r.name = 'OPERATOR' THEN TRUE ELSE FALSE END,  -- Admin y Operator pueden escribir
    CASE WHEN r.name = 'ADMIN' THEN TRUE ELSE FALSE END  -- Solo Admin puede gestionar
FROM roles r
CROSS JOIN modules m
WHERE m.code = 'crops'
AND r.name IN ('ADMIN', 'OPERATOR', 'VIEWER')
ON DUPLICATE KEY UPDATE 
    read_permission=VALUES(read_permission),
    write_permission=VALUES(write_permission),
    manage_permission=VALUES(manage_permission);

-- Verificar datos insertados
SELECT 'Módulos creados:' as info;
SELECT * FROM modules;

SELECT 'Módulos habilitados por empresa:' as info;
SELECT cm.id, e.nombre as empresa, m.name as modulo, m.code, cm.enabled
FROM company_modules cm
JOIN empresas e ON cm.company_id = e.id
JOIN modules m ON cm.module_id = m.id;

SELECT 'Permisos de roles sobre módulos:' as info;
SELECT rp.id, r.name as rol, m.name as modulo, m.code, 
       rp.read_permission as `read`, rp.write_permission as `write`, rp.manage_permission as `manage`
FROM role_permissions rp
JOIN roles r ON rp.role_id = r.id
JOIN modules m ON rp.module_id = m.id;

