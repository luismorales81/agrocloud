-- ============================================================================
-- DIAGNÓSTICO: Verificar estructura de cultivo_movimientos_inventario
-- ============================================================================
-- Ejecutar en MySQL para detectar desajustes entre la tabla y la entidad JPA.
-- Si hay diferencias, Flyway pudo no haberse ejecutado o hay migraciones pendientes.
-- ============================================================================

-- 1. Verificar que la tabla existe
SELECT 
    CASE WHEN COUNT(*) > 0 THEN 'OK' ELSE 'FALTA' END AS estado_tabla,
    CASE WHEN COUNT(*) > 0 THEN 'Tabla existe' ELSE 'Tabla NO existe - ejecutar migraciones Flyway' END AS mensaje
FROM information_schema.tables 
WHERE table_schema = DATABASE() AND table_name = 'cultivo_movimientos_inventario';

-- 2. Listar columnas actuales vs. columnas esperadas por la entidad MovimientoInventario
-- Esperadas: id, insumo_id, labor_id, tipo_movimiento, cantidad, motivo, fecha_movimiento,
--            usuario_id, created_at, updated_at, origen, referencia_id
SELECT 
    COLUMN_NAME AS columna,
    DATA_TYPE AS tipo,
    IS_NULLABLE AS nullable,
    COLUMN_DEFAULT AS default_val
FROM information_schema.columns 
WHERE table_schema = DATABASE() 
  AND table_name = 'cultivo_movimientos_inventario'
ORDER BY ORDINAL_POSITION;

-- 3. Verificar columnas requeridas por la entidad (añadidas en V1_119)
SELECT 
    CASE 
        WHEN (SELECT COUNT(*) FROM information_schema.columns 
              WHERE table_schema = DATABASE() AND table_name = 'cultivo_movimientos_inventario' 
              AND COLUMN_NAME = 'origen') > 0 
        THEN 'OK' ELSE 'FALTA' 
    END AS columna_origen,
    CASE 
        WHEN (SELECT COUNT(*) FROM information_schema.columns 
              WHERE table_schema = DATABASE() AND table_name = 'cultivo_movimientos_inventario' 
              AND COLUMN_NAME = 'referencia_id') > 0 
        THEN 'OK' ELSE 'FALTA' 
    END AS columna_referencia_id;

-- 4. Verificar FKs (deben apuntar a cultivo_insumos, cultivo_labores, usuarios)
SELECT 
    CONSTRAINT_NAME,
    COLUMN_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM information_schema.KEY_COLUMN_USAGE
WHERE table_schema = DATABASE() 
  AND table_name = 'cultivo_movimientos_inventario'
  AND REFERENCED_TABLE_NAME IS NOT NULL;

-- 5. Si faltan columnas origen/referencia_id, aplicar manualmente:
-- ALTER TABLE cultivo_movimientos_inventario ADD COLUMN origen VARCHAR(50) NULL;
-- ALTER TABLE cultivo_movimientos_inventario ADD COLUMN referencia_id BIGINT NULL;
