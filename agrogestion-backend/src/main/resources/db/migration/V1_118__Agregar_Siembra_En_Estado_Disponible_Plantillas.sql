-- V1_118__Agregar_Siembra_En_Estado_Disponible_Plantillas.sql
-- Permite que lotes en estado "Disponible" sin tipo de cultivo asignado (aún no sembrados)
-- muestren la tarea "Siembra" usando el fallback por plantillas.
-- Así el usuario puede registrar Siembra en un lote disponible sin tener que elegir antes el cultivo.

INSERT INTO cultivo_tareas_por_estado (tipo_cultivo_id, empresa_id, estado_id, tipo_labor, nombre_tarea, descripcion, es_obligatoria, orden, activo)
SELECT 
    e.tipo_cultivo_id,
    NULL,
    e.id,
    'SIEMBRA',
    'Siembra',
    'Plantación del cultivo (tipo se define al registrar la labor)',
    TRUE,
    10,
    TRUE
FROM cultivo_estados_lote e
INNER JOIN cultivo_tipos_cultivo tc ON tc.id = e.tipo_cultivo_id
WHERE e.nombre = 'Disponible'
  AND e.empresa_id IS NULL
  AND tc.nombre IN ('Soja', 'Maíz', 'Trigo', 'Girasol', 'Sorgo')
  AND NOT EXISTS (
    SELECT 1 FROM cultivo_tareas_por_estado t
    WHERE t.estado_id = e.id AND t.tipo_labor = 'SIEMBRA'
  );
