-- ============================================================================
-- SCRIPT PARA INSERTAR DATOS DE PRUEBA DEL MÓDULO PORCINOS
-- Usa los datos existentes de empresa y usuarios
-- Solo inserta datos relacionados con el módulo de porcinos
-- ============================================================================

USE agrocloud;

-- Obtener IDs de empresa y usuario existentes
SET @empresa_id = (SELECT id FROM empresas WHERE nombre LIKE '%AgroCloud%' AND activo = 1 LIMIT 1);
SET @empresa_id = IFNULL(@empresa_id, (SELECT id FROM empresas WHERE activo = 1 LIMIT 1));

SET @admin_user_id = (SELECT id FROM usuarios WHERE email = 'admin@agrocloud.com' AND activo = 1 LIMIT 1);
SET @admin_user_id = IFNULL(@admin_user_id, (SELECT id FROM usuarios WHERE activo = 1 LIMIT 1));

-- Verificar que tenemos empresa y usuario
SELECT CONCAT('✓ Empresa ID: ', COALESCE(@empresa_id, 'NO ENCONTRADA')) AS verificacion;
SELECT CONCAT('✓ Usuario ID: ', COALESCE(@admin_user_id, 'NO ENCONTRADO')) AS verificacion;

-- ============================================================================
-- INSERTAR CATÁLOGOS PORCINOS
-- ============================================================================

-- Razas de porcinos
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

SELECT CONCAT('✓ Razas insertadas/actualizadas: ', (SELECT COUNT(*) FROM porcinos_razas_porcinos WHERE empresa_id = @empresa_id AND activo = 1)) AS resultado;

-- Tipos de parto (si no existen, se insertan)
INSERT INTO porcinos_tipos_parto (nombre, descripcion, requiere_intervencion, activo, empresa_id, fecha_creacion)
VALUES
('Normal', 'Parto sin complicaciones', 0, 1, @empresa_id, NOW()),
('Asistido', 'Parto con asistencia', 1, 1, @empresa_id, NOW()),
('Distocia', 'Requiere intervención', 1, 1, @empresa_id, NOW()),
('Cesárea', 'Parto por cesárea', 1, 1, @empresa_id, NOW())
ON DUPLICATE KEY UPDATE nombre = nombre;

SET @tipo_parto_normal_id = (SELECT id FROM porcinos_tipos_parto WHERE nombre = 'Normal' AND empresa_id = @empresa_id LIMIT 1);

SELECT CONCAT('✓ Tipos de parto insertados/actualizados: ', (SELECT COUNT(*) FROM porcinos_tipos_parto WHERE empresa_id = @empresa_id AND activo = 1)) AS resultado;

-- Ubicaciones internas (si no existen, se insertan)
INSERT INTO porcinos_ubicaciones_internas (nombre, codigo, nivel, tipo_ubicacion, capacidad_maxima, descripcion, activo, empresa_id, fecha_creacion)
VALUES
('Galpón Principal', 'GAL-01', 'GALPON', 'GENERAL', 200, 'Galpón principal', 1, @empresa_id, NOW()),
('Sala Maternidad', 'MAT-01', 'SALA', 'MATERNIDAD', 20, 'Sala de maternidad', 1, @empresa_id, NOW()),
('Sala Gestación', 'GES-01', 'SALA', 'GESTACION', 40, 'Sala de gestación', 1, @empresa_id, NOW())
ON DUPLICATE KEY UPDATE nombre = nombre;

-- Establecer jerarquía (salas dentro del galpón)
SET @galpon_id = (SELECT id FROM porcinos_ubicaciones_internas WHERE codigo = 'GAL-01' AND empresa_id = @empresa_id LIMIT 1);
SET @ubicacion_maternidad_id = (SELECT id FROM porcinos_ubicaciones_internas WHERE codigo = 'MAT-01' AND empresa_id = @empresa_id LIMIT 1);
SET @ubicacion_gestacion_id = (SELECT id FROM porcinos_ubicaciones_internas WHERE codigo = 'GES-01' AND empresa_id = @empresa_id LIMIT 1);

UPDATE porcinos_ubicaciones_internas 
SET ubicacion_padre_id = @galpon_id 
WHERE id IN (@ubicacion_maternidad_id, @ubicacion_gestacion_id) AND empresa_id = @empresa_id;

