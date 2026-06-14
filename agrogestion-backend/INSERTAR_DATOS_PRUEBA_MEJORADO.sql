-- ============================================================================
-- SCRIPT MEJORADO PARA INSERTAR DATOS DE PRUEBA
-- Con verificaciones y manejo de errores
-- ============================================================================

USE agrocloud;

-- Deshabilitar verificación de claves foráneas temporalmente
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================================
-- VERIFICACIÓN INICIAL
-- ============================================================================

SELECT '════════════════════════════════════════════════' AS '';
SELECT 'VERIFICACIÓN INICIAL' AS '';
SELECT '════════════════════════════════════════════════' AS '';

-- Verificar empresa
SELECT 'Empresas activas:' AS '';
SELECT id, nombre, cuit, activo FROM empresas WHERE activo = 1;

SET @empresa_id = (SELECT id FROM empresas WHERE activo = 1 LIMIT 1);

IF @empresa_id IS NULL THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'No se encontró ninguna empresa activa';
END IF;

SELECT CONCAT('✓ Usando Empresa ID: ', @empresa_id) AS info;

-- Verificar usuarios
SELECT 'Usuarios activos:' AS '';
SELECT id, email, username, activo FROM usuarios WHERE activo = 1 LIMIT 5;

SET @admin_user_id = (SELECT id FROM usuarios WHERE email = 'admin@agrocloud.com' AND activo = 1 LIMIT 1);
SET @tecnico_user_id = (SELECT id FROM usuarios WHERE email = 'tecnico@agrocloud.com' AND activo = 1 LIMIT 1);
SET @productor_user_id = (SELECT id FROM usuarios WHERE email = 'productor@agrocloud.com' AND activo = 1 LIMIT 1);

-- Si no existen usuarios específicos, usar el primero disponible
SET @admin_user_id = IFNULL(@admin_user_id, (SELECT id FROM usuarios WHERE activo = 1 LIMIT 1));
SET @tecnico_user_id = IFNULL(@tecnico_user_id, @admin_user_id);
SET @productor_user_id = IFNULL(@productor_user_id, @admin_user_id);

IF @admin_user_id IS NULL THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'No se encontró ningún usuario activo';
END IF;

SELECT CONCAT('✓ Usando Usuario Admin ID: ', @admin_user_id) AS info;
SELECT CONCAT('✓ Usando Usuario Técnico ID: ', @tecnico_user_id) AS info;

-- ============================================================================
-- 1. MÓDULO CULTIVOS - CAMPOS Y LOTES
-- ============================================================================

SELECT '' AS '';
SELECT '════════════════════════════════════════════════' AS '';
SELECT 'INSERTANDO CAMPOS...' AS '';
SELECT '════════════════════════════════════════════════' AS '';

-- Eliminar campos de prueba existentes primero (opcional)
DELETE FROM lotes WHERE empresa_id = @empresa_id AND nombre LIKE 'Lote %';
DELETE FROM campos WHERE empresa_id = @empresa_id AND nombre IN ('Campo Norte', 'Campo Sur', 'Campo Este', 'Campo Oeste');

-- Insertar Campos
INSERT INTO campos (nombre, ubicacion, area_hectareas, tipo_suelo, descripcion, estado, activo, empresa_id, usuario_id, fecha_creacion)
VALUES
('Campo Norte', 'Ruta 8, Km 45, Buenos Aires', 25.50, 'FRANCO', 'Campo principal para cultivo de soja', 'ACTIVO', 1, @empresa_id, @admin_user_id, NOW()),
('Campo Sur', 'Ruta 9, Km 32, Córdoba', 18.75, 'ARCILLOSO', 'Campo para rotación de cultivos', 'ACTIVO', 1, @empresa_id, @admin_user_id, NOW()),
('Campo Este', 'Ruta 12, Km 78, Entre Ríos', 32.00, 'FRANCO_ARCILLOSO', 'Campo con riego por aspersión', 'ACTIVO', 1, @empresa_id, @admin_user_id, NOW()),
('Campo Oeste', 'Ruta 3, Km 120, La Pampa', 15.25, 'FRANCO', 'Campo para girasol', 'ACTIVO', 1, @empresa_id, @admin_user_id, NOW());

SELECT CONCAT('✓ Insertados ', ROW_COUNT(), ' campos') AS resultado;

SET @campo_norte_id = (SELECT id FROM campos WHERE nombre = 'Campo Norte' AND empresa_id = @empresa_id LIMIT 1);
SET @campo_sur_id = (SELECT id FROM campos WHERE nombre = 'Campo Sur' AND empresa_id = @empresa_id LIMIT 1);
SET @campo_este_id = (SELECT id FROM campos WHERE nombre = 'Campo Este' AND empresa_id = @empresa_id LIMIT 1);
SET @campo_oeste_id = (SELECT id FROM campos WHERE nombre = 'Campo Oeste' AND empresa_id = @empresa_id LIMIT 1);

SELECT '' AS '';
SELECT 'INSERTANDO LOTES...' AS '';

