-- ============================================================================
-- SCRIPT PARA INSERTAR CONFIGURACIONES GENERALES DEL MÓDULO PORCINOS
-- Configuraciones clave-valor flexibles para el módulo
-- ============================================================================

USE agrocloud;

-- Obtener IDs de empresa y usuario existentes
SET @empresa_id = (SELECT id FROM empresas WHERE nombre LIKE '%AgroCloud%' AND activo = 1 LIMIT 1);
SET @empresa_id = IFNULL(@empresa_id, (SELECT id FROM empresas WHERE activo = 1 LIMIT 1));

SET @admin_user_id = (SELECT id FROM usuarios WHERE email = 'admin@agrocloud.com' AND activo = 1 LIMIT 1);
SET @admin_user_id = IFNULL(@admin_user_id, (SELECT id FROM usuarios WHERE activo = 1 LIMIT 1));

-- Verificar que tenemos empresa y usuario
SELECT CONCAT('✓ Empresa ID: ', COALESCE(@empresa_id, 'NO ENCONTRADA')) AS verificacion;
SELECT CONCAT('✓ Usuario ID: ', COALESCE(@admin_user_id, 'NO ENCONTRADO')) AS verificacion;

-- ============================================================================
-- INSERTAR CONFIGURACIONES GENERALES
-- ============================================================================

-- Configuraciones de Etapas
-- NOTA: Solo mantener configuraciones que NO están en Parámetros Productivos
-- DIAS_GESTACION, DIAS_LACTANCIA, DIAS_RECRIA, DIAS_ENGORDE están en Parámetros Productivos
-- DIAS_ENTRE_CELOS, DIAS_CONTROL_CELO, DIAS_PASAJE_MATERNIDAD también están en Parámetros Productivos
-- MAX_SERVICIOS_PADRILLO_DIA, HORAS_ESPERA_ENTRE_SERVICIOS también están en Parámetros Productivos
-- Solo DIAS_CACHORRA se mantiene aquí porque se usa exclusivamente en Configuraciones Generales (en MadreService)
-- 
-- TODAS LAS DEMÁS CONFIGURACIONES ESTÁN DUPLICADAS EN PARÁMETROS PRODUCTIVOS:
-- - DIAS_ALERTA_* → diasAntelacionAlertar* (Parámetros Productivos)
-- - UMBRAL_* → umbralMortalidad* y porcentajeMinimoPrenez* (Parámetros Productivos)
-- - PESO_* → pesoPromedioNacimiento, pesoDesteteObjetivo, pesoVentaObjetivo (Parámetros Productivos)
-- - LECHONES_* → lechonesVivosPartoObjetivo, lechonesDestetadosObjetivo (Parámetros Productivos)
-- - PARTOS_* → partosMadreAnioObjetivo (Parámetros Productivos)
-- 
-- Configuraciones del sistema (HABILITAR_NOTIFICACIONES, FORMATO_FECHA, IDIOMA_INTERFAZ) 
-- están definidas pero NO se implementaron en el código. Si no se van a usar, deben eliminarse.
INSERT INTO porcinos_configuraciones_porcinos (clave, valor, tipo, categoria, descripcion, activo, empresa_id, fecha_creacion)
VALUES
('DIAS_CACHORRA', '160', 'NUMERO', 'ETAPAS', 'Días que una cachorra permanece antes de ser considerada adulta', 1, @empresa_id, NOW())
ON DUPLICATE KEY UPDATE valor = valor;

SELECT CONCAT('✓ Configuraciones insertadas/actualizadas: ', (SELECT COUNT(*) FROM porcinos_configuraciones_porcinos WHERE empresa_id = @empresa_id AND activo = 1)) AS resultado;

-- ============================================================================
-- RESUMEN FINAL
-- ============================================================================

SELECT '' AS '';
SELECT '════════════════════════════════════════════════' AS '';
SELECT 'RESUMEN DE CONFIGURACIONES INSERTADAS' AS '';
SELECT '════════════════════════════════════════════════' AS '';
SELECT '' AS '';

SELECT
    categoria AS categoria,
    COUNT(*) AS cantidad
FROM porcinos_configuraciones_porcinos 
WHERE empresa_id = @empresa_id AND activo = 1
GROUP BY categoria
ORDER BY categoria;

SELECT '' AS '';
SELECT '════════════════════════════════════════════════' AS '';
SELECT '✓ PROCESO COMPLETADO EXITOSAMENTE' AS '';
SELECT '════════════════════════════════════════════════' AS '';

