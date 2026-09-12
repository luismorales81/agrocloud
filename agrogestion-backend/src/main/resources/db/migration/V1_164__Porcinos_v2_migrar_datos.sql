-- ============================================================================
-- MIGRACIÓN: Migración idempotente de datos legacy → Porcinos v2
-- Versión: V1_164
-- Requiere: V1_162 (legacy) y V1_163 (esquema v2)
-- Idempotente: cada bloque solo corre si existe la tabla legacy (SQL dinámico).
-- ============================================================================

SET @esquema = DATABASE();

-- --------------------------------------------------------------------------
-- Catálogos
-- --------------------------------------------------------------------------

SET @consulta = (SELECT IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_legacy_razas_porcinos'),
    'INSERT IGNORE INTO porcinos_raza (id, empresa_id, nombre, tipo, activo, created_at, updated_at)
     SELECT r.id, r.empresa_id, r.nombre, r.tipo, r.activo,
            COALESCE(r.fecha_creacion, CURRENT_TIMESTAMP), r.fecha_actualizacion
     FROM porcinos_legacy_razas_porcinos r',
    'SELECT 1'));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_legacy_motivos_baja_porcinos'),
    'INSERT IGNORE INTO porcinos_motivo_baja (id, empresa_id, nombre, activo, created_at, updated_at)
     SELECT m.id, m.empresa_id, m.nombre, m.activo,
            COALESCE(m.fecha_creacion, CURRENT_TIMESTAMP), m.fecha_actualizacion
     FROM porcinos_legacy_motivos_baja_porcinos m',
    'SELECT 1'));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_legacy_causas_mortalidad_porcinos'),
    'INSERT IGNORE INTO porcinos_causa_mortalidad (id, empresa_id, nombre, etapa, activo, created_at, updated_at)
     SELECT c.id, c.empresa_id, c.nombre, c.etapa, c.activo,
            COALESCE(c.fecha_creacion, CURRENT_TIMESTAMP), c.fecha_actualizacion
     FROM porcinos_legacy_causas_mortalidad_porcinos c',
    'SELECT 1'));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_legacy_tipos_servicio_porcinos'),
    'INSERT IGNORE INTO porcinos_tipo_servicio (id, empresa_id, nombre, activo, created_at, updated_at)
     SELECT t.id, t.empresa_id, t.nombre, t.activo,
            COALESCE(t.fecha_creacion, CURRENT_TIMESTAMP), t.fecha_actualizacion
     FROM porcinos_legacy_tipos_servicio_porcinos t',
    'SELECT 1'));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

-- --------------------------------------------------------------------------
-- Establecimiento
-- --------------------------------------------------------------------------

SET @tiene_realiza_faena = (
    SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = @esquema AND table_name = 'porcinos_legacy_parametros_establecimiento_porcinos' AND column_name = 'realiza_faena'
);

SET @consulta = (SELECT IF(
    NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_legacy_parametros_establecimiento_porcinos'),
    'SELECT 1',
    IF(@tiene_realiza_faena > 0,
        'INSERT IGNORE INTO porcinos_establecimiento (
        id, empresa_id, nombre, ubicacion, coordenadas,
        dias_gestacion, dias_lactancia, dias_entre_celos,
        faena_habilitada, capacidad_cabezas, activo, created_at, updated_at
     )
     SELECT
        pe.id, pe.empresa_id, pe.nombre_establecimiento,
        pe.localidad,
        NULL,
        COALESCE(pp.dias_promedio_gestacion, 114),
        COALESCE(pp.dias_lactancia, 21),
        COALESCE(pp.dias_entre_celos, 21),
        COALESCE(pe.realiza_faena, TRUE),
        pe.maxima_capacidad_recria_engorde,
        TRUE,
        COALESCE(pe.fecha_creacion, CURRENT_TIMESTAMP),
        pe.fecha_actualizacion
     FROM porcinos_legacy_parametros_establecimiento_porcinos pe
     LEFT JOIN porcinos_legacy_parametros_productivos_porcinos pp ON pp.empresa_id = pe.empresa_id',
        'INSERT IGNORE INTO porcinos_establecimiento (
        id, empresa_id, nombre, ubicacion, coordenadas,
        dias_gestacion, dias_lactancia, dias_entre_celos,
        faena_habilitada, capacidad_cabezas, activo, created_at, updated_at
     )
     SELECT
        pe.id, pe.empresa_id, pe.nombre_establecimiento,
        pe.localidad,
        NULL,
        COALESCE(pp.dias_promedio_gestacion, 114),
        COALESCE(pp.dias_lactancia, 21),
        COALESCE(pp.dias_entre_celos, 21),
        TRUE,
        pe.maxima_capacidad_recria_engorde,
        TRUE,
        COALESCE(pe.fecha_creacion, CURRENT_TIMESTAMP),
        pe.fecha_actualizacion
     FROM porcinos_legacy_parametros_establecimiento_porcinos pe
     LEFT JOIN porcinos_legacy_parametros_productivos_porcinos pp ON pp.empresa_id = pe.empresa_id'
    )
));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

