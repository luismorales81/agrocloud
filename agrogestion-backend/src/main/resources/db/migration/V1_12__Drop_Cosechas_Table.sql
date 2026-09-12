-- Migración V1_12: Eliminar tabla cosechas duplicada (defensiva)
-- Si no existe `cosechas`, solo se omite la migración de datos.

SET @esquema = DATABASE();

SET @tabla_historial = (
    SELECT CASE
        WHEN EXISTS (SELECT 1 FROM information_schema.tables t WHERE t.table_schema = @esquema AND t.table_name = 'cultivo_historial_cosechas')
            THEN 'cultivo_historial_cosechas'
        WHEN EXISTS (SELECT 1 FROM information_schema.tables t WHERE t.table_schema = @esquema AND t.table_name = 'historial_cosechas')
            THEN 'historial_cosechas'
        ELSE NULL
    END
);

SET @tabla_lotes = (
    SELECT CASE
        WHEN EXISTS (SELECT 1 FROM information_schema.tables t WHERE t.table_schema = @esquema AND t.table_name = 'cultivo_lotes')
            THEN 'cultivo_lotes'
        WHEN EXISTS (SELECT 1 FROM information_schema.tables t WHERE t.table_schema = @esquema AND t.table_name = 'lotes')
            THEN 'lotes'
        ELSE NULL
    END
);

SET @existe_cosechas = (
    SELECT COUNT(*) FROM information_schema.tables
    WHERE table_schema = @esquema AND table_name = 'cosechas'
);

SET @sql = IF(
    @existe_cosechas = 0 OR @tabla_historial IS NULL,
    'SELECT ''Omitiendo migración cosechas → historial (tabla cosechas o historial ausente)'' AS mensaje',
    CONCAT(
        'INSERT INTO ', @tabla_historial, ' (',
        'lote_id, cultivo_id, fecha_siembra, fecha_cosecha, superficie_hectareas, cantidad_cosechada, ',
        'unidad_cosecha, rendimiento_real, rendimiento_esperado, observaciones, estado_suelo, ',
        'requiere_descanso, dias_descanso_recomendados, usuario_id, fecha_creacion, fecha_actualizacion) ',
        'SELECT c.lote_id, c.cultivo_id, ',
        'COALESCE(l.fecha_siembra, DATE_SUB(c.fecha_cosecha, INTERVAL 120 DAY)), c.fecha_cosecha, ',
        'COALESCE(l.area_hectareas, 0), COALESCE(c.cantidad_toneladas, 0), ''ton'', ',
        'CASE WHEN l.area_hectareas > 0 THEN c.cantidad_toneladas / l.area_hectareas ELSE 0 END, ',
        'NULL, c.observaciones, ''BUENO'', FALSE, 0, c.usuario_id, c.created_at, c.updated_at ',
        'FROM cosechas c LEFT JOIN ', @tabla_lotes, ' l ON c.lote_id = l.id ',
        'WHERE NOT EXISTS (SELECT 1 FROM ', @tabla_historial, ' hc ',
        'WHERE hc.lote_id = c.lote_id AND hc.fecha_cosecha = c.fecha_cosecha AND hc.cultivo_id = c.cultivo_id)'
    )
);
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

DROP TABLE IF EXISTS cosechas;

SELECT 'Migración V1_12 cosechas aplicada (modo defensivo)' AS mensaje;
