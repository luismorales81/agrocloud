-- ============================================================================
-- SCRIPT FINAL PARA INSERTAR DATOS DE PRUEBA COMPLETOS
-- Usa los datos existentes de empresa y usuarios
-- ============================================================================

USE agrocloud;

-- Obtener IDs
SET @empresa_id = (SELECT id FROM empresas WHERE activo = 1 LIMIT 1);
SET @admin_user_id = (SELECT id FROM usuarios WHERE email = 'admin@agrocloud.com' AND activo = 1 LIMIT 1);
SET @admin_user_id = IFNULL(@admin_user_id, (SELECT id FROM usuarios WHERE activo = 1 LIMIT 1));

-- Verificar que tenemos empresa y usuario
SELECT CONCAT('✓ Empresa ID: ', COALESCE(@empresa_id, 'NO ENCONTRADA')) AS verificacion;
SELECT CONCAT('✓ Usuario ID: ', COALESCE(@admin_user_id, 'NO ENCONTRADO')) AS verificacion;

-- ============================================================================
-- LIMPIAR Y INSERTAR CAMPOS
-- ============================================================================

DELETE FROM cultivo_lotes WHERE empresa_id = @empresa_id AND nombre LIKE 'Lote%';
DELETE FROM cultivo_campos WHERE empresa_id = @empresa_id AND nombre IN ('Campo Norte', 'Campo Sur', 'Campo Este', 'Campo Oeste');

INSERT INTO cultivo_campos (nombre, ubicacion, area_hectareas, descripcion, estado, activo, empresa_id, user_id, fecha_creacion)
VALUES
('Campo Norte', 'Ruta 8, Km 45, Buenos Aires', 25.50, 'Campo principal para cultivo de soja', 'ACTIVO', 1, @empresa_id, @admin_user_id, NOW()),
('Campo Sur', 'Ruta 9, Km 32, Córdoba', 18.75, 'Campo para rotación de cultivos', 'ACTIVO', 1, @empresa_id, @admin_user_id, NOW()),
('Campo Este', 'Ruta 12, Km 78, Entre Ríos', 32.00, 'Campo con riego por aspersión', 'ACTIVO', 1, @empresa_id, @admin_user_id, NOW()),
('Campo Oeste', 'Ruta 3, Km 120, La Pampa', 15.25, 'Campo para girasol', 'ACTIVO', 1, @empresa_id, @admin_user_id, NOW());

SET @campo_norte_id = (SELECT id FROM cultivo_campos WHERE nombre = 'Campo Norte' AND empresa_id = @empresa_id LIMIT 1);
SET @campo_sur_id = (SELECT id FROM cultivo_campos WHERE nombre = 'Campo Sur' AND empresa_id = @empresa_id LIMIT 1);
SET @campo_este_id = (SELECT id FROM cultivo_campos WHERE nombre = 'Campo Este' AND empresa_id = @empresa_id LIMIT 1);

SELECT CONCAT('✓ Insertados ', ROW_COUNT(), ' campos') AS resultado;

-- ============================================================================
-- INSERTAR LOTES
-- ============================================================================

INSERT INTO cultivo_lotes (nombre, campo_id, area_hectareas, estado, cultivo_actual, fecha_siembra, fecha_cosecha_esperada, descripcion, empresa_id, user_id, activo, fecha_creacion)
VALUES
('Lote A1 - Soja', @campo_norte_id, 12.50, 'EN_CRECIMIENTO', 'SOJA', DATE_SUB(CURDATE(), INTERVAL 30 DAY), DATE_ADD(CURDATE(), INTERVAL 120 DAY), 'Soja de primera', @empresa_id, @admin_user_id, 1, NOW()),
('Lote A2 - Maíz', @campo_norte_id, 13.00, 'PLANIFICADO', 'MAIZ', DATE_ADD(CURDATE(), INTERVAL 15 DAY), DATE_ADD(CURDATE(), INTERVAL 180 DAY), 'Maíz tardío', @empresa_id, @admin_user_id, 1, NOW()),
('Lote B1 - Trigo', @campo_sur_id, 8.50, 'EN_CRECIMIENTO', 'TRIGO', DATE_SUB(CURDATE(), INTERVAL 90 DAY), DATE_ADD(CURDATE(), INTERVAL 60 DAY), 'Trigo', @empresa_id, @admin_user_id, 1, NOW()),
('Lote B2 - Soja', @campo_sur_id, 10.25, 'DISPONIBLE', NULL, NULL, NULL, 'Disponible', @empresa_id, @admin_user_id, 1, NOW()),
('Lote C1 - Girasol', @campo_este_id, 15.00, 'COSECHADO', 'GIRASOL', DATE_SUB(CURDATE(), INTERVAL 180 DAY), DATE_SUB(CURDATE(), INTERVAL 30 DAY), 'Girasol cosechado', @empresa_id, @admin_user_id, 1, NOW()),
('Lote C2 - Soja', @campo_este_id, 17.00, 'EN_CRECIMIENTO', 'SOJA', DATE_SUB(CURDATE(), INTERVAL 45 DAY), DATE_ADD(CURDATE(), INTERVAL 105 DAY), 'Soja segunda', @empresa_id, @admin_user_id, 1, NOW());

