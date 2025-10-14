-- Script para corregir la estructura de la tabla egresos
-- Ejecutar este script en la base de datos MySQL

USE agrocloud;

-- Agregar las columnas faltantes a la tabla egresos
ALTER TABLE egresos 
ADD COLUMN cantidad DECIMAL(10,2) NULL AFTER monto,
ADD COLUMN unidad_medida VARCHAR(50) NULL AFTER cantidad,
ADD COLUMN proveedor VARCHAR(200) NULL AFTER unidad_medida,
ADD COLUMN observaciones TEXT NULL AFTER proveedor,
ADD COLUMN estado VARCHAR(50) NOT NULL DEFAULT 'REGISTRADO' AFTER observaciones,
ADD COLUMN tipo_egreso VARCHAR(50) NOT NULL DEFAULT 'OTROS_EGRESOS' AFTER estado;

-- Actualizar los registros existentes para que tengan valores por defecto
UPDATE egresos SET 
    estado = 'REGISTRADO',
    tipo_egreso = 'OTROS_EGRESOS'
WHERE estado IS NULL OR tipo_egreso IS NULL;

-- Crear índices para las nuevas columnas
CREATE INDEX idx_egresos_estado ON egresos(estado);
CREATE INDEX idx_egresos_tipo_egreso ON egresos(tipo_egreso);

-- Verificar la estructura final
DESCRIBE egresos;
