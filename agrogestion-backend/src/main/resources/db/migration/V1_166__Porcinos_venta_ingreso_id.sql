-- Columna ingreso_id referenciada por PorcinosVenta (alineado con feedlot_venta).

SET @esquema = DATABASE();

SET @consulta = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_schema = @esquema AND table_name = 'porcinos_venta' AND column_name = 'ingreso_id'
        ),
        'SELECT 1',
        'ALTER TABLE porcinos_venta ADD COLUMN ingreso_id BIGINT NULL AFTER comprador'
    )
);
PREPARE stmt FROM @consulta;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
