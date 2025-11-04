
/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `campos` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `nombre` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `ubicacion` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `area_hectareas` decimal(10,2) DEFAULT NULL,
  `descripcion` text COLLATE utf8mb4_unicode_ci,
  `poligono` text COLLATE utf8mb4_unicode_ci COMMENT 'Pol??gono del campo en formato GeoJSON',
  `coordenadas` json DEFAULT NULL COMMENT 'Coordenadas del campo como array de puntos lat/lng',
  `estado` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT 'ACTIVO',
  `activo` tinyint(1) DEFAULT '1',
  `user_id` bigint DEFAULT NULL,
  `fecha_creacion` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `fecha_actualizacion` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `empresa_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_campos_empresa` (`empresa_id`),
  KEY `fk_campos_user` (`user_id`),
  CONSTRAINT `campos_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `usuarios` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_campos_empresa` FOREIGN KEY (`empresa_id`) REFERENCES `empresas` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_campos_user` FOREIGN KEY (`user_id`) REFERENCES `usuarios` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cultivos` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `nombre` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `tipo` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `variedad` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `ciclo_dias` int DEFAULT NULL,
  `rendimiento_esperado` decimal(10,2) DEFAULT NULL,
  `precio_por_tonelada` decimal(10,2) DEFAULT NULL,
  `descripcion` text COLLATE utf8mb4_unicode_ci,
  `usuario_id` bigint DEFAULT NULL,
  `activo` tinyint(1) DEFAULT '1',
  `empresa_id` bigint DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `estado` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT 'ACTIVO',
  `unidad_rendimiento` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT 'kg/ha',
  PRIMARY KEY (`id`),
  KEY `usuario_id` (`usuario_id`),
  CONSTRAINT `cultivos_ibfk_1` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `egresos` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `concepto` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `tipo` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `monto` decimal(10,2) NOT NULL,
  `cantidad` decimal(10,2) DEFAULT NULL,
  `costo_unitario` decimal(10,2) DEFAULT NULL,
  `referencia_id` bigint DEFAULT NULL,
  `unidad_medida` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `proveedor` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `observaciones` text COLLATE utf8mb4_unicode_ci,
  `estado` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'REGISTRADO',
  `tipo_egreso` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'OTROS_EGRESOS',
  `fecha` date NOT NULL,
  `descripcion` text COLLATE utf8mb4_unicode_ci,
  `lote_id` bigint DEFAULT NULL,
  `usuario_id` bigint DEFAULT NULL,
  `fecha_creacion` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `fecha_actualizacion` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `user_id` bigint DEFAULT NULL,
  `empresa_id` bigint DEFAULT NULL,
  `activo` tinyint(1) DEFAULT '1',
  `costo_total` decimal(15,2) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `lote_id` (`lote_id`),
  KEY `usuario_id` (`usuario_id`),
  KEY `idx_egresos_estado` (`estado`),
  KEY `idx_egresos_tipo_egreso` (`tipo_egreso`),
  KEY `idx_egresos_referencia` (`referencia_id`),
  CONSTRAINT `egresos_ibfk_1` FOREIGN KEY (`lote_id`) REFERENCES `lotes` (`id`) ON DELETE SET NULL,
  CONSTRAINT `egresos_ibfk_2` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `empresas` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `nombre` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `cuit` varchar(13) COLLATE utf8mb4_unicode_ci NOT NULL,
  `descripcion` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `activo` tinyint(1) DEFAULT '1',
  `fecha_creacion` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `creado_por` bigint DEFAULT NULL,
  `direccion` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `telefono_contacto` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `email_contacto` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `fecha_inicio_trial` date DEFAULT NULL,
  `fecha_fin_trial` date DEFAULT NULL,
  `estado` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'ACTIVO',
  `fecha_actualizacion` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `cuit` (`cuit`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `formas_aplicacion` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `nombre` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `descripcion` text COLLATE utf8mb4_unicode_ci,
  `activo` tinyint(1) DEFAULT '1',
  `fecha_creacion` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `fecha_actualizacion` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `historial_cosechas` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Identificador ??nico del registro de historial',
  `lote_id` bigint NOT NULL COMMENT 'Referencia al lote donde se realiz?? la cosecha',
  `cultivo_id` bigint NOT NULL COMMENT 'Referencia al cultivo cosechado',
  `fecha_siembra` date NOT NULL COMMENT 'Fecha de siembra del cultivo',
  `fecha_cosecha` date NOT NULL COMMENT 'Fecha de cosecha del cultivo',
  `superficie_hectareas` decimal(10,2) NOT NULL COMMENT 'Superficie cosechada en hect??reas',
  `cantidad_cosechada` decimal(10,2) NOT NULL COMMENT 'Cantidad total cosechada',
  `unidad_cosecha` varchar(10) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Unidad de medida de la cosecha (kg, tn, qq)',
  `rendimiento_real` decimal(10,2) DEFAULT NULL COMMENT 'Rendimiento real obtenido en kg/ha',
  `rendimiento_esperado` decimal(10,2) DEFAULT NULL COMMENT 'Rendimiento esperado en kg/ha',
  `variedad_semilla` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Variedad de semilla utilizada',
  `observaciones` text COLLATE utf8mb4_unicode_ci COMMENT 'Observaciones adicionales sobre la cosecha',
  `estado_suelo` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT 'BUENO' COMMENT 'Estado del suelo despu??s de la cosecha',
  `requiere_descanso` tinyint(1) DEFAULT '0' COMMENT 'Indica si el lote requiere per??odo de descanso',
  `dias_descanso_recomendados` int DEFAULT '0' COMMENT 'D??as de descanso recomendados para el lote',
  `fecha_creacion` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Fecha de creaci??n del registro',
  `fecha_actualizacion` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Fecha de ??ltima actualizaci??n',
  `usuario_id` bigint NOT NULL COMMENT 'Usuario que registr?? la cosecha',
  `precio_venta_unitario` decimal(10,2) DEFAULT '0.00' COMMENT 'Precio de venta por unidad (kg/ton)',
  `ingreso_total` decimal(15,2) DEFAULT '0.00' COMMENT 'Ingreso total por la venta',
  `costo_total_produccion` decimal(15,2) DEFAULT '0.00' COMMENT 'Costo total de producción',
  PRIMARY KEY (`id`),
  KEY `idx_historial_cosechas_lote_id` (`lote_id`),
  KEY `idx_historial_cosechas_cultivo_id` (`cultivo_id`),
  KEY `idx_historial_cosechas_usuario_id` (`usuario_id`),
  KEY `idx_historial_cosechas_fecha_cosecha` (`fecha_cosecha`),
  KEY `idx_historial_cosechas_estado_suelo` (`estado_suelo`),
  KEY `idx_historial_cosechas_requiere_descanso` (`requiere_descanso`),
  KEY `idx_historial_cosechas_lote_fecha` (`lote_id`,`fecha_cosecha` DESC),
  KEY `idx_historial_cosechas_usuario_fecha` (`usuario_id`,`fecha_cosecha` DESC),
  CONSTRAINT `fk_historial_cosechas_cultivo` FOREIGN KEY (`cultivo_id`) REFERENCES `cultivos` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_historial_cosechas_lote` FOREIGN KEY (`lote_id`) REFERENCES `lotes` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_historial_cosechas_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`) ON DELETE CASCADE,
  CONSTRAINT `chk_historial_cantidad_positiva` CHECK ((`cantidad_cosechada` > 0)),
  CONSTRAINT `chk_historial_dias_descanso_positivos` CHECK ((`dias_descanso_recomendados` >= 0)),
  CONSTRAINT `chk_historial_estado_suelo_valido` CHECK ((`estado_suelo` in (_utf8mb4'BUENO',_utf8mb4'DESCANSANDO',_utf8mb4'AGOTADO'))),
  CONSTRAINT `chk_historial_fecha_siembra_antes_cosecha` CHECK ((`fecha_siembra` <= `fecha_cosecha`)),
  CONSTRAINT `chk_historial_superficie_positiva` CHECK ((`superficie_hectareas` > 0))
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Historial completo de cosechas por lote para an??lisis y planificaci??n';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ingresos` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `concepto` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `monto` decimal(10,2) NOT NULL,
  `cantidad` decimal(10,2) DEFAULT NULL,
  `unidad_medida` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `cliente_comprador` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `observaciones` text COLLATE utf8mb4_unicode_ci,
  `estado` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'REGISTRADO',
  `tipo_ingreso` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'OTROS_INGRESOS',
  `fecha` date NOT NULL,
  `descripcion` text COLLATE utf8mb4_unicode_ci,
  `lote_id` bigint DEFAULT NULL,
  `usuario_id` bigint DEFAULT NULL,
  `fecha_creacion` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `fecha_actualizacion` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `user_id` bigint NOT NULL,
  `empresa_id` bigint DEFAULT NULL,
  `activo` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`),
  KEY `lote_id` (`lote_id`),
  KEY `usuario_id` (`usuario_id`),
  KEY `fk_ingresos_user` (`user_id`),
  KEY `fk_ingresos_empresa` (`empresa_id`),
  KEY `idx_ingresos_estado` (`estado`),
  KEY `idx_ingresos_tipo_ingreso` (`tipo_ingreso`),
  CONSTRAINT `fk_ingresos_empresa` FOREIGN KEY (`empresa_id`) REFERENCES `empresas` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_ingresos_user` FOREIGN KEY (`user_id`) REFERENCES `usuarios` (`id`) ON DELETE CASCADE,
  CONSTRAINT `ingresos_ibfk_1` FOREIGN KEY (`lote_id`) REFERENCES `lotes` (`id`) ON DELETE SET NULL,
  CONSTRAINT `ingresos_ibfk_2` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `insumos` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `nombre` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `tipo` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `descripcion` text COLLATE utf8mb4_unicode_ci,
  `unidad_medida` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `precio_unitario` decimal(10,2) DEFAULT NULL,
  `stock_minimo` decimal(10,2) DEFAULT '0.00',
  `proveedor` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `fecha_creacion` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `fecha_actualizacion` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `activo` tinyint(1) DEFAULT '1',
  `empresa_id` bigint DEFAULT NULL,
  `user_id` bigint DEFAULT NULL,
  `fecha_vencimiento` date DEFAULT NULL,
  `stock_actual` decimal(10,2) DEFAULT '0.00',
  `principio_activo` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `concentracion` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `clase_quimica` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `categoria_toxicologica` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `periodo_carencia_dias` int DEFAULT NULL,
  `dosis_minima_por_ha` decimal(10,2) DEFAULT NULL,
  `dosis_maxima_por_ha` decimal(10,2) DEFAULT NULL,
  `unidad_dosis` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_insumos_user` (`user_id`),
  KEY `fk_insumos_empresa` (`empresa_id`),
  CONSTRAINT `fk_insumos_empresa` FOREIGN KEY (`empresa_id`) REFERENCES `empresas` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_insumos_user` FOREIGN KEY (`user_id`) REFERENCES `usuarios` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=57 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `inventario_granos` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `cosecha_id` bigint NOT NULL,
  `cultivo_id` bigint NOT NULL,
  `lote_id` bigint NOT NULL,
  `usuario_id` bigint NOT NULL,
  `cantidad_inicial` decimal(10,2) NOT NULL,
  `cantidad_disponible` decimal(10,2) NOT NULL,
  `unidad_medida` varchar(10) NOT NULL,
  `costo_produccion_total` decimal(15,2) NOT NULL DEFAULT '0.00',
  `costo_unitario` decimal(10,2) NOT NULL DEFAULT '0.00',
  `fecha_ingreso` date NOT NULL,
  `estado` varchar(20) NOT NULL DEFAULT 'DISPONIBLE',
  `variedad` varchar(100) DEFAULT NULL,
  `calidad` varchar(50) DEFAULT NULL,
  `ubicacion_almacenamiento` varchar(200) DEFAULT NULL,
  `observaciones` text,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `cosecha_id` (`cosecha_id`),
  KEY `idx_cultivo` (`cultivo_id`),
  KEY `idx_lote` (`lote_id`),
  KEY `idx_usuario` (`usuario_id`),
  KEY `idx_estado` (`estado`),
  KEY `idx_fecha_ingreso` (`fecha_ingreso`),
  CONSTRAINT `inventario_granos_ibfk_1` FOREIGN KEY (`cosecha_id`) REFERENCES `historial_cosechas` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `inventario_granos_ibfk_2` FOREIGN KEY (`cultivo_id`) REFERENCES `cultivos` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `inventario_granos_ibfk_3` FOREIGN KEY (`lote_id`) REFERENCES `lotes` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `inventario_granos_ibfk_4` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `labor_insumos` (
  `id_labor_insumo` bigint NOT NULL AUTO_INCREMENT,
  `id_labor` bigint NOT NULL,
  `id_insumo` bigint NOT NULL,
  `cantidad_usada` decimal(10,2) NOT NULL,
  `cantidad_planificada` decimal(10,2) NOT NULL,
  `costo_unitario` decimal(12,2) NOT NULL,
  `costo_total` decimal(12,2) NOT NULL,
  `observaciones` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_labor_insumo`),
  KEY `idx_labor_insumos_labor` (`id_labor`),
  KEY `idx_labor_insumos_insumo` (`id_insumo`),
  KEY `idx_labor_insumos_created_at` (`created_at`),
  CONSTRAINT `labor_insumos_ibfk_1` FOREIGN KEY (`id_labor`) REFERENCES `labores` (`id`) ON DELETE CASCADE,
  CONSTRAINT `labor_insumos_ibfk_2` FOREIGN KEY (`id_insumo`) REFERENCES `insumos` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Tabla que almacena los insumos utilizados en cada labor agr??cola';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `labor_mano_obra` (
  `id_labor_mano_obra` bigint NOT NULL AUTO_INCREMENT,
  `id_labor` bigint NOT NULL,
  `descripcion` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Descripci??n de la mano de obra (ej: Cuadrilla de 5 jornaleros)',
  `cantidad_personas` int NOT NULL DEFAULT '1' COMMENT 'N??mero de personas involucradas',
  `proveedor` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Proveedor/contratista (si es tercerizado)',
  `costo_total` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT 'Costo total de la mano de obra',
  `horas_trabajo` decimal(8,2) DEFAULT NULL COMMENT 'Horas totales de trabajo',
  `observaciones` text COLLATE utf8mb4_unicode_ci COMMENT 'Observaciones adicionales',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_labor_mano_obra`),
  KEY `idx_labor_mano_obra_labor` (`id_labor`),
  KEY `idx_labor_mano_obra_proveedor` (`proveedor`),
  KEY `idx_labor_mano_obra_cantidad` (`cantidad_personas`),
  CONSTRAINT `labor_mano_obra_ibfk_1` FOREIGN KEY (`id_labor`) REFERENCES `labores` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `chk_labor_mano_obra_cantidad` CHECK ((`cantidad_personas` > 0)),
  CONSTRAINT `chk_labor_mano_obra_costo` CHECK ((`costo_total` >= 0)),
  CONSTRAINT `chk_labor_mano_obra_horas` CHECK (((`horas_trabajo` is null) or (`horas_trabajo` >= 0)))
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Registro de mano de obra utilizada en cada labor';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `labor_maquinaria` (
  `id_labor_maquinaria` bigint NOT NULL AUTO_INCREMENT,
  `id_labor` bigint NOT NULL,
  `descripcion` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Descripci??n de la maquinaria (ej: Sembradora John Deere)',
  `tipo_maquinaria` enum('PROPIA','ALQUILADA') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PROPIA',
  `proveedor` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Proveedor de alquiler (solo si tipo = ALQUILADA)',
  `costo` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT 'Costo de uso/alquiler de la maquinaria',
  `observaciones` text COLLATE utf8mb4_unicode_ci COMMENT 'Observaciones adicionales',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_labor_maquinaria`),
  KEY `idx_labor_maquinaria_labor` (`id_labor`),
  KEY `idx_labor_maquinaria_proveedor` (`proveedor`),
  CONSTRAINT `labor_maquinaria_ibfk_1` FOREIGN KEY (`id_labor`) REFERENCES `labores` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `chk_labor_maquinaria_costo` CHECK ((`costo` >= 0))
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Registro de maquinarias utilizadas en cada labor';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `labores` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `lote_id` bigint NOT NULL,
  `tipo_labor` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `descripcion` text COLLATE utf8mb4_unicode_ci,
  `fecha_inicio` date NOT NULL,
  `fecha_fin` date DEFAULT NULL,
  `estado` enum('PLANIFICADA','EN_PROGRESO','COMPLETADA','CANCELADA','ANULADA') COLLATE utf8mb4_unicode_ci DEFAULT 'PLANIFICADA' COMMENT 'Estado actual de la labor: PLANIFICADA (no ejecutada), EN_PROGRESO (en ejecuci├│n), COMPLETADA (finalizada), CANCELADA (cancelada antes de ejecutar), ANULADA (anulada despu├®s de ejecutar)',
  `costo_total` decimal(15,2) DEFAULT '0.00',
  `observaciones` text COLLATE utf8mb4_unicode_ci,
  `usuario_id` bigint DEFAULT NULL,
  `fecha_creacion` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `fecha_actualizacion` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `activo` tinyint(1) DEFAULT '1',
  `responsable` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Nombre del responsable de la labor',
  `motivo_anulacion` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Justificaci├│n de la anulaci├│n de la labor',
  `fecha_anulacion` timestamp NULL DEFAULT NULL COMMENT 'Fecha y hora en que se anul├│ la labor',
  `usuario_anulacion_id` bigint DEFAULT NULL COMMENT 'Usuario que realiz├│ la anulaci├│n',
  PRIMARY KEY (`id`),
  KEY `lote_id` (`lote_id`),
  KEY `usuario_id` (`usuario_id`),
  KEY `idx_labores_responsable` (`responsable`),
  KEY `idx_labores_usuario_anulacion` (`usuario_anulacion_id`),
  KEY `idx_labores_fecha_anulacion` (`fecha_anulacion`),
  CONSTRAINT `fk_labores_usuario_anulacion` FOREIGN KEY (`usuario_anulacion_id`) REFERENCES `usuarios` (`id`) ON DELETE SET NULL,
  CONSTRAINT `labores_ibfk_1` FOREIGN KEY (`lote_id`) REFERENCES `lotes` (`id`) ON DELETE CASCADE,
  CONSTRAINT `labores_ibfk_2` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=41 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Tabla de labores agr├¡colas con sistema de anulaci├│n y auditor├¡a completo. Incluye gesti├│n de estados y trazabilidad de cambios.';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `lotes` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `nombre` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `campo_id` bigint NOT NULL,
  `area_hectareas` decimal(10,2) DEFAULT NULL,
  `cultivo_actual` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `estado` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'DISPONIBLE' COMMENT 'Estado actual del lote en el ciclo de cultivo',
  `fecha_siembra` date DEFAULT NULL,
  `fecha_cosecha_esperada` date DEFAULT NULL,
  `descripcion` text COLLATE utf8mb4_unicode_ci,
  `fecha_creacion` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `fecha_actualizacion` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `user_id` bigint DEFAULT NULL,
  `activo` tinyint(1) DEFAULT '1',
  `tipo_suelo` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT 'Franco',
  `fecha_ultimo_cambio_estado` timestamp NULL DEFAULT NULL COMMENT 'Fecha y hora del ├║ltimo cambio de estado',
  `motivo_cambio_estado` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Motivo del ├║ltimo cambio de estado',
  `fecha_cosecha_real` date DEFAULT NULL COMMENT 'Fecha real de cosecha (cuando se complet├│)',
  `rendimiento_esperado` decimal(10,2) DEFAULT NULL COMMENT 'Rendimiento esperado en toneladas por hect├írea',
  `rendimiento_real` decimal(10,2) DEFAULT NULL COMMENT 'Rendimiento real obtenido en toneladas por hect├írea',
  PRIMARY KEY (`id`),
  KEY `campo_id` (`campo_id`),
  KEY `fk_lotes_user` (`user_id`),
  KEY `idx_lotes_estado` (`estado`),
  KEY `idx_lotes_fecha_cambio_estado` (`fecha_ultimo_cambio_estado`),
  KEY `idx_lotes_fecha_siembra` (`fecha_siembra`),
  KEY `idx_lotes_fecha_cosecha_esperada` (`fecha_cosecha_esperada`),
  CONSTRAINT `fk_lotes_user` FOREIGN KEY (`user_id`) REFERENCES `usuarios` (`id`) ON DELETE SET NULL,
  CONSTRAINT `lotes_ibfk_1` FOREIGN KEY (`campo_id`) REFERENCES `campos` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=36 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `maquinaria` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `nombre` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `marca` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `modelo` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `estado` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT 'OPERATIVA',
  `descripcion` text COLLATE utf8mb4_unicode_ci,
  `fecha_creacion` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `fecha_actualizacion` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `user_id` bigint DEFAULT NULL,
  `activo` tinyint(1) DEFAULT '1',
  `empresa_id` bigint DEFAULT NULL,
  `anio_fabricacion` int DEFAULT NULL,
  `numero_serie` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `kilometros_uso` decimal(10,2) DEFAULT '0.00' COMMENT 'Kil├│metros totales de uso',
  `costo_por_hora` decimal(10,2) DEFAULT '0.00' COMMENT 'Costo por hora de operaci├│n en ARS',
  `kilometros_mantenimiento_intervalo` int DEFAULT '5000' COMMENT 'Intervalo de mantenimiento en kil├│metros',
  `ultimo_mantenimiento_kilometros` decimal(10,2) DEFAULT '0.00' COMMENT 'Kil├│metros del ├║ltimo mantenimiento',
  `rendimiento_combustible` decimal(8,2) DEFAULT '0.00' COMMENT 'Rendimiento de combustible',
  `unidad_rendimiento` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'km/L' COMMENT 'Unidad de rendimiento (km/L, L/hora, etc.)',
  `costo_combustible_por_litro` decimal(8,2) DEFAULT '0.00' COMMENT 'Costo de combustible por litro en ARS',
  `valor_actual` decimal(12,2) DEFAULT '0.00' COMMENT 'Valor actual de mercado en ARS',
  `fecha_compra` date DEFAULT NULL COMMENT 'Fecha de compra de la maquinaria',
  `tipo` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Tipo de maquinaria (tractor, cosechadora, etc.)',
  PRIMARY KEY (`id`),
  KEY `fk_maquinaria_user` (`user_id`),
  KEY `fk_maquinaria_empresa` (`empresa_id`),
  CONSTRAINT `fk_maquinaria_empresa` FOREIGN KEY (`empresa_id`) REFERENCES `empresas` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_maquinaria_user` FOREIGN KEY (`user_id`) REFERENCES `usuarios` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `movimientos_inventario` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `insumo_id` bigint NOT NULL,
  `labor_id` bigint DEFAULT NULL COMMENT 'ID de la labor relacionada (si aplica)',
  `tipo_movimiento` enum('ENTRADA','SALIDA','AJUSTE') COLLATE utf8mb4_unicode_ci NOT NULL,
  `cantidad` decimal(10,2) NOT NULL,
  `motivo` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Motivo del movimiento',
  `fecha_movimiento` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `usuario_id` bigint DEFAULT NULL COMMENT 'Usuario que realiz├│ el movimiento',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_movimientos_insumo` (`insumo_id`),
  KEY `idx_movimientos_labor` (`labor_id`),
  KEY `idx_movimientos_tipo` (`tipo_movimiento`),
  KEY `idx_movimientos_fecha` (`fecha_movimiento`),
  KEY `idx_movimientos_usuario` (`usuario_id`),
  CONSTRAINT `movimientos_inventario_ibfk_1` FOREIGN KEY (`insumo_id`) REFERENCES `insumos` (`id`) ON DELETE CASCADE,
  CONSTRAINT `movimientos_inventario_ibfk_2` FOREIGN KEY (`labor_id`) REFERENCES `labores` (`id`) ON DELETE SET NULL,
  CONSTRAINT `movimientos_inventario_ibfk_3` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Registro de movimientos de inventario de insumos';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `movimientos_inventario_granos` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `inventario_id` bigint NOT NULL COMMENT 'ID del inventario de granos relacionado',
  `usuario_id` bigint DEFAULT NULL COMMENT 'Usuario que realiz├│ el movimiento',
  `tipo_movimiento` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Tipo de movimiento: ENTRADA, SALIDA, AJUSTE, etc.',
  `cantidad` decimal(10,2) NOT NULL COMMENT 'Cantidad del movimiento',
  `referencia_tipo` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Tipo de referencia: COSECHA, VENTA, COMPRA, etc.',
  `referencia_id` bigint DEFAULT NULL COMMENT 'ID de la referencia relacionada',
  `precio_unitario` decimal(10,2) DEFAULT NULL COMMENT 'Precio unitario del grano',
  `monto_total` decimal(15,2) DEFAULT NULL COMMENT 'Monto total del movimiento',
  `cliente_destino` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Cliente destino (si aplica para ventas)',
  `fecha_movimiento` date NOT NULL COMMENT 'Fecha del movimiento',
  `observaciones` text COLLATE utf8mb4_unicode_ci COMMENT 'Observaciones adicionales',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_movimientos_granos_inventario` (`inventario_id`),
  KEY `idx_movimientos_granos_usuario` (`usuario_id`),
  KEY `idx_movimientos_granos_tipo` (`tipo_movimiento`),
  KEY `idx_movimientos_granos_referencia` (`referencia_tipo`,`referencia_id`),
  KEY `idx_movimientos_granos_fecha` (`fecha_movimiento`),
  CONSTRAINT `movimientos_inventario_granos_ibfk_1` FOREIGN KEY (`inventario_id`) REFERENCES `inventario_granos` (`id`) ON DELETE CASCADE,
  CONSTRAINT `movimientos_inventario_granos_ibfk_2` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Registro de movimientos de inventario de granos';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `permisos` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `nombre` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `descripcion` text COLLATE utf8mb4_unicode_ci,
  `modulo` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `fecha_creacion` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `accion` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT 'READ',
  `activo` tinyint(1) DEFAULT '1',
  `fecha_actualizacion` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `nombre` (`nombre`)
) ENGINE=InnoDB AUTO_INCREMENT=39 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `roles` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `descripcion` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `activo` tinyint(1) DEFAULT '1',
  `fecha_creacion` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `roles_permisos` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `rol_id` bigint NOT NULL,
  `permiso_id` bigint NOT NULL,
  `activo` tinyint(1) DEFAULT '1',
  `fecha_creacion` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `fecha_actualizacion` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_rol_permiso` (`rol_id`,`permiso_id`),
  KEY `permiso_id` (`permiso_id`),
  CONSTRAINT `roles_permisos_ibfk_1` FOREIGN KEY (`rol_id`) REFERENCES `roles` (`id`) ON DELETE CASCADE,
  CONSTRAINT `roles_permisos_ibfk_2` FOREIGN KEY (`permiso_id`) REFERENCES `permisos` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuario_empresas` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `usuario_id` bigint NOT NULL,
  `empresa_id` bigint NOT NULL,
  `rol` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `estado` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'ACTIVO',
  `fecha_creacion` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `creado_por` bigint DEFAULT NULL,
  `fecha_actualizacion` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `fecha_fin` timestamp NULL DEFAULT NULL,
  `fecha_inicio` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_usuario_empresa` (`usuario_id`,`empresa_id`),
  KEY `empresa_id` (`empresa_id`),
  CONSTRAINT `usuario_empresas_ibfk_1` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`) ON DELETE CASCADE,
  CONSTRAINT `usuario_empresas_ibfk_2` FOREIGN KEY (`empresa_id`) REFERENCES `empresas` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuarios` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `password` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `first_name` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `last_name` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `activo` tinyint(1) DEFAULT '1',
  `estado` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'ACTIVO',
  `fecha_creacion` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `fecha_actualizacion` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `parent_user_id` bigint DEFAULT NULL,
  `creado_por_id` bigint DEFAULT NULL,
  `email_verified` tinyint(1) DEFAULT '0',
  `verification_token` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `reset_password_token` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `reset_password_token_expiry` timestamp NULL DEFAULT NULL,
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `email` (`email`),
  KEY `idx_parent_user` (`parent_user_id`),
  KEY `idx_creado_por` (`creado_por_id`),
  KEY `idx_email_verified` (`email_verified`),
  KEY `idx_verification_token` (`verification_token`),
  KEY `idx_reset_password_token` (`reset_password_token`),
  KEY `idx_phone` (`phone`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuarios_empresas_roles` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `usuario_id` bigint NOT NULL,
  `empresa_id` bigint NOT NULL,
  `rol_id` bigint NOT NULL,
  `activo` tinyint(1) DEFAULT '1',
  `fecha_creacion` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `fecha_actualizacion` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_usuario_empresa_rol` (`usuario_id`,`empresa_id`,`rol_id`),
  KEY `empresa_id` (`empresa_id`),
  KEY `rol_id` (`rol_id`),
  CONSTRAINT `usuarios_empresas_roles_ibfk_1` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`) ON DELETE CASCADE,
  CONSTRAINT `usuarios_empresas_roles_ibfk_2` FOREIGN KEY (`empresa_id`) REFERENCES `empresas` (`id`) ON DELETE CASCADE,
  CONSTRAINT `usuarios_empresas_roles_ibfk_3` FOREIGN KEY (`rol_id`) REFERENCES `roles` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuarios_roles` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `usuario_id` bigint NOT NULL,
  `rol_id` bigint NOT NULL,
  `activo` tinyint(1) DEFAULT '1',
  `fecha_creacion` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `fecha_actualizacion` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_usuario_rol` (`usuario_id`,`rol_id`),
  KEY `rol_id` (`rol_id`),
  CONSTRAINT `usuarios_roles_ibfk_1` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`) ON DELETE CASCADE,
  CONSTRAINT `usuarios_roles_ibfk_2` FOREIGN KEY (`rol_id`) REFERENCES `roles` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `weather_api_usage` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `fecha` date NOT NULL,
  `usos_hoy` int NOT NULL DEFAULT '0',
  `limite_diario` int NOT NULL DEFAULT '1000',
  `ultima_actualizacion` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `fecha` (`fecha`),
  KEY `idx_fecha` (`fecha`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Registro de uso diario de la API del clima';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `AddEstadoColumnToCultivos`()
BEGIN
    DECLARE column_exists INT DEFAULT 0;
    
    SELECT COUNT(*) INTO column_exists
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = 'agrocloud'
    AND TABLE_NAME = 'cultivos'
    AND COLUMN_NAME = 'estado';
    
    IF column_exists = 0 THEN
        ALTER TABLE cultivos ADD COLUMN estado VARCHAR(50) DEFAULT 'ACTIVO';
        SELECT 'Columna estado agregada a cultivos' as resultado;
    ELSE
        SELECT 'Columna estado ya existe en cultivos' as resultado;
    END IF;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

