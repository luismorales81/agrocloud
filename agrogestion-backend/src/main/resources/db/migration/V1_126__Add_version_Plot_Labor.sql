-- Optimistic locking: columna version para cultivo_lotes y cultivo_labores
ALTER TABLE cultivo_lotes ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE cultivo_labores ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
