-- ============================================================================
-- SCRIPT PARA EJECUTAR MIGRACIONES DEL MÓDULO PORCINOS
-- ============================================================================
-- Este script ejecuta las migraciones V1_102 y V1_103
-- Ejecutar en MySQL: SOURCE ejecutar_migraciones_porcinos.sql;
-- ============================================================================

USE agrocloud;

-- ============================================================================
-- MIGRACIÓN V1_102: Crear catálogos faltantes
-- ============================================================================
SOURCE agrogestion-backend/src/main/resources/db/migration/V1_102__Create_Catalogos_Faltantes_Porcinos.sql;

-- ============================================================================
-- MIGRACIÓN V1_103: Agregar Foreign Keys a entidades existentes
-- ============================================================================
SOURCE agrogestion-backend/src/main/resources/db/migration/V1_103__Add_FKs_To_Existing_Entities.sql;

-- ============================================================================
-- VERIFICACIÓN
-- ============================================================================
SELECT 'Migraciones ejecutadas correctamente' AS resultado;

-- Verificar tablas creadas
SHOW TABLES LIKE 'porcinos_%';


















