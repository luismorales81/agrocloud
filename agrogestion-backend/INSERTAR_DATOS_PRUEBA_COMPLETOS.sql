-- ============================================================================
-- SCRIPT PARA INSERTAR DATOS DE PRUEBA COMPLETOS
-- Usa los datos existentes de empresa y usuarios de AgroCloud
-- ============================================================================
-- Ejecutar: mysql -u root -p123456 agrocloud < INSERTAR_DATOS_PRUEBA_COMPLETOS.sql
-- ============================================================================

USE agrocloud;

-- ============================================================================
-- OBTENER IDs EXISTENTES
-- ============================================================================

SET @empresa_id = (SELECT id FROM empresas WHERE activo = 1 LIMIT 1);
SET @admin_user_id = (SELECT id FROM usuarios WHERE email = 'admin@agrocloud.com' AND activo = 1 LIMIT 1);
SET @tecnico_user_id = (SELECT id FROM usuarios WHERE email = 'tecnico@agrocloud.com' AND activo = 1 LIMIT 1);
SET @productor_user_id = (SELECT id FROM usuarios WHERE email = 'productor@agrocloud.com' AND activo = 1 LIMIT 1);

-- Si no existen usuarios específicos, usar el primero disponible
SET @admin_user_id = IFNULL(@admin_user_id, (SELECT id FROM usuarios WHERE activo = 1 LIMIT 1));
SET @tecnico_user_id = IFNULL(@tecnico_user_id, @admin_user_id);
SET @productor_user_id = IFNULL(@productor_user_id, @admin_user_id);

SELECT CONCAT('Usando Empresa ID: ', @empresa_id) AS info;
SELECT CONCAT('Usando Usuario Admin ID: ', @admin_user_id) AS info;
SELECT CONCAT('Usando Usuario Técnico ID: ', @tecnico_user_id) AS info;

-- ============================================================================
-- 1. MÓDULO CULTIVOS - CAMPOS Y LOTES
-- ============================================================================

-- Insertar Campos (si no existen) - Tabla: cultivo_campos
INSERT IGNORE INTO cultivo_campos (nombre, ubicacion, area_hectareas, descripcion, estado, activo, empresa_id, user_id, fecha_creacion)
VALUES
('Campo Norte', 'Ruta 8, Km 45, Buenos Aires', 25.50, 'Campo principal para cultivo de soja', 'ACTIVO', 1, @empresa_id, @admin_user_id, NOW()),
('Campo Sur', 'Ruta 9, Km 32, Córdoba', 18.75, 'Campo para rotación de cultivos', 'ACTIVO', 1, @empresa_id, @admin_user_id, NOW()),
('Campo Este', 'Ruta 12, Km 78, Entre Ríos', 32.00, 'Campo con riego por aspersión', 'ACTIVO', 1, @empresa_id, @admin_user_id, NOW()),
('Campo Oeste', 'Ruta 3, Km 120, La Pampa', 15.25, 'Campo para girasol', 'ACTIVO', 1, @empresa_id, @admin_user_id, NOW());

SET @campo_norte_id = (SELECT id FROM cultivo_campos WHERE nombre = 'Campo Norte' AND empresa_id = @empresa_id LIMIT 1);
SET @campo_sur_id = (SELECT id FROM cultivo_campos WHERE nombre = 'Campo Sur' AND empresa_id = @empresa_id LIMIT 1);
SET @campo_este_id = (SELECT id FROM cultivo_campos WHERE nombre = 'Campo Este' AND empresa_id = @empresa_id LIMIT 1);
SET @campo_oeste_id = (SELECT id FROM cultivo_campos WHERE nombre = 'Campo Oeste' AND empresa_id = @empresa_id LIMIT 1);

