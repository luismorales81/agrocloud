-- Días mínimos desde siembra y modo de avance por estado configurado
ALTER TABLE cultivo_estados_lote
    ADD COLUMN dias_minimos INT NULL COMMENT 'Días desde siembra para alcanzar este estado',
    ADD COLUMN modo_avance VARCHAR(20) NOT NULL DEFAULT 'MIXTO'
        COMMENT 'EVENTO, TIEMPO, TAREAS o MIXTO';

-- Valores sugeridos para plantilla Soja (tipo_cultivo_id = 1 si existe)
UPDATE cultivo_estados_lote e
    INNER JOIN cultivo_tipos_cultivo tc ON e.tipo_cultivo_id = tc.id
SET e.dias_minimos = CASE
        WHEN LOWER(REPLACE(e.nombre, ' ', '')) LIKE '%disponible%' THEN 0
        WHEN LOWER(REPLACE(e.nombre, ' ', '')) LIKE '%preparado%' THEN 0
        WHEN LOWER(REPLACE(e.nombre, ' ', '')) LIKE '%sembrado%' THEN 0
        WHEN LOWER(REPLACE(e.nombre, ' ', '')) LIKE '%emergencia%' OR LOWER(e.nombre) LIKE '%v2%' THEN 7
        WHEN LOWER(REPLACE(e.nombre, ' ', '')) LIKE '%establecimiento%' OR LOWER(e.nombre) LIKE '%v6%' OR LOWER(e.nombre) LIKE '%r1%' THEN 15
        WHEN LOWER(e.nombre) LIKE '%r3%' THEN 35
        WHEN LOWER(e.nombre) LIKE '%r6%' THEN 55
        WHEN LOWER(REPLACE(e.nombre, ' ', '')) LIKE '%flor%' THEN 45
        WHEN LOWER(REPLACE(e.nombre, ' ', '')) LIKE '%listo%' OR LOWER(REPLACE(e.nombre, ' ', '')) LIKE '%cosecha%' THEN 100
        ELSE NULL
    END,
    e.modo_avance = CASE
        WHEN LOWER(REPLACE(e.nombre, ' ', '')) LIKE '%disponible%' THEN 'EVENTO'
        WHEN LOWER(REPLACE(e.nombre, ' ', '')) LIKE '%sembrado%' THEN 'EVENTO'
        WHEN LOWER(REPLACE(e.nombre, ' ', '')) LIKE '%cosechado%' THEN 'EVENTO'
        WHEN LOWER(REPLACE(e.nombre, ' ', '')) LIKE '%emergencia%' OR LOWER(e.nombre) LIKE '%v2%' THEN 'TIEMPO'
        WHEN LOWER(REPLACE(e.nombre, ' ', '')) LIKE '%establecimiento%' OR LOWER(e.nombre) LIKE '%v6%' OR LOWER(e.nombre) LIKE '%r1%' THEN 'TIEMPO'
        WHEN LOWER(e.nombre) LIKE '%r3%' OR LOWER(e.nombre) LIKE '%r6%' THEN 'TIEMPO'
        WHEN LOWER(REPLACE(e.nombre, ' ', '')) LIKE '%flor%' THEN 'TIEMPO'
        WHEN LOWER(REPLACE(e.nombre, ' ', '')) LIKE '%listo%' THEN 'TIEMPO'
        ELSE 'MIXTO'
    END
WHERE tc.nombre IN ('Soja', 'Maíz', 'Trigo', 'Girasol', 'Sorgo', 'Alfalfa')
  AND e.empresa_id IS NULL;
