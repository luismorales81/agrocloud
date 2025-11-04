-- Script para eliminar todas las tablas no necesarias
-- Mantener solo las tablas esenciales del sistema

-- Deshabilitar verificación de claves foráneas temporalmente
SET FOREIGN_KEY_CHECKS = 0;

-- Eliminar vistas primero
DROP VIEW IF EXISTS vista_estadisticas_rendimiento_cultivo;
DROP VIEW IF EXISTS vista_lotes_requieren_descanso;
DROP VIEW IF EXISTS vista_agroquimicos_completos;
DROP VIEW IF EXISTS vista_aplicaciones_agroquimicos;
DROP VIEW IF EXISTS vista_dosis_agroquimicos;

-- Eliminar tablas de agroquímicos complejas (no necesarias)
DROP TABLE IF EXISTS condiciones_aplicacion;
DROP TABLE IF EXISTS aplicaciones_agroquimicas;
DROP TABLE IF EXISTS aplicaciones_agroquimicos;
DROP TABLE IF EXISTS dosis_agroquimicos;
DROP TABLE IF EXISTS dosis_aplicacion;
DROP TABLE IF EXISTS dosis_insumos;
DROP TABLE IF EXISTS agroquimico_cultivos_permitidos;
DROP TABLE IF EXISTS agroquimicos;

-- Eliminar tablas de labor complejas (no necesarias)
DROP TABLE IF EXISTS labor_insumos;
DROP TABLE IF EXISTS labor_mano_obra;
DROP TABLE IF EXISTS labor_maquinaria;

-- Eliminar tablas de inventario complejas (no necesarias)
DROP TABLE IF EXISTS movimientos_inventario;
DROP TABLE IF EXISTS movimientos_inventario_granos;

-- Eliminar tablas de logs (no necesarias para funcionalidad básica)
DROP TABLE IF EXISTS logs_acceso;
DROP TABLE IF EXISTS weather_api_usage;

-- Eliminar tablas de permisos complejas (simplificar)
DROP TABLE IF EXISTS roles_permisos;
DROP TABLE IF EXISTS usuarios_roles;
DROP TABLE IF EXISTS usuarios_empresas_roles;

-- Habilitar verificación de claves foráneas
SET FOREIGN_KEY_CHECKS = 1;

-- Verificar tablas restantes
SHOW TABLES;
