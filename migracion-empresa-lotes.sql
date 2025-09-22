-- Agregar columna 'empresa_id' a la tabla 'lotes' para sistema multitenant
ALTER TABLE lotes 
ADD COLUMN empresa_id BIGINT NULL COMMENT 'ID de la empresa propietaria del lote';

-- Crear índice para búsquedas por empresa
CREATE INDEX idx_lotes_empresa ON lotes (empresa_id);

-- Agregar foreign key constraint
ALTER TABLE lotes 
ADD CONSTRAINT fk_lotes_empresa 
FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE SET NULL;

-- Actualizar lotes existentes con empresa_id basado en el usuario propietario
UPDATE lotes l 
SET empresa_id = (
    SELECT ucr.empresa_id 
    FROM usuarios_empresas_roles ucr 
    WHERE ucr.usuario_id = l.user_id 
    LIMIT 1
)
WHERE l.empresa_id IS NULL;

SELECT 'Migracion completada exitosamente' as resultado;
