-- Datos de prueba para admin@localhost (empresa 1, usuario 1).
-- Ubicación: departamento Banda y Capital, Santiago del Estero, zona de riego del Río Dulce.
-- Idempotente: no duplica filas si ya existen por nombre.

SET NAMES utf8mb4;

-- Columnas que la entidad de cultivos espera y el esquema viejo no tenía.
SET @esquema = DATABASE();

SET @sql = (SELECT IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@esquema AND TABLE_NAME='cultivo_cultivos' AND COLUMN_NAME='empresa_id'), 'SELECT 1', 'ALTER TABLE cultivo_cultivos ADD COLUMN empresa_id BIGINT NULL'));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql = (SELECT IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@esquema AND TABLE_NAME='cultivo_cultivos' AND COLUMN_NAME='usuario_id'), 'SELECT 1', 'ALTER TABLE cultivo_cultivos ADD COLUMN usuario_id BIGINT NULL'));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql = (SELECT IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@esquema AND TABLE_NAME='cultivo_cultivos' AND COLUMN_NAME='estado'), 'SELECT 1', 'ALTER TABLE cultivo_cultivos ADD COLUMN estado VARCHAR(20) NOT NULL DEFAULT ''ACTIVO'''));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql = (SELECT IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@esquema AND TABLE_NAME='cultivo_cultivos' AND COLUMN_NAME='ciclo_dias'), 'SELECT 1', 'ALTER TABLE cultivo_cultivos ADD COLUMN ciclo_dias INT NULL'));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql = (SELECT IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@esquema AND TABLE_NAME='cultivo_cultivos' AND COLUMN_NAME='rendimiento_esperado'), 'SELECT 1', 'ALTER TABLE cultivo_cultivos ADD COLUMN rendimiento_esperado DECIMAL(10,2) NULL'));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql = (SELECT IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@esquema AND TABLE_NAME='cultivo_cultivos' AND COLUMN_NAME='unidad_rendimiento'), 'SELECT 1', 'ALTER TABLE cultivo_cultivos ADD COLUMN unidad_rendimiento VARCHAR(50) NULL'));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql = (SELECT IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@esquema AND TABLE_NAME='cultivo_cultivos' AND COLUMN_NAME='precio_por_tonelada'), 'SELECT 1', 'ALTER TABLE cultivo_cultivos ADD COLUMN precio_por_tonelada DECIMAL(10,2) NULL'));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (SELECT IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@esquema AND TABLE_NAME='cultivo_maquinaria' AND COLUMN_NAME='user_id'), 'SELECT 1', 'ALTER TABLE cultivo_maquinaria ADD COLUMN user_id BIGINT NULL'));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql = (SELECT IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@esquema AND TABLE_NAME='cultivo_maquinaria' AND COLUMN_NAME='empresa_id'), 'SELECT 1', 'ALTER TABLE cultivo_maquinaria ADD COLUMN empresa_id BIGINT NULL'));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

INSERT INTO company_modules (company_id, module_id, enabled)
SELECT 1, m.id, 1
FROM modules m
WHERE m.code = 'AVICOLA_PONEDORAS'
  AND NOT EXISTS (
      SELECT 1 FROM company_modules cm WHERE cm.company_id = 1 AND cm.module_id = m.id
  );
UPDATE company_modules cm
INNER JOIN modules m ON m.id = cm.module_id
SET cm.enabled = 1
WHERE cm.company_id = 1 AND m.code = 'AVICOLA_PONEDORAS';

-- Cultivos de la zona: soja y maíz de verano, algodón bajo riego, trigo de invierno.
INSERT INTO cultivo_cultivos (nombre, tipo, variedad, descripcion, activo, estado, ciclo_dias, rendimiento_esperado, unidad_rendimiento, precio_por_tonelada, empresa_id, usuario_id)
SELECT 'Soja', 'GRANO', 'DM 67i20', 'Soja de primera bajo riego del Río Dulce, departamento Banda.', 1, 'ACTIVO', 130, 3.20, 'tn/ha', 320000, 1, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM cultivo_cultivos WHERE nombre = 'Soja' AND variedad = 'DM 67i20');

