-- ========================================
-- SCRIPT SIMPLE PARA INSERTAR AGROQUÍMICOS
-- ========================================
-- Este script inserta solo los agroquímicos que faltan

USE agrocloud;

-- Verificar qué insumos ya existen
SELECT 'INSUMOS EXISTENTES' as estado;
SELECT nombre, tipo FROM insumos WHERE empresa_id = 1 ORDER BY tipo, nombre;

-- Insertar solo agroquímicos que no existen
INSERT IGNORE INTO insumos (
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
('Atrazina 50%', 'HERBICIDA', 'Herbicida selectivo para maíz', 'L', 2000.00, 50.00, 5.00, 'AgroQuímica del Sur S.A.', '2025-10-15', 1, 1, 1, NOW(), NOW(), 'Atrazina', '50%', 'Herbicida', 'II', 45, 1.5, 3.0, 'L/ha'),
('2,4-D Amina 48%', 'HERBICIDA', 'Herbicida hormonal selectivo', 'L', 1800.00, 30.00, 3.00, 'AgroQuímica Nacional', '2025-11-30', 1, 1, 1, NOW(), NOW(), '2,4-D', '48%', 'Herbicida', 'II', 30, 1.0, 2.5, 'L/ha'),

-- Fungicidas
('Mancozeb 80%', 'FUNGICIDA', 'Fungicida de contacto para hongos', 'kg', 3200.00, 25.00, 3.00, 'Fungicidas del Campo S.A.', '2025-09-30', 1, 1, 1, NOW(), NOW(), 'Mancozeb', '80%', 'Fungicida', 'III', 21, 2.0, 4.0, 'kg/ha'),
('Azoxistrobina 25%', 'FUNGICIDA', 'Fungicida sistémico de amplio espectro', 'L', 4500.00, 15.00, 2.00, 'Fungicidas del Campo S.A.', '2025-08-15', 1, 1, 1, NOW(), NOW(), 'Azoxistrobina', '25%', 'Fungicida', 'III', 14, 0.5, 1.0, 'L/ha'),
('Tebuconazol 25%', 'FUNGICIDA', 'Fungicida sistémico para royas', 'L', 3800.00, 20.00, 2.00, 'AgroQuímica Nacional', '2025-10-20', 1, 1, 1, NOW(), NOW(), 'Tebuconazol', '25%', 'Fungicida', 'III', 21, 0.3, 0.6, 'L/ha'),

-- Insecticidas
('Imidacloprid 20%', 'INSECTICIDA', 'Insecticida sistémico para pulgones', 'L', 2800.00, 40.00, 4.00, 'Insecticidas Agrícolas S.A.', '2025-07-31', 1, 1, 1, NOW(), NOW(), 'Imidacloprid', '20%', 'Insecticida', 'II', 21, 0.2, 0.4, 'L/ha'),
('Deltametrina 2.5%', 'INSECTICIDA', 'Insecticida de contacto para lepidópteros', 'L', 2200.00, 35.00, 4.00, 'Insecticidas Agrícolas S.A.', '2025-09-15', 1, 1, 1, NOW(), NOW(), 'Deltametrina', '2.5%', 'Insecticida', 'II', 14, 0.1, 0.2, 'L/ha'),
('Clorpirifos 48%', 'INSECTICIDA', 'Insecticida organofosforado', 'L', 1900.00, 25.00, 3.00, 'AgroQuímica del Sur S.A.', '2025-06-30', 1, 1, 1, NOW(), NOW(), 'Clorpirifos', '48%', 'Insecticida', 'II', 30, 0.5, 1.0, 'L/ha');

-- Insertar en la tabla agroquimicos
INSERT IGNORE INTO agroquimicos (
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
AND i.empresa_id = 1
AND NOT EXISTS (SELECT 1 FROM agroquimicos a WHERE a.insumo_id = i.id);

-- Verificar resultado
SELECT 'RESULTADO FINAL' as estado;
SELECT COUNT(*) as total_insumos FROM insumos WHERE empresa_id = 1;
SELECT COUNT(*) as total_agroquimicos FROM agroquimicos a JOIN insumos i ON a.insumo_id = i.id WHERE i.empresa_id = 1;

-- Mostrar agroquímicos insertados
SELECT 
    a.id,
    i.nombre,
    i.tipo,
    a.principio_activo,
    a.concentracion,
    a.categoria_toxicologica,
    i.stock_actual,
    i.precio_unitario
FROM agroquimicos a
JOIN insumos i ON a.insumo_id = i.id
WHERE i.empresa_id = 1
ORDER BY i.tipo, i.nombre;
