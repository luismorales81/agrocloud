-- ============================================================================
-- SCRIPT DE PRUEBA DEL CICLO PRODUCTIVO DE PORCINOS
-- ============================================================================
-- Este script crea datos de prueba para validar el ciclo productivo completo:
-- Servicio → Gestación → Parto → Destete → Recría
-- 
-- Las fechas se calculan automáticamente según los parámetros productivos configurados:
-- - Gestación: días_promedio_gestacion (por defecto 115, pero CONFIGURABLE)
-- - Lactancia: dias_lactancia (por defecto 21, pero CONFIGURABLE)
-- - Recría: dias_recria_antes_engorde (por defecto 60, pero CONFIGURABLE)
-- - Engorde: dias_engorde (por defecto 120, pero CONFIGURABLE)
-- 
-- IMPORTANTE: El script lee los valores de los parámetros productivos y los usa
-- para calcular todas las fechas. Si cambias los parámetros, las fechas se ajustarán.
-- ============================================================================

-- Variables de configuración (ajustar según necesidad)
SET @empresa_id = (SELECT id FROM empresas LIMIT 1);
SET @admin_user_id = (SELECT id FROM users WHERE username = 'admin' LIMIT 1);
SET @fecha_base = CURDATE(); -- Fecha de referencia (puede cambiarse a una fecha pasada para probar ciclos completos)

-- Si no hay empresa o usuario, crear datos básicos
INSERT IGNORE INTO empresas (nombre, razon_social, cuit, activo, fecha_creacion)
VALUES ('Empresa Prueba Porcinos', 'Empresa Prueba Porcinos S.A.', '20-12345678-9', 1, NOW());

SET @empresa_id = (SELECT id FROM empresas WHERE nombre = 'Empresa Prueba Porcinos' LIMIT 1);

-- ============================================================================
-- 1. CONFIGURAR PARÁMETROS PRODUCTIVOS (si no existen)
-- ============================================================================
-- IMPORTANTE: Estos valores se usarán para calcular todas las fechas del ciclo
-- Si ya existen parámetros, se usarán esos valores en lugar de estos
INSERT INTO porcinos_parametros_productivos_porcinos (
    empresa_id, 
    dias_promedio_gestacion, 
    dias_lactancia, 
    dias_recria_antes_engorde, 
    dias_engorde,
    dias_tolerancia_vencimiento_gestacion,
    dias_control_celo,
    dias_entre_celos,
    dias_pasaje_maternidad,
    dias_antelacion_alertar_partos,
    dias_antelacion_alertar_ecografias,
    dias_antelacion_alertar_destetes,
    peso_promedio_nacimiento,
    peso_destete_objetivo,
    peso_venta_objetivo,
    fecha_creacion
)
VALUES (
    @empresa_id,
    115,  -- días de gestación (CONFIGURABLE - se usa para calcular fechas)
    21,   -- días de lactancia (CONFIGURABLE)
    60,   -- días de recría antes de engorde (CONFIGURABLE)
    120,  -- días de engorde (CONFIGURABLE)
    5,    -- tolerancia vencimiento gestación
    21,   -- control de celo
    21,   -- días entre celos
    7,    -- pasaje a maternidad (7 días antes del parto)
    7,    -- alertar partos 7 días antes
    3,    -- alertar ecografías 3 días antes
    2,    -- alertar destetes 2 días antes
    1.5,  -- peso promedio nacimiento (kg)
    7.0,  -- peso destete objetivo (kg)
    110.0, -- peso venta objetivo (kg)
    NOW()
)
ON DUPLICATE KEY UPDATE empresa_id = empresa_id;

-- Obtener los valores configurados de los parámetros (para usar en cálculos)
SET @dias_gestacion = (SELECT COALESCE(dias_promedio_gestacion, 115) FROM porcinos_parametros_productivos_porcinos WHERE empresa_id = @empresa_id LIMIT 1);
SET @dias_lactancia = (SELECT COALESCE(dias_lactancia, 21) FROM porcinos_parametros_productivos_porcinos WHERE empresa_id = @empresa_id LIMIT 1);
SET @dias_pasaje_maternidad = (SELECT COALESCE(dias_pasaje_maternidad, 7) FROM porcinos_parametros_productivos_porcinos WHERE empresa_id = @empresa_id LIMIT 1);

