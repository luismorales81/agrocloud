-- =============================================================================
-- Habilitar mÃ³dulo AVICOLA_CRIANZA + datos demo (engorde parrillero)
-- =============================================================================
-- Requisitos: V1_140, V1_156 aplicadas.
-- Uso: mysql -u root -p agrocloud < HABILITAR_AVICOLA_CRIANZA_EMPRESA_DEMO.sql
--      o: scripts\ejecutar-habilitar-avicola-carne-demo.bat
-- QA: admin@agrocloud.com en empresa AgroCloud Demo
-- @CARGAR_DATOS_DEMO: 1 = inserta galpones, razas, lotes y operaciones (idempotente)
-- =============================================================================

SET @USUARIO_DEMO      := 'admin';
SET @NOMBRE_EMPRESA    := 'AgroCloud Demo';
SET @EMPRESA_ID        := NULL;
SET @CARGAR_DATOS_DEMO := 1;
SET @MODULO_CRIANZA      := 'AVICOLA_CRIANZA';

SET @EMPRESA_RESUELTA := @EMPRESA_ID;

SET @EMPRESA_RESUELTA := COALESCE(
    @EMPRESA_RESUELTA,
    (SELECT e.id FROM empresas e WHERE e.nombre = @NOMBRE_EMPRESA LIMIT 1),
    (
        SELECT ue.empresa_id FROM usuario_empresas ue
        INNER JOIN usuarios u ON u.id = ue.usuario_id
        WHERE u.username = @USUARIO_DEMO AND ue.estado = 'ACTIVO'
        ORDER BY ue.id LIMIT 1
    ),
    (
        SELECT uer.empresa_id FROM usuarios_empresas_roles uer
        INNER JOIN usuarios u ON u.id = uer.usuario_id
        WHERE u.username = @USUARIO_DEMO
        ORDER BY uer.id LIMIT 1
    ),
    1
);

SELECT @EMPRESA_RESUELTA AS empresa_id_destino,
       (SELECT nombre FROM empresas WHERE id = @EMPRESA_RESUELTA LIMIT 1) AS empresa_nombre;

INSERT INTO modules (name, code, description, active)
SELECT 'AvÃ­cola carne', 'AVICOLA_CRIANZA',
       'Engorde avÃ­cola: lotes, pesadas, mortalidad, faena, consumos y reportes.',
       TRUE
WHERE NOT EXISTS (SELECT 1 FROM modules WHERE code = 'AVICOLA_CRIANZA');

SET @MODULO_CRIANZA_ID := (SELECT id FROM modules WHERE code = 'AVICOLA_CRIANZA' LIMIT 1);

INSERT INTO company_modules (company_id, module_id, enabled)
SELECT @EMPRESA_RESUELTA, @MODULO_CRIANZA_ID, TRUE
WHERE @EMPRESA_RESUELTA IS NOT NULL AND @MODULO_CRIANZA_ID IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM company_modules cm
      WHERE cm.company_id = @EMPRESA_RESUELTA AND cm.module_id = @MODULO_CRIANZA_ID
  );

UPDATE company_modules cm
INNER JOIN modules m ON m.id = cm.module_id
SET cm.enabled = TRUE
WHERE cm.company_id = @EMPRESA_RESUELTA AND m.code = 'AVICOLA_CRIANZA';

SELECT e.id AS empresa_id, e.nombre AS empresa, m.code AS modulo, cm.enabled AS habilitado
FROM company_modules cm
INNER JOIN empresas e ON e.id = cm.company_id
INNER JOIN modules m ON m.id = cm.module_id
WHERE e.id = @EMPRESA_RESUELTA AND m.code = 'AVICOLA_CRIANZA';

-- =============================================================================
-- Datos demo
-- =============================================================================

SET @CAMPANA_ID := (
    SELECT id FROM core_campanas
    WHERE empresa_id = @EMPRESA_RESUELTA AND estado = 'ACTIVO'
    ORDER BY id DESC LIMIT 1
);

SET @USER_DEMO_ID := (SELECT id FROM usuarios WHERE username = @USUARIO_DEMO LIMIT 1);

