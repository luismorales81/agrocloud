-- Spec SDD: Labor como fuente de verdad. Añadir cultivo_id opcional.
-- Permite asociar la labor a un cultivo (ej. siembra, cosecha).

ALTER TABLE cultivo_labores
ADD COLUMN cultivo_id BIGINT NULL COMMENT 'Cultivo asociado (opcional)' AFTER lote_id;

ALTER TABLE cultivo_labores
ADD CONSTRAINT fk_cultivo_labores_cultivo
  FOREIGN KEY (cultivo_id) REFERENCES cultivo_cultivos(id) ON DELETE SET NULL;

CREATE INDEX idx_cultivo_labores_cultivo_id ON cultivo_labores(cultivo_id);
