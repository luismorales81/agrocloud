-- Eliminar tabla legacy de agroquímicos (ya integrado en insumos)
-- Ejecutar en MySQL

SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS;
SET FOREIGN_KEY_CHECKS = 0;

-- Quitar posibles FKs que referencien a agroquimicos (defensivo)
-- NOTA: Ajustar si tu esquema tiene restricciones específicas nombradas
-- Este bloque es seguro si no existen
-- ALTER TABLE alguna_tabla DROP FOREIGN KEY fk_alguna;

DROP TABLE IF EXISTS agroquimicos;

SET FOREIGN_KEY_CHECKS = @OLD_FOREIGN_KEY_CHECKS;







