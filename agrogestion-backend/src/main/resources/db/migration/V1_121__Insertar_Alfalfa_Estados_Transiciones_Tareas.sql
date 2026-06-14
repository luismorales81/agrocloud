-- V1_121__Insertar_Alfalfa_Estados_Transiciones_Tareas.sql
-- Carga el tipo de cultivo Alfalfa (forrajera perenne) con sus estados, transiciones y tareas.
-- Ciclo típico: implantación → cortes sucesivos → dormancia → levantado para rotar.

-- ========== 1. TIPO DE CULTIVO ALFALFA ==========
INSERT INTO cultivo_tipos_cultivo (nombre, descripcion, es_plantilla, activo)
SELECT 'Alfalfa', 'Cultivo forrajero perenne para heno o pastoreo', TRUE, TRUE
WHERE NOT EXISTS (SELECT 1 FROM cultivo_tipos_cultivo WHERE nombre = 'Alfalfa');

-- ========== 2. ESTADOS PARA ALFALFA ==========
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
    SELECT 1 as orden, 'Disponible' as nombre, 'Lote disponible para implante de alfalfa' as descripcion, '#10b981' as color, '🟢' as icono, TRUE as es_estado_inicial, FALSE as es_estado_final
    UNION ALL SELECT 2, 'Preparado', 'Lote preparado y listo para siembra', '#22c55e', '🟡', FALSE, FALSE
    UNION ALL SELECT 3, 'Sembrado', 'Semilla de alfalfa sembrada', '#3b82f6', '🔵', FALSE, FALSE
    UNION ALL SELECT 4, 'Emergencia', 'Plántulas emergidas', '#8b5cf6', '🌱', FALSE, FALSE
    UNION ALL SELECT 5, 'Establecimiento', 'Primer año: desarrollo inicial del cultivo', '#06b6d4', '🌿', FALSE, FALSE
    UNION ALL SELECT 6, 'Primer Corte', 'Listo para primer corte de heno', '#22c55e', '🥬', FALSE, FALSE
    UNION ALL SELECT 7, 'Rebrote', 'Crecimiento tras el corte', '#84cc16', '🌾', FALSE, FALSE
    UNION ALL SELECT 8, 'Segundo Corte', 'Listo para segundo corte', '#22c55e', '🥬', FALSE, FALSE
    UNION ALL SELECT 9, 'Tercer Corte', 'Listo para tercer corte', '#22c55e', '🥬', FALSE, FALSE
    UNION ALL SELECT 10, 'Dormancia', 'Invierno: cultivo en reposo', '#64748b', '❄️', FALSE, FALSE
    UNION ALL SELECT 11, 'Levantado', 'Cultivo levantado para rotar', '#6b7280', '✅', FALSE, TRUE
) estados
CROSS JOIN cultivo_tipos_cultivo tc
WHERE tc.nombre = 'Alfalfa'
  AND NOT EXISTS (SELECT 1 FROM cultivo_estados_lote WHERE tipo_cultivo_id = tc.id AND nombre = estados.nombre AND empresa_id IS NULL);

-- ========== 3. TRANSICIONES PARA ALFALFA ==========
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
WHERE tc.nombre = 'Alfalfa'
  AND eo.tipo_cultivo_id = tc.id
  AND ed.tipo_cultivo_id = tc.id
  AND eo.empresa_id IS NULL
  AND ed.empresa_id IS NULL
  AND (
    (eo.nombre = 'Disponible' AND ed.nombre = 'Preparado') OR
    (eo.nombre = 'Preparado' AND ed.nombre = 'Sembrado') OR
    (eo.nombre = 'Sembrado' AND ed.nombre = 'Emergencia') OR
    (eo.nombre = 'Emergencia' AND ed.nombre = 'Establecimiento') OR
    (eo.nombre = 'Establecimiento' AND ed.nombre = 'Primer Corte') OR
    (eo.nombre = 'Primer Corte' AND ed.nombre = 'Rebrote') OR
    (eo.nombre = 'Rebrote' AND ed.nombre = 'Segundo Corte') OR
    (eo.nombre = 'Segundo Corte' AND ed.nombre = 'Rebrote') OR
    (eo.nombre = 'Rebrote' AND ed.nombre = 'Tercer Corte') OR
    (eo.nombre = 'Tercer Corte' AND ed.nombre = 'Dormancia') OR
    (eo.nombre = 'Rebrote' AND ed.nombre = 'Dormancia') OR
    (eo.nombre = 'Dormancia' AND ed.nombre = 'Rebrote') OR
    (eo.nombre = 'Dormancia' AND ed.nombre = 'Levantado') OR
    (eo.nombre = 'Levantado' AND ed.nombre = 'Disponible')
  )
  AND NOT EXISTS (
    SELECT 1 FROM cultivo_transiciones_estado t
    WHERE t.tipo_cultivo_id = tc.id AND t.empresa_id IS NULL
      AND t.estado_origen_id = eo.id AND t.estado_destino_id = ed.id
  );

