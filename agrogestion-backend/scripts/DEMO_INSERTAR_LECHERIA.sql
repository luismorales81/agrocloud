-- =============================================================================
-- Datos operativos demo — módulo Lechería (multi-especie)
-- Ejecutar después de HABILITAR_LECHERIA_EMPRESA_DEMO.sql
-- QA: login admin@agrocloud.com → módulo Lechería → /lecheria/panel
-- Idempotente por identificación de animal.
-- =============================================================================

SET @EMPRESA_ID := 1;
SET @NOMBRE_EMPRESA := 'AgroCloud Demo';

SET @EMPRESA_ID := COALESCE(
    (SELECT e.id FROM empresas e WHERE e.nombre = @NOMBRE_EMPRESA LIMIT 1),
    @EMPRESA_ID
);

SET @CAMPANA_ID := (
    SELECT id FROM core_campanas WHERE empresa_id = @EMPRESA_ID AND estado = 'ACTIVA' LIMIT 1
);

SET @RODEO_VACAS_ID := (
    SELECT r.id FROM lecheria_rodeo r
    WHERE r.empresa_id = @EMPRESA_ID AND r.nombre = 'Rodeo Vacas Alto' LIMIT 1
);

SET @RODEO_VAQUILLONAS_ID := (
    SELECT r.id FROM lecheria_rodeo r
    WHERE r.empresa_id = @EMPRESA_ID AND r.nombre = 'Rodeo Vaquillonas' LIMIT 1
);

SET @RODEO_CABRAS_ID := (
    SELECT r.id FROM lecheria_rodeo r
    WHERE r.empresa_id = @EMPRESA_ID AND r.nombre = 'Rodeo Cabras Ordeñe' LIMIT 1
);

SET @RODEO_OVEJAS_ID := (
    SELECT r.id FROM lecheria_rodeo r
    WHERE r.empresa_id = @EMPRESA_ID AND r.nombre = 'Rodeo Ovejas Latxa' LIMIT 1
);

SET @RAZA_HOLSTEIN_ID := (
    SELECT id FROM lecheria_raza
    WHERE empresa_id = @EMPRESA_ID AND especie = 'BOVINO' AND nombre = 'Holstein' LIMIT 1
);

SET @RAZA_JERSEY_ID := (
    SELECT id FROM lecheria_raza
    WHERE empresa_id = @EMPRESA_ID AND especie = 'BOVINO' AND nombre = 'Jersey' LIMIT 1
);

SET @RAZA_SAANEN_ID := (
    SELECT id FROM lecheria_raza
    WHERE empresa_id = @EMPRESA_ID AND especie = 'CAPRINO' AND nombre = 'Saanen' LIMIT 1
);

SET @RAZA_LATXA_ID := (
    SELECT id FROM lecheria_raza
    WHERE empresa_id = @EMPRESA_ID AND especie = 'OVINO' AND nombre = 'Latxa' LIMIT 1
);

-- -----------------------------------------------------------------------------
-- Animales bovinos en lactancia (5 vacas)
-- -----------------------------------------------------------------------------
INSERT INTO lecheria_animal (
    empresa_id, identificacion, especie, sexo, estado, raza_id, rodeo_id,
    campana_id, fecha_nacimiento, fecha_ingreso, activo, observaciones
)
SELECT @EMPRESA_ID, 'AR-1001', 'BOVINO', 'HEMBRA', 'LACTANDO', @RAZA_HOLSTEIN_ID, @RODEO_VACAS_ID,
       @CAMPANA_ID, DATE_SUB(CURDATE(), INTERVAL 4 YEAR), DATE_SUB(CURDATE(), INTERVAL 3 YEAR), TRUE,
       'Vaca demo — alta producción'
WHERE @RODEO_VACAS_ID IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM lecheria_animal a
      WHERE a.empresa_id = @EMPRESA_ID AND a.identificacion = 'AR-1001'
  );

