-- ============================================================================
-- EJECUTAR MIGRACIÓN V1_113 - VERSIÓN RÁPIDA (Sin verificaciones extensas)
-- ============================================================================
-- Script simplificado para ejecución rápida
-- Ejecutar: mysql -u root -p agrocloud < EJECUTAR_V1_113_RAPIDO.sql
-- ============================================================================

USE agrocloud;

-- Eliminar tablas
DROP TABLE IF EXISTS porcinos_tipos_corral_porcinos;
DROP TABLE IF EXISTS tipos_corral_porcinos;

-- Confirmación
SELECT '✅ Migración V1_113 ejecutada: Tablas tipos_corral_porcinos eliminadas' AS resultado;
