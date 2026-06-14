-- ============================================================================
-- SCRIPT DE VERIFICACIÓN - Migración V1_30
-- ============================================================================
-- Este script verifica que la migración V1_30 se ejecutó correctamente
-- Ejecutar: mysql -u root -p agrocloud < scripts/verificar-migracion-V1_30.sql
-- ============================================================================

USE agrocloud;

-- Verificar que todas las tablas existen
SELECT 
    'Verificando tablas de configuración maestra...' AS estado;

-- Listar todas las tablas creadas por V1_30
SELECT 
    TABLE_NAME AS 'Tabla',
    TABLE_ROWS AS 'Registros',
    CREATE_TIME AS 'Fecha Creación'
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'agrocloud'
  AND TABLE_NAME IN (
    'razas_porcinos',
    -- 'tipos_alimento_porcinos' - ELIMINADA (V1_114) - Redundante con InsumoCompuesto/Insumo/Cultivo
    'tipos_servicio_porcinos',
    'causas_mortalidad_porcinos',
    'motivos_baja_porcinos',
    'esquemas_sanitarios_porcinos',
    -- 'tipos_corral_porcinos' - ELIMINADA (V1_113) - Reemplazada por porcinos_ubicaciones_internas
    'parametros_establecimiento_porcinos',
    'parametros_productivos_porcinos',
    'datos_economicos_porcinos'
  )
ORDER BY TABLE_NAME;

-- Verificar estructura de una tabla ejemplo (razas_porcinos)
SELECT 
    'Estructura de razas_porcinos:' AS info;
DESCRIBE razas_porcinos;

-- Verificar índices
SELECT 
    'Índices de razas_porcinos:' AS info;
SHOW INDEXES FROM razas_porcinos;

-- Verificar que Flyway registró la migración
SELECT 
    'Migraciones de Flyway:' AS info;
SELECT 
    installed_rank,
    version,
    description,
    type,
    script,
    installed_on,
    execution_time,
    success
FROM flyway_schema_history
WHERE version = '1.30' OR description LIKE '%Configuracion_Maestra_Porcinos%'
ORDER BY installed_rank DESC
LIMIT 5;

-- Resumen final
SELECT 
    '✅ Verificación completada' AS resultado,
    COUNT(*) AS 'Tablas encontradas'
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'agrocloud'
  AND TABLE_NAME IN (
    'razas_porcinos',
    -- 'tipos_alimento_porcinos' - ELIMINADA (V1_114) - Redundante con InsumoCompuesto/Insumo/Cultivo
    'tipos_servicio_porcinos',
    'causas_mortalidad_porcinos',
    'motivos_baja_porcinos',
    'esquemas_sanitarios_porcinos',
    -- 'tipos_corral_porcinos' - ELIMINADA (V1_113) - Reemplazada por porcinos_ubicaciones_internas
    'parametros_establecimiento_porcinos',
    'parametros_productivos_porcinos',
    'datos_economicos_porcinos'
  );

