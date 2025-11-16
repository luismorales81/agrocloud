-- Script para crear agroquímicos de prueba
USE agrocloud;

-- Insertar agroquímicos de prueba
INSERT INTO agroquimicos (
    nombre_comercial, 
    principio_activo, 
    tipo, 
    forma_aplicacion, 
    dosis_por_ha, 
    unidad_de_medida, 
    fecha_vencimiento, 
    stock_actual, 
    stock_minimo, 
    activo, 
    fecha_creacion, 
    fecha_actualizacion
) VALUES 
(
    'Glifosato 48%', 
    'Glifosato', 
    'HERBICIDA', 
    'TERRESTRE', 
    2.5, 
    'LTS', 
    '2025-12-31', 
    100.0, 
    10.0, 
    true, 
    NOW(), 
    NOW()
),
(
    'Atrazina 90%', 
    'Atrazina', 
    'HERBICIDA', 
    'TERRESTRE', 
    1.5, 
    'KG', 
    '2025-11-30', 
    50.0, 
    5.0, 
    true, 
    NOW(), 
    NOW()
),
(
    'Mancozeb 80%', 
    'Mancozeb', 
    'FUNGICIDA', 
    'TERRESTRE', 
    2.0, 
    'KG', 
    '2025-10-31', 
    75.0, 
    8.0, 
    true, 
    NOW(), 
    NOW()
),
(
    'Imidacloprid 20%', 
    'Imidacloprid', 
    'INSECTICIDA', 
    'TERRESTRE', 
    0.3, 
    'LTS', 
    '2025-09-30', 
    25.0, 
    3.0, 
    true, 
    NOW(), 
    NOW()
),
(
    '2,4-D Amina', 
    '2,4-D', 
    'HERBICIDA', 
    'TERRESTRE', 
    1.0, 
    'LTS', 
    '2025-08-31', 
    30.0, 
    4.0, 
    true, 
    NOW(), 
    NOW()
);

-- Verificar que se crearon correctamente
SELECT id, nombre_comercial, principio_activo, tipo, stock_actual, activo 
FROM agroquimicos 
ORDER BY id;