INSERT INTO cultivo_cultivos (nombre, tipo, variedad, descripcion, activo, estado, ciclo_dias, rendimiento_esperado, unidad_rendimiento, precio_por_tonelada, empresa_id, usuario_id)
SELECT 'Maíz', 'GRANO', 'DK 72-10', 'Maíz tardío en lote con riego por surco, La Banda.', 1, 'ACTIVO', 140, 8.50, 'tn/ha', 180000, 1, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM cultivo_cultivos WHERE nombre = 'Maíz' AND variedad = 'DK 72-10');

INSERT INTO cultivo_cultivos (nombre, tipo, variedad, descripcion, activo, estado, ciclo_dias, rendimiento_esperado, unidad_rendimiento, precio_por_tonelada, empresa_id, usuario_id)
SELECT 'Algodón', 'FIBRA', 'NuOpal', 'Algodón en la zona de riego de Santiago del Estero.', 1, 'ACTIVO', 160, 2.80, 'tn/ha', 450000, 1, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM cultivo_cultivos WHERE nombre = 'Algodón' AND variedad = 'NuOpal');

INSERT INTO cultivo_cultivos (nombre, tipo, variedad, descripcion, activo, estado, ciclo_dias, rendimiento_esperado, unidad_rendimiento, precio_por_tonelada, empresa_id, usuario_id)
SELECT 'Trigo', 'GRANO', 'ACA 602', 'Trigo de invierno cosechado en 2025, departamento Capital.', 1, 'ACTIVO', 140, 2.40, 'tn/ha', 210000, 1, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM cultivo_cultivos WHERE nombre = 'Trigo' AND variedad = 'ACA 602');

INSERT INTO cultivo_campos (nombre, descripcion, ubicacion, area_hectareas, tipo_suelo, estado, activo, poligono, coordenadas, user_id, empresa_id)
SELECT 'La Banda Norte',
       'Campo bajo riego del Río Dulce, al norte de La Banda.',
       'Ruta Provincial 5, La Banda, Santiago del Estero',
       180.00, 'Franco limoso', 'ACTIVO', 1,
       '-27.7315,-64.2465;-27.7315,-64.2395;-27.7365,-64.2395;-27.7365,-64.2465',
       '[{"lat":-27.7315,"lng":-64.2465},{"lat":-27.7315,"lng":-64.2395},{"lat":-27.7365,"lng":-64.2395},{"lat":-27.7365,"lng":-64.2465}]',
       1, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM cultivo_campos WHERE nombre = 'La Banda Norte' AND empresa_id = 1);

INSERT INTO cultivo_campos (nombre, descripcion, ubicacion, area_hectareas, tipo_suelo, estado, activo, poligono, coordenadas, user_id, empresa_id)
SELECT 'Los Cardones',
       'Loma al sur de la ciudad de Santiago del Estero.',
       'Ruta Nacional 9, sur de Capital, Santiago del Estero',
       95.00, 'Franco arenoso', 'ACTIVO', 1,
       '-27.8220,-64.2780;-27.8220,-64.2720;-27.8280,-64.2720;-27.8280,-64.2780',
       '[{"lat":-27.8220,"lng":-64.2780},{"lat":-27.8220,"lng":-64.2720},{"lat":-27.8280,"lng":-64.2720},{"lat":-27.8280,"lng":-64.2780}]',
       1, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM cultivo_campos WHERE nombre = 'Los Cardones' AND empresa_id = 1);

INSERT INTO cultivo_campos (nombre, descripcion, ubicacion, area_hectareas, tipo_suelo, estado, activo, poligono, coordenadas, user_id, empresa_id)
SELECT 'Forres',
       'Cuadro de algodón sobre el canal matriz, departamento Robles.',
       'Forres, departamento Robles, Santiago del Estero',
       240.00, 'Franco limoso', 'ACTIVO', 1,
       '-27.8840,-63.9960;-27.8840,-63.9880;-27.8900,-63.9880;-27.8900,-63.9960',
       '[{"lat":-27.8840,"lng":-63.9960},{"lat":-27.8840,"lng":-63.9880},{"lat":-27.8900,"lng":-63.9880},{"lat":-27.8900,"lng":-63.9960}]',
       1, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM cultivo_campos WHERE nombre = 'Forres' AND empresa_id = 1);

