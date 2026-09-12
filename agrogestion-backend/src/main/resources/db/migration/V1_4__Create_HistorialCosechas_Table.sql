-- Migración V1.4: Crear tabla historial_cosechas (defensiva)
-- Compatible con BD vacía: crea la tabla sin FK si aún no existen lotes/cultivos/usuarios.
-- Las FK y vistas se agregan solo cuando las tablas referenciadas existen.

SET @esquema = DATABASE();

SET @tabla_lotes = (
    SELECT CASE
        WHEN EXISTS (SELECT 1 FROM information_schema.tables t WHERE t.table_schema = @esquema AND t.table_name = 'cultivo_lotes')
            THEN 'cultivo_lotes'
        WHEN EXISTS (SELECT 1 FROM information_schema.tables t WHERE t.table_schema = @esquema AND t.table_name = 'lotes')
            THEN 'lotes'
        ELSE NULL
    END
);

SET @tabla_cultivos = (
    SELECT CASE
        WHEN EXISTS (SELECT 1 FROM information_schema.tables t WHERE t.table_schema = @esquema AND t.table_name = 'cultivo_cultivos')
            THEN 'cultivo_cultivos'
        WHEN EXISTS (SELECT 1 FROM information_schema.tables t WHERE t.table_schema = @esquema AND t.table_name = 'cultivos')
            THEN 'cultivos'
        ELSE NULL
    END
);

SET @tabla_usuarios = (
    SELECT CASE
        WHEN EXISTS (SELECT 1 FROM information_schema.tables t WHERE t.table_schema = @esquema AND t.table_name = 'usuarios')
            THEN 'usuarios'
        WHEN EXISTS (SELECT 1 FROM information_schema.tables t WHERE t.table_schema = @esquema AND t.table_name = 'users')
            THEN 'users'
        ELSE NULL
    END
);

CREATE TABLE IF NOT EXISTS historial_cosechas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    lote_id BIGINT NOT NULL,
    cultivo_id BIGINT NOT NULL,
    fecha_siembra DATE NOT NULL,
    fecha_cosecha DATE NOT NULL,
    superficie_hectareas DECIMAL(10,2) NOT NULL,
    cantidad_cosechada DECIMAL(10,2) NOT NULL,
    unidad_cosecha VARCHAR(10) NOT NULL,
    rendimiento_real DECIMAL(10,2),
    rendimiento_esperado DECIMAL(10,2),
    humedad_cosecha DECIMAL(5,2),
    variedad_semilla VARCHAR(100),
    observaciones TEXT,
    estado_suelo VARCHAR(50) DEFAULT 'BUENO',
    requiere_descanso BOOLEAN DEFAULT FALSE,
    dias_descanso_recomendados INT DEFAULT 0,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    usuario_id BIGINT NOT NULL,
    CONSTRAINT chk_historial_superficie_positiva CHECK (superficie_hectareas > 0),
    CONSTRAINT chk_historial_cantidad_positiva CHECK (cantidad_cosechada > 0),
    CONSTRAINT chk_historial_fecha_siembra_antes_cosecha CHECK (fecha_siembra <= fecha_cosecha),
    CONSTRAINT chk_historial_humedad_valida CHECK (humedad_cosecha IS NULL OR (humedad_cosecha >= 0 AND humedad_cosecha <= 100)),
    CONSTRAINT chk_historial_dias_descanso_positivos CHECK (dias_descanso_recomendados >= 0),
    CONSTRAINT chk_historial_estado_suelo_valido CHECK (estado_suelo IN ('BUENO', 'DESCANSANDO', 'AGOTADO'))
);

