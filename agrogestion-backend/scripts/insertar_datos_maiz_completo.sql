-- Script completo para insertar estados, transiciones y tareas para Maíz
USE agrocloud;

-- ============================================================================
-- ESTADOS PARA MAÍZ
-- ============================================================================
INSERT INTO cultivo_estados_lote (tipo_cultivo_id, empresa_id, nombre, descripcion, color, icono, orden, es_estado_inicial, es_estado_final, activo)
SELECT 
    tc.id,
    NULL,
    estados.nombre,
    estados.descripcion,
    estados.color,
    estados.icono,
    estados.orden,
    estados.es_estado_inicial,
    estados.es_estado_final,
    TRUE
FROM (
    SELECT 1 as orden, 'Disponible' as nombre, 'Lote disponible para comenzar un nuevo ciclo' as descripcion, '#10b981' as color, '🟢' as icono, TRUE as es_estado_inicial, FALSE as es_estado_final
    UNION ALL SELECT 2, 'Preparado', 'Lote preparado y listo para siembra', '#22c55e', '🟡', FALSE, FALSE
    UNION ALL SELECT 3, 'Sembrado', 'Cultivo sembrado y en desarrollo inicial', '#3b82f6', '🔵', FALSE, FALSE
    UNION ALL SELECT 4, 'Emergencia', 'Cultivo en etapa de emergencia', '#8b5cf6', '🌱', FALSE, FALSE
    UNION ALL SELECT 5, 'V6', 'Seis hojas desplegadas (V6)', '#f59e0b', '🌾', FALSE, FALSE
    UNION ALL SELECT 6, 'V12', 'Doce hojas desplegadas (V12)', '#ef4444', '🌽', FALSE, FALSE
    UNION ALL SELECT 7, 'R1', 'Floración (R1)', '#ec4899', '🌸', FALSE, FALSE
    UNION ALL SELECT 8, 'R3', 'Inicio de llenado de granos (R3)', '#f97316', '🌾', FALSE, FALSE
    UNION ALL SELECT 9, 'Listo para Cosecha', 'Cultivo listo para ser cosechado', '#f97316', '📦', FALSE, FALSE
    UNION ALL SELECT 10, 'Cosechado', 'Cosecha completada', '#6b7280', '✅', FALSE, TRUE
) estados
CROSS JOIN cultivo_tipos_cultivo tc
WHERE tc.id = 2;

-- ============================================================================
-- TRANSICIONES PARA MAÍZ
-- ============================================================================
INSERT INTO cultivo_transiciones_estado (tipo_cultivo_id, empresa_id, estado_origen_id, estado_destino_id, requiere_motivo, activo)
SELECT 
    tc.id,
    NULL,
    eo.id,
    ed.id,
    FALSE,
    TRUE
FROM cultivo_tipos_cultivo tc
CROSS JOIN cultivo_estados_lote eo
CROSS JOIN cultivo_estados_lote ed
WHERE tc.id = 2
  AND eo.tipo_cultivo_id = tc.id
  AND ed.tipo_cultivo_id = tc.id
  AND (
    (eo.nombre = 'Disponible' AND ed.nombre = 'Preparado') OR
    (eo.nombre = 'Preparado' AND ed.nombre = 'Sembrado') OR
    (eo.nombre = 'Sembrado' AND ed.nombre = 'Emergencia') OR
    (eo.nombre = 'Emergencia' AND ed.nombre = 'V6') OR
    (eo.nombre = 'V6' AND ed.nombre = 'V12') OR
    (eo.nombre = 'V12' AND ed.nombre = 'R1') OR
    (eo.nombre = 'R1' AND ed.nombre = 'R3') OR
    (eo.nombre = 'R3' AND ed.nombre = 'Listo para Cosecha') OR
    (eo.nombre = 'Listo para Cosecha' AND ed.nombre = 'Cosechado') OR
    (eo.nombre = 'Cosechado' AND ed.nombre = 'Disponible')
  );

-- ============================================================================
-- TAREAS PARA MAÍZ
-- ============================================================================
INSERT INTO cultivo_tareas_por_estado (tipo_cultivo_id, empresa_id, estado_id, tipo_labor, nombre_tarea, descripcion, es_obligatoria, orden, activo)
SELECT 
    tc.id,
    NULL,
    e.id,
    tareas.tipo_labor,
    tareas.nombre_tarea,
    tareas.descripcion,
    tareas.es_obligatoria,
    tareas.orden,
    TRUE
