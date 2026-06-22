-- =============================================================================
-- Habilitar módulo FEEDLOT para la empresa de prueba / demo
-- =============================================================================
-- Requisitos: migración V1_153__Modulo_feedlot.sql aplicada (tablas feedlot_* + modules).
--
-- Resolución de empresa (prioridad):
--   1) @EMPRESA_ID explícito (si no es NULL)
--   2) Empresa con nombre @NOMBRE_EMPRESA (p. ej. AgroCloud Demo)
--   3) Primera empresa activa del usuario @USUARIO_DEMO (p. ej. admin — NO superman)
--   4) ID 1
--
-- QA funcional del módulo: usar admin@agrocloud.com en AgroCloud Demo (ver README-FEEDLOT-DEMO.md).
-- superman (SUPERADMIN) solo para tareas de plataforma; no valida permisos de módulo.
--
-- Uso:
--   mysql -u root -p agrocloud < HABILITAR_FEEDLOT_EMPRESA_DEMO.sql
--   o: scripts\ejecutar-habilitar-feedlot-demo.bat
--
-- @CARGAR_DATOS_DEMO: 1 = inserta catálogos + establecimiento + 3 corrales (idempotente)
-- =============================================================================

SET @USUARIO_DEMO      := 'admin';
SET @NOMBRE_EMPRESA    := 'AgroCloud Demo';
SET @EMPRESA_ID        := NULL;
SET @CARGAR_DATOS_DEMO := 1;

-- Resolver empresa destino
SET @EMPRESA_RESUELTA := @EMPRESA_ID;

SET @EMPRESA_RESUELTA := COALESCE(
    @EMPRESA_RESUELTA,
    (SELECT e.id FROM empresas e WHERE e.nombre = @NOMBRE_EMPRESA LIMIT 1),
    (
        SELECT ue.empresa_id
        FROM usuario_empresas ue
        INNER JOIN usuarios u ON u.id = ue.usuario_id
        WHERE u.username = @USUARIO_DEMO AND ue.estado = 'ACTIVO'
        ORDER BY ue.id
        LIMIT 1
    ),
    (
        SELECT uer.empresa_id
        FROM usuarios_empresas_roles uer
        INNER JOIN usuarios u ON u.id = uer.usuario_id
        WHERE u.username = @USUARIO_DEMO
        ORDER BY uer.id
        LIMIT 1
    ),
    1
);

SELECT @EMPRESA_RESUELTA AS empresa_id_destino,
       (SELECT nombre FROM empresas WHERE id = @EMPRESA_RESUELTA LIMIT 1) AS empresa_nombre;

-- Asegurar módulo FEEDLOT en catálogo
INSERT INTO modules (name, code, description, active)
SELECT 'Engorde a corral (Feedlot)', 'FEEDLOT',
       'Engorde bovino a corral: lotes, pesadas, alimento, sanidad, faena y closeout.',
       TRUE
WHERE NOT EXISTS (SELECT 1 FROM modules WHERE code = 'FEEDLOT');

SET @MODULO_FEEDLOT_ID := (SELECT id FROM modules WHERE code = 'FEEDLOT' LIMIT 1);

-- Habilitar en company_modules
INSERT INTO company_modules (company_id, module_id, enabled)
SELECT @EMPRESA_RESUELTA, @MODULO_FEEDLOT_ID, TRUE
WHERE @EMPRESA_RESUELTA IS NOT NULL
  AND @MODULO_FEEDLOT_ID IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM company_modules cm
      WHERE cm.company_id = @EMPRESA_RESUELTA AND cm.module_id = @MODULO_FEEDLOT_ID
  );

UPDATE company_modules cm
INNER JOIN modules m ON m.id = cm.module_id
SET cm.enabled = TRUE
WHERE cm.company_id = @EMPRESA_RESUELTA AND m.code = 'FEEDLOT';

-- Verificación módulo habilitado
SELECT e.id AS empresa_id,
       e.nombre AS empresa,
       m.code AS modulo,
       cm.enabled AS habilitado
FROM company_modules cm
INNER JOIN empresas e ON e.id = cm.company_id
INNER JOIN modules m ON m.id = cm.module_id
WHERE e.id = @EMPRESA_RESUELTA AND m.code = 'FEEDLOT';

-- =============================================================================
-- Datos demo (solo si @CARGAR_DATOS_DEMO = 1)
-- =============================================================================

INSERT INTO feedlot_categoria (empresa_id, nombre, activo)
SELECT @EMPRESA_RESUELTA, 'Novillo', TRUE
WHERE @CARGAR_DATOS_DEMO = 1
  AND NOT EXISTS (
      SELECT 1 FROM feedlot_categoria c
      WHERE c.empresa_id = @EMPRESA_RESUELTA AND c.nombre = 'Novillo'
  );

INSERT INTO feedlot_categoria (empresa_id, nombre, activo)
SELECT @EMPRESA_RESUELTA, 'Vaquillona', TRUE
WHERE @CARGAR_DATOS_DEMO = 1
  AND NOT EXISTS (
      SELECT 1 FROM feedlot_categoria c
      WHERE c.empresa_id = @EMPRESA_RESUELTA AND c.nombre = 'Vaquillona'
  );

