-- ============================================================================
-- SCRIPT DE MIGRACIÓN: UNIFICAR FAENAS CON VENTAS
-- ============================================================================
-- Este script migra los registros de la tabla porcinos_faena a porcinos_ventas_porcinos
-- como ventas de tipo FAENA, unificando ambos conceptos en una sola tabla.
--
-- IMPORTANTE: Ejecutar este script DESPUÉS de que la columna realiza_faena 
-- haya sido agregada a porcinos_parametros_establecimiento_porcinos
-- ============================================================================

USE agrocloud;

-- 1. Agregar columna realiza_faena a parámetros del establecimiento (si no existe)
ALTER TABLE porcinos_parametros_establecimiento_porcinos
ADD COLUMN IF NOT EXISTS realiza_faena BOOLEAN NOT NULL DEFAULT FALSE
COMMENT 'Indica si el establecimiento realiza faenas';

-- 2. Detectar empresas que tienen faenas y marcarlas como realiza_faena = true
UPDATE porcinos_parametros_establecimiento_porcinos pep
INNER JOIN (
    SELECT DISTINCT empresa_id 
    FROM porcinos_faena 
    WHERE empresa_id IS NOT NULL
) faenas ON pep.empresa_id = faenas.empresa_id
SET pep.realiza_faena = TRUE;

-- 3. Migrar faenas existentes a ventas (solo si no existen ya como ventas FAENA)
INSERT INTO porcinos_ventas_porcinos (
    tipo,
    fecha,
    cantidad,
    peso_promedio,
    precio_kg,
    ingreso_total,
    recria_id,
    cliente,
    observaciones,
    peso_envio,
    peso_faena,
    rendimiento,
    fecha_envio,
    fecha_faena,
    empresa_id,
    usuario_id,
    activo,
    fecha_creacion,
    fecha_actualizacion
)
SELECT 
    'FAENA' as tipo,
    COALESCE(fecha_envio, fecha_creacion) as fecha,
    cantidad_animales as cantidad,
    CASE 
        WHEN cantidad_animales > 0 THEN peso_envio / cantidad_animales
        ELSE 0
    END as peso_promedio,
    precio_kg,
    COALESCE(ingreso_total, 0) as ingreso_total,
    recria_id,
    NULL as cliente, -- Las faenas antiguas no tenían cliente
    observaciones,
    peso_envio,
    peso_faena,
    rendimiento,
    fecha_envio,
    fecha_faena,
    empresa_id,
    usuario_id,
    TRUE as activo,
    fecha_creacion,
    COALESCE(fecha_actualizacion, fecha_creacion) as fecha_actualizacion
FROM porcinos_faena f
WHERE NOT EXISTS (
    -- Evitar duplicados: verificar que no exista ya una venta FAENA para esta recría con fecha similar
    SELECT 1 
    FROM porcinos_ventas_porcinos v 
    WHERE v.tipo = 'FAENA' 
    AND v.recria_id = f.recria_id 
    AND ABS(DATEDIFF(v.fecha, COALESCE(f.fecha_envio, f.fecha_creacion))) <= 1
);

-- 4. Mostrar resumen de migración
SELECT 
    'Resumen de Migración' as tipo,
    (SELECT COUNT(*) FROM porcinos_faena) as faenas_originales,
    (SELECT COUNT(*) FROM porcinos_ventas_porcinos WHERE tipo = 'FAENA') as ventas_faena_finales,
    (SELECT COUNT(DISTINCT empresa_id) FROM porcinos_parametros_establecimiento_porcinos WHERE realiza_faena = TRUE) as empresas_con_faena;

SELECT '✓ Migración completada. Las faenas ahora están en la tabla de ventas con tipo FAENA' AS resultado;

-- NOTA: Después de verificar que la migración fue exitosa, se puede eliminar la tabla porcinos_faena
-- (dejarla como backup por un tiempo antes de eliminarla definitivamente)
