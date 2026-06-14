-- Script para insertar tareas predeterminadas para estados de Soja
-- Este script elimina las tareas existentes y las vuelve a insertar para asegurar consistencia

USE agrocloud;

-- Eliminar tareas existentes para Soja (solo las plantillas globales, no las personalizadas por empresa)
DELETE FROM cultivo_tareas_por_estado 
WHERE tipo_cultivo_id = (SELECT id FROM cultivo_tipos_cultivo WHERE nombre = 'Soja')
  AND empresa_id IS NULL;

-- Insertar tareas predeterminadas para estados de Soja
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
WHERE tc.nombre = 'Soja'
  AND e.tipo_cultivo_id = tc.id
  AND e.nombre = tareas.estado;

-- Verificar las tareas insertadas
SELECT 
    e.nombre as estado,
    t.tipo_labor,
    t.nombre_tarea,
    t.descripcion,
    t.es_obligatoria,
    t.orden
FROM cultivo_tareas_por_estado t
JOIN cultivo_estados_lote e ON t.estado_id = e.id
WHERE t.tipo_cultivo_id = (SELECT id FROM cultivo_tipos_cultivo WHERE nombre = 'Soja')
  AND t.empresa_id IS NULL
ORDER BY e.orden, t.orden;

SELECT CONCAT('Total de tareas insertadas: ', COUNT(*)) as resultado
FROM cultivo_tareas_por_estado
WHERE tipo_cultivo_id = (SELECT id FROM cultivo_tipos_cultivo WHERE nombre = 'Soja')
  AND empresa_id IS NULL;

