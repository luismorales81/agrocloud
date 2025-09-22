-- V1_9__Add_Responsable_To_Labores.sql

-- Agregar columna 'responsable' a la tabla 'labores'
ALTER TABLE labores 
ADD COLUMN responsable VARCHAR(255) NULL COMMENT 'Nombre del responsable de la labor';

-- Crear índice para búsquedas por responsable si es necesario
CREATE INDEX idx_labores_responsable ON labores (responsable);
