-- =============================================================================
-- Datos de demostración — módulo Porcinos v2
-- Ciclo completo: madres, gestación, lactancia, destete → lote, engorde, ventas.
-- Idempotente: no duplica si ya existen registros demo.
--
-- Uso:
--   mysql -u root -p agrocloud < scripts/DEMO_INSERTAR_PORCINOS_V2.sql
--   o: scripts\ejecutar-demo-porcinos.bat
-- =============================================================================

SET @USUARIO_DEMO   := 'admin';
SET @NOMBRE_EMPRESA := 'AgroCloud Demo';

SET @EMPRESA_ID := COALESCE(
    (SELECT e.id FROM empresas e WHERE e.nombre = @NOMBRE_EMPRESA LIMIT 1),
    (
        SELECT ue.empresa_id
        FROM usuario_empresas ue
        INNER JOIN usuarios u ON u.id = ue.usuario_id
        WHERE u.username = @USUARIO_DEMO AND ue.estado = 'ACTIVO'
        ORDER BY ue.id LIMIT 1
    ),
    1
);

SET @USER_DEMO_ID := (SELECT id FROM usuarios WHERE username = @USUARIO_DEMO LIMIT 1);

-- Módulos PORCINOS (v2) y pigs (legacy)
INSERT INTO modules (name, code, description, active)
SELECT 'Porcinos', 'PORCINOS', 'Gestión porcina v2', TRUE
WHERE NOT EXISTS (SELECT 1 FROM modules WHERE code = 'PORCINOS');

INSERT INTO modules (name, code, description, active)
SELECT 'Porcinos', 'pigs', 'Módulo de gestión de porcinos', TRUE
WHERE NOT EXISTS (SELECT 1 FROM modules WHERE code = 'pigs');

UPDATE modules SET active = TRUE WHERE code IN ('PORCINOS', 'pigs');

INSERT INTO company_modules (company_id, module_id, enabled)
SELECT @EMPRESA_ID, m.id, TRUE
FROM modules m
WHERE m.code IN ('PORCINOS', 'pigs')
  AND NOT EXISTS (
      SELECT 1 FROM company_modules cm
      WHERE cm.company_id = @EMPRESA_ID AND cm.module_id = m.id
  );

UPDATE company_modules cm
INNER JOIN modules m ON m.id = cm.module_id
SET cm.enabled = TRUE
WHERE cm.company_id = @EMPRESA_ID AND m.code IN ('PORCINOS', 'pigs');

-- Campaña activa
INSERT INTO core_campanas (empresa_id, codigo, nombre, fecha_inicio, fecha_fin, estado, es_default, created_at)
SELECT @EMPRESA_ID,
       CONCAT(
           IF(MONTH(CURDATE()) >= 10, YEAR(CURDATE()), YEAR(CURDATE()) - 1),
           '-',
           LPAD(MOD(IF(MONTH(CURDATE()) >= 10, YEAR(CURDATE()) + 1, YEAR(CURDATE())), 100), 2, '0')
       ),
       CONCAT('Campaña demo ',
           IF(MONTH(CURDATE()) >= 10, YEAR(CURDATE()), YEAR(CURDATE()) - 1),
           '/',
           MOD(IF(MONTH(CURDATE()) >= 10, YEAR(CURDATE()) + 1, YEAR(CURDATE())), 100)
       ),
       DATE(CONCAT(IF(MONTH(CURDATE()) >= 10, YEAR(CURDATE()), YEAR(CURDATE()) - 1), '-10-01')),
       DATE(CONCAT(IF(MONTH(CURDATE()) >= 10, YEAR(CURDATE()) + 1, YEAR(CURDATE())), '-09-30')),
       'ACTIVA', 1, NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM core_campanas c
    WHERE c.empresa_id = @EMPRESA_ID AND c.estado = 'ACTIVA'
);

SET @CAMPANA_ID := (
    SELECT id FROM core_campanas
    WHERE empresa_id = @EMPRESA_ID AND estado = 'ACTIVA'
    ORDER BY es_default DESC, id DESC LIMIT 1
);

-- Insumo alimento
INSERT INTO cultivo_insumos (
    nombre, tipo, descripcion, unidad_medida, precio_unitario,
    stock_actual, stock_minimo, proveedor, activo, empresa_id, user_id,
    fecha_creacion, fecha_actualizacion
)
SELECT 'Balanceado porcino engorde 16% PB', 'OTROS', 'Alimento demo módulo porcinos', 'kg',
       420.00, 25000.00, 1000.00, 'NutriPorc demo', TRUE, @EMPRESA_ID, @USER_DEMO_ID, NOW(), NOW()