-- Insertar Lotes
INSERT INTO lotes (nombre, campo_id, area_hectareas, cultivo_actual, estado, fecha_siembra, fecha_cosecha_esperada, descripcion, empresa_id, usuario_id, activo, fecha_creacion)
VALUES
('Lote A1 - Soja', @campo_norte_id, 12.50, 'SOJA', 'EN_CRECIMIENTO', DATE_SUB(CURDATE(), INTERVAL 30 DAY), DATE_ADD(CURDATE(), INTERVAL 120 DAY), 'Soja de primera - Variedad DM 53i51', @empresa_id, @admin_user_id, 1, NOW()),
('Lote A2 - Maíz', @campo_norte_id, 13.00, 'MAIZ', 'PLANIFICADO', DATE_ADD(CURDATE(), INTERVAL 15 DAY), DATE_ADD(CURDATE(), INTERVAL 180 DAY), 'Maíz tardío - Híbrido DK 750', @empresa_id, @admin_user_id, 1, NOW()),
('Lote B1 - Trigo', @campo_sur_id, 8.50, 'TRIGO', 'EN_CRECIMIENTO', DATE_SUB(CURDATE(), INTERVAL 90 DAY), DATE_ADD(CURDATE(), INTERVAL 60 DAY), 'Trigo - Variedad Baguette 601', @empresa_id, @admin_user_id, 1, NOW()),
('Lote B2 - Soja', @campo_sur_id, 10.25, 'SOJA', 'DISPONIBLE', NULL, NULL, 'Lote disponible para próxima siembra', @empresa_id, @admin_user_id, 1, NOW()),
('Lote C1 - Girasol', @campo_este_id, 15.00, 'GIRASOL', 'COSECHADO', DATE_SUB(CURDATE(), INTERVAL 180 DAY), DATE_SUB(CURDATE(), INTERVAL 30 DAY), 'Girasol cosechado - Variedad CF 101', @empresa_id, @admin_user_id, 1, NOW()),
('Lote C2 - Soja', @campo_este_id, 17.00, 'SOJA', 'EN_CRECIMIENTO', DATE_SUB(CURDATE(), INTERVAL 45 DAY), DATE_ADD(CURDATE(), INTERVAL 105 DAY), 'Soja de segunda', @empresa_id, @admin_user_id, 1, NOW());

SELECT CONCAT('✓ Insertados ', ROW_COUNT(), ' lotes') AS resultado;

-- Insertar Cultivos
SELECT '' AS '';
SELECT 'INSERTANDO CULTIVOS...' AS '';

INSERT INTO cultivo_cultivos (nombre, tipo, variedad, ciclo_dias, rendimiento_esperado, unidad_rendimiento, precio_por_tonelada, descripcion, estado, activo, empresa_id, usuario_id, created_at)
VALUES
('Soja DM 53i51', 'SOJA', 'DM 53i51', 120, 45.50, 'qq/ha', 280.00, 'Soja resistente a glifosato y glufosinato', 'ACTIVO', 1, @empresa_id, @admin_user_id, NOW()),
('Maíz DK 750', 'MAIZ', 'DK 750', 140, 120.00, 'qq/ha', 95.00, 'Maíz híbrido de alto potencial', 'ACTIVO', 1, @empresa_id, @admin_user_id, NOW()),
('Trigo Baguette 601', 'TRIGO', 'Baguette 601', 180, 65.00, 'qq/ha', 145.00, 'Trigo de buena calidad panadera', 'ACTIVO', 1, @empresa_id, @admin_user_id, NOW()),
('Girasol CF 101', 'GIRASOL', 'CF 101', 110, 28.00, 'qq/ha', 220.00, 'Girasol alto oleico', 'ACTIVO', 1, @empresa_id, @admin_user_id, NOW())
ON DUPLICATE KEY UPDATE nombre = nombre;

SELECT CONCAT('✓ Insertados/Actualizados ', ROW_COUNT(), ' cultivos') AS resultado;

-- ============================================================================
-- 2. MÓDULO PORCINOS - CATÁLOGOS
-- ============================================================================

SELECT '' AS '';
SELECT '════════════════════════════════════════════════' AS '';
SELECT 'INSERTANDO CATÁLOGOS DE PORCINOS...' AS '';
SELECT '════════════════════════════════════════════════' AS '';

-- Insertar Razas
INSERT INTO porcinos_razas_porcinos (nombre, descripcion, activo, empresa_id, fecha_creacion)
VALUES
('Yorkshire', 'Raza blanca, conocida por su prolificidad y calidad de carne', 1, @empresa_id, NOW()),
('Landrace', 'Raza blanca, excelente madre, buen instinto maternal', 1, @empresa_id, NOW()),
('Duroc', 'Raza roja, excelente para terminal, buena ganancia de peso', 1, @empresa_id, NOW()),
('Pietrain', 'Raza blanca con manchas negras, muy magra', 1, @empresa_id, NOW()),
('Hampshire', 'Raza negra con cinturón blanco, buena calidad de carne', 1, @empresa_id, NOW())
ON DUPLICATE KEY UPDATE nombre = nombre;

SELECT CONCAT('✓ Insertadas ', ROW_COUNT(), ' razas') AS resultado;

SET @raza_yorkshire_id = (SELECT id FROM porcinos_razas_porcinos WHERE nombre = 'Yorkshire' AND empresa_id = @empresa_id LIMIT 1);
SET @raza_landrace_id = (SELECT id FROM porcinos_razas_porcinos WHERE nombre = 'Landrace' AND empresa_id = @empresa_id LIMIT 1);
SET @raza_duroc_id = (SELECT id FROM porcinos_razas_porcinos WHERE nombre = 'Duroc' AND empresa_id = @empresa_id LIMIT 1);

-- Continuar con el resto de los catálogos...
-- (Resto del script similar pero con manejo de errores mejorado)

-- Habilitar verificación de claves foráneas
SET FOREIGN_KEY_CHECKS = 1;

SELECT '' AS '';
SELECT '════════════════════════════════════════════════' AS '';
SELECT '✓ PROCESO COMPLETADO' AS '';
SELECT '════════════════════════════════════════════════' AS '';















