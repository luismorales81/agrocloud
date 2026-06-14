-- Alineación esquema ponedoras con entidades JPA (V1_141 + campos galpón, postura simplificada, venta por categoría).
-- Idempotencia limitada: Flyway ejecuta una vez por entorno.

-- Galpón: explotación compartida (avicola_establecimiento) y raza textual
ALTER TABLE avicola_ponedoras_galpon
    ADD COLUMN establecimiento_id BIGINT NULL COMMENT 'FK explotación avícola (módulo crianza)' AFTER empresa_id,
    ADD COLUMN raza VARCHAR(120) NOT NULL DEFAULT '' COMMENT 'Línea o raza comercial' AFTER nombre;

ALTER TABLE avicola_ponedoras_galpon
    ADD CONSTRAINT fk_ponedoras_galpon_establecimiento FOREIGN KEY (establecimiento_id) REFERENCES avicola_establecimiento (id) ON DELETE RESTRICT;

ALTER TABLE avicola_ponedoras_galpon
    CHANGE COLUMN fecha_inicio_ciclo fecha_ingreso DATE NOT NULL COMMENT 'Fecha de ingreso al galpón',
    CHANGE COLUMN cantidad_aves_inicial cantidad_inicial INT NOT NULL COMMENT 'Aves al inicio del ciclo',
    CHANGE COLUMN cantidad_aves_actual cantidad_aves INT NULL COMMENT 'Stock vivo actual (descartes / negocio)';

-- Postura: una fecha por registro (sin periodo DIARIO/SEMANAL en modelo)
ALTER TABLE avicola_ponedoras_postura DROP COLUMN periodo_tipo;
ALTER TABLE avicola_ponedoras_postura
    CHANGE COLUMN fecha_periodo fecha DATE NOT NULL COMMENT 'Día de la producción';

-- Venta de huevos: categoría de calidad + columna cantidad
ALTER TABLE avicola_ponedoras_venta_huevos
    ADD COLUMN categoria_huevo VARCHAR(40) NOT NULL DEFAULT 'A' AFTER fecha;
ALTER TABLE avicola_ponedoras_venta_huevos
    CHANGE COLUMN cantidad_huevos cantidad INT NOT NULL COMMENT 'Cantidad de huevos';

-- Descarte: motivo acotado a códigos de enum de aplicación
ALTER TABLE avicola_ponedoras_descarte_aves
    MODIFY COLUMN motivo VARCHAR(40) NOT NULL COMMENT 'LIQUIDACION | BAJA_PRODUCTIVA | OTRO';