-- ============================================================================
-- 2. CREAR RAZA Y CATÁLOGOS BÁSICOS (si no existen)
-- ============================================================================
INSERT IGNORE INTO porcinos_razas_porcinos (empresa_id, nombre, descripcion, activo, fecha_creacion)
VALUES (@empresa_id, 'Yorkshire', 'Raza Yorkshire para pruebas', 1, NOW());

SET @raza_id = (SELECT id FROM porcinos_razas_porcinos WHERE empresa_id = @empresa_id AND nombre = 'Yorkshire' LIMIT 1);

-- ============================================================================
-- 3. CREAR MADRES DE PRUEBA
-- ============================================================================
-- Madre 1: Ciclo completo en el pasado (para verificar historial)
INSERT INTO porcinos_madres (
    identificacion, 
    fecha_nacimiento, 
    fecha_ingreso_granja, 
    cantidad_tetas, 
    origen, 
    estado_actual, 
    raza_id,
    numero_partos,
    activo, 
    empresa_id, 
    usuario_id, 
    fecha_creacion
)
VALUES (
    'MADRE-PRUEBA-001',
    DATE_SUB(@fecha_base, INTERVAL 2 YEAR),
    DATE_SUB(@fecha_base, INTERVAL 1 YEAR),
    14,
    'EXTERNA',
    'ADULTA',
    @raza_id,
    0,
    1,
    @empresa_id,
    @admin_user_id,
    NOW()
);

SET @madre1_id = LAST_INSERT_ID();

-- Madre 2: Ciclo en curso (gestación activa)
INSERT INTO porcinos_madres (
    identificacion, 
    fecha_nacimiento, 
    fecha_ingreso_granja, 
    cantidad_tetas, 
    origen, 
    estado_actual, 
    raza_id,
    numero_partos,
    activo, 
    empresa_id, 
    usuario_id, 
    fecha_creacion
)
VALUES (
    'MADRE-PRUEBA-002',
    DATE_SUB(@fecha_base, INTERVAL 1 YEAR),
    DATE_SUB(@fecha_base, INTERVAL 6 MONTH),
    14,
    'EXTERNA',
    'ADULTA',
    @raza_id,
    0,
    1,
    @empresa_id,
    @admin_user_id,
    NOW()
);

SET @madre2_id = LAST_INSERT_ID();

-- Madre 3: En lactancia (parto reciente)
INSERT INTO porcinos_madres (
    identificacion, 
    fecha_nacimiento, 
    fecha_ingreso_granja, 
    cantidad_tetas, 
    origen, 
    estado_actual, 
    raza_id,
    numero_partos,
    activo, 
    empresa_id, 
    usuario_id, 
    fecha_creacion
)
VALUES (
    'MADRE-PRUEBA-003',
    DATE_SUB(@fecha_base, INTERVAL 1 YEAR),
    DATE_SUB(@fecha_base, INTERVAL 6 MONTH),
    14,
    'EXTERNA',
    'LACTANCIA',
    @raza_id,
    1,
    1,
    @empresa_id,
    @admin_user_id,
    NOW()
);

SET @madre3_id = LAST_INSERT_ID();

-- ============================================================================
-- 4. CREAR PADRILLO DE PRUEBA
-- ============================================================================
INSERT INTO porcinos_padrillos (
    identificacion,
    fecha_nacimiento,
    fecha_ingreso_granja,
    origen,
    activo,
    empresa_id,
    usuario_id,
    fecha_creacion
)
VALUES (
    'PADRILLO-PRUEBA-001',
    DATE_SUB(@fecha_base, INTERVAL 2 YEAR),
    DATE_SUB(@fecha_base, INTERVAL 1 YEAR),
    'INTERNO',
    1,
    @empresa_id,
    @admin_user_id,
    NOW()
);