INSERT INTO lecheria_animal (
    empresa_id, identificacion, especie, sexo, estado, raza_id, rodeo_id,
    campana_id, fecha_nacimiento, fecha_ingreso, activo, observaciones
)
SELECT @EMPRESA_ID, 'AR-1002', 'BOVINO', 'HEMBRA', 'LACTANDO', @RAZA_HOLSTEIN_ID, @RODEO_VACAS_ID,
       @CAMPANA_ID, DATE_SUB(CURDATE(), INTERVAL 5 YEAR), DATE_SUB(CURDATE(), INTERVAL 4 YEAR), TRUE,
       'Vaca demo — RCS elevado (alerta panel)'
WHERE @RODEO_VACAS_ID IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM lecheria_animal a
      WHERE a.empresa_id = @EMPRESA_ID AND a.identificacion = 'AR-1002'
  );

INSERT INTO lecheria_animal (
    empresa_id, identificacion, especie, sexo, estado, raza_id, rodeo_id,
    campana_id, fecha_nacimiento, fecha_ingreso, activo, observaciones
)
SELECT @EMPRESA_ID, 'AR-1003', 'BOVINO', 'HEMBRA', 'LACTANDO', @RAZA_JERSEY_ID, @RODEO_VACAS_ID,
       @CAMPANA_ID, DATE_SUB(CURDATE(), INTERVAL 3 YEAR), DATE_SUB(CURDATE(), INTERVAL 2 YEAR), TRUE,
       'Vaca Jersey demo'
WHERE @RODEO_VACAS_ID IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM lecheria_animal a
      WHERE a.empresa_id = @EMPRESA_ID AND a.identificacion = 'AR-1003'
  );

INSERT INTO lecheria_animal (
    empresa_id, identificacion, especie, sexo, estado, raza_id, rodeo_id,
    campana_id, fecha_nacimiento, fecha_ingreso, activo, observaciones
)
SELECT @EMPRESA_ID, 'AR-1004', 'BOVINO', 'HEMBRA', 'LACTANDO', @RAZA_HOLSTEIN_ID, @RODEO_VACAS_ID,
       @CAMPANA_ID, DATE_SUB(CURDATE(), INTERVAL 6 YEAR), DATE_SUB(CURDATE(), INTERVAL 5 YEAR), TRUE,
       'Vaca demo — lactancia 3'
WHERE @RODEO_VACAS_ID IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM lecheria_animal a
      WHERE a.empresa_id = @EMPRESA_ID AND a.identificacion = 'AR-1004'
  );

INSERT INTO lecheria_animal (
    empresa_id, identificacion, especie, sexo, estado, raza_id, rodeo_id,
    campana_id, fecha_nacimiento, fecha_ingreso, activo, observaciones
)
SELECT @EMPRESA_ID, 'AR-1005', 'BOVINO', 'HEMBRA', 'LACTANDO', @RAZA_HOLSTEIN_ID, @RODEO_VACAS_ID,
       @CAMPANA_ID, DATE_SUB(CURDATE(), INTERVAL 4 YEAR), DATE_SUB(CURDATE(), INTERVAL 3 YEAR), TRUE,
       'Vaca demo — a secar (DIM alto)'
WHERE @RODEO_VACAS_ID IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM lecheria_animal a
      WHERE a.empresa_id = @EMPRESA_ID AND a.identificacion = 'AR-1005'
  );

-- Vaquillona y preñada (acciones panel)
INSERT INTO lecheria_animal (
    empresa_id, identificacion, especie, sexo, estado, raza_id, rodeo_id,
    campana_id, fecha_nacimiento, fecha_ingreso, activo, observaciones
)
SELECT @EMPRESA_ID, 'AR-1006', 'BOVINO', 'HEMBRA', 'VAQUILLONA', @RAZA_HOLSTEIN_ID, @RODEO_VAQUILLONAS_ID,
       @CAMPANA_ID, DATE_SUB(CURDATE(), INTERVAL 22 MONTH), DATE_SUB(CURDATE(), INTERVAL 20 MONTH), TRUE,
       'Vaquillona demo — sin servicio'
WHERE @RODEO_VAQUILLONAS_ID IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM lecheria_animal a
      WHERE a.empresa_id = @EMPRESA_ID AND a.identificacion = 'AR-1006'
  );

