-- =============================================================================
-- Habilitar módulo LECHERIA para la empresa de prueba / demo
-- =============================================================================
-- Requisitos: migraciones V1_158+ aplicadas (tablas lecheria_* + modules).
--
-- Resolución de empresa (prioridad):
--   1) @EMPRESA_ID explícito (si no es NULL)
--   2) Empresa con nombre @NOMBRE_EMPRESA (p. ej. AgroCloud Demo)
--   3) Primera empresa activa del usuario @USUARIO_DEMO (p. ej. admin)
--   4) ID 1
--
-- Uso:
--   mysql -u root -p agrocloud < HABILITAR_LECHERIA_EMPRESA_DEMO.sql
--   o: scripts\ejecutar-habilitar-lecheria-demo.bat
--
-- @CARGAR_DATOS_DEMO: 1 = catálogos + establecimientos + rodeos (idempotente)
-- Luego ejecutar DEMO_INSERTAR_LECHERIA.sql para animales y ordeñes.
-- =============================================================================

SET @USUARIO_DEMO      := 'admin';
SET @NOMBRE_EMPRESA    := 'AgroCloud Demo';
SET @EMPRESA_ID        := NULL;
SET @CARGAR_DATOS_DEMO := 1;

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

INSERT INTO modules (name, code, description, active)
SELECT 'Lechería', 'LECHERIA',
       'Gestión lechera multi-especie: animales, lactancia, ordeñe, reproducción, sanidad y ventas.',
       TRUE
WHERE NOT EXISTS (SELECT 1 FROM modules WHERE code = 'LECHERIA');

SET @MODULO_LECHERIA_ID := (SELECT id FROM modules WHERE code = 'LECHERIA' LIMIT 1);

INSERT INTO company_modules (company_id, module_id, enabled)
SELECT @EMPRESA_RESUELTA, @MODULO_LECHERIA_ID, TRUE
WHERE @EMPRESA_RESUELTA IS NOT NULL
  AND @MODULO_LECHERIA_ID IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM company_modules cm
      WHERE cm.company_id = @EMPRESA_RESUELTA AND cm.module_id = @MODULO_LECHERIA_ID
  );

UPDATE company_modules cm
INNER JOIN modules m ON m.id = cm.module_id
SET cm.enabled = TRUE
WHERE cm.company_id = @EMPRESA_RESUELTA AND m.code = 'LECHERIA';

SELECT e.id AS empresa_id, e.nombre AS empresa, m.code AS modulo, cm.enabled AS habilitado
FROM company_modules cm
INNER JOIN empresas e ON e.id = cm.company_id
INNER JOIN modules m ON m.id = cm.module_id
WHERE e.id = @EMPRESA_RESUELTA AND m.code = 'LECHERIA';

-- =============================================================================
-- Catálogos y estructura demo
-- =============================================================================

INSERT INTO lecheria_raza (empresa_id, especie, nombre, activo)
SELECT @EMPRESA_RESUELTA, 'BOVINO', 'Holstein', TRUE
WHERE @CARGAR_DATOS_DEMO = 1
  AND NOT EXISTS (
      SELECT 1 FROM lecheria_raza r
      WHERE r.empresa_id = @EMPRESA_RESUELTA AND r.especie = 'BOVINO' AND r.nombre = 'Holstein'
  );

INSERT INTO lecheria_raza (empresa_id, especie, nombre, activo)
SELECT @EMPRESA_RESUELTA, 'BOVINO', 'Jersey', TRUE
WHERE @CARGAR_DATOS_DEMO = 1
  AND NOT EXISTS (
      SELECT 1 FROM lecheria_raza r
      WHERE r.empresa_id = @EMPRESA_RESUELTA AND r.especie = 'BOVINO' AND r.nombre = 'Jersey'
  );

INSERT INTO lecheria_raza (empresa_id, especie, nombre, activo)
SELECT @EMPRESA_RESUELTA, 'CAPRINO', 'Saanen', TRUE
WHERE @CARGAR_DATOS_DEMO = 1
  AND NOT EXISTS (
      SELECT 1 FROM lecheria_raza r
      WHERE r.empresa_id = @EMPRESA_RESUELTA AND r.especie = 'CAPRINO' AND r.nombre = 'Saanen'
  );