SELECT CONCAT('✓ Insertados ', ROW_COUNT(), ' lotes') AS resultado;

-- ============================================================================
-- INSERTAR CULTIVOS
-- ============================================================================

INSERT INTO cultivo_cultivos (nombre, tipo, variedad, ciclo_dias, rendimiento_esperado, unidad_rendimiento, precio_por_tonelada, descripcion, estado, activo, empresa_id, usuario_id, created_at)
VALUES
('Soja DM 53i51', 'SOJA', 'DM 53i51', 120, 45.50, 'qq/ha', 280.00, 'Soja resistente', 'ACTIVO', 1, @empresa_id, @admin_user_id, NOW()),
('Maíz DK 750', 'MAIZ', 'DK 750', 140, 120.00, 'qq/ha', 95.00, 'Maíz híbrido', 'ACTIVO', 1, @empresa_id, @admin_user_id, NOW()),
('Trigo Baguette 601', 'TRIGO', 'Baguette 601', 180, 65.00, 'qq/ha', 145.00, 'Trigo panadero', 'ACTIVO', 1, @empresa_id, @admin_user_id, NOW()),
('Girasol CF 101', 'GIRASOL', 'CF 101', 110, 28.00, 'qq/ha', 220.00, 'Girasol alto oleico', 'ACTIVO', 1, @empresa_id, @admin_user_id, NOW())
ON DUPLICATE KEY UPDATE nombre = nombre;

SELECT CONCAT('✓ Insertados/Actualizados ', ROW_COUNT(), ' cultivos') AS resultado;

-- ============================================================================
-- INSERTAR CATÁLOGOS PORCINOS
-- ============================================================================

INSERT INTO porcinos_razas_porcinos (nombre, descripcion, activo, empresa_id, fecha_creacion)
VALUES
('Yorkshire', 'Raza blanca, prolificidad', 1, @empresa_id, NOW()),
('Landrace', 'Raza blanca, excelente madre', 1, @empresa_id, NOW()),
('Duroc', 'Raza roja, buen peso', 1, @empresa_id, NOW()),
('Pietrain', 'Raza muy magra', 1, @empresa_id, NOW()),
('Hampshire', 'Raza negra con cinturón', 1, @empresa_id, NOW())
ON DUPLICATE KEY UPDATE nombre = nombre;

SET @raza_yorkshire_id = (SELECT id FROM porcinos_razas_porcinos WHERE nombre = 'Yorkshire' AND empresa_id = @empresa_id LIMIT 1);
SET @raza_landrace_id = (SELECT id FROM porcinos_razas_porcinos WHERE nombre = 'Landrace' AND empresa_id = @empresa_id LIMIT 1);
SET @raza_duroc_id = (SELECT id FROM porcinos_razas_porcinos WHERE nombre = 'Duroc' AND empresa_id = @empresa_id LIMIT 1);

INSERT INTO porcinos_tipos_parto (nombre, descripcion, requiere_intervencion, activo, empresa_id, fecha_creacion)
VALUES
('Normal', 'Parto sin complicaciones', 0, 1, @empresa_id, NOW()),
('Asistido', 'Parto con asistencia', 1, 1, @empresa_id, NOW()),
('Distocia', 'Requiere intervención', 1, 1, @empresa_id, NOW()),
('Cesárea', 'Parto por cesárea', 1, 1, @empresa_id, NOW())
ON DUPLICATE KEY UPDATE nombre = nombre;