INSERT INTO lecheria_animal (
    empresa_id, identificacion, especie, sexo, estado, raza_id, rodeo_id,
    campana_id, fecha_nacimiento, fecha_ingreso, activo, observaciones
)
SELECT @EMPRESA_ID, 'AR-1007', 'BOVINO', 'HEMBRA', 'PRENADA', @RAZA_HOLSTEIN_ID, @RODEO_VACAS_ID,
       @CAMPANA_ID, DATE_SUB(CURDATE(), INTERVAL 3 YEAR), DATE_SUB(CURDATE(), INTERVAL 2 YEAR), TRUE,
       'Vaca seca preñada — parto próximo'
WHERE @RODEO_VACAS_ID IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM lecheria_animal a
      WHERE a.empresa_id = @EMPRESA_ID AND a.identificacion = 'AR-1007'
  );

-- Caprinos y ovino
INSERT INTO lecheria_animal (
    empresa_id, identificacion, especie, sexo, estado, raza_id, rodeo_id,
    campana_id, fecha_nacimiento, fecha_ingreso, activo, observaciones
)
SELECT @EMPRESA_ID, 'CAP-201', 'CAPRINO', 'HEMBRA', 'LACTANDO', @RAZA_SAANEN_ID, @RODEO_CABRAS_ID,
       @CAMPANA_ID, DATE_SUB(CURDATE(), INTERVAL 3 YEAR), DATE_SUB(CURDATE(), INTERVAL 2 YEAR), TRUE,
       'Cabra Saanen demo'
WHERE @RODEO_CABRAS_ID IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM lecheria_animal a
      WHERE a.empresa_id = @EMPRESA_ID AND a.identificacion = 'CAP-201'
  );

INSERT INTO lecheria_animal (
    empresa_id, identificacion, especie, sexo, estado, raza_id, rodeo_id,
    campana_id, fecha_nacimiento, fecha_ingreso, activo, observaciones
)
SELECT @EMPRESA_ID, 'CAP-202', 'CAPRINO', 'HEMBRA', 'LACTANDO', @RAZA_SAANEN_ID, @RODEO_CABRAS_ID,
       @CAMPANA_ID, DATE_SUB(CURDATE(), INTERVAL 2 YEAR), DATE_SUB(CURDATE(), INTERVAL 18 MONTH), TRUE,
       'Cabra Saanen demo 2'
WHERE @RODEO_CABRAS_ID IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM lecheria_animal a
      WHERE a.empresa_id = @EMPRESA_ID AND a.identificacion = 'CAP-202'
  );

INSERT INTO lecheria_animal (
    empresa_id, identificacion, especie, sexo, estado, raza_id, rodeo_id,
    campana_id, fecha_nacimiento, fecha_ingreso, activo, observaciones
)
SELECT @EMPRESA_ID, 'OV-301', 'OVINO', 'HEMBRA', 'LACTANDO', @RAZA_LATXA_ID, @RODEO_OVEJAS_ID,
       @CAMPANA_ID, DATE_SUB(CURDATE(), INTERVAL 4 YEAR), DATE_SUB(CURDATE(), INTERVAL 3 YEAR), TRUE,
       'Oveja Latxa demo'
WHERE @RODEO_OVEJAS_ID IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM lecheria_animal a
      WHERE a.empresa_id = @EMPRESA_ID AND a.identificacion = 'OV-301'
  );

-- -----------------------------------------------------------------------------
-- Lactancias activas (partos hace 90–310 días según animal)
-- -----------------------------------------------------------------------------
INSERT INTO lecheria_lactancia (animal_id, empresa_id, numero_lactancia, fecha_parto, activa)
SELECT a.id, @EMPRESA_ID, 2, DATE_SUB(CURDATE(), INTERVAL 95 DAY), TRUE
FROM lecheria_animal a
WHERE a.empresa_id = @EMPRESA_ID AND a.identificacion = 'AR-1001'
  AND NOT EXISTS (
      SELECT 1 FROM lecheria_lactancia l WHERE l.animal_id = a.id AND l.activa = TRUE
  );