SELECT CONCAT('✓ Ubicaciones internas insertadas/actualizadas: ', (SELECT COUNT(*) FROM porcinos_ubicaciones_internas WHERE empresa_id = @empresa_id AND activo = 1)) AS resultado;

-- ============================================================================
-- INSERTAR PADRILLOS Y MADRES
-- ============================================================================

-- Limpiar datos de prueba anteriores (opcional - comentar si no se desea limpiar)
DELETE FROM porcinos_madres WHERE empresa_id = @empresa_id AND identificacion LIKE 'MAD-%';
DELETE FROM porcinos_padrillos WHERE empresa_id = @empresa_id AND identificacion LIKE 'PAD-%';

-- Insertar padrillos (ahora con ubicación)
INSERT INTO porcinos_padrillos (identificacion, fecha_nacimiento, origen, fecha_ingreso_granja, raza_id, ubicacion_interna_id, activo, empresa_id, usuario_id, fecha_creacion)
VALUES
('PAD-001', DATE_SUB(CURDATE(), INTERVAL 730 DAY), 'EXTERNA', DATE_SUB(CURDATE(), INTERVAL 730 DAY), @raza_duroc_id, @galpon_id, 1, @empresa_id, @admin_user_id, NOW()),
('PAD-002', DATE_SUB(CURDATE(), INTERVAL 600 DAY), 'EXTERNA', DATE_SUB(CURDATE(), INTERVAL 600 DAY), @raza_yorkshire_id, @galpon_id, 1, @empresa_id, @admin_user_id, NOW()),
('PAD-003', DATE_SUB(CURDATE(), INTERVAL 545 DAY), 'EXTERNA', DATE_SUB(CURDATE(), INTERVAL 545 DAY), @raza_duroc_id, @galpon_id, 1, @empresa_id, @admin_user_id, NOW());

SET @padrillo_1_id = (SELECT id FROM porcinos_padrillos WHERE identificacion = 'PAD-001' AND empresa_id = @empresa_id LIMIT 1);
SET @padrillo_2_id = (SELECT id FROM porcinos_padrillos WHERE identificacion = 'PAD-002' AND empresa_id = @empresa_id LIMIT 1);

SELECT CONCAT('✓ Padrillos insertados: ', (SELECT COUNT(*) FROM porcinos_padrillos WHERE empresa_id = @empresa_id AND activo = 1)) AS resultado;

-- Insertar madres (ahora con ubicación)
INSERT INTO porcinos_madres (identificacion, fecha_nacimiento, cantidad_tetas, numero_partos, raza_id, origen, estado_actual, fecha_ingreso_granja, ubicacion_interna_id, activo, empresa_id, usuario_id, fecha_creacion)
VALUES
('MAD-001', DATE_SUB(CURDATE(), INTERVAL 1095 DAY), 14, 4, @raza_yorkshire_id, 'EXTERNA', 'ADULTA', DATE_SUB(CURDATE(), INTERVAL 1095 DAY), @ubicacion_maternidad_id, 1, @empresa_id, @admin_user_id, NOW()),
('MAD-002', DATE_SUB(CURDATE(), INTERVAL 910 DAY), 14, 3, @raza_landrace_id, 'EXTERNA', 'ADULTA', DATE_SUB(CURDATE(), INTERVAL 910 DAY), @ubicacion_maternidad_id, 1, @empresa_id, @admin_user_id, NOW()),
('MAD-003', DATE_SUB(CURDATE(), INTERVAL 1200 DAY), 14, 5, @raza_yorkshire_id, 'EXTERNA', 'GESTACION', DATE_SUB(CURDATE(), INTERVAL 1200 DAY), @ubicacion_gestacion_id, 1, @empresa_id, @admin_user_id, NOW()),
('MAD-004', DATE_SUB(CURDATE(), INTERVAL 800 DAY), 14, 2, @raza_landrace_id, 'EXTERNA', 'GESTACION', DATE_SUB(CURDATE(), INTERVAL 800 DAY), @ubicacion_gestacion_id, 1, @empresa_id, @admin_user_id, NOW()),
('MAD-005', DATE_SUB(CURDATE(), INTERVAL 950 DAY), 14, 3, @raza_yorkshire_id, 'EXTERNA', 'GESTACION', DATE_SUB(CURDATE(), INTERVAL 950 DAY), @ubicacion_gestacion_id, 1, @empresa_id, @admin_user_id, NOW()),
('MAD-006', DATE_SUB(CURDATE(), INTERVAL 730 DAY), 14, 1, @raza_landrace_id, 'EXTERNA', 'GESTACION', DATE_SUB(CURDATE(), INTERVAL 730 DAY), @ubicacion_gestacion_id, 1, @empresa_id, @admin_user_id, NOW()),
('MAD-007', DATE_SUB(CURDATE(), INTERVAL 1100 DAY), 14, 4, @raza_yorkshire_id, 'EXTERNA', 'GESTACION', DATE_SUB(CURDATE(), INTERVAL 1100 DAY), @ubicacion_gestacion_id, 1, @empresa_id, @admin_user_id, NOW()),
('MAD-008', DATE_SUB(CURDATE(), INTERVAL 850 DAY), 14, 2, @raza_landrace_id, 'EXTERNA', 'GESTACION', DATE_SUB(CURDATE(), INTERVAL 850 DAY), @ubicacion_gestacion_id, 1, @empresa_id, @admin_user_id, NOW());

