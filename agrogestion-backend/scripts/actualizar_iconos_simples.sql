-- Actualizar iconos con caracteres simples que funcionan bien
UPDATE cultivo_estados_lote SET icono = '●' WHERE nombre = 'Disponible' AND tipo_cultivo_id = 1;
UPDATE cultivo_estados_lote SET icono = '○' WHERE nombre = 'Preparado' AND tipo_cultivo_id = 1;
UPDATE cultivo_estados_lote SET icono = '■' WHERE nombre = 'Sembrado' AND tipo_cultivo_id = 1;
UPDATE cultivo_estados_lote SET icono = '▲' WHERE nombre = 'Emergencia' AND tipo_cultivo_id = 1;
UPDATE cultivo_estados_lote SET icono = '◆' WHERE nombre = 'R3' AND tipo_cultivo_id = 1;
UPDATE cultivo_estados_lote SET icono = '★' WHERE nombre = 'R6' AND tipo_cultivo_id = 1;
UPDATE cultivo_estados_lote SET icono = '□' WHERE nombre = 'Listo para Cosecha' AND tipo_cultivo_id = 1;
UPDATE cultivo_estados_lote SET icono = '✓' WHERE nombre = 'Cosechado' AND tipo_cultivo_id = 1;