INSERT INTO lecheria_lactancia (animal_id, empresa_id, numero_lactancia, fecha_parto, activa)
SELECT a.id, @EMPRESA_ID, 3, DATE_SUB(CURDATE(), INTERVAL 110 DAY), TRUE
FROM lecheria_animal a
WHERE a.empresa_id = @EMPRESA_ID AND a.identificacion = 'AR-1002'
  AND NOT EXISTS (
      SELECT 1 FROM lecheria_lactancia l WHERE l.animal_id = a.id AND l.activa = TRUE
  );

INSERT INTO lecheria_lactancia (animal_id, empresa_id, numero_lactancia, fecha_parto, activa)
SELECT a.id, @EMPRESA_ID, 1, DATE_SUB(CURDATE(), INTERVAL 75 DAY), TRUE
FROM lecheria_animal a
WHERE a.empresa_id = @EMPRESA_ID AND a.identificacion = 'AR-1003'
  AND NOT EXISTS (
      SELECT 1 FROM lecheria_lactancia l WHERE l.animal_id = a.id AND l.activa = TRUE
  );

INSERT INTO lecheria_lactancia (animal_id, empresa_id, numero_lactancia, fecha_parto, activa)
SELECT a.id, @EMPRESA_ID, 3, DATE_SUB(CURDATE(), INTERVAL 130 DAY), TRUE
FROM lecheria_animal a
WHERE a.empresa_id = @EMPRESA_ID AND a.identificacion = 'AR-1004'
  AND NOT EXISTS (
      SELECT 1 FROM lecheria_lactancia l WHERE l.animal_id = a.id AND l.activa = TRUE
  );

INSERT INTO lecheria_lactancia (animal_id, empresa_id, numero_lactancia, fecha_parto, activa)
SELECT a.id, @EMPRESA_ID, 4, DATE_SUB(CURDATE(), INTERVAL 300 DAY), TRUE
FROM lecheria_animal a
WHERE a.empresa_id = @EMPRESA_ID AND a.identificacion = 'AR-1005'
  AND NOT EXISTS (
      SELECT 1 FROM lecheria_lactancia l WHERE l.animal_id = a.id AND l.activa = TRUE
  );

INSERT INTO lecheria_lactancia (animal_id, empresa_id, numero_lactancia, fecha_parto, activa)
SELECT a.id, @EMPRESA_ID, 2, DATE_SUB(CURDATE(), INTERVAL 60 DAY), TRUE
FROM lecheria_animal a
WHERE a.empresa_id = @EMPRESA_ID AND a.identificacion = 'CAP-201'
  AND NOT EXISTS (
      SELECT 1 FROM lecheria_lactancia l WHERE l.animal_id = a.id AND l.activa = TRUE
  );

INSERT INTO lecheria_lactancia (animal_id, empresa_id, numero_lactancia, fecha_parto, activa)
SELECT a.id, @EMPRESA_ID, 1, DATE_SUB(CURDATE(), INTERVAL 45 DAY), TRUE
FROM lecheria_animal a
WHERE a.empresa_id = @EMPRESA_ID AND a.identificacion = 'CAP-202'
  AND NOT EXISTS (
      SELECT 1 FROM lecheria_lactancia l WHERE l.animal_id = a.id AND l.activa = TRUE
  );

INSERT INTO lecheria_lactancia (animal_id, empresa_id, numero_lactancia, fecha_parto, activa)
SELECT a.id, @EMPRESA_ID, 2, DATE_SUB(CURDATE(), INTERVAL 55 DAY), TRUE
FROM lecheria_animal a
WHERE a.empresa_id = @EMPRESA_ID AND a.identificacion = 'OV-301'
  AND NOT EXISTS (
      SELECT 1 FROM lecheria_lactancia l WHERE l.animal_id = a.id AND l.activa = TRUE
  );

