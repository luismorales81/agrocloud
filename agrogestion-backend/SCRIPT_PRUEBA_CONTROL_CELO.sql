-- ============================================================================
-- SCRIPT DE PRUEBA PARA CONTROL DE CELO
-- ============================================================================
-- Este script crea servicios con estado PENDIENTE_CONTROL para probar
-- el endpoint de control de celo y verificar que funcione correctamente
-- ============================================================================

-- Variables de configuración
SET @empresa_id = (SELECT id FROM empresas WHERE activo = 1 LIMIT 1);
SET @admin_user_id = (SELECT id FROM usuarios WHERE username = 'admin' LIMIT 1);

-- Si no hay empresa, usar los del script de prueba
SET @empresa_id = IFNULL(@empresa_id, (SELECT id FROM empresas WHERE nombre = 'Empresa Prueba Porcinos' LIMIT 1));
SET @admin_user_id = IFNULL(@admin_user_id, (SELECT id FROM usuarios WHERE activo = 1 LIMIT 1));

-- Verificar que tenemos empresa y usuario
SELECT CONCAT('✓ Empresa ID: ', COALESCE(@empresa_id, 'NO ENCONTRADA')) AS verificacion;
SELECT CONCAT('✓ Usuario ID: ', COALESCE(@admin_user_id, 'NO ENCONTRADO')) AS verificacion;

-- ============================================================================
-- 1. VERIFICAR/CREAR PARÁMETROS PRODUCTIVOS
-- ============================================================================
INSERT INTO porcinos_parametros_productivos_porcinos (
    empresa_id, 
    dias_promedio_gestacion, 
    dias_lactancia, 
    dias_control_celo,
    dias_entre_celos,
    fecha_creacion
)
VALUES (
    @empresa_id,
    115,  -- días de gestación
    21,   -- días de lactancia
    21,   -- días para control de celo
    21,   -- días entre celos
    NOW()
)
ON DUPLICATE KEY UPDATE empresa_id = empresa_id;

-- ============================================================================
-- 2. VERIFICAR/CREAR RAZA
-- ============================================================================
INSERT IGNORE INTO porcinos_razas_porcinos (empresa_id, nombre, descripcion, activo, fecha_creacion)
VALUES (@empresa_id, 'Yorkshire', 'Raza Yorkshire para pruebas', 1, NOW());

SET @raza_id = (SELECT id FROM porcinos_razas_porcinos WHERE empresa_id = @empresa_id AND nombre = 'Yorkshire' LIMIT 1);

-- ============================================================================
-- 3. CREAR/VERIFICAR MADRE PARA PRUEBA
-- ============================================================================
-- Buscar una madre existente o crear una nueva
SET @madre_id = (SELECT id FROM porcinos_madres WHERE empresa_id = @empresa_id AND activo = 1 LIMIT 1);

-- Si no existe, crear una nueva
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
SELECT 
    'MADRE-CONTROL-CELO-001',
    DATE_SUB(CURDATE(), INTERVAL 1 YEAR),
    DATE_SUB(CURDATE(), INTERVAL 6 MONTH),
    14,
    'EXTERNA',
    'ADULTA',
    @raza_id,
    0,
    1,
    @empresa_id,
    @admin_user_id,
    NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM porcinos_madres 
    WHERE identificacion = 'MADRE-CONTROL-CELO-001' 
    AND empresa_id = @empresa_id
);

SET @madre_id = IFNULL(@madre_id, (SELECT id FROM porcinos_madres WHERE identificacion = 'MADRE-CONTROL-CELO-001' AND empresa_id = @empresa_id LIMIT 1));

SELECT CONCAT('✓ Madre ID: ', @madre_id) AS verificacion;

-- ============================================================================
-- 4. CREAR/VERIFICAR PADRILLO
-- ============================================================================
SET @padrillo_id = (SELECT id FROM porcinos_padrillos WHERE empresa_id = @empresa_id AND activo = 1 LIMIT 1);

