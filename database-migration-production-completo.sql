-- ============================================================
-- SCRIPT COMPLETO: Migración PRODUCTION (TODO EN UNO)
-- AgroGestion - Version 2.0
-- ============================================================
-- 
-- ⚠️⚠️⚠️ ADVERTENCIA: Este es un script de PRODUCCIÓN ⚠️⚠️⚠️
-- 
-- Este script ELIMINA todas las tablas existentes y las recrea.
-- ¡TODOS LOS DATOS SE PERDERÁN!
--
-- Incluye TODO en un solo archivo:
-- - Eliminación de tablas existentes
-- - Creación de estructura completa
-- - Roles del sistema
-- - Cultivos base
-- - 1 usuario administrador (password TEMPORAL)
-- - 1 empresa (DEBES actualizar los datos)
--
-- USO (Railway):
--   mysql -h HOST -P PORT -u USER -pPASSWORD DATABASE < database-migration-production-completo.sql
--
-- USO (Local):
--   mysql -u root -p agrogestion_db < database-migration-production-completo.sql
--
-- O desde MySQL:
--   USE agrogestion_db;
--   source database-migration-production-completo.sql;
--
-- ============================================================

SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE `agrogestion_db`;

-- ============================================================
-- PASO 1: ELIMINAR TABLAS EXISTENTES
-- ============================================================

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
-- PASO 2: CREAR ESTRUCTURA DE TABLAS
-- ============================================================

-- Tabla: usuarios
CREATE TABLE `usuarios` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `email` varchar(100) NOT NULL,
  `password` varchar(255) NOT NULL,
  `first_name` varchar(50) DEFAULT NULL,
  `last_name` varchar(50) DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `estado` varchar(20) DEFAULT 'ACTIVO',
  `activo` tinyint(1) NOT NULL DEFAULT '1',
  `email_verified` tinyint(1) NOT NULL DEFAULT '0',
  `verification_token` varchar(255) DEFAULT NULL,
  `reset_password_token` varchar(255) DEFAULT NULL,
  `reset_password_token_expiry` datetime DEFAULT NULL,
  `creado_por_id` bigint DEFAULT NULL,
  `parent_user_id` bigint DEFAULT NULL,
  `fecha_creacion` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `fecha_actualizacion` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_email` (`email`),
  KEY `idx_email` (`email`),
  KEY `fk_creado_por` (`creado_por_id`),
  KEY `fk_parent_user` (`parent_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: roles
