-- ============================================================================
-- Spec: Un destete por parto. Limpieza de duplicados y UNIQUE(parto_id).
-- Ejecutar en entorno con datos: revisar duplicados antes de añadir constraint.
-- ============================================================================

-- Para cada parto_id duplicado, dejar un destete (el de id mayor) y marcar el resto inactivos
UPDATE porcinos_destetes d1
INNER JOIN (
    SELECT parto_id, MAX(id) AS id_keep
    FROM porcinos_destetes
    WHERE activo = 1
    GROUP BY parto_id
    HAVING COUNT(*) > 1
) dup ON d1.parto_id = dup.parto_id AND d1.id <> dup.id_keep
SET d1.activo = 0;

-- Segunda pasada: si aún hubiera duplicados (mismo parto_id, varios activo=1), dejar solo uno
UPDATE porcinos_destetes d1
INNER JOIN (
    SELECT parto_id, MIN(id) AS id_keep
    FROM porcinos_destetes
    WHERE activo = 1
    GROUP BY parto_id
) one ON d1.parto_id = one.parto_id AND d1.id <> one.id_keep AND d1.activo = 1
SET d1.activo = 0;

-- Añadir constraint único
ALTER TABLE porcinos_destetes ADD CONSTRAINT uk_destetes_parto UNIQUE (parto_id);
