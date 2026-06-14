-- Crear tabla de recordatorios
CREATE TABLE IF NOT EXISTS `recordatorios` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `usuario_id` bigint NOT NULL,
  `titulo` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `descripcion` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `fecha` date NOT NULL,
  `tipo` enum('GENERAL','LABOR','COSECHA','MANTENIMIENTO','INSUMO','REUNION','OTRO') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'GENERAL',
  `labor_id` bigint DEFAULT NULL COMMENT 'ID de la labor relacionada, si aplica',
  `lote_id` bigint DEFAULT NULL COMMENT 'ID del lote relacionado, si aplica',
  `completado` tinyint(1) NOT NULL DEFAULT '0',
  `activo` tinyint(1) NOT NULL DEFAULT '1',
  `fecha_creacion` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `fecha_actualizacion` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_usuario_id` (`usuario_id`),
  KEY `idx_fecha` (`fecha`),
  KEY `idx_labor_id` (`labor_id`),
  KEY `idx_lote_id` (`lote_id`),
  KEY `idx_completado` (`completado`),
  KEY `idx_activo` (`activo`),
  CONSTRAINT `fk_recordatorios_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_recordatorios_labor` FOREIGN KEY (`labor_id`) REFERENCES `labores` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_recordatorios_lote` FOREIGN KEY (`lote_id`) REFERENCES `lotes` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

