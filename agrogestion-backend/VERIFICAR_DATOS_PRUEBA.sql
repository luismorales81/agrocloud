-- ============================================================================
-- SCRIPT DE VERIFICACIÓN DE DATOS DE PRUEBA
-- ============================================================================

USE agrocloud;

SET @empresa_id = (SELECT id FROM empresas WHERE activo = 1 LIMIT 1);

SELECT '════════════════════════════════════════════════════════════════' AS '';
SELECT CONCAT('VERIFICACIÓN DE DATOS PARA EMPRESA ID: ', @empresa_id) AS '';
SELECT '════════════════════════════════════════════════════════════════' AS '';
SELECT '' AS '';

SELECT '────────────────────────────────────────────────────────────────' AS '';
SELECT 'MÓDULO CULTIVOS' AS '';
SELECT '────────────────────────────────────────────────────────────────' AS '';

SELECT 'CAMPOS:' AS '';
SELECT nombre, area_hectareas, ubicacion, estado
FROM campos 
WHERE empresa_id = @empresa_id
ORDER BY nombre;

SELECT '' AS '';

SELECT 'LOTES:' AS '';
SELECT l.nombre, c.nombre AS campo, l.cultivo_actual, l.estado, l.area_hectareas
FROM lotes l
JOIN campos c ON l.campo_id = c.id
WHERE l.empresa_id = @empresa_id
ORDER BY c.nombre, l.nombre;

SELECT '' AS '';

SELECT 'CULTIVOS:' AS '';
SELECT nombre, tipo, variedad, rendimiento_esperado, precio_por_tonelada
FROM cultivo_cultivos
WHERE empresa_id = @empresa_id
ORDER BY tipo, nombre;

SELECT '' AS '';
SELECT '────────────────────────────────────────────────────────────────' AS '';
SELECT 'MÓDULO PORCINOS - CATÁLOGOS' AS '';
SELECT '────────────────────────────────────────────────────────────────' AS '';

SELECT 'RAZAS:' AS '';
SELECT nombre, descripcion
FROM porcinos_razas_porcinos
WHERE empresa_id = @empresa_id AND activo = 1
ORDER BY nombre;

SELECT '' AS '';

SELECT 'TIPOS DE SERVICIO:' AS '';
SELECT nombre, tipo, descripcion
FROM porcinos_tipos_servicio_porcinos
WHERE empresa_id = @empresa_id AND activo = 1
ORDER BY nombre;

SELECT '' AS '';

SELECT 'TIPOS DE PARTO:' AS '';
SELECT nombre, descripcion, requiere_intervencion
FROM porcinos_tipos_parto
WHERE empresa_id = @empresa_id AND activo = 1
ORDER BY nombre;

SELECT '' AS '';

SELECT 'UBICACIONES INTERNAS:' AS '';
SELECT nombre, codigo, nivel, tipo_ubicacion, capacidad_maxima
FROM porcinos_ubicaciones_internas
WHERE empresa_id = @empresa_id AND activo = 1
ORDER BY nivel, nombre;

SELECT '' AS '';
SELECT '────────────────────────────────────────────────────────────────' AS '';
SELECT 'MÓDULO PORCINOS - REPRODUCTORES' AS '';
SELECT '────────────────────────────────────────────────────────────────' AS '';

SELECT 'MADRES:' AS '';
SELECT identificacion, nombre, fecha_nacimiento, numero_partos, peso_actual
FROM porcinos_madres
WHERE empresa_id = @empresa_id AND activo = 1
ORDER BY identificacion;

SELECT '' AS '';

SELECT 'PADRILLOS:' AS '';
SELECT identificacion, nombre, fecha_nacimiento, peso_actual
FROM porcinos_padrillos
WHERE empresa_id = @empresa_id AND activo = 1
ORDER BY identificacion;