SET @tipo_parto_normal_id = (SELECT id FROM porcinos_tipos_parto WHERE nombre = 'Normal' AND empresa_id = @empresa_id LIMIT 1);

INSERT INTO porcinos_ubicaciones_internas (nombre, codigo, nivel, tipo_ubicacion, capacidad_maxima, descripcion, activo, empresa_id, fecha_creacion)
VALUES
('Galpón Principal', 'GAL-01', 'GALPON', 'GENERAL', 200, 'Galpón principal', 1, @empresa_id, NOW()),
('Sala Maternidad', 'MAT-01', 'SALA', 'MATERNIDAD', 20, 'Sala de maternidad', 1, @empresa_id, NOW()),
('Sala Gestación', 'GES-01', 'SALA', 'GESTACION', 40, 'Sala de gestación', 1, @empresa_id, NOW())
ON DUPLICATE KEY UPDATE nombre = nombre;

SET @ubicacion_maternidad_id = (SELECT id FROM porcinos_ubicaciones_internas WHERE codigo = 'MAT-01' AND empresa_id = @empresa_id LIMIT 1);
SET @ubicacion_gestacion_id = (SELECT id FROM porcinos_ubicaciones_internas WHERE codigo = 'GES-01' AND empresa_id = @empresa_id LIMIT 1);

-- ============================================================================
-- INSERTAR PADRILLOS Y MADRES
-- ============================================================================

DELETE FROM porcinos_madres WHERE empresa_id = @empresa_id AND identificacion LIKE 'MAD-%';
DELETE FROM porcinos_padrillos WHERE empresa_id = @empresa_id AND identificacion LIKE 'PAD-%';

INSERT INTO porcinos_padrillos (identificacion, nombre, fecha_nacimiento, peso_actual, raza_id, activo, empresa_id, usuario_id, fecha_creacion)
VALUES
('PAD-001', 'Toro', DATE_SUB(CURDATE(), INTERVAL 730 DAY), 280.50, @raza_duroc_id, 1, @empresa_id, @admin_user_id, NOW()),
('PAD-002', 'Bravo', DATE_SUB(CURDATE(), INTERVAL 600 DAY), 265.00, @raza_yorkshire_id, 1, @empresa_id, @admin_user_id, NOW()),
('PAD-003', 'Fuerte', DATE_SUB(CURDATE(), INTERVAL 545 DAY), 275.75, @raza_duroc_id, 1, @empresa_id, @admin_user_id, NOW());

SET @padrillo_1_id = (SELECT id FROM porcinos_padrillos WHERE identificacion = 'PAD-001' AND empresa_id = @empresa_id LIMIT 1);
SET @padrillo_2_id = (SELECT id FROM porcinos_padrillos WHERE identificacion = 'PAD-002' AND empresa_id = @empresa_id LIMIT 1);

INSERT INTO porcinos_madres (identificacion, nombre, fecha_nacimiento, peso_actual, numero_partos, raza_id, ubicacion_interna_id, activo, empresa_id, usuario_id, fecha_creacion)
VALUES
('MAD-001', 'Blanca', DATE_SUB(CURDATE(), INTERVAL 1095 DAY), 180.50, 4, @raza_yorkshire_id, @ubicacion_maternidad_id, 1, @empresa_id, @admin_user_id, NOW()),
('MAD-002', 'Linda', DATE_SUB(CURDATE(), INTERVAL 910 DAY), 175.00, 3, @raza_landrace_id, @ubicacion_maternidad_id, 1, @empresa_id, @admin_user_id, NOW()),
('MAD-003', 'Bella', DATE_SUB(CURDATE(), INTERVAL 1200 DAY), 185.25, 5, @raza_yorkshire_id, @ubicacion_gestacion_id, 1, @empresa_id, @admin_user_id, NOW()),
('MAD-004', 'Rosa', DATE_SUB(CURDATE(), INTERVAL 800 DAY), 170.50, 2, @raza_landrace_id, @ubicacion_gestacion_id, 1, @empresa_id, @admin_user_id, NOW()),
('MAD-005', 'Dulce', DATE_SUB(CURDATE(), INTERVAL 950 DAY), 178.00, 3, @raza_yorkshire_id, @ubicacion_gestacion_id, 1, @empresa_id, @admin_user_id, NOW()),
('MAD-006', 'Luna', DATE_SUB(CURDATE(), INTERVAL 730 DAY), 165.75, 1, @raza_landrace_id, @ubicacion_gestacion_id, 1, @empresa_id, @admin_user_id, NOW()),
('MAD-007', 'Estrella', DATE_SUB(CURDATE(), INTERVAL 1100 DAY), 182.50, 4, @raza_yorkshire_id, @ubicacion_gestacion_id, 1, @empresa_id, @admin_user_id, NOW()),
('MAD-008', 'Sol', DATE_SUB(CURDATE(), INTERVAL 850 DAY), 177.25, 2, @raza_landrace_id, @ubicacion_gestacion_id, 1, @empresa_id, @admin_user_id, NOW());

