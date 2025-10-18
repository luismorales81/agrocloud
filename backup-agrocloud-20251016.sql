-- MySQL dump 10.13  Distrib 8.0.43, for Win64 (x86_64)
--
-- Host: localhost    Database: agrocloud
-- ------------------------------------------------------
-- Server version	8.0.43

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `campos`
--

DROP TABLE IF EXISTS `campos`;
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

--
-- Dumping data for table `campos`
--

LOCK TABLES `campos` WRITE;
/*!40000 ALTER TABLE `campos` DISABLE KEYS */;
INSERT INTO `campos` VALUES (1,'Campo Principal','Ruta 5 Km 45, C??rdoba',150.00,'Campo principal de la empresa','-27.813,-64.264;-27.814,-64.265;-27.815,-64.266;-27.816,-64.267','[{\"lat\": -27.813, \"lng\": -64.264}, {\"lat\": -27.814, \"lng\": -64.265}, {\"lat\": -27.815, \"lng\": -64.266}, {\"lat\": -27.816, \"lng\": -64.267}]','ACTIVO',1,2,'2025-09-12 02:48:59','2025-09-15 21:48:48',1),(2,'Campo Norte','Ruta 5 Km 50, C??rdoba',200.00,'Campo norte para cultivos de verano','-27.820,-64.270;-27.821,-64.271;-27.822,-64.272;-27.823,-64.273','[{\"lat\": -27.82, \"lng\": -64.27}, {\"lat\": -27.821, \"lng\": -64.271}, {\"lat\": -27.822, \"lng\": -64.272}, {\"lat\": -27.823, \"lng\": -64.273}]','ACTIVO',1,2,'2025-09-12 03:20:09','2025-09-15 21:48:48',1),(3,'Campo Sur','Ruta 5 Km 40, C??rdoba',180.00,'Campo sur para cultivos de invierno','-27.830,-64.280;-27.831,-64.281;-27.832,-64.282;-27.833,-64.283','[{\"lat\": -27.83, \"lng\": -64.28}, {\"lat\": -27.831, \"lng\": -64.281}, {\"lat\": -27.832, \"lng\": -64.282}, {\"lat\": -27.833, \"lng\": -64.283}]','ACTIVO',1,2,'2025-09-12 03:20:09','2025-09-15 21:48:48',1),(4,'Campo Este','Ruta 5 Km 60, C??rdoba',120.00,'Campo este para rotaci??n de cultivos','-27.840,-64.290;-27.841,-64.291;-27.842,-64.292;-27.843,-64.293','[{\"lat\": -27.84, \"lng\": -64.29}, {\"lat\": -27.841, \"lng\": -64.291}, {\"lat\": -27.842, \"lng\": -64.292}, {\"lat\": -27.843, \"lng\": -64.293}]','ELIMINADO',0,2,'2025-09-12 03:20:09','2025-09-15 21:48:48',1),(8,'Campo en santiago - zona sur','los cardozo',28.44,'campo para alfalfa en el sur de santiago','-27.94656013937243,-64.17674240984515;-27.948683121908193,-64.17584118761614;-27.9501236934319,-64.17616305269793;-27.9478111877028,-64.16916785158709;-27.94633267448264,-64.169511174341;-27.94398217590303,-64.16908202089861','[{\"lat\": -27.94656013937243, \"lng\": -64.17674240984515}, {\"lat\": -27.948683121908193, \"lng\": -64.17584118761614}, {\"lat\": -27.9501236934319, \"lng\": -64.17616305269793}, {\"lat\": -27.9478111877028, \"lng\": -64.16916785158709}, {\"lat\": -27.94633267448264, \"lng\": -64.169511174341}, {\"lat\": -27.94398217590303, \"lng\": -64.16908202089861}]','ACTIVO',1,2,'2025-09-16 00:19:27','2025-09-15 22:12:10',1);
/*!40000 ALTER TABLE `campos` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cultivos`
--

DROP TABLE IF EXISTS `cultivos`;
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
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cultivos`
--

LOCK TABLES `cultivos` WRITE;
/*!40000 ALTER TABLE `cultivos` DISABLE KEYS */;
INSERT INTO `cultivos` VALUES (1,'Soja','Oleaginosa','DM 53i53',120,3.50,450.00,'Soja de primera calidad',2,1,1,'2025-09-13 00:10:20','2025-09-15 22:18:42','ACTIVO','kg/ha'),(2,'Ma├¡z','Cereal','DK 72-10',140,8.00,180.00,'Ma├¡z h├¡brido de alto rendimiento',2,1,1,'2025-09-13 00:10:20','2025-09-15 22:18:42','ACTIVO','kg/ha'),(3,'Trigo','Cereal','Klein Escorpi├│n',200,4.50,220.00,'Trigo panadero',2,1,1,'2025-09-13 00:10:20','2025-09-15 22:18:42','ACTIVO','kg/ha'),(4,'Girasol','Oleaginosa','Para├¡so 33',110,2.80,380.00,'Girasol confitero',2,1,1,'2025-09-13 00:10:20','2025-09-15 22:18:42','ACTIVO','kg/ha'),(5,'Cultivo de Prueba','Cereal','Variedad Test',90,5.00,100.00,'Cultivo creado para probar la funcionalidad',2,1,1,'2025-09-15 22:30:28','2025-09-15 22:45:40','ACTIVO','kg/ha'),(7,'Alfalfa','Forrajera','Alfalfa Albert 90- Grupo 9',45,2.00,250000.00,'prueba de nuevo con alfalfa',2,1,1,'2025-09-19 02:05:06','2025-09-19 02:05:06','ACTIVO','tn/ha');
/*!40000 ALTER TABLE `cultivos` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `egresos`
--

DROP TABLE IF EXISTS `egresos`;
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
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `egresos`
--

LOCK TABLES `egresos` WRITE;
/*!40000 ALTER TABLE `egresos` DISABLE KEYS */;
INSERT INTO `egresos` VALUES (1,'Compra de semillas soja','INSUMOS',15000.00,NULL,NULL,NULL,NULL,NULL,NULL,'CONFIRMADO','OTROS_EGRESOS','2025-01-10','Semillas de soja RR para siembra - 25 bolsas',NULL,1,'2025-09-12 03:20:42','2025-10-07 02:44:52',2,NULL,1,15000.00),(2,'Combustible para siembra','OTROS',8000.00,NULL,NULL,NULL,NULL,NULL,NULL,'PAGADO','OTROS_EGRESOS','2025-01-15','Diesel para tractor durante siembra - 100 litros',NULL,1,'2025-09-12 03:20:42','2025-10-07 02:44:55',2,NULL,1,8000.00),(3,'Mano de obra cosecha','OTROS',12000.00,NULL,NULL,NULL,NULL,NULL,NULL,'CANCELADO','OTROS_EGRESOS','2025-04-15','Pago a trabajadores temporarios - 8 jornales',NULL,1,'2025-09-12 03:20:42','2025-10-07 02:44:58',2,NULL,1,12000.00),(4,'Mantenimiento cosechadora','MAQUINARIA_COMPRA',15000.00,NULL,NULL,NULL,NULL,NULL,NULL,'PAGADO','OTROS_EGRESOS','2025-03-01','Reparaci??n de cosechadora',NULL,1,'2025-09-12 03:20:42','2025-10-07 02:45:01',2,NULL,1,15000.00);
/*!40000 ALTER TABLE `egresos` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `empresas`
--

DROP TABLE IF EXISTS `empresas`;
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
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `empresas`
--

LOCK TABLES `empresas` WRITE;
/*!40000 ALTER TABLE `empresas` DISABLE KEYS */;
INSERT INTO `empresas` VALUES (1,'AgroCloud Demo','20-12345678-9','Empresa de demostraci??n',1,'2025-09-12 02:46:55',NULL,NULL,NULL,NULL,NULL,NULL,'ACTIVO','2025-09-12 23:13:05'),(3,'Empresa Admin','','Empresa por defecto para el administrador',1,'2025-09-13 13:16:24',NULL,NULL,NULL,NULL,NULL,NULL,'ACTIVO','2025-09-13 13:46:20'),(5,'MoralesAgro','23290378249',NULL,1,'2025-09-25 06:37:02',1,'mza d lote 12 B° solar mariano moreno','+5493855871225','morales.luis.ariel@gmail.com','2025-09-25','2025-10-25','ACTIVO','2025-09-25 06:37:02'),(6,'CespedesAgro','23233456789',NULL,1,'2025-09-25 07:01:37',1,'mza d lote 12 B° solar mariano moreno','+5493855871225','sacespedes5@gmail.com','2025-09-25','2025-10-25','ACTIVO','2025-09-25 07:01:37');
/*!40000 ALTER TABLE `empresas` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `historial_cosechas`
--

DROP TABLE IF EXISTS `historial_cosechas`;
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
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Historial completo de cosechas por lote para an??lisis y planificaci??n';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `historial_cosechas`
--

LOCK TABLES `historial_cosechas` WRITE;
/*!40000 ALTER TABLE `historial_cosechas` DISABLE KEYS */;
INSERT INTO `historial_cosechas` VALUES (2,33,7,'2025-06-16','2025-09-18',28.44,57000.00,'kg',2004.22,2.00,'Alfalfa Albert 90- Grupo 9','prueba de lote y cosecha','BUENO',0,0,'2025-09-19 02:12:42','2025-10-02 21:46:49',2,120.00,6840.00,5119200.00),(3,1,1,'2024-11-15','2025-04-15',12.50,45.00,'TONELADAS',3600.00,4000.00,'Soja RR 4809','Cosecha exitosa con buen rendimiento','BUENO',0,0,'2025-09-29 21:44:07','2025-10-02 21:46:49',2,350.00,15.75,3125000.00),(4,2,2,'2024-12-01','2025-06-01',13.00,65.00,'TONELADAS',5000.00,5500.00,'Ma├¡z DK 72-10','Rendimiento por encima del esperado','BUENO',0,0,'2025-09-29 21:44:07','2025-10-02 21:46:49',2,180.00,11700.00,3900000.00),(5,1,1,'2025-07-01','2025-11-15',12.50,40.00,'TONELADAS',3200.00,3500.00,'Soja RR 4809','Cosecha de segunda con rendimiento aceptable','BUENO',1,45,'2025-09-29 21:44:07','2025-10-02 21:46:49',2,350.00,14.00,3125000.00),(6,2,3,'2025-06-15','2025-11-15',13.00,52.00,'TONELADAS',4000.00,4200.00,'Trigo Baguette 11','Trigo de buena calidad despu├®s del ma├¡z','BUENO',0,0,'2025-09-29 21:44:07','2025-10-02 21:46:49',2,220.00,11440.00,2600000.00),(11,2,2,'2025-01-20','2025-05-20',50.00,200.00,'ton',4.00,NULL,NULL,'Cosecha de ma??z con buen rendimiento','BUENO',0,0,'2025-09-13 00:10:21','2025-10-02 21:46:49',2,180.00,36000.00,15000000.00),(12,9,3,'2024-07-04','2024-11-01',50.00,35.00,'ton',0.70,NULL,NULL,'Cosecha de trigo de calidad','BUENO',0,0,'2025-09-29 21:48:25','2025-10-02 21:46:49',2,220.00,7700.00,10000000.00),(17,34,1,'2025-05-30','2025-10-01',50.00,183.00,'kg/ha',3.66,3.50,'DM 53i53','buena cosecha de prueba','BUENO',0,0,'2025-10-01 06:36:32','2025-10-02 21:46:49',2,350.00,64.05,12500000.00),(18,34,7,'2025-08-21','2025-10-03',50.00,110.00,'tn/ha',2.20,2.00,'Alfalfa Albert 90- Grupo 9','prueba de cosecha y acopio','BUENO',0,0,'2025-10-03 06:46:40','2025-10-03 06:46:40',2,250000.00,27500000.00,0.01),(19,33,7,'2025-08-20','2025-10-07',28.44,1.00,'tn',0.04,2.00,NULL,'CONVERSIÓN A FORRAJE | Cosecha anticipada para alimentación animal | necesidad de forraje','BUENO',0,0,'2025-10-07 03:03:09','2025-10-07 03:03:09',2,0.00,0.00,3530000.01);
/*!40000 ALTER TABLE `historial_cosechas` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ingresos`
--

DROP TABLE IF EXISTS `ingresos`;
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

--
-- Dumping data for table `ingresos`
--

LOCK TABLES `ingresos` WRITE;
/*!40000 ALTER TABLE `ingresos` DISABLE KEYS */;
INSERT INTO `ingresos` VALUES (1,'Venta soja',33750.00,NULL,NULL,NULL,NULL,'CANCELADO','OTROS_INGRESOS','2025-04-20','Venta de 75 toneladas de soja',NULL,1,'2025-09-12 03:20:42','2025-10-07 02:44:35',2,NULL,1),(2,'Venta ma??z',36000.00,NULL,NULL,NULL,NULL,'CONFIRMADO','OTROS_INGRESOS','2025-05-25','Venta de 200 toneladas de ma??z',NULL,1,'2025-09-12 03:20:42','2025-10-07 02:44:44',2,NULL,1),(3,'Subsidio gobierno',5000.00,NULL,NULL,NULL,NULL,'PAGADO','OTROS_INGRESOS','2025-01-30','Subsidio del gobierno provincial',NULL,1,'2025-09-12 03:20:42','2025-10-07 02:44:25',2,NULL,1),(4,'Venta 40 tn/ha de Alfalfa - Lote lote prueba CP',156000.00,NULL,NULL,'karim beddur',NULL,'PAGADO','VENTA_CULTIVO','2025-10-06','Venta desde inventario ID 1',34,NULL,'2025-10-06 23:59:16','2025-10-06 23:59:16',2,NULL,1);
/*!40000 ALTER TABLE `ingresos` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `insumos`
--

DROP TABLE IF EXISTS `insumos`;
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
  PRIMARY KEY (`id`),
  KEY `fk_insumos_user` (`user_id`),
  KEY `fk_insumos_empresa` (`empresa_id`),
  CONSTRAINT `fk_insumos_empresa` FOREIGN KEY (`empresa_id`) REFERENCES `empresas` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_insumos_user` FOREIGN KEY (`user_id`) REFERENCES `usuarios` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `insumos`
--

LOCK TABLES `insumos` WRITE;
/*!40000 ALTER TABLE `insumos` DISABLE KEYS */;
INSERT INTO `insumos` VALUES (1,'Glifosato 48%','HERBICIDA','Herbicida sistémico no selectivo','Litro',850.00,20.00,'AgroQuímica S.A.','2025-09-12 02:48:59','2025-09-16 15:28:53',1,1,2,'2027-12-31',50.00),(2,'Urea 46%','FERTILIZANTE','Fertilizante nitrogenado','Kg',45.00,100.00,'Fertilizantes del Sur','2025-09-12 02:48:59','2025-09-16 15:28:53',1,1,2,'2028-12-31',100.00),(3,'Fosfato Diamónico','FERTILIZANTE','Fertilizante fosforado','Kg',38.00,50.00,'Fertilizantes del Sur','2025-09-12 02:48:59','2025-09-16 15:28:53',1,1,2,'2028-12-31',80.00),(4,'Semilla Soja','SEMILLA','Semilla de soja certificada','Bolsa',120.00,10.00,'Semillas Premium','2025-09-12 02:48:59','2025-10-01 00:33:22',1,1,2,'2026-12-31',140.00),(5,'Semilla Maíz','SEMILLA','Semilla de maíz híbrido','Bolsa',180.00,5.00,'Semillas Premium','2025-09-12 02:48:59','2025-09-16 15:28:48',1,1,2,'2026-12-31',150.00),(6,'Atrazina 90%','HERBICIDA','Herbicida selectivo para maíz','Litro',1200.00,10.00,'Syngenta','2025-09-12 03:20:09','2025-09-16 15:28:53',1,1,2,'2027-12-31',50.00),(7,'2,4-D Amina','HERBICIDA','Herbicida hormonal','Litro',800.00,5.00,'Dow AgroSciences','2025-09-12 03:20:09','2025-09-16 15:28:53',1,1,2,'2027-12-31',50.00),(8,'Imidacloprid','INSECTICIDA','Insecticida sistémico','Litro',1500.00,5.00,'Bayer','2025-09-12 03:20:09','2025-09-16 15:28:53',1,1,2,'2027-12-31',50.00),(9,'Fosfato Monoamónico','FERTILIZANTE','Fertilizante fosforado','Kg',55.00,100.00,'Fertilizantes del Sur','2025-09-12 03:20:09','2025-09-16 15:28:53',1,1,2,'2028-12-31',50.00),(10,'Sulfato de Amonio','FERTILIZANTE','Fertilizante nitrogenado','Kg',35.00,80.00,'Fertilizantes del Sur','2025-09-12 03:20:09','2025-09-16 15:28:53',1,1,2,'2028-12-31',50.00),(11,'Semilla Trigo','SEMILLA','Semilla de trigo certificada','Bolsa',95.00,8.00,'Semillas Premium','2025-09-12 03:20:09','2025-09-16 15:28:48',1,1,2,'2026-12-31',50.00),(12,'Semilla Girasol','SEMILLA','Semilla de girasol híbrido','Bolsa',150.00,4.00,'Semillas Premium','2025-09-12 03:20:09','2025-09-16 15:28:48',1,1,2,'2026-12-31',50.00),(13,'Semilla Sorgo','SEMILLA','Semilla de sorgo forrajero','Bolsa',110.00,3.00,'Semillas Premium','2025-09-12 03:20:09','2025-09-16 15:28:48',1,1,2,'2026-12-31',50.00),(22,'CLOROTALONIL 72 Delta','FUNGICIDA','fungicidal de ejemplo por si','Litro',78000.00,10.00,'Delta Agro','2025-09-17 00:58:51','2025-09-17 01:26:49',1,1,2,'2028-09-16',50.00);
/*!40000 ALTER TABLE `insumos` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `inventario_granos`
--

DROP TABLE IF EXISTS `inventario_granos`;
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
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `inventario_granos`
--

LOCK TABLES `inventario_granos` WRITE;
/*!40000 ALTER TABLE `inventario_granos` DISABLE KEYS */;
INSERT INTO `inventario_granos` VALUES (1,18,7,34,2,110.00,70.00,'tn/ha',0.01,0.00,'2025-10-03','DISPONIBLE','Alfalfa Albert 90- Grupo 9',NULL,NULL,NULL,'2025-10-03 06:46:40','2025-10-06 23:59:16'),(2,19,7,33,2,1.00,1.00,'tn',3530000.01,3530000.01,'2025-10-07','DISPONIBLE',NULL,NULL,NULL,NULL,'2025-10-07 03:03:10','2025-10-07 03:03:10');
/*!40000 ALTER TABLE `inventario_granos` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `labor_insumos`
--

DROP TABLE IF EXISTS `labor_insumos`;
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
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Tabla que almacena los insumos utilizados en cada labor agr├¡cola';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `labor_insumos`
--

LOCK TABLES `labor_insumos` WRITE;
/*!40000 ALTER TABLE `labor_insumos` DISABLE KEYS */;
INSERT INTO `labor_insumos` VALUES (4,17,4,60.00,60.00,120.00,7200.00,NULL,'2025-10-01 00:33:22','2025-10-01 00:33:22');
/*!40000 ALTER TABLE `labor_insumos` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `labor_mano_obra`
--

DROP TABLE IF EXISTS `labor_mano_obra`;
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
  CONSTRAINT `fk_labor_mano_obra_labor` FOREIGN KEY (`id_labor`) REFERENCES `labores` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `chk_labor_mano_obra_cantidad` CHECK ((`cantidad_personas` > 0)),
  CONSTRAINT `chk_labor_mano_obra_costo` CHECK ((`costo_total` >= 0)),
  CONSTRAINT `chk_labor_mano_obra_horas` CHECK (((`horas_trabajo` is null) or (`horas_trabajo` >= 0)))
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Registro de mano de obra utilizada en cada labor';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `labor_mano_obra`
--

LOCK TABLES `labor_mano_obra` WRITE;
/*!40000 ALTER TABLE `labor_mano_obra` DISABLE KEYS */;
INSERT INTO `labor_mano_obra` VALUES (1,1,'Operador de tractor',1,NULL,200.00,8.00,'Personal propio','2025-09-17 00:03:51','2025-09-17 00:03:51'),(2,1,'Cuadrilla de jornaleros',5,'Jornaleros del Sur',500.00,8.00,'Tercerizado para siembra manual','2025-09-17 00:03:51','2025-09-17 00:03:51'),(3,2,'Operador de fertilizadora',1,NULL,150.00,4.00,'Personal propio','2025-09-17 00:03:51','2025-09-17 00:03:51'),(4,1,'Operador de tractor',1,NULL,150.00,8.50,'Operador especializado en fertilizaci├│n','2025-09-17 15:01:00','2025-09-17 15:01:00'),(5,1,'Cuadrilla de apoyo',2,'Trabajadores Rurales',200.00,4.00,'Personal de apoyo para carga y descarga','2025-09-17 15:01:00','2025-09-17 15:01:00'),(6,2,'Operador de sembradora',1,NULL,120.00,6.00,'Operador especializado en siembra','2025-09-17 15:01:00','2025-09-17 15:01:00'),(7,2,'Supervisor de campo',1,NULL,80.00,8.00,'Supervisor para control de calidad','2025-09-17 15:01:00','2025-09-17 15:01:00'),(8,10,'jornalero',1,NULL,20000.00,1.00,'lalala','2025-09-18 03:06:25','2025-09-18 03:06:25'),(9,13,'changada',4,NULL,100000.00,4.00,NULL,'2025-09-20 02:59:08','2025-09-20 02:59:08'),(10,17,'changada 2',2,NULL,100000.00,NULL,NULL,'2025-10-01 00:33:22','2025-09-30 21:57:14'),(11,31,'changos',3,NULL,300000.00,2.00,NULL,'2025-10-11 00:49:28','2025-10-11 00:49:28');
/*!40000 ALTER TABLE `labor_mano_obra` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `labor_maquinaria`
--

DROP TABLE IF EXISTS `labor_maquinaria`;
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
  CONSTRAINT `fk_labor_maquinaria_labor` FOREIGN KEY (`id_labor`) REFERENCES `labores` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `chk_labor_maquinaria_costo` CHECK ((`costo` >= 0))
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Registro de maquinarias utilizadas en cada labor';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `labor_maquinaria`
--

LOCK TABLES `labor_maquinaria` WRITE;
/*!40000 ALTER TABLE `labor_maquinaria` DISABLE KEYS */;
INSERT INTO `labor_maquinaria` VALUES (1,1,'Tractor John Deere 6120R','PROPIA',NULL,150.00,'Tractor propio para siembra','2025-09-17 00:03:51','2025-09-17 00:03:51'),(2,1,'Sembradora de Precisi??n','PROPIA','AgroMaq SRL',300.00,'Alquiler por d??a','2025-09-17 00:03:51','2025-09-17 00:03:51'),(3,2,'Fertilizadora Amazone','PROPIA',NULL,120.00,'Aplicaci??n de fertilizante NPK','2025-09-17 00:03:51','2025-09-17 00:03:51'),(4,1,'Tractor John Deere 6120','PROPIA',NULL,450.00,'Tractor propio para fertilizaci├│n','2025-09-17 15:01:00','2025-09-17 15:01:00'),(5,1,'Fertilizadora Amazone','PROPIA','AgroServicios SRL',120.00,'Fertilizadora alquilada por 4 horas','2025-09-17 15:01:00','2025-09-17 15:01:00'),(6,2,'Sembradora de Precisi├│n','PROPIA',NULL,200.00,'Sembradora propia para siembra directa','2025-09-17 15:01:00','2025-09-17 15:01:00'),(7,2,'Tractor Massey Ferguson','PROPIA',NULL,300.00,'Tractor para arrastrar sembradora','2025-09-17 15:01:00','2025-09-17 15:01:00'),(8,10,'Tractor Principal','PROPIA',NULL,30000.00,NULL,'2025-09-18 03:06:25','2025-09-18 03:06:25'),(9,10,'pala mec (kcho srl)','PROPIA',NULL,200000.00,NULL,'2025-09-18 03:06:25','2025-09-18 03:06:25'),(10,13,'Tractor Principal','PROPIA',NULL,60000.00,NULL,'2025-09-20 02:59:08','2025-09-20 02:59:08'),(11,13,'tractor de kcho (kcho srl)','PROPIA',NULL,250000.00,NULL,'2025-09-20 02:59:08','2025-09-20 02:59:08'),(12,17,'Tractor Principal - tractor','PROPIA',NULL,30000.00,'2 horas × $15000/h','2025-10-01 00:33:22','2025-10-01 00:33:22'),(13,17,'tractor','ALQUILADA','vecino',100000.00,NULL,'2025-10-01 00:33:22','2025-10-01 00:33:22'),(14,18,'Tractor Principal','PROPIA',NULL,30000.00,NULL,'2025-10-01 01:33:10','2025-10-01 01:33:10'),(15,31,'Tractor Principal','PROPIA',NULL,30000.00,NULL,'2025-10-11 00:49:28','2025-10-11 00:49:28');
/*!40000 ALTER TABLE `labor_maquinaria` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `labores`
--

DROP TABLE IF EXISTS `labores`;
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
  `costo_total` decimal(10,2) DEFAULT NULL,
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
) ENGINE=InnoDB AUTO_INCREMENT=32 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Tabla de labores agr├¡colas con sistema de anulaci├│n y auditor├¡a completo. Incluye gesti├│n de estados y trazabilidad de cambios.';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `labores`
--

LOCK TABLES `labores` WRITE;
/*!40000 ALTER TABLE `labores` DISABLE KEYS */;
INSERT INTO `labores` VALUES (1,1,'SIEMBRA','Siembra de soja con sembradora','2025-01-15','2025-01-16','COMPLETADA',2500.00,'Siembra exitosa, buena humedad',1,'2025-09-12 03:20:09','2025-09-12 03:20:09',1,NULL,NULL,NULL,NULL),(2,2,'FERTILIZACION','Aplicaci??n de fertilizante NPK','2025-01-20','2025-01-20','COMPLETADA',1800.00,'Fertilizaci??n en V4',1,'2025-09-12 03:20:09','2025-09-12 03:20:09',1,NULL,NULL,NULL,NULL),(5,1,'SIEMBRA','Siembra de soja con sembradora','2025-01-15','2025-01-16','COMPLETADA',2500.00,'Siembra exitosa, buena humedad',1,'2025-09-12 03:20:42','2025-09-12 03:20:42',1,NULL,NULL,NULL,NULL),(6,2,'FERTILIZACION','Aplicaci??n de fertilizante NPK','2025-01-20','2025-01-20','COMPLETADA',1800.00,'Fertilizaci??n en V4',1,'2025-09-12 03:20:42','2025-09-12 03:20:42',1,NULL,NULL,NULL,NULL),(9,33,'CONTROL_MALEZAS','mocmiomqoxq|','2025-09-11',NULL,'COMPLETADA',1166000.00,NULL,2,'2025-09-17 23:48:49','2025-09-18 03:16:15',0,NULL,NULL,NULL,NULL),(10,33,'SIEMBRA','lalala','2025-09-17',NULL,'EN_PROGRESO',1030000.00,NULL,2,'2025-09-18 03:06:25','2025-09-20 02:23:00',0,NULL,NULL,NULL,NULL),(13,33,'SIEMBRA','probando probando','2025-06-19',NULL,'COMPLETADA',3530000.00,NULL,2,'2025-09-20 02:59:08','2025-09-20 00:17:44',1,'lucho',NULL,NULL,NULL),(17,34,'SIEMBRA','Siembra de Soja en lote lote prueba CP','2025-05-30','2025-05-30','COMPLETADA',237200.00,'prueba de siembra y cosecha',2,'2025-10-01 00:33:22','2025-10-01 00:33:22',1,'admin.empresa@agrocloud.com',NULL,NULL,NULL),(18,34,'RIEGO','','2025-09-30',NULL,'COMPLETADA',30275.00,NULL,2,'2025-10-01 01:33:10','2025-10-01 01:33:10',1,'luis',NULL,NULL,NULL),(21,34,'COSECHA','Cosecha de Soja en lote lote prueba CP','2025-10-01','2025-10-01','COMPLETADA',0.01,'Cantidad cosechada: 183 kg/ha | Estado suelo: BUENO | buena cosecha de prueba',2,'2025-10-01 06:36:32','2025-10-01 06:36:32',1,'admin.empresa@agrocloud.com',NULL,NULL,NULL),(22,34,'MANTENIMIENTO','','2025-10-01',NULL,'COMPLETADA',0.00,NULL,2,'2025-10-01 17:19:05','2025-10-02 00:22:58',1,'luis',NULL,NULL,NULL),(23,34,'MANTENIMIENTO','','2025-10-01',NULL,'COMPLETADA',0.00,NULL,2,'2025-10-02 00:24:14','2025-10-02 00:24:14',1,'lucho',NULL,NULL,NULL),(24,34,'SIEMBRA','Siembra de Alfalfa en lote lote prueba CP','2025-08-21','2025-08-21','COMPLETADA',0.00,'',2,'2025-10-03 06:03:57','2025-10-03 06:03:57',1,'admin.empresa@agrocloud.com',NULL,NULL,NULL),(25,33,'SIEMBRA','Siembra de Alfalfa en lote alfa1','2025-08-20','2025-08-20','COMPLETADA',0.00,'probando la cosecha nueva de alfa',2,'2025-10-03 06:18:41','2025-10-03 06:18:41',1,'admin.empresa@agrocloud.com',NULL,NULL,NULL),(26,33,'MANTENIMIENTO','','2025-10-03',NULL,'COMPLETADA',0.00,NULL,2,'2025-10-03 06:43:17','2025-10-03 06:43:17',1,'el paco',NULL,NULL,NULL),(27,34,'RIEGO','','2025-10-03',NULL,'COMPLETADA',0.00,NULL,2,'2025-10-03 06:45:35','2025-10-03 06:45:35',1,'leopoldo',NULL,NULL,NULL),(28,34,'COSECHA','Cosecha de Alfalfa en lote lote prueba CP','2025-10-03','2025-10-03','COMPLETADA',0.01,'Cantidad cosechada: 110 tn/ha | Estado suelo: BUENO | prueba de cosecha y acopio',2,'2025-10-03 06:46:40','2025-10-03 06:46:40',1,'admin.empresa@agrocloud.com',NULL,NULL,NULL),(29,33,'COSECHA','Cosecha de Alfalfa en lote alfa1','2025-10-07','2025-10-07','COMPLETADA',0.01,'Cantidad cosechada: 1 tn | Estado suelo: BUENO | CONVERSIÓN A FORRAJE | Cosecha anticipada para alimentación animal | necesidad de forraje',2,'2025-10-07 03:03:09','2025-10-07 03:03:09',1,'admin.empresa@agrocloud.com',NULL,NULL,NULL),(30,2,'MANTENIMIENTO','anionaocac','2025-10-06',NULL,'EN_PROGRESO',0.00,NULL,2,'2025-10-07 03:06:03','2025-10-07 03:06:03',1,'lalo',NULL,NULL,NULL),(31,2,'MANTENIMIENTO','','2025-10-10',NULL,'COMPLETADA',338500.00,NULL,9,'2025-10-11 00:49:28','2025-10-11 00:49:28',1,'samy',NULL,NULL,NULL);
/*!40000 ALTER TABLE `labores` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `logs_acceso`
--

DROP TABLE IF EXISTS `logs_acceso`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `logs_acceso` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `usuario_id` bigint NOT NULL,
  `accion` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `ip_address` varchar(45) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `user_agent` text COLLATE utf8mb4_unicode_ci,
  `fecha_acceso` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `usuario_id` (`usuario_id`),
  CONSTRAINT `logs_acceso_ibfk_1` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `logs_acceso`
--

LOCK TABLES `logs_acceso` WRITE;
/*!40000 ALTER TABLE `logs_acceso` DISABLE KEYS */;
/*!40000 ALTER TABLE `logs_acceso` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `lotes`
--

DROP TABLE IF EXISTS `lotes`;
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

--
-- Dumping data for table `lotes`
--

LOCK TABLES `lotes` WRITE;
/*!40000 ALTER TABLE `lotes` DISABLE KEYS */;
INSERT INTO `lotes` VALUES (1,'Lote Norte',1,50.00,'Soja','DISPONIBLE',NULL,NULL,'Lote norte del campo principal','2025-09-12 02:48:59','2025-09-19 22:38:43',2,0,'Franco',NULL,NULL,NULL,NULL,NULL),(2,'Lote Sur',1,50.00,'Maíz','EN_PREPARACION',NULL,NULL,'Lote sur del campo principal','2025-09-12 02:48:59','2025-10-11 00:49:28',2,1,'Franco','2025-10-11 00:49:28','Cambio automático por labor: MANTENIMIENTO',NULL,NULL,NULL),(9,'Lote B1',2,50.00,'Soja','DISPONIBLE',NULL,NULL,'Lote B1 - Soja de segunda','2025-09-12 03:20:09','2025-09-19 22:38:43',2,1,'Franco',NULL,NULL,NULL,NULL,NULL),(10,'Lote B2',2,50.00,'Maíz','DISPONIBLE',NULL,NULL,'Lote B2 - Ma??z tard??o','2025-09-12 03:20:09','2025-09-15 23:46:55',2,1,'Franco',NULL,NULL,NULL,NULL,NULL),(13,'Lote C1',3,60.00,'Trigo','DISPONIBLE',NULL,NULL,'Lote C1 - Trigo de primera','2025-09-12 03:20:09','2025-09-19 22:38:43',2,1,'Franco',NULL,NULL,NULL,NULL,NULL),(14,'Lote C2',3,60.00,'Cebada','DISPONIBLE',NULL,NULL,'Lote C2 - Cebada cervecera','2025-09-12 03:20:09','2025-09-15 23:38:25',2,1,'Franco',NULL,NULL,NULL,NULL,NULL),(16,'Lote D1',4,40.00,'Soja','DISPONIBLE',NULL,NULL,'Lote D1 - Soja RR','2025-09-12 03:20:09','2025-09-19 22:38:43',2,1,'Franco',NULL,NULL,NULL,NULL,NULL),(17,'Lote D2',4,40.00,'Maíz','DISPONIBLE',NULL,NULL,'Lote D2 - Ma??z BT','2025-09-12 03:20:09','2025-09-15 23:46:55',2,1,'Franco',NULL,NULL,NULL,NULL,NULL),(33,'alfa1',8,28.44,'Alfalfa','COSECHADO','2025-08-20','2025-12-18','test de alfa','2025-09-17 02:27:38','2025-10-07 03:03:10',2,1,'Franco Limoso','2025-10-03 06:43:17','Cambio automático por labor: MANTENIMIENTO','2025-10-07',NULL,0.04),(34,'lote prueba CP',1,50.00,'Alfalfa','COSECHADO','2025-08-21','2025-12-19','lote de prueba del campo principal','2025-09-30 23:51:58','2025-10-03 06:46:40',2,1,'Franco Limoso','2025-10-03 06:45:35','Cambio automático por labor: RIEGO','2025-10-03',NULL,2.20),(35,'lote de lucho',1,50.00,NULL,'DISPONIBLE',NULL,NULL,'','2025-10-10 01:55:03','2025-10-10 01:55:12',6,0,'Arcilloso',NULL,NULL,NULL,NULL,NULL);
/*!40000 ALTER TABLE `lotes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `maquinaria`
--

DROP TABLE IF EXISTS `maquinaria`;
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

--
-- Dumping data for table `maquinaria`
--

LOCK TABLES `maquinaria` WRITE;
/*!40000 ALTER TABLE `maquinaria` DISABLE KEYS */;
INSERT INTO `maquinaria` VALUES (1,'Tractor Principal','John Deere','6120R','ACTIVA','Tractor de 120 HP','2025-09-12 02:48:59','2025-09-16 13:42:42',2,1,1,2020,NULL,2500.00,15000.00,5000,0.00,8.50,'km/L',1200.00,45000000.00,'2025-09-11','tractor'),(2,'Sembradora','Máquinas Agrícolas','MA-2000','ACTIVA','Sembradora de 24 surcos','2025-09-12 02:48:59','2025-09-16 13:42:42',5,1,1,2019,NULL,800.00,8000.00,5000,0.00,12.00,'km/L',1200.00,18000000.00,'2025-09-11','sembradora'),(3,'Pulverizadora','Metalfor','Pulverizadora 2000','ACTIVA','Pulverizadora de 2000 litros','2025-09-12 02:48:59','2025-09-16 13:42:42',5,1,1,2021,NULL,1200.00,12000.00,5000,0.00,6.00,'km/L',1200.00,25000000.00,'2025-09-11','pulverizadora'),(4,'Cosechadora','Case IH','Axial Flow 2388','ACTIVA','Cosechadora de 300 HP','2025-09-12 02:48:59','2025-09-16 13:42:42',2,1,1,2018,NULL,1800.00,25000.00,5000,0.00,4.50,'km/L',1200.00,80000000.00,'2025-09-11','cosechadora'),(15,'Rastra Lijera 4 Mts Ancho 32 Discos 23 Dentados Nueva','Genérica','MA-2000','ACTIVA','descripcion','2025-09-16 16:17:57','2025-09-16 13:42:42',2,1,1,2020,'MA2000-001',500.00,3000.00,4999,1.00,15.00,'km/L',1200.00,8000000.00,'2025-09-16','rastra'),(16,'Elevador Todo Terreno 4x4, 2 Tn Desplazador','Hanomag','E100/4','ACTIVA','','2025-09-16 16:47:27','2025-09-16 16:47:27',2,1,1,2024,'klmlkmwkjcq10212',1.00,28800000.00,5000,1.00,40.00,'km/L',1300.00,0.00,'2025-09-16','tractor');
/*!40000 ALTER TABLE `maquinaria` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `movimientos_inventario`
--

DROP TABLE IF EXISTS `movimientos_inventario`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `movimientos_inventario` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `insumo_id` bigint NOT NULL,
  `labor_id` bigint DEFAULT NULL,
  `tipo_movimiento` enum('ENTRADA','SALIDA','AJUSTE') COLLATE utf8mb4_unicode_ci NOT NULL,
  `cantidad` decimal(10,2) NOT NULL,
  `motivo` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `fecha_movimiento` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `usuario_id` bigint DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `usuario_id` (`usuario_id`),
  KEY `idx_movimientos_insumo` (`insumo_id`),
  KEY `idx_movimientos_labor` (`labor_id`),
  KEY `idx_movimientos_fecha` (`fecha_movimiento`),
  KEY `idx_movimientos_tipo` (`tipo_movimiento`),
  CONSTRAINT `movimientos_inventario_ibfk_1` FOREIGN KEY (`insumo_id`) REFERENCES `insumos` (`id`) ON DELETE CASCADE,
  CONSTRAINT `movimientos_inventario_ibfk_2` FOREIGN KEY (`labor_id`) REFERENCES `labores` (`id`) ON DELETE SET NULL,
  CONSTRAINT `movimientos_inventario_ibfk_3` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Tabla que registra todos los movimientos de inventario de insumos para auditor├¡a';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `movimientos_inventario`
--

LOCK TABLES `movimientos_inventario` WRITE;
/*!40000 ALTER TABLE `movimientos_inventario` DISABLE KEYS */;
INSERT INTO `movimientos_inventario` VALUES (4,4,17,'SALIDA',60.00,'Uso en labor: Siembra de Soja en lote lote prueba CP','2025-10-01 00:33:22',2,'2025-10-01 00:33:22','2025-10-01 00:33:22');
/*!40000 ALTER TABLE `movimientos_inventario` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `movimientos_inventario_granos`
--

DROP TABLE IF EXISTS `movimientos_inventario_granos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `movimientos_inventario_granos` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `inventario_id` bigint NOT NULL,
  `usuario_id` bigint NOT NULL,
  `tipo_movimiento` varchar(30) NOT NULL,
  `cantidad` decimal(10,2) NOT NULL,
  `referencia_tipo` varchar(30) DEFAULT NULL,
  `referencia_id` bigint DEFAULT NULL,
  `precio_unitario` decimal(10,2) DEFAULT NULL,
  `monto_total` decimal(15,2) DEFAULT NULL,
  `cliente_destino` varchar(200) DEFAULT NULL,
  `fecha_movimiento` date NOT NULL,
  `observaciones` text,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `usuario_id` (`usuario_id`),
  KEY `idx_inventario` (`inventario_id`),
  KEY `idx_tipo` (`tipo_movimiento`),
  KEY `idx_fecha` (`fecha_movimiento`),
  KEY `idx_referencia` (`referencia_tipo`,`referencia_id`),
  CONSTRAINT `movimientos_inventario_granos_ibfk_1` FOREIGN KEY (`inventario_id`) REFERENCES `inventario_granos` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `movimientos_inventario_granos_ibfk_2` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `movimientos_inventario_granos`
--

LOCK TABLES `movimientos_inventario_granos` WRITE;
/*!40000 ALTER TABLE `movimientos_inventario_granos` DISABLE KEYS */;
INSERT INTO `movimientos_inventario_granos` VALUES (1,1,2,'ENTRADA_COSECHA',110.00,NULL,NULL,NULL,NULL,NULL,'2025-10-03','Entrada automática desde cosecha ID 18','2025-10-03 06:46:40'),(3,1,2,'SALIDA_VENTA',40.00,'INGRESO',4,3900.00,156000.00,'karim beddur','2025-10-06','buena cosecha de prueba','2025-10-06 23:59:16'),(4,2,2,'ENTRADA_COSECHA',1.00,NULL,NULL,NULL,NULL,NULL,'2025-10-07','Entrada automática desde cosecha ID 19','2025-10-07 03:03:10');
/*!40000 ALTER TABLE `movimientos_inventario_granos` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `permisos`
--

DROP TABLE IF EXISTS `permisos`;
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
) ENGINE=InnoDB AUTO_INCREMENT=34 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `permisos`
--

LOCK TABLES `permisos` WRITE;
/*!40000 ALTER TABLE `permisos` DISABLE KEYS */;
INSERT INTO `permisos` VALUES (1,'GESTION_USUARIOS','Gestionar usuarios del sistema','ADMINISTRACION','2025-09-12 02:48:59','READ',1,'2025-09-13 13:40:09'),(2,'GESTION_EMPRESAS','Gestionar empresas','ADMINISTRACION','2025-09-12 02:48:59','READ',1,'2025-09-13 13:40:09'),(3,'GESTION_CAMPOS','Gestionar campos','CAMPO','2025-09-12 02:48:59','READ',1,'2025-09-13 13:40:09'),(4,'GESTION_LOTES','Gestionar lotes','CAMPO','2025-09-12 02:48:59','READ',1,'2025-09-13 13:40:09'),(5,'GESTION_CULTIVOS','Gestionar cultivos','CULTIVO','2025-09-12 02:48:59','READ',1,'2025-09-13 13:40:09'),(6,'GESTION_INSUMOS','Gestionar insumos','INSUMO','2025-09-12 02:48:59','READ',1,'2025-09-13 13:40:09'),(7,'GESTION_MAQUINARIA','Gestionar maquinaria','MAQUINARIA','2025-09-12 02:48:59','READ',1,'2025-09-13 13:40:09'),(8,'GESTION_LABORES','Gestionar labores','LABOR','2025-09-12 02:48:59','READ',1,'2025-09-13 13:40:09'),(9,'GESTION_COSECHAS','Gestionar cosechas','COSECHA','2025-09-12 02:48:59','READ',1,'2025-09-13 13:40:09'),(10,'GESTION_GASTOS','Gestionar gastos','FINANZAS','2025-09-12 02:48:59','READ',1,'2025-09-13 13:40:09'),(11,'GESTION_INGRESOS','Gestionar ingresos','FINANZAS','2025-09-12 02:48:59','READ',1,'2025-09-13 13:40:09'),(12,'VER_REPORTES','Ver reportes','REPORTES','2025-09-12 02:48:59','READ',1,'2025-09-13 13:40:09'),(13,'EXPORTAR_DATOS','Exportar datos','REPORTES','2025-09-12 02:48:59','READ',1,'2025-09-13 13:40:09'),(14,'READ_USERS','Leer usuarios','USUARIOS','2025-09-13 13:23:18','READ',1,'2025-09-13 13:40:09'),(15,'WRITE_USERS','Escribir usuarios','USUARIOS','2025-09-13 13:23:18','WRITE',1,'2025-09-13 13:40:09'),(16,'DELETE_USERS','Eliminar usuarios','USUARIOS','2025-09-13 13:23:18','DELETE',1,'2025-09-13 13:40:09'),(17,'READ_EMPRESAS','Leer empresas','EMPRESAS','2025-09-13 13:23:18','READ',1,'2025-09-13 13:40:09'),(18,'WRITE_EMPRESAS','Escribir empresas','EMPRESAS','2025-09-13 13:23:18','WRITE',1,'2025-09-13 13:40:09'),(19,'DELETE_EMPRESAS','Eliminar empresas','EMPRESAS','2025-09-13 13:23:18','DELETE',1,'2025-09-13 13:40:09'),(20,'READ_ROLES','Leer roles','ROLES','2025-09-13 13:23:18','READ',1,'2025-09-13 13:40:09'),(21,'WRITE_ROLES','Escribir roles','ROLES','2025-09-13 13:23:18','WRITE',1,'2025-09-13 13:40:09'),(22,'DELETE_ROLES','Eliminar roles','ROLES','2025-09-13 13:23:18','DELETE',1,'2025-09-13 13:40:09'),(23,'ADMIN_ACCESS','Acceso de administración','ADMIN','2025-09-13 13:23:18','ACCESS',1,'2025-09-13 13:40:09');
/*!40000 ALTER TABLE `permisos` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `roles`
--

DROP TABLE IF EXISTS `roles`;
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
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `roles`
--

LOCK TABLES `roles` WRITE;
/*!40000 ALTER TABLE `roles` DISABLE KEYS */;
INSERT INTO `roles` VALUES (1,'SUPERADMIN','Super Administrador',1,'2025-09-12 02:46:55'),(2,'ADMINISTRADOR','Administrador de Empresa',1,'2025-09-12 02:46:55'),(3,'PRODUCTOR','Productor (DEPRECADO - usar JEFE_CAMPO)',0,'2025-09-12 02:46:55'),(4,'TECNICO','Técnico (DEPRECADO - usar JEFE_CAMPO)',0,'2025-09-12 02:46:55'),(9,'ASESOR','Asesor/Ingeniero Agrónomo (DEPRECADO - usar JEFE_CAMPO)',0,'2025-09-12 03:00:44'),(10,'INVITADO','Usuario invitado con acceso limitado',1,'2025-09-12 03:00:44'),(11,'OPERARIO','Operario',1,'2025-09-12 03:00:44'),(12,'USUARIO_REGISTRADO','Usuario común que puede loguearse y acceder a empresas',0,'2025-09-12 06:53:28'),(13,'ADMIN','Administrador del sistema',0,'2025-09-12 22:00:46'),(14,'CONTADOR','Contador (DEPRECADO - usar JEFE_FINANCIERO)',0,'2025-10-07 21:53:03'),(15,'LECTURA','Solo Lectura (DEPRECADO - usar CONSULTOR_EXTERNO)',0,'2025-10-07 21:53:03'),(16,'JEFE_CAMPO','Jefe de Campo',1,'2025-10-08 22:48:09'),(17,'JEFE_FINANCIERO','Jefe Financiero',1,'2025-10-08 22:48:09'),(18,'CONSULTOR_EXTERNO','Consultor Externo',1,'2025-10-08 22:48:09');
/*!40000 ALTER TABLE `roles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `roles_permisos`
--

DROP TABLE IF EXISTS `roles_permisos`;
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
) ENGINE=InnoDB AUTO_INCREMENT=80 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `roles_permisos`
--

LOCK TABLES `roles_permisos` WRITE;
/*!40000 ALTER TABLE `roles_permisos` DISABLE KEYS */;
INSERT INTO `roles_permisos` VALUES (1,1,1,1,'2025-09-13 13:25:04','2025-09-13 13:25:04'),(2,1,2,1,'2025-09-13 13:25:04','2025-09-13 13:25:04'),(3,1,3,1,'2025-09-13 13:25:04','2025-09-13 13:25:04'),(4,2,3,1,'2025-09-13 13:25:04','2025-09-13 13:25:04'),(5,3,3,1,'2025-09-13 13:25:04','2025-09-13 13:25:04'),(6,4,3,1,'2025-09-13 13:25:04','2025-09-13 13:25:04'),(7,9,3,1,'2025-09-13 13:25:04','2025-09-13 13:25:04'),(8,10,3,1,'2025-09-13 13:25:04','2025-09-13 13:25:04'),(9,11,3,1,'2025-09-13 13:25:04','2025-09-13 13:25:04'),(10,1,4,1,'2025-09-13 13:25:04','2025-09-13 13:25:04'),(11,2,4,1,'2025-09-13 13:25:04','2025-09-13 13:25:04'),(12,3,4,1,'2025-09-13 13:25:04','2025-09-13 13:25:04'),(13,4,4,1,'2025-09-13 13:25:04','2025-09-13 13:25:04'),(14,9,4,1,'2025-09-13 13:25:04','2025-09-13 13:25:04'),(15,10,4,1,'2025-09-13 13:25:04','2025-09-13 13:25:04'),(16,11,4,1,'2025-09-13 13:25:04','2025-09-13 13:25:04'),(17,1,5,1,'2025-09-13 13:25:04','2025-09-13 13:25:04'),(18,2,5,1,'2025-09-13 13:25:04','2025-09-13 13:25:04'),(19,3,5,1,'2025-09-13 13:25:04','2025-09-13 13:25:04'),(20,4,5,1,'2025-09-13 13:25:04','2025-09-13 13:25:04'),(21,9,5,1,'2025-09-13 13:25:04','2025-09-13 13:25:04'),(22,11,5,1,'2025-09-13 13:25:04','2025-09-13 13:25:04'),(23,1,6,1,'2025-09-13 13:25:04','2025-09-13 13:25:04'),(24,2,6,1,'2025-09-13 13:25:04','2025-09-13 13:25:04'),(25,3,6,1,'2025-09-13 13:25:04','2025-09-13 13:25:04'),(26,4,6,1,'2025-09-13 13:25:04','2025-09-13 13:25:04'),(27,9,6,1,'2025-09-13 13:25:04','2025-09-13 13:25:04'),(28,11,6,1,'2025-09-13 13:25:04','2025-09-13 13:25:04'),(29,1,7,1,'2025-09-13 13:25:04','2025-09-13 13:25:04'),(30,2,7,1,'2025-09-13 13:25:04','2025-09-13 13:25:04'),(31,4,7,1,'2025-09-13 13:25:04','2025-09-13 13:25:04'),(32,9,7,1,'2025-09-13 13:25:04','2025-09-13 13:25:04'),(33,11,7,1,'2025-09-13 13:25:04','2025-09-13 13:25:04'),(34,1,8,1,'2025-09-13 13:25:04','2025-09-13 13:25:04'),(35,2,8,1,'2025-09-13 13:25:04','2025-09-13 13:25:04'),(36,3,8,1,'2025-09-13 13:25:04','2025-09-13 13:25:04'),(37,4,8,1,'2025-09-13 13:25:04','2025-09-13 13:25:04'),(38,9,8,1,'2025-09-13 13:25:04','2025-09-13 13:25:04'),(39,11,8,1,'2025-09-13 13:25:04','2025-09-13 13:25:04'),(40,1,9,1,'2025-09-13 13:25:04','2025-09-13 13:25:04'),(41,2,9,1,'2025-09-13 13:25:04','2025-09-13 13:25:04'),(42,3,9,1,'2025-09-13 13:25:04','2025-09-13 13:25:04'),(43,4,9,1,'2025-09-13 13:25:04','2025-09-13 13:25:04'),(44,9,9,1,'2025-09-13 13:25:04','2025-09-13 13:25:04'),(45,11,9,1,'2025-09-13 13:25:04','2025-09-13 13:25:04'),(46,1,10,1,'2025-09-13 13:25:04','2025-09-13 13:25:04'),(47,2,10,1,'2025-09-13 13:25:04','2025-09-13 13:25:04'),(48,1,11,1,'2025-09-13 13:25:04','2025-09-13 13:25:04'),(49,2,11,1,'2025-09-13 13:25:04','2025-09-13 13:25:04'),(50,1,12,1,'2025-09-13 13:25:04','2025-09-13 13:25:04'),(51,2,12,1,'2025-09-13 13:25:04','2025-09-13 13:25:04'),(52,3,12,1,'2025-09-13 13:25:04','2025-09-13 13:25:04'),(53,4,12,1,'2025-09-13 13:25:04','2025-09-13 13:25:04'),(54,9,12,1,'2025-09-13 13:25:04','2025-09-13 13:25:04'),(55,10,12,1,'2025-09-13 13:25:04','2025-09-13 13:25:04'),(56,11,12,1,'2025-09-13 13:25:04','2025-09-13 13:25:04'),(57,1,13,1,'2025-09-13 13:25:04','2025-09-13 13:25:04'),(64,1,14,1,'2025-09-13 13:25:04','2025-09-13 13:25:04'),(65,1,15,1,'2025-09-13 13:25:04','2025-09-13 13:25:04'),(66,1,16,1,'2025-09-13 13:25:04','2025-09-13 13:25:04'),(67,1,17,1,'2025-09-13 13:25:04','2025-09-13 13:25:04'),(68,1,18,1,'2025-09-13 13:25:04','2025-09-13 13:25:04'),(69,1,19,1,'2025-09-13 13:25:04','2025-09-13 13:25:04'),(70,1,20,1,'2025-09-13 13:25:04','2025-09-13 13:25:04'),(71,1,21,1,'2025-09-13 13:25:04','2025-09-13 13:25:04'),(72,1,22,1,'2025-09-13 13:25:04','2025-09-13 13:25:04'),(73,1,23,1,'2025-09-13 13:25:04','2025-09-13 13:25:04'),(74,2,14,1,'2025-09-13 13:25:04','2025-09-13 13:25:04'),(75,2,15,1,'2025-09-13 13:25:04','2025-09-13 13:25:04'),(76,2,17,1,'2025-09-13 13:25:04','2025-09-13 13:25:04'),(77,2,18,1,'2025-09-13 13:25:04','2025-09-13 13:25:04'),(78,2,20,1,'2025-09-13 13:25:04','2025-09-13 13:25:04'),(79,2,23,1,'2025-09-13 13:25:04','2025-09-13 13:25:04');
/*!40000 ALTER TABLE `roles_permisos` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `usuario_empresas`
--

DROP TABLE IF EXISTS `usuario_empresas`;
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
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usuario_empresas`
--

LOCK TABLES `usuario_empresas` WRITE;
/*!40000 ALTER TABLE `usuario_empresas` DISABLE KEYS */;
INSERT INTO `usuario_empresas` VALUES (1,1,1,'SUPERADMIN','ACTIVO','2025-09-12 02:46:55',NULL,'2025-09-12 22:18:12',NULL,NULL),(2,2,1,'ADMINISTRADOR','ACTIVO','2025-09-12 02:46:55',NULL,'2025-09-12 22:18:12',NULL,NULL),(5,5,1,'ADMINISTRADOR','ACTIVO','2025-09-12 03:20:05',NULL,'2025-09-12 22:18:12',NULL,NULL),(6,7,1,'ASESOR','ACTIVO','2025-09-12 03:20:05',NULL,'2025-09-12 22:18:12',NULL,NULL),(7,10,1,'LECTURA','ACTIVO','2025-09-12 03:20:05',NULL,'2025-09-13 13:50:02',NULL,NULL),(8,9,1,'OPERARIO','ACTIVO','2025-09-12 03:20:05',NULL,'2025-09-12 22:18:12',NULL,NULL),(9,8,1,'PRODUCTOR','ACTIVO','2025-09-12 03:20:05',NULL,'2025-09-12 22:18:12',NULL,NULL),(10,6,1,'TECNICO','ACTIVO','2025-09-12 03:20:05',NULL,'2025-09-12 22:18:12',NULL,NULL),(12,13,5,'ADMINISTRADOR','ACTIVO','2025-09-25 06:37:03',1,'2025-09-25 06:37:03',NULL,'2025-09-25 03:00:00'),(13,14,6,'ADMINISTRADOR','ACTIVO','2025-09-25 07:01:38',1,'2025-09-25 07:01:38',NULL,'2025-09-25 03:00:00'),(14,18,1,'PRODUCTOR','ACTIVO','2025-10-08 02:43:40',1,'2025-10-08 02:43:40',NULL,'2025-10-08 02:43:40');
/*!40000 ALTER TABLE `usuario_empresas` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `usuarios`
--

DROP TABLE IF EXISTS `usuarios`;
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
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usuarios`
--

LOCK TABLES `usuarios` WRITE;
/*!40000 ALTER TABLE `usuarios` DISABLE KEYS */;
INSERT INTO `usuarios` VALUES (1,'admin','admin@agrocloud.com','$2a$10$tfobtlu6.o5QdOum9zzHPO21o9ui713jQiBE5d/7QmAkOJZS91be2','Admin','Sistema',1,'ACTIVO','2025-09-12 02:46:55','2025-10-06 23:23:19',NULL,NULL,1,NULL,NULL,NULL,NULL),(2,'admin_empresa','admin.empresa@agrocloud.com','$2a$10$tfobtlu6.o5QdOum9zzHPO21o9ui713jQiBE5d/7QmAkOJZS91be2','Admin','Empresa',1,'ACTIVO','2025-09-12 02:46:55','2025-10-06 23:23:19',NULL,NULL,0,NULL,NULL,NULL,NULL),(5,'admin.campo','admin.campo@agrocloud.com','$2a$10$tfobtlu6.o5QdOum9zzHPO21o9ui713jQiBE5d/7QmAkOJZS91be2','Carlos','Administrador',1,'ACTIVO','2025-09-12 03:20:05','2025-10-06 23:23:19',2,NULL,0,NULL,NULL,NULL,NULL),(6,'tecnico.juan','tecnico.juan@agrocloud.com','$2a$10$tfobtlu6.o5QdOum9zzHPO21o9ui713jQiBE5d/7QmAkOJZS91be2','Juan','T??cnico',1,'ACTIVO','2025-09-12 03:20:05','2025-10-06 23:23:19',5,NULL,0,NULL,NULL,NULL,NULL),(7,'asesor.maria','asesor.maria@agrocloud.com','$2a$10$tfobtlu6.o5QdOum9zzHPO21o9ui713jQiBE5d/7QmAkOJZS91be2','Mar??a','Asesora',1,'ACTIVO','2025-09-12 03:20:05','2025-10-06 23:23:19',5,NULL,0,NULL,NULL,NULL,NULL),(8,'productor.pedro','productor.pedro@agrocloud.com','$2a$10$tfobtlu6.o5QdOum9zzHPO21o9ui713jQiBE5d/7QmAkOJZS91be2','Pedro','Productor',1,'ACTIVO','2025-09-12 03:20:05','2025-10-06 23:23:19',5,NULL,0,NULL,NULL,NULL,NULL),(9,'operario.luis','operario.luis@agrocloud.com','$2a$10$tfobtlu6.o5QdOum9zzHPO21o9ui713jQiBE5d/7QmAkOJZS91be2','Luis','Operario',1,'ACTIVO','2025-09-12 03:20:05','2025-10-06 23:23:19',6,NULL,0,NULL,NULL,NULL,NULL),(10,'invitado.ana','invitado.ana@agrocloud.com','$2a$10$tfobtlu6.o5QdOum9zzHPO21o9ui713jQiBE5d/7QmAkOJZS91be2','Ana','Invitada',1,'ACTIVO','2025-09-12 03:20:05','2025-10-06 23:23:19',2,NULL,0,NULL,NULL,NULL,NULL),(13,'admin@agrocloud.com','admin_empresa@agrocloud.com','$2a$10$hkEPRfoM8r4DLe1HRNKC5uUb.nrnSKLMXsql.V2AsL9dBydcS1Mxe','pablo','perez',1,'ACTIVO','2025-09-25 06:37:03','2025-09-25 06:37:03',NULL,1,0,NULL,NULL,NULL,NULL),(14,'samy@agrocloud.com','samy@agrocloud.com','$2a$10$dlmxP3cZXcXb4uVq8yjg8uLSL.0QVYnIrcPTGaUWIOHslcN5hL.x.','vir','cespedes',1,'ACTIVO','2025-09-25 07:01:38','2025-09-25 07:01:38',NULL,1,0,NULL,NULL,NULL,NULL),(15,'test.hash@agrocloud.com','test.hash@agrocloud.com','$2a$10$tfobtlu6.o5QdOum9zzHPO21o9ui713jQiBE5d/7QmAkOJZS91be2','Usuario Prueba Hash',NULL,1,'ACTIVO','2025-10-07 02:22:35','2025-10-07 02:22:35',NULL,NULL,0,NULL,NULL,NULL,NULL),(16,'test.final@agrocloud.com','test.final@agrocloud.com','$2a$10$ipspYSFJjju04n8Q7tMHR.U4/dAue9vbofpJRfqczTte1utXwFlpy','Usuario Prueba Final',NULL,1,'ACTIVO','2025-10-07 02:26:23','2025-10-07 02:26:23',NULL,NULL,0,NULL,NULL,NULL,NULL),(17,'leopoldo','leo@agrocloud.com','$2a$10$8OKD5ahGdbjLCozXhmQrg.bZC8ZUCUmXe0U5H0VsJThG8CC2mkJXK','leo','ledesma',1,'ACTIVO','2025-10-08 01:12:30','2025-10-08 02:16:15',1,2,0,NULL,NULL,NULL,'23232332'),(18,'pepecito','pepe@agrocloud.com','$2a$10$mkFB31Qj5./n.5SqtRSJruifubV.vwDCH9EeqCidEgydlWEkzQ97.','pepe','lopez',1,'ACTIVO','2025-10-08 03:12:09','2025-10-08 05:33:02',2,2,0,NULL,NULL,NULL,'12333333'),(19,'raulito','raul@agrocloud.com','$2a$10$OF8WVTnkAOdnoxZHSRH2a.2v5wHY88oyEsfKkewot9fcHj1Kuoix.','raul','rodriguez',1,'ACTIVO','2025-10-08 05:21:45','2025-10-08 05:22:08',2,2,0,NULL,NULL,NULL,'12121212');
/*!40000 ALTER TABLE `usuarios` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `usuarios_empresas_roles`
--

DROP TABLE IF EXISTS `usuarios_empresas_roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuarios_empresas_roles` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `usuario_id` bigint NOT NULL,
  `empresa_id` bigint NOT NULL,
  `rol_id` bigint NOT NULL,
  `estado` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'ACTIVO',
  `fecha_inicio` date DEFAULT NULL,
  `fecha_fin` date DEFAULT NULL,
  `fecha_creacion` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `fecha_actualizacion` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `activo` tinyint(1) DEFAULT '1',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_usuario_empresa_rol` (`usuario_id`,`empresa_id`,`rol_id`),
  KEY `empresa_id` (`empresa_id`),
  KEY `rol_id` (`rol_id`),
  CONSTRAINT `usuarios_empresas_roles_ibfk_1` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`) ON DELETE CASCADE,
  CONSTRAINT `usuarios_empresas_roles_ibfk_2` FOREIGN KEY (`empresa_id`) REFERENCES `empresas` (`id`) ON DELETE CASCADE,
  CONSTRAINT `usuarios_empresas_roles_ibfk_3` FOREIGN KEY (`rol_id`) REFERENCES `roles` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usuarios_empresas_roles`
--

LOCK TABLES `usuarios_empresas_roles` WRITE;
/*!40000 ALTER TABLE `usuarios_empresas_roles` DISABLE KEYS */;
INSERT INTO `usuarios_empresas_roles` VALUES (1,2,1,2,'ACTIVO','2025-09-12',NULL,'2025-09-12 03:20:05','2025-09-12 03:20:05',1),(2,5,1,2,'ACTIVO','2025-09-12',NULL,'2025-09-12 03:20:05','2025-09-12 03:20:05',1),(3,7,1,16,'ACTIVO','2025-09-12',NULL,'2025-09-12 03:20:05','2025-10-08 22:49:26',1),(4,10,1,10,'ACTIVO','2025-09-12',NULL,'2025-09-12 03:20:05','2025-09-12 03:20:05',1),(5,9,1,11,'ACTIVO','2025-09-12',NULL,'2025-09-12 03:20:05','2025-09-12 03:20:05',1),(6,8,1,16,'ACTIVO','2025-09-12',NULL,'2025-09-12 03:20:05','2025-10-08 22:49:26',1),(7,6,1,16,'ACTIVO','2025-09-12',NULL,'2025-09-12 03:20:05','2025-10-08 22:49:26',1),(8,1,3,1,'ACTIVO','2025-09-13',NULL,'2025-09-13 13:16:25','2025-09-13 13:16:25',1),(9,1,3,2,'ACTIVO','2025-09-13',NULL,'2025-09-13 13:16:25','2025-09-13 13:16:25',1),(10,17,1,16,'ACTIVO',NULL,NULL,'2025-10-08 03:11:30','2025-10-08 22:49:26',1),(11,19,1,17,'ACTIVO',NULL,NULL,'2025-10-08 05:21:45','2025-10-08 22:49:26',1),(12,18,1,16,'ACTIVO',NULL,NULL,'2025-10-08 05:22:01','2025-10-08 22:49:26',1);
/*!40000 ALTER TABLE `usuarios_empresas_roles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `usuarios_roles`
--

DROP TABLE IF EXISTS `usuarios_roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuarios_roles` (
  `usuario_id` bigint NOT NULL,
  `rol_id` bigint NOT NULL,
  PRIMARY KEY (`usuario_id`,`rol_id`),
  KEY `rol_id` (`rol_id`),
  CONSTRAINT `usuarios_roles_ibfk_1` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`) ON DELETE CASCADE,
  CONSTRAINT `usuarios_roles_ibfk_2` FOREIGN KEY (`rol_id`) REFERENCES `roles` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usuarios_roles`
--

LOCK TABLES `usuarios_roles` WRITE;
/*!40000 ALTER TABLE `usuarios_roles` DISABLE KEYS */;
INSERT INTO `usuarios_roles` VALUES (1,1),(2,2),(5,2),(10,10),(9,11),(6,16),(7,16),(8,16);
/*!40000 ALTER TABLE `usuarios_roles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Temporary view structure for view `vista_estadisticas_rendimiento_cultivo`
--

DROP TABLE IF EXISTS `vista_estadisticas_rendimiento_cultivo`;
/*!50001 DROP VIEW IF EXISTS `vista_estadisticas_rendimiento_cultivo`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `vista_estadisticas_rendimiento_cultivo` AS SELECT 
 1 AS `cultivo_id`,
 1 AS `cultivo_nombre`,
 1 AS `total_cosechas`,
 1 AS `rendimiento_promedio`,
 1 AS `rendimiento_minimo`,
 1 AS `rendimiento_maximo`,
 1 AS `cumplimiento_promedio`,
 1 AS `superficie_total_cosechada`,
 1 AS `cantidad_total_cosechada`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `vista_lotes_requieren_descanso`
--

DROP TABLE IF EXISTS `vista_lotes_requieren_descanso`;
/*!50001 DROP VIEW IF EXISTS `vista_lotes_requieren_descanso`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `vista_lotes_requieren_descanso` AS SELECT 
 1 AS `lote_id`,
 1 AS `lote_nombre`,
 1 AS `fecha_cosecha`,
 1 AS `estado_suelo`,
 1 AS `dias_descanso_recomendados`,
 1 AS `dias_desde_cosecha`,
 1 AS `estado_lote`*/;
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `weather_api_usage`
--