-- ========== 4. TAREAS POR ESTADO PARA ALFALFA ==========
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
    SELECT 'Disponible' as estado, 'MANTENIMIENTO' as tipo_labor, 'Arado y rastra' as nombre_tarea, 'Preparación del suelo para siembra' as descripcion, FALSE as es_obligatoria, 1 as orden
    UNION ALL SELECT 'Disponible', 'SIEMBRA', 'Siembra', 'Implantación de alfalfa (tipo se define al registrar)', TRUE, 10
    UNION ALL SELECT 'Preparado', 'SIEMBRA', 'Siembra', 'Implantación de alfalfa', TRUE, 1
    UNION ALL SELECT 'Sembrado', 'RIEGO', 'Riego', 'Aplicación de agua', FALSE, 1
    UNION ALL SELECT 'Sembrado', 'FERTILIZACION', 'Fertilización', 'Aplicación de nutrientes', FALSE, 2
    UNION ALL SELECT 'Emergencia', 'RIEGO', 'Riego', 'Aplicación de agua', FALSE, 1
    UNION ALL SELECT 'Emergencia', 'CONTROL_MALEZAS', 'Control de Malezas', 'Aplicación de herbicidas', FALSE, 2
    UNION ALL SELECT 'Establecimiento', 'FERTILIZACION', 'Fertilización', 'Aplicación de nutrientes', FALSE, 1
    UNION ALL SELECT 'Establecimiento', 'RIEGO', 'Riego', 'Aplicación de agua', FALSE, 2
    UNION ALL SELECT 'Establecimiento', 'CONTROL_MALEZAS', 'Control de Malezas', 'Manejo de malezas', FALSE, 3
    UNION ALL SELECT 'Primer Corte', 'COSECHA', 'Corte de heno', 'Corte y henificado', TRUE, 1
    UNION ALL SELECT 'Rebrote', 'RIEGO', 'Riego', 'Aplicación de agua', FALSE, 1
    UNION ALL SELECT 'Rebrote', 'FERTILIZACION', 'Fertilización', 'Aplicación de nutrientes post-corte', FALSE, 2
    UNION ALL SELECT 'Segundo Corte', 'COSECHA', 'Corte de heno', 'Corte y henificado', TRUE, 1
    UNION ALL SELECT 'Tercer Corte', 'COSECHA', 'Corte de heno', 'Corte y henificado', TRUE, 1
    UNION ALL SELECT 'Dormancia', 'MANTENIMIENTO', 'Poda de residuos', 'Limpieza de rastrojos', FALSE, 1
    UNION ALL SELECT 'Levantado', 'MANTENIMIENTO', 'Arado', 'Preparación para rotar', FALSE, 1
) tareas
WHERE tc.nombre = 'Alfalfa'
  AND e.tipo_cultivo_id = tc.id
  AND e.empresa_id IS NULL
  AND e.nombre = tareas.estado
  AND NOT EXISTS (
    SELECT 1 FROM cultivo_tareas_por_estado t
    WHERE t.estado_id = e.id AND t.tipo_labor = tareas.tipo_labor
  );
