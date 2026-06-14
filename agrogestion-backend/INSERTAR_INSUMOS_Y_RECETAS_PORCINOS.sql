-- ============================================================================
-- SCRIPT PARA INSERTAR INSUMOS Y RECETAS DE ALIMENTACIÓN PARA PORCINOS
-- ============================================================================
-- Este script inserta:
-- 1. Los insumos básicos necesarios (maíz, soja, núcleos, etc.)
-- 2. Las recetas (insumos_compuestos) para cada etapa
-- 3. Los componentes de cada receta (componentes_insumo_compuesto)
-- 4. Las asociaciones de recetas a etapas (porcinos_recetas_alimentacion_etapa)
--
-- IMPORTANTE: Antes de ejecutar, reemplazar @USUARIO_ID y @EMPRESA_ID con valores reales
-- ============================================================================

SET @USUARIO_ID = (SELECT id FROM usuarios WHERE email = 'admin.empresa@agrocloud.com' LIMIT 1);
SET @EMPRESA_ID = (SELECT id FROM empresas WHERE nombre LIKE '%AgroCloud%' OR nombre LIKE '%Test%' LIMIT 1);

-- Si no se encuentran valores por defecto, usar ID 1
SET @USUARIO_ID = IFNULL(@USUARIO_ID, 1);
SET @EMPRESA_ID = IFNULL(@EMPRESA_ID, 1);

-- ============================================================================
-- PARTE 1: INSERTAR INSUMOS BÁSICOS
-- ============================================================================

INSERT IGNORE INTO cultivo_insumos (
    nombre, descripcion, tipo, unidad_medida, precio_unitario, 
    stock_minimo, stock_actual, activo, user_id, empresa_id, 
    fecha_creacion, fecha_actualizacion
) VALUES
-- Ingredientes principales
('Maíz', 'Maíz molido para alimentación porcina', 'OTROS', 'kg', 300.00, 100.00, 500.00, TRUE, @USUARIO_ID, @EMPRESA_ID, NOW(), NOW()),
('Harina de Soja 44%', 'Harina de soja con 44% de proteína bruta', 'OTROS', 'kg', 450.00, 100.00, 400.00, TRUE, @USUARIO_ID, @EMPRESA_ID, NOW(), NOW()),
('Salvado de Trigo', 'Salvado de trigo para fibra', 'OTROS', 'kg', 200.00, 50.00, 200.00, TRUE, @USUARIO_ID, @EMPRESA_ID, NOW(), NOW()),
('Aceite/Grasa Animal', 'Aceite o grasa animal para energía', 'OTROS', 'kg', 800.00, 50.00, 150.00, TRUE, @USUARIO_ID, @EMPRESA_ID, NOW(), NOW()),
-- Núcleos vitamínicos por etapa
('Núcleo Gestación', 'Núcleo vitamínico-mineral para gestación', 'OTROS', 'kg', 1200.00, 25.00, 100.00, TRUE, @USUARIO_ID, @EMPRESA_ID, NOW(), NOW()),
('Núcleo Lactancia', 'Núcleo vitamínico-mineral para lactancia', 'OTROS', 'kg', 1300.00, 25.00, 100.00, TRUE, @USUARIO_ID, @EMPRESA_ID, NOW(), NOW()),
('Núcleo Destete', 'Núcleo vitamínico-mineral para destete (F1)', 'OTROS', 'kg', 1400.00, 25.00, 100.00, TRUE, @USUARIO_ID, @EMPRESA_ID, NOW(), NOW()),
('Núcleo Recría Inicio', 'Núcleo vitamínico-mineral para inicio recría (F2)', 'OTROS', 'kg', 1350.00, 25.00, 100.00, TRUE, @USUARIO_ID, @EMPRESA_ID, NOW(), NOW()),
('Núcleo Recría Crecimiento', 'Núcleo vitamínico-mineral para crecimiento recría (F3)', 'OTROS', 'kg', 1250.00, 25.00, 100.00, TRUE, @USUARIO_ID, @EMPRESA_ID, NOW(), NOW()),
('Núcleo Desarrollo', 'Núcleo vitamínico-mineral para desarrollo', 'OTROS', 'kg', 1150.00, 25.00, 100.00, TRUE, @USUARIO_ID, @EMPRESA_ID, NOW(), NOW()),
('Núcleo Terminación', 'Núcleo vitamínico-mineral para terminación', 'OTROS', 'kg', 1100.00, 25.00, 100.00, TRUE, @USUARIO_ID, @EMPRESA_ID, NOW(), NOW()),
('Sal Común', 'Sal común para balance mineral', 'OTROS', 'kg', 150.00, 25.00, 100.00, TRUE, @USUARIO_ID, @EMPRESA_ID, NOW(), NOW()),
('Carbonato de Calcio', 'Carbonato de calcio para calcio', 'OTROS', 'kg', 250.00, 25.00, 100.00, TRUE, @USUARIO_ID, @EMPRESA_ID, NOW(), NOW()),
('Fosfato Dicálcico', 'Fosfato dicálcico para fósforo', 'OTROS', 'kg', 450.00, 25.00, 100.00, TRUE, @USUARIO_ID, @EMPRESA_ID, NOW(), NOW());