-- Si no existe, crear uno nuevo
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
SELECT 
    'PADRILLO-CONTROL-CELO-001',
    DATE_SUB(CURDATE(), INTERVAL 2 YEAR),
    DATE_SUB(CURDATE(), INTERVAL 1 YEAR),
    'INTERNA',
    1,
    @empresa_id,
    @admin_user_id,
    NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM porcinos_padrillos 
    WHERE identificacion = 'PADRILLO-CONTROL-CELO-001' 
    AND empresa_id = @empresa_id
);

SET @padrillo_id = IFNULL(@padrillo_id, (SELECT id FROM porcinos_padrillos WHERE identificacion = 'PADRILLO-CONTROL-CELO-001' AND empresa_id = @empresa_id LIMIT 1));

SELECT CONCAT('✓ Padrillo ID: ', @padrillo_id) AS verificacion;

-- ============================================================================
-- 5. LIMPIAR SERVICIOS ANTERIORES DE PRUEBA (OPCIONAL)
-- ============================================================================
-- Descomentar si quieres limpiar servicios de prueba anteriores
-- DELETE FROM porcinos_gestacion WHERE empresa_id = @empresa_id AND servicio_id IN (
--     SELECT id FROM porcinos_servicios WHERE empresa_id = @empresa_id AND identificacion LIKE 'SERVICIO-PRUEBA-CONTROL-CELO-%'
-- );
-- DELETE FROM porcinos_servicios WHERE empresa_id = @empresa_id AND identificacion LIKE 'SERVICIO-PRUEBA-CONTROL-CELO-%';

-- ============================================================================
-- 6. CREAR SERVICIOS CON ESTADO PENDIENTE_CONTROL
-- ============================================================================

-- Servicio 1: Reciente (hace 20 días) - Listo para control de celo
SET @fecha_servicio1 = DATE_SUB(CURDATE(), INTERVAL 20 DAY);
SET @fecha_control_celo1 = DATE_ADD(@fecha_servicio1, INTERVAL 21 DAY);

INSERT INTO porcinos_servicios (
    madre_id,
    tipo,
    fecha_servicio,
    macho_id,
    numero_intento,
    estado_servicio,
    fecha_control_celo,
    observaciones,
    activo,
    empresa_id,
    usuario_id,
    fecha_creacion
)
VALUES (
    @madre_id,
    'MONTA_NATURAL',
    @fecha_servicio1,
    @padrillo_id,
    1,
    'PENDIENTE_CONTROL',  -- ESTADO PENDIENTE para probar control de celo
    @fecha_control_celo1,
    'Servicio de prueba para control de celo - Servicio 1',
    1,
    @empresa_id,
    @admin_user_id,
    NOW()
);

SET @servicio1_id = LAST_INSERT_ID();

-- Servicio 2: Más reciente (hace 10 días) - Aún no es momento del control
SET @fecha_servicio2 = DATE_SUB(CURDATE(), INTERVAL 10 DAY);
SET @fecha_control_celo2 = DATE_ADD(@fecha_servicio2, INTERVAL 21 DAY);

INSERT INTO porcinos_servicios (
    madre_id,
    tipo,
    fecha_servicio,
    macho_id,
    numero_intento,
    estado_servicio,
    fecha_control_celo,
    observaciones,
    activo,
    empresa_id,
    usuario_id,
    fecha_creacion
)
VALUES (
    @madre_id,
    'IA',
    @fecha_servicio2,
    @padrillo_id,
    1,
    'PENDIENTE_CONTROL',  -- ESTADO PENDIENTE para probar control de celo
    @fecha_control_celo2,
    'Servicio de prueba para control de celo - Servicio 2 (IA)',
    1,
    @empresa_id,
    @admin_user_id,
    NOW()
);

SET @servicio2_id = LAST_INSERT_ID();

