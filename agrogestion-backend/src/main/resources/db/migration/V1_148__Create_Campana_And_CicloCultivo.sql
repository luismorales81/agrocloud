-- Campaña transversal + CicloCultivo + columnas FK en módulos afectados

CREATE TABLE IF NOT EXISTS core_campanas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id BIGINT NOT NULL,
    codigo VARCHAR(20) NOT NULL,
    nombre VARCHAR(120) NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'BORRADOR',
    es_default TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_campana_empresa_codigo (empresa_id, codigo),
    KEY idx_campana_empresa_estado (empresa_id, estado),
    CONSTRAINT fk_campana_empresa FOREIGN KEY (empresa_id) REFERENCES empresas(id)
);

CREATE TABLE IF NOT EXISTS cultivo_ciclos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    campana_id BIGINT NOT NULL,
    lote_id BIGINT NOT NULL,
    cultivo_id BIGINT NOT NULL,
    superficie_hectareas DECIMAL(10,2) NOT NULL,
    fecha_siembra DATE NULL,
    fecha_cosecha DATE NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'PLANIFICADO',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_ciclo_campana (campana_id),
    KEY idx_ciclo_lote_campana (lote_id, campana_id),
    CONSTRAINT fk_ciclo_campana FOREIGN KEY (campana_id) REFERENCES core_campanas(id),
    CONSTRAINT fk_ciclo_lote FOREIGN KEY (lote_id) REFERENCES cultivo_lotes(id),
    CONSTRAINT fk_ciclo_cultivo FOREIGN KEY (cultivo_id) REFERENCES cultivo_cultivos(id)
);

-- cultivo_lotes: ciclo activo
SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.columns
     WHERE table_schema = DATABASE() AND table_name = 'cultivo_lotes' AND column_name = 'ciclo_activo_id') = 0,
    'ALTER TABLE cultivo_lotes ADD COLUMN ciclo_activo_id BIGINT NULL',
    'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- cultivo_labores: ciclo cultivo
SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.columns
     WHERE table_schema = DATABASE() AND table_name = 'cultivo_labores' AND column_name = 'ciclo_cultivo_id') = 0,
    'ALTER TABLE cultivo_labores ADD COLUMN ciclo_cultivo_id BIGINT NULL',
    'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- historial cosechas (nombre legacy o renombrado)
SET @tabla_historial = (
    SELECT IF(
        EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'cultivo_historial_cosechas'),
        'cultivo_historial_cosechas',
        'historial_cosechas'
    )
);

SET @col_exists = (
    SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = @tabla_historial AND column_name = 'ciclo_cultivo_id'
);
SET @sql = IF(@col_exists = 0,
    CONCAT('ALTER TABLE ', @tabla_historial, ' ADD COLUMN ciclo_cultivo_id BIGINT NULL'),
    'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ingresos / egresos (nombre legacy o renombrado cultivo_*)
SET @tabla_ingresos = (
    SELECT IF(
        EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'cultivo_ingresos'),
        'cultivo_ingresos',
        IF(
            EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'ingresos'),
            'ingresos',
            NULL
        )
    )
);
SET @col_exists = IF(@tabla_ingresos IS NULL, 1, (
    SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = @tabla_ingresos AND column_name = 'campana_id'
));
SET @sql = IF(@tabla_ingresos IS NULL OR @col_exists > 0, 'SELECT 1',
    CONCAT('ALTER TABLE ', @tabla_ingresos, ' ADD COLUMN campana_id BIGINT NULL'));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @tabla_egresos = (
    SELECT IF(
        EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'cultivo_egresos'),
        'cultivo_egresos',
        IF(
            EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'egresos'),
            'egresos',
            NULL
        )
    )
);
SET @col_exists = IF(@tabla_egresos IS NULL, 1, (
    SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = @tabla_egresos AND column_name = 'campana_id'
));
SET @sql = IF(@tabla_egresos IS NULL OR @col_exists > 0, 'SELECT 1',
    CONCAT('ALTER TABLE ', @tabla_egresos, ' ADD COLUMN campana_id BIGINT NULL'));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- inventario granos
