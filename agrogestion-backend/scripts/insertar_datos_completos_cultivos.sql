-- Script completo para insertar estados, transiciones y tareas para todos los cultivos
-- Cultivos: Soja, Maíz, Trigo, Girasol, Sorgo

USE agrocloud;

-- ============================================================================
-- MAÍZ
-- ============================================================================

-- Insertar estados para Maíz
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
    UNION ALL SELECT 5, 'V6', 'Seis hojas visibles (V6)', '#f59e0b', '🌾', FALSE, FALSE
    UNION ALL SELECT 6, 'R1', 'Inicio de floración (R1)', '#ef4444', '🌽', FALSE, FALSE
    UNION ALL SELECT 7, 'R5', 'Llenado de granos (R5)', '#f97316', '🌾', FALSE, FALSE
    UNION ALL SELECT 8, 'Listo para Cosecha', 'Cultivo listo para ser cosechado', '#eab308', '📦', FALSE, FALSE
    UNION ALL SELECT 9, 'Cosechado', 'Cosecha completada', '#6b7280', '✅', FALSE, TRUE
) estados
CROSS JOIN cultivo_tipos_cultivo tc
WHERE tc.nombre = 'Maíz'
  AND NOT EXISTS (SELECT 1 FROM cultivo_estados_lote WHERE tipo_cultivo_id = tc.id AND nombre = estados.nombre AND empresa_id IS NULL);

-- Insertar transiciones para Maíz
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
WHERE tc.nombre = 'Maíz'
  AND eo.tipo_cultivo_id = tc.id
  AND ed.tipo_cultivo_id = tc.id
  AND eo.empresa_id IS NULL
  AND ed.empresa_id IS NULL
  AND (
    (eo.nombre = 'Disponible' AND ed.nombre = 'Preparado') OR
    (eo.nombre = 'Preparado' AND ed.nombre = 'Sembrado') OR
    (eo.nombre = 'Sembrado' AND ed.nombre = 'Emergencia') OR
    (eo.nombre = 'Emergencia' AND ed.nombre = 'V6') OR
    (eo.nombre = 'V6' AND ed.nombre = 'R1') OR
    (eo.nombre = 'R1' AND ed.nombre = 'R5') OR
    (eo.nombre = 'R5' AND ed.nombre = 'Listo para Cosecha') OR
    (eo.nombre = 'Listo para Cosecha' AND ed.nombre = 'Cosechado') OR
    (eo.nombre = 'Cosechado' AND ed.nombre = 'Disponible')
  )
  AND NOT EXISTS (SELECT 1 FROM cultivo_transiciones_estado WHERE estado_origen_id = eo.id AND estado_destino_id = ed.id);

-- Insertar tareas para Maíz
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
    SELECT 'Disponible' as estado, 'MANTENIMIENTO' as tipo_labor, 'Arado' as nombre_tarea, 'Preparación profunda del suelo' as descripcion, FALSE as es_obligatoria, 1 as orden
    UNION ALL SELECT 'Disponible', 'OTROS', 'Rastra', 'Nivelación del suelo', FALSE, 2
    UNION ALL SELECT 'Preparado', 'SIEMBRA', 'Siembra', 'Plantación del cultivo', TRUE, 1
    UNION ALL SELECT 'Sembrado', 'RIEGO', 'Riego', 'Aplicación de agua', FALSE, 1
    UNION ALL SELECT 'Sembrado', 'FERTILIZACION', 'Fertilización', 'Aplicación de nutrientes', FALSE, 2
    UNION ALL SELECT 'Emergencia', 'RIEGO', 'Riego', 'Aplicación de agua', FALSE, 1
    UNION ALL SELECT 'Emergencia', 'CONTROL_MALEZAS', 'Control de Malezas', 'Aplicación de herbicidas', FALSE, 2
    UNION ALL SELECT 'V6', 'FERTILIZACION', 'Fertilización', 'Aplicación de nutrientes', FALSE, 1
    UNION ALL SELECT 'V6', 'RIEGO', 'Riego', 'Aplicación de agua', FALSE, 2
    UNION ALL SELECT 'R1', 'RIEGO', 'Riego', 'Aplicación de agua (crítico)', FALSE, 1
    UNION ALL SELECT 'R1', 'FERTILIZACION', 'Fertilización', 'Aplicación de nutrientes', FALSE, 2
    UNION ALL SELECT 'R5', 'RIEGO', 'Riego', 'Aplicación de agua', FALSE, 1
    UNION ALL SELECT 'R5', 'CONTROL_PLAGAS', 'Control de Plagas', 'Aplicación de insecticidas', FALSE, 2
    UNION ALL SELECT 'Listo para Cosecha', 'COSECHA', 'Cosecha', 'Recolección del cultivo', TRUE, 1
    UNION ALL SELECT 'Cosechado', 'MANTENIMIENTO', 'Arado', 'Preparación para nuevo ciclo', FALSE, 1
) tareas
WHERE tc.nombre = 'Maíz'
  AND e.tipo_cultivo_id = tc.id
  AND e.nombre = tareas.estado
  AND e.empresa_id IS NULL
  AND NOT EXISTS (SELECT 1 FROM cultivo_tareas_por_estado WHERE estado_id = e.id AND tipo_labor = tareas.tipo_labor AND tipo_cultivo_id = tc.id);