SET @madre_1_id = (SELECT id FROM porcinos_madres WHERE identificacion = 'MAD-001' AND empresa_id = @empresa_id LIMIT 1);
SET @madre_2_id = (SELECT id FROM porcinos_madres WHERE identificacion = 'MAD-002' AND empresa_id = @empresa_id LIMIT 1);
SET @madre_3_id = (SELECT id FROM porcinos_madres WHERE identificacion = 'MAD-003' AND empresa_id = @empresa_id LIMIT 1);
SET @madre_4_id = (SELECT id FROM porcinos_madres WHERE identificacion = 'MAD-004' AND empresa_id = @empresa_id LIMIT 1);

SELECT CONCAT('✓ Insertados ', (SELECT COUNT(*) FROM porcinos_padrillos WHERE empresa_id = @empresa_id), ' padrillos') AS resultado;
SELECT CONCAT('✓ Insertadas ', (SELECT COUNT(*) FROM porcinos_madres WHERE empresa_id = @empresa_id), ' madres') AS resultado;

-- ============================================================================
-- INSERTAR SERVICIOS
-- ============================================================================

DELETE FROM porcinos_servicios WHERE empresa_id = @empresa_id;

INSERT INTO porcinos_servicios (madre_id, macho_id, tipo, fecha_servicio, fecha_control_celo, estado_servicio, numero_intento, observaciones, activo, empresa_id, usuario_id, fecha_creacion)
VALUES
(@madre_1_id, @padrillo_1_id, 'IA', DATE_SUB(CURDATE(), INTERVAL 120 DAY), DATE_SUB(CURDATE(), INTERVAL 118 DAY), 'PREÑEZ_CONFIRMADA', 1, 'Servicio exitoso', 1, @empresa_id, @admin_user_id, NOW()),
(@madre_2_id, @padrillo_2_id, 'MONTA_NATURAL', DATE_SUB(CURDATE(), INTERVAL 110 DAY), DATE_SUB(CURDATE(), INTERVAL 108 DAY), 'PREÑEZ_CONFIRMADA', 1, 'Monta natural exitosa', 1, @empresa_id, @admin_user_id, NOW()),
(@madre_3_id, @padrillo_1_id, 'IA', DATE_SUB(CURDATE(), INTERVAL 100 DAY), DATE_SUB(CURDATE(), INTERVAL 98 DAY), 'PREÑEZ_CONFIRMADA', 1, 'IA exitosa', 1, @empresa_id, @admin_user_id, NOW()),
(@madre_4_id, @padrillo_2_id, 'IA', DATE_SUB(CURDATE(), INTERVAL 90 DAY), DATE_SUB(CURDATE(), INTERVAL 88 DAY), 'PREÑEZ_CONFIRMADA', 1, NULL, 1, @empresa_id, @admin_user_id, NOW());

SET @servicio_1_id = (SELECT id FROM porcinos_servicios WHERE madre_id = @madre_1_id ORDER BY fecha_servicio DESC LIMIT 1);
SET @servicio_2_id = (SELECT id FROM porcinos_servicios WHERE madre_id = @madre_2_id ORDER BY fecha_servicio DESC LIMIT 1);

SELECT CONCAT('✓ Insertados ', ROW_COUNT(), ' servicios') AS resultado;

-- ============================================================================
-- INSERTAR GESTACIONES
-- ============================================================================

