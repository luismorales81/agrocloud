-- Script de migración para agregar columnas de polígono y coordenadas
-- Ejecutar en la base de datos MySQL existente

USE agroclouddb;

-- Agregar columna para almacenar el polígono del campo (GeoJSON)
ALTER TABLE campos 
ADD COLUMN poligono TEXT NULL COMMENT 'Polígono del campo en formato GeoJSON';

-- Agregar columna para almacenar las coordenadas del campo
ALTER TABLE campos 
ADD COLUMN coordenadas JSON NULL COMMENT 'Coordenadas del campo como array de puntos lat/lng';

-- Agregar índices para mejorar el rendimiento de búsquedas
CREATE INDEX idx_campos_poligono ON campos(poligono(100));
CREATE INDEX idx_campos_coordenadas ON campos(coordenadas(100));

-- Comentario sobre la migración
-- Esta migración agrega soporte para almacenar la geometría de los campos
-- permitiendo dibujar polígonos en Google Maps y almacenar las coordenadas
-- para uso en otros sistemas de gestión agrícola.