WHERE @USER_DEMO_ID IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM cultivo_insumos i
      WHERE i.empresa_id = @EMPRESA_ID AND i.nombre = 'Balanceado porcino engorde 16% PB'
  );

SET @INSUMO_ALIMENTO_ID := (
    SELECT id FROM cultivo_insumos
    WHERE empresa_id = @EMPRESA_ID AND nombre = 'Balanceado porcino engorde 16% PB' LIMIT 1
);

-- Establecimiento (uno por empresa)
INSERT INTO porcinos_establecimiento (
    empresa_id, nombre, ubicacion, coordenadas,
    dias_gestacion, dias_lactancia, dias_entre_celos, faena_habilitada, capacidad_cabezas, activo
)
SELECT @EMPRESA_ID,
       'Granja Porcina Demo',
       'Ruta 9 km 45, Pilar, Buenos Aires',
       '[{"lat":-34.4580,"lng":-58.9140},{"lat":-34.4590,"lng":-58.9120},{"lat":-34.4600,"lng":-58.9150}]',
       114, 21, 21, TRUE, 800, TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM porcinos_establecimiento e WHERE e.empresa_id = @EMPRESA_ID
);

SET @EST_ID := (SELECT id FROM porcinos_establecimiento WHERE empresa_id = @EMPRESA_ID LIMIT 1);

-- Galpones
INSERT INTO porcinos_galpon (establecimiento_id, nombre, capacidad_cabezas, estado, activo)
SELECT @EST_ID, 'Maternidad demo', 120, 'OCUPADO', TRUE
WHERE @EST_ID IS NOT NULL AND NOT EXISTS (
    SELECT 1 FROM porcinos_galpon g WHERE g.establecimiento_id = @EST_ID AND g.nombre = 'Maternidad demo'
);

INSERT INTO porcinos_galpon (establecimiento_id, nombre, capacidad_cabezas, estado, activo)
SELECT @EST_ID, 'Engorde A demo', 200, 'OCUPADO', TRUE
WHERE @EST_ID IS NOT NULL AND NOT EXISTS (
    SELECT 1 FROM porcinos_galpon g WHERE g.establecimiento_id = @EST_ID AND g.nombre = 'Engorde A demo'
);

INSERT INTO porcinos_galpon (establecimiento_id, nombre, capacidad_cabezas, estado, activo)
SELECT @EST_ID, 'Engorde B demo', 200, 'DISPONIBLE', TRUE
WHERE @EST_ID IS NOT NULL AND NOT EXISTS (
    SELECT 1 FROM porcinos_galpon g WHERE g.establecimiento_id = @EST_ID AND g.nombre = 'Engorde B demo'
);

SET @GALPON_MAT_ID := (SELECT id FROM porcinos_galpon WHERE establecimiento_id = @EST_ID AND nombre = 'Maternidad demo' LIMIT 1);
SET @GALPON_ENG_A_ID := (SELECT id FROM porcinos_galpon WHERE establecimiento_id = @EST_ID AND nombre = 'Engorde A demo' LIMIT 1);
SET @GALPON_ENG_B_ID := (SELECT id FROM porcinos_galpon WHERE establecimiento_id = @EST_ID AND nombre = 'Engorde B demo' LIMIT 1);

-- Catálogos
INSERT INTO porcinos_raza (empresa_id, nombre, tipo, activo)
SELECT @EMPRESA_ID, 'Landrace demo', 'MADRE', TRUE
WHERE NOT EXISTS (SELECT 1 FROM porcinos_raza WHERE empresa_id = @EMPRESA_ID AND nombre = 'Landrace demo');

INSERT INTO porcinos_raza (empresa_id, nombre, tipo, activo)
SELECT @EMPRESA_ID, 'Duroc demo', 'PADRILLO', TRUE
WHERE NOT EXISTS (SELECT 1 FROM porcinos_raza WHERE empresa_id = @EMPRESA_ID AND nombre = 'Duroc demo');

INSERT INTO porcinos_tipo_servicio (empresa_id, nombre, activo)
SELECT @EMPRESA_ID, 'Monta natural demo', TRUE
WHERE NOT EXISTS (SELECT 1 FROM porcinos_tipo_servicio WHERE empresa_id = @EMPRESA_ID AND nombre = 'Monta natural demo');

