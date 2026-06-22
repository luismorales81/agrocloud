-- Datos demo: lote feedlot con operaciones (empresa AgroCloud Demo, id=1)
-- Ejecutar después de HABILITAR_FEEDLOT_EMPRESA_DEMO.sql
-- QA en browser: login admin@agrocloud.com / admin123 → /feedlot/lotes/{id}
-- Idempotente por nombre de lote

SET @EMPRESA_ID := 1;
SET @NOMBRE_LOTE := 'Lote Demo Feedlot Mar 2026';

-- Resolver corral disponible o usar el primero
SET @CORRAL_ID := (
    SELECT c.id FROM feedlot_corral c
    INNER JOIN feedlot_establecimiento e ON e.id = c.establecimiento_id
    WHERE e.empresa_id = @EMPRESA_ID AND c.estado = 'DISPONIBLE'
    LIMIT 1
);

SET @CAMPANA_ID := (
    SELECT id FROM core_campanas WHERE empresa_id = @EMPRESA_ID AND estado = 'ACTIVA' LIMIT 1
);

SET @CATEGORIA_ID := (SELECT id FROM feedlot_categoria WHERE empresa_id = @EMPRESA_ID LIMIT 1);
SET @RAZA_ID := (SELECT id FROM feedlot_raza WHERE empresa_id = @EMPRESA_ID LIMIT 1);

-- Crear lote si no existe
INSERT INTO feedlot_lote (
    empresa_id, corral_id, campana_id, nombre, categoria_id, raza_id,
    tipo_tenencia, fecha_ingreso, cabezas_inicial, cabezas_actuales,
    peso_promedio_ingreso_kg, estado, created_at, updated_at
)
SELECT @EMPRESA_ID, @CORRAL_ID, @CAMPANA_ID, @NOMBRE_LOTE, @CATEGORIA_ID, @RAZA_ID,
       'PROPIO', DATE_SUB(CURDATE(), INTERVAL 90 DAY), 80, 78,
       275.00, 'ACTIVO', NOW(), NOW()
WHERE @CORRAL_ID IS NOT NULL
  AND @CATEGORIA_ID IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM feedlot_lote WHERE empresa_id = @EMPRESA_ID AND nombre = @NOMBRE_LOTE);

UPDATE feedlot_corral SET estado = 'OCUPADO'
WHERE id = @CORRAL_ID AND EXISTS (SELECT 1 FROM feedlot_lote WHERE nombre = @NOMBRE_LOTE AND corral_id = @CORRAL_ID);

SET @LOTE_ID := (SELECT id FROM feedlot_lote WHERE empresa_id = @EMPRESA_ID AND nombre = @NOMBRE_LOTE LIMIT 1);

-- Pesadas demo
INSERT INTO feedlot_pesada (lote_id, empresa_id, fecha, peso_promedio_kg, cabezas_muestreadas, created_at)
SELECT @LOTE_ID, @EMPRESA_ID, DATE_SUB(CURDATE(), INTERVAL 60 DAY), 310.00, 20, NOW()
WHERE @LOTE_ID IS NOT NULL AND NOT EXISTS (
    SELECT 1 FROM feedlot_pesada WHERE lote_id = @LOTE_ID AND fecha = DATE_SUB(CURDATE(), INTERVAL 60 DAY)
);

INSERT INTO feedlot_pesada (lote_id, empresa_id, fecha, peso_promedio_kg, cabezas_muestreadas, created_at)
SELECT @LOTE_ID, @EMPRESA_ID, DATE_SUB(CURDATE(), INTERVAL 30 DAY), 335.00, 20, NOW()
WHERE @LOTE_ID IS NOT NULL AND NOT EXISTS (
    SELECT 1 FROM feedlot_pesada WHERE lote_id = @LOTE_ID AND fecha = DATE_SUB(CURDATE(), INTERVAL 30 DAY)
);

-- Muerte demo (2 cabezas)
INSERT INTO feedlot_muerte (lote_id, empresa_id, fecha, cabezas, observaciones, created_at)
SELECT @LOTE_ID, @EMPRESA_ID, DATE_SUB(CURDATE(), INTERVAL 45 DAY), 2, 'Demo mortalidad', NOW()
WHERE @LOTE_ID IS NOT NULL AND NOT EXISTS (
    SELECT 1 FROM feedlot_muerte WHERE lote_id = @LOTE_ID AND cabezas = 2 AND observaciones = 'Demo mortalidad'
);

SELECT @LOTE_ID AS lote_demo_id, @NOMBRE_LOTE AS lote_nombre, 'OK demo lote' AS resultado;