INSERT INTO lecheria_raza (empresa_id, especie, nombre, activo)
SELECT @EMPRESA_RESUELTA, 'OVINO', 'Latxa', TRUE
WHERE @CARGAR_DATOS_DEMO = 1
  AND NOT EXISTS (
      SELECT 1 FROM lecheria_raza r
      WHERE r.empresa_id = @EMPRESA_RESUELTA AND r.especie = 'OVINO' AND r.nombre = 'Latxa'
  );

INSERT INTO lecheria_motivo_baja (empresa_id, nombre, activo)
SELECT @EMPRESA_RESUELTA, 'Venta', TRUE
WHERE @CARGAR_DATOS_DEMO = 1
  AND NOT EXISTS (
      SELECT 1 FROM lecheria_motivo_baja m
      WHERE m.empresa_id = @EMPRESA_RESUELTA AND m.nombre = 'Venta'
  );

INSERT INTO lecheria_motivo_baja (empresa_id, nombre, activo)
SELECT @EMPRESA_RESUELTA, 'Muerte', TRUE
WHERE @CARGAR_DATOS_DEMO = 1
  AND NOT EXISTS (
      SELECT 1 FROM lecheria_motivo_baja m
      WHERE m.empresa_id = @EMPRESA_RESUELTA AND m.nombre = 'Muerte'
  );

INSERT INTO lecheria_motivo_baja (empresa_id, nombre, activo)
SELECT @EMPRESA_RESUELTA, 'Descarte productivo', TRUE
WHERE @CARGAR_DATOS_DEMO = 1
  AND NOT EXISTS (
      SELECT 1 FROM lecheria_motivo_baja m
      WHERE m.empresa_id = @EMPRESA_RESUELTA AND m.nombre = 'Descarte productivo'
  );

INSERT INTO lecheria_establecimiento (
    empresa_id, nombre, ubicacion, coordenadas, capacidad_animales, activo
)
SELECT @EMPRESA_RESUELTA,
       'Tambo demo — Planta Sur',
       'Ruta 205 km 45, Brandsen, Buenos Aires',
       '[{"lat":-35.187,"lng":-58.234},{"lat":-35.189,"lng":-58.231},{"lat":-35.191,"lng":-58.236}]',
       200,
       TRUE
WHERE @CARGAR_DATOS_DEMO = 1
  AND NOT EXISTS (
      SELECT 1 FROM lecheria_establecimiento e
      WHERE e.empresa_id = @EMPRESA_RESUELTA AND e.nombre = 'Tambo demo — Planta Sur'
  );

INSERT INTO lecheria_establecimiento (
    empresa_id, nombre, ubicacion, coordenadas, capacidad_animales, activo
)
SELECT @EMPRESA_RESUELTA,
       'Caprinocultura demo — Loma Verde',
       'Camino rural s/n, Tandil',
       '[{"lat":-37.321,"lng":-59.133}]',
       80,
       TRUE
WHERE @CARGAR_DATOS_DEMO = 1
  AND NOT EXISTS (
      SELECT 1 FROM lecheria_establecimiento e
      WHERE e.empresa_id = @EMPRESA_RESUELTA AND e.nombre = 'Caprinocultura demo — Loma Verde'
  );

SET @EST_TAMBO_ID := (
    SELECT id FROM lecheria_establecimiento
    WHERE empresa_id = @EMPRESA_RESUELTA AND nombre = 'Tambo demo — Planta Sur' LIMIT 1
);

SET @EST_CAPRINO_ID := (
    SELECT id FROM lecheria_establecimiento
    WHERE empresa_id = @EMPRESA_RESUELTA AND nombre = 'Caprinocultura demo — Loma Verde' LIMIT 1
);

INSERT INTO lecheria_rodeo (establecimiento_id, empresa_id, nombre, especie, activo)
SELECT @EST_TAMBO_ID, @EMPRESA_RESUELTA, 'Rodeo Vacas Alto', 'BOVINO', TRUE
WHERE @CARGAR_DATOS_DEMO = 1 AND @EST_TAMBO_ID IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM lecheria_rodeo r
      WHERE r.establecimiento_id = @EST_TAMBO_ID AND r.nombre = 'Rodeo Vacas Alto'
  );

