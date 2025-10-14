-- ============================================================
-- SCRIPT DE MIGRACION: Datos de Testing
-- AgroGestion - Version 2.0
-- SOLO para ambiente de Testing
-- ============================================================

SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- ============================================================
-- USUARIOS DE PRUEBA
-- ============================================================

-- Password para todos los usuarios de testing: "password123"
-- Hash bcrypt: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhCu

INSERT INTO `usuarios` (`id`, `username`, `email`, `password`, `first_name`, `last_name`, `phone`, `estado`, `activo`, `email_verified`) VALUES
(1, 'admin.testing', 'admin.testing@agrogestion.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhCu', 'Admin', 'Testing', '1234567890', 'ACTIVO', 1, 1),
(2, 'jefe.campo', 'jefe.campo@agrogestion.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhCu', 'Carlos', 'Campo', '1234567891', 'ACTIVO', 1, 1),
(3, 'jefe.financiero', 'jefe.financiero@agrogestion.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhCu', 'María', 'Finanzas', '1234567892', 'ACTIVO', 1, 1),
(4, 'operario.test', 'operario.test@agrogestion.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhCu', 'Juan', 'Operario', '1234567893', 'ACTIVO', 1, 1),
(5, 'consultor.test', 'consultor.test@agrogestion.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhCu', 'Ana', 'Consultora', '1234567894', 'ACTIVO', 1, 1)
ON DUPLICATE KEY UPDATE
  username = VALUES(username),
  password = VALUES(password),
  first_name = VALUES(first_name),
  last_name = VALUES(last_name);

-- ============================================================
-- EMPRESA DE PRUEBA
-- ============================================================

INSERT INTO `empresas` (`id`, `nombre`, `razon_social`, `cuit`, `direccion`, `ciudad`, `provincia`, 
  `telefono`, `email_contacto`, `estado`, `activo`) VALUES
(1, 'AgroCloud Demo', 'AgroCloud Demo S.A.', '20-12345678-9', 'Av. Principal 1234', 
  'Rosario', 'Santa Fe', '0341-1234567', 'demo@agrocloud.com', 'ACTIVO', 1)
ON DUPLICATE KEY UPDATE
  nombre = VALUES(nombre),
  estado = VALUES(estado);

-- ============================================================
-- ASIGNACIÓN DE ROLES A USUARIOS (Sistema NUEVO)
-- ============================================================

INSERT INTO `usuario_empresas` (`usuario_id`, `empresa_id`, `rol`, `estado`, `fecha_inicio`) VALUES
(1, 1, 'ADMINISTRADOR', 'ACTIVO', CURDATE()),
(2, 1, 'JEFE_CAMPO', 'ACTIVO', CURDATE()),
(3, 1, 'JEFE_FINANCIERO', 'ACTIVO', CURDATE()),
(4, 1, 'OPERARIO', 'ACTIVO', CURDATE()),
(5, 1, 'CONSULTOR_EXTERNO', 'ACTIVO', CURDATE())
ON DUPLICATE KEY UPDATE
  estado = VALUES(estado);

-- ============================================================
-- CAMPOS DE PRUEBA
-- ============================================================

INSERT INTO `campos` (`id`, `nombre`, `ubicacion`, `superficie_total`, `tipo_suelo`, 
  `latitud`, `longitud`, `usuario_id`, `empresa_id`) VALUES
(1, 'Campo Norte', 'Ruta 9 Km 45', 500.00, 'Franco Limoso', -33.1234567, -60.1234567, 1, 1),
(2, 'Campo Sur', 'Ruta 33 Km 120', 350.00, 'Franco Arcilloso', -33.2345678, -60.2345678, 1, 1),
(3, 'Campo Este', 'Ruta 11 Km 230', 400.00, 'Franco Arenoso', -33.3456789, -60.3456789, 1, 1)
ON DUPLICATE KEY UPDATE
  nombre = VALUES(nombre),
  superficie_total = VALUES(superficie_total);

-- ============================================================
-- LOTES DE PRUEBA
-- ============================================================