-- ============================================================================
-- 7. CREAR GESTACIONES AUTOMÁTICAS (como lo hace el sistema al crear servicio)
-- ============================================================================
-- Obtener días de gestación de parámetros
SET @dias_gestacion = (SELECT COALESCE(dias_promedio_gestacion, 115) FROM porcinos_parametros_productivos_porcinos WHERE empresa_id = @empresa_id LIMIT 1);

-- Gestación para Servicio 1
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
    @madre_id,
    @servicio1_id,
    @fecha_servicio1,
    DATE_ADD(@fecha_servicio1, INTERVAL @dias_gestacion DAY),
    'EN_CURSO',
    1,
    @empresa_id,
    @admin_user_id,
    NOW()
)
ON DUPLICATE KEY UPDATE servicio_id = servicio_id;

-- Gestación para Servicio 2
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
    @madre_id,
    @servicio2_id,
    @fecha_servicio2,
    DATE_ADD(@fecha_servicio2, INTERVAL @dias_gestacion DAY),
    'EN_CURSO',
    1,
    @empresa_id,
    @admin_user_id,
    NOW()
)
ON DUPLICATE KEY UPDATE servicio_id = servicio_id;

-- ============================================================================
-- 8. ACTUALIZAR ESTADO DE LA MADRE
-- ============================================================================
-- La madre debe estar en estado ADULTA o GESTACION según corresponda
UPDATE porcinos_madres 
SET estado_actual = 'ADULTA' 
WHERE id = @madre_id 
  AND estado_actual NOT IN ('GESTACION', 'LACTANCIA');

-- ============================================================================
-- 9. RESUMEN DE DATOS CREADOS
-- ============================================================================
SELECT '═══════════════════════════════════════════════════════════════' AS '';
SELECT 'RESUMEN DE SERVICIOS PARA CONTROL DE CELO' AS '';
SELECT '═══════════════════════════════════════════════════════════════' AS '';
SELECT '' AS '';

SELECT 
    s.id AS servicio_id,
    m.identificacion AS madre,
    s.fecha_servicio,
    s.estado_servicio,
    s.fecha_control_celo,
    DATEDIFF(CURDATE(), s.fecha_servicio) AS dias_desde_servicio,
    CASE 
        WHEN s.estado_servicio = 'PENDIENTE_CONTROL' THEN '✅ LISTO PARA CONTROL'
        WHEN s.estado_servicio = 'PREÑEZ_CONFIRMADA' THEN '✅ PREÑEZ CONFIRMADA'
        WHEN s.estado_servicio = 'FALLIDO' THEN '❌ FALLIDO'
        ELSE '⚠️ OTRO ESTADO'
    END AS estado_descripcion
FROM porcinos_servicios s
JOIN porcinos_madres m ON s.madre_id = m.id
WHERE s.empresa_id = @empresa_id 
  AND s.activo = 1
  AND s.estado_servicio = 'PENDIENTE_CONTROL'
ORDER BY s.fecha_servicio DESC;

SELECT '' AS '';
SELECT '═══════════════════════════════════════════════════════════════' AS '';
SELECT 'INSTRUCCIONES PARA PROBAR:' AS '';
SELECT '' AS '';
SELECT CONCAT('1. Servicio ID ', @servicio1_id, ' - Listo para control (hace 20 días)') AS paso1;
SELECT CONCAT('2. Servicio ID ', @servicio2_id, ' - Aún no es momento (hace 10 días)') AS paso2;
SELECT '' AS '';
SELECT '3. Probar endpoint: POST /api/v1/porcinos/servicios/{id}/control-celo' AS paso3;
SELECT '   Body: { "preñada": true, "observaciones": "Control positivo" }' AS body_ejemplo;
SELECT '' AS '';
SELECT '4. Verificar que:' AS paso4;
SELECT '   - El estado del servicio cambie a PREÑEZ_CONFIRMADA' AS verificacion1;
SELECT '   - La gestación se confirme (estado EN_CURSO)' AS verificacion2;
SELECT '   - El estado de la madre cambie a GESTACION' AS verificacion3;
SELECT '' AS '';
SELECT '═══════════════════════════════════════════════════════════════' AS '';
