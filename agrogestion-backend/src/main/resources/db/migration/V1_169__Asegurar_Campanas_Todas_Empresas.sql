-- ============================================================================
-- V1_169: Campaña activa por defecto para empresas sin período (idempotente)
-- Corrige barra global "Sin período" cuando la empresa se creó después de V1_148.
-- ============================================================================

INSERT INTO core_campanas (empresa_id, codigo, nombre, fecha_inicio, fecha_fin, estado, es_default, created_at)
SELECT e.id,
       CONCAT(
           IF(MONTH(CURDATE()) >= 10, YEAR(CURDATE()), YEAR(CURDATE()) - 1),
           '-',
           LPAD(MOD(IF(MONTH(CURDATE()) >= 10, YEAR(CURDATE()) + 1, YEAR(CURDATE())), 100), 2, '0')
       ),
       CONCAT('Campaña ',
           IF(MONTH(CURDATE()) >= 10, YEAR(CURDATE()), YEAR(CURDATE()) - 1),
           '/',
           MOD(IF(MONTH(CURDATE()) >= 10, YEAR(CURDATE()) + 1, YEAR(CURDATE())), 100)
       ),
       DATE(CONCAT(IF(MONTH(CURDATE()) >= 10, YEAR(CURDATE()), YEAR(CURDATE()) - 1), '-10-01')),
       DATE(CONCAT(IF(MONTH(CURDATE()) >= 10, YEAR(CURDATE()) + 1, YEAR(CURDATE())), '-09-30')),
       'ACTIVA',
       1,
       NOW()
FROM empresas e
WHERE NOT EXISTS (
    SELECT 1 FROM core_campanas c WHERE c.empresa_id = e.id
);

SELECT 'V1_169: campañas por defecto aseguradas' AS mensaje;
