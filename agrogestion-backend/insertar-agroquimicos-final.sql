-- ========================================
-- SCRIPT FINAL PARA INSERTAR AGROQUÍMICOS
-- ========================================
-- Este script inserta agroquímicos en la tabla específica

USE agrocloud;

-- Insertar en la tabla agroquimicos (sin categoria_toxicologica)
INSERT IGNORE INTO agroquimicos (
    insumo_id,
    principio_activo,
    concentracion,
    clase_quimica,
    periodo_carencia_dias,
    dosis_minima_por_ha,
    dosis_maxima_por_ha,
    unidad_dosis,
    activo,
    fecha_creacion,
    fecha_actualizacion
) 
SELECT 
    i.id,
    i.principio_activo,
    i.concentracion,
    i.clase_quimica,
    i.periodo_carencia_dias,
    i.dosis_minima_por_ha,
    i.dosis_maxima_por_ha,
    i.unidad_dosis,
    1,
    NOW(),
    NOW()
FROM insumos i 
WHERE i.tipo IN ('HERBICIDA', 'FUNGICIDA', 'INSECTICIDA') 
AND i.empresa_id = 1
AND i.principio_activo IS NOT NULL
AND NOT EXISTS (SELECT 1 FROM agroquimicos a WHERE a.insumo_id = i.id);

-- Verificar resultado
SELECT 'RESULTADO FINAL' as estado;
SELECT COUNT(*) as total_insumos FROM insumos WHERE empresa_id = 1;
SELECT COUNT(*) as total_agroquimicos FROM agroquimicos a JOIN insumos i ON a.insumo_id = i.id WHERE i.empresa_id = 1;

-- Mostrar agroquímicos en tabla específica
SELECT 
    a.id,
    i.nombre,
    i.tipo,
    a.principio_activo,
    a.concentracion,
    a.clase_quimica,
    a.periodo_carencia_dias,
    i.stock_actual,
    i.precio_unitario
FROM agroquimicos a
JOIN insumos i ON a.insumo_id = i.id
WHERE i.empresa_id = 1
ORDER BY i.tipo, i.nombre;

-- Resumen final
SELECT 'RESUMEN FINAL' as titulo;
SELECT 
    'INSUMOS GENERALES' as categoria,
    COUNT(*) as cantidad
FROM insumos 
WHERE empresa_id = 1 
AND tipo NOT IN ('HERBICIDA', 'FUNGICIDA', 'INSECTICIDA')

UNION ALL

SELECT 
    'AGROQUÍMICOS' as categoria,
    COUNT(*) as cantidad
FROM insumos 
WHERE empresa_id = 1 
AND tipo IN ('HERBICIDA', 'FUNGICIDA', 'INSECTICIDA');
