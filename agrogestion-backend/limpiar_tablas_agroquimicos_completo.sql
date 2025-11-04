-- Script completo para limpiar tablas de agroquímicos existentes
-- Eliminar restricciones de clave foránea primero

-- Deshabilitar verificación de claves foráneas temporalmente
SET FOREIGN_KEY_CHECKS = 0;

-- Eliminar vistas primero
DROP VIEW IF EXISTS vista_agroquimicos_completos;
DROP VIEW IF EXISTS vista_aplicaciones_agroquimicos;
DROP VIEW IF EXISTS vista_dosis_agroquimicos;

-- Eliminar tablas dependientes en orden correcto
DROP TABLE IF EXISTS condiciones_aplicacion;
DROP TABLE IF EXISTS aplicaciones_agroquimicos;
DROP TABLE IF EXISTS dosis_agroquimicos;
DROP TABLE IF EXISTS agroquimico_cultivos_permitidos;

-- Eliminar tabla principal
DROP TABLE IF EXISTS agroquimicos;

-- Habilitar verificación de claves foráneas
SET FOREIGN_KEY_CHECKS = 1;

-- Verificar que se eliminaron
SHOW TABLES LIKE '%agroquimico%';
