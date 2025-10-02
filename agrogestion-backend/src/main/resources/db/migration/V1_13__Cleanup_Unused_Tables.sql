-- Migración V1_13: Limpieza de tablas no utilizadas
-- Fecha: 30 de Septiembre, 2025
-- Descripción: Eliminar tablas que no tienen implementación o están duplicadas

-- ========================================
-- TABLAS A ELIMINAR
-- ========================================

-- 1. alquiler_maquinaria
--    Motivo: Redundante - Ya está cubierto por labor_maquinaria con tipoMaquinaria=ALQUILADA
--    Registros: 0
--    Código: Entidad existe pero no se usa
DROP TABLE IF EXISTS alquiler_maquinaria;
SELECT 'Tabla alquiler_maquinaria eliminada (redundante con labor_maquinaria)' AS mensaje;

-- 2. mantenimiento_maquinaria (y su variante plural mantenimientos_maquinaria)
--    Motivo: Funcionalidad no implementada
--    Registros: 0
--    Código: Solo entidad, sin servicio ni controlador
DROP TABLE IF EXISTS mantenimiento_maquinaria;
DROP TABLE IF EXISTS mantenimientos_maquinaria;
SELECT 'Tabla mantenimiento_maquinaria eliminada (sin implementación)' AS mensaje;

-- 3. auditoria_empresa
--    Motivo: Tabla huérfana sin código asociado
--    Registros: 0
--    Código: No existe entidad Java
DROP TABLE IF EXISTS auditoria_empresa;
SELECT 'Tabla auditoria_empresa eliminada (sin código asociado)' AS mensaje;

-- 4. configuracion_empresa
--    Motivo: Tabla huérfana sin código asociado
--    Registros: 0
--    Código: No existe entidad Java
DROP TABLE IF EXISTS configuracion_empresa;
SELECT 'Tabla configuracion_empresa eliminada (sin código asociado)' AS mensaje;

-- ========================================
-- RESUMEN
-- ========================================
SELECT 
    '✅ Limpieza completada' AS estado,
    '4 tablas eliminadas' AS cambio,
    'Base de datos optimizada' AS resultado;

-- ========================================
-- TABLAS CONSERVADAS
-- ========================================
-- ✅ movimientos_inventario - EN USO ACTIVO (auditoría de inventario)
-- ✅ logs_acceso - CÓDIGO LISTO (puede activarse para seguridad)

-- ========================================
-- NOTAS IMPORTANTES
-- ========================================
-- - Todas las tablas eliminadas tenían 0 registros
-- - No se pierde información
-- - La funcionalidad de alquiler está en labor_maquinaria
-- - Mantenimiento puede reimplementarse después si se necesita
-- - Auditoría y configuración de empresa nunca se implementaron
