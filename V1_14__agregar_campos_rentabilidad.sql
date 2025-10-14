-- Migration: Agregar campos de rentabilidad a historial_cosechas
-- Version: V1_14
-- Description: Agrega columnas para calcular rentabilidad automáticamente

USE agrocloud;

ALTER TABLE historial_cosechas 
ADD COLUMN IF NOT EXISTS precio_venta_unitario DECIMAL(10,2) DEFAULT 0 COMMENT 'Precio de venta por unidad (kg/ton)',
ADD COLUMN IF NOT EXISTS ingreso_total DECIMAL(15,2) DEFAULT 0 COMMENT 'Ingreso total por la venta (cantidad * precio)',
ADD COLUMN IF NOT EXISTS costo_total_produccion DECIMAL(15,2) DEFAULT 0 COMMENT 'Costo total de producción (maquinaria + mano de obra)';

