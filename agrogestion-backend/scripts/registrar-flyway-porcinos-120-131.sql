-- Registrar en Flyway las migraciones porcinos ejecutadas a mano (V1_120, V1_128 a V1_131)
-- Ejecutar: mysql -u root -p123456 agrocloud < scripts/registrar-flyway-porcinos-120-131.sql
USE agrocloud;

SET @r = (SELECT COALESCE(MAX(installed_rank), 0) FROM flyway_schema_history);

INSERT INTO flyway_schema_history (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success)
VALUES
(@r+1, '1.120', 'Add Origen To Porcinos Recria', 'SQL', 'V1_120__Add_Origen_To_Porcinos_Recria.sql', NULL, 'root', NOW(), 0, 1),
(@r+2, '1.128', 'Porcinos Limpieza Destetes Duplicados Y Unique Parto', 'SQL', 'V1_128__Porcinos_Limpieza_Destetes_Duplicados_Y_Unique_Parto.sql', NULL, 'root', NOW(), 0, 1),
(@r+3, '1.129', 'Porcinos Parto Gestacion Id Recria Check Origen', 'SQL', 'V1_129__Porcinos_Parto_Gestacion_Id_Recria_Check_Origen.sql', NULL, 'root', NOW(), 0, 1),
(@r+4, '1.130', 'Porcinos Recria Version', 'SQL', 'V1_130__Porcinos_Recria_Version.sql', NULL, 'root', NOW(), 0, 1),
(@r+5, '1.131', 'Porcinos Recria Version Not Null', 'SQL', 'V1_131__Porcinos_Recria_Version_NotNull.sql', NULL, 'root', NOW(), 0, 1);