-- Insertar Lotes - Tabla: cultivo_lotes
INSERT IGNORE INTO cultivo_lotes (nombre, campo_id, area_hectareas, estado, cultivo_actual, fecha_siembra, fecha_cosecha_esperada, descripcion, empresa_id, user_id, activo, fecha_creacion)
VALUES
('Lote A1 - Soja', @campo_norte_id, 12.50, 'EN_CRECIMIENTO', 'SOJA', DATE_SUB(CURDATE(), INTERVAL 30 DAY), DATE_ADD(CURDATE(), INTERVAL 120 DAY), 'Soja de primera - Variedad DM 53i51', @empresa_id, @admin_user_id, 1, NOW()),
('Lote A2 - Maíz', @campo_norte_id, 13.00, 'PLANIFICADO', 'MAIZ', DATE_ADD(CURDATE(), INTERVAL 15 DAY), DATE_ADD(CURDATE(), INTERVAL 180 DAY), 'Maíz tardío - Híbrido DK 750', @empresa_id, @admin_user_id, 1, NOW()),
('Lote B1 - Trigo', @campo_sur_id, 8.50, 'EN_CRECIMIENTO', 'TRIGO', DATE_SUB(CURDATE(), INTERVAL 90 DAY), DATE_ADD(CURDATE(), INTERVAL 60 DAY), 'Trigo - Variedad Baguette 601', @empresa_id, @admin_user_id, 1, NOW()),
('Lote B2 - Soja', @campo_sur_id, 10.25, 'DISPONIBLE', 'SOJA', NULL, NULL, 'Lote disponible para próxima siembra', @empresa_id, @admin_user_id, 1, NOW()),
('Lote C1 - Girasol', @campo_este_id, 15.00, 'COSECHADO', 'GIRASOL', DATE_SUB(CURDATE(), INTERVAL 180 DAY), DATE_SUB(CURDATE(), INTERVAL 30 DAY), 'Girasol cosechado - Variedad CF 101', @empresa_id, @admin_user_id, 1, NOW()),
('Lote C2 - Soja', @campo_este_id, 17.00, 'EN_CRECIMIENTO', 'SOJA', DATE_SUB(CURDATE(), INTERVAL 45 DAY), DATE_ADD(CURDATE(), INTERVAL 105 DAY), 'Soja de segunda', @empresa_id, @admin_user_id, 1, NOW());

-- Insertar Cultivos
INSERT IGNORE INTO cultivo_cultivos (nombre, tipo, variedad, ciclo_dias, rendimiento_esperado, unidad_rendimiento, precio_por_tonelada, descripcion, estado, activo, empresa_id, usuario_id, created_at)
VALUES
('Soja DM 53i51', 'SOJA', 'DM 53i51', 120, 45.50, 'qq/ha', 280.00, 'Soja resistente a glifosato y glufosinato', 'ACTIVO', 1, @empresa_id, @admin_user_id, NOW()),
('Maíz DK 750', 'MAIZ', 'DK 750', 140, 120.00, 'qq/ha', 95.00, 'Maíz híbrido de alto potencial', 'ACTIVO', 1, @empresa_id, @admin_user_id, NOW()),
('Trigo Baguette 601', 'TRIGO', 'Baguette 601', 180, 65.00, 'qq/ha', 145.00, 'Trigo de buena calidad panadera', 'ACTIVO', 1, @empresa_id, @admin_user_id, NOW()),
('Girasol CF 101', 'GIRASOL', 'CF 101', 110, 28.00, 'qq/ha', 220.00, 'Girasol alto oleico', 'ACTIVO', 1, @empresa_id, @admin_user_id, NOW());

-- ============================================================================
-- 2. MÓDULO PORCINOS - CATÁLOGOS Y CONFIGURACIÓN
-- ============================================================================

-- Insertar Razas de Porcinos
INSERT IGNORE INTO porcinos_razas_porcinos (nombre, descripcion, activo, empresa_id, fecha_creacion)
VALUES
('Yorkshire', 'Raza blanca, conocida por su prolificidad y calidad de carne', 1, @empresa_id, NOW()),
('Landrace', 'Raza blanca, excelente madre, buen instinto maternal', 1, @empresa_id, NOW()),
('Duroc', 'Raza roja, excelente para terminal, buena ganancia de peso', 1, @empresa_id, NOW()),
('Pietrain', 'Raza blanca con manchas negras, muy magra', 1, @empresa_id, NOW()),
('Hampshire', 'Raza negra con cinturón blanco, buena calidad de carne', 1, @empresa_id, NOW());

