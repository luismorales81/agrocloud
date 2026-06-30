-- =============================================================================
-- Datos de demostración — módulo Avícola carne (engorde / parrillero)
-- Ejecutar en MySQL. Requiere migraciones V1_136+ y V1_156 (modulo_origen).
-- Ajustar @EMPRESA_ID. Módulo AVICOLA_CARNE habilitado en company_modules.
-- =============================================================================

SET @EMPRESA_ID := 1;
SET @MODULO_CARNE := 'AVICOLA_CARNE';

SET @CAMPANA_ID := (
    SELECT id FROM core_campanas
    WHERE empresa_id = @EMPRESA_ID AND estado = 'ACTIVO'
    ORDER BY id DESC LIMIT 1
);

SET @USER_DEMO_ID := (SELECT id FROM usuarios WHERE username = 'admin' LIMIT 1);

-- Insumo alimento demo (consumos en lote; sin movimiento inventario en este script)
INSERT INTO cultivo_insumos (
    nombre, tipo, descripcion, unidad_medida, precio_unitario,
    stock_actual, stock_minimo, proveedor, activo, empresa_id, user_id,
    fecha_creacion, fecha_actualizacion
)
SELECT 'Balanceado parrillero 18% PB', 'OTROS', 'Alimento inicio/crecimiento avícola carne', 'kg',
       380.00, 12000.00, 500.00, 'NutriAves demo', TRUE, @EMPRESA_ID, @USER_DEMO_ID, NOW(), NOW()
WHERE @USER_DEMO_ID IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM cultivo_insumos i
      WHERE i.empresa_id = @EMPRESA_ID AND i.nombre = 'Balanceado parrillero 18% PB'
  );

SET @INSUMO_ALIMENTO_ID := (
    SELECT id FROM cultivo_insumos
    WHERE empresa_id = @EMPRESA_ID AND nombre = 'Balanceado parrillero 18% PB'
    LIMIT 1
);

-- Galpón 1 (con polígono para mapa/clima — zona Córdoba demo)
INSERT INTO avicola_establecimiento (
    empresa_id, nombre, ubicacion, coordenadas, observaciones, activo, modulo_origen
)
SELECT @EMPRESA_ID,
       'Galpón demo — Engorde Norte',
       'Ruta 36 km 12, Córdoba',
       '[{"lat":-31.4200,"lng":-64.1880},{"lat":-31.4210,"lng":-64.1860},{"lat":-31.4220,"lng":-64.1890},{"lat":-31.4210,"lng":-64.1910}]',
       'Demo avícola carne — mapa y clima',
       TRUE,
       @MODULO_CARNE
WHERE NOT EXISTS (
    SELECT 1 FROM avicola_establecimiento e
    WHERE e.empresa_id = @EMPRESA_ID AND e.nombre = 'Galpón demo — Engorde Norte' AND e.modulo_origen = @MODULO_CARNE
);

INSERT INTO avicola_establecimiento (
    empresa_id, nombre, ubicacion, coordenadas, observaciones, activo, modulo_origen
)
SELECT @EMPRESA_ID,
       'Galpón demo — Engorde Sur',
       'Camino rural s/n',
       '[{"lat":-31.4350,"lng":-64.1750},{"lat":-31.4360,"lng":-64.1730},{"lat":-31.4370,"lng":-64.1760}]',
       'Segundo galpón demo',
       TRUE,
       @MODULO_CARNE
WHERE NOT EXISTS (
    SELECT 1 FROM avicola_establecimiento e
    WHERE e.empresa_id = @EMPRESA_ID AND e.nombre = 'Galpón demo — Engorde Sur' AND e.modulo_origen = @MODULO_CARNE
);

SET @EST_NORTE_ID := (
    SELECT id FROM avicola_establecimiento
    WHERE empresa_id = @EMPRESA_ID AND nombre = 'Galpón demo — Engorde Norte' AND modulo_origen = @MODULO_CARNE
    LIMIT 1
);

SET @EST_SUR_ID := (
    SELECT id FROM avicola_establecimiento
    WHERE empresa_id = @EMPRESA_ID AND nombre = 'Galpón demo — Engorde Sur' AND modulo_origen = @MODULO_CARNE
    LIMIT 1
);

