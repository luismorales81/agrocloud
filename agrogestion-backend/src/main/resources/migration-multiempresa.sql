-- =====================================================
-- MIGRACIÓN A ARQUITECTURA MULTIEMPRESA (MULTI-TENANT)
-- Sistema de Gestión Agrícola AgroCloud
-- =====================================================

-- Usar la base de datos
USE agrocloud;

-- =====================================================
-- 1. CREAR TABLA EMPRESA
-- =====================================================

CREATE TABLE IF NOT EXISTS empresas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(200) NOT NULL,
    cuit VARCHAR(20) UNIQUE,
    email_contacto VARCHAR(100),
    telefono_contacto VARCHAR(20),
    direccion TEXT,
    estado ENUM('ACTIVO', 'INACTIVO', 'SUSPENDIDO', 'TRIAL') DEFAULT 'TRIAL',
    fecha_inicio_trial DATE,
    fecha_fin_trial DATE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    creado_por BIGINT,
    activo BOOLEAN DEFAULT TRUE,
    
    INDEX idx_empresa_estado (estado),
    INDEX idx_empresa_cuit (cuit),
    
    FOREIGN KEY (creado_por) REFERENCES usuarios(id) ON DELETE SET NULL
);

-- =====================================================
-- 2. CREAR TABLA INTERMEDIA USUARIO-EMPRESA
-- =====================================================

CREATE TABLE IF NOT EXISTS usuario_empresas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    empresa_id BIGINT NOT NULL,
    rol ENUM('ADMINISTRADOR', 'ASESOR', 'OPERARIO', 'CONTADOR', 'TECNICO', 'LECTURA') DEFAULT 'OPERARIO',
    estado ENUM('ACTIVO', 'INACTIVO', 'PENDIENTE') DEFAULT 'PENDIENTE',
    fecha_inicio DATE,
    fecha_fin DATE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    creado_por BIGINT,
    
    UNIQUE KEY uk_usuario_empresa (usuario_id, empresa_id),
    INDEX idx_usuario_empresa_rol (rol),
    INDEX idx_usuario_empresa_estado (estado),
    
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE,
    FOREIGN KEY (creado_por) REFERENCES usuarios(id) ON DELETE SET NULL
);

-- =====================================================
-- 3. AGREGAR EMPRESA_ID A TABLA USUARIOS
-- =====================================================

-- Verificar si la columna empresa_id ya existe en usuarios
SET @col_exists = 0;
SELECT COUNT(*) INTO @col_exists 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'agrocloud' 
AND TABLE_NAME = 'usuarios' 
AND COLUMN_NAME = 'empresa_id';

-- Agregar columna empresa_id a usuarios solo si no existe
SET @sql = IF(@col_exists = 0, 
    'ALTER TABLE usuarios ADD COLUMN empresa_id BIGINT AFTER parent_user_id', 
    'SELECT "Columna empresa_id ya existe en usuarios" as mensaje');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Verificar si el índice ya existe
SET @idx_exists = 0;
SELECT COUNT(*) INTO @idx_exists 
FROM INFORMATION_SCHEMA.STATISTICS 
WHERE TABLE_SCHEMA = 'agrocloud' 
AND TABLE_NAME = 'usuarios' 
AND INDEX_NAME = 'idx_usuario_empresa';

-- Agregar índice solo si no existe
SET @sql = IF(@idx_exists = 0, 
    'ALTER TABLE usuarios ADD INDEX idx_usuario_empresa (empresa_id)', 
    'SELECT "Índice idx_usuario_empresa ya existe" as mensaje');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Verificar si la clave foránea ya existe
SET @fk_exists = 0;
SELECT COUNT(*) INTO @fk_exists 
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE 
WHERE TABLE_SCHEMA = 'agrocloud' 
AND TABLE_NAME = 'usuarios' 
AND COLUMN_NAME = 'empresa_id' 
AND REFERENCED_TABLE_NAME = 'empresas';

-- Agregar clave foránea solo si no existe
SET @sql = IF(@fk_exists = 0, 
    'ALTER TABLE usuarios ADD FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE SET NULL', 
    'SELECT "Clave foránea empresa_id ya existe en usuarios" as mensaje');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- =====================================================
-- 4. INSERTAR DATOS INICIALES (EMPRESA DEMO)
-- =====================================================

-- Insertar empresa por defecto para datos existentes (solo si no existe)
INSERT IGNORE INTO empresas (id, nombre, cuit, email_contacto, estado, fecha_inicio_trial, fecha_fin_trial, creado_por, activo) 
VALUES (1, 'Empresa Demo', '20-12345678-9', 'admin@agrocloud.com', 'ACTIVO', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 30 DAY), 1, TRUE);

