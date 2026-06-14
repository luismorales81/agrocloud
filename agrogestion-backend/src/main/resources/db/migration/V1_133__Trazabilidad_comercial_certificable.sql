-- Trazabilidad comercial: certificaciones, reglas configurables, reportes sellados (PDF + snapshot)

CREATE TABLE IF NOT EXISTS trazabilidad_certificacion (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  codigo VARCHAR(64) NOT NULL,
  descripcion VARCHAR(500) NULL,
  activa TINYINT(1) NOT NULL DEFAULT 1,
  creado_en DATETIME(6) NULL,
  actualizado_en DATETIME(6) NULL,
  UNIQUE KEY uk_traz_cert_codigo (codigo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS trazabilidad_regla_certificacion (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  certificacion_id BIGINT NOT NULL,
  tipo_regla VARCHAR(64) NOT NULL,
  parametros_json TEXT NOT NULL,
  orden_ejecucion INT NOT NULL DEFAULT 0,
  activa TINYINT(1) NOT NULL DEFAULT 1,
  creado_en DATETIME(6) NULL,
  actualizado_en DATETIME(6) NULL,
  CONSTRAINT fk_traz_regla_cert FOREIGN KEY (certificacion_id) REFERENCES trazabilidad_certificacion(id) ON DELETE CASCADE,
  KEY idx_traz_regla_cert (certificacion_id, activa)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS trazabilidad_reporte (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  empresa_id BIGINT NOT NULL,
  usuario_id BIGINT NOT NULL,
  entidad_tipo VARCHAR(20) NOT NULL,
  entidad_id BIGINT NOT NULL,
  certificacion_codigo VARCHAR(64) NOT NULL,
  resultado VARCHAR(20) NOT NULL,
  version_motor VARCHAR(16) NOT NULL,
  snapshot_json LONGTEXT NOT NULL,
  hash_snapshot VARCHAR(64) NOT NULL,
  hash_pdf VARCHAR(64) NULL,
  contenido_pdf LONGBLOB NULL,
  generado_en DATETIME(6) NOT NULL,
  CONSTRAINT fk_traz_rep_empresa FOREIGN KEY (empresa_id) REFERENCES empresas(id),
  CONSTRAINT fk_traz_rep_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
  KEY idx_traz_rep_emp (empresa_id, generado_en),
  KEY idx_traz_rep_ent (entidad_tipo, entidad_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Semilla: reglas en JSON (no lógica en código de negocio)
INSERT INTO trazabilidad_certificacion (codigo, descripcion, activa, creado_en) VALUES
('LIBRE_AGROQUIMICOS', 'No se registran usos de insumos de tipos indicados (configurables) en el alcance de labores del lote/ciclo.', 1, NOW(6)),
('SIN_ANTIBIOTICOS', 'No se registran eventos sanitarios con categoría antibiótico en la recría indicada.', 1, NOW(6))
ON DUPLICATE KEY UPDATE descripcion=VALUES(descripcion), activa=1;

SET @c_libre = (SELECT id FROM trazabilidad_certificacion WHERE codigo = 'LIBRE_AGROQUIMICOS' LIMIT 1);
SET @c_antib = (SELECT id FROM trazabilidad_certificacion WHERE codigo = 'SIN_ANTIBIOTICOS' LIMIT 1);

INSERT INTO trazabilidad_regla_certificacion (certificacion_id, tipo_regla, parametros_json, orden_ejecucion, activa, creado_en)
SELECT @c_libre, 'NINGUNO_TIPOS_INSUMO', '{"tiposInsumoProhibidos":["HERBICIDA","FUNGICIDA","INSECTICIDA","FERTILIZANTE"]}', 0, 1, NOW(6)
FROM DUAL
WHERE @c_libre IS NOT NULL
AND NOT EXISTS (SELECT 1 FROM trazabilidad_regla_certificacion r WHERE r.certificacion_id = @c_libre AND r.tipo_regla = 'NINGUNO_TIPOS_INSUMO');

INSERT INTO trazabilidad_regla_certificacion (certificacion_id, tipo_regla, parametros_json, orden_ejecucion, activa, creado_en)
SELECT @c_antib, 'SIN_CATEGORIA_SANITARIA', '{"categoriasProhibidas":["ANTIBIOTICO"]}', 0, 1, NOW(6)
FROM DUAL
WHERE @c_antib IS NOT NULL
AND NOT EXISTS (SELECT 1 FROM trazabilidad_regla_certificacion r WHERE r.certificacion_id = @c_antib AND r.tipo_regla = 'SIN_CATEGORIA_SANITARIA');