SET @raza_yorkshire_id = (SELECT id FROM porcinos_razas_porcinos WHERE nombre = 'Yorkshire' AND empresa_id = @empresa_id LIMIT 1);
SET @raza_landrace_id = (SELECT id FROM porcinos_razas_porcinos WHERE nombre = 'Landrace' AND empresa_id = @empresa_id LIMIT 1);
SET @raza_duroc_id = (SELECT id FROM porcinos_razas_porcinos WHERE nombre = 'Duroc' AND empresa_id = @empresa_id LIMIT 1);

-- Insertar Tipos de Servicio
INSERT IGNORE INTO porcinos_tipos_servicio_porcinos (nombre, tipo, descripcion, activo, empresa_id, fecha_creacion)
VALUES
('Monta Natural', 'MONTA_NATURAL_DIRECTA', 'Servicio por monta natural directa', 1, @empresa_id, NOW()),
('IA Tradicional', 'IA_TRADICIONAL', 'Inseminación artificial tradicional', 1, @empresa_id, NOW()),
('IA Post-Cervical', 'IA_POSCERVICAL', 'Inseminación artificial post-cervical', 1, @empresa_id, NOW());

-- Insertar Tipos de Parto
INSERT IGNORE INTO porcinos_tipos_parto (nombre, descripcion, requiere_intervencion, activo, empresa_id, fecha_creacion)
VALUES
('Normal', 'Parto sin complicaciones, sin asistencia', 0, 1, @empresa_id, NOW()),
('Asistido', 'Parto con asistencia leve', 1, 1, @empresa_id, NOW()),
('Distocia', 'Parto con complicaciones, requiere intervención veterinaria', 1, 1, @empresa_id, NOW()),
('Cesárea', 'Parto por cesárea', 1, 1, @empresa_id, NOW());

SET @tipo_parto_normal_id = (SELECT id FROM porcinos_tipos_parto WHERE nombre = 'Normal' AND empresa_id = @empresa_id LIMIT 1);

-- Insertar Causas de Nacidos Muertos
INSERT IGNORE INTO porcinos_causas_nacidos_muertos (nombre, descripcion, activo, empresa_id, fecha_creacion)
VALUES
('Asfixia', 'Muerte por asfixia durante el parto', 1, @empresa_id, NOW()),
('Malformación', 'Nacido con malformaciones incompatibles con la vida', 1, @empresa_id, NOW()),
('Debilidad', 'Lechón demasiado débil al nacer', 1, @empresa_id, NOW()),
('Anoxia', 'Falta de oxígeno durante el parto prolongado', 1, @empresa_id, NOW());

-- Insertar Causas de Momificados
INSERT IGNORE INTO porcinos_causas_momificados (nombre, descripcion, activo, empresa_id, fecha_creacion)
VALUES
('Infección uterina', 'Momificado por infección uterina temprana', 1, @empresa_id, NOW()),
('Malnutrición', 'Momificado por falta de nutrientes', 1, @empresa_id, NOW()),
('Trauma', 'Momificado por trauma durante gestación', 1, @empresa_id, NOW()),
('Viral', 'Momificado por enfermedad viral', 1, @empresa_id, NOW());

-- Insertar Motivos de Baja
INSERT IGNORE INTO porcinos_motivos_baja_porcinos (nombre, descripcion, tipo, activo, empresa_id, fecha_creacion)
VALUES
('Muerte natural', 'Muerte por causas naturales', 'MUERTE', 1, @empresa_id, NOW()),
('Sacrificio sanitario', 'Sacrificio por motivos sanitarios', 'SACRIFICIO', 1, @empresa_id, NOW()),
('Venta', 'Venta de reproductor', 'VENTA', 1, @empresa_id, NOW()),
('Descarte productivo', 'Descarte por baja producción', 'DESCARTE', 1, @empresa_id, NOW()),
('Descarte sanitario', 'Descarte por problemas sanitarios', 'DESCARTE', 1, @empresa_id, NOW());

