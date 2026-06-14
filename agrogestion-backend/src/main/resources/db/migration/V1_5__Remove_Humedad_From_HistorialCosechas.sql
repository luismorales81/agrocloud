-- Quitar humedad_cosecha del historial de cosechas (nombre de tabla segun V1_4 o V1_99).
-- Compatible: historial_cosechas (antes del renombrado) o cultivo_historial_cosechas (despues de V1_99).
-- Si ninguna tabla existe aun (V1_4 pendiente), la migracion no hace cambios de esquema.

SET @esquema = DATABASE();
SET @tabla_historial = (
    SELECT CASE
        WHEN EXISTS (
            SELECT 1 FROM information_schema.tables t
            WHERE t.table_schema = @esquema AND t.table_name = 'cultivo_historial_cosechas'
        ) THEN 'cultivo_historial_cosechas'
        WHEN EXISTS (
            SELECT 1 FROM information_schema.tables t
            WHERE t.table_schema = @esquema AND t.table_name = 'historial_cosechas'
        ) THEN 'historial_cosechas'
        ELSE NULL
    END
);

-- Quitar columna humedad_cosecha si existe
SET @consulta = IF(
    @tabla_historial IS NULL,
    'SELECT 1',
    IF(
        (SELECT COUNT(*) FROM information_schema.COLUMNS
         WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = @tabla_historial AND COLUMN_NAME = 'humedad_cosecha') > 0,
        CONCAT('ALTER TABLE ', @tabla_historial, ' DROP COLUMN humedad_cosecha'),
        'SELECT 1'
    )
);
PREPARE sentencia FROM @consulta;
EXECUTE sentencia;
DEALLOCATE PREPARE sentencia;

-- Quitar check de humedad si existe
SET @consulta = IF(
    @tabla_historial IS NULL,
    'SELECT 1',
    IF(
        (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
         WHERE CONSTRAINT_SCHEMA = @esquema AND TABLE_NAME = @tabla_historial
           AND CONSTRAINT_NAME = 'chk_historial_humedad_valida' AND CONSTRAINT_TYPE = 'CHECK') > 0,
        CONCAT('ALTER TABLE ', @tabla_historial, ' DROP CHECK chk_historial_humedad_valida'),
        'SELECT 1'
    )
);
PREPARE sentencia FROM @consulta;
EXECUTE sentencia;
DEALLOCATE PREPARE sentencia;

-- Comentarios en columnas de rendimiento (solo si la tabla ya existe)
SET @consulta = IF(
    @tabla_historial IS NULL,
    'SELECT 1',
    CONCAT(
        'ALTER TABLE ', @tabla_historial,
        ' MODIFY COLUMN rendimiento_real DECIMAL(10,2) COMMENT ''Rendimiento real obtenido en la cosecha'',',
        ' MODIFY COLUMN rendimiento_esperado DECIMAL(10,2) COMMENT ''Rendimiento esperado segun el cultivo'''
    )
);
PREPARE sentencia FROM @consulta;
EXECUTE sentencia;
DEALLOCATE PREPARE sentencia;

-- Solo modificar porcentaje_cumplimiento si existe como columna (no viene en V1_4)
SET @consulta = IF(
    @tabla_historial IS NULL,
    'SELECT 1',
    IF(
        (SELECT COUNT(*) FROM information_schema.COLUMNS
         WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = @tabla_historial AND COLUMN_NAME = 'porcentaje_cumplimiento') > 0,
        CONCAT(
            'ALTER TABLE ', @tabla_historial,
            ' MODIFY COLUMN porcentaje_cumplimiento DECIMAL(5,2) COMMENT ''Porcentaje de cumplimiento del rendimiento esperado'''
        ),
        'SELECT 1'
    )
);
PREPARE sentencia FROM @consulta;
EXECUTE sentencia;
DEALLOCATE PREPARE sentencia;