CREATE TABLE `roles` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `nombre` varchar(50) NOT NULL,
  `descripcion` varchar(255) DEFAULT NULL,
  `activo` tinyint(1) NOT NULL DEFAULT '1',
  `fecha_creacion` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `fecha_actualizacion` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_nombre` (`nombre`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: empresas
CREATE TABLE `empresas` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `nombre` varchar(100) NOT NULL,
  `razon_social` varchar(150) DEFAULT NULL,
  `cuit` varchar(20) DEFAULT NULL,
  `direccion` varchar(200) DEFAULT NULL,
  `ciudad` varchar(100) DEFAULT NULL,
  `provincia` varchar(100) DEFAULT NULL,
  `codigo_postal` varchar(10) DEFAULT NULL,
  `telefono` varchar(20) DEFAULT NULL,
  `email_contacto` varchar(100) DEFAULT NULL,
  `sitio_web` varchar(150) DEFAULT NULL,
  `logo_url` varchar(255) DEFAULT NULL,
  `estado` varchar(20) DEFAULT 'ACTIVO',
  `activo` tinyint(1) NOT NULL DEFAULT '1',
  `fecha_creacion` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `fecha_actualizacion` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_cuit` (`cuit`),
  KEY `idx_nombre` (`nombre`),
  KEY `idx_estado` (`estado`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: usuario_empresas
CREATE TABLE `usuario_empresas` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `usuario_id` bigint NOT NULL,
  `empresa_id` bigint NOT NULL,
  `rol` varchar(50) NOT NULL,
  `estado` varchar(20) NOT NULL DEFAULT 'PENDIENTE',
  `fecha_inicio` date DEFAULT NULL,
  `fecha_fin` date DEFAULT NULL,
  `creado_por` bigint DEFAULT NULL,
  `fecha_creacion` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `fecha_actualizacion` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_usuario_empresa` (`usuario_id`, `empresa_id`),
  KEY `idx_usuario` (`usuario_id`),
  KEY `idx_empresa` (`empresa_id`),
  KEY `idx_rol` (`rol`),
  KEY `idx_estado` (`estado`),
  KEY `fk_creado_por` (`creado_por`),
  CONSTRAINT `fk_usuario_empresas_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_usuario_empresas_empresa` FOREIGN KEY (`empresa_id`) REFERENCES `empresas` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_usuario_empresas_creado_por` FOREIGN KEY (`creado_por`) REFERENCES `usuarios` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: usuarios_empresas_roles
CREATE TABLE `usuarios_empresas_roles` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `usuario_id` bigint NOT NULL,
  `empresa_id` bigint NOT NULL,
  `rol_id` bigint NOT NULL,
  `activo` tinyint(1) NOT NULL DEFAULT '1',
  `fecha_creacion` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `fecha_actualizacion` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_usuario_empresa_rol` (`usuario_id`, `empresa_id`, `rol_id`),
  KEY `idx_usuario` (`usuario_id`),
  KEY `idx_empresa` (`empresa_id`),
  KEY `idx_rol` (`rol_id`),
  CONSTRAINT `fk_uer_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_uer_empresa` FOREIGN KEY (`empresa_id`) REFERENCES `empresas` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_uer_rol` FOREIGN KEY (`rol_id`) REFERENCES `roles` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: campos
CREATE TABLE `campos` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `nombre` varchar(100) NOT NULL,
  `ubicacion` varchar(200) DEFAULT NULL,
  `superficie_total` decimal(10,2) DEFAULT NULL,
  `descripcion` text,
  `tipo_suelo` varchar(50) DEFAULT NULL,
  `latitud` decimal(10,8) DEFAULT NULL,
  `longitud` decimal(11,8) DEFAULT NULL,
  `usuario_id` bigint DEFAULT NULL,
  `empresa_id` bigint DEFAULT NULL,
  `activo` tinyint(1) NOT NULL DEFAULT '1',
  `fecha_creacion` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `fecha_actualizacion` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_nombre` (`nombre`),
  KEY `idx_usuario` (`usuario_id`),
  KEY `idx_empresa` (`empresa_id`),
  CONSTRAINT `fk_campos_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_campos_empresa` FOREIGN KEY (`empresa_id`) REFERENCES `empresas` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: lotes
CREATE TABLE `lotes` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `nombre` varchar(100) NOT NULL,
  `area_hectareas` decimal(10,2) NOT NULL,
  `cultivo_actual` varchar(100) DEFAULT NULL,
  `descripcion` text,
  `tipo_suelo` varchar(50) DEFAULT 'Franco Limoso',
  `estado` varchar(20) DEFAULT 'DISPONIBLE',
  `fecha_siembra` date DEFAULT NULL,
  `fecha_cosecha_esperada` date DEFAULT NULL,
  `campo_id` bigint NOT NULL,
  `usuario_id` bigint DEFAULT NULL,
  `activo` tinyint(1) NOT NULL DEFAULT '1',
  `fecha_creacion` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `fecha_actualizacion` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_nombre` (`nombre`),
  KEY `idx_campo` (`campo_id`),
  KEY `idx_usuario` (`usuario_id`),
  KEY `idx_estado` (`estado`),
  CONSTRAINT `fk_lotes_campo` FOREIGN KEY (`campo_id`) REFERENCES `campos` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_lotes_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: cultivos
CREATE TABLE `cultivos` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `nombre` varchar(100) NOT NULL,
  `nombre_cientifico` varchar(150) DEFAULT NULL,
  `tipo` varchar(50) DEFAULT NULL,
  `descripcion` text,
  `ciclo_dias` int DEFAULT NULL,
  `epoca_siembra_inicio` varchar(20) DEFAULT NULL,
  `epoca_siembra_fin` varchar(20) DEFAULT NULL,
  `temperatura_optima_min` decimal(5,2) DEFAULT NULL,
  `temperatura_optima_max` decimal(5,2) DEFAULT NULL,
  `requerimiento_agua` varchar(50) DEFAULT NULL,
  `activo` tinyint(1) NOT NULL DEFAULT '1',
  `creado_por` bigint DEFAULT NULL,
  `fecha_creacion` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `fecha_actualizacion` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_nombre` (`nombre`),
  KEY `idx_tipo` (`tipo`),
  CONSTRAINT `fk_cultivos_creado_por` FOREIGN KEY (`creado_por`) REFERENCES `usuarios` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: insumos
CREATE TABLE `insumos` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `nombre` varchar(100) NOT NULL,
  `tipo` varchar(50) NOT NULL,
  `descripcion` text,
  `unidad_medida` varchar(20) NOT NULL,
  `precio_unitario` decimal(10,2) DEFAULT '0.00',
  `stock_actual` decimal(10,2) DEFAULT '0.00',
  `stock_minimo` decimal(10,2) DEFAULT '0.00',
  `proveedor` varchar(100) DEFAULT NULL,
  `fecha_vencimiento` date DEFAULT NULL,
  `estado` varchar(20) DEFAULT 'activo',
  `categoria` varchar(50) DEFAULT NULL,
  `activo` tinyint(1) NOT NULL DEFAULT '1',
  `fecha_creacion` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `fecha_actualizacion` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_nombre` (`nombre`),
  KEY `idx_tipo` (`tipo`),
  KEY `idx_estado` (`estado`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: maquinaria
CREATE TABLE `maquinaria` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `nombre` varchar(100) NOT NULL,
  `tipo` varchar(50) NOT NULL,
  `tipo_maquinaria` varchar(50) DEFAULT NULL,
  `marca` varchar(50) DEFAULT NULL,
  `modelo` varchar(50) DEFAULT NULL,
  `anio` int DEFAULT NULL,
  `numero_serie` varchar(50) DEFAULT NULL,
  `estado` varchar(20) DEFAULT 'ACTIVA',
  `horas_uso` decimal(10,2) DEFAULT '0.00',
  `combustible_tipo` varchar(20) DEFAULT NULL,
  `rendimiento_combustible` decimal(10,2) DEFAULT NULL,
  `unidad_rendimiento` varchar(20) DEFAULT NULL,
  `costo_combustible_por_litro` decimal(10,2) DEFAULT NULL,
  `valor_actual` decimal(12,2) DEFAULT NULL,
  `fecha_adquisicion` date DEFAULT NULL,
  `fecha_ultimo_mantenimiento` date DEFAULT NULL,
  `notas` text,
  `activo` tinyint(1) NOT NULL DEFAULT '1',
  `fecha_creacion` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `fecha_actualizacion` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_nombre` (`nombre`),
  KEY `idx_tipo` (`tipo`),
  KEY `idx_estado` (`estado`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: labores
CREATE TABLE `labores` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tipo_labor` varchar(50) NOT NULL,
  `descripcion` text,
  `fecha_realizacion` date NOT NULL,
  `fecha_inicio` date DEFAULT NULL,
  `fecha_fin` date DEFAULT NULL,
  `responsable` varchar(100) DEFAULT NULL,
  `estado` varchar(20) DEFAULT 'PLANIFICADA',
  `costo_base` decimal(12,2) DEFAULT '0.00',
  `costo_maquinaria` decimal(12,2) DEFAULT '0.00',
  `costo_mano_obra` decimal(12,2) DEFAULT '0.00',
  `costo_total` decimal(12,2) DEFAULT '0.00',
  `lote_id` bigint NOT NULL,
  `progreso` int DEFAULT '0',
  `observaciones` text,
  `anulado` tinyint(1) DEFAULT '0',
  `motivo_anulacion` text,
  `anulado_por` bigint DEFAULT NULL,
  `fecha_anulacion` datetime DEFAULT NULL,
  `fecha_creacion` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `fecha_actualizacion` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_tipo` (`tipo_labor`),
  KEY `idx_lote` (`lote_id`),
  KEY `idx_fecha` (`fecha_realizacion`),
  KEY `idx_estado` (`estado`),
  KEY `fk_anulado_por` (`anulado_por`),
  CONSTRAINT `fk_labores_lote` FOREIGN KEY (`lote_id`) REFERENCES `lotes` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_labores_anulado_por` FOREIGN KEY (`anulado_por`) REFERENCES `usuarios` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: labor_insumos
CREATE TABLE `labor_insumos` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `labor_id` bigint NOT NULL,
  `insumo_id` bigint NOT NULL,
  `cantidad` decimal(10,2) NOT NULL,
  `costo_unitario` decimal(10,2) DEFAULT '0.00',
  `costo_total` decimal(12,2) DEFAULT '0.00',
  `fecha_creacion` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_labor` (`labor_id`),
  KEY `idx_insumo` (`insumo_id`),
  CONSTRAINT `fk_labor_insumos_labor` FOREIGN KEY (`labor_id`) REFERENCES `labores` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_labor_insumos_insumo` FOREIGN KEY (`insumo_id`) REFERENCES `insumos` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: labor_maquinaria
CREATE TABLE `labor_maquinaria` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `labor_id` bigint NOT NULL,
  `maquinaria_id` bigint NOT NULL,
  `horas_uso` decimal(10,2) DEFAULT '0.00',
  `costo_por_hora` decimal(10,2) DEFAULT '0.00',
  `combustible_consumido` decimal(10,2) DEFAULT '0.00',
  `costo_combustible` decimal(10,2) DEFAULT '0.00',
  `costo_total` decimal(12,2) DEFAULT '0.00',
  `fecha_creacion` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_labor` (`labor_id`),
  KEY `idx_maquinaria` (`maquinaria_id`),
  CONSTRAINT `fk_labor_maquinaria_labor` FOREIGN KEY (`labor_id`) REFERENCES `labores` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_labor_maquinaria_maquinaria` FOREIGN KEY (`maquinaria_id`) REFERENCES `maquinaria` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: labor_mano_obra
CREATE TABLE `labor_mano_obra` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `labor_id` bigint NOT NULL,
  `nombre_trabajador` varchar(100) DEFAULT NULL,
  `horas_trabajadas` decimal(10,2) DEFAULT '0.00',
  `costo_por_hora` decimal(10,2) DEFAULT '0.00',
  `costo_total` decimal(12,2) DEFAULT '0.00',
  `fecha_creacion` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_labor` (`labor_id`),
  CONSTRAINT `fk_labor_mano_obra_labor` FOREIGN KEY (`labor_id`) REFERENCES `labores` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: historial_cosechas
CREATE TABLE `historial_cosechas` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `lote_id` bigint NOT NULL,
  `cultivo` varchar(100) NOT NULL,
  `fecha_siembra` date NOT NULL,
  `fecha_cosecha` date NOT NULL,
  `rendimiento_total_kg` decimal(12,2) DEFAULT NULL,
  `rendimiento_kg_ha` decimal(10,2) DEFAULT NULL,
  `calidad` varchar(50) DEFAULT NULL,
  `precio_venta_kg` decimal(10,2) DEFAULT NULL,
  `ingreso_total` decimal(12,2) DEFAULT NULL,
  `observaciones` text,
  `anio_agricola` int DEFAULT NULL,
  `fecha_creacion` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_lote` (`lote_id`),
  KEY `idx_cultivo` (`cultivo`),
  KEY `idx_fecha_cosecha` (`fecha_cosecha`),
  KEY `idx_anio` (`anio_agricola`),
  CONSTRAINT `fk_historial_cosechas_lote` FOREIGN KEY (`lote_id`) REFERENCES `lotes` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: ingresos
CREATE TABLE `ingresos` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `concepto` varchar(200) NOT NULL,
  `monto` decimal(12,2) NOT NULL,
  `fecha` date NOT NULL,
  `categoria` varchar(50) DEFAULT 'VENTA_PRODUCCION',
  `metodo_pago` varchar(50) DEFAULT NULL,
  `comprobante` varchar(100) DEFAULT NULL,
  `observaciones` text,
  `lote_id` bigint DEFAULT NULL,
  `fecha_creacion` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `fecha_actualizacion` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_fecha` (`fecha`),
  KEY `idx_categoria` (`categoria`),
  KEY `idx_lote` (`lote_id`),
  CONSTRAINT `fk_ingresos_lote` FOREIGN KEY (`lote_id`) REFERENCES `lotes` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: egresos
CREATE TABLE `egresos` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `concepto` varchar(200) NOT NULL,
  `monto` decimal(12,2) NOT NULL,
  `fecha` date NOT NULL,
  `categoria` varchar(50) DEFAULT 'OTROS',
  `metodo_pago` varchar(50) DEFAULT NULL,
  `comprobante` varchar(100) DEFAULT NULL,
  `observaciones` text,
  `proveedor` varchar(100) DEFAULT NULL,
  `fecha_creacion` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `fecha_actualizacion` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_fecha` (`fecha`),
  KEY `idx_categoria` (`categoria`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: inventario_granos
CREATE TABLE `inventario_granos` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tipo_grano` varchar(50) NOT NULL,
  `cantidad_kg` decimal(12,2) NOT NULL DEFAULT '0.00',
  `ubicacion_almacenamiento` varchar(100) DEFAULT NULL,
  `fecha_ingreso` date DEFAULT NULL,
  `calidad` varchar(50) DEFAULT NULL,
  `humedad_porcentaje` decimal(5,2) DEFAULT NULL,
  `precio_estimado_kg` decimal(10,2) DEFAULT NULL,
  `observaciones` text,
  `lote_origen_id` bigint DEFAULT NULL,
  `fecha_creacion` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `fecha_actualizacion` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_tipo` (`tipo_grano`),
  KEY `idx_lote` (`lote_origen_id`),
  CONSTRAINT `fk_inventario_granos_lote` FOREIGN KEY (`lote_origen_id`) REFERENCES `lotes` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: weather_api_usage
CREATE TABLE `weather_api_usage` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `api_name` varchar(50) NOT NULL,
  `request_date` date NOT NULL,
  `request_count` int NOT NULL DEFAULT '0',
  `ultima_actualizacion` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_api_date` (`api_name`, `request_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- PASO 3: INSERTAR DATOS INICIALES (Roles y Cultivos)
-- ============================================================

-- Roles del sistema
INSERT INTO `roles` (`id`, `nombre`, `descripcion`, `activo`) VALUES
(1, 'SUPERADMIN', 'Super Administrador del Sistema', 1),
(2, 'USUARIO_REGISTRADO', 'Usuario Registrado Básico', 1),
(3, 'INVITADO', 'Usuario Invitado con acceso limitado', 1),
(4, 'ADMINISTRADOR', 'Administrador de Empresa', 1),
(5, 'JEFE_CAMPO', 'Jefe de Campo', 1),
(6, 'JEFE_FINANCIERO', 'Jefe Financiero', 1),
(7, 'OPERARIO', 'Operario de Campo', 1),
(8, 'CONSULTOR_EXTERNO', 'Consultor Externo (Solo Lectura)', 1),
(9, 'PRODUCTOR', 'Productor (Legacy - usar JEFE_CAMPO)', 1),
(10, 'ASESOR', 'Asesor (Legacy - usar JEFE_CAMPO)', 1),
(11, 'TECNICO', 'Técnico (Legacy - usar JEFE_CAMPO)', 1),
(12, 'CONTADOR', 'Contador (Legacy - usar JEFE_FINANCIERO)', 1),
(13, 'LECTURA', 'Solo Lectura (Legacy - usar CONSULTOR_EXTERNO)', 1);

-- Cultivos base
INSERT INTO `cultivos` (`nombre`, `nombre_cientifico`, `tipo`, `descripcion`, `ciclo_dias`, 
  `epoca_siembra_inicio`, `epoca_siembra_fin`, `temperatura_optima_min`, `temperatura_optima_max`, 
  `requerimiento_agua`, `activo`) VALUES
('Maíz', 'Zea mays', 'GRANO', 'Cereal de grano grueso', 120, 'Septiembre', 'Diciembre', 15.00, 30.00, 'MEDIO', 1),
('Soja', 'Glycine max', 'GRANO', 'Leguminosa oleaginosa', 130, 'Octubre', 'Diciembre', 20.00, 30.00, 'MEDIO', 1),
('Trigo', 'Triticum aestivum', 'GRANO', 'Cereal de grano fino', 150, 'Mayo', 'Julio', 10.00, 24.00, 'BAJO', 1),
('Girasol', 'Helianthus annuus', 'GRANO', 'Oleaginosa', 110, 'Septiembre', 'Diciembre', 15.00, 25.00, 'MEDIO', 1),
('Sorgo', 'Sorghum bicolor', 'GRANO', 'Cereal resistente a sequía', 100, 'Octubre', 'Enero', 18.00, 35.00, 'BAJO', 1),
('Cebada', 'Hordeum vulgare', 'GRANO', 'Cereal para maltería', 120, 'Mayo', 'Julio', 8.00, 20.00, 'BAJO', 1),
('Alfalfa', 'Medicago sativa', 'FORRAJE', 'Leguminosa forrajera perenne', 365, 'Marzo', 'Abril', 15.00, 25.00, 'ALTO', 1),
('Avena', 'Avena sativa', 'FORRAJE', 'Cereal forrajero', 90, 'Marzo', 'Julio', 10.00, 25.00, 'MEDIO', 1),
('Centeno', 'Secale cereale', 'FORRAJE', 'Cereal forrajero de invierno', 120, 'Marzo', 'Junio', 5.00, 20.00, 'BAJO', 1),
('Sorgo Forrajero', 'Sorghum bicolor', 'FORRAJE', 'Forraje de verano', 90, 'Octubre', 'Diciembre', 18.00, 35.00, 'BAJO', 1),
('Rye Grass', 'Lolium multiflorum', 'FORRAJE', 'Gramínea forrajera anual', 180, 'Febrero', 'Mayo', 10.00, 25.00, 'MEDIO', 1),
('Tomate', 'Solanum lycopersicum', 'HORTALIZA', 'Fruto climatérico', 120, 'Septiembre', 'Noviembre', 18.00, 28.00, 'ALTO', 1),
('Papa', 'Solanum tuberosum', 'HORTALIZA', 'Tubérculo', 120, 'Agosto', 'Octubre', 15.00, 20.00, 'MEDIO', 1),
('Cebolla', 'Allium cepa', 'HORTALIZA', 'Bulbo', 150, 'Marzo', 'Mayo', 12.00, 24.00, 'MEDIO', 1);

-- ============================================================
-- PASO 4: INSERTAR DATOS DE PRODUCTION
-- ============================================================

-- Usuario administrador inicial (Password temporal: Admin2025!Temp)
INSERT INTO `usuarios` (`id`, `username`, `email`, `password`, `first_name`, `last_name`, 
  `phone`, `estado`, `activo`, `email_verified`) VALUES
(1, 'admin', 'admin@tudominio.com', '$2a$10$K9p3LMoV8xN2wH5yE3pQg.7fB8jK9xM4nL2pR5tQ6wX8vY1zA3bCm', 
  'Administrador', 'Sistema', '', 'ACTIVO', 1, 1);

-- Empresa principal (ACTUALIZAR ESTOS DATOS)
INSERT INTO `empresas` (`id`, `nombre`, `razon_social`, `cuit`, `direccion`, `ciudad`, 
  `provincia`, `telefono`, `email_contacto`, `estado`, `activo`) VALUES
(1, 'Mi Empresa Agropecuaria', 'Mi Empresa Agropecuaria S.A.', '30-00000000-0', 
  'Dirección de la Empresa', 'Ciudad', 'Provincia', 
  'Teléfono', 'contacto@miempresa.com', 'ACTIVO', 1);

-- Asignación de rol administrador
INSERT INTO `usuario_empresas` (`usuario_id`, `empresa_id`, `rol`, `estado`, `fecha_inicio`) VALUES
(1, 1, 'ADMINISTRADOR', 'ACTIVO', CURDATE());

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- VERIFICACIÓN
-- ============================================================

SELECT '============================================' AS '';
SELECT '✅ MIGRACION PRODUCTION COMPLETADA' AS 'ESTADO';
SELECT '============================================' AS '';
SELECT '' AS '';
SELECT 'Tablas creadas:' AS '';
SELECT COUNT(*) AS 'Total' FROM information_schema.tables WHERE table_schema = 'agrogestion_db';
SELECT '' AS '';
SELECT 'Datos cargados:' AS '';
SELECT COUNT(*) AS 'Roles' FROM roles;
SELECT COUNT(*) AS 'Usuarios' FROM usuarios;
SELECT COUNT(*) AS 'Empresas' FROM empresas;
SELECT COUNT(*) AS 'Cultivos' FROM cultivos;
SELECT '' AS '';
SELECT '============================================' AS '';
SELECT '⚠️ TAREAS PENDIENTES:' AS '';
SELECT '============================================' AS '';
SELECT '1. Cambiar password del administrador' AS '';
SELECT '2. Actualizar email del administrador' AS '';
SELECT '3. Actualizar datos de la empresa' AS '';
SELECT '' AS '';
SELECT 'Credenciales temporales:' AS '';
SELECT 'Email: admin@tudominio.com' AS '';
SELECT 'Password: Admin2025!Temp' AS '';
SELECT '============================================' AS '';

-- ============================================================
-- FIN DEL SCRIPT COMPLETO PRODUCTION
-- ============================================================