-- ============================================================================
-- PARTE 2: INSERTAR RECETAS (INSUMOS COMPUESTOS)
-- ============================================================================

-- Gestación
INSERT IGNORE INTO insumos_compuestos (
    nombre, descripcion, tipo, unidad_medida, rendimiento,
    empresa_id, usuario_id, activo, fecha_creacion, fecha_actualizacion
) VALUES (
    'Ración Gestación',
    'Ración completa para madres gestantes. Consumo: 2-2.5 kg/día. Lisina: 0.70-0.80%. EM: 3.100 kcal/kg',
    'RACION', 'kg', 1.0000,
    @EMPRESA_ID, @USUARIO_ID, TRUE, NOW(), NOW()
);

-- Lactancia
INSERT IGNORE INTO insumos_compuestos (
    nombre, descripcion, tipo, unidad_medida, rendimiento,
    empresa_id, usuario_id, activo, fecha_creacion, fecha_actualizacion
) VALUES (
    'Ración Lactancia',
    'Ración completa para madres lactantes. Consumo: 6+ kg/día. Lisina: 1.0%. EM: 3.325 kcal/kg',
    'RACION', 'kg', 1.0000,
    @EMPRESA_ID, @USUARIO_ID, TRUE, NOW(), NOW()
);

-- Destete (F1)
INSERT IGNORE INTO insumos_compuestos (
    nombre, descripcion, tipo, unidad_medida, rendimiento,
    empresa_id, usuario_id, activo, fecha_creacion, fecha_actualizacion
) VALUES (
    'Ración Destete F1',
    'Ración para lechones destetados (8-12 kg). Consumo: 0.57 kg/día. PB: 20-24%. EM: 3300-3400 kcal/kg. Lisina: 1.2-1.4%',
    'RACION', 'kg', 1.0000,
    @EMPRESA_ID, @USUARIO_ID, TRUE, NOW(), NOW()
);

-- Inicio Recría (F2)
INSERT IGNORE INTO insumos_compuestos (
    nombre, descripcion, tipo, unidad_medida, rendimiento,
    empresa_id, usuario_id, activo, fecha_creacion, fecha_actualizacion
) VALUES (
    'Ración Inicio Recría F2',
    'Ración para inicio de recría (12-28 kg). Consumo: 1.05 kg/día. PB: 16-18%. EM: 3200-3300 kcal/kg. Lisina: 0.9-1.1%',
    'RACION', 'kg', 1.0000,
    @EMPRESA_ID, @USUARIO_ID, TRUE, NOW(), NOW()
);

-- Crecimiento Recría (F3)
INSERT IGNORE INTO insumos_compuestos (
    nombre, descripcion, tipo, unidad_medida, rendimiento,
    empresa_id, usuario_id, activo, fecha_creacion, fecha_actualizacion
) VALUES (
    'Ración Crecimiento Recría F3',
    'Ración para crecimiento en recría (28-54 kg). Consumo: 2.07 kg/día. PB: 14-16%. EM: 3100-3200 kcal/kg. Lisina: 0.8-1.0%',
    'RACION', 'kg', 1.0000,
    @EMPRESA_ID, @USUARIO_ID, TRUE, NOW(), NOW()
);

