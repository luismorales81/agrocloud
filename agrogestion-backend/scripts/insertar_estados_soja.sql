-- Insertar estados predeterminados para Soja
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
    UNION ALL SELECT 5, 'R3', 'Inicio de llenado de granos (R3)', '#f59e0b', '🌾', FALSE, FALSE
    UNION ALL SELECT 6, 'R6', 'Llenado completo de granos (R6)', '#ef4444', '🌽', FALSE, FALSE
    UNION ALL SELECT 7, 'Listo para Cosecha', 'Cultivo listo para ser cosechado', '#f97316', '📦', FALSE, FALSE
    UNION ALL SELECT 8, 'Cosechado', 'Cosecha completada', '#6b7280', '✅', FALSE, TRUE
) estados
CROSS JOIN cultivo_tipos_cultivo tc
WHERE tc.nombre = 'Soja';