-- Insertar Causas de Mortalidad
INSERT IGNORE INTO porcinos_causas_mortalidad_porcinos (nombre, descripcion, categoria, activo, empresa_id, fecha_creacion)
VALUES
('Aplastamiento', 'Muerte por aplastamiento de la madre', 'LACTANCIA', 1, @empresa_id, NOW()),
('Diarrea', 'Muerte por diarrea', 'LACTANCIA', 1, @empresa_id, NOW()),
('Debilidad', 'Muerte por debilidad congénita', 'LACTANCIA', 1, @empresa_id, NOW()),
('Enteritis', 'Muerte por enteritis', 'RECRIA', 1, @empresa_id, NOW()),
('Neumonía', 'Muerte por neumonía', 'RECRIA', 1, @empresa_id, NOW()),
('Estrés', 'Muerte por estrés', 'RECRIA', 1, @empresa_id, NOW());

-- Insertar Tipos de Corral (DEPRECADO - ELIMINADO)
-- NOTA: La tabla tipos_corral_porcinos fue eliminada porque estaba en superposición 
-- con UbicacionInterna (porcinos_ubicaciones_internas).
-- El sistema ahora usa únicamente UbicacionInterna (estructura jerárquica: Galpón → Sala → Corral).
-- Use UbicacionInterna para gestionar ubicaciones en lugar de TipoCorralPorcino.
-- Ejemplo: INSERT INTO porcinos_ubicaciones_internas (nombre, nivel, tipo_ubicacion, ...)

-- Insertar Proveedores de Genética
INSERT IGNORE INTO porcinos_proveedores_genetica (nombre, tipo, contacto, telefono, email, activo, empresa_id, fecha_creacion)
VALUES
('Genética Porcina SA', 'CENTRO_IA', 'Juan Pérez', '+54 11 4567-8901', 'contacto@geneticaporcina.com', 1, @empresa_id, NOW()),
('Semen Premium', 'EMPRESA_SEMEN', 'María García', '+54 11 2345-6789', 'ventas@semenpremium.com', 1, @empresa_id, NOW());

-- Insertar Tipos de Evento Sanitario
INSERT IGNORE INTO porcinos_tipos_evento_sanitario (nombre, categoria, descripcion, requiere_fecha_retiro, dias_retiro_defecto, activo, empresa_id, fecha_creacion)
VALUES
('Vacunación PRRS', 'VACUNACION', 'Vacunación contra PRRS', 0, NULL, 1, @empresa_id, NOW()),
('Desparasitación Ivermectina', 'DESPARASITACION', 'Desparasitación con Ivermectina', 1, 21, 1, @empresa_id, NOW()),
('Tratamiento Antibiótico', 'ANTIBIOTICO', 'Tratamiento antibiótico general', 1, 14, 1, @empresa_id, NOW()),
('Vitaminización', 'VITAMINA', 'Aplicación de vitaminas', 0, NULL, 1, @empresa_id, NOW());

-- Insertar Ubicaciones Internas
INSERT IGNORE INTO porcinos_ubicaciones_internas (nombre, codigo, nivel, tipo_ubicacion, capacidad_maxima, descripcion, activo, empresa_id, fecha_creacion)
VALUES
('Galpón Principal', 'GAL-01', 'GALPON', 'GENERAL', 200, 'Galpón principal del establecimiento', 1, @empresa_id, NOW()),
('Sala Maternidad', 'MAT-01', 'SALA', 'MATERNIDAD', 20, 'Sala de maternidad', 1, @empresa_id, NOW()),
('Sala Gestación', 'GES-01', 'SALA', 'GESTACION', 40, 'Sala de gestación', 1, @empresa_id, NOW()),
('Corral Recría 1', 'REC-01', 'CORRAL', 'RECRIA', 25, 'Corral de recría 1', 1, @empresa_id, NOW()),
('Corral Engorde 1', 'ENG-01', 'CORRAL', 'ENGORDE', 30, 'Corral de engorde 1', 1, @empresa_id, NOW());

