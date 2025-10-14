-- Script para completar la estructura de la tabla egresos
-- Ejecutar este script en la base de datos MySQL

USE agrocloud;

-- Agregar las columnas faltantes a la tabla egresos
ALTER TABLE egresos 
ADD COLUMN costo_unitario DECIMAL(10,2) NULL AFTER cantidad,
ADD COLUMN referencia_id BIGINT NULL AFTER costo_unitario;

-- Crear índice para la nueva columna referencia_id
CREATE INDEX idx_egresos_referencia ON egresos(referencia_id);

-- Verificar la estructura final
DESCRIBE egresos;