SELECT '' AS '';
SELECT '────────────────────────────────────────────────────────────────' AS '';
SELECT 'MÓDULO PORCINOS - OPERACIONES' AS '';
SELECT '────────────────────────────────────────────────────────────────' AS '';

SELECT 'SERVICIOS:' AS '';
SELECT m.identificacion AS madre, p.identificacion AS padrillo, s.fecha_servicio, s.tipo, s.estado_servicio
FROM porcinos_servicios s
JOIN porcinos_madres m ON s.madre_id = m.id
LEFT JOIN porcinos_padrillos p ON s.macho_id = p.id
WHERE s.empresa_id = @empresa_id AND s.activo = 1
ORDER BY s.fecha_servicio DESC;

SELECT '' AS '';

SELECT 'GESTACIONES:' AS '';
SELECT m.identificacion AS madre, g.fecha_inicio, g.fecha_probable_parto, g.estado
FROM porcinos_gestacion g
JOIN porcinos_madres m ON g.madre_id = m.id
WHERE g.empresa_id = @empresa_id AND g.activo = 1
ORDER BY g.fecha_inicio DESC;

SELECT '' AS '';

SELECT 'PARTOS:' AS '';
SELECT m.identificacion AS madre, p.fecha_inicio, p.nacidos_vivos, p.nacidos_muertos, p.momias, p.total_nacidos
FROM porcinos_partos p
JOIN porcinos_madres m ON p.madre_id = m.id
WHERE p.empresa_id = @empresa_id AND p.activo = 1
ORDER BY p.fecha_inicio DESC;

SELECT '' AS '';

SELECT 'RECRÍAS:' AS '';
SELECT cantidad_animales, fecha_ingreso, peso_promedio, sexo, destino
FROM porcinos_recria
WHERE empresa_id = @empresa_id AND activo = 1
ORDER BY fecha_ingreso DESC;

SELECT '' AS '';
SELECT '────────────────────────────────────────────────────────────────' AS '';
SELECT 'RESUMEN TOTAL' AS '';
SELECT '────────────────────────────────────────────────────────────────' AS '';

SELECT 
    'CAMPOS' AS categoria,
    COUNT(*) AS cantidad
FROM campos WHERE empresa_id = @empresa_id

UNION ALL

SELECT 
    'LOTES',
    COUNT(*)
FROM lotes WHERE empresa_id = @empresa_id

UNION ALL

SELECT 
    'CULTIVOS',
    COUNT(*)
FROM cultivo_cultivos WHERE empresa_id = @empresa_id

UNION ALL

SELECT 
    'RAZAS',
    COUNT(*)
FROM porcinos_razas_porcinos WHERE empresa_id = @empresa_id AND activo = 1

UNION ALL

SELECT 
    'MADRES',
    COUNT(*)
FROM porcinos_madres WHERE empresa_id = @empresa_id AND activo = 1

UNION ALL

SELECT 
    'PADRILLOS',
    COUNT(*)
FROM porcinos_padrillos WHERE empresa_id = @empresa_id AND activo = 1

UNION ALL

SELECT 
    'SERVICIOS',
    COUNT(*)
FROM porcinos_servicios WHERE empresa_id = @empresa_id AND activo = 1

UNION ALL

SELECT 
    'GESTACIONES',
    COUNT(*)
FROM porcinos_gestacion WHERE empresa_id = @empresa_id AND activo = 1

UNION ALL

SELECT 
    'PARTOS',
    COUNT(*)
FROM porcinos_partos WHERE empresa_id = @empresa_id AND activo = 1

UNION ALL

SELECT 
    'RECRÍAS',
    COUNT(*)
FROM porcinos_recria WHERE empresa_id = @empresa_id AND activo = 1

ORDER BY categoria;

SELECT '' AS '';
SELECT '════════════════════════════════════════════════════════════════' AS '';
SELECT 'VERIFICACIÓN COMPLETADA' AS '';
SELECT '════════════════════════════════════════════════════════════════' AS '';