INSERT INTO cultivo_insumos (
    nombre, tipo, descripcion, unidad_medida, precio_unitario,
    stock_actual, stock_minimo, proveedor, activo, empresa_id, user_id,
    fecha_creacion, fecha_actualizacion
)
SELECT 'Balanceado parrillero 18% PB', 'OTROS', 'Alimento inicio/crecimiento avÃ­cola carne', 'kg',
       380.00, 12000.00, 500.00, 'NutriAves demo', TRUE, @EMPRESA_RESUELTA, @USER_DEMO_ID, NOW(), NOW()
WHERE @CARGAR_DATOS_DEMO = 1 AND @USER_DEMO_ID IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM cultivo_insumos i
      WHERE i.empresa_id = @EMPRESA_RESUELTA AND i.nombre = 'Balanceado parrillero 18% PB'
  );

SET @INSUMO_ALIMENTO_ID := (
    SELECT id FROM cultivo_insumos
    WHERE empresa_id = @EMPRESA_RESUELTA AND nombre = 'Balanceado parrillero 18% PB'
    LIMIT 1
);

INSERT INTO avicola_establecimiento (
    empresa_id, nombre, ubicacion, coordenadas, observaciones, activo, modulo_origen
)
SELECT @EMPRESA_RESUELTA,
       'GalpÃ³n demo â€” Engorde Norte',
       'Ruta 36 km 12, CÃ³rdoba',
       '[{"lat":-31.4200,"lng":-64.1880},{"lat":-31.4210,"lng":-64.1860},{"lat":-31.4220,"lng":-64.1890},{"lat":-31.4210,"lng":-64.1910}]',
       'Demo avÃ­cola carne â€” mapa y clima',
       TRUE,
       @MODULO_CRIANZA
WHERE @CARGAR_DATOS_DEMO = 1
  AND NOT EXISTS (
      SELECT 1 FROM avicola_establecimiento e
      WHERE e.empresa_id = @EMPRESA_RESUELTA AND e.nombre = 'GalpÃ³n demo â€” Engorde Norte' AND e.modulo_origen = @MODULO_CRIANZA
  );

INSERT INTO avicola_establecimiento (
    empresa_id, nombre, ubicacion, coordenadas, observaciones, activo, modulo_origen
)
SELECT @EMPRESA_RESUELTA,
       'GalpÃ³n demo â€” Engorde Sur',
       'Camino rural s/n',
       '[{"lat":-31.4350,"lng":-64.1750},{"lat":-31.4360,"lng":-64.1730},{"lat":-31.4370,"lng":-64.1760}]',
       'Segundo galpÃ³n demo',
       TRUE,
       @MODULO_CRIANZA
WHERE @CARGAR_DATOS_DEMO = 1
  AND NOT EXISTS (
      SELECT 1 FROM avicola_establecimiento e
      WHERE e.empresa_id = @EMPRESA_RESUELTA AND e.nombre = 'GalpÃ³n demo â€” Engorde Sur' AND e.modulo_origen = @MODULO_CRIANZA
  );

SET @EST_NORTE_ID := (
    SELECT id FROM avicola_establecimiento
    WHERE empresa_id = @EMPRESA_RESUELTA AND nombre = 'GalpÃ³n demo â€” Engorde Norte' AND modulo_origen = @MODULO_CRIANZA
    LIMIT 1
);

SET @EST_SUR_ID := (
    SELECT id FROM avicola_establecimiento
    WHERE empresa_id = @EMPRESA_RESUELTA AND nombre = 'GalpÃ³n demo â€” Engorde Sur' AND modulo_origen = @MODULO_CRIANZA
    LIMIT 1
);

INSERT INTO avicola_raza (empresa_id, nombre, activo, modulo_origen)
SELECT @EMPRESA_RESUELTA, 'Cobb 500', TRUE, @MODULO_CRIANZA
WHERE @CARGAR_DATOS_DEMO = 1
  AND NOT EXISTS (
      SELECT 1 FROM avicola_raza r
      WHERE r.empresa_id = @EMPRESA_RESUELTA AND r.nombre = 'Cobb 500' AND r.modulo_origen = @MODULO_CRIANZA
  );

INSERT INTO avicola_raza (empresa_id, nombre, activo, modulo_origen)
SELECT @EMPRESA_RESUELTA, 'Ross 308', TRUE, @MODULO_CRIANZA
WHERE @CARGAR_DATOS_DEMO = 1
  AND NOT EXISTS (
      SELECT 1 FROM avicola_raza r
      WHERE r.empresa_id = @EMPRESA_RESUELTA AND r.nombre = 'Ross 308' AND r.modulo_origen = @MODULO_CRIANZA
  );

