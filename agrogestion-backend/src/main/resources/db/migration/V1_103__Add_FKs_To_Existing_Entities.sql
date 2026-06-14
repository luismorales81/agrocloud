-- ============================================================================
-- MIGRACIÓN: Agregar Foreign Keys a catálogos en entidades existentes
-- Versión: V1_103
-- Fecha: 2025-12-05
-- Descripción: Agregar FKs a catálogos en Madre, Padrillo, Parto y entidades de mortalidad
-- Nota: Sin ADD COLUMN IF NOT EXISTS (requiere MySQL 8.0.29+); compatible con 5.7 / 8.0 antiguos.
-- ============================================================================

SET @esquema = DATABASE();

-- Agrega columna solo si no existe (information_schema + prepared statement)
-- Uso: llamar con nombre_tabla, nombre_columna, fragmento SQL tras el nombre (tipo, null, default, comment)

-- ============================================================================
-- AGREGAR CAMPOS A TABLA porcinos_madres
-- ============================================================================
SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'porcinos_madres' AND COLUMN_NAME = 'raza_id') > 0,
  'SELECT 1',
  'ALTER TABLE porcinos_madres ADD COLUMN raza_id BIGINT NULL'));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'porcinos_madres' AND COLUMN_NAME = 'numero_partos') > 0,
  'SELECT 1',
  'ALTER TABLE porcinos_madres ADD COLUMN numero_partos INT NOT NULL DEFAULT 0'));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'porcinos_madres' AND COLUMN_NAME = 'fecha_baja') > 0,
  'SELECT 1',
  'ALTER TABLE porcinos_madres ADD COLUMN fecha_baja DATE NULL COMMENT ''Fecha de baja (muerte, venta, descarte)'''));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'porcinos_madres' AND COLUMN_NAME = 'motivo_baja_id') > 0,
  'SELECT 1',
  'ALTER TABLE porcinos_madres ADD COLUMN motivo_baja_id BIGINT NULL'));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
   WHERE CONSTRAINT_SCHEMA = @esquema AND TABLE_NAME = 'porcinos_madres' AND CONSTRAINT_NAME = 'fk_madre_raza') > 0,
  'SELECT 1',
  'ALTER TABLE porcinos_madres ADD CONSTRAINT fk_madre_raza FOREIGN KEY (raza_id) REFERENCES porcinos_razas_porcinos(id) ON DELETE SET NULL'));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
   WHERE CONSTRAINT_SCHEMA = @esquema AND TABLE_NAME = 'porcinos_madres' AND CONSTRAINT_NAME = 'fk_madre_motivo_baja') > 0,
  'SELECT 1',
  'ALTER TABLE porcinos_madres ADD CONSTRAINT fk_madre_motivo_baja FOREIGN KEY (motivo_baja_id) REFERENCES porcinos_motivos_baja_porcinos(id) ON DELETE SET NULL'));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- ============================================================================
-- AGREGAR CAMPOS A TABLA porcinos_padrillos
-- ============================================================================
SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'porcinos_padrillos' AND COLUMN_NAME = 'raza_id') > 0,
  'SELECT 1',
  'ALTER TABLE porcinos_padrillos ADD COLUMN raza_id BIGINT NULL'));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'porcinos_padrillos' AND COLUMN_NAME = 'proveedor_genetica_id') > 0,
  'SELECT 1',
  'ALTER TABLE porcinos_padrillos ADD COLUMN proveedor_genetica_id BIGINT NULL'));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
   WHERE CONSTRAINT_SCHEMA = @esquema AND TABLE_NAME = 'porcinos_padrillos' AND CONSTRAINT_NAME = 'fk_padrillo_raza') > 0,
  'SELECT 1',
  'ALTER TABLE porcinos_padrillos ADD CONSTRAINT fk_padrillo_raza FOREIGN KEY (raza_id) REFERENCES porcinos_razas_porcinos(id) ON DELETE SET NULL'));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
   WHERE CONSTRAINT_SCHEMA = @esquema AND TABLE_NAME = 'porcinos_padrillos' AND CONSTRAINT_NAME = 'fk_padrillo_proveedor_genetica') > 0,
  'SELECT 1',
  'ALTER TABLE porcinos_padrillos ADD CONSTRAINT fk_padrillo_proveedor_genetica FOREIGN KEY (proveedor_genetica_id) REFERENCES porcinos_proveedores_genetica(id) ON DELETE SET NULL'));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- ============================================================================
