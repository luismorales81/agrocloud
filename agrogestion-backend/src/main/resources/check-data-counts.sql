-- =====================================================
-- SCRIPT PARA VERIFICAR CONTEOS DE DATOS EN TABLAS
-- =====================================================

-- Asegurar que estamos en la base de datos correcta
USE agroclouddb;

-- Verificar conteo de datos en cada tabla
SELECT 'CONTEOS DE DATOS EN TABLAS:' as titulo;

-- Verificar roles
SELECT 'ROLES:' as tabla;
SELECT COUNT(*) as cantidad FROM roles;

-- Verificar usuarios
SELECT 'USUARIOS:' as tabla;
SELECT COUNT(*) as cantidad FROM usuarios;

-- Verificar campos
SELECT 'CAMPOS:' as tabla;
SELECT COUNT(*) as cantidad FROM campos;

-- Verificar lotes
SELECT 'LOTES:' as tabla;
SELECT COUNT(*) as cantidad FROM lotes;

-- Verificar insumos
SELECT 'INSUMOS:' as tabla;
SELECT COUNT(*) as cantidad FROM insumos;

-- Verificar maquinaria
SELECT 'MAQUINARIA:' as tabla;
SELECT COUNT(*) as cantidad FROM maquinaria;

-- Verificar labores
SELECT 'LABORES:' as tabla;
SELECT COUNT(*) as cantidad FROM labores;

-- Verificar mantenimientos
SELECT 'MANTENIMIENTOS:' as tabla;
SELECT COUNT(*) as cantidad FROM mantenimiento_maquinaria;

-- Mostrar algunos datos de ejemplo
SELECT 'DATOS DE EJEMPLO:' as titulo;

-- Mostrar roles
SELECT 'ROLES DISPONIBLES:' as subtitulo;
SELECT id, nombre, descripcion FROM roles;

-- Mostrar usuarios
SELECT 'USUARIOS DISPONIBLES:' as subtitulo;
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

-- Mostrar campos
SELECT 'CAMPOS DISPONIBLES:' as subtitulo;
SELECT 
    id,
    nombre,
    ubicacion,
    area_hectareas,
    tipo_suelo,
    estado,
    activo
FROM campos
ORDER BY id;

-- Mostrar lotes
SELECT 'LOTES DISPONIBLES:' as subtitulo;
SELECT 
    l.id,
    l.nombre,
    c.nombre as campo,
    l.area_hectareas,
    l.cultivo_actual,
    l.estado
FROM lotes l
JOIN campos c ON l.campo_id = c.id
ORDER BY l.id;

-- Mostrar insumos
SELECT 'INSUMOS DISPONIBLES:' as subtitulo;
SELECT 
    id,
    nombre,
    tipo,
    unidad_medida,
    precio_unitario,
    stock_disponible,
    stock_minimo
FROM insumos
ORDER BY id;

-- Mostrar maquinaria
SELECT 'MAQUINARIA DISPONIBLE:' as subtitulo;
SELECT 
    id,
    nombre,
    tipo,
    marca,
    modelo,
    anio,
    estado,
    horas_uso
FROM maquinaria
ORDER BY id;

-- Mostrar labores
SELECT 'LABORES DISPONIBLES:' as subtitulo;
SELECT 
    l.id,
    l.tipo_labor,
    l.descripcion,
    l.fecha_inicio,
    l.fecha_fin,
    l.estado,
    l.costo_total,
    lo.nombre as lote
FROM labores l
JOIN lotes lo ON l.lote_id = lo.id
ORDER BY l.id;

-- Mostrar mantenimientos
SELECT 'MANTENIMIENTOS DISPONIBLES:' as subtitulo;
SELECT 
    m.id,
    m.tipo_mantenimiento,
    m.descripcion,
    m.fecha_mantenimiento,
    m.costo,
    m.proximo_mantenimiento,
    ma.nombre as maquinaria
FROM mantenimiento_maquinaria m
JOIN maquinaria ma ON m.maquinaria_id = ma.id
ORDER BY m.id;

-- =====================================================
-- RESUMEN FINAL
-- =====================================================

SELECT 'RESUMEN FINAL:' as titulo;
SELECT 
    'Total de registros en el sistema:' as concepto,
    (SELECT COUNT(*) FROM roles) +
    (SELECT COUNT(*) FROM usuarios) +
    (SELECT COUNT(*) FROM campos) +
    (SELECT COUNT(*) FROM lotes) +
    (SELECT COUNT(*) FROM insumos) +
    (SELECT COUNT(*) FROM maquinaria) +
    (SELECT COUNT(*) FROM labores) +
    (SELECT COUNT(*) FROM mantenimiento_maquinaria) as total_registros;