INSERT INTO avicola_raza (empresa_id, nombre, activo, modulo_origen)
SELECT @EMPRESA_ID, 'Cobb 500', TRUE, @MODULO_CARNE
WHERE NOT EXISTS (
    SELECT 1 FROM avicola_raza r
    WHERE r.empresa_id = @EMPRESA_ID AND r.nombre = 'Cobb 500' AND r.modulo_origen = @MODULO_CARNE
);

INSERT INTO avicola_raza (empresa_id, nombre, activo, modulo_origen)
SELECT @EMPRESA_ID, 'Ross 308', TRUE, @MODULO_CARNE
WHERE NOT EXISTS (
    SELECT 1 FROM avicola_raza r
    WHERE r.empresa_id = @EMPRESA_ID AND r.nombre = 'Ross 308' AND r.modulo_origen = @MODULO_CARNE
);

SET @RAZA_COBB_ID := (
    SELECT id FROM avicola_raza
    WHERE empresa_id = @EMPRESA_ID AND nombre = 'Cobb 500' AND modulo_origen = @MODULO_CARNE LIMIT 1
);

SET @RAZA_ROSS_ID := (
    SELECT id FROM avicola_raza
    WHERE empresa_id = @EMPRESA_ID AND nombre = 'Ross 308' AND modulo_origen = @MODULO_CARNE LIMIT 1
);

-- Lote activo (día 28 de engorde)
INSERT INTO avicola_lote (
    empresa_id, campana_id, modulo_origen, establecimiento_id, raza_id, nombre, especie, origen,
    fecha_ingreso, cantidad_inicial, cantidad_animales, peso_promedio_ingreso, estado, observaciones
)
SELECT @EMPRESA_ID, @CAMPANA_ID, @MODULO_CARNE, @EST_NORTE_ID, @RAZA_COBB_ID,
       'Lote parrillero demo — Norte',
       'POLLO_PARRILLERO', 'EXTERNO',
       CURDATE() - INTERVAL 28 DAY,
       10000, 10000, 0.042,
       'ACTIVO',
       'Generado por DEMO_INSERTAR_AVICOLA_CARNE.sql'
WHERE @EST_NORTE_ID IS NOT NULL AND @RAZA_COBB_ID IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM avicola_lote l
      WHERE l.empresa_id = @EMPRESA_ID AND l.nombre = 'Lote parrillero demo — Norte' AND l.modulo_origen = @MODULO_CARNE
  );

-- Lote cerrado (ciclo anterior faenado)
INSERT INTO avicola_lote (
    empresa_id, campana_id, modulo_origen, establecimiento_id, raza_id, nombre, especie, origen,
    fecha_ingreso, cantidad_inicial, cantidad_animales, peso_promedio_ingreso, estado, fecha_salida, observaciones
)
SELECT @EMPRESA_ID, @CAMPANA_ID, @MODULO_CARNE, @EST_SUR_ID, @RAZA_ROSS_ID,
       'Lote parrillero demo — Sur (cerrado)',
       'POLLO_PARRILLERO', 'EXTERNO',
       CURDATE() - INTERVAL 90 DAY,
       8000, 0, 0.040,
       'CERRADO',
       CURDATE() - INTERVAL 45 DAY,
       'Lote histórico demo — faena completa'
WHERE @EST_SUR_ID IS NOT NULL AND @RAZA_ROSS_ID IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM avicola_lote l
      WHERE l.empresa_id = @EMPRESA_ID AND l.nombre = 'Lote parrillero demo — Sur (cerrado)' AND l.modulo_origen = @MODULO_CARNE
  );

SET @LOTE_ACTIVO_ID := (
    SELECT id FROM avicola_lote
    WHERE empresa_id = @EMPRESA_ID AND nombre = 'Lote parrillero demo — Norte' AND modulo_origen = @MODULO_CARNE
    LIMIT 1
);

SET @LOTE_CERRADO_ID := (
    SELECT id FROM avicola_lote
    WHERE empresa_id = @EMPRESA_ID AND nombre = 'Lote parrillero demo — Sur (cerrado)' AND modulo_origen = @MODULO_CARNE
    LIMIT 1
);

