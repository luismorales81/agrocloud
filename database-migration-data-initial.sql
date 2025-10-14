-- ============================================================
-- SCRIPT DE MIGRACION: Datos Iniciales (Roles y Sistema)
-- AgroGestion - Version 2.0
-- Para: Testing y Production
-- ============================================================

SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- ============================================================
-- ROLES DEL SISTEMA
-- ============================================================

INSERT INTO `roles` (`id`, `nombre`, `descripcion`, `activo`) VALUES
(1, 'SUPERADMIN', 'Super Administrador del Sistema', 1),
(2, 'USUARIO_REGISTRADO', 'Usuario Registrado Básico', 1),
(3, 'INVITADO', 'Usuario Invitado con acceso limitado', 1),
(4, 'ADMINISTRADOR', 'Administrador de Empresa', 1),
(5, 'JEFE_CAMPO', 'Jefe de Campo', 1),
(6, 'JEFE_FINANCIERO', 'Jefe Financiero', 1),
(7, 'OPERARIO', 'Operario de Campo', 1),
(8, 'CONSULTOR_EXTERNO', 'Consultor Externo (Solo Lectura)', 1),
-- Roles Legacy (mantener para compatibilidad)
(9, 'PRODUCTOR', 'Productor (Legacy - usar JEFE_CAMPO)', 1),
(10, 'ASESOR', 'Asesor (Legacy - usar JEFE_CAMPO)', 1),
(11, 'TECNICO', 'Técnico (Legacy - usar JEFE_CAMPO)', 1),
(12, 'CONTADOR', 'Contador (Legacy - usar JEFE_FINANCIERO)', 1),
(13, 'LECTURA', 'Solo Lectura (Legacy - usar CONSULTOR_EXTERNO)', 1)
ON DUPLICATE KEY UPDATE
  descripcion = VALUES(descripcion),
  activo = VALUES(activo);

-- ============================================================
-- CULTIVOS BASE DEL SISTEMA
-- ============================================================

INSERT INTO `cultivos` (`nombre`, `nombre_cientifico`, `tipo`, `descripcion`, `ciclo_dias`, 
  `epoca_siembra_inicio`, `epoca_siembra_fin`, `temperatura_optima_min`, `temperatura_optima_max`, 
  `requerimiento_agua`, `activo`) VALUES
-- Granos
('Maíz', 'Zea mays', 'GRANO', 'Cereal de grano grueso', 120, 'Septiembre', 'Diciembre', 15.00, 30.00, 'MEDIO', 1),
('Soja', 'Glycine max', 'GRANO', 'Leguminosa oleaginosa', 130, 'Octubre', 'Diciembre', 20.00, 30.00, 'MEDIO', 1),
('Trigo', 'Triticum aestivum', 'GRANO', 'Cereal de grano fino', 150, 'Mayo', 'Julio', 10.00, 24.00, 'BAJO', 1),
('Girasol', 'Helianthus annuus', 'GRANO', 'Oleaginosa', 110, 'Septiembre', 'Diciembre', 15.00, 25.00, 'MEDIO', 1),
('Sorgo', 'Sorghum bicolor', 'GRANO', 'Cereal resistente a sequía', 100, 'Octubre', 'Enero', 18.00, 35.00, 'BAJO', 1),
('Cebada', 'Hordeum vulgare', 'GRANO', 'Cereal para maltería', 120, 'Mayo', 'Julio', 8.00, 20.00, 'BAJO', 1),
-- Forrajes
('Alfalfa', 'Medicago sativa', 'FORRAJE', 'Leguminosa forrajera perenne', 365, 'Marzo', 'Abril', 15.00, 25.00, 'ALTO', 1),
('Avena', 'Avena sativa', 'FORRAJE', 'Cereal forrajero', 90, 'Marzo', 'Julio', 10.00, 25.00, 'MEDIO', 1),
('Centeno', 'Secale cereale', 'FORRAJE', 'Cereal forrajero de invierno', 120, 'Marzo', 'Junio', 5.00, 20.00, 'BAJO', 1),
('Sorgo Forrajero', 'Sorghum bicolor', 'FORRAJE', 'Forraje de verano', 90, 'Octubre', 'Diciembre', 18.00, 35.00, 'BAJO', 1),
('Rye Grass', 'Lolium multiflorum', 'FORRAJE', 'Gramínea forrajera anual', 180, 'Febrero', 'Mayo', 10.00, 25.00, 'MEDIO', 1),
-- Hortalizas
('Tomate', 'Solanum lycopersicum', 'HORTALIZA', 'Fruto climatérico', 120, 'Septiembre', 'Noviembre', 18.00, 28.00, 'ALTO', 1),
('Papa', 'Solanum tuberosum', 'HORTALIZA', 'Tubérculo', 120, 'Agosto', 'Octubre', 15.00, 20.00, 'MEDIO', 1),
('Cebolla', 'Allium cepa', 'HORTALIZA', 'Bulbo', 150, 'Marzo', 'Mayo', 12.00, 24.00, 'MEDIO', 1),
('Lechuga', 'Lactuca sativa', 'HORTALIZA', 'Hoja', 60, 'Todo el año', 'Todo el año', 12.00, 20.00, 'MEDIO', 1)
ON DUPLICATE KEY UPDATE
  nombre_cientifico = VALUES(nombre_cientifico),
  tipo = VALUES(tipo),
  descripcion = VALUES(descripcion),
  ciclo_dias = VALUES(ciclo_dias),
  activo = VALUES(activo);

-- ============================================================
-- FIN DEL SCRIPT DE DATOS INICIALES
-- ============================================================

