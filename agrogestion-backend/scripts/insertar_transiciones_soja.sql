-- Insertar transiciones predeterminadas para Soja
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
WHERE tc.nombre = 'Soja'
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