-- Lactancia cerrada para AR-1007 (seca preñada)
INSERT INTO lecheria_lactancia (animal_id, empresa_id, numero_lactancia, fecha_parto, fecha_secado, activa)
SELECT a.id, @EMPRESA_ID, 2, DATE_SUB(CURDATE(), INTERVAL 400 DAY), DATE_SUB(CURDATE(), INTERVAL 60 DAY), FALSE
FROM lecheria_animal a
WHERE a.empresa_id = @EMPRESA_ID AND a.identificacion = 'AR-1007'
  AND NOT EXISTS (
      SELECT 1 FROM lecheria_lactancia l
      WHERE l.animal_id = a.id AND l.numero_lactancia = 2
  );

-- -----------------------------------------------------------------------------
-- Eventos reproductivos demo
-- -----------------------------------------------------------------------------
INSERT INTO lecheria_evento_reproductivo (
    animal_id, empresa_id, tipo, fecha, resultado, toro_pajuela, fecha_prevista_parto, observaciones
)
SELECT a.id, @EMPRESA_ID, 'SERVICIO', DATE_SUB(CURDATE(), INTERVAL 200 DAY), NULL,
       'Toro Holstein 4521', NULL, 'IA demo'
FROM lecheria_animal a
WHERE a.empresa_id = @EMPRESA_ID AND a.identificacion = 'AR-1001'
  AND NOT EXISTS (
      SELECT 1 FROM lecheria_evento_reproductivo e
      WHERE e.animal_id = a.id AND e.tipo = 'SERVICIO' AND e.fecha = DATE_SUB(CURDATE(), INTERVAL 200 DAY)
  );

INSERT INTO lecheria_evento_reproductivo (
    animal_id, empresa_id, tipo, fecha, resultado, fecha_prevista_parto, observaciones
)
SELECT a.id, @EMPRESA_ID, 'PARTO', DATE_SUB(CURDATE(), INTERVAL 95 DAY), 'VIVO', NULL, 'Parto que abrió lactancia actual'
FROM lecheria_animal a
WHERE a.empresa_id = @EMPRESA_ID AND a.identificacion = 'AR-1001'
  AND NOT EXISTS (
      SELECT 1 FROM lecheria_evento_reproductivo e
      WHERE e.animal_id = a.id AND e.tipo = 'PARTO' AND e.fecha = DATE_SUB(CURDATE(), INTERVAL 95 DAY)
  );

INSERT INTO lecheria_evento_reproductivo (
    animal_id, empresa_id, tipo, fecha, resultado, fecha_prevista_parto, observaciones
)
SELECT a.id, @EMPRESA_ID, 'TACTO', DATE_SUB(CURDATE(), INTERVAL 30 DAY), 'POSITIVO',
       DATE_ADD(CURDATE(), INTERVAL 5 DAY), 'Parto previsto en 5 días — acción panel'
FROM lecheria_animal a
WHERE a.empresa_id = @EMPRESA_ID AND a.identificacion = 'AR-1007'
  AND NOT EXISTS (
      SELECT 1 FROM lecheria_evento_reproductivo e
      WHERE e.animal_id = a.id AND e.tipo = 'TACTO' AND e.resultado = 'POSITIVO'
  );

-- -----------------------------------------------------------------------------
-- Ordeñes últimos 3 días (vacas bovinas + caprinos + ovino)
-- -----------------------------------------------------------------------------
INSERT INTO lecheria_registro_ordene (
    animal_id, lactancia_id, empresa_id, fecha, turno, litros,
    grasa_pct, proteina_pct, rcs, temperatura_ambiente, humedad_ambiente, observaciones
)
SELECT a.id, l.id, @EMPRESA_ID, CURDATE() - INTERVAL 1 DAY, 'AM', 14.500,
       3.80, 3.20, 120000, 18.5, 72.0, 'Demo ordeñe mañana'
FROM lecheria_animal a
INNER JOIN lecheria_lactancia l ON l.animal_id = a.id AND l.activa = TRUE
WHERE a.empresa_id = @EMPRESA_ID AND a.identificacion = 'AR-1001'
  AND NOT EXISTS (
      SELECT 1 FROM lecheria_registro_ordene o
      WHERE o.animal_id = a.id AND o.fecha = CURDATE() - INTERVAL 1 DAY AND o.turno = 'AM'
  );

