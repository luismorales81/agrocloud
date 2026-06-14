-- ============================================================================
-- SCRIPT PARA ACTUALIZAR NOMBRES DE LOTES DE RECRÍA
-- Formato: "NombreMadre - FechaNacimiento"
-- ============================================================================

-- Este script actualiza los nombres de los lotes que están asociados a recrías
-- con el formato: "IdentificaciónMadre - FechaIngreso"

-- Ejemplo: "MAD-001 - 08/01/2026"

SET @empresa_id = 1; -- Ajustar según sea necesario, o comentar para procesar todas las empresas

-- Actualizar nombres de lotes asociados a recrías
UPDATE cultivo_lotes l
INNER JOIN porcinos_recria r ON l.id = r.lote_id
INNER JOIN porcinos_partos p ON 
    DATE(p.fecha_inicio) = r.fecha_ingreso 
    AND p.empresa_id = r.empresa_id
    AND p.activo = 1
INNER JOIN porcinos_madres m ON p.madre_id = m.id
SET l.nombre = CONCAT(
    m.identificacion, 
    ' - ', 
    DATE_FORMAT(r.fecha_ingreso, '%d/%m/%Y')
)
WHERE r.activo = 1
  AND m.activo = 1
  AND (p.empresa_id = @empresa_id OR @empresa_id IS NULL);

-- Mostrar resultados
SELECT 
    'Lotes actualizados' AS resultado,
    COUNT(*) AS cantidad
FROM cultivo_lotes l
INNER JOIN porcinos_recria r ON l.id = r.lote_id
INNER JOIN porcinos_partos p ON 
    DATE(p.fecha_inicio) = r.fecha_ingreso 
    AND p.empresa_id = r.empresa_id
    AND p.activo = 1
INNER JOIN porcinos_madres m ON p.madre_id = m.id
WHERE r.activo = 1
  AND m.activo = 1
  AND (p.empresa_id = @empresa_id OR @empresa_id IS NULL);

-- Mostrar los lotes actualizados
SELECT 
    l.id AS lote_id,
    l.nombre AS nombre_lote,
    m.identificacion AS madre,
    r.fecha_ingreso AS fecha_nacimiento,
    r.cantidad_animales AS cantidad_animales
FROM cultivo_lotes l
INNER JOIN porcinos_recria r ON l.id = r.lote_id
INNER JOIN porcinos_partos p ON 
    DATE(p.fecha_inicio) = r.fecha_ingreso 
    AND p.empresa_id = r.empresa_id
    AND p.activo = 1
INNER JOIN porcinos_madres m ON p.madre_id = m.id
WHERE r.activo = 1
  AND m.activo = 1
  AND (p.empresa_id = @empresa_id OR @empresa_id IS NULL)
ORDER BY r.fecha_ingreso DESC;

-- Nota: Si hay recrías sin parto asociado, sus lotes no se actualizarán
-- Para ver recrías sin parto asociado:
SELECT 
    r.id AS recria_id,
    l.id AS lote_id,
    l.nombre AS nombre_lote_actual,
    r.fecha_ingreso AS fecha_ingreso,
    r.cantidad_animales AS cantidad_animales
FROM porcinos_recria r
INNER JOIN cultivo_lotes l ON r.lote_id = l.id
LEFT JOIN porcinos_partos p ON 
    DATE(p.fecha_inicio) = r.fecha_ingreso 
    AND p.empresa_id = r.empresa_id
    AND p.activo = 1
WHERE r.activo = 1
  AND p.id IS NULL
  AND (r.empresa_id = @empresa_id OR @empresa_id IS NULL);