SET @madre_1_id = (SELECT id FROM porcinos_madres WHERE identificacion = 'MAD-001' AND empresa_id = @empresa_id LIMIT 1);
SET @madre_2_id = (SELECT id FROM porcinos_madres WHERE identificacion = 'MAD-002' AND empresa_id = @empresa_id LIMIT 1);
SET @madre_3_id = (SELECT id FROM porcinos_madres WHERE identificacion = 'MAD-003' AND empresa_id = @empresa_id LIMIT 1);
SET @madre_4_id = (SELECT id FROM porcinos_madres WHERE identificacion = 'MAD-004' AND empresa_id = @empresa_id LIMIT 1);

SELECT CONCAT('✓ Madres insertadas: ', (SELECT COUNT(*) FROM porcinos_madres WHERE empresa_id = @empresa_id AND activo = 1)) AS resultado;

-- ============================================================================
-- INSERTAR SERVICIOS
-- ============================================================================

-- Limpiar servicios anteriores (opcional)
DELETE FROM porcinos_servicios WHERE empresa_id = @empresa_id;

-- Temporalmente deshabilitar constraint para insertar con Ñ
SET @constraint_name = 'chk_estado_servicio';
SET @sql = CONCAT('ALTER TABLE porcinos_servicios DROP CONSTRAINT ', @constraint_name);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Insertar servicios (usando variable para evitar problemas de codificación)
SET @estado_prenez = CONCAT('PENDIENTE_', CHAR(67), 'ONTROL'); -- Workaround temporal
SET @estado_prenez = 'PENDIENTE_CONTROL'; -- Usar PENDIENTE_CONTROL por ahora

INSERT INTO porcinos_servicios (madre_id, macho_id, tipo, fecha_servicio, fecha_control_celo, estado_servicio, numero_intento, observaciones, activo, empresa_id, usuario_id, fecha_creacion)
VALUES
(@madre_1_id, @padrillo_1_id, 'IA', DATE_SUB(CURDATE(), INTERVAL 120 DAY), DATE_SUB(CURDATE(), INTERVAL 118 DAY), @estado_prenez, 1, 'Servicio exitoso', 1, @empresa_id, @admin_user_id, NOW()),
(@madre_2_id, @padrillo_2_id, 'MONTA_NATURAL', DATE_SUB(CURDATE(), INTERVAL 110 DAY), DATE_SUB(CURDATE(), INTERVAL 108 DAY), @estado_prenez, 1, 'Monta natural exitosa', 1, @empresa_id, @admin_user_id, NOW()),
(@madre_3_id, @padrillo_1_id, 'IA', DATE_SUB(CURDATE(), INTERVAL 100 DAY), DATE_SUB(CURDATE(), INTERVAL 98 DAY), @estado_prenez, 1, 'IA exitosa', 1, @empresa_id, @admin_user_id, NOW()),
(@madre_4_id, @padrillo_2_id, 'IA', DATE_SUB(CURDATE(), INTERVAL 90 DAY), DATE_SUB(CURDATE(), INTERVAL 88 DAY), @estado_prenez, 1, NULL, 1, @empresa_id, @admin_user_id, NOW());