-- =====================================================
-- 5. AGREGAR EMPRESA_ID A TODAS LAS ENTIDADES PRINCIPALES
-- =====================================================

-- Campos
ALTER TABLE campos 
ADD COLUMN empresa_id BIGINT AFTER user_id;
UPDATE campos SET empresa_id = 1 WHERE empresa_id IS NULL;
ALTER TABLE campos 
ADD INDEX idx_campo_empresa (empresa_id),
ADD FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE;

-- Lotes
ALTER TABLE lotes 
ADD COLUMN empresa_id BIGINT AFTER user_id;
UPDATE lotes SET empresa_id = 1 WHERE empresa_id IS NULL;
ALTER TABLE lotes 
ADD INDEX idx_lote_empresa (empresa_id),
ADD FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE;

-- Insumos
ALTER TABLE insumos 
ADD COLUMN empresa_id BIGINT AFTER usuario_id;
UPDATE insumos SET empresa_id = 1 WHERE empresa_id IS NULL;
ALTER TABLE insumos 
ADD INDEX idx_insumo_empresa (empresa_id),
ADD FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE;

-- Maquinaria
ALTER TABLE maquinaria 
ADD COLUMN empresa_id BIGINT AFTER user_id;
UPDATE maquinaria SET empresa_id = 1 WHERE empresa_id IS NULL;
ALTER TABLE maquinaria 
ADD INDEX idx_maquinaria_empresa (empresa_id),
ADD FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE;

-- Ingresos
ALTER TABLE ingresos 
ADD COLUMN empresa_id BIGINT AFTER user_id;
UPDATE ingresos SET empresa_id = 1 WHERE empresa_id IS NULL;
ALTER TABLE ingresos 
ADD INDEX idx_ingreso_empresa (empresa_id),
ADD FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE;

-- Egresos
ALTER TABLE egresos 
ADD COLUMN empresa_id BIGINT AFTER user_id;
UPDATE egresos SET empresa_id = 1 WHERE empresa_id IS NULL;
ALTER TABLE egresos 
ADD INDEX idx_egreso_empresa (empresa_id),
ADD FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE;

-- Labores
ALTER TABLE labores 
ADD COLUMN empresa_id BIGINT AFTER usuario_id;
UPDATE labores SET empresa_id = 1 WHERE empresa_id IS NULL;
ALTER TABLE labores 
ADD INDEX idx_labor_empresa (empresa_id),
ADD FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE;

-- Cultivos
ALTER TABLE cultivos 
ADD COLUMN empresa_id BIGINT AFTER usuario_id;
UPDATE cultivos SET empresa_id = 1 WHERE empresa_id IS NULL;
ALTER TABLE cultivos 
ADD INDEX idx_cultivo_empresa (empresa_id),
ADD FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE;

-- Alquiler Maquinaria
ALTER TABLE alquiler_maquinaria 
ADD COLUMN empresa_id BIGINT AFTER user_id;
UPDATE alquiler_maquinaria SET empresa_id = 1 WHERE empresa_id IS NULL;
ALTER TABLE alquiler_maquinaria 
ADD INDEX idx_alquiler_empresa (empresa_id),
ADD FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE;

-- Mantenimiento Maquinaria
ALTER TABLE mantenimiento_maquinaria 
ADD COLUMN empresa_id BIGINT AFTER usuario_id;
UPDATE mantenimiento_maquinaria SET empresa_id = 1 WHERE empresa_id IS NULL;
ALTER TABLE mantenimiento_maquinaria 
ADD INDEX idx_mantenimiento_empresa (empresa_id),
ADD FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE;

-- Logs de Acceso
ALTER TABLE log_acceso 
ADD COLUMN empresa_id BIGINT AFTER usuario_id;
UPDATE log_acceso SET empresa_id = 1 WHERE empresa_id IS NULL;
ALTER TABLE log_acceso 
ADD INDEX idx_log_empresa (empresa_id),
ADD FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE SET NULL;

-- =====================================================
-- 6. CREAR TABLA DE CONFIGURACIÓN POR EMPRESA
-- =====================================================

CREATE TABLE IF NOT EXISTS configuracion_empresa (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id BIGINT NOT NULL,
    clave VARCHAR(100) NOT NULL,
    valor TEXT,
    descripcion VARCHAR(255),
    tipo ENUM('STRING', 'NUMBER', 'BOOLEAN', 'JSON') DEFAULT 'STRING',
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    UNIQUE KEY uk_empresa_clave (empresa_id, clave),
    INDEX idx_configuracion_empresa (empresa_id),
    
    FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE
);

