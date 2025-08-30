-- =====================================================
-- SCRIPT DE VERIFICACIÓN PARA AGROCLOUDBD
-- Verificar que la base de datos esté configurada correctamente
-- =====================================================

USE agroclouddb;

-- =====================================================
-- VERIFICACIÓN DE TABLAS
-- =====================================================

SELECT 'VERIFICACIÓN DE TABLAS:' as titulo;

SELECT 
    TABLE_NAME,
    TABLE_ROWS,
    DATA_LENGTH,
    INDEX_LENGTH
FROM INFORMATION_SCHEMA.TABLES 
WHERE TABLE_SCHEMA = 'agroclouddb'
ORDER BY TABLE_NAME;

-- =====================================================
-- VERIFICACIÓN DE USUARIOS Y ROLES
-- =====================================================

SELECT 'USUARIOS Y ROLES:' as titulo;

SELECT 
    u.id,
    u.nombre_usuario,
    u.email,
    u.nombre,
    u.apellido,
    u.activo,
    GROUP_CONCAT(r.nombre) as roles
FROM usuarios u
LEFT JOIN usuario_roles ur ON u.id = ur.usuario_id
LEFT JOIN roles r ON ur.rol_id = r.id
GROUP BY u.id
ORDER BY u.id;

-- =====================================================
-- VERIFICACIÓN DE CAMPOS
-- =====================================================

SELECT 'CAMPOS REGISTRADOS:' as titulo;

SELECT 
    id,
    nombre,
    ubicacion,
    area_hectareas,
    tipo_suelo,
    estado,
    activo,
    CASE 
        WHEN poligono IS NOT NULL THEN 'SÍ'
        ELSE 'NO'
    END as tiene_poligono,
    CASE 
        WHEN coordenadas IS NOT NULL THEN 'SÍ'
        ELSE 'NO'
    END as tiene_coordenadas,
    fecha_creacion
FROM campos
ORDER BY id;

-- =====================================================
-- VERIFICACIÓN DE LOTES
-- =====================================================

SELECT 'LOTES REGISTRADOS:' as titulo;

SELECT 
    l.id,
    l.nombre,
    c.nombre as campo,
    l.area_hectareas,
    l.cultivo_actual,
    l.estado,
    l.fecha_siembra,
    l.fecha_cosecha_esperada
FROM lotes l
JOIN campos c ON l.campo_id = c.id
ORDER BY l.id;

-- =====================================================
-- VERIFICACIÓN DE INSUMOS
-- =====================================================

SELECT 'INSUMOS REGISTRADOS:' as titulo;

SELECT 
    id,
    nombre,
    tipo,
    unidad_medida,
    precio_unitario,
    stock_disponible,
    stock_minimo,
    proveedor
FROM insumos
ORDER BY tipo, nombre;

-- =====================================================
-- VERIFICACIÓN DE MAQUINARIA
-- =====================================================

SELECT 'MAQUINARIA REGISTRADA:' as titulo;

SELECT 
    id,
    nombre,
    tipo,
    marca,
    modelo,
    anio,
    estado,
    horas_uso,
    costo_por_hora
FROM maquinaria
ORDER BY tipo, nombre;

-- =====================================================
-- VERIFICACIÓN DE LABORES
-- =====================================================

SELECT 'LABORES REGISTRADAS:' as titulo;

SELECT 
    l.id,
    l.tipo_labor,
    lo.nombre as lote,
    c.nombre as campo,
    l.fecha_inicio,
    l.fecha_fin,
    l.estado,
    l.costo_total
FROM labores l
JOIN lotes lo ON l.lote_id = lo.id
JOIN campos c ON lo.campo_id = c.id
ORDER BY l.fecha_inicio DESC;

-- =====================================================
-- VERIFICACIÓN DE MANTENIMIENTOS
-- =====================================================

SELECT 'MANTENIMIENTOS REGISTRADOS:' as titulo;

SELECT 
    m.id,
    ma.nombre as maquinaria,
    m.tipo_mantenimiento,
    m.fecha_mantenimiento,
    m.proximo_mantenimiento,
    m.costo,
    m.estado
FROM mantenimiento_maquinaria m
JOIN maquinaria ma ON m.maquinaria_id = ma.id
ORDER BY m.fecha_mantenimiento DESC;

-- =====================================================
-- VERIFICACIÓN DE ÍNDICES
-- =====================================================

SELECT 'ÍNDICES CREADOS:' as titulo;

SELECT 
    TABLE_NAME,
    INDEX_NAME,
    COLUMN_NAME,
    NON_UNIQUE
FROM INFORMATION_SCHEMA.STATISTICS 
WHERE TABLE_SCHEMA = 'agroclouddb'
ORDER BY TABLE_NAME, INDEX_NAME;

-- =====================================================
-- ESTADÍSTICAS GENERALES
-- =====================================================

SELECT 'ESTADÍSTICAS GENERALES:' as titulo;

SELECT 
    'Usuarios' as entidad,
    COUNT(*) as cantidad
FROM usuarios
UNION ALL
SELECT 
    'Roles' as entidad,
    COUNT(*) as cantidad
FROM roles
UNION ALL
SELECT 
    'Campos' as entidad,
    COUNT(*) as cantidad
FROM campos
UNION ALL
SELECT 
    'Lotes' as entidad,
    COUNT(*) as cantidad
FROM lotes
UNION ALL
SELECT 
    'Insumos' as entidad,
    COUNT(*) as cantidad
FROM insumos
UNION ALL
SELECT 
    'Maquinaria' as entidad,
    COUNT(*) as cantidad
FROM maquinaria
UNION ALL
SELECT 
    'Labores' as entidad,
    COUNT(*) as cantidad
FROM labores
UNION ALL
SELECT 
    'Mantenimientos' as entidad,
    COUNT(*) as cantidad
FROM mantenimiento_maquinaria;

-- =====================================================
-- VERIFICACIÓN DE CONFIGURACIÓN
-- =====================================================

SELECT 'CONFIGURACIÓN DE LA BASE DE DATOS:' as titulo;

SELECT 
    'agroclouddb' as base_datos,
    'utf8mb4' as charset,
    'utf8mb4_unicode_ci' as collation;

-- =====================================================
-- VERIFICACIÓN DE CONEXIÓN
-- =====================================================

SELECT 'VERIFICACIÓN DE CONEXIÓN:' as titulo;

SELECT 
    'Conexión exitosa' as estado,
    NOW() as fecha_verificacion,
    VERSION() as version_mysql;

-- =====================================================
-- FIN DEL SCRIPT DE VERIFICACIÓN
-- =====================================================

SELECT 'VERIFICACIÓN COMPLETADA' as mensaje;
SELECT 'Si todos los datos se muestran correctamente, la base de datos está funcionando bien' as mensaje;
