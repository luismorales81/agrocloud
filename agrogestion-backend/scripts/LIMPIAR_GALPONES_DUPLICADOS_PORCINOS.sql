-- Desactiva galpones duplicados por nombre dentro del mismo establecimiento (conserva el de menor id).
UPDATE porcinos_galpon g
INNER JOIN (
    SELECT establecimiento_id, LOWER(TRIM(nombre)) AS nombre_norm, MIN(id) AS id_conservar
    FROM porcinos_galpon
    WHERE activo = 1
    GROUP BY establecimiento_id, LOWER(TRIM(nombre))
    HAVING COUNT(*) > 1
) keeper
    ON g.establecimiento_id = keeper.establecimiento_id
    AND LOWER(TRIM(g.nombre)) = keeper.nombre_norm
    AND g.id <> keeper.id_conservar
SET g.activo = 0;
