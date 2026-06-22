-- V1_151: Desactiva transiciones duplicadas lógicas (mismo tipo de cultivo, empresa y par origen→destino por nombre).
-- Causa habitual: scripts con CROSS JOIN sobre estados duplicados generan varias filas para el mismo par.

UPDATE cultivo_transiciones_estado t
INNER JOIN cultivo_estados_lote eo ON t.estado_origen_id = eo.id
INNER JOIN cultivo_estados_lote ed ON t.estado_destino_id = ed.id
LEFT JOIN (
    SELECT MIN(t2.id) AS id_conservar,
           t2.tipo_cultivo_id,
           IFNULL(t2.empresa_id, -1) AS empresa_key,
           LOWER(TRIM(eo2.nombre)) AS origen_nombre,
           LOWER(TRIM(ed2.nombre)) AS destino_nombre
    FROM cultivo_transiciones_estado t2
    INNER JOIN cultivo_estados_lote eo2 ON t2.estado_origen_id = eo2.id
    INNER JOIN cultivo_estados_lote ed2 ON t2.estado_destino_id = ed2.id
    WHERE t2.activo = TRUE
    GROUP BY t2.tipo_cultivo_id, IFNULL(t2.empresa_id, -1), LOWER(TRIM(eo2.nombre)), LOWER(TRIM(ed2.nombre))
) conservar
    ON t.tipo_cultivo_id = conservar.tipo_cultivo_id
    AND IFNULL(t.empresa_id, -1) = conservar.empresa_key
    AND LOWER(TRIM(eo.nombre)) = conservar.origen_nombre
    AND LOWER(TRIM(ed.nombre)) = conservar.destino_nombre
    AND t.id = conservar.id_conservar
SET t.activo = FALSE
WHERE t.activo = TRUE
  AND conservar.id_conservar IS NULL;
