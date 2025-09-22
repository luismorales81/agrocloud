-- Crear vistas para historial de cosechas

-- Crear vista para consultas frecuentes del historial
CREATE VIEW vista_historial_cosechas_completo AS
SELECT 
    hc.id,
    hc.lote_id,
    l.nombre AS lote_nombre,
    hc.cultivo_id,
    c.nombre AS cultivo_nombre,
    c.variedad AS cultivo_variedad,
    hc.fecha_siembra,
    hc.fecha_cosecha,
    DATEDIFF(hc.fecha_cosecha, hc.fecha_siembra) AS dias_ciclo,
    hc.superficie_hectareas,
    hc.cantidad_cosechada,
    hc.unidad_cosecha,
    hc.rendimiento_real,
    hc.rendimiento_esperado,
    CASE 
        WHEN hc.rendimiento_esperado > 0 THEN 
            ROUND((hc.rendimiento_real / hc.rendimiento_esperado) * 100, 2)
        ELSE 0 
    END AS porcentaje_cumplimiento,
    hc.humedad_cosecha,
    hc.variedad_semilla,
    hc.observaciones,
    hc.estado_suelo,
    hc.requiere_descanso,
    hc.dias_descanso_recomendados,
    hc.fecha_creacion,
    hc.fecha_actualizacion,
    hc.usuario_id,
    u.email AS usuario_email
FROM historial_cosechas hc
JOIN lotes l ON hc.lote_id = l.id
JOIN cultivos c ON hc.cultivo_id = c.id
JOIN usuarios u ON hc.usuario_id = u.id;

-- Crear vista para estadísticas de rendimiento por cultivo
CREATE VIEW vista_estadisticas_rendimiento_cultivo AS
SELECT 
    c.id AS cultivo_id,
    c.nombre AS cultivo_nombre,
    COUNT(hc.id) AS total_cosechas,
    AVG(hc.rendimiento_real) AS rendimiento_promedio,
    MIN(hc.rendimiento_real) AS rendimiento_minimo,
    MAX(hc.rendimiento_real) AS rendimiento_maximo,
    AVG(CASE 
        WHEN hc.rendimiento_esperado > 0 THEN 
            (hc.rendimiento_real / hc.rendimiento_esperado) * 100
        ELSE 0 
    END) AS cumplimiento_promedio,
    SUM(hc.superficie_hectareas) AS superficie_total_cosechada,
    SUM(hc.cantidad_cosechada) AS cantidad_total_cosechada
FROM cultivos c
LEFT JOIN historial_cosechas hc ON c.id = hc.cultivo_id
GROUP BY c.id, c.nombre;

-- Crear vista para lotes que requieren descanso
CREATE VIEW vista_lotes_requieren_descanso AS
SELECT 
    l.id AS lote_id,
    l.nombre AS lote_nombre,
    hc.fecha_cosecha,
    hc.estado_suelo,
    hc.dias_descanso_recomendados,
    DATEDIFF(CURDATE(), hc.fecha_cosecha) AS dias_desde_cosecha,
    CASE 
        WHEN DATEDIFF(CURDATE(), hc.fecha_cosecha) >= hc.dias_descanso_recomendados THEN 'LISTO_PARA_SIEMBRA'
        ELSE 'EN_DESCANSO'
    END AS estado_lote
FROM lotes l
JOIN historial_cosechas hc ON l.id = hc.lote_id
WHERE hc.requiere_descanso = TRUE
AND hc.fecha_cosecha = (
    SELECT MAX(fecha_cosecha) 
    FROM historial_cosechas hc2 
    WHERE hc2.lote_id = l.id
);
