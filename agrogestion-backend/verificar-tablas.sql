-- Script para verificar la estructura de la base de datos
USE agrocloud;

-- Mostrar todas las tablas
SHOW TABLES;

-- Ver estructura de la tabla insumos
DESCRIBE insumos;

-- Ver estructura de la tabla agroquimicos
DESCRIBE agroquimicos;

-- Ver estructura de la tabla formas_aplicacion (si existe)
DESCRIBE formas_aplicacion;

-- Ver datos de la tabla insumos
SELECT id, nombre, tipo, unidad_medida, stock_actual, activo 
FROM insumos 
ORDER BY id 
LIMIT 10;

-- Ver datos de la tabla agroquimicos
SELECT id, nombre_comercial, tipo, stock_actual, activo 
FROM agroquimicos 
ORDER BY id 
LIMIT 10;

-- Ver datos de la tabla formas_aplicacion (si existe)
SELECT * FROM formas_aplicacion;