-- Galpones (solo nivel GALPON y con establecimiento v2 existente)
SET @consulta = (SELECT IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_legacy_ubicaciones_internas'),
    'INSERT IGNORE INTO porcinos_galpon (id, establecimiento_id, nombre, capacidad_cabezas, estado, activo, created_at, updated_at)
     SELECT u.id, pe.id, u.nombre, u.capacidad_maxima, ''DISPONIBLE'', u.activo,
            COALESCE(u.fecha_creacion, CURRENT_TIMESTAMP), u.fecha_actualizacion
     FROM porcinos_legacy_ubicaciones_internas u
     INNER JOIN porcinos_establecimiento pe ON pe.empresa_id = u.empresa_id
     WHERE u.nivel = ''GALPON''',
    'SELECT 1'));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

-- --------------------------------------------------------------------------
-- Plantel reproductivo
-- --------------------------------------------------------------------------

SET @consulta = (SELECT IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_legacy_madres'),
    'INSERT IGNORE INTO porcinos_madre (
        id, empresa_id, caravana, raza_id, galpon_id, estado,
        fecha_ingreso, fecha_nacimiento, activo, created_at, updated_at
     )
     SELECT
        m.id, m.empresa_id, m.identificacion, m.raza_id,
        CASE WHEN u.nivel = ''GALPON'' AND EXISTS (SELECT 1 FROM porcinos_galpon g WHERE g.id = m.ubicacion_interna_id)
             THEN m.ubicacion_interna_id ELSE NULL END,
        CASE m.estado_actual
            WHEN ''DESCARTE'' THEN ''BAJA''
            WHEN ''RECRIA'' THEN ''ADULTA''
            ELSE m.estado_actual
        END,
        m.fecha_ingreso_granja, m.fecha_nacimiento, m.activo,
        COALESCE(m.fecha_creacion, CURRENT_TIMESTAMP), m.fecha_actualizacion
     FROM porcinos_legacy_madres m
     LEFT JOIN porcinos_legacy_ubicaciones_internas u ON u.id = m.ubicacion_interna_id',
    'SELECT 1'));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @consulta = (SELECT IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_legacy_padrillos'),
    'INSERT IGNORE INTO porcinos_padrillo (id, empresa_id, nombre, raza_id, activo, created_at, updated_at)
     SELECT p.id, p.empresa_id, p.identificacion, p.raza_id, p.activo,
            COALESCE(p.fecha_creacion, CURRENT_TIMESTAMP), p.fecha_actualizacion
     FROM porcinos_legacy_padrillos p',
    'SELECT 1'));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

-- --------------------------------------------------------------------------
-- Lotes de engorde (desde recría legacy)
-- --------------------------------------------------------------------------

SET @consulta = (SELECT IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_legacy_recria'),
    'INSERT IGNORE INTO porcinos_lote (
        id, empresa_id, galpon_id, campana_id, nombre, origen, destete_id,
        fecha_ingreso, fecha_cierre, cabezas_inicial, cabezas_actuales,
        peso_promedio_ingreso_kg, etapa, estado, observaciones, created_at, updated_at
     )
     SELECT
        r.id, r.empresa_id,
        CASE WHEN u.nivel = ''GALPON'' AND EXISTS (SELECT 1 FROM porcinos_galpon g WHERE g.id = r.ubicacion_interna_id)
             THEN r.ubicacion_interna_id ELSE NULL END,
        COALESCE(
            r.campana_id,
            (SELECT c.id FROM core_campanas c WHERE c.empresa_id = r.empresa_id AND c.es_default = 1 ORDER BY c.id LIMIT 1),
            (SELECT c.id FROM core_campanas c WHERE c.empresa_id = r.empresa_id AND c.estado = ''ACTIVA'' ORDER BY c.fecha_inicio DESC, c.id LIMIT 1),
            (SELECT c.id FROM core_campanas c WHERE c.empresa_id = r.empresa_id ORDER BY c.id LIMIT 1)
        ),
        CONCAT(''Lote '', r.id),
        COALESCE(r.origen, ''EXTERNO''),
        NULL,
        r.fecha_ingreso, r.fecha_salida,
        COALESCE(r.cantidad_animales, 0), COALESCE(r.cantidad_animales, 0),
        COALESCE(r.peso_inicial_kg, r.peso_promedio, 0),
        CASE WHEN r.etapa IN (''TERMINACION'', ''DESARROLLO'') THEN ''ENGORDE'' ELSE ''RECRIA'' END,
        CASE WHEN r.fecha_salida IS NOT NULL OR r.activo = FALSE THEN ''CERRADO'' ELSE ''ACTIVO'' END,
        r.observaciones,
        COALESCE(r.fecha_creacion, CURRENT_TIMESTAMP), r.fecha_actualizacion
     FROM porcinos_legacy_recria r
     LEFT JOIN porcinos_legacy_ubicaciones_internas u ON u.id = r.ubicacion_interna_id
     WHERE COALESCE(
            r.campana_id,
            (SELECT c.id FROM core_campanas c WHERE c.empresa_id = r.empresa_id AND c.es_default = 1 ORDER BY c.id LIMIT 1),
            (SELECT c.id FROM core_campanas c WHERE c.empresa_id = r.empresa_id AND c.estado = ''ACTIVA'' ORDER BY c.fecha_inicio DESC, c.id LIMIT 1),
            (SELECT c.id FROM core_campanas c WHERE c.empresa_id = r.empresa_id ORDER BY c.id LIMIT 1)
        ) IS NOT NULL',
    'SELECT 1'));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

-- --------------------------------------------------------------------------
-- Ventas
-- --------------------------------------------------------------------------

SET @tiene_peso_envio = (
    SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = @esquema AND table_name = 'porcinos_legacy_ventas_porcinos' AND column_name = 'peso_envio'
);

SET @consulta = (SELECT IF(
    NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_legacy_ventas_porcinos'),
    'SELECT 1',
    IF(@tiene_peso_envio > 0,
        'INSERT IGNORE INTO porcinos_venta (
            id, lote_id, empresa_id, campana_id, fecha, tipo, cabezas,
            peso_promedio_kg, precio_kg, total, comprador, observaciones, created_at, updated_at
         )
         SELECT v.id, COALESCE(v.recria_id, v.lote_id), v.empresa_id,
            COALESCE(
                v.campana_id,
                (SELECT c.id FROM core_campanas c WHERE c.empresa_id = v.empresa_id AND c.es_default = 1 ORDER BY c.id LIMIT 1),
                (SELECT c.id FROM core_campanas c WHERE c.empresa_id = v.empresa_id AND c.estado = ''ACTIVA'' ORDER BY c.fecha_inicio DESC, c.id LIMIT 1),
                (SELECT c.id FROM core_campanas c WHERE c.empresa_id = v.empresa_id ORDER BY c.id LIMIT 1)
            ),
            COALESCE(v.fecha_faena, v.fecha_envio, v.fecha), v.tipo, v.cantidad,
            COALESCE(v.peso_faena, v.peso_envio, v.peso_promedio),
            v.precio_kg, v.ingreso_total, v.cliente, v.observaciones,
            COALESCE(v.fecha_creacion, CURRENT_TIMESTAMP), v.fecha_actualizacion
         FROM porcinos_legacy_ventas_porcinos v
         WHERE COALESCE(v.recria_id, v.lote_id) IS NOT NULL
           AND EXISTS (SELECT 1 FROM porcinos_lote l WHERE l.id = COALESCE(v.recria_id, v.lote_id))
           AND COALESCE(
                v.campana_id,
                (SELECT c.id FROM core_campanas c WHERE c.empresa_id = v.empresa_id AND c.es_default = 1 ORDER BY c.id LIMIT 1),
                (SELECT c.id FROM core_campanas c WHERE c.empresa_id = v.empresa_id AND c.estado = ''ACTIVA'' ORDER BY c.fecha_inicio DESC, c.id LIMIT 1),
                (SELECT c.id FROM core_campanas c WHERE c.empresa_id = v.empresa_id ORDER BY c.id LIMIT 1)
            ) IS NOT NULL AND v.activo = TRUE',
        'INSERT IGNORE INTO porcinos_venta (
            id, lote_id, empresa_id, campana_id, fecha, tipo, cabezas,
            peso_promedio_kg, precio_kg, total, comprador, observaciones, created_at, updated_at
         )
         SELECT v.id, COALESCE(v.recria_id, v.lote_id), v.empresa_id,
            COALESCE(
                v.campana_id,
                (SELECT c.id FROM core_campanas c WHERE c.empresa_id = v.empresa_id AND c.es_default = 1 ORDER BY c.id LIMIT 1),
                (SELECT c.id FROM core_campanas c WHERE c.empresa_id = v.empresa_id AND c.estado = ''ACTIVA'' ORDER BY c.fecha_inicio DESC, c.id LIMIT 1),
                (SELECT c.id FROM core_campanas c WHERE c.empresa_id = v.empresa_id ORDER BY c.id LIMIT 1)
            ),
            v.fecha, v.tipo, v.cantidad, v.peso_promedio, v.precio_kg, v.ingreso_total,
            v.cliente, v.observaciones,
            COALESCE(v.fecha_creacion, CURRENT_TIMESTAMP), v.fecha_actualizacion
         FROM porcinos_legacy_ventas_porcinos v
         WHERE COALESCE(v.recria_id, v.lote_id) IS NOT NULL
           AND EXISTS (SELECT 1 FROM porcinos_lote l WHERE l.id = COALESCE(v.recria_id, v.lote_id))
           AND COALESCE(
                v.campana_id,
                (SELECT c.id FROM core_campanas c WHERE c.empresa_id = v.empresa_id AND c.es_default = 1 ORDER BY c.id LIMIT 1),
                (SELECT c.id FROM core_campanas c WHERE c.empresa_id = v.empresa_id AND c.estado = ''ACTIVA'' ORDER BY c.fecha_inicio DESC, c.id LIMIT 1),
                (SELECT c.id FROM core_campanas c WHERE c.empresa_id = v.empresa_id ORDER BY c.id LIMIT 1)
            ) IS NOT NULL AND v.activo = TRUE'
    )
));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

-- --------------------------------------------------------------------------
-- Pesadas
-- --------------------------------------------------------------------------

SET @consulta = (SELECT IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_legacy_registros_peso'),
    'INSERT IGNORE INTO porcinos_pesada (
        id, lote_id, empresa_id, fecha, peso_promedio_kg, cabezas_muestreadas, observaciones, created_at
     )
     SELECT p.id, p.recria_id, l.empresa_id, p.fecha_pesaje, p.peso_promedio, p.cantidad_animales,
            p.observaciones, COALESCE(p.fecha_creacion, CURRENT_TIMESTAMP)
     FROM porcinos_legacy_registros_peso p
     INNER JOIN porcinos_lote l ON l.id = p.recria_id',
    'SELECT 1'));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

-- --------------------------------------------------------------------------
-- Muertes
-- --------------------------------------------------------------------------

SET @consulta = (SELECT IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = @esquema AND table_name = 'porcinos_legacy_muertes_recria'),
    'INSERT IGNORE INTO porcinos_muerte (
        id, lote_id, empresa_id, fecha, cabezas, causa_mortalidad_id, observaciones, created_at
     )
     SELECT m.id, m.recria_id, l.empresa_id, m.fecha, m.cantidad,
            CASE WHEN m.causa_mortalidad_id IS NOT NULL
                      AND EXISTS (SELECT 1 FROM porcinos_causa_mortalidad c WHERE c.id = m.causa_mortalidad_id)
                 THEN m.causa_mortalidad_id ELSE NULL END,
            m.observaciones,
            COALESCE(m.fecha_creacion, CURRENT_TIMESTAMP)
     FROM porcinos_legacy_muertes_recria m
     INNER JOIN porcinos_lote l ON l.id = m.recria_id',
    'SELECT 1'));
PREPARE sentencia FROM @consulta; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;