INSERT INTO lecheria_rodeo (establecimiento_id, empresa_id, nombre, especie, activo)
SELECT @EST_TAMBO_ID, @EMPRESA_RESUELTA, 'Rodeo Vaquillonas', 'BOVINO', TRUE
WHERE @CARGAR_DATOS_DEMO = 1 AND @EST_TAMBO_ID IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM lecheria_rodeo r
      WHERE r.establecimiento_id = @EST_TAMBO_ID AND r.nombre = 'Rodeo Vaquillonas'
  );

INSERT INTO lecheria_rodeo (establecimiento_id, empresa_id, nombre, especie, activo)
SELECT @EST_CAPRINO_ID, @EMPRESA_RESUELTA, 'Rodeo Cabras Ordeñe', 'CAPRINO', TRUE
WHERE @CARGAR_DATOS_DEMO = 1 AND @EST_CAPRINO_ID IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM lecheria_rodeo r
      WHERE r.establecimiento_id = @EST_CAPRINO_ID AND r.nombre = 'Rodeo Cabras Ordeñe'
  );

INSERT INTO lecheria_rodeo (establecimiento_id, empresa_id, nombre, especie, activo)
SELECT @EST_CAPRINO_ID, @EMPRESA_RESUELTA, 'Rodeo Ovejas Latxa', 'OVINO', TRUE
WHERE @CARGAR_DATOS_DEMO = 1 AND @EST_CAPRINO_ID IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM lecheria_rodeo r
      WHERE r.establecimiento_id = @EST_CAPRINO_ID AND r.nombre = 'Rodeo Ovejas Latxa'
  );

-- Insumos demo (alimento y sanidad)
SET @USER_DEMO_ID := (SELECT id FROM usuarios WHERE username = @USUARIO_DEMO LIMIT 1);

INSERT INTO cultivo_insumos (
    nombre, tipo, descripcion, unidad_medida, precio_unitario,
    stock_actual, stock_minimo, proveedor, activo, empresa_id, user_id,
    fecha_creacion, fecha_actualizacion
)
SELECT 'Balanceado vacas en lactancia 18% PB', 'OTROS',
       'Ración para tambo — vacas en producción', 'kg',
       380.00, 10000.00, 1000.00, 'NutriLeche S.A.', TRUE,
       @EMPRESA_RESUELTA, @USER_DEMO_ID, NOW(), NOW()
WHERE @CARGAR_DATOS_DEMO = 1 AND @USER_DEMO_ID IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM cultivo_insumos i
      WHERE i.empresa_id = @EMPRESA_RESUELTA
        AND i.nombre = 'Balanceado vacas en lactancia 18% PB'
  );

INSERT INTO cultivo_insumos (
    nombre, tipo, descripcion, unidad_medida, precio_unitario,
    stock_actual, stock_minimo, proveedor, activo, empresa_id, user_id,
    fecha_creacion, fecha_actualizacion
)
SELECT 'Antibiótico intramamario demo', 'OTROS',
       'Tratamiento mastitis — uso demo', 'unidad',
       2500.00, 50.00, 5.00, 'VetAgro', TRUE,
       @EMPRESA_RESUELTA, @USER_DEMO_ID, NOW(), NOW()
WHERE @CARGAR_DATOS_DEMO = 1 AND @USER_DEMO_ID IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM cultivo_insumos i
      WHERE i.empresa_id = @EMPRESA_RESUELTA
        AND i.nombre = 'Antibiótico intramamario demo'
  );

SELECT 'LECHERIA habilitado' AS resultado,
       @EMPRESA_RESUELTA AS empresa_id,
       (SELECT COUNT(*) FROM lecheria_rodeo WHERE empresa_id = @EMPRESA_RESUELTA) AS rodeos_demo,
       'Ejecutar DEMO_INSERTAR_LECHERIA.sql para animales y ordeñes' AS siguiente_paso;
