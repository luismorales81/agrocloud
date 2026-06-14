-- Expediente de trazabilidad: ampliar tipos de entidad y certificación de expediente

ALTER TABLE trazabilidad_reporte
  MODIFY COLUMN entidad_tipo VARCHAR(32) NOT NULL;

INSERT INTO trazabilidad_certificacion (codigo, descripcion, activa, creado_en) VALUES
('EXPEDIENTE_CICLO_VIDA', 'Documento PDF con el ciclo de vida y datos de origen del producto/lote (sin reglas de rechazo).', 1, NOW(6))
ON DUPLICATE KEY UPDATE descripcion=VALUES(descripcion), activa=1;