SET @campo_banda = (SELECT id FROM cultivo_campos WHERE nombre = 'La Banda Norte' AND empresa_id = 1 LIMIT 1);
SET @campo_cardones = (SELECT id FROM cultivo_campos WHERE nombre = 'Los Cardones' AND empresa_id = 1 LIMIT 1);
SET @campo_forres = (SELECT id FROM cultivo_campos WHERE nombre = 'Forres' AND empresa_id = 1 LIMIT 1);
SET @cultivo_soja = (SELECT id FROM cultivo_cultivos WHERE nombre = 'Soja' AND variedad = 'DM 67i20' LIMIT 1);
SET @cultivo_maiz = (SELECT id FROM cultivo_cultivos WHERE nombre = 'Maíz' AND variedad = 'DK 72-10' LIMIT 1);
SET @cultivo_algodon = (SELECT id FROM cultivo_cultivos WHERE nombre = 'Algodón' AND variedad = 'NuOpal' LIMIT 1);
SET @cultivo_trigo = (SELECT id FROM cultivo_cultivos WHERE nombre = 'Trigo' AND variedad = 'ACA 602' LIMIT 1);

INSERT INTO cultivo_lotes (nombre, descripcion, campo_id, area_hectareas, estado, tipo_suelo, cultivo_actual, fecha_siembra, cultivo_id, user_id, empresa_id, activo, tipo_uso, liberado_para_siembra, version)
SELECT 'Lote 4 Soja', 'Soja de primera, riego por surco.', @campo_banda, 62.00, 'EN_CRECIMIENTO', 'Franco limoso', 'Soja', '2025-12-12', @cultivo_soja, 1, 1, 1, 'CULTIVO', 0, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM cultivo_lotes WHERE nombre = 'Lote 4 Soja' AND campo_id = @campo_banda);

INSERT INTO cultivo_lotes (nombre, descripcion, campo_id, area_hectareas, estado, tipo_suelo, cultivo_actual, fecha_siembra, cultivo_id, user_id, empresa_id, activo, tipo_uso, liberado_para_siembra, version)
SELECT 'Lote 2 Maíz', 'Maíz tardío en el bajo de La Banda.', @campo_banda, 48.00, 'SEMBRADO', 'Franco limoso', 'Maíz', '2026-01-08', @cultivo_maiz, 1, 1, 1, 'CULTIVO', 0, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM cultivo_lotes WHERE nombre = 'Lote 2 Maíz' AND campo_id = @campo_banda);

INSERT INTO cultivo_lotes (nombre, descripcion, campo_id, area_hectareas, estado, tipo_suelo, cultivo_actual, fecha_siembra, fecha_cosecha_real, rendimiento_real, cultivo_id, user_id, empresa_id, activo, tipo_uso, liberado_para_siembra, version)
SELECT 'Lote 7 Trigo', 'Trigo cosechado en noviembre 2025.', @campo_cardones, 40.00, 'COSECHADO', 'Franco arenoso', 'Trigo', '2025-06-02', '2025-11-18', 96.00, @cultivo_trigo, 1, 1, 1, 'CULTIVO', 1, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM cultivo_lotes WHERE nombre = 'Lote 7 Trigo' AND campo_id = @campo_cardones);

INSERT INTO cultivo_lotes (nombre, descripcion, campo_id, area_hectareas, estado, tipo_suelo, cultivo_actual, fecha_siembra, cultivo_id, user_id, empresa_id, activo, tipo_uso, liberado_para_siembra, version)
SELECT 'Cuadro Algodón', 'Algodón en cuadro del canal matriz.', @campo_forres, 80.00, 'EN_CRECIMIENTO', 'Franco limoso', 'Algodón', '2025-11-20', @cultivo_algodon, 1, 1, 1, 'CULTIVO', 0, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM cultivo_lotes WHERE nombre = 'Cuadro Algodón' AND campo_id = @campo_forres);

