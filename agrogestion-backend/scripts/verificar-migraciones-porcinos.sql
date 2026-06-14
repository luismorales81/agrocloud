-- ============================================================================
-- Verificar si las migraciones de Porcinos (V1_128, V1_129, V1_130, V1_131) ya corrieron
-- Ejecutar en MySQL: mysql -u root -p agrocloud < scripts/verificar-migraciones-porcinos.sql
-- O abrir en MySQL Workbench / DBeaver y ejecutar
-- ============================================================================

USE agrocloud;

-- 1) Historial Flyway: últimas migraciones
SELECT '=== flyway_schema_history (últimas 30) ===' AS info;
SELECT installed_rank, version, description, type, script, installed_on, success
FROM flyway_schema_history
ORDER BY installed_rank DESC
LIMIT 30;

-- 2) ¿Están aplicadas las de Porcinos?
SELECT '=== Migraciones Porcinos (V1_128, V1_129, V1_130, V1_131) ===' AS info;
SELECT version, description, installed_on, success
FROM flyway_schema_history
WHERE version IN ('1.128', '1.129', '1.130', '1.131')
ORDER BY installed_rank;

-- 3) Estructura porcinos_destetes (UNIQUE parto_id, FK)
SELECT '=== porcinos_destetes - constraints ===' AS info;
SHOW CREATE TABLE porcinos_destetes;

-- 4) Estructura porcinos_recria (version, CHECK cantidad_animales)
SELECT '=== porcinos_recria - columnas version y constraints ===' AS info;
SHOW CREATE TABLE porcinos_recria;

-- 5) Columna gestacion_id en partos
SELECT '=== porcinos_partos - columna gestacion_id ===' AS info;
SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE, COLUMN_DEFAULT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'agrocloud' AND TABLE_NAME = 'porcinos_partos' AND COLUMN_NAME = 'gestacion_id';
