-- ========================================
-- SCRIPT PARA APLICAR CAMBIOS DE LOCAL A RAILWAY
-- ========================================
-- Fecha: 2025-11-04
-- Base de datos origen: agrocloud (local)
-- Base de datos destino: railway (Railway)
-- ========================================

USE railway;

-- ========================================
-- 1. CREAR TABLAS FALTANTES
-- ========================================

-- Tabla: dosis_insumos
DROP TABLE IF EXISTS `dosis_insumos`;
CREATE TABLE `dosis_insumos` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `insumo_id` bigint NOT NULL,
  `tipo_aplicacion` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `forma_aplicacion` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `dosis_recomendada_por_ha` double NOT NULL,
  `activo` tinyint(1) DEFAULT '1',
  `fecha_creacion` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `fecha_actualizacion` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_dosis_insumo` (`insumo_id`),
  KEY `idx_dosis_tipo` (`tipo_aplicacion`),
  KEY `idx_dosis_activo` (`activo`),
  CONSTRAINT `dosis_insumos_ibfk_1` FOREIGN KEY (`insumo_id`) REFERENCES `insumos` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: formas_aplicacion
DROP TABLE IF EXISTS `formas_aplicacion`;
CREATE TABLE `formas_aplicacion` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `nombre` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `descripcion` text COLLATE utf8mb4_unicode_ci,
  `activo` tinyint(1) DEFAULT '1',
  `fecha_creacion` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `fecha_actualizacion` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- 2. MODIFICAR TABLA: insumos
-- ========================================
-- Agregar columnas faltantes para agroquímicos
-- NOTA: Si la columna ya existe, se producirá un error que puede ignorarse

ALTER TABLE `insumos`
  ADD COLUMN `principio_activo` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL AFTER `stock_actual`,
  ADD COLUMN `concentracion` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL AFTER `principio_activo`,
  ADD COLUMN `clase_quimica` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL AFTER `concentracion`,
  ADD COLUMN `categoria_toxicologica` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL AFTER `clase_quimica`,
  ADD COLUMN `periodo_carencia_dias` int DEFAULT NULL AFTER `categoria_toxicologica`,
  ADD COLUMN `dosis_minima_por_ha` decimal(10,2) DEFAULT NULL AFTER `periodo_carencia_dias`,
  ADD COLUMN `dosis_maxima_por_ha` decimal(10,2) DEFAULT NULL AFTER `dosis_minima_por_ha`,
  ADD COLUMN `unidad_dosis` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL AFTER `dosis_maxima_por_ha`;

-- ========================================
-- 3. MODIFICAR TABLA: labores
-- ========================================
-- Cambiar precisión de costo_total de decimal(10,2) a decimal(15,2)
-- y cambiar DEFAULT de NULL a '0.00'

ALTER TABLE `labores`
  MODIFY COLUMN `costo_total` decimal(15,2) DEFAULT '0.00';

-- ========================================
-- 4. VERIFICAR OTRAS TABLAS CON DIFERENCIAS
-- ========================================
-- Las siguientes tablas tienen diferencias menores que pueden ser solo diferencias de formato:
-- - movimientos_inventario
-- - movimientos_inventario_granos
-- - usuarios_empresas_roles
-- - usuarios_roles
-- 
-- Revisar manualmente si hay diferencias significativas en estas tablas.

-- ========================================
-- FIN DEL SCRIPT
-- ========================================