SET @ubicacion_maternidad_id = (SELECT id FROM porcinos_ubicaciones_internas WHERE codigo = 'MAT-01' AND empresa_id = @empresa_id LIMIT 1);
SET @ubicacion_gestacion_id = (SELECT id FROM porcinos_ubicaciones_internas WHERE codigo = 'GES-01' AND empresa_id = @empresa_id LIMIT 1);

-- ============================================================================
-- 3. MÓDULO PORCINOS - REPRODUCTORES
-- ============================================================================

-- Insertar Padrillos
INSERT IGNORE INTO porcinos_padrillos (identificacion, nombre, fecha_nacimiento, peso_actual, raza_id, activo, empresa_id, usuario_id, fecha_creacion)
VALUES
('PAD-001', 'Toro', DATE_SUB(CURDATE(), INTERVAL 730 DAY), 280.50, @raza_duroc_id, 1, @empresa_id, @admin_user_id, NOW()),
('PAD-002', 'Bravo', DATE_SUB(CURDATE(), INTERVAL 600 DAY), 265.00, @raza_yorkshire_id, 1, @empresa_id, @admin_user_id, NOW()),
('PAD-003', 'Fuerte', DATE_SUB(CURDATE(), INTERVAL 545 DAY), 275.75, @raza_duroc_id, 1, @empresa_id, @admin_user_id, NOW());

SET @padrillo_1_id = (SELECT id FROM porcinos_padrillos WHERE identificacion = 'PAD-001' AND empresa_id = @empresa_id LIMIT 1);
SET @padrillo_2_id = (SELECT id FROM porcinos_padrillos WHERE identificacion = 'PAD-002' AND empresa_id = @empresa_id LIMIT 1);

-- Insertar Madres
INSERT IGNORE INTO porcinos_madres (identificacion, nombre, fecha_nacimiento, peso_actual, numero_partos, raza_id, ubicacion_interna_id, activo, empresa_id, usuario_id, fecha_creacion)
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

-- ============================================================================
-- 4. MÓDULO PORCINOS - SERVICIOS Y GESTACIONES
-- ============================================================================

-- Insertar Servicios (la tabla usa tipo ENUM directamente, no tipo_servicio_id)
INSERT IGNORE INTO porcinos_servicios (madre_id, macho_id, tipo, fecha_servicio, fecha_control_celo, estado_servicio, numero_intento, observaciones, activo, empresa_id, usuario_id, fecha_creacion)
VALUES
(@madre_1_id, @padrillo_1_id, 'IA', DATE_SUB(CURDATE(), INTERVAL 120 DAY), DATE_SUB(CURDATE(), INTERVAL 118 DAY), 'PREÑEZ_CONFIRMADA', 1, 'Servicio exitoso', 1, @empresa_id, @admin_user_id, NOW()),
(@madre_2_id, @padrillo_2_id, 'MONTA_NATURAL', DATE_SUB(CURDATE(), INTERVAL 110 DAY), DATE_SUB(CURDATE(), INTERVAL 108 DAY), 'PREÑEZ_CONFIRMADA', 1, 'Monta natural exitosa', 1, @empresa_id, @admin_user_id, NOW()),
(@madre_3_id, @padrillo_1_id, 'IA', DATE_SUB(CURDATE(), INTERVAL 100 DAY), DATE_SUB(CURDATE(), INTERVAL 98 DAY), 'PREÑEZ_CONFIRMADA', 1, 'IA exitosa', 1, @empresa_id, @admin_user_id, NOW()),
(@madre_4_id, @padrillo_2_id, 'IA', DATE_SUB(CURDATE(), INTERVAL 90 DAY), DATE_SUB(CURDATE(), INTERVAL 88 DAY), 'PREÑEZ_CONFIRMADA', 1, NULL, 1, @empresa_id, @admin_user_id, NOW());