-- Desarrollo
INSERT IGNORE INTO insumos_compuestos (
    nombre, descripcion, tipo, unidad_medida, rendimiento,
    empresa_id, usuario_id, activo, fecha_creacion, fecha_actualizacion
) VALUES (
    'Ración Desarrollo',
    'Ración para cerdos en desarrollo. Consumo: 2.4-3.0 kg/día. PB: 13-14%. EM: 3000-3100 kcal/kg. Lisina: 0.7-0.9%',
    'RACION', 'kg', 1.0000,
    @EMPRESA_ID, @USUARIO_ID, TRUE, NOW(), NOW()
);

-- Terminación
INSERT IGNORE INTO insumos_compuestos (
    nombre, descripcion, tipo, unidad_medida, rendimiento,
    empresa_id, usuario_id, activo, fecha_creacion, fecha_actualizacion
) VALUES (
    'Ración Terminación',
    'Ración para terminación (70-110 kg). Consumo: 2.8-3.0 kg/día. PB: 13%. EM: 3.265 kcal/kg. Lisina: 0.7-0.9%',
    'RACION', 'kg', 1.0000,
    @EMPRESA_ID, @USUARIO_ID, TRUE, NOW(), NOW()
);

-- ============================================================================
-- PARTE 3: INSERTAR COMPONENTES DE CADA RECETA
-- ============================================================================

-- RACIÓN GESTACIÓN (65% maíz, 20% soja, 10% salvado, 4% núcleo, 1% grasa)
SET @RECETA_GESTACION = (SELECT id FROM insumos_compuestos WHERE nombre = 'Ración Gestación' AND empresa_id = @EMPRESA_ID LIMIT 1);
SET @INS_Maiz = (SELECT id FROM cultivo_insumos WHERE nombre = 'Maíz' AND empresa_id = @EMPRESA_ID LIMIT 1);
SET @INS_Soja = (SELECT id FROM cultivo_insumos WHERE nombre = 'Harina de Soja 44%' AND empresa_id = @EMPRESA_ID LIMIT 1);
SET @INS_Salvado = (SELECT id FROM cultivo_insumos WHERE nombre = 'Salvado de Trigo' AND empresa_id = @EMPRESA_ID LIMIT 1);
SET @INS_NucleoGest = (SELECT id FROM cultivo_insumos WHERE nombre = 'Núcleo Gestación' AND empresa_id = @EMPRESA_ID LIMIT 1);
SET @INS_Grasa = (SELECT id FROM cultivo_insumos WHERE nombre = 'Aceite/Grasa Animal' AND empresa_id = @EMPRESA_ID LIMIT 1);

INSERT IGNORE INTO componentes_insumo_compuesto (
    insumo_compuesto_id, insumo_id, tipo_componente, porcentaje, orden_mezcla, unidad_medida
) VALUES
(@RECETA_GESTACION, @INS_Maiz, 'INSUMO', 65.0, 1, 'kg'),
(@RECETA_GESTACION, @INS_Soja, 'INSUMO', 20.0, 2, 'kg'),
(@RECETA_GESTACION, @INS_Salvado, 'INSUMO', 10.0, 3, 'kg'),
(@RECETA_GESTACION, @INS_NucleoGest, 'INSUMO', 4.0, 4, 'kg'),
(@RECETA_GESTACION, @INS_Grasa, 'INSUMO', 1.0, 5, 'kg');

-- RACIÓN LACTANCIA (60% maíz, 25% soja, 5% salvado, 7% núcleo, 3% grasa)
SET @RECETA_LACTANCIA = (SELECT id FROM insumos_compuestos WHERE nombre = 'Ración Lactancia' AND empresa_id = @EMPRESA_ID LIMIT 1);
SET @INS_NucleoLact = (SELECT id FROM cultivo_insumos WHERE nombre = 'Núcleo Lactancia' AND empresa_id = @EMPRESA_ID LIMIT 1);