SET @padrillo_id = LAST_INSERT_ID();

-- ============================================================================
-- 5. CICLO COMPLETO - MADRE 1 (PASADO - PARA VERIFICAR HISTORIAL)
-- ============================================================================
-- Servicio 1 (hace 6 meses)
SET @fecha_servicio1 = DATE_SUB(@fecha_base, INTERVAL 6 MONTH);
SET @fecha_control_celo1 = DATE_ADD(@fecha_servicio1, INTERVAL 21 DAY);

INSERT INTO porcinos_servicios (
    madre_id,
    tipo,
    fecha_servicio,
    macho_id,
    numero_intento,
    estado_servicio,
    fecha_control_celo,
    activo,
    empresa_id,
    usuario_id,
    fecha_creacion
)
VALUES (
    @madre1_id,
    'MONTA_NATURAL',
    @fecha_servicio1,
    @padrillo_id,
    1,
    'PREÑEZ_CONFIRMADA',
    @fecha_control_celo1,
    1,
    @empresa_id,
    @admin_user_id,
    NOW()
);

SET @servicio1_id = LAST_INSERT_ID();

-- Gestación 1 (usando días de gestación configurados)
SET @fecha_inicio_gestacion1 = @fecha_servicio1;
SET @fecha_probable_parto1 = DATE_ADD(@fecha_inicio_gestacion1, INTERVAL @dias_gestacion DAY);
SET @fecha_sala_maternidad1 = DATE_SUB(@fecha_probable_parto1, INTERVAL @dias_pasaje_maternidad DAY);

INSERT INTO porcinos_gestacion (
    madre_id,
    servicio_id,
    fecha_inicio,
    fecha_probable_parto,
    fecha_sala_maternidad,
    estado,
    activo,
    empresa_id,
    usuario_id,
    fecha_creacion
)
VALUES (
    @madre1_id,
    @servicio1_id,
    @fecha_inicio_gestacion1,
    @fecha_probable_parto1,
    @fecha_sala_maternidad1,
    'FINALIZADA',
    1,
    @empresa_id,
    @admin_user_id,
    NOW()
);

SET @gestacion1_id = LAST_INSERT_ID();

-- Parto 1 (en fecha probable de parto)
SET @fecha_parto1 = @fecha_probable_parto1;

INSERT INTO porcinos_partos (
    madre_id,
    fecha_inicio,
    nacidos_vivos,
    nacidos_muertos,
    momias,
    total_nacidos,
    peso_promedio_nacimiento,
    activo,
    empresa_id,
    usuario_id,
    fecha_creacion
)
VALUES (
    @madre1_id,
    @fecha_parto1,
    12,  -- nacidos vivos
    1,   -- nacidos muertos
    0,   -- momias
    13,  -- total
    1.5, -- peso promedio (kg)
    1,
    @empresa_id,
    @admin_user_id,
    NOW()
);

SET @parto1_id = LAST_INSERT_ID();

-- Actualizar número de partos de la madre
UPDATE porcinos_madres SET numero_partos = 1 WHERE id = @madre1_id;

-- Destete 1 (usando días de lactancia configurados)
SET @fecha_destete1 = DATE_ADD(@fecha_parto1, INTERVAL @dias_lactancia DAY);

INSERT INTO porcinos_destetes (
    parto_id,
    fecha_destete,
    cantidad_destetados,
    peso_promedio_destete,
    dias_lactancia,
    activo,
    empresa_id,
    usuario_id,
    fecha_creacion
)
VALUES (
    @parto1_id,
    @fecha_destete1,
    11,  -- destetados (1 murió durante lactancia)
    7.0, -- peso promedio destete (kg)
    21,  -- días de lactancia
    1,
    @empresa_id,
    @admin_user_id,
    NOW()
);

SET @destete1_id = LAST_INSERT_ID();