-- Recrear constraint
SET @sql = CONCAT('ALTER TABLE porcinos_servicios ADD CONSTRAINT ', @constraint_name, ' CHECK (estado_servicio IN (''PENDIENTE_CONTROL'', ''FALLIDO'', ''PREÑEZ_CONFIRMADA''))');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @servicio_1_id = (SELECT id FROM porcinos_servicios WHERE madre_id = @madre_1_id ORDER BY fecha_servicio DESC LIMIT 1);
SET @servicio_2_id = (SELECT id FROM porcinos_servicios WHERE madre_id = @madre_2_id ORDER BY fecha_servicio DESC LIMIT 1);

SELECT CONCAT('✓ Servicios insertados: ', (SELECT COUNT(*) FROM porcinos_servicios WHERE empresa_id = @empresa_id AND activo = 1)) AS resultado;

-- ============================================================================
-- INSERTAR GESTACIONES
-- ============================================================================

-- Limpiar gestaciones anteriores (opcional)
DELETE FROM porcinos_chequeos_gestacion WHERE empresa_id = @empresa_id;
DELETE FROM porcinos_gestacion WHERE empresa_id = @empresa_id;

INSERT INTO porcinos_gestacion (servicio_id, madre_id, fecha_inicio, fecha_probable_parto, estado, activo, empresa_id, usuario_id, fecha_creacion)
VALUES
(@servicio_1_id, @madre_1_id, DATE_SUB(CURDATE(), INTERVAL 120 DAY), DATE_ADD(CURDATE(), INTERVAL 14 DAY), 'EN_CURSO', 1, @empresa_id, @admin_user_id, NOW()),
(@servicio_2_id, @madre_2_id, DATE_SUB(CURDATE(), INTERVAL 110 DAY), DATE_ADD(CURDATE(), INTERVAL 24 DAY), 'EN_CURSO', 1, @empresa_id, @admin_user_id, NOW());

SET @gestacion_1_id = (SELECT id FROM porcinos_gestacion WHERE servicio_id = @servicio_1_id AND empresa_id = @empresa_id LIMIT 1);

-- Insertar chequeos de gestación
INSERT INTO porcinos_chequeos_gestacion (gestacion_id, fecha, metodo, resultado, observaciones, activo, empresa_id, usuario_id, fecha_creacion)
VALUES
(@gestacion_1_id, DATE_SUB(CURDATE(), INTERVAL 90 DAY), 'ECO', 'POSITIVO', 'Gestación confirmada por ecografía', 1, @empresa_id, @admin_user_id, NOW());

SELECT CONCAT('✓ Gestaciones insertadas: ', (SELECT COUNT(*) FROM porcinos_gestacion WHERE empresa_id = @empresa_id AND activo = 1)) AS resultado;

-- ============================================================================
-- INSERTAR PARTOS Y DESTETES
-- ============================================================================

-- Limpiar partos y destetes anteriores (opcional)
DELETE FROM porcinos_destetes WHERE empresa_id = @empresa_id;
DELETE FROM porcinos_partos WHERE empresa_id = @empresa_id;

-- Ahora se puede usar tipo_parto_id (tabla creada)
INSERT INTO porcinos_partos (madre_id, fecha_inicio, fecha_fin, nacidos_vivos, nacidos_muertos, momias, total_nacidos, peso_promedio_nacimiento, tipo_parto_id, intervenciones, observaciones, activo, empresa_id, usuario_id, fecha_creacion)
VALUES
(@madre_1_id, CONCAT(DATE_SUB(CURDATE(), INTERVAL 180 DAY), ' 08:00:00'), CONCAT(DATE_SUB(CURDATE(), INTERVAL 180 DAY), ' 10:30:00'), 12, 1, 0, 13, 1.45, @tipo_parto_normal_id, NULL, 'Parto normal sin complicaciones', 1, @empresa_id, @admin_user_id, NOW()),
(@madre_2_id, CONCAT(DATE_SUB(CURDATE(), INTERVAL 200 DAY), ' 07:00:00'), CONCAT(DATE_SUB(CURDATE(), INTERVAL 200 DAY), ' 09:15:00'), 10, 2, 1, 13, 1.38, @tipo_parto_normal_id, NULL, 'Parto normal', 1, @empresa_id, @admin_user_id, NOW()),
(@madre_3_id, CONCAT(DATE_SUB(CURDATE(), INTERVAL 160 DAY), ' 09:00:00'), CONCAT(DATE_SUB(CURDATE(), INTERVAL 160 DAY), ' 11:00:00'), 14, 0, 0, 14, 1.52, @tipo_parto_normal_id, NULL, 'Excelente parto', 1, @empresa_id, @admin_user_id, NOW());