-- ============================================================================
-- TRIGO
-- ============================================================================

-- Insertar estados para Trigo
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
    UNION ALL SELECT 5, 'Macollaje', 'Etapa de macollaje', '#f59e0b', '🌾', FALSE, FALSE
    UNION ALL SELECT 6, 'Encañado', 'Etapa de encañado', '#ef4444', '🌾', FALSE, FALSE
    UNION ALL SELECT 7, 'Espigado', 'Etapa de espigado', '#f97316', '🌾', FALSE, FALSE
    UNION ALL SELECT 8, 'Llenado de Granos', 'Llenado de granos', '#eab308', '🌾', FALSE, FALSE
    UNION ALL SELECT 9, 'Listo para Cosecha', 'Cultivo listo para ser cosechado', '#fbbf24', '📦', FALSE, FALSE
    UNION ALL SELECT 10, 'Cosechado', 'Cosecha completada', '#6b7280', '✅', FALSE, TRUE
) estados
CROSS JOIN cultivo_tipos_cultivo tc
WHERE tc.nombre = 'Trigo'
  AND NOT EXISTS (SELECT 1 FROM cultivo_estados_lote WHERE tipo_cultivo_id = tc.id AND nombre = estados.nombre AND empresa_id IS NULL);

-- Insertar transiciones para Trigo
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
WHERE tc.nombre = 'Trigo'
  AND eo.tipo_cultivo_id = tc.id
  AND ed.tipo_cultivo_id = tc.id
  AND eo.empresa_id IS NULL
  AND ed.empresa_id IS NULL
  AND (
    (eo.nombre = 'Disponible' AND ed.nombre = 'Preparado') OR
    (eo.nombre = 'Preparado' AND ed.nombre = 'Sembrado') OR
    (eo.nombre = 'Sembrado' AND ed.nombre = 'Emergencia') OR
    (eo.nombre = 'Emergencia' AND ed.nombre = 'Macollaje') OR
    (eo.nombre = 'Macollaje' AND ed.nombre = 'Encañado') OR
    (eo.nombre = 'Encañado' AND ed.nombre = 'Espigado') OR
    (eo.nombre = 'Espigado' AND ed.nombre = 'Llenado de Granos') OR
    (eo.nombre = 'Llenado de Granos' AND ed.nombre = 'Listo para Cosecha') OR
    (eo.nombre = 'Listo para Cosecha' AND ed.nombre = 'Cosechado') OR
    (eo.nombre = 'Cosechado' AND ed.nombre = 'Disponible')
  )
  AND NOT EXISTS (SELECT 1 FROM cultivo_transiciones_estado WHERE estado_origen_id = eo.id AND estado_destino_id = ed.id);

