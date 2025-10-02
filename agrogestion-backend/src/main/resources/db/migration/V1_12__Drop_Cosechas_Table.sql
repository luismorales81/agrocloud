-- Migración V1_12: Eliminar tabla cosechas duplicada
-- Motivo: Se unifica el sistema usando solo historial_cosechas
-- Fecha: 30 de Septiembre, 2025

-- 1. Migrar datos existentes de cosechas a historial_cosechas (si existen)
INSERT INTO historial_cosechas (
    lote_id,
    cultivo_id,
    fecha_siembra,
    fecha_cosecha,
    superficie_hectareas,
    cantidad_cosechada,
    unidad_cosecha,
    rendimiento_real,
    rendimiento_esperado,
    observaciones,
    estado_suelo,
    requiere_descanso,
    dias_descanso_recomendados,
    usuario_id,
    fecha_creacion,
    fecha_actualizacion
)
SELECT 
    c.lote_id,
    c.cultivo_id,
    COALESCE(l.fecha_siembra, DATE_SUB(c.fecha_cosecha, INTERVAL 120 DAY)) as fecha_siembra,
    c.fecha_cosecha,
    COALESCE(l.area_hectareas, 0) as superficie_hectareas,
    COALESCE(c.cantidad_toneladas, 0) as cantidad_cosechada,
    'ton' as unidad_cosecha,
    CASE 
        WHEN l.area_hectareas > 0 THEN c.cantidad_toneladas / l.area_hectareas
        ELSE 0
    END as rendimiento_real,
    NULL as rendimiento_esperado,
    c.observaciones,
    'BUENO' as estado_suelo,
    FALSE as requiere_descanso,
    0 as dias_descanso_recomendados,
    c.usuario_id,
    c.created_at,
    c.updated_at
FROM cosechas c
LEFT JOIN lotes l ON c.lote_id = l.id
WHERE NOT EXISTS (
    SELECT 1 FROM historial_cosechas hc 
    WHERE hc.lote_id = c.lote_id 
    AND hc.fecha_cosecha = c.fecha_cosecha
    AND hc.cultivo_id = c.cultivo_id
);

-- 2. Eliminar tabla cosechas
DROP TABLE IF EXISTS cosechas;

-- 3. Mensaje de confirmación
SELECT 'Tabla cosechas eliminada exitosamente. Sistema unificado con historial_cosechas' AS mensaje;