INSERT INTO feedlot_raza (empresa_id, nombre, activo)
SELECT @EMPRESA_RESUELTA, 'Angus', TRUE
WHERE @CARGAR_DATOS_DEMO = 1
  AND NOT EXISTS (
      SELECT 1 FROM feedlot_raza r
      WHERE r.empresa_id = @EMPRESA_RESUELTA AND r.nombre = 'Angus'
  );

INSERT INTO feedlot_raza (empresa_id, nombre, activo)
SELECT @EMPRESA_RESUELTA, 'Hereford', TRUE
WHERE @CARGAR_DATOS_DEMO = 1
  AND NOT EXISTS (
      SELECT 1 FROM feedlot_raza r
      WHERE r.empresa_id = @EMPRESA_RESUELTA AND r.nombre = 'Hereford'
  );

INSERT INTO feedlot_motivo_muerte (empresa_id, nombre, activo)
SELECT @EMPRESA_RESUELTA, 'Neumonía', TRUE
WHERE @CARGAR_DATOS_DEMO = 1
  AND NOT EXISTS (
      SELECT 1 FROM feedlot_motivo_muerte m
      WHERE m.empresa_id = @EMPRESA_RESUELTA AND m.nombre = 'Neumonía'
  );

INSERT INTO feedlot_motivo_muerte (empresa_id, nombre, activo)
SELECT @EMPRESA_RESUELTA, 'Timpanismo', TRUE
WHERE @CARGAR_DATOS_DEMO = 1
  AND NOT EXISTS (
      SELECT 1 FROM feedlot_motivo_muerte m
      WHERE m.empresa_id = @EMPRESA_RESUELTA AND m.nombre = 'Timpanismo'
  );

INSERT INTO feedlot_proveedor_origen (empresa_id, nombre, tipo, activo)
SELECT @EMPRESA_RESUELTA, 'Campo propio', 'CAMPO_PROPIO', TRUE
WHERE @CARGAR_DATOS_DEMO = 1
  AND NOT EXISTS (
      SELECT 1 FROM feedlot_proveedor_origen p
      WHERE p.empresa_id = @EMPRESA_RESUELTA AND p.nombre = 'Campo propio'
  );

INSERT INTO feedlot_proveedor_origen (empresa_id, nombre, tipo, activo)
SELECT @EMPRESA_RESUELTA, 'Consignatario demo', 'CONSIGNATARIO', TRUE
WHERE @CARGAR_DATOS_DEMO = 1
  AND NOT EXISTS (
      SELECT 1 FROM feedlot_proveedor_origen p
      WHERE p.empresa_id = @EMPRESA_RESUELTA AND p.nombre = 'Consignatario demo'
  );

INSERT INTO feedlot_establecimiento (empresa_id, nombre, ubicacion, capacidad_total_cabezas, activo)
SELECT @EMPRESA_RESUELTA, 'Feedlot demo — Planta Norte', 'Ruta 9 km 120', 500, TRUE
WHERE @CARGAR_DATOS_DEMO = 1
  AND NOT EXISTS (
      SELECT 1 FROM feedlot_establecimiento e
      WHERE e.empresa_id = @EMPRESA_RESUELTA AND e.nombre = 'Feedlot demo — Planta Norte'
  );

SET @EST_FEEDLOT_ID := (
    SELECT id FROM feedlot_establecimiento
    WHERE empresa_id = @EMPRESA_RESUELTA AND nombre = 'Feedlot demo — Planta Norte'
    LIMIT 1
);

INSERT INTO feedlot_corral (establecimiento_id, nombre, capacidad_cabezas, estado, activo)
SELECT @EST_FEEDLOT_ID, 'Corral A', 120, 'DISPONIBLE', TRUE
WHERE @CARGAR_DATOS_DEMO = 1 AND @EST_FEEDLOT_ID IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM feedlot_corral c
      WHERE c.establecimiento_id = @EST_FEEDLOT_ID AND c.nombre = 'Corral A'
  );

INSERT INTO feedlot_corral (establecimiento_id, nombre, capacidad_cabezas, estado, activo)
SELECT @EST_FEEDLOT_ID, 'Corral B', 150, 'DISPONIBLE', TRUE
WHERE @CARGAR_DATOS_DEMO = 1 AND @EST_FEEDLOT_ID IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM feedlot_corral c
      WHERE c.establecimiento_id = @EST_FEEDLOT_ID AND c.nombre = 'Corral B'
  );

INSERT INTO feedlot_corral (establecimiento_id, nombre, capacidad_cabezas, estado, activo)
SELECT @EST_FEEDLOT_ID, 'Corral C', 100, 'DISPONIBLE', TRUE
WHERE @CARGAR_DATOS_DEMO = 1 AND @EST_FEEDLOT_ID IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM feedlot_corral c
      WHERE c.establecimiento_id = @EST_FEEDLOT_ID AND c.nombre = 'Corral C'
  );

-- Resumen final
SELECT 'FEEDLOT habilitado' AS resultado,
       @EMPRESA_RESUELTA AS empresa_id,
       (SELECT COUNT(*) FROM feedlot_corral c
        INNER JOIN feedlot_establecimiento e ON e.id = c.establecimiento_id
        WHERE e.empresa_id = @EMPRESA_RESUELTA) AS corrales_demo;