SET @lote_soja = (SELECT id FROM cultivo_lotes WHERE nombre = 'Lote 4 Soja' AND campo_id = @campo_banda LIMIT 1);
SET @lote_maiz = (SELECT id FROM cultivo_lotes WHERE nombre = 'Lote 2 Maíz' AND campo_id = @campo_banda LIMIT 1);
SET @lote_trigo = (SELECT id FROM cultivo_lotes WHERE nombre = 'Lote 7 Trigo' AND campo_id = @campo_cardones LIMIT 1);
SET @lote_algodon = (SELECT id FROM cultivo_lotes WHERE nombre = 'Cuadro Algodón' AND campo_id = @campo_forres LIMIT 1);

INSERT INTO cultivo_labores (lote_id, cultivo_id, tipo_labor, descripcion, fecha_inicio, fecha_fin, estado, costo_total, observaciones, usuario_id, responsable, activo, version)
SELECT @lote_soja, @cultivo_soja, 'SIEMBRA', 'Siembra de soja DM 67i20 a 0,52 m.', '2025-12-12', '2025-12-14', 'COMPLETADA', 1850000, 'Contratista de La Banda.', 1, 'Juan Pérez', 1, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM cultivo_labores WHERE lote_id = @lote_soja AND tipo_labor = 'SIEMBRA');

INSERT INTO cultivo_labores (lote_id, cultivo_id, tipo_labor, descripcion, fecha_inicio, estado, costo_total, observaciones, usuario_id, responsable, activo, version)
SELECT @lote_maiz, @cultivo_maiz, 'CONTROL_MALEZAS', 'Pulverización de preemergencia en maíz.', '2026-01-10', 'EN_PROGRESO', 640000, 'Agua del canal, boquillas antideriva.', 1, 'Mario Gómez', 1, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM cultivo_labores WHERE lote_id = @lote_maiz AND tipo_labor = 'CONTROL_MALEZAS');

INSERT INTO cultivo_labores (lote_id, cultivo_id, tipo_labor, descripcion, fecha_inicio, fecha_fin, estado, costo_total, observaciones, usuario_id, responsable, activo, version)
SELECT @lote_algodon, @cultivo_algodon, 'RIEGO', 'Primer riego por surco desde el canal matriz.', '2025-12-02', '2025-12-05', 'COMPLETADA', 420000, 'Turno de riego de Forres.', 1, 'Canalero', 1, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM cultivo_labores WHERE lote_id = @lote_algodon AND tipo_labor = 'RIEGO');

INSERT INTO cultivo_historial_cosechas (lote_id, cultivo_id, fecha_siembra, fecha_cosecha, superficie_hectareas, cantidad_cosechada, unidad_cosecha, rendimiento_real, rendimiento_esperado, observaciones, usuario_id)
SELECT @lote_trigo, @cultivo_trigo, '2025-06-02', '2025-11-18', 40.00, 96.00, 'tn', 2.40, 2.40, 'Trigo ACA 602 entregado en acopio de La Banda.', 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM cultivo_historial_cosechas WHERE lote_id = @lote_trigo AND fecha_cosecha = '2025-11-18');

CREATE TABLE IF NOT EXISTS cultivo_ingresos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    concepto VARCHAR(200) NOT NULL,
    descripcion TEXT NULL,
    tipo_ingreso VARCHAR(40) NOT NULL,
    monto DECIMAL(15,2) NOT NULL,
    cantidad DECIMAL(10,2) NULL,
    unidad_medida VARCHAR(50) NULL,
    fecha DATE NOT NULL,
    cliente_comprador VARCHAR(200) NULL,
    observaciones TEXT NULL,
    estado VARCHAR(20) NOT NULL,
    lote_id BIGINT NULL,
    user_id BIGINT NOT NULL,
    fecha_creacion TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    activo TINYINT(1) NOT NULL DEFAULT 1,
    campana_id BIGINT NULL
);