SET @RAZA_COBB_ID := (
    SELECT id FROM avicola_raza
    WHERE empresa_id = @EMPRESA_RESUELTA AND nombre = 'Cobb 500' AND modulo_origen = @MODULO_CRIANZA LIMIT 1
);

SET @RAZA_ROSS_ID := (
    SELECT id FROM avicola_raza
    WHERE empresa_id = @EMPRESA_RESUELTA AND nombre = 'Ross 308' AND modulo_origen = @MODULO_CRIANZA LIMIT 1
);

INSERT INTO avicola_lote (
    empresa_id, campana_id, modulo_origen, establecimiento_id, raza_id, nombre, especie, origen,
    fecha_ingreso, cantidad_inicial, cantidad_animales, peso_promedio_ingreso, estado, observaciones
)
SELECT @EMPRESA_RESUELTA, @CAMPANA_ID, @MODULO_CRIANZA, @EST_NORTE_ID, @RAZA_COBB_ID,
       'Lote parrillero demo â€” Norte',
       'POLLO_PARRILLERO', 'EXTERNO',
       CURDATE() - INTERVAL 28 DAY,
       10000, 10000, 0.042,
       'ACTIVO',
       'Generado por HABILITAR_AVICOLA_CRIANZA_EMPRESA_DEMO.sql'
WHERE @CARGAR_DATOS_DEMO = 1 AND @EST_NORTE_ID IS NOT NULL AND @RAZA_COBB_ID IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM avicola_lote l
      WHERE l.empresa_id = @EMPRESA_RESUELTA AND l.nombre = 'Lote parrillero demo â€” Norte' AND l.modulo_origen = @MODULO_CRIANZA
  );

INSERT INTO avicola_lote (
    empresa_id, campana_id, modulo_origen, establecimiento_id, raza_id, nombre, especie, origen,
    fecha_ingreso, cantidad_inicial, cantidad_animales, peso_promedio_ingreso, estado, fecha_salida, observaciones
)
SELECT @EMPRESA_RESUELTA, @CAMPANA_ID, @MODULO_CRIANZA, @EST_SUR_ID, @RAZA_ROSS_ID,
       'Lote parrillero demo â€” Sur (cerrado)',
       'POLLO_PARRILLERO', 'EXTERNO',
       CURDATE() - INTERVAL 90 DAY,
       8000, 0, 0.040,
       'CERRADO',
       CURDATE() - INTERVAL 45 DAY,
       'Lote histÃ³rico demo â€” faena completa'
WHERE @CARGAR_DATOS_DEMO = 1 AND @EST_SUR_ID IS NOT NULL AND @RAZA_ROSS_ID IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM avicola_lote l
      WHERE l.empresa_id = @EMPRESA_RESUELTA AND l.nombre = 'Lote parrillero demo â€” Sur (cerrado)' AND l.modulo_origen = @MODULO_CRIANZA
  );

SET @LOTE_ACTIVO_ID := (
    SELECT id FROM avicola_lote
    WHERE empresa_id = @EMPRESA_RESUELTA AND nombre = 'Lote parrillero demo â€” Norte' AND modulo_origen = @MODULO_CRIANZA
    LIMIT 1
);

SET @LOTE_CERRADO_ID := (
    SELECT id FROM avicola_lote
    WHERE empresa_id = @EMPRESA_RESUELTA AND nombre = 'Lote parrillero demo â€” Sur (cerrado)' AND modulo_origen = @MODULO_CRIANZA
    LIMIT 1
);

INSERT INTO avicola_pesada (lote_id, empresa_id, fecha, peso_promedio, cantidad_pesada, temperatura_ambiente, humedad_ambiente, observaciones)
SELECT @LOTE_ACTIVO_ID, @EMPRESA_RESUELTA, CURDATE() - INTERVAL 21 DAY, 0.850, 200, 22.5, 65.0, 'Pesada semana 1'
WHERE @CARGAR_DATOS_DEMO = 1 AND @LOTE_ACTIVO_ID IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM avicola_pesada p WHERE p.lote_id = @LOTE_ACTIVO_ID AND p.fecha = CURDATE() - INTERVAL 21 DAY);

