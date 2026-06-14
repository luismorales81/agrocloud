-- Ubicación y polígono del establecimiento de crianza (misma semántica que huevos / cultivos)
ALTER TABLE avicola_establecimiento
    ADD COLUMN ubicacion VARCHAR(500) NULL COMMENT 'Dirección o referencia textual' AFTER nombre,
    ADD COLUMN coordenadas JSON NULL COMMENT 'Puntos o polígono [{lat,lng},...]' AFTER ubicacion;
