-- Migración para asociar cultivos a lotes existentes que ya tienen cultivo_actual
-- Esto permite que los lotes existentes puedan usar la configuración de estados
-- sin necesidad de volver a sembrar

-- Paso 1: Asociar cultivos por coincidencia exacta de nombre y empresa
UPDATE cultivo_lotes l
INNER JOIN cultivo_campos campo ON campo.id = l.campo_id
INNER JOIN empresas e ON e.id = campo.empresa_id
INNER JOIN cultivo_cultivos c ON c.nombre = l.cultivo_actual 
  AND (c.empresa_id = e.id OR c.empresa_id IS NULL)
SET l.cultivo_id = c.id
WHERE l.cultivo_actual IS NOT NULL 
  AND l.cultivo_actual != ''
  AND l.cultivo_id IS NULL
  AND c.nombre = l.cultivo_actual;

-- Paso 2: Asociar cultivos por coincidencia parcial (LIKE) si no se encontró en el paso 1
-- (MySQL no admite LIMIT en UPDATE con varios JOIN; se elige un cultivo por lote con MIN(id))
UPDATE cultivo_lotes l
INNER JOIN (
    SELECT l2.id AS lote_id, MIN(c.id) AS cultivo_elegido_id
    FROM cultivo_lotes l2
    INNER JOIN cultivo_campos campo ON campo.id = l2.campo_id
    INNER JOIN empresas e ON e.id = campo.empresa_id
    INNER JOIN cultivo_cultivos c ON c.nombre LIKE CONCAT('%', l2.cultivo_actual, '%')
      AND (c.empresa_id = e.id OR c.empresa_id IS NULL)
    WHERE l2.cultivo_actual IS NOT NULL
      AND l2.cultivo_actual != ''
      AND l2.cultivo_id IS NULL
    GROUP BY l2.id
) coincidencias ON coincidencias.lote_id = l.id
SET l.cultivo_id = coincidencias.cultivo_elegido_id;

-- Paso 3: Después de asociar cultivos, asociar tipos de cultivo
-- Buscar el TipoCultivo que coincida con el NOMBRE del cultivo (no el campo tipo)
-- El campo 'tipo' en cultivo_cultivos es una categoría, no el nombre del tipo de cultivo
UPDATE cultivo_lotes l
INNER JOIN cultivo_cultivos c ON c.id = l.cultivo_id
INNER JOIN cultivo_tipos_cultivo tc ON tc.nombre = c.nombre
SET l.tipo_cultivo_id = tc.id
WHERE l.cultivo_id IS NOT NULL
  AND l.tipo_cultivo_id IS NULL
  AND tc.activo = true;

-- Nota: Si un lote tiene cultivo_actual pero no se encuentra el cultivo en la BD,
-- el cultivo_id quedará NULL y el sistema usará el método tradicional (fallback)