INSERT INTO avicola_pesada (lote_id, empresa_id, fecha, peso_promedio, cantidad_pesada, temperatura_ambiente, humedad_ambiente, observaciones)
SELECT @LOTE_ACTIVO_ID, @EMPRESA_RESUELTA, CURDATE() - INTERVAL 14 DAY, 1.420, 200, 24.0, 62.0, 'Pesada semana 2'
WHERE @CARGAR_DATOS_DEMO = 1 AND @LOTE_ACTIVO_ID IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM avicola_pesada p WHERE p.lote_id = @LOTE_ACTIVO_ID AND p.fecha = CURDATE() - INTERVAL 14 DAY);

INSERT INTO avicola_pesada (lote_id, empresa_id, fecha, peso_promedio, cantidad_pesada, temperatura_ambiente, humedad_ambiente, observaciones)
SELECT @LOTE_ACTIVO_ID, @EMPRESA_RESUELTA, CURDATE() - INTERVAL 7 DAY, 2.050, 200, 26.0, 58.0, 'Pesada semana 3'
WHERE @CARGAR_DATOS_DEMO = 1 AND @LOTE_ACTIVO_ID IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM avicola_pesada p WHERE p.lote_id = @LOTE_ACTIVO_ID AND p.fecha = CURDATE() - INTERVAL 7 DAY);

INSERT INTO avicola_pesada (lote_id, empresa_id, fecha, peso_promedio, cantidad_pesada, observaciones)
SELECT @LOTE_ACTIVO_ID, @EMPRESA_RESUELTA, CURDATE() - INTERVAL 1 DAY, 2.480, 250, 'Pesada pre-faena'
WHERE @CARGAR_DATOS_DEMO = 1 AND @LOTE_ACTIVO_ID IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM avicola_pesada p WHERE p.lote_id = @LOTE_ACTIVO_ID AND p.fecha = CURDATE() - INTERVAL 1 DAY);

INSERT INTO avicola_muerte (lote_id, empresa_id, fecha, cantidad, causa, observaciones)
SELECT @LOTE_ACTIVO_ID, @EMPRESA_RESUELTA, CURDATE() - INTERVAL 20 DAY, 45, 'Problemas respiratorios', 'Demo'
WHERE @CARGAR_DATOS_DEMO = 1 AND @LOTE_ACTIVO_ID IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM avicola_muerte m WHERE m.lote_id = @LOTE_ACTIVO_ID AND m.fecha = CURDATE() - INTERVAL 20 DAY);

INSERT INTO avicola_muerte (lote_id, empresa_id, fecha, cantidad, causa, observaciones)
SELECT @LOTE_ACTIVO_ID, @EMPRESA_RESUELTA, CURDATE() - INTERVAL 10 DAY, 75, 'Ascitis', 'Demo'
WHERE @CARGAR_DATOS_DEMO = 1 AND @LOTE_ACTIVO_ID IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM avicola_muerte m WHERE m.lote_id = @LOTE_ACTIVO_ID AND m.fecha = CURDATE() - INTERVAL 10 DAY);

INSERT INTO avicola_consumo (lote_id, empresa_id, insumo_id, fecha, cantidad, tipo, campana_id, observaciones)
SELECT @LOTE_ACTIVO_ID, @EMPRESA_RESUELTA, @INSUMO_ALIMENTO_ID, CURDATE() - INTERVAL 14 DAY, 850.000, 'MANUAL', @CAMPANA_ID, 'Demo consumo'
WHERE @CARGAR_DATOS_DEMO = 1 AND @LOTE_ACTIVO_ID IS NOT NULL AND @INSUMO_ALIMENTO_ID IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM avicola_consumo c
      WHERE c.lote_id = @LOTE_ACTIVO_ID AND c.fecha = CURDATE() - INTERVAL 14 DAY AND c.insumo_id = @INSUMO_ALIMENTO_ID
  );