-- Cerrar parto
UPDATE porcinos_partos SET fecha_fin = @fecha_destete1 WHERE id = @parto1_id;

-- ============================================================================
-- 6. CICLO EN CURSO - MADRE 2 (GESTACIÓN ACTIVA)
-- ============================================================================
-- Servicio 2 (hace 60 días - gestación en curso)
SET @fecha_servicio2 = DATE_SUB(@fecha_base, INTERVAL 60 DAY);
SET @fecha_control_celo2 = DATE_ADD(@fecha_servicio2, INTERVAL 21 DAY);

INSERT INTO porcinos_servicios (
    madre_id,
    tipo,
    fecha_servicio,
    macho_id,
    numero_intento,
    estado_servicio,
    fecha_control_celo,
    activo,
    empresa_id,
    usuario_id,
    fecha_creacion
)
VALUES (
    @madre2_id,
    'MONTA_NATURAL',
    @fecha_servicio2,
    @padrillo_id,
    1,
    'PREÑEZ_CONFIRMADA',
    @fecha_control_celo2,
    1,
    @empresa_id,
    @admin_user_id,
    NOW()
);

SET @servicio2_id = LAST_INSERT_ID();

-- Gestación 2 (ACTIVA - debe generar alertas próximamente)
SET @fecha_inicio_gestacion2 = @fecha_servicio2;
SET @fecha_probable_parto2 = DATE_ADD(@fecha_inicio_gestacion2, INTERVAL @dias_gestacion DAY);
SET @fecha_sala_maternidad2 = DATE_SUB(@fecha_probable_parto2, INTERVAL @dias_pasaje_maternidad DAY);

INSERT INTO porcinos_gestacion (
    madre_id,
    servicio_id,
    fecha_inicio,
    fecha_probable_parto,
    fecha_sala_maternidad,
    estado,
    activo,
    empresa_id,
    usuario_id,
    fecha_creacion
)
VALUES (
    @madre2_id,
    @servicio2_id,
    @fecha_inicio_gestacion2,
    @fecha_probable_parto2,
    @fecha_sala_maternidad2,
    'EN_CURSO',  -- ESTADO ACTIVO
    1,
    @empresa_id,
    @admin_user_id,
    NOW()
);

SET @gestacion2_id = LAST_INSERT_ID();

-- Actualizar estado de la madre a GESTACION
UPDATE porcinos_madres SET estado_actual = 'GESTACION' WHERE id = @madre2_id;

-- ============================================================================
-- 7. CICLO EN LACTANCIA - MADRE 3 (PARTO RECIENTE)
-- ============================================================================
-- Servicio 3 (hace 4 meses)
SET @fecha_servicio3 = DATE_SUB(@fecha_base, INTERVAL 4 MONTH);
SET @fecha_control_celo3 = DATE_ADD(@fecha_servicio3, INTERVAL 21 DAY);

INSERT INTO porcinos_servicios (
    madre_id,
    tipo,
    fecha_servicio,
    macho_id,
    numero_intento,
    estado_servicio,
    fecha_control_celo,
    activo,
    empresa_id,
    usuario_id,
    fecha_creacion
)
VALUES (
    @madre3_id,
    'MONTA_NATURAL',
    @fecha_servicio3,
    @padrillo_id,
    1,
    'PREÑEZ_CONFIRMADA',
    @fecha_control_celo3,
    1,
    @empresa_id,
    @admin_user_id,
    NOW()
);

SET @servicio3_id = LAST_INSERT_ID();

-- Gestación 3 (finalizada)
SET @fecha_inicio_gestacion3 = @fecha_servicio3;
SET @fecha_probable_parto3 = DATE_ADD(@fecha_inicio_gestacion3, INTERVAL @dias_gestacion DAY);

