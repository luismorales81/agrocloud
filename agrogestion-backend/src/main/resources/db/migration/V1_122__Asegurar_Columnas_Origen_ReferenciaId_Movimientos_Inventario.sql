-- Asegurar que cultivo_movimientos_inventario tenga origen y referencia_id (evita AssertionFailure al crear labores).
-- Idempotente: solo agrega columnas si no existen (p. ej. si V1_119 no se ejecutó o la BD se restauró sin ella).

DELIMITER //
DROP PROCEDURE IF EXISTS agregar_origen_referencia_id_si_faltan//
CREATE PROCEDURE agregar_origen_referencia_id_si_faltan()
BEGIN
  IF (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'cultivo_movimientos_inventario'
        AND COLUMN_NAME = 'origen') = 0 THEN
    ALTER TABLE cultivo_movimientos_inventario ADD COLUMN origen VARCHAR(50) NULL;
  END IF;
  IF (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'cultivo_movimientos_inventario'
        AND COLUMN_NAME = 'referencia_id') = 0 THEN
    ALTER TABLE cultivo_movimientos_inventario ADD COLUMN referencia_id BIGINT NULL;
  END IF;
END//
DELIMITER ;
CALL agregar_origen_referencia_id_si_faltan();
DROP PROCEDURE IF EXISTS agregar_origen_referencia_id_si_faltan;
