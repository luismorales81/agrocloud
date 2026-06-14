-- ============================================================================
-- SCRIPT PARA EJECUTAR MANUALMENTE EN MYSQL
-- ============================================================================
-- Copia y pega este script en MySQL Workbench o ejecuta:
-- mysql -u root -p123456 agrocloud < EJECUTAR_ESTE_SCRIPT.sql
-- ============================================================================

USE agrocloud;

-- ============================================================================
-- PROCEDIMIENTO PARA AGREGAR COLUMNA SI NO EXISTE
-- ============================================================================

DELIMITER $$

DROP PROCEDURE IF EXISTS AgregarColumnaSiNoExiste$$

CREATE PROCEDURE AgregarColumnaSiNoExiste(
    IN p_table_name VARCHAR(255),
    IN p_column_name VARCHAR(255),
    IN p_column_definition TEXT
)
BEGIN
    DECLARE column_exists INT DEFAULT 0;
    
    SELECT COUNT(*) INTO column_exists
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'agrocloud'
      AND TABLE_NAME = p_table_name
      AND COLUMN_NAME = p_column_name;
    
    IF column_exists = 0 THEN
        SET @sql = CONCAT('ALTER TABLE ', p_table_name, ' ADD COLUMN ', p_column_name, ' ', p_column_definition);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
        SELECT CONCAT('Columna ', p_column_name, ' agregada a ', p_table_name) AS resultado;
    ELSE
        SELECT CONCAT('Columna ', p_column_name, ' ya existe en ', p_table_name) AS resultado;
    END IF;
END$$

DELIMITER ;

-- ============================================================================
-- AGREGAR COLUMNAS A porcinos_parametros_productivos_porcinos
-- ============================================================================

CALL AgregarColumnaSiNoExiste('porcinos_parametros_productivos_porcinos', 
    'dias_tolerancia_vencimiento_gestacion', 
    'INT NULL COMMENT ''Días de tolerancia para el vencimiento de gestación''');

CALL AgregarColumnaSiNoExiste('porcinos_parametros_productivos_porcinos', 
    'dias_control_celo', 
    'INT NULL COMMENT ''Días de control de celo''');

CALL AgregarColumnaSiNoExiste('porcinos_parametros_productivos_porcinos', 
    'dias_entre_celos', 
    'INT NULL COMMENT ''Días entre celos''');

CALL AgregarColumnaSiNoExiste('porcinos_parametros_productivos_porcinos', 
    'dias_pasaje_maternidad', 
    'INT NULL COMMENT ''Días antes del parto para pasaje a maternidad''');

CALL AgregarColumnaSiNoExiste('porcinos_parametros_productivos_porcinos', 
    'peso_promedio_nacimiento', 
    'DECIMAL(10,2) NULL COMMENT ''Peso promedio al nacer (kg)''');

CALL AgregarColumnaSiNoExiste('porcinos_parametros_productivos_porcinos', 
    'peso_destete_objetivo', 
    'DECIMAL(10,2) NULL COMMENT ''Peso objetivo al destete (kg)''');

CALL AgregarColumnaSiNoExiste('porcinos_parametros_productivos_porcinos', 
    'peso_venta_objetivo', 
    'DECIMAL(10,2) NULL COMMENT ''Peso objetivo para venta (kg)''');

CALL AgregarColumnaSiNoExiste('porcinos_parametros_productivos_porcinos', 
    'lechones_vivos_parto_objetivo', 
    'DECIMAL(5,2) NULL COMMENT ''Lechones vivos por parto objetivo''');

CALL AgregarColumnaSiNoExiste('porcinos_parametros_productivos_porcinos', 
    'lechones_destetados_objetivo', 
    'DECIMAL(5,2) NULL COMMENT ''Lechones destetados objetivo''');

CALL AgregarColumnaSiNoExiste('porcinos_parametros_productivos_porcinos', 
    'partos_madre_anio_objetivo', 
    'DECIMAL(5,2) NULL COMMENT ''Partos por madre por año objetivo''');

-- ============================================================================
-- AGREGAR COLUMNAS FALTANTES A TABLA porcinos_partos
-- ============================================================================

CALL AgregarColumnaSiNoExiste('porcinos_partos', 
    'intervenciones', 
    'TEXT NULL COMMENT ''Intervenciones realizadas durante el parto''');

CALL AgregarColumnaSiNoExiste('porcinos_partos', 
    'tipo_parto_id', 
    'BIGINT NULL');

-- ============================================================================
-- AGREGAR FOREIGN KEY PARA tipo_parto_id SI NO EXISTE
-- Solo se agrega si la tabla porcinos_tipos_parto existe
-- ============================================================================

-- Verificar si la tabla porcinos_tipos_parto existe
SET @table_exists := (SELECT COUNT(*) FROM information_schema.TABLES 
                      WHERE TABLE_SCHEMA = 'agrocloud' 
                      AND TABLE_NAME = 'porcinos_tipos_parto');

-- Verificar si la foreign key ya existe
SET @fk_exists := (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS 
                   WHERE CONSTRAINT_SCHEMA = 'agrocloud' 
                   AND TABLE_NAME = 'porcinos_partos' 
                   AND CONSTRAINT_NAME = 'fk_parto_tipo_parto');

-- Solo crear la foreign key si la tabla existe y la FK no existe
SET @sqlstmt := IF(@table_exists > 0 AND @fk_exists = 0, 
    'ALTER TABLE porcinos_partos ADD CONSTRAINT fk_parto_tipo_parto FOREIGN KEY (tipo_parto_id) REFERENCES porcinos_tipos_parto(id) ON DELETE SET NULL',
    IF(@table_exists = 0,
        'SELECT ''ADVERTENCIA: La tabla porcinos_tipos_parto no existe. La foreign key no se puede crear. Ejecute primero la migración V1_102.'' AS mensaje',
        'SELECT ''La foreign key fk_parto_tipo_parto ya existe'' AS mensaje'));

PREPARE stmt FROM @sqlstmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ============================================================================
-- LIMPIAR PROCEDIMIENTO TEMPORAL
-- ============================================================================

DROP PROCEDURE IF EXISTS AgregarColumnaSiNoExiste;

-- Verificar que las columnas fueron agregadas
SELECT 
    COLUMN_NAME as 'Columna',
    DATA_TYPE as 'Tipo',
    IS_NULLABLE as 'Permite NULL',
    COLUMN_COMMENT as 'Comentario'
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'agrocloud' 
  AND TABLE_NAME = 'porcinos_parametros_productivos_porcinos'
  AND COLUMN_NAME IN (
    'dias_tolerancia_vencimiento_gestacion',
    'dias_control_celo',
    'dias_entre_celos',
    'dias_pasaje_maternidad',
    'peso_promedio_nacimiento',
    'peso_destete_objetivo',
    'peso_venta_objetivo',
    'lechones_vivos_parto_objetivo',
    'lechones_destetados_objetivo',
    'partos_madre_anio_objetivo'
  )
ORDER BY COLUMN_NAME;
