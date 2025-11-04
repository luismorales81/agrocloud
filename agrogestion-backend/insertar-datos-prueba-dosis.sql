-- ========================================
-- SCRIPT PARA INSERTAR DATOS DE PRUEBA - DOSIS DE AGROQUÍMICOS
-- ========================================
-- Este script inserta dosis específicas para cada agroquímico

USE agrocloud;

-- Insertar dosis para herbicidas
INSERT INTO dosis_insumos (
    insumo_id,
    tipo_aplicacion,
    dosis_por_ha,
    unidad_medida,
    descripcion,
    activo,
    fecha_creacion,
    fecha_actualizacion
)
SELECT 
    i.id,
    'PRE_EMERGENCIA',
    2.5,
    'L/ha',
    'Aplicación pre-emergencia para control de malezas',
    1,
    NOW(),
    NOW()
FROM insumos i 
WHERE i.nombre = 'Glifosato 48%' AND i.empresa_id = 1

UNION ALL

SELECT 
    i.id,
    'POST_EMERGENCIA',
    3.0,
    'L/ha',
    'Aplicación post-emergencia para malezas desarrolladas',
    1,
    NOW(),
    NOW()
FROM insumos i 
WHERE i.nombre = 'Glifosato 48%' AND i.empresa_id = 1

UNION ALL

-- Dosis para Atrazina
SELECT 
    i.id,
    'PRE_EMERGENCIA',
    2.0,
    'L/ha',
    'Aplicación pre-emergencia en maíz',
    1,
    NOW(),
    NOW()
FROM insumos i 
WHERE i.nombre = 'Atrazina 50%' AND i.empresa_id = 1

UNION ALL

-- Dosis para 2,4-D
SELECT 
    i.id,
    'POST_EMERGENCIA',
    1.5,
    'L/ha',
    'Aplicación post-emergencia para malezas de hoja ancha',
    1,
    NOW(),
    NOW()
FROM insumos i 
WHERE i.nombre = '2,4-D Amina 48%' AND i.empresa_id = 1

UNION ALL

-- Dosis para fungicidas
SELECT 
    i.id,
    'PREVENTIVO',
    3.0,
    'kg/ha',
    'Aplicación preventiva contra hongos',
    1,
    NOW(),
    NOW()
FROM insumos i 
WHERE i.nombre = 'Mancozeb 80%' AND i.empresa_id = 1

UNION ALL

SELECT 
    i.id,
    'CURATIVO',
    0.5,
    'L/ha',
    'Aplicación curativa contra royas',
    1,
    NOW(),
    NOW()
FROM insumos i 
WHERE i.nombre = 'Azoxistrobina 25%' AND i.empresa_id = 1

UNION ALL

-- Dosis para insecticidas
SELECT 
    i.id,
    'PREVENTIVO',
    0.3,
    'L/ha',
    'Aplicación preventiva contra pulgones',
    1,
    NOW(),
    NOW()
FROM insumos i 
WHERE i.nombre = 'Imidacloprid 20%' AND i.empresa_id = 1

UNION ALL

SELECT 
    i.id,
    'CURATIVO',
    0.15,
    'L/ha',
    'Aplicación curativa contra lepidópteros',
    1,
    NOW(),
    NOW()
FROM insumos i 
WHERE i.nombre = 'Deltametrina 2.5%' AND i.empresa_id = 1;

-- Verificar inserción de dosis
SELECT 
    di.id,
    i.nombre,
    i.tipo,
    di.tipo_aplicacion,
    di.dosis_por_ha,
    di.unidad_medida,
    di.descripcion
FROM dosis_insumos di
JOIN insumos i ON di.insumo_id = i.id
WHERE i.empresa_id = 1
ORDER BY i.tipo, i.nombre, di.tipo_aplicacion;
