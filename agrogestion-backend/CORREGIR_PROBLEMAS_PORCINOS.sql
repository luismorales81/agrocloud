-- ============================================================================
-- SCRIPT PARA CORREGIR PROBLEMAS IDENTIFICADOS EN MÓDULO PORCINOS
-- ============================================================================

USE agrocloud;

-- ============================================================================
-- 1. CORREGIR ENUM estado_servicio EN porcinos_servicios
-- ============================================================================

-- El enum tiene problema de codificación con PREÑEZ_CONFIRMADA
-- Solución: Modificar el enum para corregir el valor

-- Primero, actualizar los registros existentes que tengan el valor incorrecto
UPDATE porcinos_servicios 
SET estado_servicio = 'PENDIENTE_CONTROL' 
WHERE estado_servicio = 'PRE├æEZ_CONFIRMADA';

-- Convertir enum a VARCHAR con CHECK constraint (solución para problema de codificación con Ñ)
ALTER TABLE porcinos_servicios 
MODIFY COLUMN estado_servicio VARCHAR(50) NOT NULL DEFAULT 'PENDIENTE_CONTROL';

-- Agregar CHECK constraint para validar valores permitidos
ALTER TABLE porcinos_servicios 
ADD CONSTRAINT chk_estado_servicio 
CHECK (estado_servicio IN ('PENDIENTE_CONTROL', 'FALLIDO', 'PREÑEZ_CONFIRMADA'));

SELECT '✓ Enum estado_servicio corregido' AS resultado;

-- ============================================================================
-- 2. CREAR TABLA porcinos_tipos_parto
-- ============================================================================

CREATE TABLE IF NOT EXISTS porcinos_tipos_parto (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    requiere_intervencion BOOLEAN NOT NULL DEFAULT FALSE,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    empresa_id BIGINT NOT NULL,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE,
    
    INDEX idx_tipos_parto_empresa (empresa_id),
    INDEX idx_tipos_parto_activo (activo),
    UNIQUE KEY uk_tipos_parto_empresa_nombre (empresa_id, nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SELECT '✓ Tabla porcinos_tipos_parto creada' AS resultado;

-- ============================================================================
-- 3. CREAR TABLA porcinos_ubicaciones_internas
-- ============================================================================

CREATE TABLE IF NOT EXISTS porcinos_ubicaciones_internas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(200) NOT NULL,
    codigo VARCHAR(50),
    nivel ENUM('GALPON', 'SALA', 'CORRAL') NOT NULL,
    ubicacion_padre_id BIGINT NULL,
    tipo_ubicacion ENUM('MATERNIDAD', 'GESTACION', 'RECRIA', 'ENGORDE', 'AISLAMIENTO', 'GENERAL'),
    capacidad_maxima INT,
    descripcion TEXT,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    empresa_id BIGINT NOT NULL,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE,
    FOREIGN KEY (ubicacion_padre_id) REFERENCES porcinos_ubicaciones_internas(id) ON DELETE SET NULL,
    
    INDEX idx_ubicaciones_empresa (empresa_id),
    INDEX idx_ubicaciones_nivel (nivel),
    INDEX idx_ubicaciones_tipo (tipo_ubicacion),
    INDEX idx_ubicaciones_padre (ubicacion_padre_id),
    INDEX idx_ubicaciones_activo (activo),
    INDEX idx_ubicaciones_codigo (codigo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SELECT '✓ Tabla porcinos_ubicaciones_internas creada' AS resultado;

-- ============================================================================
-- 4. INSERTAR DATOS INICIALES DE TIPOS DE PARTO
-- ============================================================================

-- Obtener empresa
SET @empresa_id = (SELECT id FROM empresas WHERE nombre LIKE '%AgroCloud%' AND activo = 1 LIMIT 1);
SET @empresa_id = IFNULL(@empresa_id, (SELECT id FROM empresas WHERE activo = 1 LIMIT 1));

-- Insertar tipos de parto básicos
INSERT INTO porcinos_tipos_parto (nombre, descripcion, requiere_intervencion, activo, empresa_id, fecha_creacion)
VALUES
('Normal', 'Parto sin complicaciones', FALSE, TRUE, @empresa_id, NOW()),
('Asistido', 'Parto con asistencia básica', TRUE, TRUE, @empresa_id, NOW()),
('Distocia', 'Parto con dificultades, requiere intervención', TRUE, TRUE, @empresa_id, NOW()),
('Cesárea', 'Parto por cesárea', TRUE, TRUE, @empresa_id, NOW())
ON DUPLICATE KEY UPDATE nombre = nombre;

SELECT CONCAT('✓ Tipos de parto insertados: ', (SELECT COUNT(*) FROM porcinos_tipos_parto WHERE empresa_id = @empresa_id)) AS resultado;

-- ============================================================================
-- 5. INSERTAR DATOS INICIALES DE UBICACIONES INTERNAS
-- ============================================================================

-- Insertar ubicaciones básicas (estructura jerárquica)
INSERT INTO porcinos_ubicaciones_internas (nombre, codigo, nivel, tipo_ubicacion, capacidad_maxima, descripcion, activo, empresa_id, fecha_creacion)
VALUES
('Galpón Principal', 'GAL-01', 'GALPON', 'GENERAL', 200, 'Galpón principal de la granja', TRUE, @empresa_id, NOW()),
('Sala Maternidad', 'MAT-01', 'SALA', 'MATERNIDAD', 20, 'Sala de maternidad', TRUE, @empresa_id, NOW()),
('Sala Gestación', 'GES-01', 'SALA', 'GESTACION', 40, 'Sala de gestación', TRUE, @empresa_id, NOW())
ON DUPLICATE KEY UPDATE nombre = nombre;

-- Obtener IDs para establecer jerarquía
SET @galpon_id = (SELECT id FROM porcinos_ubicaciones_internas WHERE codigo = 'GAL-01' AND empresa_id = @empresa_id LIMIT 1);
SET @maternidad_id = (SELECT id FROM porcinos_ubicaciones_internas WHERE codigo = 'MAT-01' AND empresa_id = @empresa_id LIMIT 1);
SET @gestacion_id = (SELECT id FROM porcinos_ubicaciones_internas WHERE codigo = 'GES-01' AND empresa_id = @empresa_id LIMIT 1);

-- Establecer jerarquía (salas dentro del galpón)
UPDATE porcinos_ubicaciones_internas 
SET ubicacion_padre_id = @galpon_id 
WHERE id IN (@maternidad_id, @gestacion_id) AND empresa_id = @empresa_id;

SELECT CONCAT('✓ Ubicaciones internas insertadas: ', (SELECT COUNT(*) FROM porcinos_ubicaciones_internas WHERE empresa_id = @empresa_id)) AS resultado;

-- ============================================================================
-- RESUMEN FINAL
-- ============================================================================

SELECT '' AS '';
SELECT '════════════════════════════════════════════════' AS '';
SELECT 'CORRECCIONES APLICADAS EXITOSAMENTE' AS '';
SELECT '════════════════════════════════════════════════' AS '';
SELECT '' AS '';
SELECT '1. Enum estado_servicio corregido' AS correccion;
SELECT '2. Tabla porcinos_tipos_parto creada' AS correccion;
SELECT '3. Tabla porcinos_ubicaciones_internas creada' AS correccion;
SELECT '4. Datos iniciales insertados' AS correccion;
SELECT '' AS '';
SELECT '✓ PROCESO COMPLETADO EXITOSAMENTE' AS '';
SELECT '════════════════════════════════════════════════' AS '';