INSERT INTO lecheria_registro_ordene (
    animal_id, lactancia_id, empresa_id, fecha, turno, litros,
    grasa_pct, proteina_pct, rcs, temperatura_ambiente, humedad_ambiente, observaciones
)
SELECT a.id, l.id, @EMPRESA_ID, CURDATE() - INTERVAL 1 DAY, 'PM', 13.200,
       3.75, 3.15, 125000, 22.0, 65.0, 'Demo ordeñe tarde'
FROM lecheria_animal a
INNER JOIN lecheria_lactancia l ON l.animal_id = a.id AND l.activa = TRUE
WHERE a.empresa_id = @EMPRESA_ID AND a.identificacion = 'AR-1001'
  AND NOT EXISTS (
      SELECT 1 FROM lecheria_registro_ordene o
      WHERE o.animal_id = a.id AND o.fecha = CURDATE() - INTERVAL 1 DAY AND o.turno = 'PM'
  );

-- AR-1002: RCS alto para alerta panel
INSERT INTO lecheria_registro_ordene (
    animal_id, lactancia_id, empresa_id, fecha, turno, litros, rcs, observaciones
)
SELECT a.id, l.id, @EMPRESA_ID, CURDATE(), 'AM', 11.800, 450000, 'RCS elevado demo'
FROM lecheria_animal a
INNER JOIN lecheria_lactancia l ON l.animal_id = a.id AND l.activa = TRUE
WHERE a.empresa_id = @EMPRESA_ID AND a.identificacion = 'AR-1002'
  AND NOT EXISTS (
      SELECT 1 FROM lecheria_registro_ordene o
      WHERE o.animal_id = a.id AND o.fecha = CURDATE() AND o.turno = 'AM'
  );

-- Resto vacas: ordeñe TOTAL hoy
INSERT INTO lecheria_registro_ordene (
    animal_id, lactancia_id, empresa_id, fecha, turno, litros, grasa_pct, proteina_pct, rcs
)
SELECT a.id, l.id, @EMPRESA_ID, CURDATE(), 'TOTAL', 26.500, 3.60, 3.10, 180000
FROM lecheria_animal a
INNER JOIN lecheria_lactancia l ON l.animal_id = a.id AND l.activa = TRUE
WHERE a.empresa_id = @EMPRESA_ID AND a.identificacion = 'AR-1003'
  AND NOT EXISTS (
      SELECT 1 FROM lecheria_registro_ordene o
      WHERE o.animal_id = a.id AND o.fecha = CURDATE() AND o.turno = 'TOTAL'
  );

INSERT INTO lecheria_registro_ordene (
    animal_id, lactancia_id, empresa_id, fecha, turno, litros, grasa_pct, proteina_pct, rcs
)
SELECT a.id, l.id, @EMPRESA_ID, CURDATE(), 'TOTAL', 24.000, 3.55, 3.05, 150000
FROM lecheria_animal a
INNER JOIN lecheria_lactancia l ON l.animal_id = a.id AND l.activa = TRUE
WHERE a.empresa_id = @EMPRESA_ID AND a.identificacion = 'AR-1004'
  AND NOT EXISTS (
      SELECT 1 FROM lecheria_registro_ordene o
      WHERE o.animal_id = a.id AND o.fecha = CURDATE() AND o.turno = 'TOTAL'
  );

INSERT INTO lecheria_registro_ordene (
    animal_id, lactancia_id, empresa_id, fecha, turno, litros, grasa_pct, proteina_pct, rcs
)
SELECT a.id, l.id, @EMPRESA_ID, CURDATE(), 'TOTAL', 18.000, 3.40, 2.95, 210000
FROM lecheria_animal a
INNER JOIN lecheria_lactancia l ON l.animal_id = a.id AND l.activa = TRUE
WHERE a.empresa_id = @EMPRESA_ID AND a.identificacion = 'AR-1005'
  AND NOT EXISTS (
      SELECT 1 FROM lecheria_registro_ordene o
      WHERE o.animal_id = a.id AND o.fecha = CURDATE() AND o.turno = 'TOTAL'
  );

