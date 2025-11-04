-- ========================================
-- SCRIPT PARA VERIFICAR DATOS EXISTENTES
-- ========================================

USE agrocloud;

-- Mostrar resumen de insumos
SELECT 'RESUMEN DE INSUMOS' as titulo;
SELECT 
    tipo,
    COUNT(*) as cantidad,
    SUM(stock_actual) as stock_total,
    AVG(precio_unitario) as precio_promedio
FROM insumos 
WHERE empresa_id = 1 
GROUP BY tipo
ORDER BY tipo;

-- Mostrar agroquímicos existentes
SELECT 'AGROQUÍMICOS EXISTENTES' as titulo;
SELECT 
    i.nombre,
    i.tipo,
    i.principio_activo,
    i.concentracion,
    i.categoria_toxicologica,
    i.stock_actual,
    i.precio_unitario
FROM insumos i 
WHERE i.tipo IN ('HERBICIDA', 'FUNGICIDA', 'INSECTICIDA') 
AND i.empresa_id = 1
ORDER BY i.tipo, i.nombre;

-- Verificar si existen en tabla agroquimicos
SELECT 'AGROQUÍMICOS EN TABLA ESPECÍFICA' as titulo;
SELECT 
    a.id,
    i.nombre,
    a.principio_activo,
    a.concentracion,
    a.categoria_toxicologica
FROM agroquimicos a
JOIN insumos i ON a.insumo_id = i.id
WHERE i.empresa_id = 1
ORDER BY i.tipo, i.nombre;

-- Mostrar insumos generales
SELECT 'INSUMOS GENERALES' as titulo;
SELECT 
    nombre,
    tipo,
    stock_actual,
    precio_unitario,
    proveedor
FROM insumos 
WHERE empresa_id = 1 
AND tipo NOT IN ('HERBICIDA', 'FUNGICIDA', 'INSECTICIDA')
ORDER BY tipo, nombre;
