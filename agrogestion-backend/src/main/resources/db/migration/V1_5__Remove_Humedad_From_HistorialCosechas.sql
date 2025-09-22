-- Eliminar columna humedad_cosecha de la tabla historial_cosechas
-- Esta migración elimina todas las referencias a humedad del sistema de cosechas

-- Eliminar la columna humedad_cosecha de la tabla historial_cosechas
ALTER TABLE historial_cosechas DROP COLUMN IF EXISTS humedad_cosecha;

-- Eliminar la restricción de validación de humedad si existe
ALTER TABLE historial_cosechas DROP CONSTRAINT IF EXISTS chk_historial_humedad_valida;

-- Actualizar los comentarios de la tabla para reflejar los cambios
ALTER TABLE historial_cosechas 
MODIFY COLUMN rendimiento_real DECIMAL(10,2) COMMENT 'Rendimiento real obtenido en la cosecha',
MODIFY COLUMN rendimiento_esperado DECIMAL(10,2) COMMENT 'Rendimiento esperado según el cultivo',
MODIFY COLUMN porcentaje_cumplimiento DECIMAL(5,2) COMMENT 'Porcentaje de cumplimiento del rendimiento esperado';

-- Actualizar la vista de historial de cosechas para eliminar referencias a humedad
DROP VIEW IF EXISTS vista_historial_cosechas_completo;

CREATE VIEW vista_historial_cosechas_completo AS
SELECT 
    hc.id,
    hc.lote_id,
    p.nombre AS nombre_lote,
    hc.cultivo_id,
    c.nombre AS nombre_cultivo,
    c.variedad AS variedad_semilla,
    hc.fecha_siembra,
    hc.fecha_cosecha,
    hc.superficie_hectareas,
    hc.cantidad_cosechada,
    hc.unidad_cosecha,
    hc.rendimiento_real,
    hc.rendimiento_esperado,
    hc.porcentaje_cumplimiento,
    hc.observaciones,
    hc.estado_suelo,
    hc.requiere_descanso,
    hc.dias_descanso_recomendados,
    hc.usuario_id,
    u.email AS usuario_email,
    f.nombre AS nombre_campo,
    f.ubicacion AS ubicacion_campo
FROM historial_cosechas hc
LEFT JOIN lotes p ON hc.lote_id = p.id
LEFT JOIN cultivos c ON hc.cultivo_id = c.id
LEFT JOIN usuarios u ON hc.usuario_id = u.id
LEFT JOIN campos f ON p.campo_id = f.id
WHERE hc.activo = true;

-- Actualizar la vista de estadísticas de cosechas
DROP VIEW IF EXISTS vista_estadisticas_cosechas;

CREATE VIEW vista_estadisticas_cosechas AS
SELECT 
    c.nombre AS cultivo,
    COUNT(*) AS total_cosechas,
    AVG(hc.rendimiento_real) AS rendimiento_promedio,
    AVG(hc.porcentaje_cumplimiento) AS cumplimiento_promedio,
    SUM(hc.cantidad_cosechada) AS cantidad_total_cosechada,
    AVG(hc.superficie_hectareas) AS superficie_promedio
FROM historial_cosechas hc
JOIN cultivos c ON hc.cultivo_id = c.id
WHERE hc.activo = true
GROUP BY c.id, c.nombre
ORDER BY total_cosechas DESC;