INSERT INTO porcinos_tipo_servicio (empresa_id, nombre, activo)
SELECT @EMPRESA_ID, 'Inseminación artificial demo', TRUE
WHERE NOT EXISTS (SELECT 1 FROM porcinos_tipo_servicio WHERE empresa_id = @EMPRESA_ID AND nombre = 'Inseminación artificial demo');

INSERT INTO porcinos_causa_mortalidad (empresa_id, nombre, etapa, activo)
SELECT @EMPRESA_ID, 'Neumonía demo', 'ENGORDE', TRUE
WHERE NOT EXISTS (SELECT 1 FROM porcinos_causa_mortalidad WHERE empresa_id = @EMPRESA_ID AND nombre = 'Neumonía demo');

INSERT INTO porcinos_motivo_baja (empresa_id, nombre, activo)
SELECT @EMPRESA_ID, 'Descarte productivo demo', TRUE
WHERE NOT EXISTS (SELECT 1 FROM porcinos_motivo_baja WHERE empresa_id = @EMPRESA_ID AND nombre = 'Descarte productivo demo');

SET @RAZA_MADRE_ID := (SELECT id FROM porcinos_raza WHERE empresa_id = @EMPRESA_ID AND nombre = 'Landrace demo' LIMIT 1);
SET @RAZA_PADRILLO_ID := (SELECT id FROM porcinos_raza WHERE empresa_id = @EMPRESA_ID AND nombre = 'Duroc demo' LIMIT 1);
SET @TIPO_SERVICIO_ID := (SELECT id FROM porcinos_tipo_servicio WHERE empresa_id = @EMPRESA_ID AND nombre = 'Monta natural demo' LIMIT 1);
SET @CAUSA_MORT_ID := (SELECT id FROM porcinos_causa_mortalidad WHERE empresa_id = @EMPRESA_ID AND nombre = 'Neumonía demo' LIMIT 1);

-- Dieta
INSERT INTO porcinos_dieta (empresa_id, nombre, activo)
SELECT @EMPRESA_ID, 'Dieta engorde demo', TRUE
WHERE NOT EXISTS (SELECT 1 FROM porcinos_dieta WHERE empresa_id = @EMPRESA_ID AND nombre = 'Dieta engorde demo');

SET @DIETA_ID := (SELECT id FROM porcinos_dieta WHERE empresa_id = @EMPRESA_ID AND nombre = 'Dieta engorde demo' LIMIT 1);

INSERT INTO porcinos_dieta_fase (dieta_id, nombre_fase, dias_desde_ingreso, kg_cabeza_dia, insumo_id)
SELECT @DIETA_ID, 'Recría inicial', 0, 0.450, @INSUMO_ALIMENTO_ID
WHERE @DIETA_ID IS NOT NULL AND @INSUMO_ALIMENTO_ID IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM porcinos_dieta_fase f WHERE f.dieta_id = @DIETA_ID AND f.nombre_fase = 'Recría inicial'
  );

INSERT INTO porcinos_dieta_fase (dieta_id, nombre_fase, dias_desde_ingreso, kg_cabeza_dia, insumo_id)
SELECT @DIETA_ID, 'Engorde final', 45, 2.800, @INSUMO_ALIMENTO_ID
WHERE @DIETA_ID IS NOT NULL AND @INSUMO_ALIMENTO_ID IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM porcinos_dieta_fase f WHERE f.dieta_id = @DIETA_ID AND f.nombre_fase = 'Engorde final'
  );

-- Padrillos
INSERT INTO porcinos_padrillo (empresa_id, nombre, raza_id, activo)
SELECT @EMPRESA_ID, 'Padrillo Rocco demo', @RAZA_PADRILLO_ID, TRUE
WHERE NOT EXISTS (SELECT 1 FROM porcinos_padrillo WHERE empresa_id = @EMPRESA_ID AND nombre = 'Padrillo Rocco demo');

INSERT INTO porcinos_padrillo (empresa_id, nombre, raza_id, activo)
SELECT @EMPRESA_ID, 'Padrillo Thor demo', @RAZA_PADRILLO_ID, TRUE
WHERE NOT EXISTS (SELECT 1 FROM porcinos_padrillo WHERE empresa_id = @EMPRESA_ID AND nombre = 'Padrillo Thor demo');