SET @servicio_1_id = (SELECT id FROM porcinos_servicios WHERE madre_id = @madre_1_id ORDER BY fecha_servicio DESC LIMIT 1);

-- Insertar Gestaciones (primero las gestaciones)
INSERT IGNORE INTO porcinos_gestacion (servicio_id, madre_id, fecha_inicio, fecha_probable_parto, estado, activo, empresa_id, usuario_id, fecha_creacion)
VALUES
(@servicio_1_id, @madre_1_id, DATE_SUB(CURDATE(), INTERVAL 120 DAY), DATE_ADD(CURDATE(), INTERVAL 14 DAY), 'EN_CURSO', 1, @empresa_id, @admin_user_id, NOW()),
((SELECT id FROM porcinos_servicios WHERE madre_id = @madre_2_id ORDER BY fecha_servicio DESC LIMIT 1), @madre_2_id, DATE_SUB(CURDATE(), INTERVAL 110 DAY), DATE_ADD(CURDATE(), INTERVAL 24 DAY), 'EN_CURSO', 1, @empresa_id, @admin_user_id, NOW());

SET @gestacion_1_id = (SELECT id FROM porcinos_gestacion WHERE madre_id = @madre_1_id AND estado = 'EN_CURSO' LIMIT 1);

-- Insertar Chequeos de Gestación (después de las gestaciones)
INSERT IGNORE INTO porcinos_chequeos_gestacion (gestacion_id, fecha, metodo, resultado, observaciones, activo, empresa_id, usuario_id, fecha_creacion)
VALUES
(@gestacion_1_id, DATE_SUB(CURDATE(), INTERVAL 90 DAY), 'ECO', 'POSITIVO', 'Gestación confirmada por ecografía', 1, @empresa_id, @admin_user_id, NOW());

-- ============================================================================
-- 5. MÓDULO PORCINOS - PARTOS Y DESTETES
-- ============================================================================

-- Insertar Partos (partos históricos y uno próximo)
INSERT IGNORE INTO porcinos_partos (madre_id, fecha_inicio, fecha_fin, nacidos_vivos, nacidos_muertos, momias, total_nacidos, peso_promedio_nacimiento, tipo_parto_id, intervenciones, observaciones, activo, empresa_id, usuario_id, fecha_creacion)
VALUES
(@madre_1_id, DATE_SUB(CURDATE(), INTERVAL 180 DAY), DATE_SUB(CURDATE(), INTERVAL 180 DAY), 12, 1, 0, 13, 1.45, @tipo_parto_normal_id, NULL, 'Parto normal sin complicaciones', 1, @empresa_id, @admin_user_id, NOW()),
(@madre_2_id, DATE_SUB(CURDATE(), INTERVAL 200 DAY), DATE_SUB(CURDATE(), INTERVAL 200 DAY), 10, 2, 1, 13, 1.38, @tipo_parto_normal_id, NULL, 'Parto normal', 1, @empresa_id, @admin_user_id, NOW()),
(@madre_3_id, DATE_SUB(CURDATE(), INTERVAL 160 DAY), DATE_SUB(CURDATE(), INTERVAL 160 DAY), 14, 0, 0, 14, 1.52, @tipo_parto_normal_id, NULL, 'Excelente parto', 1, @empresa_id, @admin_user_id, NOW());

SET @parto_1_id = (SELECT id FROM porcinos_partos WHERE madre_id = @madre_1_id ORDER BY fecha_inicio DESC LIMIT 1);

