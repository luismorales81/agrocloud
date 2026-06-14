-- Script para corregir los iconos de los estados de cultivos
USE agrocloud;

-- Configurar charset para emojis
SET NAMES utf8mb4;

-- Corregir iconos para Soja
UPDATE cultivo_estados_lote 
SET icono = '🟢' 
WHERE nombre = 'Disponible' AND tipo_cultivo_id = 1;

UPDATE cultivo_estados_lote 
SET icono = '🟡' 
WHERE nombre = 'Preparado' AND tipo_cultivo_id = 1;

UPDATE cultivo_estados_lote 
SET icono = '🔵' 
WHERE nombre = 'Sembrado' AND tipo_cultivo_id = 1;

UPDATE cultivo_estados_lote 
SET icono = '🌱' 
WHERE nombre = 'Emergencia' AND tipo_cultivo_id = 1;

UPDATE cultivo_estados_lote 
SET icono = '🌾' 
WHERE nombre = 'R3' AND tipo_cultivo_id = 1;

UPDATE cultivo_estados_lote 
SET icono = '🌽' 
WHERE nombre = 'R6' AND tipo_cultivo_id = 1;

UPDATE cultivo_estados_lote 
SET icono = '📦' 
WHERE nombre = 'Listo para Cosecha' AND tipo_cultivo_id = 1;

UPDATE cultivo_estados_lote 
SET icono = '✅' 
WHERE nombre = 'Cosechado' AND tipo_cultivo_id = 1;

-- Verificar los cambios
SELECT id, nombre, icono, HEX(icono) as icono_hex 
FROM cultivo_estados_lote 
WHERE tipo_cultivo_id = 1 
ORDER BY orden;