-- Pesadas lote activo (curva de crecimiento)
INSERT INTO avicola_pesada (lote_id, empresa_id, fecha, peso_promedio, cantidad_pesada, temperatura_ambiente, humedad_ambiente, observaciones)
SELECT @LOTE_ACTIVO_ID, @EMPRESA_ID, CURDATE() - INTERVAL 21 DAY, 0.850, 200, 22.5, 65.0, 'Pesada semana 1'
WHERE @LOTE_ACTIVO_ID IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM avicola_pesada p WHERE p.lote_id = @LOTE_ACTIVO_ID AND p.fecha = CURDATE() - INTERVAL 21 DAY);

INSERT INTO avicola_pesada (lote_id, empresa_id, fecha, peso_promedio, cantidad_pesada, temperatura_ambiente, humedad_ambiente, observaciones)
SELECT @LOTE_ACTIVO_ID, @EMPRESA_ID, CURDATE() - INTERVAL 14 DAY, 1.420, 200, 24.0, 62.0, 'Pesada semana 2'
WHERE @LOTE_ACTIVO_ID IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM avicola_pesada p WHERE p.lote_id = @LOTE_ACTIVO_ID AND p.fecha = CURDATE() - INTERVAL 14 DAY);

INSERT INTO avicola_pesada (lote_id, empresa_id, fecha, peso_promedio, cantidad_pesada, temperatura_ambiente, humedad_ambiente, observaciones)
SELECT @LOTE_ACTIVO_ID, @EMPRESA_ID, CURDATE() - INTERVAL 7 DAY, 2.050, 200, 26.0, 58.0, 'Pesada semana 3'
WHERE @LOTE_ACTIVO_ID IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM avicola_pesada p WHERE p.lote_id = @LOTE_ACTIVO_ID AND p.fecha = CURDATE() - INTERVAL 7 DAY);

INSERT INTO avicola_pesada (lote_id, empresa_id, fecha, peso_promedio, cantidad_pesada, observaciones)
SELECT @LOTE_ACTIVO_ID, @EMPRESA_ID, CURDATE() - INTERVAL 1 DAY, 2.480, 250, 'Pesada pre-faena'
WHERE @LOTE_ACTIVO_ID IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM avicola_pesada p WHERE p.lote_id = @LOTE_ACTIVO_ID AND p.fecha = CURDATE() - INTERVAL 1 DAY);

-- Mortalidad (no descuenta cantidad_animales)
INSERT INTO avicola_muerte (lote_id, empresa_id, fecha, cantidad, causa, observaciones)
SELECT @LOTE_ACTIVO_ID, @EMPRESA_ID, CURDATE() - INTERVAL 20 DAY, 45, 'Problemas respiratorios', 'Demo'
WHERE @LOTE_ACTIVO_ID IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM avicola_muerte m WHERE m.lote_id = @LOTE_ACTIVO_ID AND m.fecha = CURDATE() - INTERVAL 20 DAY);

INSERT INTO avicola_muerte (lote_id, empresa_id, fecha, cantidad, causa, observaciones)
SELECT @LOTE_ACTIVO_ID, @EMPRESA_ID, CURDATE() - INTERVAL 10 DAY, 75, 'Ascitis', 'Demo'
WHERE @LOTE_ACTIVO_ID IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM avicola_muerte m WHERE m.lote_id = @LOTE_ACTIVO_ID AND m.fecha = CURDATE() - INTERVAL 10 DAY);

-- Consumos de alimento (últimas 2 semanas, muestra)
INSERT INTO avicola_consumo (lote_id, empresa_id, insumo_id, fecha, cantidad, tipo, campana_id, observaciones)
SELECT @LOTE_ACTIVO_ID, @EMPRESA_ID, @INSUMO_ALIMENTO_ID, CURDATE() - INTERVAL 14 DAY, 850.000, 'MANUAL', @CAMPANA_ID, 'Demo consumo'
WHERE @LOTE_ACTIVO_ID IS NOT NULL AND @INSUMO_ALIMENTO_ID IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM avicola_consumo c
      WHERE c.lote_id = @LOTE_ACTIVO_ID AND c.fecha = CURDATE() - INTERVAL 14 DAY AND c.insumo_id = @INSUMO_ALIMENTO_ID
  );