INSERT INTO porcinos_gestacion (
    madre_id,
    servicio_id,
    fecha_inicio,
    fecha_probable_parto,
    estado,
    activo,
    empresa_id,
    usuario_id,
    fecha_creacion
)
VALUES (
    @madre3_id,
    @servicio3_id,
    @fecha_inicio_gestacion3,
    @fecha_probable_parto3,
    'FINALIZADA',
    1,
    @empresa_id,
    @admin_user_id,
    NOW()
);

SET @gestacion3_id = LAST_INSERT_ID();

-- Parto 3 (hace 10 días - en lactancia)
SET @fecha_parto3 = DATE_SUB(@fecha_base, INTERVAL 10 DAY);

INSERT INTO porcinos_partos (
    madre_id,
    fecha_inicio,
    nacidos_vivos,
    nacidos_muertos,
    momias,
    total_nacidos,
    peso_promedio_nacimiento,
    activo,
    empresa_id,
    usuario_id,
    fecha_creacion
)
VALUES (
    @madre3_id,
    @fecha_parto3,
    13,  -- nacidos vivos
    0,   -- nacidos muertos
    0,   -- momias
    13,  -- total
    1.6, -- peso promedio (kg)
    1,
    @empresa_id,
    @admin_user_id,
    NOW()
);

SET @parto3_id = LAST_INSERT_ID();

-- Actualizar número de partos
UPDATE porcinos_madres SET numero_partos = 1 WHERE id = @madre3_id;

-- ============================================================================
-- 8. RESUMEN DE DATOS CREADOS
-- ============================================================================
SELECT '═══════════════════════════════════════════════════════════════' AS '';
SELECT 'RESUMEN DE DATOS DE PRUEBA CREADOS' AS '';
SELECT '═══════════════════════════════════════════════════════════════' AS '';
SELECT '' AS '';

SELECT 
    'MADRES' AS categoria,
    COUNT(*) AS cantidad,
    GROUP_CONCAT(identificacion SEPARATOR ', ') AS detalles
FROM porcinos_madres 
WHERE empresa_id = @empresa_id AND identificacion LIKE 'MADRE-PRUEBA-%';

SELECT 
    'SERVICIOS' AS categoria,
    COUNT(*) AS cantidad,
    CONCAT('Desde ', MIN(fecha_servicio), ' hasta ', MAX(fecha_servicio)) AS detalles
FROM porcinos_servicios 
WHERE empresa_id = @empresa_id;

SELECT 
    'GESTACIONES' AS categoria,
    COUNT(*) AS total,
    SUM(CASE WHEN estado = 'EN_CURSO' THEN 1 ELSE 0 END) AS activas,
    SUM(CASE WHEN estado = 'FINALIZADA' THEN 1 ELSE 0 END) AS finalizadas
FROM porcinos_gestacion 
WHERE empresa_id = @empresa_id;

SELECT 
    'PARTOS' AS categoria,
    COUNT(*) AS cantidad,
    SUM(nacidos_vivos) AS total_lechones_vivos
FROM porcinos_partos 
WHERE empresa_id = @empresa_id;

SELECT 
    'DESTETES' AS categoria,
    COUNT(*) AS cantidad,
    SUM(cantidad_destetados) AS total_destetados
FROM porcinos_destetes 
WHERE empresa_id = @empresa_id;

SELECT '' AS '';
SELECT '═══════════════════════════════════════════════════════════════' AS '';
SELECT 'CICLOS PRODUCTIVOS CREADOS:' AS '';
SELECT '' AS '';
SELECT 
    CONCAT('MADRE 1: Ciclo completo (pasado) - Parto: ', @fecha_parto1) AS ciclo1;
SELECT 
    CONCAT('MADRE 2: Gestación activa - Parto esperado: ', @fecha_probable_parto2) AS ciclo2;
SELECT 
    CONCAT('MADRE 3: En lactancia - Parto: ', @fecha_parto3, ' (destete esperado: ', DATE_ADD(@fecha_parto3, INTERVAL 21 DAY), ')') AS ciclo3;
SELECT '' AS '';
SELECT '═══════════════════════════════════════════════════════════════' AS '';