DELETE FROM porcinos_chequeos_gestacion WHERE empresa_id = @empresa_id;
DELETE FROM porcinos_gestacion WHERE empresa_id = @empresa_id;

INSERT INTO porcinos_gestacion (servicio_id, madre_id, fecha_inicio, fecha_probable_parto, estado, activo, empresa_id, usuario_id, fecha_creacion)
VALUES
(@servicio_1_id, @madre_1_id, DATE_SUB(CURDATE(), INTERVAL 120 DAY), DATE_ADD(CURDATE(), INTERVAL 14 DAY), 'EN_CURSO', 1, @empresa_id, @admin_user_id, NOW()),
(@servicio_2_id, @madre_2_id, DATE_SUB(CURDATE(), INTERVAL 110 DAY), DATE_ADD(CURDATE(), INTERVAL 24 DAY), 'EN_CURSO', 1, @empresa_id, @admin_user_id, NOW());

SET @gestacion_1_id = (SELECT id FROM porcinos_gestacion WHERE servicio_id = @servicio_1_id AND empresa_id = @empresa_id LIMIT 1);

INSERT INTO porcinos_chequeos_gestacion (gestacion_id, fecha, metodo, resultado, observaciones, activo, empresa_id, usuario_id, fecha_creacion)
VALUES
(@gestacion_1_id, DATE_SUB(CURDATE(), INTERVAL 90 DAY), 'ECO', 'POSITIVO', 'Gestación confirmada por ecografía', 1, @empresa_id, @admin_user_id, NOW());

SELECT CONCAT('✓ Insertadas ', (SELECT COUNT(*) FROM porcinos_gestacion WHERE empresa_id = @empresa_id), ' gestaciones') AS resultado;

-- ============================================================================
-- INSERTAR PARTOS Y DESTETES
-- ============================================================================

DELETE FROM porcinos_destetes WHERE empresa_id = @empresa_id;
DELETE FROM porcinos_partos WHERE empresa_id = @empresa_id;

INSERT INTO porcinos_partos (madre_id, fecha_inicio, fecha_fin, nacidos_vivos, nacidos_muertos, momias, total_nacidos, peso_promedio_nacimiento, tipo_parto_id, intervenciones, observaciones, activo, empresa_id, usuario_id, fecha_creacion)
VALUES
(@madre_1_id, CONCAT(DATE_SUB(CURDATE(), INTERVAL 180 DAY), ' 08:00:00'), CONCAT(DATE_SUB(CURDATE(), INTERVAL 180 DAY), ' 10:30:00'), 12, 1, 0, 13, 1.45, @tipo_parto_normal_id, NULL, 'Parto normal sin complicaciones', 1, @empresa_id, @admin_user_id, NOW()),
(@madre_2_id, CONCAT(DATE_SUB(CURDATE(), INTERVAL 200 DAY), ' 07:00:00'), CONCAT(DATE_SUB(CURDATE(), INTERVAL 200 DAY), ' 09:15:00'), 10, 2, 1, 13, 1.38, @tipo_parto_normal_id, NULL, 'Parto normal', 1, @empresa_id, @admin_user_id, NOW()),
(@madre_3_id, CONCAT(DATE_SUB(CURDATE(), INTERVAL 160 DAY), ' 09:00:00'), CONCAT(DATE_SUB(CURDATE(), INTERVAL 160 DAY), ' 11:00:00'), 14, 0, 0, 14, 1.52, @tipo_parto_normal_id, NULL, 'Excelente parto', 1, @empresa_id, @admin_user_id, NOW());

SET @parto_1_id = (SELECT id FROM porcinos_partos WHERE madre_id = @madre_1_id ORDER BY fecha_inicio DESC LIMIT 1);

INSERT INTO porcinos_destetes (parto_id, fecha_destete, lechones_destetados, peso_promedio_destete, peso_total, observaciones, activo, empresa_id, usuario_id, fecha_creacion)
VALUES
(@parto_1_id, DATE_SUB(CURDATE(), INTERVAL 125 DAY), 11, 6.50, 71.50, 'Destete exitoso, todos los lechones en buen estado', 1, @empresa_id, @admin_user_id, NOW());

