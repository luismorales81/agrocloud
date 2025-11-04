-- ========================================
-- SCRIPT MAESTRO PARA INSERTAR TODOS LOS DATOS DE PRUEBA
-- ========================================
-- Este script ejecuta todos los scripts de inserción de datos

USE agrocloud;

-- Mostrar estado inicial
SELECT 'ESTADO INICIAL' as estado;
SELECT COUNT(*) as total_insumos FROM insumos WHERE empresa_id = 1;
SELECT COUNT(*) as total_agroquimicos FROM agroquimicos a JOIN insumos i ON a.insumo_id = i.id WHERE i.empresa_id = 1;

-- ========================================
-- 1. INSERTAR INSUMOS GENERALES
-- ========================================
SOURCE insertar-datos-prueba-insumos.sql;

-- ========================================
-- 2. INSERTAR AGROQUÍMICOS
-- ========================================
SOURCE insertar-datos-prueba-agroquimicos.sql;

-- ========================================
-- 3. INSERTAR DOSIS DE AGROQUÍMICOS
-- ========================================
SOURCE insertar-datos-prueba-dosis.sql;

-- ========================================
-- VERIFICACIÓN FINAL
-- ========================================
SELECT 'ESTADO FINAL' as estado;

-- Resumen de insumos
SELECT 
    'INSUMOS GENERALES' as categoria,
    COUNT(*) as cantidad
FROM insumos 
WHERE empresa_id = 1 
AND tipo NOT IN ('HERBICIDA', 'FUNGICIDA', 'INSECTICIDA')

UNION ALL

SELECT 
    'AGROQUÍMICOS' as categoria,
    COUNT(*) as cantidad
FROM insumos 
WHERE empresa_id = 1 
AND tipo IN ('HERBICIDA', 'FUNGICIDA', 'INSECTICIDA');

-- Resumen por tipo
SELECT 
    tipo,
    COUNT(*) as cantidad,
    SUM(stock_actual) as stock_total,
    AVG(precio_unitario) as precio_promedio
FROM insumos 
WHERE empresa_id = 1 
GROUP BY tipo
ORDER BY tipo;

-- Resumen de dosis
SELECT 
    i.tipo,
    COUNT(di.id) as dosis_configuradas
FROM insumos i
LEFT JOIN dosis_insumos di ON i.id = di.insumo_id
WHERE i.empresa_id = 1 
AND i.tipo IN ('HERBICIDA', 'FUNGICIDA', 'INSECTICIDA')
GROUP BY i.tipo
ORDER BY i.tipo;

SELECT 'DATOS INSERTADOS EXITOSAMENTE' as resultado;
