-- ============================================================
-- SCRIPT MAESTRO: Migración Completa PRODUCTION
-- AgroGestion - Version 2.0
-- ============================================================
-- 
-- ⚠️⚠️⚠️ ADVERTENCIA: Este script es para PRODUCCIÓN ⚠️⚠️⚠️
-- 
-- Este script ELIMINA todas las tablas existentes y las recrea.
-- ¡TODOS LOS DATOS SE PERDERÁN!
--
-- - Solo crea 1 usuario administrador
-- - No incluye datos de prueba
-- - Debes actualizar los datos de la empresa
-- - Debes cambiar la contraseña inmediatamente
--
-- USO (Railway o servidor remoto):
--   mysql -h HOST -u USER -p DATABASE < database-migration-master-production.sql
--
-- USO (local):
--   mysql -u root -p < database-migration-master-production.sql
--
-- ============================================================

SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- PASO 1: Crear base de datos (si no existe)
-- ============================================================

CREATE DATABASE IF NOT EXISTS `agrogestion_db` 
  CHARACTER SET utf8mb4 
  COLLATE utf8mb4_unicode_ci;

USE `agrogestion_db`;

-- ============================================================
-- PASO 1.5: ELIMINAR TABLAS EXISTENTES
-- ============================================================
-- ⚠️ ADVERTENCIA: Esto eliminará TODAS las tablas y sus datos
-- Orden: De tablas dependientes a tablas principales

DROP TABLE IF EXISTS `labor_insumos`;
DROP TABLE IF EXISTS `labor_maquinaria`;
DROP TABLE IF EXISTS `labor_mano_obra`;
DROP TABLE IF EXISTS `labores`;
DROP TABLE IF EXISTS `historial_cosechas`;
DROP TABLE IF EXISTS `ingresos`;
DROP TABLE IF EXISTS `egresos`;
DROP TABLE IF EXISTS `inventario_granos`;
DROP TABLE IF EXISTS `lotes`;
DROP TABLE IF EXISTS `campos`;
DROP TABLE IF EXISTS `cultivos`;
DROP TABLE IF EXISTS `insumos`;
DROP TABLE IF EXISTS `maquinaria`;
DROP TABLE IF EXISTS `weather_api_usage`;
DROP TABLE IF EXISTS `usuario_empresas`;
DROP TABLE IF EXISTS `usuarios_empresas_roles`;
DROP TABLE IF EXISTS `usuarios`;
DROP TABLE IF EXISTS `roles`;
DROP TABLE IF EXISTS `empresas`;

-- ============================================================
-- PASO 2: Ejecutar estructura de tablas
-- ============================================================

SOURCE database-migration-structure.sql;

-- ============================================================
-- PASO 3: Insertar datos iniciales (roles y cultivos)
-- ============================================================

SOURCE database-migration-data-initial.sql;

-- ============================================================
-- PASO 4: Insertar datos de producción (admin inicial)
-- ============================================================

SOURCE database-migration-data-production.sql;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- VERIFICACIÓN
-- ============================================================

SELECT '============================================' AS '';
SELECT 'MIGRACION COMPLETADA EXITOSAMENTE' AS 'ESTADO';
SELECT '============================================' AS '';
SELECT '' AS '';

SELECT 'Verificando tablas creadas...' AS '';
SELECT COUNT(*) AS 'Total Tablas' FROM information_schema.tables 
WHERE table_schema = 'agrogestion_db';

SELECT '' AS '';
SELECT 'Verificando datos iniciales...' AS '';
SELECT COUNT(*) AS 'Total Roles' FROM roles;
SELECT COUNT(*) AS 'Total Usuarios' FROM usuarios;
SELECT COUNT(*) AS 'Total Empresas' FROM empresas;
SELECT COUNT(*) AS 'Total Cultivos' FROM cultivos;

SELECT '' AS '';
SELECT '============================================' AS '';
SELECT '⚠️  TAREAS PENDIENTES POST-DEPLOYMENT:' AS '';
SELECT '============================================' AS '';
SELECT '1. Cambiar contraseña del administrador' AS '';
SELECT '2. Actualizar datos de la empresa' AS '';
SELECT '3. Configurar backups automáticos' AS '';
SELECT '4. Crear usuarios adicionales' AS '';
SELECT '' AS '';
SELECT 'Credenciales temporales:' AS '';
SELECT 'Email: admin@tudominio.com' AS '';
SELECT 'Password: Admin2025!Temp' AS '';
SELECT '============================================' AS '';

-- ============================================================
-- FIN DEL SCRIPT MAESTRO PRODUCTION
-- ============================================================

