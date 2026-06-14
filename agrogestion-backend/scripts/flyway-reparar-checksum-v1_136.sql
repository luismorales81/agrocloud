-- Reparación manual cuando Flyway falla con:
--   Migration checksum mismatch for migration version 1.136
-- Ocurre si se editó V1_136__avicola_initial.sql después de haberse aplicado en esta base.
--
-- Opción preferida: desde agrogestion-backend ejecutar (con tu URL/usuario/clave reales):
--   .\scripts\ejecutar-flyway-repair.ps1 -Url "jdbc:mysql://HOST:3306/TU_BD" -User "..." -Password "..."
-- Eso recalcula checksums de TODAS las migraciones respecto a los .sql del repo.
--
-- Opción rápida (solo 1.136): el valor "checksum local" lo muestra el propio error de Flyway
-- en la línea "Resolved locally : XXXXX". Sustituí ? por ese número y ejecutá en TU_BD:

-- MySQL / MariaDB (reemplazá el checksum si tu error de Flyway muestra otro "Resolved locally"):
UPDATE flyway_schema_history
SET checksum = -831526342
WHERE version = '1.136'
  AND success = 1;

-- Ver fila:
-- SELECT installed_rank, version, description, script, checksum, success FROM flyway_schema_history WHERE version = '1.136';