SET @sql = IF(
    @tabla_lotes IS NULL,
    'SELECT 1',
    IF((SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
        WHERE CONSTRAINT_SCHEMA = @esquema AND TABLE_NAME = 'historial_cosechas' AND CONSTRAINT_NAME = 'fk_historial_cosechas_lote') > 0,
        'SELECT 1',
        CONCAT('ALTER TABLE historial_cosechas ADD CONSTRAINT fk_historial_cosechas_lote FOREIGN KEY (lote_id) REFERENCES ', @tabla_lotes, '(id) ON DELETE CASCADE')
    )
);
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = IF(
    @tabla_cultivos IS NULL,
    'SELECT 1',
    IF((SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
        WHERE CONSTRAINT_SCHEMA = @esquema AND TABLE_NAME = 'historial_cosechas' AND CONSTRAINT_NAME = 'fk_historial_cosechas_cultivo') > 0,
        'SELECT 1',
        CONCAT('ALTER TABLE historial_cosechas ADD CONSTRAINT fk_historial_cosechas_cultivo FOREIGN KEY (cultivo_id) REFERENCES ', @tabla_cultivos, '(id) ON DELETE CASCADE')
    )
);
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = IF(
    @tabla_usuarios IS NULL,
    'SELECT 1',
    IF((SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
        WHERE CONSTRAINT_SCHEMA = @esquema AND TABLE_NAME = 'historial_cosechas' AND CONSTRAINT_NAME = 'fk_historial_cosechas_usuario') > 0,
        'SELECT 1',
        CONCAT('ALTER TABLE historial_cosechas ADD CONSTRAINT fk_historial_cosechas_usuario FOREIGN KEY (usuario_id) REFERENCES ', @tabla_usuarios, '(id) ON DELETE CASCADE')
    )
);
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = @esquema AND table_name = 'historial_cosechas' AND index_name = 'idx_historial_cosechas_lote_id') > 0,
    'SELECT 1',
    'CREATE INDEX idx_historial_cosechas_lote_id ON historial_cosechas(lote_id)'
);
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = @esquema AND table_name = 'historial_cosechas' AND index_name = 'idx_historial_cosechas_cultivo_id') > 0,
    'SELECT 1',
    'CREATE INDEX idx_historial_cosechas_cultivo_id ON historial_cosechas(cultivo_id)'
);
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = @esquema AND table_name = 'historial_cosechas' AND index_name = 'idx_historial_cosechas_usuario_id') > 0,
    'SELECT 1',
    'CREATE INDEX idx_historial_cosechas_usuario_id ON historial_cosechas(usuario_id)'
);
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- Vistas solo si existen todas las tablas referenciadas
SET @sql = IF(
    @tabla_lotes IS NULL OR @tabla_cultivos IS NULL OR @tabla_usuarios IS NULL,
    'SELECT 1',
    CONCAT('CREATE OR REPLACE VIEW vista_historial_cosechas_completo AS SELECT hc.id, hc.lote_id, l.nombre AS lote_nombre, hc.cultivo_id, c.nombre AS cultivo_nombre, c.variedad AS cultivo_variedad, hc.fecha_siembra, hc.fecha_cosecha, DATEDIFF(hc.fecha_cosecha, hc.fecha_siembra) AS dias_ciclo, hc.superficie_hectareas, hc.cantidad_cosechada, hc.unidad_cosecha, hc.rendimiento_real, hc.rendimiento_esperado, CASE WHEN hc.rendimiento_esperado > 0 THEN ROUND((hc.rendimiento_real / hc.rendimiento_esperado) * 100, 2) ELSE 0 END AS porcentaje_cumplimiento, hc.humedad_cosecha, hc.variedad_semilla, hc.observaciones, hc.estado_suelo, hc.requiere_descanso, hc.dias_descanso_recomendados, hc.fecha_creacion, hc.fecha_actualizacion, hc.usuario_id, u.email AS usuario_email FROM historial_cosechas hc JOIN ', @tabla_lotes, ' l ON hc.lote_id = l.id JOIN ', @tabla_cultivos, ' c ON hc.cultivo_id = c.id JOIN ', @tabla_usuarios, ' u ON hc.usuario_id = u.id')
);
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SELECT 'Migración V1_4 historial_cosechas aplicada (modo defensivo)' AS mensaje;
