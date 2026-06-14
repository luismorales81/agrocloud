-- ============================================================================
-- Spec: Asegurar que version en porcinos_recria sea NOT NULL para concurrencia.
-- ============================================================================

UPDATE porcinos_recria SET version = 0 WHERE version IS NULL;
ALTER TABLE porcinos_recria MODIFY COLUMN version BIGINT NOT NULL DEFAULT 0;