SET @PADRILLO_ID := (SELECT id FROM porcinos_padrillo WHERE empresa_id = @EMPRESA_ID AND nombre = 'Padrillo Rocco demo' LIMIT 1);

-- Madres demo
INSERT INTO porcinos_madre (empresa_id, caravana, raza_id, galpon_id, estado, fecha_ingreso, activo)
SELECT @EMPRESA_ID, 'M-DEMO-101', @RAZA_MADRE_ID, @GALPON_MAT_ID, 'ADULTA', CURDATE() - INTERVAL 400 DAY, TRUE
WHERE NOT EXISTS (SELECT 1 FROM porcinos_madre WHERE empresa_id = @EMPRESA_ID AND caravana = 'M-DEMO-101');

INSERT INTO porcinos_madre (empresa_id, caravana, raza_id, galpon_id, estado, fecha_ingreso, activo)
SELECT @EMPRESA_ID, 'M-DEMO-102', @RAZA_MADRE_ID, @GALPON_MAT_ID, 'GESTACION', CURDATE() - INTERVAL 350 DAY, TRUE
WHERE NOT EXISTS (SELECT 1 FROM porcinos_madre WHERE empresa_id = @EMPRESA_ID AND caravana = 'M-DEMO-102');

INSERT INTO porcinos_madre (empresa_id, caravana, raza_id, galpon_id, estado, fecha_ingreso, activo)
SELECT @EMPRESA_ID, 'M-DEMO-103', @RAZA_MADRE_ID, @GALPON_MAT_ID, 'LACTANCIA', CURDATE() - INTERVAL 300 DAY, TRUE
WHERE NOT EXISTS (SELECT 1 FROM porcinos_madre WHERE empresa_id = @EMPRESA_ID AND caravana = 'M-DEMO-103');

INSERT INTO porcinos_madre (empresa_id, caravana, raza_id, galpon_id, estado, fecha_ingreso, activo)
SELECT @EMPRESA_ID, 'M-DEMO-104', @RAZA_MADRE_ID, @GALPON_MAT_ID, 'ADULTA', CURDATE() - INTERVAL 500 DAY, TRUE
WHERE NOT EXISTS (SELECT 1 FROM porcinos_madre WHERE empresa_id = @EMPRESA_ID AND caravana = 'M-DEMO-104');

SET @MADRE_101 := (SELECT id FROM porcinos_madre WHERE empresa_id = @EMPRESA_ID AND caravana = 'M-DEMO-101' LIMIT 1);
SET @MADRE_102 := (SELECT id FROM porcinos_madre WHERE empresa_id = @EMPRESA_ID AND caravana = 'M-DEMO-102' LIMIT 1);
SET @MADRE_103 := (SELECT id FROM porcinos_madre WHERE empresa_id = @EMPRESA_ID AND caravana = 'M-DEMO-103' LIMIT 1);
SET @MADRE_104 := (SELECT id FROM porcinos_madre WHERE empresa_id = @EMPRESA_ID AND caravana = 'M-DEMO-104' LIMIT 1);

-- M-102: gestación activa (servicio hace 90 días)
INSERT INTO porcinos_servicio (madre_id, padrillo_id, tipo_servicio_id, fecha, observaciones)
SELECT @MADRE_102, @PADRILLO_ID, @TIPO_SERVICIO_ID, CURDATE() - INTERVAL 90 DAY, 'Servicio demo M-102'
WHERE @MADRE_102 IS NOT NULL AND NOT EXISTS (
    SELECT 1 FROM porcinos_servicio s WHERE s.madre_id = @MADRE_102
);

SET @SERVICIO_102 := (SELECT id FROM porcinos_servicio WHERE madre_id = @MADRE_102 ORDER BY id DESC LIMIT 1);

INSERT INTO porcinos_gestacion (madre_id, servicio_id, fecha_inicio, fecha_probable_parto, estado, activo)
SELECT @MADRE_102, @SERVICIO_102, CURDATE() - INTERVAL 90 DAY, CURDATE() + INTERVAL 24 DAY, 'EN_CURSO', TRUE
WHERE @MADRE_102 IS NOT NULL AND NOT EXISTS (
    SELECT 1 FROM porcinos_gestacion g WHERE g.madre_id = @MADRE_102 AND g.estado = 'EN_CURSO'
);

