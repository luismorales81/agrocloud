-- ========================================
-- SCRIPT PARA INSERTAR DATOS DE PRUEBA - AGROQUÍMICOS
-- ========================================
-- Este script inserta agroquímicos reales en la base de datos

USE agrocloud;

-- Insertar agroquímicos en la tabla insumos con propiedades específicas
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
    fecha_actualizacion,
    -- Campos específicos de agroquímicos
    principio_activo,
    concentracion,
    clase_quimica,
    categoria_toxicologica,
    periodo_carencia_dias,
    dosis_minima_por_ha,
    dosis_maxima_por_ha,
    unidad_dosis
) VALUES 
-- Herbicidas
('Glifosato 48%', 'HERBICIDA', 'Herbicida sistémico no selectivo', 'L', 1500.00, 100.00, 10.00, 'AgroQuímica del Sur S.A.', '2025-12-31', 1, 1, 1, NOW(), NOW(), 'Glifosato', '48%', 'Herbicida', 'III', 30, 2.0, 4.0, 'L/ha'),
('Atrazina 50%', 'HERBICIDA', 'Herbicida selectivo para maíz', 'L', 2000.00, 50.00, 5.00, 'AgroQuímica del Sur S.A.', '2025-10-15', 1, 1, 1, NOW(), NOW(), 'Atrazina', '50%', 'Herbicida', 'II', 45, 1.5, 3.0, 'L/ha'),
('2,4-D Amina 48%', 'HERBICIDA', 'Herbicida hormonal selectivo', 'L', 1800.00, 30.00, 3.00, 'AgroQuímica Nacional', '2025-11-30', 1, 1, 1, NOW(), NOW(), '2,4-D', '48%', 'Herbicida', 'II', 30, 1.0, 2.5, 'L/ha'),

-- Fungicidas
('Mancozeb 80%', 'FUNGICIDA', 'Fungicida de contacto para hongos', 'kg', 3200.00, 25.00, 3.00, 'Fungicidas del Campo S.A.', '2025-09-30', 1, 1, 1, NOW(), NOW(), 'Mancozeb', '80%', 'Fungicida', 'III', 21, 2.0, 4.0, 'kg/ha'),
('Azoxistrobina 25%', 'FUNGICIDA', 'Fungicida sistémico de amplio espectro', 'L', 4500.00, 15.00, 2.00, 'Fungicidas del Campo S.A.', '2025-08-15', 1, 1, 1, NOW(), NOW(), 'Azoxistrobina', '25%', 'Fungicida', 'III', 14, 0.5, 1.0, 'L/ha'),
('Tebuconazol 25%', 'FUNGICIDA', 'Fungicida sistémico para royas', 'L', 3800.00, 20.00, 2.00, 'AgroQuímica Nacional', '2025-10-20', 1, 1, 1, NOW(), NOW(), 'Tebuconazol', '25%', 'Fungicida', 'III', 21, 0.3, 0.6, 'L/ha'),

-- Insecticidas
('Imidacloprid 20%', 'INSECTICIDA', 'Insecticida sistémico para pulgones', 'L', 2800.00, 40.00, 4.00, 'Insecticidas Agrícolas S.A.', '2025-07-31', 1, 1, 1, NOW(), NOW(), 'Imidacloprid', '20%', 'Insecticida', 'II', 21, 0.2, 0.4, 'L/ha'),
('Deltametrina 2.5%', 'INSECTICIDA', 'Insecticida de contacto para lepidópteros', 'L', 2200.00, 35.00, 4.00, 'Insecticidas Agrícolas S.A.', '2025-09-15', 1, 1, 1, NOW(), NOW(), 'Deltametrina', '2.5%', 'Insecticida', 'II', 14, 0.1, 0.2, 'L/ha'),
('Clorpirifos 48%', 'INSECTICIDA', 'Insecticida organofosforado', 'L', 1900.00, 25.00, 3.00, 'AgroQuímica del Sur S.A.', '2025-06-30', 1, 1, 1, NOW(), NOW(), 'Clorpirifos', '48%', 'Insecticida', 'II', 30, 0.5, 1.0, 'L/ha'),

-- Fertilizantes especiales
('Fosfato Monoamónico 12-61-0', 'FERTILIZANTE', 'Fertilizante fosforado de alta concentración', 'kg', 550.00, 200.00, 20.00, 'Fertilizantes del Sur S.A.', '2025-12-31', 1, 1, 1, NOW(), NOW(), 'Fosfato Monoamónico', '12-61-0', 'Fertilizante', 'No tóxico', 0, 50.0, 100.0, 'kg/ha'),
('Sulfato de Potasio 0-0-50', 'FERTILIZANTE', 'Fertilizante potásico puro', 'kg', 420.00, 150.00, 15.00, 'AgroQuímica Nacional', '2025-12-31', 1, 1, 1, NOW(), NOW(), 'Sulfato de Potasio', '0-0-50', 'Fertilizante', 'No tóxico', 0, 30.0, 60.0, 'kg/ha');

-- Insertar en la tabla agroquimicos para los que tienen propiedades específicas
INSERT INTO agroquimicos (
    insumo_id,
    principio_activo,
    concentracion,
    clase_quimica,
    categoria_toxicologica,
    periodo_carencia_dias,
    dosis_minima_por_ha,
    dosis_maxima_por_ha,
    unidad_dosis,
    activo,
    fecha_creacion,
    fecha_actualizacion
) 
SELECT 
    i.id,
    i.principio_activo,
    i.concentracion,
    i.clase_quimica,
    i.categoria_toxicologica,
    i.periodo_carencia_dias,
    i.dosis_minima_por_ha,
    i.dosis_maxima_por_ha,
    i.unidad_dosis,
    1,
    NOW(),
    NOW()
FROM insumos i 
WHERE i.tipo IN ('HERBICIDA', 'FUNGICIDA', 'INSECTICIDA') 
AND i.empresa_id = 1;

-- Verificar inserción de agroquímicos
SELECT 
    a.id,
    i.nombre,
    i.tipo,
    a.principio_activo,
    a.concentracion,
    a.categoria_toxicologica,
    a.periodo_carencia_dias,
    i.stock_actual,
    i.precio_unitario
FROM agroquimicos a
JOIN insumos i ON a.insumo_id = i.id
WHERE i.empresa_id = 1
ORDER BY i.tipo, i.nombre;
