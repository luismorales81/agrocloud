-- =============================================================================
-- Flyway: quitar migraciones FALLIDAS (success = 0) del historial para reintentar
-- Base: la misma que usa la app (por defecto agrocloud). Luego COMMIT en Workbench.
-- =============================================================================
-- Si ademas editaste V1_5 / V1_99 en el repo, ejecuta tambien: mvn flyway:repair
-- (script PowerShell: scripts/ejecutar-flyway-repair.ps1) para alinear checksums.
-- =============================================================================

USE agrocloud;

-- Todas las migraciones fallidas (revision)
SELECT installed_rank, version, description, script, success, installed_on
FROM flyway_schema_history
WHERE success = 0
ORDER BY installed_rank;

-- Quitar intentos fallidos de versiones que suelen quedar a medias en local
-- (incluye 1.102: catálogos porcinos; error típico "Cannot resolve entityManagerFactory" viene de aquí)
DELETE FROM flyway_schema_history
WHERE success = 0
  AND version IN ('1.5', '1.99', '1.100', '1.102');

-- Por script exacto (si version no coincide con el patron anterior):
-- DELETE FROM flyway_schema_history
-- WHERE success = 0 AND script IN (
--   'V1_5__Remove_Humedad_From_HistorialCosechas.sql',
--   'V1_99__Rename_Tables_By_Module.sql'
-- );

-- Opcion nuclear: borrar TODAS las filas fallidas (solo si sabes lo que haces)
-- DELETE FROM flyway_schema_history WHERE success = 0;

-- Comprobar que no queden fallidas para esas versiones
SELECT version, COUNT(*) AS cantidad_fallidas
FROM flyway_schema_history
WHERE success = 0 AND version IN ('1.5', '1.99', '1.100', '1.102')
GROUP BY version;

-- Workbench: COMMIT;
