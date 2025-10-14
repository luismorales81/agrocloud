-- ============================================================
-- SCRIPT DE MIGRACION: Estructura de Base de Datos
-- AgroGestion - Version 2.0
-- Fecha: Octubre 2025
-- ============================================================

SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- TABLA: usuarios
-- ============================================================
CREATE TABLE IF NOT EXISTS `usuarios` (
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
  KEY `fk_parent_user` (`parent_user_id`),
  CONSTRAINT `fk_usuarios_creado_por` FOREIGN KEY (`creado_por_id`) REFERENCES `usuarios` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_usuarios_parent` FOREIGN KEY (`parent_user_id`) REFERENCES `usuarios` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- TABLA: roles
-- ============================================================
CREATE TABLE IF NOT EXISTS `roles` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `nombre` varchar(50) NOT NULL,
  `descripcion` varchar(255) DEFAULT NULL,
  `activo` tinyint(1) NOT NULL DEFAULT '1',
  `fecha_creacion` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `fecha_actualizacion` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_nombre` (`nombre`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- TABLA: empresas
-- ============================================================
CREATE TABLE IF NOT EXISTS `empresas` (
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

-- ============================================================
-- TABLA: usuario_empresas (Sistema NUEVO de roles)
-- ============================================================
CREATE TABLE IF NOT EXISTS `usuario_empresas` (
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

-- ============================================================
-- TABLA: usuarios_empresas_roles (Sistema ANTIGUO - mantener para compatibilidad)
-- ============================================================
CREATE TABLE IF NOT EXISTS `usuarios_empresas_roles` (
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

-- ============================================================
-- TABLA: campos
-- ============================================================
CREATE TABLE IF NOT EXISTS `campos` (
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

-- ============================================================
-- TABLA: lotes
-- ============================================================
CREATE TABLE IF NOT EXISTS `lotes` (
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

-- ============================================================
-- TABLA: cultivos
-- ============================================================
CREATE TABLE IF NOT EXISTS `cultivos` (
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

-- ============================================================
-- TABLA: insumos
-- ============================================================
CREATE TABLE IF NOT EXISTS `insumos` (
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

-- ============================================================
-- TABLA: maquinaria
-- ============================================================
CREATE TABLE IF NOT EXISTS `maquinaria` (
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

-- ============================================================
-- TABLA: labores
-- ============================================================
CREATE TABLE IF NOT EXISTS `labores` (
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

-- ============================================================
-- TABLA: labor_insumos
-- ============================================================
CREATE TABLE IF NOT EXISTS `labor_insumos` (
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

-- ============================================================
-- TABLA: labor_maquinaria
-- ============================================================
CREATE TABLE IF NOT EXISTS `labor_maquinaria` (
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

-- ============================================================
-- TABLA: labor_mano_obra
-- ============================================================
CREATE TABLE IF NOT EXISTS `labor_mano_obra` (
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

-- ============================================================
-- TABLA: historial_cosechas
-- ============================================================
CREATE TABLE IF NOT EXISTS `historial_cosechas` (
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

-- ============================================================
-- TABLA: ingresos
-- ============================================================
CREATE TABLE IF NOT EXISTS `ingresos` (
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

-- ============================================================
-- TABLA: egresos
-- ============================================================
CREATE TABLE IF NOT EXISTS `egresos` (
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

-- ============================================================
-- TABLA: inventario_granos
-- ============================================================
CREATE TABLE IF NOT EXISTS `inventario_granos` (
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

-- ============================================================
-- TABLA: weather_api_usage
-- ============================================================
CREATE TABLE IF NOT EXISTS `weather_api_usage` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `api_name` varchar(50) NOT NULL,
  `request_date` date NOT NULL,
  `request_count` int NOT NULL DEFAULT '0',
  `ultima_actualizacion` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_api_date` (`api_name`, `request_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- FIN DEL SCRIPT DE ESTRUCTURA
-- ============================================================

