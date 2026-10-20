ALTER TABLE feedlot_sesion_manga_fila
    ADD COLUMN origen_recuento VARCHAR(20) NULL,
    ADD COLUMN accion_desconocido VARCHAR(30) NULL,
    ADD COLUMN lote_destino_id BIGINT NULL;