-- M-103: lactancia (parto hace 18 días, pendiente destete)
INSERT INTO porcinos_servicio (madre_id, padrillo_id, tipo_servicio_id, fecha, observaciones)
SELECT @MADRE_103, @PADRILLO_ID, @TIPO_SERVICIO_ID, CURDATE() - INTERVAL 132 DAY, 'Servicio demo M-103'
WHERE @MADRE_103 IS NOT NULL AND NOT EXISTS (
    SELECT 1 FROM porcinos_servicio s WHERE s.madre_id = @MADRE_103
);

SET @SERVICIO_103 := (SELECT id FROM porcinos_servicio WHERE madre_id = @MADRE_103 ORDER BY id DESC LIMIT 1);

INSERT INTO porcinos_gestacion (madre_id, servicio_id, fecha_inicio, fecha_probable_parto, estado, activo)
SELECT @MADRE_103, @SERVICIO_103, CURDATE() - INTERVAL 132 DAY, CURDATE() - INTERVAL 18 DAY, 'FINALIZADA', FALSE
WHERE @MADRE_103 IS NOT NULL AND NOT EXISTS (
    SELECT 1 FROM porcinos_gestacion g WHERE g.madre_id = @MADRE_103
);

SET @GESTACION_103 := (SELECT id FROM porcinos_gestacion WHERE madre_id = @MADRE_103 LIMIT 1);

INSERT INTO porcinos_parto (gestacion_id, madre_id, fecha, nacidos_vivos, nacidos_muertos, momificados, observaciones)
SELECT @GESTACION_103, @MADRE_103, CURDATE() - INTERVAL 18 DAY, 12, 1, 0, 'Parto demo M-103'
WHERE @GESTACION_103 IS NOT NULL AND NOT EXISTS (
    SELECT 1 FROM porcinos_parto p WHERE p.gestacion_id = @GESTACION_103
);

SET @PARTO_103 := (SELECT id FROM porcinos_parto WHERE gestacion_id = @GESTACION_103 LIMIT 1);

-- M-104: ciclo completo con destete y lote
INSERT INTO porcinos_servicio (madre_id, padrillo_id, tipo_servicio_id, fecha, observaciones)
SELECT @MADRE_104, @PADRILLO_ID, @TIPO_SERVICIO_ID, CURDATE() - INTERVAL 200 DAY, 'Servicio demo M-104'
WHERE @MADRE_104 IS NOT NULL AND NOT EXISTS (
    SELECT 1 FROM porcinos_servicio s WHERE s.madre_id = @MADRE_104
);

SET @SERVICIO_104 := (SELECT id FROM porcinos_servicio WHERE madre_id = @MADRE_104 ORDER BY id DESC LIMIT 1);

INSERT INTO porcinos_gestacion (madre_id, servicio_id, fecha_inicio, fecha_probable_parto, estado, activo)
SELECT @MADRE_104, @SERVICIO_104, CURDATE() - INTERVAL 200 DAY, CURDATE() - INTERVAL 86 DAY, 'FINALIZADA', FALSE
WHERE @MADRE_104 IS NOT NULL AND NOT EXISTS (
    SELECT 1 FROM porcinos_gestacion g WHERE g.madre_id = @MADRE_104
);

SET @GESTACION_104 := (SELECT id FROM porcinos_gestacion WHERE madre_id = @MADRE_104 LIMIT 1);

INSERT INTO porcinos_parto (gestacion_id, madre_id, fecha, nacidos_vivos, nacidos_muertos, momificados, observaciones)
SELECT @GESTACION_104, @MADRE_104, CURDATE() - INTERVAL 86 DAY, 11, 0, 1, 'Parto demo M-104'
WHERE @GESTACION_104 IS NOT NULL AND NOT EXISTS (
    SELECT 1 FROM porcinos_parto p WHERE p.gestacion_id = @GESTACION_104
);

SET @PARTO_104 := (SELECT id FROM porcinos_parto WHERE gestacion_id = @GESTACION_104 LIMIT 1);

-- Lote desde destete M-104
INSERT INTO porcinos_lote (
    empresa_id, galpon_id, campana_id, nombre, origen, fecha_ingreso,
    cabezas_inicial, cabezas_actuales, peso_promedio_ingreso_kg, etapa, estado, observaciones
)
SELECT @EMPRESA_ID, @GALPON_ENG_A_ID, @CAMPANA_ID,
       'Destete-M-DEMO-104', 'DESTETE', CURDATE() - INTERVAL 65 DAY,
       10, 9, 6.50, 'ENGORDE', 'ACTIVO', 'Lote generado por demo — destete M-104'