DROP TABLE IF EXISTS `weather_api_usage`;
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
) ENGINE=InnoDB AUTO_INCREMENT=38 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `weather_api_usage`
--

LOCK TABLES `weather_api_usage` WRITE;
/*!40000 ALTER TABLE `weather_api_usage` DISABLE KEYS */;
INSERT INTO `weather_api_usage` VALUES (1,'2025-09-25',2,1000,'2025-09-25 22:43:18'),(2,'2025-09-29',6,1000,'2025-09-29 23:56:08'),(7,'2025-09-30',26,1000,'2025-09-30 21:47:07'),(13,'2025-10-01',7,1000,'2025-10-01 23:03:53'),(15,'2025-10-02',7,1000,'2025-10-02 23:01:59'),(18,'2025-10-06',13,1000,'2025-10-06 23:48:21'),(24,'2025-10-07',4,1000,'2025-10-07 22:05:06'),(26,'2025-10-09',20,1000,'2025-10-09 23:12:19'),(32,'2025-10-10',20,1000,'2025-10-10 22:52:39'),(34,'2025-10-14',11,1000,'2025-10-15 00:01:58');
/*!40000 ALTER TABLE `weather_api_usage` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Final view structure for view `vista_estadisticas_rendimiento_cultivo`
--

/*!50001 DROP VIEW IF EXISTS `vista_estadisticas_rendimiento_cultivo`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = cp850 */;
/*!50001 SET character_set_results     = cp850 */;
/*!50001 SET collation_connection      = cp850_general_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `vista_estadisticas_rendimiento_cultivo` AS select `c`.`id` AS `cultivo_id`,`c`.`nombre` AS `cultivo_nombre`,count(`hc`.`id`) AS `total_cosechas`,avg(`hc`.`rendimiento_real`) AS `rendimiento_promedio`,min(`hc`.`rendimiento_real`) AS `rendimiento_minimo`,max(`hc`.`rendimiento_real`) AS `rendimiento_maximo`,avg((case when (`hc`.`rendimiento_esperado` > 0) then ((`hc`.`rendimiento_real` / `hc`.`rendimiento_esperado`) * 100) else 0 end)) AS `cumplimiento_promedio`,sum(`hc`.`superficie_hectareas`) AS `superficie_total_cosechada`,sum(`hc`.`cantidad_cosechada`) AS `cantidad_total_cosechada` from (`cultivos` `c` left join `historial_cosechas` `hc` on((`c`.`id` = `hc`.`cultivo_id`))) group by `c`.`id`,`c`.`nombre` */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `vista_lotes_requieren_descanso`
--

