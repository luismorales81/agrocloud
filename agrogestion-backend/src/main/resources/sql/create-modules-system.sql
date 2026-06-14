-- ========================================
-- SISTEMA DE MÓDULOS Y PERMISOS
-- ========================================
-- Script para crear las tablas del sistema de módulos (feature flags)
-- Ejecutar este script en la base de datos local

-- Tabla de módulos
CREATE TABLE IF NOT EXISTS modules (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_code (code),
    INDEX idx_active (active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla de relación empresa-módulo
CREATE TABLE IF NOT EXISTS company_modules (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    company_id BIGINT NOT NULL,
    module_id BIGINT NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_company_module (company_id, module_id),
    FOREIGN KEY (company_id) REFERENCES empresas(id) ON DELETE CASCADE,
    FOREIGN KEY (module_id) REFERENCES modules(id) ON DELETE CASCADE,
    INDEX idx_company (company_id),
    INDEX idx_module (module_id),
    INDEX idx_enabled (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla de permisos de rol sobre módulos
CREATE TABLE IF NOT EXISTS role_permissions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_id BIGINT NOT NULL,
    module_id BIGINT NOT NULL,
    read_permission BOOLEAN NOT NULL DEFAULT FALSE,
    write_permission BOOLEAN NOT NULL DEFAULT FALSE,
    manage_permission BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_role_module (role_id, module_id),
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (module_id) REFERENCES modules(id) ON DELETE CASCADE,
    INDEX idx_role (role_id),
    INDEX idx_module (module_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla de relación usuario-rol
CREATE TABLE IF NOT EXISTS user_roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_role (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    INDEX idx_user (user_id),
    INDEX idx_role (role_id),
    INDEX idx_active (active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- DATOS INICIALES
-- ========================================

-- Insertar módulos iniciales
INSERT INTO modules (name, code, description, active) VALUES
('Cultivos', 'crops', 'Módulo de gestión de cultivos, siembras y cosechas', TRUE),
('Ganadería', 'cattle', 'Módulo de gestión de ganado bovino', FALSE),
('Porcinos', 'pigs', 'Módulo de gestión de porcinos', FALSE),
('Inteligencia Artificial', 'ai', 'Módulo de análisis con IA y predicciones', FALSE),
('Reportes Avanzados', 'reports', 'Módulo de reportes y análisis avanzados', FALSE),
('Finanzas', 'finance', 'Módulo de gestión financiera avanzada', TRUE),
('Inventario', 'inventory', 'Módulo de gestión de inventario', TRUE)
ON DUPLICATE KEY UPDATE name=VALUES(name), description=VALUES(description);

-- Crear roles básicos si no existen
INSERT INTO roles (name, descripcion, activo) VALUES
('ADMIN', 'Administrador con acceso completo', TRUE),
('OPERATOR', 'Operador con permisos de lectura y escritura', TRUE),
('VIEWER', 'Visualizador con permisos de solo lectura', TRUE)
ON DUPLICATE KEY UPDATE descripcion=VALUES(descripcion);

-- Asignar permisos por defecto a los roles sobre el módulo de cultivos
-- (Solo para el módulo 'crops' que está activo)
INSERT INTO role_permissions (role_id, module_id, read_permission, write_permission, manage_permission)
SELECT r.id, m.id, 
    CASE WHEN r.name = 'ADMIN' THEN TRUE ELSE TRUE END,  -- Todos pueden leer
    CASE WHEN r.name = 'ADMIN' THEN TRUE WHEN r.name = 'OPERATOR' THEN TRUE ELSE FALSE END,  -- Admin y Operator pueden escribir
    CASE WHEN r.name = 'ADMIN' THEN TRUE ELSE FALSE END  -- Solo Admin puede gestionar
FROM roles r
CROSS JOIN modules m
WHERE m.code = 'crops'
ON DUPLICATE KEY UPDATE 
    read_permission=VALUES(read_permission),
    write_permission=VALUES(write_permission),
    manage_permission=VALUES(manage_permission);

-- ========================================
-- VERIFICACIÓN
-- ========================================
-- Ejecutar estas consultas para verificar que todo se creó correctamente

-- SELECT * FROM modules;
-- SELECT * FROM company_modules;
-- SELECT * FROM role_permissions;
-- SELECT * FROM user_roles;











