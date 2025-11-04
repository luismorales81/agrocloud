-- Script para buscar insumos agroquímicos que tienen dosis configuradas
USE agrocloud;

-- Ver insumos con dosis configuradas
SELECT 
    i.id,
    i.nombre,
    i.tipo,
    COUNT(d.id) as cantidad_dosis
FROM insumos i
INNER JOIN dosis_insumos d ON i.id = d.insumo_id
WHERE i.tipo IN ('HERBICIDA', 'FUNGICIDA', 'INSECTICIDA', 'FERTILIZANTE')
    AND i.activo = 1
    AND d.activo = 1
GROUP BY i.id, i.nombre, i.tipo
ORDER BY cantidad_dosis DESC
LIMIT 10;

-- Ver detalles de las dosis de un insumo específico
-- Reemplazar el ID con el ID del insumo que quieras ver
SELECT 
    i.id as insumo_id,
    i.nombre as insumo_nombre,
    i.tipo as tipo_insumo,
    d.id as dosis_id,
    d.tipo_aplicacion,
    d.forma_aplicacion,
    d.unidad,
    d.dosis_recomendada_por_ha
FROM insumos i
INNER JOIN dosis_insumos d ON i.id = d.insumo_id
WHERE i.tipo IN ('HERBICIDA', 'FUNGICIDA', 'INSECTICIDA', 'FERTILIZANTE')
    AND i.activo = 1
    AND d.activo = 1
ORDER BY i.nombre, d.tipo_aplicacion, d.forma_aplicacion
LIMIT 20;

