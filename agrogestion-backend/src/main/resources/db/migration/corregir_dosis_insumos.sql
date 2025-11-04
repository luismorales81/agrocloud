-- Script para corregir la relación entre dosis_insumos e insumos
-- La tabla dosis_insumos debe enlazarse directamente con insumos, no con agroquimicos

-- 1. Verificar si existe la columna agroquimico_id y cambiarla a insumo_id si es necesario
ALTER TABLE dosis_insumos 
CHANGE COLUMN IF EXISTS agroquimico_id insumo_id BIGINT NOT NULL,
ADD FOREIGN KEY (insumo_id) REFERENCES insumos(id) ON DELETE CASCADE;

-- Verificar que las dosis existentes tengan insumo_id válidos
SELECT COUNT(*) as total_dosis FROM dosis_insumos WHERE insumo_id IS NOT NULL;

