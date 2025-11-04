-- Aumentar la precisión de costo_total en la tabla labores
-- DECIMAL(10,2) puede almacenar hasta 99,999,999.99
-- DECIMAL(15,2) puede almacenar hasta 999,999,999,999,999.99 (999 billones)
-- Esto permite valores más grandes cuando se usan insumos caros o cantidades muy grandes

ALTER TABLE labores 
MODIFY COLUMN costo_total DECIMAL(15,2) DEFAULT 0.00;