INSERT IGNORE INTO componentes_insumo_compuesto (
    insumo_compuesto_id, insumo_id, tipo_componente, porcentaje, orden_mezcla, unidad_medida
) VALUES
(@RECETA_LACTANCIA, @INS_Maiz, 'INSUMO', 60.0, 1, 'kg'),
(@RECETA_LACTANCIA, @INS_Soja, 'INSUMO', 25.0, 2, 'kg'),
(@RECETA_LACTANCIA, @INS_Salvado, 'INSUMO', 5.0, 3, 'kg'),
(@RECETA_LACTANCIA, @INS_NucleoLact, 'INSUMO', 7.0, 4, 'kg'),
(@RECETA_LACTANCIA, @INS_Grasa, 'INSUMO', 3.0, 5, 'kg');

-- RACIÓN DESTETE F1 (67.5% maíz, 30% soja, 2.5% núcleo)
SET @RECETA_DESTETE = (SELECT id FROM insumos_compuestos WHERE nombre = 'Ración Destete F1' AND empresa_id = @EMPRESA_ID LIMIT 1);
SET @INS_NucleoDestete = (SELECT id FROM cultivo_insumos WHERE nombre = 'Núcleo Destete' AND empresa_id = @EMPRESA_ID LIMIT 1);

INSERT IGNORE INTO componentes_insumo_compuesto (
    insumo_compuesto_id, insumo_id, tipo_componente, porcentaje, orden_mezcla, unidad_medida
) VALUES
(@RECETA_DESTETE, @INS_Maiz, 'INSUMO', 67.5, 1, 'kg'),
(@RECETA_DESTETE, @INS_Soja, 'INSUMO', 30.0, 2, 'kg'),
(@RECETA_DESTETE, @INS_NucleoDestete, 'INSUMO', 2.5, 3, 'kg');

-- RACIÓN INICIO RECRÍA F2 (72.5% maíz, 25% soja, 2.5% núcleo)
SET @RECETA_INICIO_RECRIA = (SELECT id FROM insumos_compuestos WHERE nombre = 'Ración Inicio Recría F2' AND empresa_id = @EMPRESA_ID LIMIT 1);
SET @INS_NucleoRecriaInicio = (SELECT id FROM cultivo_insumos WHERE nombre = 'Núcleo Recría Inicio' AND empresa_id = @EMPRESA_ID LIMIT 1);

INSERT IGNORE INTO componentes_insumo_compuesto (
    insumo_compuesto_id, insumo_id, tipo_componente, porcentaje, orden_mezcla, unidad_medida
) VALUES
(@RECETA_INICIO_RECRIA, @INS_Maiz, 'INSUMO', 72.5, 1, 'kg'),
(@RECETA_INICIO_RECRIA, @INS_Soja, 'INSUMO', 25.0, 2, 'kg'),
(@RECETA_INICIO_RECRIA, @INS_NucleoRecriaInicio, 'INSUMO', 2.5, 3, 'kg');

-- RACIÓN CRECIMIENTO RECRÍA F3 (75% maíz, 22% soja, 3% núcleo)
SET @RECETA_CRECIMIENTO_RECRIA = (SELECT id FROM insumos_compuestos WHERE nombre = 'Ración Crecimiento Recría F3' AND empresa_id = @EMPRESA_ID LIMIT 1);
SET @INS_NucleoRecriaCrec = (SELECT id FROM cultivo_insumos WHERE nombre = 'Núcleo Recría Crecimiento' AND empresa_id = @EMPRESA_ID LIMIT 1);

INSERT IGNORE INTO componentes_insumo_compuesto (
    insumo_compuesto_id, insumo_id, tipo_componente, porcentaje, orden_mezcla, unidad_medida
) VALUES
(@RECETA_CRECIMIENTO_RECRIA, @INS_Maiz, 'INSUMO', 75.0, 1, 'kg'),
(@RECETA_CRECIMIENTO_RECRIA, @INS_Soja, 'INSUMO', 22.0, 2, 'kg'),
(@RECETA_CRECIMIENTO_RECRIA, @INS_NucleoRecriaCrec, 'INSUMO', 3.0, 3, 'kg');