FROM cultivo_tipos_cultivo tc
CROSS JOIN cultivo_estados_lote e
CROSS JOIN (
    -- Disponible
    SELECT 'Disponible' as estado, 'MANTENIMIENTO' as tipo_labor, 'Arado' as nombre_tarea, 'Preparación profunda del suelo' as descripcion, FALSE as es_obligatoria, 1 as orden
    UNION ALL SELECT 'Disponible', 'OTROS', 'Rastra', 'Nivelación del suelo', FALSE, 2
    -- Preparado
    UNION ALL SELECT 'Preparado', 'SIEMBRA', 'Siembra', 'Plantación del cultivo de maíz', TRUE, 1
    -- Sembrado
    UNION ALL SELECT 'Sembrado', 'RIEGO', 'Riego', 'Aplicación de agua', FALSE, 1
    UNION ALL SELECT 'Sembrado', 'FERTILIZACION', 'Fertilización de Arranque', 'Aplicación de nutrientes en siembra', FALSE, 2
    -- Emergencia
    UNION ALL SELECT 'Emergencia', 'RIEGO', 'Riego', 'Aplicación de agua', FALSE, 1
    UNION ALL SELECT 'Emergencia', 'CONTROL_MALEZAS', 'Control de Malezas', 'Aplicación de herbicidas', FALSE, 2
    -- V6
    UNION ALL SELECT 'V6', 'FERTILIZACION', 'Fertilización V6', 'Aplicación de nitrógeno', FALSE, 1
    UNION ALL SELECT 'V6', 'RIEGO', 'Riego', 'Aplicación de agua', FALSE, 2
    -- V12
    UNION ALL SELECT 'V12', 'FERTILIZACION', 'Fertilización V12', 'Aplicación de nitrógeno', FALSE, 1
    UNION ALL SELECT 'V12', 'RIEGO', 'Riego', 'Aplicación de agua', FALSE, 2
    -- R1
    UNION ALL SELECT 'R1', 'RIEGO', 'Riego', 'Aplicación de agua crítica en floración', FALSE, 1
    UNION ALL SELECT 'R1', 'CONTROL_PLAGAS', 'Control de Plagas', 'Aplicación de insecticidas', FALSE, 2
    -- R3
    UNION ALL SELECT 'R3', 'RIEGO', 'Riego', 'Aplicación de agua en llenado de granos', FALSE, 1
    UNION ALL SELECT 'R3', 'CONTROL_PLAGAS', 'Control de Plagas', 'Aplicación de insecticidas', FALSE, 2
    -- Listo para Cosecha
    UNION ALL SELECT 'Listo para Cosecha', 'COSECHA', 'Cosecha', 'Recolección del cultivo', TRUE, 1
    -- Cosechado
    UNION ALL SELECT 'Cosechado', 'MANTENIMIENTO', 'Arado', 'Preparación para nuevo ciclo', FALSE, 1
) tareas
WHERE tc.id = 2
  AND e.tipo_cultivo_id = tc.id
  AND e.nombre = tareas.estado;

-- ============================================================================
-- VERIFICACIÓN
-- ============================================================================
SELECT 
    'Estados insertados' as tipo,
    COUNT(*) as cantidad
FROM cultivo_estados_lote
WHERE tipo_cultivo_id = 2
  AND empresa_id IS NULL
UNION ALL
SELECT 
    'Transiciones insertadas',
    COUNT(*)
FROM cultivo_transiciones_estado
WHERE tipo_cultivo_id = 2
  AND empresa_id IS NULL
UNION ALL
SELECT 
    'Tareas insertadas',
    COUNT(*)
FROM cultivo_tareas_por_estado
WHERE tipo_cultivo_id = 2
  AND empresa_id IS NULL;

-- Mostrar resumen de tareas por estado
SELECT 
    e.nombre as estado,
    COUNT(t.id) as cantidad_tareas
FROM cultivo_estados_lote e
LEFT JOIN cultivo_tareas_por_estado t ON t.estado_id = e.id AND t.tipo_cultivo_id = e.tipo_cultivo_id
WHERE e.tipo_cultivo_id = (SELECT id FROM cultivo_tipos_cultivo WHERE nombre = 'Maíz')
  AND e.empresa_id IS NULL
GROUP BY e.id, e.nombre, e.orden
ORDER BY e.orden;