CREATE TABLE IF NOT EXISTS cultivo_egresos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    concepto VARCHAR(200) NULL,
    descripcion TEXT NULL,
    tipo VARCHAR(40) NOT NULL,
    referencia_id BIGINT NULL,
    cantidad DECIMAL(10,2) NULL,
    unidad_medida VARCHAR(50) NULL,
    costo_unitario DECIMAL(10,2) NULL,
    costo_total DECIMAL(10,2) NOT NULL,
    monto DECIMAL(15,2) NOT NULL,
    fecha DATE NOT NULL,
    proveedor VARCHAR(200) NULL,
    observaciones TEXT NULL,
    estado VARCHAR(20) NOT NULL,
    lote_id BIGINT NULL,
    user_id BIGINT NOT NULL,
    fecha_creacion TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    activo TINYINT(1) NOT NULL DEFAULT 1,
    campana_id BIGINT NULL
);

INSERT INTO cultivo_ingresos (concepto, descripcion, tipo_ingreso, monto, cantidad, unidad_medida, fecha, cliente_comprador, estado, lote_id, user_id, activo, campana_id)
SELECT 'Venta de trigo Los Cardones', '96 tn de trigo campaña 2025 entregadas en acopio La Banda.', 'VENTA_CULTIVO', 20160000, 96.00, 'tn', '2025-11-22', 'Acopio La Banda', 'PAGADO', @lote_trigo, 1, 1, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM cultivo_ingresos WHERE concepto = 'Venta de trigo Los Cardones');

INSERT INTO cultivo_egresos (concepto, descripcion, tipo, cantidad, unidad_medida, costo_unitario, costo_total, monto, fecha, proveedor, estado, lote_id, user_id, activo, campana_id)
SELECT 'Semilla de soja DM 67i20', 'Semilla fiscal comprada en La Banda.', 'INSUMO', 3100, 'kg', 850, 2635000, 2635000, '2025-12-01', 'Agroinsumos La Banda', 'PAGADO', @lote_soja, 1, 1, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM cultivo_egresos WHERE concepto = 'Semilla de soja DM 67i20');

INSERT INTO cultivo_maquinaria (nombre, tipo, marca, modelo, anio, estado, horas_uso, costo_por_hora, descripcion, usuario_id, user_id, empresa_id)
SELECT 'Tractor La Banda', 'TRACTOR', 'John Deere', '6130M', 2019, 'OPERATIVA', 2450, 18000, 'Tractor del campo La Banda Norte.', 1, 1, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM cultivo_maquinaria WHERE nombre = 'Tractor La Banda');

INSERT INTO cultivo_maquinaria (nombre, tipo, marca, modelo, anio, estado, horas_uso, costo_por_hora, descripcion, usuario_id, user_id, empresa_id)
SELECT 'Pulverizadora Río Dulce', 'PULVERIZADORA', 'Pla', 'MAP II 2850', 2021, 'OPERATIVA', 980, 22000, 'Pulverizadora usada en La Banda y Forres.', 1, 1, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM cultivo_maquinaria WHERE nombre = 'Pulverizadora Río Dulce');

-- Porcinos: granja en El Salado, al este de La Banda.
INSERT INTO porcinos_establecimiento (empresa_id, nombre, ubicacion, coordenadas, dias_gestacion, dias_lactancia, dias_entre_celos, faena_habilitada, capacidad_cabezas, activo)
SELECT 1, 'Granja El Salado', 'El Salado, La Banda, Santiago del Estero', '[{"lat":-27.7550,"lng":-64.2210},{"lat":-27.7550,"lng":-64.2160},{"lat":-27.7585,"lng":-64.2160},{"lat":-27.7585,"lng":-64.2210}]', 114, 21, 21, 1, 400, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM porcinos_establecimiento WHERE nombre = 'Granja El Salado' AND empresa_id = 1);
SET @granja = (SELECT id FROM porcinos_establecimiento WHERE nombre = 'Granja El Salado' AND empresa_id = 1 LIMIT 1);