-- Insertar Destetes
INSERT IGNORE INTO porcinos_destetes (parto_id, fecha_destete, lechones_destetados, peso_promedio_destete, peso_total, observaciones, activo, empresa_id, usuario_id, fecha_creacion)
VALUES
(@parto_1_id, DATE_SUB(CURDATE(), INTERVAL 125 DAY), 11, 6.50, 71.50, 'Destete exitoso, todos los lechones en buen estado', 1, @empresa_id, @admin_user_id, NOW());

-- ============================================================================
-- 6. MÓDULO PORCINOS - RECRÍAS
-- ============================================================================

SET @lote_recria_id = (SELECT id FROM cultivo_lotes WHERE nombre LIKE '%Recría%' AND empresa_id = @empresa_id LIMIT 1);
SET @lote_recria_id = IFNULL(@lote_recria_id, (SELECT id FROM cultivo_lotes WHERE empresa_id = @empresa_id LIMIT 1));

-- Insertar Recrías
INSERT IGNORE INTO porcinos_recria (lote_id, cantidad_animales, fecha_ingreso, peso_promedio, sexo, destino, activo, empresa_id, usuario_id, fecha_creacion)
VALUES
(@lote_recria_id, 11, DATE_SUB(CURDATE(), INTERVAL 125 DAY), 6.50, 'HEMBRA', 'FUTURA_MADRE', 1, @empresa_id, @admin_user_id, NOW()),
(@lote_recria_id, 30, DATE_SUB(CURDATE(), INTERVAL 200 DAY), 25.00, 'MACHO', 'ENGORDE', 1, @empresa_id, @admin_user_id, NOW());

-- ============================================================================
-- 7. CONFIGURACIÓN DE PARÁMETROS
-- ============================================================================

-- Insertar Parámetros Productivos
INSERT IGNORE INTO porcinos_parametros_productivos_porcinos (empresa_id, dias_promedio_gestacion, dias_lactancia, dias_recria_antes_engorde, dias_engorde, dias_control_celo, dias_entre_celos, dias_pasaje_maternidad, peso_promedio_nacimiento, peso_destete_objetivo, peso_venta_objetivo, lechones_vivos_parto_objetivo, lechones_destetados_objetivo, partos_madre_anio_objetivo, fecha_creacion)
VALUES
(@empresa_id, 114, 21, 60, 120, 5, 21, 7, 1.40, 6.50, 105.00, 12.50, 11.50, 2.35, NOW());

-- ============================================================================
-- RESUMEN FINAL
-- ============================================================================

SELECT '════════════════════════════════════════════════' AS '';
SELECT 'DATOS DE PRUEBA INSERTADOS EXITOSAMENTE' AS '';
SELECT '════════════════════════════════════════════════' AS '';

SELECT 
    'CAMPOS' AS categoria,
    COUNT(*) AS cantidad
FROM cultivo_campos WHERE empresa_id = @empresa_id

UNION ALL

SELECT 
    'LOTES' AS categoria,
    COUNT(*) AS cantidad
FROM cultivo_lotes WHERE empresa_id = @empresa_id

UNION ALL

SELECT 
    'CULTIVOS' AS categoria,
    COUNT(*) AS cantidad
FROM cultivo_cultivos WHERE empresa_id = @empresa_id

UNION ALL

SELECT 
    'MADRES' AS categoria,
    COUNT(*) AS cantidad
FROM porcinos_madres WHERE empresa_id = @empresa_id

UNION ALL

SELECT 
    'PADRILLOS' AS categoria,
    COUNT(*) AS cantidad
FROM porcinos_padrillos WHERE empresa_id = @empresa_id

UNION ALL

SELECT 
    'SERVICIOS' AS categoria,
    COUNT(*) AS cantidad
FROM porcinos_servicios WHERE empresa_id = @empresa_id

UNION ALL

SELECT 
    'PARTOS' AS categoria,
    COUNT(*) AS cantidad
FROM porcinos_partos WHERE empresa_id = @empresa_id

UNION ALL

SELECT 
    'RECRÍAS' AS categoria,
    COUNT(*) AS cantidad
FROM porcinos_recria WHERE empresa_id = @empresa_id;