-- RACIÓN DESARROLLO (78% maíz, 19% soja, 3% núcleo)
SET @RECETA_DESARROLLO = (SELECT id FROM insumos_compuestos WHERE nombre = 'Ración Desarrollo' AND empresa_id = @EMPRESA_ID LIMIT 1);
SET @INS_NucleoDesarrollo = (SELECT id FROM cultivo_insumos WHERE nombre = 'Núcleo Desarrollo' AND empresa_id = @EMPRESA_ID LIMIT 1);

INSERT IGNORE INTO componentes_insumo_compuesto (
    insumo_compuesto_id, insumo_id, tipo_componente, porcentaje, orden_mezcla, unidad_medida
) VALUES
(@RECETA_DESARROLLO, @INS_Maiz, 'INSUMO', 78.0, 1, 'kg'),
(@RECETA_DESARROLLO, @INS_Soja, 'INSUMO', 19.0, 2, 'kg'),
(@RECETA_DESARROLLO, @INS_NucleoDesarrollo, 'INSUMO', 3.0, 3, 'kg');

-- RACIÓN TERMINACIÓN (78% maíz, 20% soja, 2% núcleo)
SET @RECETA_TERMINACION = (SELECT id FROM insumos_compuestos WHERE nombre = 'Ración Terminación' AND empresa_id = @EMPRESA_ID LIMIT 1);
SET @INS_NucleoTerminacion = (SELECT id FROM cultivo_insumos WHERE nombre = 'Núcleo Terminación' AND empresa_id = @EMPRESA_ID LIMIT 1);

INSERT IGNORE INTO componentes_insumo_compuesto (
    insumo_compuesto_id, insumo_id, tipo_componente, porcentaje, orden_mezcla, unidad_medida
) VALUES
(@RECETA_TERMINACION, @INS_Maiz, 'INSUMO', 78.0, 1, 'kg'),
(@RECETA_TERMINACION, @INS_Soja, 'INSUMO', 20.0, 2, 'kg'),
(@RECETA_TERMINACION, @INS_NucleoTerminacion, 'INSUMO', 2.0, 3, 'kg');

-- ============================================================================
-- PARTE 4: ASOCIAR RECETAS A ETAPAS (PORCINOS_RECETAS_ALIMENTACION_ETAPA)
-- ============================================================================

-- Gestación (2-2.5 kg/día)
INSERT IGNORE INTO porcinos_recetas_alimentacion_etapa (
    insumo_compuesto_id, etapa, cantidad_diaria_por_animal, 
    cantidad_diaria_minima, cantidad_diaria_maxima,
    es_por_defecto, observaciones, empresa_id, activo
) VALUES (
    @RECETA_GESTACION, 'GESTACION', 2.25, 2.0, 2.5,
    TRUE, 'Ración por defecto para madres gestantes. Consumo diario: 2-2.5 kg',
    @EMPRESA_ID, TRUE
);

-- Lactancia (6+ kg/día)
INSERT IGNORE INTO porcinos_recetas_alimentacion_etapa (
    insumo_compuesto_id, etapa, cantidad_diaria_por_animal,
    cantidad_diaria_minima, cantidad_diaria_maxima,
    es_por_defecto, observaciones, empresa_id, activo
) VALUES (
    @RECETA_LACTANCIA, 'LACTANCIA', 6.5, 6.0, 8.0,
    TRUE, 'Ración por defecto para madres lactantes. Consumo diario: 6+ kg',
    @EMPRESA_ID, TRUE
);

-- Destete F1 (0.57 kg/día, peso 8-12 kg)
INSERT IGNORE INTO porcinos_recetas_alimentacion_etapa (
    insumo_compuesto_id, etapa, cantidad_diaria_por_animal,
    peso_minimo_animal, peso_maximo_animal,
    es_por_defecto, observaciones, empresa_id, activo
) VALUES (
    @RECETA_DESTETE, 'F1', 0.57, 8.0, 12.0,
    TRUE, 'Ración por defecto para lechones destetados (8-12 kg). Consumo diario: 0.57 kg',
    @EMPRESA_ID, TRUE
);

