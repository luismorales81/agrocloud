-- Script para limpiar tablas de agroquímicos existentes
-- Eliminar tablas en orden correcto (primero las dependientes)

-- Eliminar vistas primero
DROP VIEW IF EXISTS vista_agroquimicos_completos;
DROP VIEW IF EXISTS vista_aplicaciones_agroquimicos;
DROP VIEW IF EXISTS vista_dosis_agroquimicos;

-- Eliminar tablas dependientes
DROP TABLE IF EXISTS aplicaciones_agroquimicos;
DROP TABLE IF EXISTS dosis_agroquimicos;
DROP TABLE IF EXISTS agroquimico_cultivos_permitidos;

-- Eliminar tabla principal
DROP TABLE IF EXISTS agroquimicos;

-- Verificar que se eliminaron
SHOW TABLES LIKE '%agroquimico%';
