-- ============================================================
-- SCRIPT DE MIGRACION: Datos de Producción
-- AgroGestion - Version 2.0
-- SOLO para ambiente de Producción
-- ============================================================

SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- ============================================================
-- USUARIO ADMINISTRADOR INICIAL
-- ============================================================

-- ⚠️ IMPORTANTE: Cambia la contraseña después del primer login
-- Password temporal: "Admin2025!Temp"
-- Hash bcrypt: $2a$10$K9p3LMoV8xN2wH5yE3pQg.7fB8jK9xM4nL2pR5tQ6wX8vY1zA3bCm

INSERT INTO `usuarios` (`id`, `username`, `email`, `password`, `first_name`, `last_name`, 
  `phone`, `estado`, `activo`, `email_verified`) VALUES
(1, 'admin', 'admin@tudominio.com', '$2a$10$K9p3LMoV8xN2wH5yE3pQg.7fB8jK9xM4nL2pR5tQ6wX8vY1zA3bCm', 
  'Administrador', 'Sistema', '', 'ACTIVO', 1, 1)
ON DUPLICATE KEY UPDATE
  username = VALUES(username),
  email = VALUES(email);

-- ============================================================
-- EMPRESA PRINCIPAL
-- ============================================================

-- ⚠️ IMPORTANTE: Actualiza estos datos con la información real de tu empresa

INSERT INTO `empresas` (`id`, `nombre`, `razon_social`, `cuit`, `direccion`, `ciudad`, 
  `provincia`, `telefono`, `email_contacto`, `estado`, `activo`) VALUES
(1, 'Mi Empresa Agropecuaria', 'Mi Empresa Agropecuaria S.A.', '30-00000000-0', 
  'Dirección de la Empresa', 'Ciudad', 'Provincia', 
  'Teléfono', 'contacto@miempresa.com', 'ACTIVO', 1)
ON DUPLICATE KEY UPDATE
  nombre = VALUES(nombre),
  estado = VALUES(estado);

-- ============================================================
-- ASIGNACIÓN DE ROL ADMINISTRADOR
-- ============================================================

INSERT INTO `usuario_empresas` (`usuario_id`, `empresa_id`, `rol`, `estado`, `fecha_inicio`) VALUES
(1, 1, 'ADMINISTRADOR', 'ACTIVO', CURDATE())
ON DUPLICATE KEY UPDATE
  estado = VALUES(estado);

-- ============================================================
-- INFORMACIÓN IMPORTANTE
-- ============================================================

-- 🔐 CREDENCIALES INICIALES:
-- Email: admin@tudominio.com
-- Password temporal: Admin2025!Temp
--
-- ⚠️ TAREAS POST-DEPLOYMENT:
-- 1. Cambiar la contraseña del administrador inmediatamente
-- 2. Actualizar el email del administrador
-- 3. Actualizar los datos de la empresa
-- 4. Crear usuarios adicionales según sea necesario
-- 5. Configurar campos y lotes reales
--
-- 🚨 SEGURIDAD:
-- - No uses contraseñas débiles
-- - Cambia las credenciales por defecto
-- - Mantén backups regulares de la base de datos
-- - No compartas credenciales de producción

-- ============================================================
-- FIN DEL SCRIPT DE DATOS DE PRODUCCIÓN
-- ============================================================