-- Caprinos (litros menores)
INSERT INTO lecheria_registro_ordene (
    animal_id, lactancia_id, empresa_id, fecha, turno, litros, grasa_pct, proteina_pct, rcs
)
SELECT a.id, l.id, @EMPRESA_ID, CURDATE(), 'TOTAL', 3.200, 4.10, 3.30, 380000
FROM lecheria_animal a
INNER JOIN lecheria_lactancia l ON l.animal_id = a.id AND l.activa = TRUE
WHERE a.empresa_id = @EMPRESA_ID AND a.identificacion = 'CAP-201'
  AND NOT EXISTS (
      SELECT 1 FROM lecheria_registro_ordene o
      WHERE o.animal_id = a.id AND o.fecha = CURDATE() AND o.turno = 'TOTAL'
  );

INSERT INTO lecheria_registro_ordene (
    animal_id, lactancia_id, empresa_id, fecha, turno, litros, grasa_pct, proteina_pct, rcs
)
SELECT a.id, l.id, @EMPRESA_ID, CURDATE(), 'TOTAL', 2.800, 4.00, 3.25, 420000
FROM lecheria_animal a
INNER JOIN lecheria_lactancia l ON l.animal_id = a.id AND l.activa = TRUE
WHERE a.empresa_id = @EMPRESA_ID AND a.identificacion = 'CAP-202'
  AND NOT EXISTS (
      SELECT 1 FROM lecheria_registro_ordene o
      WHERE o.animal_id = a.id AND o.fecha = CURDATE() AND o.turno = 'TOTAL'
  );

-- Ovino
INSERT INTO lecheria_registro_ordene (
    animal_id, lactancia_id, empresa_id, fecha, turno, litros, grasa_pct, proteina_pct, rcs
)
SELECT a.id, l.id, @EMPRESA_ID, CURDATE(), 'TOTAL', 1.500, 6.50, 4.80, 290000
FROM lecheria_animal a
INNER JOIN lecheria_lactancia l ON l.animal_id = a.id AND l.activa = TRUE
WHERE a.empresa_id = @EMPRESA_ID AND a.identificacion = 'OV-301'
  AND NOT EXISTS (
      SELECT 1 FROM lecheria_registro_ordene o
      WHERE o.animal_id = a.id AND o.fecha = CURDATE() AND o.turno = 'TOTAL'
  );

-- Ordeñes ayer (más volumen histórico para panel)
INSERT INTO lecheria_registro_ordene (
    animal_id, lactancia_id, empresa_id, fecha, turno, litros, rcs
)
SELECT a.id, l.id, @EMPRESA_ID, CURDATE() - INTERVAL 2 DAY, 'TOTAL', 27.000, 130000
FROM lecheria_animal a
INNER JOIN lecheria_lactancia l ON l.animal_id = a.id AND l.activa = TRUE
WHERE a.empresa_id = @EMPRESA_ID AND a.identificacion IN ('AR-1001','AR-1003','AR-1004')
  AND NOT EXISTS (
      SELECT 1 FROM lecheria_registro_ordene o
      WHERE o.animal_id = a.id AND o.fecha = CURDATE() - INTERVAL 2 DAY AND o.turno = 'TOTAL'
  );

-- -----------------------------------------------------------------------------
-- Score corporal, sanidad, consumo, venta
-- -----------------------------------------------------------------------------
INSERT INTO lecheria_score_corporal (animal_id, empresa_id, fecha, valor, observaciones)
SELECT a.id, @EMPRESA_ID, CURDATE() - INTERVAL 15 DAY, 3.0, 'ECC demo'
FROM lecheria_animal a
WHERE a.empresa_id = @EMPRESA_ID AND a.identificacion = 'AR-1001'
  AND NOT EXISTS (
      SELECT 1 FROM lecheria_score_corporal s
      WHERE s.animal_id = a.id AND s.fecha = CURDATE() - INTERVAL 15 DAY
  );

