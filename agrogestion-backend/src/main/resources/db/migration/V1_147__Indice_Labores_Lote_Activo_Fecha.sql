-- Indice compuesto para listados frecuentes de labores por lote
CREATE INDEX idx_cultivo_labores_lote_activo_fecha
    ON cultivo_labores (lote_id, activo, fecha_inicio DESC);
