-- ============================================================================
-- SCRIPT: Crear Baseline de Flyway para Migraciones Existentes
-- ============================================================================
-- Este script marca todas las migraciones existentes como ya ejecutadas
-- Útil cuando las tablas ya existen pero Flyway no tiene registro
-- 
-- Ejecutar: mysql -u root -p123456 agrocloud < scripts/fix-flyway-baseline.sql
-- ============================================================================

USE agrocloud;

-- Crear tabla flyway_schema_history si no existe
CREATE TABLE IF NOT EXISTS flyway_schema_history (
    installed_rank INT NOT NULL,
    version VARCHAR(50),
    description VARCHAR(200) NOT NULL,
    type VARCHAR(20) NOT NULL,
    script VARCHAR(1000) NOT NULL,
    checksum INT,
    installed_by VARCHAR(100) NOT NULL,
    installed_on TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    execution_time INT NOT NULL,
    success BOOLEAN NOT NULL,
    PRIMARY KEY (installed_rank)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insertar registros para migraciones que ya están ejecutadas
-- Solo insertar si no existen ya

-- V1.4 - Historial Cosechas
INSERT IGNORE INTO flyway_schema_history 
    (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success)
VALUES 
    (1, '1.4', 'Create HistorialCosechas Table', 'SQL', 'V1_4__Create_HistorialCosechas_Table.sql', NULL, 'root', NOW(), 0, TRUE);

-- V1.5 - Remove Humedad From HistorialCosechas
INSERT IGNORE INTO flyway_schema_history 
    (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success)
VALUES 
    (2, '1.5', 'Remove Humedad From HistorialCosechas', 'SQL', 'V1_5__Remove_Humedad_From_HistorialCosechas.sql', NULL, 'root', NOW(), 0, TRUE);

-- V1.6 - Create Labor Insumos Table
INSERT IGNORE INTO flyway_schema_history 
    (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success)
VALUES 
    (3, '1.6', 'Create Labor Insumos Table', 'SQL', 'V1_6__Create_Labor_Insumos_Table.sql', NULL, 'root', NOW(), 0, TRUE);

-- V1.7 - Create Movimientos Inventario Table
INSERT IGNORE INTO flyway_schema_history 
    (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success)
VALUES 
    (4, '1.7', 'Create Movimientos Inventario Table', 'SQL', 'V1_7__Create_Movimientos_Inventario_Table.sql', NULL, 'root', NOW(), 0, TRUE);

-- V1.8 - Update Lotes Table Estado Fields
INSERT IGNORE INTO flyway_schema_history 
    (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success)
VALUES 
    (5, '1.8', 'Update Lotes Table Estado Fields', 'SQL', 'V1_8__Update_Lotes_Table_Estado_Fields.sql', NULL, 'root', NOW(), 0, TRUE);

-- V1.9 - Add Responsable To Labores
INSERT IGNORE INTO flyway_schema_history 
    (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success)
VALUES 
    (6, '1.9', 'Add Responsable To Labores', 'SQL', 'V1_9__Add_Responsable_To_Labores.sql', NULL, 'root', NOW(), 0, TRUE);

-- V1.10 - Add Empresa To Lotes
INSERT IGNORE INTO flyway_schema_history 
    (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success)
VALUES 
    (7, '1.10', 'Add Empresa To Lotes', 'SQL', 'V1_10__Add_Empresa_To_Lotes.sql', NULL, 'root', NOW(), 0, TRUE);

-- V1.11 - Add Anulacion Fields To Labores
INSERT IGNORE INTO flyway_schema_history 
    (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success)
VALUES 
    (8, '1.11', 'Add Anulacion Fields To Labores', 'SQL', 'V1_11__Add_Anulacion_Fields_To_Labores.sql', NULL, 'root', NOW(), 0, TRUE);

-- V1.12 - Drop Cosechas Table
INSERT IGNORE INTO flyway_schema_history 
    (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success)
VALUES 
    (9, '1.12', 'Drop Cosechas Table', 'SQL', 'V1_12__Drop_Cosechas_Table.sql', NULL, 'root', NOW(), 0, TRUE);

-- V1.13 - Cleanup Unused Tables
INSERT IGNORE INTO flyway_schema_history 
    (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success)
VALUES 
    (10, '1.13', 'Cleanup Unused Tables', 'SQL', 'V1_13__Cleanup_Unused_Tables.sql', NULL, 'root', NOW(), 0, TRUE);

-- V1.15 - Create Inventario Granos Tables
INSERT IGNORE INTO flyway_schema_history 
    (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success)
VALUES 
    (11, '1.15', 'Create Inventario Granos Tables', 'SQL', 'V1_15__Create_Inventario_Granos_Tables.sql', NULL, 'root', NOW(), 0, TRUE);

-- V1.17 - Create Labor Maquinaria And Mano Obra Tables
INSERT IGNORE INTO flyway_schema_history 
    (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success)
VALUES 
    (12, '1.17', 'Create Labor Maquinaria And Mano Obra Tables', 'SQL', 'V1_17__Create_Labor_Maquinaria_And_Mano_Obra_Tables.sql', NULL, 'root', NOW(), 0, TRUE);

-- V1.18 - Alter Labores Costo Total Precision
INSERT IGNORE INTO flyway_schema_history 
    (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success)
VALUES 
    (13, '1.18', 'Alter Labores Costo Total Precision', 'SQL', 'V1_18__Alter_Labores_Costo_Total_Precision.sql', NULL, 'root', NOW(), 0, TRUE);

-- V1.19 - Remove Unidad From Dosis Insumos
INSERT IGNORE INTO flyway_schema_history 
    (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success)
VALUES 
    (14, '1.19', 'Remove Unidad From Dosis Insumos', 'SQL', 'V1_19__Remove_Unidad_From_Dosis_Insumos.sql', NULL, 'root', NOW(), 0, TRUE);

-- V1.20 - Create Movimientos Inventario Table
INSERT IGNORE INTO flyway_schema_history 
    (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success)
VALUES 
    (15, '1.20', 'Create Movimientos Inventario Table', 'SQL', 'V1_20__Create_Movimientos_Inventario_Table.sql', NULL, 'root', NOW(), 0, TRUE);

-- V1.21 - Create Movimientos Inventario Granos Table
INSERT IGNORE INTO flyway_schema_history 
    (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success)
VALUES 
    (16, '1.21', 'Create Movimientos Inventario Granos Table', 'SQL', 'V1_21__Create_Movimientos_Inventario_Granos_Table.sql', NULL, 'root', NOW(), 0, TRUE);

-- V1.22 - Create Weather Api Usage Table
INSERT IGNORE INTO flyway_schema_history 
    (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success)
VALUES 
    (17, '1.22', 'Create Weather Api Usage Table', 'SQL', 'V1_22__Create_Weather_Api_Usage_Table.sql', NULL, 'root', NOW(), 0, TRUE);

-- V1.23 - Add EULA Fields To Usuarios
INSERT IGNORE INTO flyway_schema_history 
    (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success)
VALUES 
    (18, '1.23', 'Add EULA Fields To Usuarios', 'SQL', 'V1_23__Add_EULA_Fields_To_Usuarios.sql', NULL, 'root', NOW(), 0, TRUE);

-- V1.24 - Create Recordatorios Table
INSERT IGNORE INTO flyway_schema_history 
    (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success)
VALUES 
    (19, '1.24', 'Create Recordatorios Table', 'SQL', 'V1_24__Create_Recordatorios_Table.sql', NULL, 'root', NOW(), 0, TRUE);

-- V1.25 - Create Porcinos Tables
INSERT IGNORE INTO flyway_schema_history 
    (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success)
VALUES 
    (20, '1.25', 'Create Porcinos Tables', 'SQL', 'V1_25__Create_Porcinos_Tables.sql', NULL, 'root', NOW(), 0, TRUE);

-- V1.26 - Add Activo To Porcinos Tables
INSERT IGNORE INTO flyway_schema_history 
    (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success)
VALUES 
    (21, '1.26', 'Add Activo To Porcinos Tables', 'SQL', 'V1_26__Add_Activo_To_Porcinos_Tables.sql', NULL, 'root', NOW(), 0, TRUE);

-- V1.27 - Create Muertes Tables
INSERT IGNORE INTO flyway_schema_history 
    (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success)
VALUES 
    (22, '1.27', 'Create Muertes Tables', 'SQL', 'V1_27__Create_Muertes_Tables.sql', NULL, 'root', NOW(), 0, TRUE);

-- V1.28 - Create Extended Porcinos Tables
INSERT IGNORE INTO flyway_schema_history 
    (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success)
VALUES 
    (23, '1.28', 'Create Extended Porcinos Tables', 'SQL', 'V1_28__Create_Extended_Porcinos_Tables.sql', NULL, 'root', NOW(), 0, TRUE);

-- V1.29 - Create Porcinos Config Tables
INSERT IGNORE INTO flyway_schema_history 
    (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success)
VALUES 
    (24, '1.29', 'Create Porcinos Config Tables', 'SQL', 'V1_29__Create_Porcinos_Config_Tables.sql', NULL, 'root', NOW(), 0, TRUE);

-- V1.30 - Create Configuracion Maestra Porcinos Tables (ya ejecutada manualmente)
INSERT IGNORE INTO flyway_schema_history 
    (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success)
VALUES 
    (25, '1.30', 'Create Configuracion Maestra Porcinos Tables', 'SQL', 'V1_30__Create_Configuracion_Maestra_Porcinos_Tables.sql', NULL, 'root', NOW(), 0, TRUE);

-- Verificar que se insertaron correctamente
SELECT 
    installed_rank,
    version,
    description,
    success,
    installed_on
FROM flyway_schema_history
ORDER BY installed_rank;

SELECT 
    '✅ Baseline de Flyway creado correctamente' AS resultado,
    COUNT(*) AS 'Migraciones registradas'
FROM flyway_schema_history;