SET @INSUMO_ANTIBIOTICO_ID := (
    SELECT id FROM cultivo_insumos
    WHERE empresa_id = @EMPRESA_ID AND nombre = 'Antibiótico intramamario demo' LIMIT 1
);

INSERT INTO lecheria_evento_sanitario (
    animal_id, empresa_id, tipo, fecha, descripcion, insumo_id, cantidad, dias_retiro
)
SELECT a.id, @EMPRESA_ID, 'MASTITIS', DATE_SUB(CURDATE(), INTERVAL 3 DAY),
       'Tratamiento mastitis demo — retiro activo', @INSUMO_ANTIBIOTICO_ID, 1.000, 4
FROM lecheria_animal a
WHERE a.empresa_id = @EMPRESA_ID AND a.identificacion = 'AR-1002'
  AND NOT EXISTS (
      SELECT 1 FROM lecheria_evento_sanitario e
      WHERE e.animal_id = a.id AND e.tipo = 'MASTITIS'
        AND e.fecha = DATE_SUB(CURDATE(), INTERVAL 3 DAY)
  );

INSERT INTO lecheria_consumo (rodeo_id, empresa_id, campana_id, insumo_id, fecha, cantidad_kg, observaciones)
SELECT @RODEO_VACAS_ID, @EMPRESA_ID, @CAMPANA_ID, i.id, CURDATE() - INTERVAL 1 DAY, 450.000,
       'Consumo alimento demo — rodeo vacas'
FROM cultivo_insumos i
WHERE i.empresa_id = @EMPRESA_ID AND i.nombre = 'Balanceado vacas en lactancia 18% PB'
  AND @RODEO_VACAS_ID IS NOT NULL AND @CAMPANA_ID IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM lecheria_consumo c
      WHERE c.rodeo_id = @RODEO_VACAS_ID
        AND c.fecha = CURDATE() - INTERVAL 1 DAY
        AND c.observaciones = 'Consumo alimento demo — rodeo vacas'
  );

INSERT INTO lecheria_venta_leche (
    empresa_id, campana_id, fecha, litros, precio_litro, total, comprador, observaciones
)
SELECT @EMPRESA_ID, @CAMPANA_ID, CURDATE() - INTERVAL 7 DAY, 1250.000, 185.50,
       231875.00, 'Cooperativa Láctea Demo', 'Venta semanal demo'
WHERE @CAMPANA_ID IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM lecheria_venta_leche v
      WHERE v.empresa_id = @EMPRESA_ID
        AND v.comprador = 'Cooperativa Láctea Demo'
        AND v.fecha = CURDATE() - INTERVAL 7 DAY
  );

-- Movimiento SENASA borrador (baja futura)
INSERT INTO lecheria_movimiento_senasa (
    empresa_id, animal_id, tipo_movimiento, fecha, datos_json, exportado
)
SELECT @EMPRESA_ID, a.id, 'BAJA', CURDATE(), '{"motivo":"demo","observacion":"Pendiente exportación SENASA"}', FALSE
FROM lecheria_animal a
WHERE a.empresa_id = @EMPRESA_ID AND a.identificacion = 'AR-1006'
  AND NOT EXISTS (
      SELECT 1 FROM lecheria_movimiento_senasa m
      WHERE m.animal_id = a.id AND m.tipo_movimiento = 'BAJA'
  );

-- Resumen
SELECT 'Demo lechería cargada' AS resultado,
       @EMPRESA_ID AS empresa_id,
       (SELECT COUNT(*) FROM lecheria_animal WHERE empresa_id = @EMPRESA_ID AND activo = TRUE) AS animales_activos,
       (SELECT COUNT(*) FROM lecheria_animal WHERE empresa_id = @EMPRESA_ID AND estado = 'LACTANDO') AS lactando,
       (SELECT COUNT(*) FROM lecheria_registro_ordene WHERE empresa_id = @EMPRESA_ID) AS registros_ordene,
       (SELECT SUM(litros) FROM lecheria_registro_ordene WHERE empresa_id = @EMPRESA_ID) AS litros_totales_demo;
