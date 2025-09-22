-- Verificar si existen las tablas de maquinaria y mano de obra
SHOW TABLES LIKE 'labor_maquinaria';
SHOW TABLES LIKE 'labor_mano_obra';

-- Verificar estructura de labor_maquinaria si existe
DESCRIBE labor_maquinaria;

-- Verificar estructura de labor_mano_obra si existe
DESCRIBE labor_mano_obra;

-- Verificar datos en labor_maquinaria si existe
SELECT * FROM labor_maquinaria LIMIT 5;

-- Verificar datos en labor_mano_obra si existe
SELECT * FROM labor_mano_obra LIMIT 5;

-- Verificar estructura de la tabla labores
DESCRIBE labores;

-- Ver datos recientes de labores
SELECT id, tipo_labor, nombre, descripcion, fecha_inicio, fecha_fin, estado, costo_total, lote_id, usuario_id 
FROM labores 
ORDER BY id DESC 
LIMIT 5;