-- Insertar tareas para Trigo
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
    SELECT 'Disponible' as estado, 'MANTENIMIENTO' as tipo_labor, 'Arado' as nombre_tarea, 'Preparación profunda del suelo' as descripcion, FALSE as es_obligatoria, 1 as orden
    UNION ALL SELECT 'Disponible', 'OTROS', 'Rastra', 'Nivelación del suelo', FALSE, 2
    UNION ALL SELECT 'Preparado', 'SIEMBRA', 'Siembra', 'Plantación del cultivo', TRUE, 1
    UNION ALL SELECT 'Sembrado', 'RIEGO', 'Riego', 'Aplicación de agua', FALSE, 1
    UNION ALL SELECT 'Sembrado', 'FERTILIZACION', 'Fertilización', 'Aplicación de nutrientes', FALSE, 2
    UNION ALL SELECT 'Emergencia', 'RIEGO', 'Riego', 'Aplicación de agua', FALSE, 1
    UNION ALL SELECT 'Emergencia', 'CONTROL_MALEZAS', 'Control de Malezas', 'Aplicación de herbicidas', FALSE, 2
    UNION ALL SELECT 'Macollaje', 'FERTILIZACION', 'Fertilización', 'Aplicación de nutrientes', FALSE, 1
    UNION ALL SELECT 'Macollaje', 'RIEGO', 'Riego', 'Aplicación de agua', FALSE, 2
    UNION ALL SELECT 'Encañado', 'FERTILIZACION', 'Fertilización', 'Aplicación de nutrientes', FALSE, 1
    UNION ALL SELECT 'Encañado', 'RIEGO', 'Riego', 'Aplicación de agua', FALSE, 2
    UNION ALL SELECT 'Espigado', 'RIEGO', 'Riego', 'Aplicación de agua', FALSE, 1
    UNION ALL SELECT 'Espigado', 'CONTROL_PLAGAS', 'Control de Plagas', 'Aplicación de insecticidas', FALSE, 2
    UNION ALL SELECT 'Llenado de Granos', 'RIEGO', 'Riego', 'Aplicación de agua', FALSE, 1
    UNION ALL SELECT 'Llenado de Granos', 'CONTROL_PLAGAS', 'Control de Plagas', 'Aplicación de insecticidas', FALSE, 2
    UNION ALL SELECT 'Listo para Cosecha', 'COSECHA', 'Cosecha', 'Recolección del cultivo', TRUE, 1
    UNION ALL SELECT 'Cosechado', 'MANTENIMIENTO', 'Arado', 'Preparación para nuevo ciclo', FALSE, 1
) tareas
WHERE tc.nombre = 'Trigo'
  AND e.tipo_cultivo_id = tc.id
  AND e.nombre = tareas.estado
  AND e.empresa_id IS NULL
  AND NOT EXISTS (SELECT 1 FROM cultivo_tareas_por_estado WHERE estado_id = e.id AND tipo_labor = tareas.tipo_labor AND tipo_cultivo_id = tc.id);

-- ============================================================================
-- GIRASOL
-- ============================================================================

-- Insertar estados para Girasol
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
    UNION ALL SELECT 5, 'V4', 'Cuatro hojas visibles (V4)', '#f59e0b', '🌻', FALSE, FALSE
    UNION ALL SELECT 6, 'R1', 'Inicio de floración (R1)', '#ef4444', '🌻', FALSE, FALSE
    UNION ALL SELECT 7, 'R5', 'Llenado de granos (R5)', '#f97316', '🌻', FALSE, FALSE
    UNION ALL SELECT 8, 'R9', 'Madurez fisiológica (R9)', '#eab308', '🌻', FALSE, FALSE
    UNION ALL SELECT 9, 'Listo para Cosecha', 'Cultivo listo para ser cosechado', '#fbbf24', '📦', FALSE, FALSE
    UNION ALL SELECT 10, 'Cosechado', 'Cosecha completada', '#6b7280', '✅', FALSE, TRUE
) estados
CROSS JOIN cultivo_tipos_cultivo tc
WHERE tc.nombre = 'Girasol'
  AND NOT EXISTS (SELECT 1 FROM cultivo_estados_lote WHERE tipo_cultivo_id = tc.id AND nombre = estados.nombre AND empresa_id IS NULL);

-- Insertar transiciones para Girasol
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
WHERE tc.nombre = 'Girasol'
  AND eo.tipo_cultivo_id = tc.id
  AND ed.tipo_cultivo_id = tc.id
  AND eo.empresa_id IS NULL
  AND ed.empresa_id IS NULL
  AND (
    (eo.nombre = 'Disponible' AND ed.nombre = 'Preparado') OR
    (eo.nombre = 'Preparado' AND ed.nombre = 'Sembrado') OR
    (eo.nombre = 'Sembrado' AND ed.nombre = 'Emergencia') OR
    (eo.nombre = 'Emergencia' AND ed.nombre = 'V4') OR
    (eo.nombre = 'V4' AND ed.nombre = 'R1') OR
    (eo.nombre = 'R1' AND ed.nombre = 'R5') OR
    (eo.nombre = 'R5' AND ed.nombre = 'R9') OR
    (eo.nombre = 'R9' AND ed.nombre = 'Listo para Cosecha') OR
    (eo.nombre = 'Listo para Cosecha' AND ed.nombre = 'Cosechado') OR
    (eo.nombre = 'Cosechado' AND ed.nombre = 'Disponible')
  )
  AND NOT EXISTS (SELECT 1 FROM cultivo_transiciones_estado WHERE estado_origen_id = eo.id AND estado_destino_id = ed.id);