INSERT INTO `lotes` (`id`, `nombre`, `area_hectareas`, `cultivo_actual`, `tipo_suelo`, 
  `estado`, `campo_id`, `usuario_id`) VALUES
(1, 'Lote 1A', 50.00, NULL, 'Franco Limoso', 'DISPONIBLE', 1, 1),
(2, 'Lote 1B', 75.00, 'Soja', 'Franco Limoso', 'SEMBRADO', 1, 1),
(3, 'Lote 1C', 60.00, 'Maíz', 'Franco Limoso', 'SEMBRADO', 1, 1),
(4, 'Lote 2A', 80.00, NULL, 'Franco Arcilloso', 'DISPONIBLE', 2, 1),
(5, 'Lote 2B', 90.00, 'Trigo', 'Franco Arcilloso', 'SEMBRADO', 2, 1),
(6, 'Lote 3A', 100.00, NULL, 'Franco Arenoso', 'DISPONIBLE', 3, 1)
ON DUPLICATE KEY UPDATE
  nombre = VALUES(nombre),
  area_hectareas = VALUES(area_hectareas);

-- ============================================================
-- INSUMOS DE PRUEBA
-- ============================================================

INSERT INTO `insumos` (`nombre`, `tipo`, `descripcion`, `unidad_medida`, `precio_unitario`, 
  `stock_actual`, `stock_minimo`, `proveedor`, `estado`) VALUES
-- Semillas
('Semilla Soja DM 4712', 'SEMILLA', 'Semilla de soja variedad 4712', 'Kg', 850.00, 2000.00, 500.00, 'Don Mario', 'activo'),
('Semilla Maíz DK 7010', 'SEMILLA', 'Semilla de maíz híbrido 7010', 'Bolsa', 12500.00, 150.00, 50.00, 'Dekalb', 'activo'),
('Semilla Trigo Baguette 31', 'SEMILLA', 'Semilla de trigo Baguette 31', 'Kg', 45.00, 8000.00, 2000.00, 'Nidera', 'activo'),
-- Fertilizantes
('Urea 46%', 'FERTILIZANTE', 'Fertilizante nitrogenado', 'Kg', 420.00, 15000.00, 5000.00, 'YPF Agro', 'activo'),
('Fosfato Diamónico', 'FERTILIZANTE', 'Fertilizante fosforado', 'Kg', 580.00, 10000.00, 3000.00, 'Profertil', 'activo'),
('Sulfato de Amonio', 'FERTILIZANTE', 'Fertilizante azufrado', 'Kg', 350.00, 8000.00, 2000.00, 'Yara', 'activo'),
-- Herbicidas
('Glifosato 66%', 'HERBICIDA', 'Herbicida total', 'Litro', 1250.00, 500.00, 200.00, 'Bayer', 'activo'),
('2,4-D', 'HERBICIDA', 'Herbicida selectivo', 'Litro', 850.00, 300.00, 100.00, 'Atanor', 'activo'),
('Imazetapir', 'HERBICIDA', 'Herbicida pre-emergente', 'Litro', 3200.00, 150.00, 50.00, 'BASF', 'activo'),
-- Insecticidas
('Cipermetrina', 'INSECTICIDA', 'Insecticida piretroide', 'Litro', 1800.00, 200.00, 50.00, 'Syngenta', 'activo'),
('Clorpirifos', 'INSECTICIDA', 'Insecticida organofosforado', 'Litro', 2100.00, 180.00, 50.00, 'Dow', 'activo'),
-- Fungicidas
('Tebuconazole', 'FUNGICIDA', 'Fungicida sistémico', 'Litro', 2800.00, 100.00, 30.00, 'BASF', 'activo'),
('Azoxistrobina', 'FUNGICIDA', 'Fungicida preventivo', 'Litro', 3500.00, 80.00, 20.00, 'Syngenta', 'activo'),
-- Combustibles
('Gasoil', 'COMBUSTIBLE', 'Combustible diesel', 'Litro', 450.00, 5000.00, 1000.00, 'YPF', 'activo'),
('Nafta', 'COMBUSTIBLE', 'Combustible nafta', 'Litro', 520.00, 2000.00, 500.00, 'Shell', 'activo')
ON DUPLICATE KEY UPDATE
  precio_unitario = VALUES(precio_unitario),
  stock_actual = VALUES(stock_actual);

