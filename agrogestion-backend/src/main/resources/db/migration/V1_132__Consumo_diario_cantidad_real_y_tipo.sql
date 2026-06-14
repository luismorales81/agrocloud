-- Consumo diario: kg reales informados por operario y tipo estimado/real
ALTER TABLE porcinos_consumos_diarios_automaticos
ADD COLUMN cantidad_receta_real DECIMAL(10,2) NULL
    COMMENT 'kg total de ración informado por operario; NULL = solo proyección'
    AFTER cantidad_receta_total,
ADD COLUMN tipo_registro_consumo VARCHAR(20) NOT NULL DEFAULT 'ESTIMADO'
    COMMENT 'ESTIMADO: solo plan; REAL: se cargó cantidad_receta_real'
    AFTER cantidad_receta_real;
