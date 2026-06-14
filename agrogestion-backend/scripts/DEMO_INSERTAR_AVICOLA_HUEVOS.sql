-- =============================================================================
-- Datos de demostración — módulo Avícola huevos (postura)
-- Ejecutar en MySQL contra la base del entorno de demo.
-- Ajustar @EMPRESA_ID al id de la empresa que usará el cliente en la demo.
-- Requisitos: migraciones V1_137+ aplicadas; módulo AVICOLA_HUEVOS habilitado
-- para esa empresa (company_modules / migración V1_139).
-- =============================================================================

SET @EMPRESA_ID = 1;

INSERT INTO avicola_huevo_establecimiento (empresa_id, nombre, observaciones, activo)
SELECT @EMPRESA_ID, 'Establecimiento demo — postura', 'Carga demo script', TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM avicola_huevo_establecimiento e WHERE e.empresa_id = @EMPRESA_ID AND e.nombre = 'Establecimiento demo — postura'
);

SET @EST_ID := (SELECT id FROM avicola_huevo_establecimiento WHERE empresa_id = @EMPRESA_ID AND nombre = 'Establecimiento demo — postura' LIMIT 1);

INSERT INTO avicola_huevo_raza (empresa_id, nombre, activo)
SELECT @EMPRESA_ID, 'Línea ponedora demo', TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM avicola_huevo_raza r WHERE r.empresa_id = @EMPRESA_ID AND r.nombre = 'Línea ponedora demo'
);

SET @RAZA_ID := (SELECT id FROM avicola_huevo_raza WHERE empresa_id = @EMPRESA_ID AND nombre = 'Línea ponedora demo' LIMIT 1);

INSERT INTO avicola_huevo_lote (
    empresa_id, establecimiento_id, raza_id, nombre, fecha_inicio,
    cantidad_aves_inicial, cantidad_aves_actual, estado, observaciones
)
SELECT
    @EMPRESA_ID,
    @EST_ID,
    @RAZA_ID,
    'Lote demo postura',
    CURDATE() - INTERVAL 14 DAY,
    5000,
    4980,
    'ACTIVO',
    'Generado por DEMO_INSERTAR_AVICOLA_HUEVOS.sql'
WHERE NOT EXISTS (
    SELECT 1 FROM avicola_huevo_lote l WHERE l.empresa_id = @EMPRESA_ID AND l.nombre = 'Lote demo postura'
);

SET @LOTE_ID := (SELECT id FROM avicola_huevo_lote WHERE empresa_id = @EMPRESA_ID AND nombre = 'Lote demo postura' LIMIT 1);

-- Producción de ejemplo (últimos 3 días; si ya existen filas para esas fechas, no duplicar por UK lote+fecha)
INSERT INTO avicola_huevo_produccion_diaria (lote_id, empresa_id, fecha, cantidad_huevos, observaciones)
SELECT l.id, l.empresa_id, CURDATE() - INTERVAL 2 DAY, 4520, 'Demo'
FROM avicola_huevo_lote l
WHERE l.id = @LOTE_ID
  AND NOT EXISTS (
      SELECT 1 FROM avicola_huevo_produccion_diaria p
      WHERE p.lote_id = l.id AND p.empresa_id = l.empresa_id AND p.fecha = CURDATE() - INTERVAL 2 DAY
  );

INSERT INTO avicola_huevo_produccion_diaria (lote_id, empresa_id, fecha, cantidad_huevos, observaciones)
SELECT l.id, l.empresa_id, CURDATE() - INTERVAL 1 DAY, 4610, 'Demo'
FROM avicola_huevo_lote l
WHERE l.id = @LOTE_ID
  AND NOT EXISTS (
      SELECT 1 FROM avicola_huevo_produccion_diaria p
      WHERE p.lote_id = l.id AND p.empresa_id = l.empresa_id AND p.fecha = CURDATE() - INTERVAL 1 DAY
  );

INSERT INTO avicola_huevo_produccion_diaria (lote_id, empresa_id, fecha, cantidad_huevos, observaciones)
SELECT l.id, l.empresa_id, CURDATE(), 4550, 'Demo — hoy'
FROM avicola_huevo_lote l
WHERE l.id = @LOTE_ID
  AND NOT EXISTS (
      SELECT 1 FROM avicola_huevo_produccion_diaria p
      WHERE p.lote_id = l.id AND p.empresa_id = l.empresa_id AND p.fecha = CURDATE()
  );