-- Vistas: solo tocar si existe la tabla (evita DROP sin CREATE cuando V1_4 aun no corrio)
SET @consulta = IF(@tabla_historial IS NULL, 'SELECT 1', 'DROP VIEW IF EXISTS vista_historial_cosechas_completo');
PREPARE sentencia FROM @consulta;
EXECUTE sentencia;
DEALLOCATE PREPARE sentencia;

-- Vista con nombres de tabla segun modulo cultivo (V1_99) o legado
SET @consulta = IF(
    @tabla_historial IS NULL,
    'SELECT 1',
    IF(
        @tabla_historial = 'cultivo_historial_cosechas',
        'CREATE VIEW vista_historial_cosechas_completo AS
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
    CASE WHEN hc.rendimiento_esperado > 0 THEN ROUND((hc.rendimiento_real / hc.rendimiento_esperado) * 100, 2) ELSE 0 END AS porcentaje_cumplimiento,
    hc.observaciones,
    hc.estado_suelo,
    hc.requiere_descanso,
    hc.dias_descanso_recomendados,
    hc.usuario_id,
    u.email AS usuario_email,
    f.nombre AS nombre_campo,
    f.ubicacion AS ubicacion_campo
FROM cultivo_historial_cosechas hc
LEFT JOIN cultivo_lotes p ON hc.lote_id = p.id
LEFT JOIN cultivo_cultivos c ON hc.cultivo_id = c.id
LEFT JOIN usuarios u ON hc.usuario_id = u.id
LEFT JOIN cultivo_campos f ON p.campo_id = f.id',
        'CREATE VIEW vista_historial_cosechas_completo AS
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
    CASE WHEN hc.rendimiento_esperado > 0 THEN ROUND((hc.rendimiento_real / hc.rendimiento_esperado) * 100, 2) ELSE 0 END AS porcentaje_cumplimiento,
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
LEFT JOIN campos f ON p.campo_id = f.id'
    )
);
PREPARE sentencia FROM @consulta;
EXECUTE sentencia;
DEALLOCATE PREPARE sentencia;

SET @consulta = IF(@tabla_historial IS NULL, 'SELECT 1', 'DROP VIEW IF EXISTS vista_estadisticas_cosechas');
PREPARE sentencia FROM @consulta;
EXECUTE sentencia;
DEALLOCATE PREPARE sentencia;

SET @consulta = IF(
    @tabla_historial IS NULL,
    'SELECT 1',
    IF(
        @tabla_historial = 'cultivo_historial_cosechas',
        'CREATE VIEW vista_estadisticas_cosechas AS
SELECT
    c.nombre AS cultivo,
    COUNT(*) AS total_cosechas,
    AVG(hc.rendimiento_real) AS rendimiento_promedio,
    AVG(CASE WHEN hc.rendimiento_esperado > 0 THEN (hc.rendimiento_real / hc.rendimiento_esperado) * 100 ELSE 0 END) AS cumplimiento_promedio,
    SUM(hc.cantidad_cosechada) AS cantidad_total_cosechada,
    AVG(hc.superficie_hectareas) AS superficie_promedio
FROM cultivo_historial_cosechas hc
JOIN cultivo_cultivos c ON hc.cultivo_id = c.id
GROUP BY c.id, c.nombre',
        'CREATE VIEW vista_estadisticas_cosechas AS
SELECT
    c.nombre AS cultivo,
    COUNT(*) AS total_cosechas,
    AVG(hc.rendimiento_real) AS rendimiento_promedio,
    AVG(CASE WHEN hc.rendimiento_esperado > 0 THEN (hc.rendimiento_real / hc.rendimiento_esperado) * 100 ELSE 0 END) AS cumplimiento_promedio,
    SUM(hc.cantidad_cosechada) AS cantidad_total_cosechada,
    AVG(hc.superficie_hectareas) AS superficie_promedio
FROM historial_cosechas hc
JOIN cultivos c ON hc.cultivo_id = c.id
GROUP BY c.id, c.nombre'
    )
);
PREPARE sentencia FROM @consulta;
EXECUTE sentencia;
DEALLOCATE PREPARE sentencia;
