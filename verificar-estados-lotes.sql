-- Script para verificar estados de lotes en la base de datos
-- Ejecutar en MySQL/phpMyAdmin

USE agrogestion;

-- Ver todos los lotes con sus estados actuales
SELECT 
    l.id,
    l.nombre AS 'Nombre Lote',
    c.nombre AS 'Campo',
    l.estado AS 'Estado',
    l.cultivo_actual AS 'Cultivo Actual',
    l.area_hectareas AS 'Superficie (ha)',
    l.fecha_siembra AS 'Fecha Siembra',
    l.fecha_cosecha_esperada AS 'Cosecha Esperada',
    l.fecha_ultima_modificacion AS 'Última Modificación'
FROM lotes l
LEFT JOIN campos c ON l.campo_id = c.id
ORDER BY l.id;

-- Contar lotes por estado
SELECT 
    estado AS 'Estado',
    COUNT(*) AS 'Cantidad de Lotes'
FROM lotes
GROUP BY estado
ORDER BY COUNT(*) DESC;

-- Ver lotes que PUEDEN ser sembrados (DISPONIBLE, PREPARADO, EN_PREPARACION)
SELECT 
    l.id,
    l.nombre AS 'Nombre Lote',
    l.estado AS 'Estado',
    'SÍ puede sembrar' AS 'Acción Disponible'
FROM lotes l
WHERE l.estado IN ('DISPONIBLE', 'PREPARADO', 'EN_PREPARACION')
ORDER BY l.id;

-- Ver lotes que PUEDEN ser cosechados
SELECT 
    l.id,
    l.nombre AS 'Nombre Lote',
    l.estado AS 'Estado',
    l.cultivo_actual AS 'Cultivo',
    'SÍ puede cosechar' AS 'Acción Disponible'
FROM lotes l
WHERE l.estado IN ('SEMBRADO', 'LISTO_PARA_COSECHA', 'EN_CRECIMIENTO', 'EN_FLORACION', 'EN_FRUTIFICACION')
ORDER BY l.id;

-- Ver últimas labores realizadas en cada lote
SELECT 
    l.nombre AS 'Lote',
    lab.tipo_labor AS 'Última Labor',
    lab.estado AS 'Estado Labor',
    lab.fecha_inicio AS 'Fecha',
    lab.responsable AS 'Responsable'
FROM lotes l
LEFT JOIN labores lab ON l.id = lab.lote_id
WHERE lab.activo = TRUE
ORDER BY l.id, lab.fecha_inicio DESC;