-- ============================================================
-- MAQUINARIA DE PRUEBA
-- ============================================================

INSERT INTO `maquinaria` (`nombre`, `tipo`, `tipo_maquinaria`, `marca`, `modelo`, `anio`, 
  `estado`, `horas_uso`, `combustible_tipo`, `rendimiento_combustible`, `unidad_rendimiento`, 
  `costo_combustible_por_litro`, `valor_actual`) VALUES
('Tractor John Deere 7210R', 'Tractor', 'tractor', 'John Deere', '7210R', 2020, 'ACTIVA', 1250.00, 'Gasoil', 12.50, 'L/hora', 450.00, 8500000.00),
('Cosechadora Case IH 2388', 'Cosechadora', 'cosechadora', 'Case IH', '2388', 2018, 'ACTIVA', 850.00, 'Gasoil', 28.00, 'L/hora', 450.00, 12000000.00),
('Pulverizadora Metalfor 3200', 'Pulverizadora', 'pulverizadora', 'Metalfor', '3200', 2019, 'ACTIVA', 450.00, 'Gasoil', 8.00, 'L/hora', 450.00, 2500000.00),
('Sembradora Agrometal TX Mega', 'Sembradora', 'sembradora', 'Agrometal', 'TX Mega', 2021, 'ACTIVA', 320.00, 'Tractor', 0.00, 'N/A', 0.00, 4200000.00),
('Tractor Massey Ferguson 4275', 'Tractor', 'tractor', 'Massey Ferguson', '4275', 2017, 'ACTIVA', 1850.00, 'Gasoil', 10.00, 'L/hora', 450.00, 5800000.00)
ON DUPLICATE KEY UPDATE
  estado = VALUES(estado),
  horas_uso = VALUES(horas_uso);

-- ============================================================
-- LABORES DE PRUEBA
-- ============================================================

INSERT INTO `labores` (`tipo_labor`, `descripcion`, `fecha_realizacion`, `responsable`, 
  `estado`, `costo_base`, `costo_maquinaria`, `costo_mano_obra`, `costo_total`, `lote_id`) VALUES
('SIEMBRA', 'Siembra de soja', CURDATE() - INTERVAL 60 DAY, 'Carlos Campo', 'COMPLETADA', 
  15000.00, 8000.00, 12000.00, 35000.00, 2),
('FERTILIZACION', 'Aplicación de Urea', CURDATE() - INTERVAL 45 DAY, 'Carlos Campo', 'COMPLETADA', 
  25000.00, 3000.00, 2000.00, 30000.00, 2),
('PULVERIZACION', 'Control de malezas', CURDATE() - INTERVAL 30 DAY, 'Juan Operario', 'COMPLETADA', 
  8000.00, 4000.00, 1500.00, 13500.00, 2),
('SIEMBRA', 'Siembra de maíz', CURDATE() - INTERVAL 50 DAY, 'Carlos Campo', 'COMPLETADA', 
  18000.00, 9000.00, 15000.00, 42000.00, 3),
('FERTILIZACION', 'Fertilización base maíz', CURDATE() - INTERVAL 40 DAY, 'Juan Operario', 'COMPLETADA', 
  22000.00, 2500.00, 1800.00, 26300.00, 3)
ON DUPLICATE KEY UPDATE
  estado = VALUES(estado);

-- ============================================================
-- INFORMACIÓN IMPORTANTE
-- ============================================================

-- Credenciales de acceso para Testing:
-- 
-- Admin: admin.testing@agrogestion.com / password123
-- Jefe Campo: jefe.campo@agrogestion.com / password123
-- Jefe Financiero: jefe.financiero@agrogestion.com / password123
-- Operario: operario.test@agrogestion.com / password123
-- Consultor: consultor.test@agrogestion.com / password123

-- ============================================================
-- FIN DEL SCRIPT DE DATOS DE TESTING
-- ============================================================