INSERT INTO porcinos_galpon (establecimiento_id, nombre, estado, capacidad_cabezas, activo)
SELECT @granja, 'Maternidad', 'DISPONIBLE', 40, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM porcinos_galpon WHERE nombre = 'Maternidad' AND establecimiento_id = @granja);
INSERT INTO porcinos_galpon (establecimiento_id, nombre, estado, capacidad_cabezas, activo)
SELECT @granja, 'Recría 1', 'DISPONIBLE', 200, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM porcinos_galpon WHERE nombre = 'Recría 1' AND establecimiento_id = @granja);
SET @galpon_mat = (SELECT id FROM porcinos_galpon WHERE nombre = 'Maternidad' AND establecimiento_id = @granja LIMIT 1);
SET @galpon_rec = (SELECT id FROM porcinos_galpon WHERE nombre = 'Recría 1' AND establecimiento_id = @granja LIMIT 1);

INSERT INTO porcinos_raza (empresa_id, nombre, activo)
SELECT 1, 'Landrace', 1 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM porcinos_raza WHERE nombre = 'Landrace' AND empresa_id = 1);
INSERT INTO porcinos_raza (empresa_id, nombre, activo)
SELECT 1, 'Yorkshire', 1 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM porcinos_raza WHERE nombre = 'Yorkshire' AND empresa_id = 1);
SET @raza_landrace = (SELECT id FROM porcinos_raza WHERE nombre = 'Landrace' AND empresa_id = 1 LIMIT 1);

INSERT INTO porcinos_madre (empresa_id, caravana, raza_id, galpon_id, estado, fecha_ingreso, fecha_nacimiento, activo)
SELECT 1, 'SDE-001', @raza_landrace, @galpon_mat, 'ADULTA', '2024-03-15', '2023-08-10', 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM porcinos_madre WHERE caravana = 'SDE-001' AND empresa_id = 1);
INSERT INTO porcinos_madre (empresa_id, caravana, raza_id, galpon_id, estado, fecha_ingreso, fecha_nacimiento, activo)
SELECT 1, 'SDE-002', @raza_landrace, @galpon_mat, 'GESTACION', '2024-06-01', '2023-11-02', 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM porcinos_madre WHERE caravana = 'SDE-002' AND empresa_id = 1);
INSERT INTO porcinos_padrillo (empresa_id, nombre, activo)
SELECT 1, 'Duque', 1 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM porcinos_padrillo WHERE nombre = 'Duque' AND empresa_id = 1);

INSERT INTO porcinos_lote (empresa_id, galpon_id, campana_id, nombre, origen, fecha_ingreso, cabezas_inicial, cabezas_actuales, peso_promedio_ingreso_kg, etapa, estado, observaciones)
SELECT 1, @galpon_rec, 1, 'Recría El Salado', 'EXTERNO', '2026-08-01', 45, 44, 28.50, 'RECRIA', 'ACTIVO', 'Capones de recría, origen La Banda.'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM porcinos_lote WHERE nombre = 'Recría El Salado' AND empresa_id = 1);

-- Avícola crianza: parrilleros en Clodomira.
INSERT INTO avicola_establecimiento (empresa_id, nombre, ubicacion, coordenadas, activo, modulo_origen)
SELECT 1, 'Granja Clodomira', 'Clodomira, departamento Banda, Santiago del Estero', '[{"lat":-27.5830,"lng":-64.1280},{"lat":-27.5830,"lng":-64.1230},{"lat":-27.5865,"lng":-64.1230},{"lat":-27.5865,"lng":-64.1280}]', 1, 'AVICOLA_CRIANZA'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM avicola_establecimiento WHERE nombre = 'Granja Clodomira' AND empresa_id = 1);
SET @granja_pollo = (SELECT id FROM avicola_establecimiento WHERE nombre = 'Granja Clodomira' AND empresa_id = 1 LIMIT 1);

