-- V1_117__Insertar_Estados_Y_Tareas_Maiz_Trigo_Girasol_Sorgo.sql
-- Carga estados, transiciones y tareas para Maíz, Trigo, Girasol y Sorgo (plantillas globales).
-- Soja ya tiene datos en V1_105. Con esto los 5 tipos de cultivo quedan con configuración lista.

-- ========== ESTADOS (mismo flujo que Soja para los 4 tipos) ==========
INSERT IGNORE INTO cultivo_estados_lote (tipo_cultivo_id, empresa_id, nombre, descripcion, color, icono, orden, es_estado_inicial, es_estado_final, activo)
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
    UNION ALL SELECT 5, 'R3', 'Inicio de llenado de granos (R3)', '#f59e0b', '🌾', FALSE, FALSE
    UNION ALL SELECT 6, 'R6', 'Llenado completo de granos (R6)', '#ef4444', '🌽', FALSE, FALSE
    UNION ALL SELECT 7, 'Listo para Cosecha', 'Cultivo listo para ser cosechado', '#f97316', '📦', FALSE, FALSE
    UNION ALL SELECT 8, 'Cosechado', 'Cosecha completada', '#6b7280', '✅', FALSE, TRUE
) estados
CROSS JOIN cultivo_tipos_cultivo tc
WHERE tc.nombre IN ('Maíz', 'Trigo', 'Girasol', 'Sorgo');

-- ========== TRANSICIONES (mismo flujo: lineal + Cosechado -> Disponible) ==========
INSERT IGNORE INTO cultivo_transiciones_estado (tipo_cultivo_id, empresa_id, estado_origen_id, estado_destino_id, requiere_motivo, activo)
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
WHERE tc.nombre IN ('Maíz', 'Trigo', 'Girasol', 'Sorgo')
  AND eo.tipo_cultivo_id = tc.id
  AND ed.tipo_cultivo_id = tc.id
  AND (
    (eo.nombre = 'Disponible' AND ed.nombre = 'Preparado') OR
    (eo.nombre = 'Preparado' AND ed.nombre = 'Sembrado') OR
    (eo.nombre = 'Sembrado' AND ed.nombre = 'Emergencia') OR
    (eo.nombre = 'Emergencia' AND ed.nombre = 'R3') OR
    (eo.nombre = 'R3' AND ed.nombre = 'R6') OR
    (eo.nombre = 'R6' AND ed.nombre = 'Listo para Cosecha') OR
    (eo.nombre = 'Listo para Cosecha' AND ed.nombre = 'Cosechado') OR
    (eo.nombre = 'Cosechado' AND ed.nombre = 'Disponible')
  );

-- ========== TAREAS POR ESTADO (mismas labores que Soja) ==========
INSERT IGNORE INTO cultivo_tareas_por_estado (tipo_cultivo_id, empresa_id, estado_id, tipo_labor, nombre_tarea, descripcion, es_obligatoria, orden, activo)
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
    UNION ALL SELECT 'Disponible', 'MANTENIMIENTO', 'Rastra', 'Nivelación del suelo', FALSE, 2
    UNION ALL SELECT 'Preparado', 'SIEMBRA', 'Siembra', 'Plantación del cultivo', TRUE, 1
    UNION ALL SELECT 'Sembrado', 'RIEGO', 'Riego', 'Aplicación de agua', FALSE, 1
    UNION ALL SELECT 'Sembrado', 'FERTILIZACION', 'Fertilización', 'Aplicación de nutrientes', FALSE, 2
    UNION ALL SELECT 'Emergencia', 'RIEGO', 'Riego', 'Aplicación de agua', FALSE, 1
    UNION ALL SELECT 'Emergencia', 'CONTROL_MALEZAS', 'Control de Malezas', 'Aplicación de herbicidas', FALSE, 2
    UNION ALL SELECT 'R3', 'FERTILIZACION', 'Fertilización', 'Aplicación de nutrientes', FALSE, 1
    UNION ALL SELECT 'R3', 'RIEGO', 'Riego', 'Aplicación de agua', FALSE, 2
    UNION ALL SELECT 'R6', 'RIEGO', 'Riego', 'Aplicación de agua', FALSE, 1
    UNION ALL SELECT 'R6', 'CONTROL_PLAGAS', 'Control de Plagas', 'Aplicación de insecticidas', FALSE, 2
    UNION ALL SELECT 'Listo para Cosecha', 'COSECHA', 'Cosecha', 'Recolección del cultivo', TRUE, 1
    UNION ALL SELECT 'Cosechado', 'MANTENIMIENTO', 'Arado', 'Preparación para nuevo ciclo', FALSE, 1
) tareas
WHERE tc.nombre IN ('Maíz', 'Trigo', 'Girasol', 'Sorgo')
  AND e.tipo_cultivo_id = tc.id
  AND e.nombre = tareas.estado;