-- Insertar tareas para Girasol
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
    SELECT 'Disponible' as estado, 'MANTENIMIENTO' as tipo_labor, 'Arado' as nombre_tarea, 'Preparación profunda del suelo' as descripcion, FALSE as es_obligatoria, 1 as orden
    UNION ALL SELECT 'Disponible', 'OTROS', 'Rastra', 'Nivelación del suelo', FALSE, 2
    UNION ALL SELECT 'Preparado', 'SIEMBRA', 'Siembra', 'Plantación del cultivo', TRUE, 1
    UNION ALL SELECT 'Sembrado', 'RIEGO', 'Riego', 'Aplicación de agua', FALSE, 1
    UNION ALL SELECT 'Sembrado', 'FERTILIZACION', 'Fertilización', 'Aplicación de nutrientes', FALSE, 2
    UNION ALL SELECT 'Emergencia', 'RIEGO', 'Riego', 'Aplicación de agua', FALSE, 1
    UNION ALL SELECT 'Emergencia', 'CONTROL_MALEZAS', 'Control de Malezas', 'Aplicación de herbicidas', FALSE, 2
    UNION ALL SELECT 'V4', 'FERTILIZACION', 'Fertilización', 'Aplicación de nutrientes', FALSE, 1
    UNION ALL SELECT 'V4', 'RIEGO', 'Riego', 'Aplicación de agua', FALSE, 2
    UNION ALL SELECT 'R1', 'RIEGO', 'Riego', 'Aplicación de agua (crítico)', FALSE, 1
    UNION ALL SELECT 'R1', 'CONTROL_PLAGAS', 'Control de Plagas', 'Aplicación de insecticidas', FALSE, 2
    UNION ALL SELECT 'R5', 'RIEGO', 'Riego', 'Aplicación de agua', FALSE, 1
    UNION ALL SELECT 'R5', 'CONTROL_PLAGAS', 'Control de Plagas', 'Aplicación de insecticidas', FALSE, 2
    UNION ALL SELECT 'R9', 'RIEGO', 'Riego', 'Aplicación de agua', FALSE, 1
    UNION ALL SELECT 'Listo para Cosecha', 'COSECHA', 'Cosecha', 'Recolección del cultivo', TRUE, 1
    UNION ALL SELECT 'Cosechado', 'MANTENIMIENTO', 'Arado', 'Preparación para nuevo ciclo', FALSE, 1
) tareas
WHERE tc.nombre = 'Girasol'
  AND e.tipo_cultivo_id = tc.id
  AND e.nombre = tareas.estado
  AND e.empresa_id IS NULL
  AND NOT EXISTS (SELECT 1 FROM cultivo_tareas_por_estado WHERE estado_id = e.id AND tipo_labor = tareas.tipo_labor AND tipo_cultivo_id = tc.id);

-- ============================================================================
-- SORGO
-- ============================================================================

-- Insertar estados para Sorgo
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
    UNION ALL SELECT 5, 'V6', 'Seis hojas visibles (V6)', '#f59e0b', '🌾', FALSE, FALSE
    UNION ALL SELECT 6, 'Floración', 'Etapa de floración', '#ef4444', '🌾', FALSE, FALSE
    UNION ALL SELECT 7, 'Llenado de Granos', 'Llenado de granos', '#f97316', '🌾', FALSE, FALSE
    UNION ALL SELECT 8, 'Madurez', 'Madurez fisiológica', '#eab308', '🌾', FALSE, FALSE
    UNION ALL SELECT 9, 'Listo para Cosecha', 'Cultivo listo para ser cosechado', '#fbbf24', '📦', FALSE, FALSE
    UNION ALL SELECT 10, 'Cosechado', 'Cosecha completada', '#6b7280', '✅', FALSE, TRUE
) estados
CROSS JOIN cultivo_tipos_cultivo tc
WHERE tc.nombre = 'Sorgo'
  AND NOT EXISTS (SELECT 1 FROM cultivo_estados_lote WHERE tipo_cultivo_id = tc.id AND nombre = estados.nombre AND empresa_id IS NULL);