/*!50001 DROP VIEW IF EXISTS `vista_lotes_requieren_descanso`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = cp850 */;
/*!50001 SET character_set_results     = cp850 */;
/*!50001 SET collation_connection      = cp850_general_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `vista_lotes_requieren_descanso` AS select `l`.`id` AS `lote_id`,`l`.`nombre` AS `lote_nombre`,`hc`.`fecha_cosecha` AS `fecha_cosecha`,`hc`.`estado_suelo` AS `estado_suelo`,`hc`.`dias_descanso_recomendados` AS `dias_descanso_recomendados`,(to_days(curdate()) - to_days(`hc`.`fecha_cosecha`)) AS `dias_desde_cosecha`,(case when ((to_days(curdate()) - to_days(`hc`.`fecha_cosecha`)) >= `hc`.`dias_descanso_recomendados`) then 'LISTO_PARA_SIEMBRA' else 'EN_DESCANSO' end) AS `estado_lote` from (`lotes` `l` join `historial_cosechas` `hc` on((`l`.`id` = `hc`.`lote_id`))) where ((`hc`.`requiere_descanso` = true) and (`hc`.`fecha_cosecha` = (select max(`hc2`.`fecha_cosecha`) from `historial_cosechas` `hc2` where (`hc2`.`lote_id` = `l`.`id`)))) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-10-16 20:13:45
