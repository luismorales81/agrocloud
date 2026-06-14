-- Insertar tareas predeterminadas para estados de Soja (con verificación de duplicados)
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
WHERE tc.nombre = 'Soja'
  AND e.tipo_cultivo_id = tc.id
  AND e.nombre = tareas.estado
  AND NOT EXISTS (
    SELECT 1 FROM cultivo_tareas_por_estado t2 
    WHERE t2.estado_id = e.id 
    AND t2.tipo_labor = tareas.tipo_labor 
    AND t2.activo = TRUE
  );