SET @parto_1_id = (SELECT id FROM porcinos_partos WHERE madre_id = @madre_1_id ORDER BY fecha_inicio DESC LIMIT 1);

-- Insertar destetes
INSERT INTO porcinos_destetes (parto_id, fecha_destete, cantidad_destetados, peso_promedio_destete, dias_lactancia, observaciones, activo, empresa_id, usuario_id, fecha_creacion)
VALUES
(@parto_1_id, DATE_SUB(CURDATE(), INTERVAL 125 DAY), 11, 6.50, 21, 'Destete exitoso, todos los lechones en buen estado', 1, @empresa_id, @admin_user_id, NOW());

SELECT CONCAT('✓ Partos insertados: ', (SELECT COUNT(*) FROM porcinos_partos WHERE empresa_id = @empresa_id AND activo = 1)) AS resultado;
SELECT CONCAT('✓ Destetes insertados: ', (SELECT COUNT(*) FROM porcinos_destetes WHERE empresa_id = @empresa_id AND activo = 1)) AS resultado;

-- ============================================================================
-- INSERTAR RECRÍAS
-- ============================================================================

-- Limpiar recrías anteriores (opcional)
DELETE FROM porcinos_recria WHERE empresa_id = @empresa_id;

-- Obtener un lote existente para la recría (usa el primer lote disponible)
-- Nota: cultivo_lotes no tiene empresa_id directamente, se relaciona a través de campo_id
SET @lote_recria_id = (SELECT id FROM cultivo_lotes LIMIT 1);

-- Si no hay lotes, usar NULL (dependiendo de la estructura de la tabla)
INSERT INTO porcinos_recria (lote_id, cantidad_animales, fecha_ingreso, peso_promedio, sexo, destino, activo, empresa_id, usuario_id, fecha_creacion)
VALUES
(@lote_recria_id, 11, DATE_SUB(CURDATE(), INTERVAL 125 DAY), 6.50, 'HEMBRA', 'FUTURA_MADRE', 1, @empresa_id, @admin_user_id, NOW()),
(@lote_recria_id, 30, DATE_SUB(CURDATE(), INTERVAL 200 DAY), 25.00, 'MACHO', 'ENGORDE', 1, @empresa_id, @admin_user_id, NOW());

SELECT CONCAT('✓ Recrías insertadas: ', (SELECT COUNT(*) FROM porcinos_recria WHERE empresa_id = @empresa_id AND activo = 1)) AS resultado;

-- ============================================================================
-- INSERTAR PARÁMETROS PRODUCTIVOS
-- ============================================================================

INSERT INTO porcinos_parametros_productivos_porcinos (empresa_id, dias_promedio_gestacion, dias_lactancia, dias_recria_antes_engorde, dias_engorde, dias_control_celo, dias_entre_celos, dias_pasaje_maternidad, peso_promedio_nacimiento, peso_destete_objetivo, peso_venta_objetivo, lechones_vivos_parto_objetivo, lechones_destetados_objetivo, partos_madre_anio_objetivo, fecha_creacion)
VALUES
(@empresa_id, 114, 21, 60, 120, 5, 21, 7, 1.40, 6.50, 105.00, 12.50, 11.50, 2.35, NOW())
ON DUPLICATE KEY UPDATE empresa_id = empresa_id;

SELECT CONCAT('✓ Parámetros productivos insertados/actualizados') AS resultado;

-- ============================================================================
-- RESUMEN FINAL
-- ============================================================================

SELECT '' AS '';
SELECT '════════════════════════════════════════════════' AS '';
SELECT 'RESUMEN DE DATOS PORCINOS INSERTADOS' AS '';
SELECT '════════════════════════════════════════════════' AS '';
SELECT '' AS '';

SELECT 
    'RAZAS' AS categoria,
    COUNT(*) AS cantidad
FROM porcinos_razas_porcinos WHERE empresa_id = @empresa_id AND activo = 1

UNION ALL

SELECT 'TIPOS PARTO', COUNT(*) FROM porcinos_tipos_parto WHERE empresa_id = @empresa_id AND activo = 1
UNION ALL
SELECT 'UBICACIONES', COUNT(*) FROM porcinos_ubicaciones_internas WHERE empresa_id = @empresa_id AND activo = 1
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