-- Insertar transiciones para Sorgo
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
WHERE tc.nombre = 'Sorgo'
  AND eo.tipo_cultivo_id = tc.id
  AND ed.tipo_cultivo_id = tc.id
  AND eo.empresa_id IS NULL
  AND ed.empresa_id IS NULL
  AND (
    (eo.nombre = 'Disponible' AND ed.nombre = 'Preparado') OR
    (eo.nombre = 'Preparado' AND ed.nombre = 'Sembrado') OR
    (eo.nombre = 'Sembrado' AND ed.nombre = 'Emergencia') OR
    (eo.nombre = 'Emergencia' AND ed.nombre = 'V6') OR
    (eo.nombre = 'V6' AND ed.nombre = 'Floración') OR
    (eo.nombre = 'Floración' AND ed.nombre = 'Llenado de Granos') OR
    (eo.nombre = 'Llenado de Granos' AND ed.nombre = 'Madurez') OR
    (eo.nombre = 'Madurez' AND ed.nombre = 'Listo para Cosecha') OR
    (eo.nombre = 'Listo para Cosecha' AND ed.nombre = 'Cosechado') OR
    (eo.nombre = 'Cosechado' AND ed.nombre = 'Disponible')
  )
  AND NOT EXISTS (SELECT 1 FROM cultivo_transiciones_estado WHERE estado_origen_id = eo.id AND estado_destino_id = ed.id);

-- Insertar tareas para Sorgo
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
    SELECT 'Disponible' as estado, 'MANTENIMIENTO' as tipo_labor, 'Arado' as nombre_tarea, 'Preparación profunda del suelo' as descripcion, FALSE as es_obligatoria, 1 as orden
    UNION ALL SELECT 'Disponible', 'OTROS', 'Rastra', 'Nivelación del suelo', FALSE, 2
    UNION ALL SELECT 'Preparado', 'SIEMBRA', 'Siembra', 'Plantación del cultivo', TRUE, 1
    UNION ALL SELECT 'Sembrado', 'RIEGO', 'Riego', 'Aplicación de agua', FALSE, 1
    UNION ALL SELECT 'Sembrado', 'FERTILIZACION', 'Fertilización', 'Aplicación de nutrientes', FALSE, 2
    UNION ALL SELECT 'Emergencia', 'RIEGO', 'Riego', 'Aplicación de agua', FALSE, 1
    UNION ALL SELECT 'Emergencia', 'CONTROL_MALEZAS', 'Control de Malezas', 'Aplicación de herbicidas', FALSE, 2
    UNION ALL SELECT 'V6', 'FERTILIZACION', 'Fertilización', 'Aplicación de nutrientes', FALSE, 1
    UNION ALL SELECT 'V6', 'RIEGO', 'Riego', 'Aplicación de agua', FALSE, 2
    UNION ALL SELECT 'Floración', 'RIEGO', 'Riego', 'Aplicación de agua', FALSE, 1
    UNION ALL SELECT 'Floración', 'CONTROL_PLAGAS', 'Control de Plagas', 'Aplicación de insecticidas', FALSE, 2
    UNION ALL SELECT 'Llenado de Granos', 'RIEGO', 'Riego', 'Aplicación de agua', FALSE, 1
    UNION ALL SELECT 'Llenado de Granos', 'CONTROL_PLAGAS', 'Control de Plagas', 'Aplicación de insecticidas', FALSE, 2
    UNION ALL SELECT 'Madurez', 'RIEGO', 'Riego', 'Aplicación de agua', FALSE, 1
    UNION ALL SELECT 'Listo para Cosecha', 'COSECHA', 'Cosecha', 'Recolección del cultivo', TRUE, 1
    UNION ALL SELECT 'Cosechado', 'MANTENIMIENTO', 'Arado', 'Preparación para nuevo ciclo', FALSE, 1
) tareas
WHERE tc.nombre = 'Sorgo'
  AND e.tipo_cultivo_id = tc.id
  AND e.nombre = tareas.estado
  AND e.empresa_id IS NULL
  AND NOT EXISTS (SELECT 1 FROM cultivo_tareas_por_estado WHERE estado_id = e.id AND tipo_labor = tareas.tipo_labor AND tipo_cultivo_id = tc.id);

-- ============================================================================
-- RESUMEN FINAL
-- ============================================================================

SELECT 
    tc.nombre as cultivo,
    COUNT(DISTINCT e.id) as estados,
    COUNT(DISTINCT t.id) as transiciones,
    COUNT(DISTINCT ta.id) as tareas
FROM cultivo_tipos_cultivo tc
LEFT JOIN cultivo_estados_lote e ON tc.id = e.tipo_cultivo_id AND e.empresa_id IS NULL
LEFT JOIN cultivo_transiciones_estado t ON tc.id = t.tipo_cultivo_id AND t.empresa_id IS NULL
LEFT JOIN cultivo_tareas_por_estado ta ON tc.id = ta.tipo_cultivo_id AND ta.empresa_id IS NULL
WHERE tc.activo = 1
GROUP BY tc.id, tc.nombre
ORDER BY tc.nombre;

