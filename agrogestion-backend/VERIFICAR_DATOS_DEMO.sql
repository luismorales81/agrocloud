-- ============================================================================
-- SCRIPT DE VERIFICACIÓN DE DATOS PARA DEMO
-- Ejecutar antes de la demostración para verificar que todos los datos estén cargados
-- ============================================================================

USE agrocloud;

-- Obtener ID de empresa
SET @empresa_id = (SELECT id FROM empresas WHERE nombre LIKE '%AgroCloud%' AND activo = 1 LIMIT 1);
SET @empresa_id = IFNULL(@empresa_id, (SELECT id FROM empresas WHERE activo = 1 LIMIT 1));

SELECT '════════════════════════════════════════════════════════' AS '';
SELECT 'VERIFICACIÓN DE DATOS PARA DEMO' AS '';
SELECT '════════════════════════════════════════════════════════' AS '';
SELECT '' AS '';

-- Verificar catálogos
SELECT '📋 CATÁLOGOS' AS '';
SELECT 
    'Razas' AS catalogo,
    COUNT(*) AS cantidad
FROM porcinos_razas_porcinos WHERE empresa_id = @empresa_id AND activo = 1

UNION ALL

SELECT 'Tipos de Alimento', COUNT(*) FROM porcinos_tipos_alimento_porcinos WHERE empresa_id = @empresa_id AND activo = 1
UNION ALL
SELECT 'Tipos de Servicio', COUNT(*) FROM porcinos_tipos_servicio_porcinos WHERE empresa_id = @empresa_id AND activo = 1
UNION ALL
SELECT 'Causas de Mortalidad', COUNT(*) FROM porcinos_causas_mortalidad_porcinos WHERE empresa_id = @empresa_id AND activo = 1
UNION ALL
SELECT 'Motivos de Baja', COUNT(*) FROM porcinos_motivos_baja_porcinos WHERE empresa_id = @empresa_id AND activo = 1
UNION ALL
SELECT 'Esquemas Sanitarios', COUNT(*) FROM porcinos_esquemas_sanitarios_porcinos WHERE empresa_id = @empresa_id AND activo = 1
UNION ALL
SELECT 'Tipos de Parto', COUNT(*) FROM porcinos_tipos_parto WHERE empresa_id = @empresa_id AND activo = 1
UNION ALL
SELECT 'Ubicaciones Internas', COUNT(*) FROM porcinos_ubicaciones_internas WHERE empresa_id = @empresa_id AND activo = 1
UNION ALL
SELECT 'Proveedores Genética', COUNT(*) FROM porcinos_proveedores_genetica WHERE empresa_id = @empresa_id AND activo = 1;

SELECT '' AS '';
SELECT '🐷 REPRODUCTORES' AS '';
SELECT 
    'Madres Activas' AS tipo,
    COUNT(*) AS cantidad
FROM porcinos_madres WHERE empresa_id = @empresa_id AND activo = 1 AND estado_actual != 'DESCARTE'

UNION ALL

SELECT 'Padrillos Activos', COUNT(*) FROM porcinos_padrillos WHERE empresa_id = @empresa_id AND activo = 1;

SELECT '' AS '';
SELECT '👶 CICLO REPRODUCTIVO' AS '';
SELECT 
    'Servicios' AS tipo,
    COUNT(*) AS cantidad
FROM porcinos_servicios WHERE empresa_id = @empresa_id AND activo = 1

UNION ALL

SELECT 'Gestaciones Activas', COUNT(*) FROM porcinos_gestacion WHERE empresa_id = @empresa_id AND activo = 1 AND estado = 'EN_CURSO'
UNION ALL
SELECT 'Partos Registrados', COUNT(*) FROM porcinos_partos WHERE empresa_id = @empresa_id AND activo = 1
UNION ALL
SELECT 'Destetes Registrados', COUNT(*) FROM porcinos_destetes WHERE empresa_id = @empresa_id AND activo = 1;

SELECT '' AS '';
SELECT '🐖 CRECIMIENTO' AS '';
SELECT 
    'Recrías Activas' AS tipo,
    COUNT(*) AS cantidad,
    SUM(cantidad_animales) AS total_animales
FROM porcinos_recria WHERE empresa_id = @empresa_id AND activo = 1;

SELECT '' AS '';
SELECT '💰 VENTAS' AS '';
SELECT 
    'Ventas Registradas' AS tipo,
    COUNT(*) AS cantidad,
    SUM(ingreso_total) AS ingreso_total
FROM porcinos_ventas_porcinos WHERE empresa_id = @empresa_id AND activo = 1;

SELECT '' AS '';
SELECT '🌾 ALIMENTACIÓN' AS '';
SELECT 
    'Consumos Registrados' AS tipo,
    COUNT(*) AS cantidad,
    SUM(cantidad_kg) AS total_kg
FROM porcinos_consumos_alimento WHERE empresa_id = @empresa_id AND activo = 1;

SELECT '' AS '';
SELECT '💉 SANIDAD' AS '';
-- Nota: La tabla porcinos_eventos_sanitarios puede no existir aún
SELECT 
    'Eventos Sanitarios' AS tipo,
    'Tabla no disponible' AS cantidad;

SELECT '' AS '';
SELECT '📅 EVENTOS EN CALENDARIO (Próximos 30 días)' AS '';
SELECT 
    'Partos Próximos' AS tipo,
    COUNT(*) AS cantidad
FROM porcinos_gestacion 
WHERE empresa_id = @empresa_id 
  AND activo = 1 
  AND estado = 'EN_CURSO'
  AND fecha_probable_parto BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 30 DAY);

SELECT '' AS '';
SELECT '════════════════════════════════════════════════════════' AS '';
SELECT '✅ VERIFICACIÓN COMPLETA' AS '';
SELECT '════════════════════════════════════════════════════════' AS '';
SELECT '' AS '';
SELECT 'NOTA: Para una demo exitosa, se recomienda tener:' AS '';
SELECT '  - Al menos 5 madres activas' AS '';
SELECT '  - Al menos 2 padrillos activos' AS '';
SELECT '  - Al menos 3 gestaciones activas' AS '';
SELECT '  - Al menos 2 partos registrados' AS '';
SELECT '  - Al menos 1 destete' AS '';
SELECT '  - Al menos 1 recría activa' AS '';
SELECT '  - Al menos 1 venta registrada' AS '';
SELECT '  - Al menos algunos consumos de alimento' AS '';
SELECT '  - Al menos algunos eventos sanitarios' AS '';
SELECT '  - Al menos 1 parto próximo (para mostrar en calendario)' AS '';