INSERT INTO avicola_raza (empresa_id, nombre, activo, modulo_origen)
SELECT 1, 'Cobb 500', 1, 'AVICOLA_CRIANZA'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM avicola_raza WHERE nombre = 'Cobb 500' AND empresa_id = 1);
SET @raza_cobb = (SELECT id FROM avicola_raza WHERE nombre = 'Cobb 500' AND empresa_id = 1 LIMIT 1);

INSERT INTO avicola_lote (empresa_id, establecimiento_id, raza_id, nombre, especie, origen, fecha_ingreso, cantidad_inicial, cantidad_animales, estado, modulo_origen)
SELECT 1, @granja_pollo, @raza_cobb, 'Parrilleros Sep 2026', 'POLLO_PARRILLERO', 'EXTERNO', '2026-09-15', 8000, 7920, 'ACTIVO', 'AVICOLA_CRIANZA'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM avicola_lote WHERE nombre = 'Parrilleros Sep 2026' AND empresa_id = 1);

-- Avícola huevos: postura en Los Quiroga.
INSERT INTO avicola_huevo_establecimiento (empresa_id, nombre, ubicacion, coordenadas, activo)
SELECT 1, 'Postura Los Quiroga', 'Los Quiroga, departamento Banda, Santiago del Estero', '[{"lat":-27.6510,"lng":-64.3680},{"lat":-27.6510,"lng":-64.3630},{"lat":-27.6545,"lng":-64.3630},{"lat":-27.6545,"lng":-64.3680}]', 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM avicola_huevo_establecimiento WHERE nombre = 'Postura Los Quiroga' AND empresa_id = 1);
SET @postura = (SELECT id FROM avicola_huevo_establecimiento WHERE nombre = 'Postura Los Quiroga' AND empresa_id = 1 LIMIT 1);

INSERT INTO avicola_huevo_raza (empresa_id, nombre, activo)
SELECT 1, 'Lohmann Brown', 1 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM avicola_huevo_raza WHERE nombre = 'Lohmann Brown' AND empresa_id = 1);
SET @raza_lohmann = (SELECT id FROM avicola_huevo_raza WHERE nombre = 'Lohmann Brown' AND empresa_id = 1 LIMIT 1);

INSERT INTO avicola_huevo_lote (empresa_id, establecimiento_id, raza_id, nombre, fecha_inicio, cantidad_aves_inicial, cantidad_aves_actual, estado)
SELECT 1, @postura, @raza_lohmann, 'Postura 1', '2026-04-01', 6000, 5840, 'ACTIVO'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM avicola_huevo_lote WHERE nombre = 'Postura 1' AND empresa_id = 1);

-- Ponedoras: galpón en La Banda, ligado a la granja de Clodomira.
INSERT INTO avicola_ponedoras_galpon (empresa_id, establecimiento_id, nombre, raza, fecha_ingreso, cantidad_inicial, cantidad_aves, estado, campana_id, observaciones)
SELECT 1, @granja_pollo, 'Galpón Hy-Line La Banda', 'Hy-Line Brown', '2026-03-01', 4500, 4410, 'ACTIVO', 1, 'Ponedoras en galpón de La Banda, Santiago del Estero.'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM avicola_ponedoras_galpon WHERE nombre = 'Galpón Hy-Line La Banda' AND empresa_id = 1);

-- Lechería: tambo en la colonia de riego.
INSERT INTO lecheria_establecimiento (empresa_id, nombre, ubicacion, coordenadas, activo)
SELECT 1, 'Tambo Río Dulce', 'Colonia El Simbolar, La Banda, Santiago del Estero', '[{"lat":-27.7020,"lng":-64.2680},{"lat":-27.7020,"lng":-64.2610},{"lat":-27.7070,"lng":-64.2610},{"lat":-27.7070,"lng":-64.2680}]', 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM lecheria_establecimiento WHERE nombre = 'Tambo Río Dulce' AND empresa_id = 1);
SET @tambo = (SELECT id FROM lecheria_establecimiento WHERE nombre = 'Tambo Río Dulce' AND empresa_id = 1 LIMIT 1);