SELECT CONCAT('✓ Insertados ', (SELECT COUNT(*) FROM porcinos_partos WHERE empresa_id = @empresa_id), ' partos') AS resultado;

-- ============================================================================
-- INSERTAR RECRÍAS
-- ============================================================================

DELETE FROM porcinos_recria WHERE empresa_id = @empresa_id;

SET @lote_recria_id = (SELECT id FROM cultivo_lotes WHERE empresa_id = @empresa_id LIMIT 1);

INSERT INTO porcinos_recria (lote_id, cantidad_animales, fecha_ingreso, peso_promedio, sexo, destino, activo, empresa_id, usuario_id, fecha_creacion)
VALUES
(@lote_recria_id, 11, DATE_SUB(CURDATE(), INTERVAL 125 DAY), 6.50, 'HEMBRA', 'FUTURA_MADRE', 1, @empresa_id, @admin_user_id, NOW()),
(@lote_recria_id, 30, DATE_SUB(CURDATE(), INTERVAL 200 DAY), 25.00, 'MACHO', 'ENGORDE', 1, @empresa_id, @admin_user_id, NOW());

SELECT CONCAT('✓ Insertadas ', ROW_COUNT(), ' recrías') AS resultado;

-- ============================================================================
-- INSERTAR PARÁMETROS PRODUCTIVOS
-- ============================================================================

INSERT INTO porcinos_parametros_productivos_porcinos (empresa_id, dias_promedio_gestacion, dias_lactancia, dias_recria_antes_engorde, dias_engorde, dias_control_celo, dias_entre_celos, dias_pasaje_maternidad, peso_promedio_nacimiento, peso_destete_objetivo, peso_venta_objetivo, lechones_vivos_parto_objetivo, lechones_destetados_objetivo, partos_madre_anio_objetivo, fecha_creacion)
VALUES
(@empresa_id, 114, 21, 60, 120, 5, 21, 7, 1.40, 6.50, 105.00, 12.50, 11.50, 2.35, NOW())
ON DUPLICATE KEY UPDATE empresa_id = empresa_id;

-- ============================================================================
-- RESUMEN FINAL
-- ============================================================================

SELECT '' AS '';
SELECT '════════════════════════════════════════════════' AS '';
SELECT 'RESUMEN DE DATOS INSERTADOS' AS '';
SELECT '════════════════════════════════════════════════' AS '';
SELECT '' AS '';

SELECT 
    'CAMPOS' AS categoria,
    COUNT(*) AS cantidad
FROM cultivo_campos WHERE empresa_id = @empresa_id

UNION ALL

SELECT 'LOTES', COUNT(*) FROM cultivo_lotes WHERE empresa_id = @empresa_id
UNION ALL
SELECT 'CULTIVOS', COUNT(*) FROM cultivo_cultivos WHERE empresa_id = @empresa_id
UNION ALL
SELECT 'RAZAS', COUNT(*) FROM porcinos_razas_porcinos WHERE empresa_id = @empresa_id AND activo = 1
UNION ALL
SELECT 'MADRES', COUNT(*) FROM porcinos_madres WHERE empresa_id = @empresa_id AND activo = 1
UNION ALL
SELECT 'PADRILLOS', COUNT(*) FROM porcinos_padrillos WHERE empresa_id = @empresa_id AND activo = 1
UNION ALL
SELECT 'SERVICIOS', COUNT(*) FROM porcinos_servicios WHERE empresa_id = @empresa_id AND activo = 1
UNION ALL
SELECT 'GESTACIONES', COUNT(*) FROM porcinos_gestacion WHERE empresa_id = @empresa_id AND activo = 1
UNION ALL
SELECT 'PARTOS', COUNT(*) FROM porcinos_partos WHERE empresa_id = @empresa_id AND activo = 1
UNION ALL
SELECT 'DESTETES', COUNT(*) FROM porcinos_destetes WHERE empresa_id = @empresa_id AND activo = 1
UNION ALL
SELECT 'RECRÍAS', COUNT(*) FROM porcinos_recria WHERE empresa_id = @empresa_id AND activo = 1;

SELECT '' AS '';
SELECT '════════════════════════════════════════════════' AS '';
SELECT '✓ PROCESO COMPLETADO EXITOSAMENTE' AS '';
SELECT '════════════════════════════════════════════════' AS '';