-- Inicio Recría F2 (1.05 kg/día, peso 12-28 kg)
INSERT IGNORE INTO porcinos_recetas_alimentacion_etapa (
    insumo_compuesto_id, etapa, cantidad_diaria_por_animal,
    peso_minimo_animal, peso_maximo_animal,
    es_por_defecto, observaciones, empresa_id, activo
) VALUES (
    @RECETA_INICIO_RECRIA, 'F2', 1.05, 12.0, 28.0,
    TRUE, 'Ración por defecto para inicio de recría (12-28 kg). Consumo diario: 1.05 kg',
    @EMPRESA_ID, TRUE
);

-- Crecimiento Recría F3 (2.07 kg/día, peso 28-54 kg)
INSERT IGNORE INTO porcinos_recetas_alimentacion_etapa (
    insumo_compuesto_id, etapa, cantidad_diaria_por_animal,
    peso_minimo_animal, peso_maximo_animal,
    es_por_defecto, observaciones, empresa_id, activo
) VALUES (
    @RECETA_CRECIMIENTO_RECRIA, 'F3', 2.07, 28.0, 54.0,
    TRUE, 'Ración por defecto para crecimiento en recría (28-54 kg). Consumo diario: 2.07 kg',
    @EMPRESA_ID, TRUE
);

-- Desarrollo (2.4-3.0 kg/día)
INSERT IGNORE INTO porcinos_recetas_alimentacion_etapa (
    insumo_compuesto_id, etapa, cantidad_diaria_por_animal,
    cantidad_diaria_minima, cantidad_diaria_maxima,
    es_por_defecto, observaciones, empresa_id, activo
) VALUES (
    @RECETA_DESARROLLO, 'DESARROLLO', 2.7, 2.4, 3.0,
    TRUE, 'Ración por defecto para cerdos en desarrollo. Consumo diario: 2.4-3.0 kg',
    @EMPRESA_ID, TRUE
);

-- Terminación (2.8-3.0 kg/día, peso 70-110 kg)
INSERT IGNORE INTO porcinos_recetas_alimentacion_etapa (
    insumo_compuesto_id, etapa, cantidad_diaria_por_animal,
    cantidad_diaria_minima, cantidad_diaria_maxima,
    peso_minimo_animal, peso_maximo_animal,
    es_por_defecto, observaciones, empresa_id, activo
) VALUES (
    @RECETA_TERMINACION, 'TERMINACION', 2.9, 2.8, 3.0,
    70.0, 110.0,
    TRUE, 'Ración por defecto para terminación (70-110 kg). Consumo diario: 2.8-3.0 kg',
    @EMPRESA_ID, TRUE
);

-- ============================================================================
-- VERIFICACIÓN: Mostrar datos insertados
-- ============================================================================

SELECT 'INSUMOS INSERTADOS:' AS TIPO, COUNT(*) AS CANTIDAD FROM cultivo_insumos WHERE empresa_id = @EMPRESA_ID
UNION ALL
SELECT 'RECETAS INSERTADAS:', COUNT(*) FROM insumos_compuestos WHERE empresa_id = @EMPRESA_ID AND tipo = 'RACION'
UNION ALL
SELECT 'COMPONENTES INSERTADOS:', COUNT(*) FROM componentes_insumo_compuesto WHERE insumo_compuesto_id IN (SELECT id FROM insumos_compuestos WHERE empresa_id = @EMPRESA_ID)
UNION ALL
SELECT 'ASOCIACIONES ETAPA:', COUNT(*) FROM porcinos_recetas_alimentacion_etapa WHERE empresa_id = @EMPRESA_ID;

SELECT 'LISTADO DE RECETAS:' AS TIPO, nombre, descripcion FROM insumos_compuestos WHERE empresa_id = @EMPRESA_ID AND tipo = 'RACION' ORDER BY nombre;
