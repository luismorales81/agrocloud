-- ========================================
-- SCRIPT PARA INSERTAR DATOS DE PRUEBA - INSUMOS
-- ========================================
-- Este script inserta datos reales en la base de datos para pruebas

USE agrocloud;

-- Insertar insumos generales
INSERT INTO insumos (
    nombre, 
    tipo, 
    descripcion, 
    unidad_medida, 
    precio_unitario, 
    stock_actual, 
    stock_minimo, 
    proveedor, 
    fecha_vencimiento, 
    activo, 
    empresa_id, 
    user_id,
    fecha_creacion,
    fecha_actualizacion
) VALUES 
-- Fertilizantes
('Urea 46-0-0', 'FERTILIZANTE', 'Fertilizante nitrogenado de alta concentración', 'kg', 450.00, 500.00, 50.00, 'Fertilizantes del Sur S.A.', '2025-12-31', 1, 1, 1, NOW(), NOW()),
('Fosfato Diamónico 18-46-0', 'FERTILIZANTE', 'Fertilizante fosforado para cultivos', 'kg', 380.00, 300.00, 30.00, 'AgroQuímica Nacional', '2025-10-15', 1, 1, 1, NOW(), NOW()),
('Sulfato de Amonio 21-0-0', 'FERTILIZANTE', 'Fertilizante nitrogenado y azufrado', 'kg', 320.00, 200.00, 25.00, 'Fertilizantes del Sur S.A.', '2025-11-30', 1, 1, 1, NOW(), NOW()),

-- Semillas
('Semilla de Soja STS 40', 'SEMILLA', 'Semilla de soja resistente a glifosato', 'kg', 8500.00, 50.00, 5.00, 'Semillas del Campo S.A.', '2025-08-15', 1, 1, 1, NOW(), NOW()),
('Semilla de Maíz Híbrido 30', 'SEMILLA', 'Semilla de maíz híbrido de alto rendimiento', 'kg', 12000.00, 30.00, 3.00, 'AgroSemillas S.A.', '2025-09-20', 1, 1, 1, NOW(), NOW()),
('Semilla de Trigo Variedad 15', 'SEMILLA', 'Semilla de trigo de ciclo corto', 'kg', 4500.00, 100.00, 10.00, 'Semillas del Campo S.A.', '2025-07-10', 1, 1, 1, NOW(), NOW()),

-- Combustibles
('Gasoil Agrícola', 'COMBUSTIBLE', 'Combustible diesel para maquinaria agrícola', 'L', 850.00, 2000.00, 200.00, 'YPF S.A.', '2025-12-31', 1, 1, 1, NOW(), NOW()),
('Nafta Super', 'COMBUSTIBLE', 'Combustible para motores pequeños', 'L', 1200.00, 500.00, 50.00, 'Shell Argentina', '2025-12-31', 1, 1, 1, NOW(), NOW()),

-- Lubricantes
('Aceite Hidráulico 46', 'LUBRICANTE', 'Aceite hidráulico para tractores', 'L', 2500.00, 100.00, 10.00, 'Mobil Argentina', '2025-06-30', 1, 1, 1, NOW(), NOW()),
('Aceite Motor 15W40', 'LUBRICANTE', 'Aceite de motor para maquinaria agrícola', 'L', 1800.00, 80.00, 8.00, 'Shell Argentina', '2025-08-15', 1, 1, 1, NOW(), NOW()),

-- Repuestos
('Filtro de Aire Tractor', 'REPUESTO', 'Filtro de aire para motor de tractor', 'unidad', 4500.00, 20.00, 2.00, 'Repuestos Agrícolas S.A.', '2025-12-31', 1, 1, 1, NOW(), NOW()),
('Correa de Distribución', 'REPUESTO', 'Correa de distribución para motor', 'unidad', 8500.00, 15.00, 2.00, 'Repuestos Agrícolas S.A.', '2025-12-31', 1, 1, 1, NOW(), NOW()),

-- Herramientas
('Pala de Mano', 'HERRAMIENTA', 'Pala de mano para trabajos de campo', 'unidad', 2500.00, 10.00, 1.00, 'Herramientas del Campo', '2025-12-31', 1, 1, 1, NOW(), NOW()),
('Manguera de Riego 50m', 'HERRAMIENTA', 'Manguera para riego por goteo', 'unidad', 15000.00, 5.00, 1.00, 'Sistemas de Riego S.A.', '2025-12-31', 1, 1, 1, NOW(), NOW());

-- Verificar inserción
SELECT 
    id, 
    nombre, 
    tipo, 
    stock_actual, 
    precio_unitario,
    proveedor
FROM insumos 
WHERE empresa_id = 1 
ORDER BY tipo, nombre;
