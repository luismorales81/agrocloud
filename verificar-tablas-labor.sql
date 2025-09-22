-- Verificar si existen las tablas de labor
SHOW TABLES LIKE 'labor_%';

-- Verificar estructura de labor_maquinaria si existe
DESCRIBE labor_maquinaria;

-- Verificar estructura de labor_mano_obra si existe
DESCRIBE labor_mano_obra;

-- Verificar datos en labor_maquinaria si existe
SELECT * FROM labor_maquinaria LIMIT 5;

-- Verificar datos en labor_mano_obra si existe
SELECT * FROM labor_mano_obra LIMIT 5;