-- =====================================================
-- 7. CREAR TABLA DE AUDITORÍA MULTIEMPRESA
-- =====================================================

CREATE TABLE IF NOT EXISTS auditoria_empresa (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id BIGINT NOT NULL,
    usuario_id BIGINT,
    accion VARCHAR(100) NOT NULL,
    entidad VARCHAR(50) NOT NULL,
    entidad_id BIGINT,
    datos_anteriores JSON,
    datos_nuevos JSON,
    ip_address VARCHAR(45),
    user_agent TEXT,
    fecha_accion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_auditoria_empresa (empresa_id),
    INDEX idx_auditoria_fecha (fecha_accion),
    INDEX idx_auditoria_accion (accion),
    
    FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE SET NULL
);

-- =====================================================
-- 8. COMPLETAR DATOS INICIALES
-- =====================================================

-- Actualizar usuarios existentes para asignarlos a la empresa demo
UPDATE usuarios SET empresa_id = 1 WHERE empresa_id IS NULL;

-- Crear relación usuario-empresa para usuarios existentes (solo si no existe)
INSERT IGNORE INTO usuario_empresas (usuario_id, empresa_id, rol, estado, fecha_inicio, creado_por)
SELECT 
    u.id, 
    1, 
    CASE 
        WHEN EXISTS (SELECT 1 FROM usuarios_roles ur JOIN roles r ON ur.rol_id = r.id WHERE ur.usuario_id = u.id AND r.nombre = 'ADMINISTRADOR') 
        THEN 'ADMINISTRADOR'
        ELSE 'OPERARIO'
    END,
    'ACTIVO',
    CURDATE(),
    1
FROM usuarios u 
WHERE NOT EXISTS (SELECT 1 FROM usuario_empresas ue WHERE ue.usuario_id = u.id AND ue.empresa_id = 1);

-- =====================================================
-- 9. CREAR VISTAS PARA FACILITAR CONSULTAS
-- =====================================================

-- Vista de usuarios con información de empresa
CREATE OR REPLACE VIEW vista_usuarios_empresas AS
SELECT 
    u.id,
    u.username,
    u.email,
    u.first_name,
    u.last_name,
    u.activo as usuario_activo,
    e.id as empresa_id,
    e.nombre as empresa_nombre,
    e.estado as empresa_estado,
    ue.rol,
    ue.estado as rol_estado,
    ue.fecha_inicio,
    ue.fecha_fin
FROM usuarios u
LEFT JOIN usuario_empresas ue ON u.id = ue.usuario_id
LEFT JOIN empresas e ON ue.empresa_id = e.id
WHERE ue.estado = 'ACTIVO' OR ue.estado IS NULL;

-- Vista de estadísticas por empresa
CREATE OR REPLACE VIEW vista_estadisticas_empresa AS
SELECT 
    e.id as empresa_id,
    e.nombre as empresa_nombre,
    e.estado as empresa_estado,
    COUNT(DISTINCT ue.usuario_id) as total_usuarios,
    COUNT(DISTINCT c.id) as total_campos,
    COUNT(DISTINCT l.id) as total_lotes,
    COUNT(DISTINCT i.id) as total_insumos,
    COUNT(DISTINCT m.id) as total_maquinaria,
    COUNT(DISTINCT ing.id) as total_ingresos,
    COUNT(DISTINCT eg.id) as total_egresos,
    COALESCE(SUM(ing.monto), 0) as total_ingresos_monto,
    COALESCE(SUM(eg.monto), 0) as total_egresos_monto
FROM empresas e
LEFT JOIN usuario_empresas ue ON e.id = ue.empresa_id AND ue.estado = 'ACTIVO'
LEFT JOIN campos c ON e.id = c.empresa_id
LEFT JOIN lotes l ON e.id = c.empresa_id
LEFT JOIN insumos i ON e.id = i.empresa_id
LEFT JOIN maquinaria m ON e.id = m.empresa_id
LEFT JOIN ingresos ing ON e.id = ing.empresa_id
LEFT JOIN egresos eg ON e.id = eg.empresa_id
     GROUP BY e.id, e.nombre, e.estado;

-- =====================================================
-- 10. CREAR PROCEDIMIENTOS ALMACENADOS ÚTILES
-- =====================================================

DELIMITER //