INSERT INTO avicola_consumo (lote_id, empresa_id, insumo_id, fecha, cantidad, tipo, campana_id, observaciones)
SELECT @LOTE_ACTIVO_ID, @EMPRESA_RESUELTA, @INSUMO_ALIMENTO_ID, CURDATE() - INTERVAL 7 DAY, 920.000, 'MANUAL', @CAMPANA_ID, 'Demo consumo'
WHERE @CARGAR_DATOS_DEMO = 1 AND @LOTE_ACTIVO_ID IS NOT NULL AND @INSUMO_ALIMENTO_ID IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM avicola_consumo c
      WHERE c.lote_id = @LOTE_ACTIVO_ID AND c.fecha = CURDATE() - INTERVAL 7 DAY AND c.insumo_id = @INSUMO_ALIMENTO_ID
  );

INSERT INTO avicola_consumo (lote_id, empresa_id, insumo_id, fecha, cantidad, tipo, campana_id, observaciones)
SELECT @LOTE_ACTIVO_ID, @EMPRESA_RESUELTA, @INSUMO_ALIMENTO_ID, CURDATE() - INTERVAL 1 DAY, 980.000, 'MANUAL', @CAMPANA_ID, 'Demo consumo hoy'
WHERE @CARGAR_DATOS_DEMO = 1 AND @LOTE_ACTIVO_ID IS NOT NULL AND @INSUMO_ALIMENTO_ID IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM avicola_consumo c
      WHERE c.lote_id = @LOTE_ACTIVO_ID AND c.fecha = CURDATE() - INTERVAL 1 DAY AND c.insumo_id = @INSUMO_ALIMENTO_ID
  );

INSERT INTO avicola_venta (lote_id, empresa_id, fecha, tipo, cantidad, peso_promedio, campana_id, observaciones)
SELECT @LOTE_ACTIVO_ID, @EMPRESA_RESUELTA, CURDATE() - INTERVAL 3 DAY, 'VENTA_EN_PIE', 500, 2.350, @CAMPANA_ID, 'Venta parcial demo'
WHERE @CARGAR_DATOS_DEMO = 1 AND @LOTE_ACTIVO_ID IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM avicola_venta v
      WHERE v.lote_id = @LOTE_ACTIVO_ID AND v.fecha = CURDATE() - INTERVAL 3 DAY AND v.tipo = 'VENTA_EN_PIE'
  );

UPDATE avicola_lote
SET cantidad_animales = 9500
WHERE @CARGAR_DATOS_DEMO = 1 AND id = @LOTE_ACTIVO_ID AND cantidad_animales = 10000;

INSERT INTO avicola_evento_sanitario (lote_id, empresa_id, fecha, tipo, descripcion, observaciones)
SELECT @LOTE_ACTIVO_ID, @EMPRESA_RESUELTA, CURDATE() - INTERVAL 25 DAY, 'VACUNACION', 'Vacuna Newcastle (demo)', 'AplicaciÃ³n dÃ­a 3'
WHERE @CARGAR_DATOS_DEMO = 1 AND @LOTE_ACTIVO_ID IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM avicola_evento_sanitario e
      WHERE e.lote_id = @LOTE_ACTIVO_ID AND e.fecha = CURDATE() - INTERVAL 25 DAY AND e.tipo = 'VACUNACION'
  );

INSERT INTO avicola_venta (lote_id, empresa_id, fecha, tipo, cantidad, peso_promedio, campana_id, observaciones)
SELECT @LOTE_CERRADO_ID, @EMPRESA_RESUELTA, CURDATE() - INTERVAL 45 DAY, 'FAENA', 8000, 2.650, @CAMPANA_ID, 'Faena total demo'
WHERE @CARGAR_DATOS_DEMO = 1 AND @LOTE_CERRADO_ID IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM avicola_venta v WHERE v.lote_id = @LOTE_CERRADO_ID AND v.tipo = 'FAENA'
  );

SELECT 'AVICOLA_CRIANZA habilitado' AS resultado,
       @EMPRESA_RESUELTA AS empresa_id,
       (SELECT COUNT(*) FROM avicola_establecimiento e WHERE e.empresa_id = @EMPRESA_RESUELTA AND e.modulo_origen = @MODULO_CRIANZA) AS galpones,
       (SELECT COUNT(*) FROM avicola_raza r WHERE r.empresa_id = @EMPRESA_RESUELTA AND r.modulo_origen = @MODULO_CRIANZA) AS razas,
       (SELECT COUNT(*) FROM avicola_lote l WHERE l.empresa_id = @EMPRESA_RESUELTA AND l.modulo_origen = @MODULO_CRIANZA) AS lotes;

