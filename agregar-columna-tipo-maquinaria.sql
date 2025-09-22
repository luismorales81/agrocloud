-- Agregar columna tipo_maquinaria a la tabla labor_maquinaria
-- para diferenciar entre maquinaria propia y alquilada

USE agrocloud;

-- Agregar la columna tipo_maquinaria
ALTER TABLE labor_maquinaria 
ADD COLUMN tipo_maquinaria ENUM('PROPIA', 'ALQUILADA') NOT NULL DEFAULT 'PROPIA' 
AFTER descripcion;

-- Verificar la estructura actualizada
DESCRIBE labor_maquinaria;

-- Mostrar los datos actuales
SELECT 
    id_labor_maquinaria,
    id_labor,
    descripcion,
    tipo_maquinaria,
    proveedor,
    costo,
    observaciones,
    created_at,
    updated_at
FROM labor_maquinaria 
ORDER BY id_labor_maquinaria DESC 
LIMIT 10;