-- AGREGAR CAMPOS A TABLA porcinos_partos
-- ============================================================================
SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'porcinos_partos' AND COLUMN_NAME = 'tipo_parto_id') > 0,
  'SELECT 1',
  'ALTER TABLE porcinos_partos ADD COLUMN tipo_parto_id BIGINT NULL'));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'porcinos_partos' AND COLUMN_NAME = 'intervenciones') > 0,
  'SELECT 1',
  'ALTER TABLE porcinos_partos ADD COLUMN intervenciones TEXT COMMENT ''Intervenciones realizadas durante el parto'''));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
   WHERE CONSTRAINT_SCHEMA = @esquema AND TABLE_NAME = 'porcinos_partos' AND CONSTRAINT_NAME = 'fk_parto_tipo_parto') > 0,
  'SELECT 1',
  'ALTER TABLE porcinos_partos ADD CONSTRAINT fk_parto_tipo_parto FOREIGN KEY (tipo_parto_id) REFERENCES porcinos_tipos_parto(id) ON DELETE SET NULL'));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- ============================================================================
-- TABLA INTERMEDIA: porcinos_partos_nacidos_muertos_causas
-- ============================================================================
CREATE TABLE IF NOT EXISTS porcinos_partos_nacidos_muertos_causas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    parto_id BIGINT NOT NULL,
    causa_nacido_muerto_id BIGINT NOT NULL,
    cantidad INT NOT NULL DEFAULT 1 COMMENT 'Cantidad de nacidos muertos con esta causa',
    
    FOREIGN KEY (parto_id) REFERENCES porcinos_partos(id) ON DELETE CASCADE,
    FOREIGN KEY (causa_nacido_muerto_id) REFERENCES porcinos_causas_nacidos_muertos(id),
    
    INDEX idx_parto_nacido_muerto_parto (parto_id),
    INDEX idx_parto_nacido_muerto_causa (causa_nacido_muerto_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- TABLA INTERMEDIA: porcinos_partos_momificados_causas
-- ============================================================================
CREATE TABLE IF NOT EXISTS porcinos_partos_momificados_causas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    parto_id BIGINT NOT NULL,
    causa_momificado_id BIGINT NOT NULL,
    cantidad INT NOT NULL DEFAULT 1 COMMENT 'Cantidad de momificados con esta causa',
    
    FOREIGN KEY (parto_id) REFERENCES porcinos_partos(id) ON DELETE CASCADE,
    FOREIGN KEY (causa_momificado_id) REFERENCES porcinos_causas_momificados(id),
    
    INDEX idx_parto_momificado_parto (parto_id),
    INDEX idx_parto_momificado_causa (causa_momificado_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- AGREGAR FK A CAUSA_MORTALIDAD_PORCINO EN TABLAS DE MORTALIDAD
-- ============================================================================
SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'porcinos_muertes_lactancia' AND COLUMN_NAME = 'causa_mortalidad_id') > 0,
  'SELECT 1',
  'ALTER TABLE porcinos_muertes_lactancia ADD COLUMN causa_mortalidad_id BIGINT NULL'));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
   WHERE CONSTRAINT_SCHEMA = @esquema AND TABLE_NAME = 'porcinos_muertes_lactancia' AND CONSTRAINT_NAME = 'fk_muerte_lactancia_causa') > 0,
  'SELECT 1',
  'ALTER TABLE porcinos_muertes_lactancia ADD CONSTRAINT fk_muerte_lactancia_causa FOREIGN KEY (causa_mortalidad_id) REFERENCES porcinos_causas_mortalidad_porcinos(id) ON DELETE SET NULL'));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'porcinos_muertes_recria' AND COLUMN_NAME = 'causa_mortalidad_id') > 0,
  'SELECT 1',
  'ALTER TABLE porcinos_muertes_recria ADD COLUMN causa_mortalidad_id BIGINT NULL'));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
   WHERE CONSTRAINT_SCHEMA = @esquema AND TABLE_NAME = 'porcinos_muertes_recria' AND CONSTRAINT_NAME = 'fk_muerte_recria_causa') > 0,
  'SELECT 1',
  'ALTER TABLE porcinos_muertes_recria ADD CONSTRAINT fk_muerte_recria_causa FOREIGN KEY (causa_mortalidad_id) REFERENCES porcinos_causas_mortalidad_porcinos(id) ON DELETE SET NULL'));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'porcinos_muertes_madres' AND COLUMN_NAME = 'causa_mortalidad_id') > 0,
  'SELECT 1',
  'ALTER TABLE porcinos_muertes_madres ADD COLUMN causa_mortalidad_id BIGINT NULL'));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'porcinos_muertes_madres' AND COLUMN_NAME = 'motivo_baja_id') > 0,
  'SELECT 1',
  'ALTER TABLE porcinos_muertes_madres ADD COLUMN motivo_baja_id BIGINT NULL'));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
   WHERE CONSTRAINT_SCHEMA = @esquema AND TABLE_NAME = 'porcinos_muertes_madres' AND CONSTRAINT_NAME = 'fk_muerte_madre_causa') > 0,
  'SELECT 1',
  'ALTER TABLE porcinos_muertes_madres ADD CONSTRAINT fk_muerte_madre_causa FOREIGN KEY (causa_mortalidad_id) REFERENCES porcinos_causas_mortalidad_porcinos(id) ON DELETE SET NULL'));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
   WHERE CONSTRAINT_SCHEMA = @esquema AND TABLE_NAME = 'porcinos_muertes_madres' AND CONSTRAINT_NAME = 'fk_muerte_madre_motivo_baja') > 0,
  'SELECT 1',
  'ALTER TABLE porcinos_muertes_madres ADD CONSTRAINT fk_muerte_madre_motivo_baja FOREIGN KEY (motivo_baja_id) REFERENCES porcinos_motivos_baja_porcinos(id) ON DELETE SET NULL'));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- ============================================================================
