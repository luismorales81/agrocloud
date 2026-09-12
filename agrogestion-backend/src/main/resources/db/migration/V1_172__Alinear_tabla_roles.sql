-- Alinear tabla roles con la entidad JPA (BD vacía Flyway vs esquemas Hibernate/Railway).

SET @esquema = DATABASE();

SET @tiene_activo = (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'roles' AND COLUMN_NAME = 'activo'
);
SET @sql_activo = IF(@tiene_activo = 0,
  'ALTER TABLE roles ADD COLUMN activo BOOLEAN NOT NULL DEFAULT TRUE',
  'SELECT 1');
PREPARE stmt_activo FROM @sql_activo;
EXECUTE stmt_activo;
DEALLOCATE PREPARE stmt_activo;

SET @tiene_name = (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'roles' AND COLUMN_NAME = 'name'
);
SET @tiene_nombre = (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @esquema AND TABLE_NAME = 'roles' AND COLUMN_NAME = 'nombre'
);
SET @sql_rename = IF(@tiene_name = 1 AND @tiene_nombre = 0,
  'ALTER TABLE roles CHANGE COLUMN name nombre VARCHAR(50) NOT NULL',
  'SELECT 1');
PREPARE stmt_rename FROM @sql_rename;
EXECUTE stmt_rename;
DEALLOCATE PREPARE stmt_rename;
