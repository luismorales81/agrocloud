-- Checksum mismatch en V1_139: el .sql del repo cambió después de aplicarse en la BD.
-- Preferí: desde agrogestion-backend → .\scripts\ejecutar-flyway-repair.ps1
--
-- Arreglo manual (valor = "Resolved locally" del error de Flyway):
UPDATE flyway_schema_history
SET checksum = -2049210628
WHERE version = '1.139'
  AND success = 1;

-- Verificación:
-- SELECT version, script, checksum FROM flyway_schema_history WHERE version = '1.139';