-- AGREGAR FK A MOTIVO_BAJA EN TABLA porcinos_padrillos
-- ============================================================================
SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'porcinos_padrillos' AND COLUMN_NAME = 'motivo_baja_id') > 0,
  'SELECT 1',
  'ALTER TABLE porcinos_padrillos ADD COLUMN motivo_baja_id BIGINT NULL'));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
   WHERE CONSTRAINT_SCHEMA = @esquema AND TABLE_NAME = 'porcinos_padrillos' AND CONSTRAINT_NAME = 'fk_padrillo_motivo_baja') > 0,
  'SELECT 1',
  'ALTER TABLE porcinos_padrillos ADD CONSTRAINT fk_padrillo_motivo_baja FOREIGN KEY (motivo_baja_id) REFERENCES porcinos_motivos_baja_porcinos(id) ON DELETE SET NULL'));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- ============================================================================
-- AGREGAR UBICACION_INTERNA A ENTIDADES
-- ============================================================================
SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'porcinos_madres' AND COLUMN_NAME = 'ubicacion_interna_id') > 0,
  'SELECT 1',
  'ALTER TABLE porcinos_madres ADD COLUMN ubicacion_interna_id BIGINT NULL'));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
   WHERE CONSTRAINT_SCHEMA = @esquema AND TABLE_NAME = 'porcinos_madres' AND CONSTRAINT_NAME = 'fk_madre_ubicacion_interna') > 0,
  'SELECT 1',
  'ALTER TABLE porcinos_madres ADD CONSTRAINT fk_madre_ubicacion_interna FOREIGN KEY (ubicacion_interna_id) REFERENCES porcinos_ubicaciones_internas(id) ON DELETE SET NULL'));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'porcinos_padrillos' AND COLUMN_NAME = 'ubicacion_interna_id') > 0,
  'SELECT 1',
  'ALTER TABLE porcinos_padrillos ADD COLUMN ubicacion_interna_id BIGINT NULL'));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
   WHERE CONSTRAINT_SCHEMA = @esquema AND TABLE_NAME = 'porcinos_padrillos' AND CONSTRAINT_NAME = 'fk_padrillo_ubicacion_interna') > 0,
  'SELECT 1',
  'ALTER TABLE porcinos_padrillos ADD CONSTRAINT fk_padrillo_ubicacion_interna FOREIGN KEY (ubicacion_interna_id) REFERENCES porcinos_ubicaciones_internas(id) ON DELETE SET NULL'));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'porcinos_recria' AND COLUMN_NAME = 'ubicacion_interna_id') > 0,
  'SELECT 1',
  'ALTER TABLE porcinos_recria ADD COLUMN ubicacion_interna_id BIGINT NULL'));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
   WHERE CONSTRAINT_SCHEMA = @esquema AND TABLE_NAME = 'porcinos_recria' AND CONSTRAINT_NAME = 'fk_recria_ubicacion_interna') > 0,
  'SELECT 1',
  'ALTER TABLE porcinos_recria ADD CONSTRAINT fk_recria_ubicacion_interna FOREIGN KEY (ubicacion_interna_id) REFERENCES porcinos_ubicaciones_internas(id) ON DELETE SET NULL'));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;