-- Procedimiento para crear una nueva empresa con usuario administrador
CREATE PROCEDURE CrearEmpresaConAdmin(
    IN p_nombre VARCHAR(200),
    IN p_cuit VARCHAR(20),
    IN p_email_contacto VARCHAR(100),
    IN p_admin_username VARCHAR(50),
    IN p_admin_email VARCHAR(100),
    IN p_admin_password VARCHAR(255),
    IN p_admin_first_name VARCHAR(100),
    IN p_admin_last_name VARCHAR(100),
    OUT p_empresa_id BIGINT,
    OUT p_usuario_id BIGINT
)
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;
    
    START TRANSACTION;
    
         -- Crear empresa
     INSERT INTO empresas (nombre, cuit, email_contacto, estado, fecha_inicio_trial, fecha_fin_trial, creado_por, activo)
     VALUES (p_nombre, p_cuit, p_email_contacto, 'TRIAL', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 30 DAY), 1, TRUE);
    
    SET p_empresa_id = LAST_INSERT_ID();
    
    -- Crear usuario administrador
    INSERT INTO usuarios (username, email, password, first_name, last_name, activo, empresa_id, estado)
    VALUES (p_admin_username, p_admin_email, p_admin_password, p_admin_first_name, p_admin_last_name, TRUE, p_empresa_id, 'ACTIVO');
    
    SET p_usuario_id = LAST_INSERT_ID();
    
    -- Asignar rol de administrador
    INSERT INTO usuarios_roles (usuario_id, rol_id)
    SELECT p_usuario_id, id FROM roles WHERE nombre = 'ADMINISTRADOR';
    
    -- Crear relación usuario-empresa
    INSERT INTO usuario_empresas (usuario_id, empresa_id, rol, estado, fecha_inicio, creado_por)
    VALUES (p_usuario_id, p_empresa_id, 'ADMINISTRADOR', 'ACTIVO', CURDATE(), 1);
    
    COMMIT;
END //

-- Procedimiento para obtener estadísticas de una empresa
CREATE PROCEDURE ObtenerEstadisticasEmpresa(IN p_empresa_id BIGINT)
BEGIN
    SELECT 
        e.nombre as empresa_nombre,
        e.estado as empresa_estado,
        COUNT(DISTINCT ue.usuario_id) as total_usuarios,
        COUNT(DISTINCT c.id) as total_campos,
        COUNT(DISTINCT l.id) as total_lotes,
        COUNT(DISTINCT i.id) as total_insumos,
        COUNT(DISTINCT m.id) as total_maquinaria,
        COUNT(DISTINCT ing.id) as total_ingresos,
        COUNT(DISTINCT eg.id) as total_egresos,
        COALESCE(SUM(ing.monto), 0) as total_ingresos_monto,
        COALESCE(SUM(eg.monto), 0) as total_egresos_monto,
        COALESCE(SUM(ing.monto), 0) - COALESCE(SUM(eg.monto), 0) as balance_general
    FROM empresas e
    LEFT JOIN usuario_empresas ue ON e.id = ue.empresa_id AND ue.estado = 'ACTIVO'
    LEFT JOIN campos c ON e.id = c.empresa_id
    LEFT JOIN lotes l ON e.id = c.empresa_id
    LEFT JOIN insumos i ON e.id = i.empresa_id
    LEFT JOIN maquinaria m ON e.id = m.empresa_id
    LEFT JOIN ingresos ing ON e.id = ing.empresa_id
    LEFT JOIN egresos eg ON e.id = eg.empresa_id
    WHERE e.id = p_empresa_id
         GROUP BY e.id, e.nombre, e.estado;
END //

DELIMITER ;

-- =====================================================
-- 11. CREAR ÍNDICES ADICIONALES PARA OPTIMIZACIÓN
-- =====================================================

-- Índices compuestos para consultas multiempresa
CREATE INDEX idx_campos_empresa_activo ON campos(empresa_id, activo);
-- CREATE INDEX idx_lotes_empresa_estado ON lotes(empresa_id, estado); -- Removido: empresa_id ya no existe en lotes
CREATE INDEX idx_insumos_empresa_activo ON insumos(empresa_id, activo);
CREATE INDEX idx_maquinaria_empresa_activo ON maquinaria(empresa_id, activo);
CREATE INDEX idx_ingresos_empresa_fecha ON ingresos(empresa_id, fecha);
CREATE INDEX idx_egresos_empresa_fecha ON egresos(empresa_id, fecha);
CREATE INDEX idx_labores_empresa_fecha ON labores(empresa_id, fecha);

-- =====================================================
-- MIGRACIÓN COMPLETADA
-- =====================================================

SELECT 'Migración a arquitectura multiempresa completada exitosamente' as resultado;
