-- ============================================================================
-- EJECUTAR MIGRACIÓN V1_113 - Eliminar tabla tipos_corral_porcinos
-- ============================================================================
-- Este script ejecuta manualmente la migración V1_113
-- 
-- INSTRUCCIONES DE EJECUCIÓN:
-- 1. Desde línea de comandos:
--    mysql -u root -p agrocloud < EJECUTAR_V1_113_MANUAL.sql
--
-- 2. Desde MySQL CLI:
--    USE agrocloud;
--    SOURCE agrogestion-backend/EJECUTAR_V1_113_MANUAL.sql;
--
-- 3. Desde phpMyAdmin:
--    Seleccionar base de datos 'agrocloud' y ejecutar este script
-- ============================================================================

USE agrocloud;

SELECT '═══════════════════════════════════════════════════════════════' AS '';
SELECT 'MIGRACIÓN V1_113: Eliminar tabla tipos_corral_porcinos' AS '';
SELECT '═══════════════════════════════════════════════════════════════' AS '';
SELECT '' AS '';

-- ============================================================================
-- PASO 1: Verificar estado actual
-- ============================================================================
SELECT 'PASO 1: Verificando tablas antes de eliminar...' AS estado;
SELECT '' AS '';

SELECT 
    TABLE_NAME AS 'Tabla',
    TABLE_ROWS AS 'Registros_Aprox',
    TABLE_TYPE AS 'Tipo',
    CREATE_TIME AS 'Fecha_Creacion'
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'agrocloud'
  AND (TABLE_NAME = 'tipos_corral_porcinos' OR TABLE_NAME = 'porcinos_tipos_corral_porcinos')
ORDER BY TABLE_NAME;

SELECT '' AS '';
SELECT 'NOTA: Si las tablas aparecen arriba, serán eliminadas permanentemente.' AS mensaje;
SELECT 'Esta acción NO se puede deshacer. Asegúrate de tener un backup si es necesario.' AS mensaje;
SELECT '' AS '';

-- ============================================================================
-- PASO 2: Eliminar tablas
-- ============================================================================
SELECT '═══════════════════════════════════════════════════════════════' AS '';
SELECT 'PASO 2: Ejecutando eliminación de tablas...' AS estado;
SELECT '' AS '';

-- Esta migración elimina la tabla tipos_corral_porcinos porque estaba en 
-- superposición con la estructura de UbicacionInterna (porcinos_ubicaciones_internas).
-- El sistema usa UbicacionInterna (estructura jerárquica: Galpón → Sala → Corral)
-- que tiene FK en Madre, Padrillo y Recria, mientras que TipoCorralPorcino no se usa.

-- Eliminar tabla con prefijo de módulo (si existe)
DROP TABLE IF EXISTS porcinos_tipos_corral_porcinos;

-- Eliminar tabla sin prefijo (si existe, por retrocompatibilidad)
DROP TABLE IF EXISTS tipos_corral_porcinos;

SELECT '✓ Comandos DROP TABLE ejecutados' AS resultado;
SELECT '' AS '';

-- ============================================================================
-- PASO 3: Verificar resultado
-- ============================================================================
SELECT '═══════════════════════════════════════════════════════════════' AS '';
SELECT 'PASO 3: Verificando que las tablas fueron eliminadas...' AS estado;
SELECT '' AS '';

-- Contar tablas restantes
SELECT 
    CASE 
        WHEN COUNT(*) = 0 THEN '✅ ÉXITO: Las tablas fueron eliminadas correctamente'
        ELSE CONCAT('⚠️  ADVERTENCIA: Aún existen ', COUNT(*), ' tabla(s)')
    END AS resultado
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'agrocloud'
  AND (TABLE_NAME = 'tipos_corral_porcinos' OR TABLE_NAME = 'porcinos_tipos_corral_porcinos');

-- Listar tablas porcinos restantes para verificación
SELECT '' AS '';
SELECT 'Verificación: Tablas del módulo Porcinos (muestra las primeras 10):' AS '';
SELECT 
    TABLE_NAME AS 'Tabla',
    TABLE_ROWS AS 'Registros_Aprox'
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'agrocloud'
  AND TABLE_NAME LIKE 'porcinos_%'
ORDER BY TABLE_NAME
LIMIT 10;

SELECT '' AS '';
SELECT '═══════════════════════════════════════════════════════════════' AS '';
SELECT '✅ MIGRACIÓN V1_113 COMPLETADA' AS '';
SELECT '═══════════════════════════════════════════════════════════════' AS '';
SELECT '' AS '';
SELECT 'La tabla tipos_corral_porcinos ha sido eliminada.' AS mensaje;
SELECT 'El sistema ahora usa únicamente porcinos_ubicaciones_internas' AS mensaje;
SELECT 'para gestionar ubicaciones (Galpón → Sala → Corral).' AS mensaje;
