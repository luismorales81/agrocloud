-- Ubicación y polígono del establecimiento (mismo criterio que cultivo_campos: JSON [{lat,lng},...])
ALTER TABLE avicola_huevo_establecimiento
    ADD COLUMN ubicacion VARCHAR(500) NULL COMMENT 'Dirección o referencia textual' AFTER nombre,
    ADD COLUMN coordenadas JSON NULL COMMENT 'Puntos o polígono en formato [{lat,lng},...]' AFTER ubicacion;