SET @tabla_inventario = (
    SELECT IF(
        EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'cultivo_inventario_granos'),
        'cultivo_inventario_granos',
        'inventario_granos'
    )
);
SET @col_exists = (
    SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = @tabla_inventario AND column_name = 'campana_id'
);
SET @sql = IF(@col_exists = 0,
    CONCAT('ALTER TABLE ', @tabla_inventario, ' ADD COLUMN campana_id BIGINT NULL'),
    'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Porcinos (condicional)
SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'porcinos_recria' AND column_name = 'campana_id') = 0, 'ALTER TABLE porcinos_recria ADD COLUMN campana_id BIGINT NULL', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'porcinos_ventas_porcinos' AND column_name = 'campana_id') = 0, 'ALTER TABLE porcinos_ventas_porcinos ADD COLUMN campana_id BIGINT NULL', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'porcinos_consumos_diarios_automaticos' AND column_name = 'campana_id') = 0, 'ALTER TABLE porcinos_consumos_diarios_automaticos ADD COLUMN campana_id BIGINT NULL', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'porcinos_derrames_perdidas' AND column_name = 'campana_id') = 0, 'ALTER TABLE porcinos_derrames_perdidas ADD COLUMN campana_id BIGINT NULL', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'porcinos_faena' AND column_name = 'campana_id') = 0, 'ALTER TABLE porcinos_faena ADD COLUMN campana_id BIGINT NULL', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Avícola (condicional)
SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'avicola_lote' AND column_name = 'campana_id') = 0, 'ALTER TABLE avicola_lote ADD COLUMN campana_id BIGINT NULL', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'avicola_huevo_lote' AND column_name = 'campana_id') = 0, 'ALTER TABLE avicola_huevo_lote ADD COLUMN campana_id BIGINT NULL', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'avicola_ponedoras_galpon' AND column_name = 'campana_id') = 0, 'ALTER TABLE avicola_ponedoras_galpon ADD COLUMN campana_id BIGINT NULL', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Backfill: campaña activa por empresa (oct-sep año vigente)
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
WHERE e.activo = 1
  AND NOT EXISTS (
      SELECT 1 FROM core_campanas c WHERE c.empresa_id = e.id
  );

-- Backfill ciclos desde historial de cosechas
SET @sql = CONCAT('
INSERT INTO cultivo_ciclos (campana_id, lote_id, cultivo_id, superficie_hectareas, fecha_siembra, fecha_cosecha, estado, created_at)
SELECT c.id, hc.lote_id, hc.cultivo_id, hc.superficie_hectareas, hc.fecha_siembra, hc.fecha_cosecha, ''COSECHADO'', NOW()
FROM ', @tabla_historial, ' hc
INNER JOIN cultivo_lotes l ON l.id = hc.lote_id
INNER JOIN cultivo_campos f ON f.id = l.campo_id
INNER JOIN core_campanas c ON c.empresa_id = f.empresa_id AND c.estado = ''ACTIVA''
WHERE NOT EXISTS (
    SELECT 1 FROM cultivo_ciclos cc WHERE cc.lote_id = hc.lote_id AND cc.fecha_cosecha = hc.fecha_cosecha
)
');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Vincular historial a ciclo
SET @sql = CONCAT('
UPDATE ', @tabla_historial, ' hc
INNER JOIN cultivo_ciclos cc ON cc.lote_id = hc.lote_id AND cc.fecha_cosecha = hc.fecha_cosecha
SET hc.ciclo_cultivo_id = cc.id
WHERE hc.ciclo_cultivo_id IS NULL
');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Ciclos EN_CULTIVO para lotes con siembra vigente sin cosecha posterior
INSERT INTO cultivo_ciclos (campana_id, lote_id, cultivo_id, superficie_hectareas, fecha_siembra, estado, created_at)
SELECT c.id, l.id, cu.id, l.area_hectareas, l.fecha_siembra, 'EN_CULTIVO', NOW()
FROM cultivo_lotes l
INNER JOIN cultivo_campos f ON f.id = l.campo_id
INNER JOIN core_campanas c ON c.empresa_id = f.empresa_id AND c.estado = 'ACTIVA'
INNER JOIN cultivo_cultivos cu ON cu.id = COALESCE(l.cultivo_id, (
    SELECT c2.id FROM cultivo_cultivos c2
    WHERE c2.empresa_id = f.empresa_id AND c2.nombre = l.cultivo_actual LIMIT 1
))
WHERE l.fecha_siembra IS NOT NULL
  AND l.cultivo_actual IS NOT NULL
  AND (l.liberado_para_siembra IS NULL OR l.liberado_para_siembra = 0)
  AND NOT EXISTS (
      SELECT 1 FROM cultivo_ciclos cc
      WHERE cc.lote_id = l.id AND cc.estado IN ('PLANIFICADO', 'EN_CULTIVO')
  );

UPDATE cultivo_lotes l
INNER JOIN cultivo_ciclos cc ON cc.lote_id = l.id AND cc.estado IN ('PLANIFICADO', 'EN_CULTIVO')
SET l.ciclo_activo_id = cc.id
WHERE l.ciclo_activo_id IS NULL;

-- Labores al ciclo del lote en rango siembra-cosecha (best-effort)
UPDATE cultivo_labores lb
INNER JOIN cultivo_ciclos cc ON cc.lote_id = lb.lote_id
    AND lb.fecha_inicio >= IFNULL(cc.fecha_siembra, lb.fecha_inicio)
    AND (cc.fecha_cosecha IS NULL OR lb.fecha_inicio <= cc.fecha_cosecha)
SET lb.ciclo_cultivo_id = cc.id
WHERE lb.ciclo_cultivo_id IS NULL AND lb.lote_id IS NOT NULL;

SET @idx_exists = (
    SELECT COUNT(*) FROM information_schema.statistics
    WHERE table_schema = DATABASE() AND table_name = 'cultivo_labores' AND index_name = 'idx_labores_ciclo'
);
SET @sql = IF(@idx_exists = 0, 'CREATE INDEX idx_labores_ciclo ON cultivo_labores (ciclo_cultivo_id)', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