WHERE @PARTO_104 IS NOT NULL AND @CAMPANA_ID IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM porcinos_lote l WHERE l.empresa_id = @EMPRESA_ID AND l.nombre = 'Destete-M-DEMO-104'
  );

SET @LOTE_DESTETE_ID := (
    SELECT id FROM porcinos_lote WHERE empresa_id = @EMPRESA_ID AND nombre = 'Destete-M-DEMO-104' LIMIT 1
);

INSERT INTO porcinos_destete (parto_id, fecha, cantidad_destetados, peso_promedio_kg, lote_id)
SELECT @PARTO_104, CURDATE() - INTERVAL 65 DAY, 10, 6.50, @LOTE_DESTETE_ID
WHERE @PARTO_104 IS NOT NULL AND @LOTE_DESTETE_ID IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM porcinos_destete d WHERE d.parto_id = @PARTO_104);

SET @DESTETE_104 := (SELECT id FROM porcinos_destete WHERE parto_id = @PARTO_104 LIMIT 1);

UPDATE porcinos_lote SET destete_id = @DESTETE_104
WHERE id = @LOTE_DESTETE_ID AND destete_id IS NULL AND @DESTETE_104 IS NOT NULL;

-- Lote engorde externo
INSERT INTO porcinos_lote (
    empresa_id, galpon_id, campana_id, nombre, origen, fecha_ingreso,
    cabezas_inicial, cabezas_actuales, peso_promedio_ingreso_kg, etapa, estado, observaciones
)
SELECT @EMPRESA_ID, @GALPON_ENG_B_ID, @CAMPANA_ID,
       'Engorde externo demo', 'EXTERNO', CURDATE() - INTERVAL 50 DAY,
       80, 78, 25.00, 'ENGORDE', 'ACTIVO', 'Ingreso externo demo'
WHERE @CAMPANA_ID IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM porcinos_lote l WHERE l.empresa_id = @EMPRESA_ID AND l.nombre = 'Engorde externo demo'
  );

SET @LOTE_EXTERNO_ID := (
    SELECT id FROM porcinos_lote WHERE empresa_id = @EMPRESA_ID AND nombre = 'Engorde externo demo' LIMIT 1
);

UPDATE porcinos_galpon SET estado = 'OCUPADO'
WHERE id = @GALPON_ENG_B_ID AND @LOTE_EXTERNO_ID IS NOT NULL;

-- Lote cerrado histórico
INSERT INTO porcinos_lote (
    empresa_id, galpon_id, campana_id, nombre, origen, fecha_ingreso, fecha_cierre,
    cabezas_inicial, cabezas_actuales, peso_promedio_ingreso_kg, etapa, estado, observaciones
)
SELECT @EMPRESA_ID, NULL, @CAMPANA_ID,
       'Lote cerrado demo', 'EXTERNO', CURDATE() - INTERVAL 120 DAY, CURDATE() - INTERVAL 10 DAY,
       60, 0, 22.00, 'ENGORDE', 'CERRADO', 'Faena completa demo'
WHERE @CAMPANA_ID IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM porcinos_lote l WHERE l.empresa_id = @EMPRESA_ID AND l.nombre = 'Lote cerrado demo'
  );

SET @LOTE_CERRADO_ID := (
    SELECT id FROM porcinos_lote WHERE empresa_id = @EMPRESA_ID AND nombre = 'Lote cerrado demo' LIMIT 1
);

-- Operaciones lote destete
INSERT INTO porcinos_pesada (lote_id, empresa_id, fecha, peso_promedio_kg, cabezas_muestreadas, observaciones)
SELECT @LOTE_DESTETE_ID, @EMPRESA_ID, CURDATE() - INTERVAL 30 DAY, 12.50, 9, 'Pesada demo día 35'
WHERE @LOTE_DESTETE_ID IS NOT NULL AND NOT EXISTS (
    SELECT 1 FROM porcinos_pesada p WHERE p.lote_id = @LOTE_DESTETE_ID AND p.fecha = CURDATE() - INTERVAL 30 DAY
);