INSERT INTO avicola_consumo (lote_id, empresa_id, insumo_id, fecha, cantidad, tipo, campana_id, observaciones)
SELECT @LOTE_ACTIVO_ID, @EMPRESA_ID, @INSUMO_ALIMENTO_ID, CURDATE() - INTERVAL 7 DAY, 920.000, 'MANUAL', @CAMPANA_ID, 'Demo consumo'
WHERE @LOTE_ACTIVO_ID IS NOT NULL AND @INSUMO_ALIMENTO_ID IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM avicola_consumo c
      WHERE c.lote_id = @LOTE_ACTIVO_ID AND c.fecha = CURDATE() - INTERVAL 7 DAY AND c.insumo_id = @INSUMO_ALIMENTO_ID
  );

INSERT INTO avicola_consumo (lote_id, empresa_id, insumo_id, fecha, cantidad, tipo, campana_id, observaciones)
SELECT @LOTE_ACTIVO_ID, @EMPRESA_ID, @INSUMO_ALIMENTO_ID, CURDATE() - INTERVAL 1 DAY, 980.000, 'MANUAL', @CAMPANA_ID, 'Demo consumo hoy'
WHERE @LOTE_ACTIVO_ID IS NOT NULL AND @INSUMO_ALIMENTO_ID IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM avicola_consumo c
      WHERE c.lote_id = @LOTE_ACTIVO_ID AND c.fecha = CURDATE() - INTERVAL 1 DAY AND c.insumo_id = @INSUMO_ALIMENTO_ID
  );

-- Venta parcial demo (descuenta plantel)
INSERT INTO avicola_venta (lote_id, empresa_id, fecha, tipo, cantidad, peso_promedio, campana_id, observaciones)
SELECT @LOTE_ACTIVO_ID, @EMPRESA_ID, CURDATE() - INTERVAL 3 DAY, 'VENTA_EN_PIE', 500, 2.350, @CAMPANA_ID, 'Venta parcial demo'
WHERE @LOTE_ACTIVO_ID IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM avicola_venta v
      WHERE v.lote_id = @LOTE_ACTIVO_ID AND v.fecha = CURDATE() - INTERVAL 3 DAY AND v.tipo = 'VENTA_EN_PIE'
  );

-- Ajustar plantel tras venta parcial (script SQL directo; en app lo hace el servicio)
UPDATE avicola_lote
SET cantidad_animales = 9500
WHERE id = @LOTE_ACTIVO_ID AND cantidad_animales = 10000;

-- Sanidad
INSERT INTO avicola_evento_sanitario (lote_id, empresa_id, fecha, tipo, descripcion, observaciones)
SELECT @LOTE_ACTIVO_ID, @EMPRESA_ID, CURDATE() - INTERVAL 25 DAY, 'VACUNACION', 'Vacuna Newcastle (demo)', 'Aplicación día 3'
WHERE @LOTE_ACTIVO_ID IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM avicola_evento_sanitario e
      WHERE e.lote_id = @LOTE_ACTIVO_ID AND e.fecha = CURDATE() - INTERVAL 25 DAY AND e.tipo = 'VACUNACION'
  );

-- Lote cerrado: faena histórica
INSERT INTO avicola_venta (lote_id, empresa_id, fecha, tipo, cantidad, peso_promedio, campana_id, observaciones)
SELECT @LOTE_CERRADO_ID, @EMPRESA_ID, CURDATE() - INTERVAL 45 DAY, 'FAENA', 8000, 2.650, @CAMPANA_ID, 'Faena total demo'
WHERE @LOTE_CERRADO_ID IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM avicola_venta v WHERE v.lote_id = @LOTE_CERRADO_ID AND v.tipo = 'FAENA'
  );

-- Resumen
SELECT 'Avícola carne demo' AS resultado,
       @EMPRESA_ID AS empresa_id,
       (SELECT COUNT(*) FROM avicola_establecimiento e WHERE e.empresa_id = @EMPRESA_ID AND e.modulo_origen = @MODULO_CARNE) AS galpones,
       (SELECT COUNT(*) FROM avicola_raza r WHERE r.empresa_id = @EMPRESA_ID AND r.modulo_origen = @MODULO_CARNE) AS razas,
       (SELECT COUNT(*) FROM avicola_lote l WHERE l.empresa_id = @EMPRESA_ID AND l.modulo_origen = @MODULO_CARNE) AS lotes;