INSERT INTO lecheria_raza (empresa_id, especie, nombre, activo)
SELECT 1, 'BOVINO', 'Holando Argentino', 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM lecheria_raza WHERE nombre = 'Holando Argentino' AND empresa_id = 1);
SET @raza_holando = (SELECT id FROM lecheria_raza WHERE nombre = 'Holando Argentino' AND empresa_id = 1 LIMIT 1);

INSERT INTO lecheria_rodeo (establecimiento_id, empresa_id, nombre, especie, activo)
SELECT @tambo, 1, 'Rodeo de ordeño', 'BOVINO', 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM lecheria_rodeo WHERE nombre = 'Rodeo de ordeño' AND empresa_id = 1);
SET @rodeo = (SELECT id FROM lecheria_rodeo WHERE nombre = 'Rodeo de ordeño' AND empresa_id = 1 LIMIT 1);

INSERT INTO lecheria_animal (empresa_id, identificacion, especie, sexo, estado, raza_id, rodeo_id, campana_id, fecha_nacimiento, fecha_ingreso, activo, observaciones)
SELECT 1, 'RP 1204', 'BOVINO', 'HEMBRA', 'LACTANDO', @raza_holando, @rodeo, 1, '2021-04-12', '2022-08-01', 1, 'Vaca en ordeño, tambo Río Dulce.'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM lecheria_animal WHERE identificacion = 'RP 1204' AND empresa_id = 1);
INSERT INTO lecheria_animal (empresa_id, identificacion, especie, sexo, estado, raza_id, rodeo_id, campana_id, fecha_nacimiento, fecha_ingreso, activo, observaciones)
SELECT 1, 'RP 1188', 'BOVINO', 'HEMBRA', 'LACTANDO', @raza_holando, @rodeo, 1, '2020-09-03', '2022-08-01', 1, 'Vaca en ordeño.'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM lecheria_animal WHERE identificacion = 'RP 1188' AND empresa_id = 1);
INSERT INTO lecheria_animal (empresa_id, identificacion, especie, sexo, estado, raza_id, rodeo_id, campana_id, fecha_nacimiento, fecha_ingreso, activo, observaciones)
SELECT 1, 'RP 1310', 'BOVINO', 'HEMBRA', 'VAQUILLONA', @raza_holando, @rodeo, 1, '2024-07-20', '2024-08-15', 1, 'Vaquillona de reposición.'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM lecheria_animal WHERE identificacion = 'RP 1310' AND empresa_id = 1);
INSERT INTO lecheria_animal (empresa_id, identificacion, especie, sexo, estado, raza_id, rodeo_id, campana_id, fecha_nacimiento, fecha_ingreso, activo, observaciones)
SELECT 1, 'RP 1042', 'BOVINO', 'HEMBRA', 'SECA', @raza_holando, @rodeo, 1, '2019-02-11', '2021-05-01', 1, 'Vaca seca preñada.'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM lecheria_animal WHERE identificacion = 'RP 1042' AND empresa_id = 1);

-- Feedlot ya cargado: se ubica en La Banda y se suma un lote de novillos.
UPDATE feedlot_establecimiento
SET ubicacion = 'Ruta Nacional 34 km 12, La Banda, Santiago del Estero',
    coordenadas = '[{"lat":-27.7480,"lng":-64.2580},{"lat":-27.7480,"lng":-64.2520},{"lat":-27.7530,"lng":-64.2520},{"lat":-27.7530,"lng":-64.2580}]',
    nombre = 'Feedlot La Banda'
WHERE id = 1 AND empresa_id = 1;

INSERT INTO feedlot_lote (empresa_id, corral_id, campana_id, nombre, tipo_tenencia, fecha_ingreso, cabezas_inicial, cabezas_actuales, peso_promedio_ingreso_kg, estado)
SELECT 1, 2, 1, 'Novillos Angus La Banda', 'PROPIO', '2026-07-20', 42, 41, 318.50, 'ACTIVO'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM feedlot_lote WHERE nombre = 'Novillos Angus La Banda' AND empresa_id = 1);