INSERT INTO porcinos_pesada (lote_id, empresa_id, fecha, peso_promedio_kg, cabezas_muestreadas, observaciones)
SELECT @LOTE_DESTETE_ID, @EMPRESA_ID, CURDATE() - INTERVAL 7 DAY, 28.00, 9, 'Pesada demo reciente'
WHERE @LOTE_DESTETE_ID IS NOT NULL AND NOT EXISTS (
    SELECT 1 FROM porcinos_pesada p WHERE p.lote_id = @LOTE_DESTETE_ID AND p.fecha = CURDATE() - INTERVAL 7 DAY
);

INSERT INTO porcinos_consumo (lote_id, empresa_id, campana_id, insumo_id, fecha, cantidad_kg, observaciones)
SELECT @LOTE_DESTETE_ID, @EMPRESA_ID, @CAMPANA_ID, @INSUMO_ALIMENTO_ID, CURDATE() - INTERVAL 5 DAY, 180.000, 'Consumo demo'
WHERE @LOTE_DESTETE_ID IS NOT NULL AND @INSUMO_ALIMENTO_ID IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM porcinos_consumo c WHERE c.lote_id = @LOTE_DESTETE_ID AND c.fecha = CURDATE() - INTERVAL 5 DAY
  );

INSERT INTO porcinos_muerte (lote_id, empresa_id, fecha, cabezas, causa_mortalidad_id, observaciones)
SELECT @LOTE_DESTETE_ID, @EMPRESA_ID, CURDATE() - INTERVAL 40 DAY, 1, @CAUSA_MORT_ID, 'Muerte demo'
WHERE @LOTE_DESTETE_ID IS NOT NULL AND NOT EXISTS (
    SELECT 1 FROM porcinos_muerte m WHERE m.lote_id = @LOTE_DESTETE_ID
);

INSERT INTO porcinos_evento_sanitario (lote_id, empresa_id, fecha, tipo, descripcion, dias_retiro, observaciones)
SELECT @LOTE_DESTETE_ID, @EMPRESA_ID, CURDATE() - INTERVAL 20 DAY, 'VACUNA', 'Vacuna circovirus demo', 21, 'Sanidad demo'
WHERE @LOTE_DESTETE_ID IS NOT NULL AND NOT EXISTS (
    SELECT 1 FROM porcinos_evento_sanitario e WHERE e.lote_id = @LOTE_DESTETE_ID AND e.tipo = 'VACUNA'
);

INSERT INTO porcinos_venta (lote_id, empresa_id, campana_id, fecha, tipo, cabezas, peso_promedio_kg, precio_kg, total, comprador, observaciones)
SELECT @LOTE_CERRADO_ID, @EMPRESA_ID, @CAMPANA_ID, CURDATE() - INTERVAL 10 DAY, 'FAENA', 60, 105.00, 850.00, 5355000.00, 'Frigorífico demo', 'Faena total demo'
WHERE @LOTE_CERRADO_ID IS NOT NULL AND NOT EXISTS (
    SELECT 1 FROM porcinos_venta v WHERE v.lote_id = @LOTE_CERRADO_ID AND v.tipo = 'FAENA'
);

INSERT INTO porcinos_consumo (lote_id, empresa_id, campana_id, insumo_id, fecha, cantidad_kg, observaciones)
SELECT @LOTE_EXTERNO_ID, @EMPRESA_ID, @CAMPANA_ID, @INSUMO_ALIMENTO_ID, CURDATE() - INTERVAL 3 DAY, 420.000, 'Consumo lote externo demo'
WHERE @LOTE_EXTERNO_ID IS NOT NULL AND @INSUMO_ALIMENTO_ID IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM porcinos_consumo c WHERE c.lote_id = @LOTE_EXTERNO_ID AND c.fecha = CURDATE() - INTERVAL 3 DAY
  );

-- Resumen
SELECT 'Porcinos v2 demo cargado' AS resultado,
       @EMPRESA_ID AS empresa_id,
       (SELECT nombre FROM empresas WHERE id = @EMPRESA_ID) AS empresa,
       @CAMPANA_ID AS campana_activa_id,
       (SELECT COUNT(*) FROM porcinos_madre WHERE empresa_id = @EMPRESA_ID) AS madres,
       (SELECT COUNT(*) FROM porcinos_lote WHERE empresa_id = @EMPRESA_ID AND estado = 'ACTIVO') AS lotes_activos,
       (SELECT COUNT(*) FROM porcinos_padrillo WHERE empresa_id = @EMPRESA_ID) AS padrillos;
