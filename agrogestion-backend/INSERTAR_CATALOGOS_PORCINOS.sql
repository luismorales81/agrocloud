-- ============================================================================
-- SCRIPT PARA INSERTAR CATÁLOGOS FALTANTES DEL MÓDULO PORCINOS
-- Agrega tipos de alimentos, servicios, causas de mortalidad, etc.
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
-- TIPOS DE ALIMENTO
-- ============================================================================

-- NOTA: Las categorías RACION_* fueron eliminadas porque están solapadas con InsumoCompuesto (recetas)
-- Las recetas (raciones) ahora se gestionan completamente en InsumoCompuesto (tipo RACION)
-- que se asocia a etapas mediante RecetaAlimentacionPorEtapa
INSERT INTO porcinos_tipos_alimento_porcinos (nombre, categoria, porcentaje_proteina, precio_kg, unidad_medida, descripcion, activo, empresa_id, fecha_creacion)
VALUES
('Balanceado 18%', 'BALANCEADO', 18.00, 0.45, 'kg', 'Balanceado comercial con 18% de proteína', 1, @empresa_id, NOW()),
('Balanceado 21%', 'BALANCEADO', 21.00, 0.52, 'kg', 'Balanceado comercial con 21% de proteína', 1, @empresa_id, NOW()),
('Maíz Propio', 'GRANO_PROPIO', NULL, 0.30, 'kg', 'Maíz producido en el establecimiento', 1, @empresa_id, NOW()),
('Sorgo Propio', 'GRANO_PROPIO', NULL, 0.28, 'kg', 'Sorgo producido en el establecimiento', 1, @empresa_id, NOW())
-- Raciones eliminadas: ahora se gestionan como InsumoCompuesto tipo RACION asociado a etapas
ON DUPLICATE KEY UPDATE nombre = nombre;

SELECT CONCAT('✓ Tipos de alimento insertados/actualizados: ', (SELECT COUNT(*) FROM porcinos_tipos_alimento_porcinos WHERE empresa_id = @empresa_id AND activo = 1)) AS resultado;

-- ============================================================================
-- TIPOS DE SERVICIO
-- ============================================================================

INSERT INTO porcinos_tipos_servicio_porcinos (nombre, tipo, descripcion, activo, empresa_id, fecha_creacion)
VALUES
('Monta Natural Directa', 'MONTA_NATURAL_DIRECTA', 'Servicio por monta natural directa', 1, @empresa_id, NOW()),
('IA Poscervical', 'IA_POSCERVICAL', 'Inseminación artificial poscervical', 1, @empresa_id, NOW()),
('IA Tradicional', 'IA_TRADICIONAL', 'Inseminación artificial tradicional', 1, @empresa_id, NOW()),
('Servicio Repetido', 'SERVICIO_REPETIDO', 'Servicio repetido para asegurar preñez', 1, @empresa_id, NOW())
ON DUPLICATE KEY UPDATE nombre = nombre;

SELECT CONCAT('✓ Tipos de servicio insertados/actualizados: ', (SELECT COUNT(*) FROM porcinos_tipos_servicio_porcinos WHERE empresa_id = @empresa_id AND activo = 1)) AS resultado;

-- ============================================================================
-- CAUSAS DE MORTALIDAD
-- ============================================================================

INSERT INTO porcinos_causas_mortalidad_porcinos (nombre, etapa, descripcion, activo, empresa_id, fecha_creacion)
VALUES
('Aplastamiento', 'LACTANCIA', 'Muerte por aplastamiento de la madre', 1, @empresa_id, NOW()),
('Diarrea', 'LACTANCIA', 'Muerte por diarrea en lechones', 1, @empresa_id, NOW()),
('Desnutrición', 'LACTANCIA', 'Muerte por desnutrición', 1, @empresa_id, NOW()),
('Problemas Respiratorios', 'RECRIA', 'Muerte por problemas respiratorios', 1, @empresa_id, NOW()),
('Mortalidad Perinatal', 'GESTACION', 'Muerte durante el parto o inmediatamente después', 1, @empresa_id, NOW()),
('Fallo Cardíaco', 'GENERAL', 'Muerte por fallo cardíaco', 1, @empresa_id, NOW()),
('Causa Desconocida', 'GENERAL', 'Causa de mortalidad no identificada', 1, @empresa_id, NOW())
ON DUPLICATE KEY UPDATE nombre = nombre;

SELECT CONCAT('✓ Causas de mortalidad insertadas/actualizadas: ', (SELECT COUNT(*) FROM porcinos_causas_mortalidad_porcinos WHERE empresa_id = @empresa_id AND activo = 1)) AS resultado;

-- ============================================================================
-- MOTIVOS DE BAJA
-- ============================================================================

INSERT INTO porcinos_motivos_baja_porcinos (nombre, tipo, descripcion, activo, empresa_id, fecha_creacion)
VALUES
('Venta', 'VENTA', 'Baja por venta del animal', 1, @empresa_id, NOW()),
('Muerte', 'MUERTE', 'Baja por muerte del animal', 1, @empresa_id, NOW()),
('Reemplazo', 'REEMPLAZO', 'Baja por reemplazo genético', 1, @empresa_id, NOW()),
('Problemas Sanitarios', 'PROBLEMAS_SANITARIOS', 'Baja por problemas sanitarios', 1, @empresa_id, NOW()),
('Problemas Reproductivos', 'PROBLEMAS_REPRODUCTIVOS', 'Baja por problemas reproductivos', 1, @empresa_id, NOW())
ON DUPLICATE KEY UPDATE nombre = nombre;

SELECT CONCAT('✓ Motivos de baja insertados/actualizados: ', (SELECT COUNT(*) FROM porcinos_motivos_baja_porcinos WHERE empresa_id = @empresa_id AND activo = 1)) AS resultado;

-- ============================================================================
-- ESQUEMAS SANITARIOS
-- ============================================================================

INSERT INTO porcinos_esquemas_sanitarios_porcinos (nombre, tipo, descripcion, activo, empresa_id, fecha_creacion)
VALUES
('Vacunación Triple', 'VACUNA', 'Esquema de vacunación triple', 1, @empresa_id, NOW()),
('Desparasitación General', 'DESPARASITACION', 'Esquema de desparasitación general', 1, @empresa_id, NOW()),
('Vacunación PRRS', 'VACUNA', 'Vacunación contra PRRS', 1, @empresa_id, NOW()),
('Vacunación Peste Porcina', 'VACUNA', 'Vacunación contra peste porcina', 1, @empresa_id, NOW())
ON DUPLICATE KEY UPDATE nombre = nombre;

SELECT CONCAT('✓ Esquemas sanitarios insertados/actualizados: ', (SELECT COUNT(*) FROM porcinos_esquemas_sanitarios_porcinos WHERE empresa_id = @empresa_id AND activo = 1)) AS resultado;

-- ============================================================================
-- TIPOS DE CORRAL
-- ============================================================================

INSERT INTO porcinos_tipos_corral_porcinos (nombre, tipo, descripcion, activo, empresa_id, fecha_creacion)
VALUES
('Sala de Gestación', 'SALA_GESTACION', 'Corral para madres gestantes', 1, @empresa_id, NOW()),
('Maternidad', 'MATERNIDAD', 'Corral para madres en maternidad', 1, @empresa_id, NOW()),
('Recría', 'RECRIA', 'Corral para animales en recría', 1, @empresa_id, NOW()),
('Engorde', 'ENGORDE', 'Corral para animales en engorde', 1, @empresa_id, NOW()),
('Enfermería', 'ENFERMERIA', 'Corral para animales enfermos', 1, @empresa_id, NOW())
ON DUPLICATE KEY UPDATE nombre = nombre;

SELECT CONCAT('✓ Tipos de corral insertados/actualizados: ', (SELECT COUNT(*) FROM porcinos_tipos_corral_porcinos WHERE empresa_id = @empresa_id AND activo = 1)) AS resultado;

-- ============================================================================
-- TIPOS DE EVENTO SANITARIO
-- ============================================================================

INSERT INTO porcinos_tipos_evento_sanitario (nombre, categoria, descripcion, requiere_fecha_retiro, dias_retiro_defecto, requiere_lote_medicamento, activo, empresa_id, fecha_creacion)
VALUES
('Vacunación', 'VACUNACION', 'Evento de vacunación', 0, NULL, 0, 1, @empresa_id, NOW()),
('Desparasitación', 'DESPARASITACION', 'Evento de desparasitación', 0, NULL, 0, 1, @empresa_id, NOW()),
('Tratamiento Antibiótico', 'ANTIBIOTICO', 'Tratamiento con antibióticos', 1, 7, 1, 1, @empresa_id, NOW()),
('Revisión Veterinaria', 'CONTROL', 'Revisión general por veterinario', 0, NULL, 0, 1, @empresa_id, NOW()),
('Aislamiento', 'TRATAMIENTO', 'Aislamiento por enfermedad', 0, NULL, 0, 1, @empresa_id, NOW()),
('Cuarentena', 'CONTROL', 'Cuarentena preventiva', 0, NULL, 0, 1, @empresa_id, NOW())
ON DUPLICATE KEY UPDATE nombre = nombre;

SELECT CONCAT('✓ Tipos de evento sanitario insertados/actualizados: ', (SELECT COUNT(*) FROM porcinos_tipos_evento_sanitario WHERE empresa_id = @empresa_id AND activo = 1)) AS resultado;

-- ============================================================================
-- PROVEEDORES DE GENÉTICA
-- ============================================================================

INSERT INTO porcinos_proveedores_genetica (nombre, contacto, telefono, email, descripcion, activo, empresa_id, fecha_creacion)
VALUES
('Genética Premium S.A.', 'Juan Pérez', '011-4567-8901', 'contacto@geneticapremium.com', 'Proveedor de genética de alta calidad', 1, @empresa_id, NOW()),
('Porcinos del Sur', 'María González', '0341-1234-5678', 'ventas@porcinosdelsur.com', 'Proveedor regional de genética', 1, @empresa_id, NOW()),
('Genética Nacional', 'Carlos Rodríguez', '011-2345-6789', 'info@geneticanacional.com', 'Proveedor nacional de genética', 1, @empresa_id, NOW())
ON DUPLICATE KEY UPDATE nombre = nombre;

SELECT CONCAT('✓ Proveedores de genética insertados/actualizados: ', (SELECT COUNT(*) FROM porcinos_proveedores_genetica WHERE empresa_id = @empresa_id AND activo = 1)) AS resultado;

-- ============================================================================
-- CAUSAS DE NACIDOS MUERTOS
-- ============================================================================

INSERT INTO porcinos_causas_nacidos_muertos (nombre, descripcion, activo, empresa_id, fecha_creacion)
VALUES
('Asfixia', 'Muerte por asfixia durante el parto', 1, @empresa_id, NOW()),
('Malformación', 'Malformación congénita', 1, @empresa_id, NOW()),
('Prematuridad', 'Nacimiento prematuro', 1, @empresa_id, NOW()),
('Infección', 'Infección intrauterina', 1, @empresa_id, NOW()),
('Causa Desconocida', 'Causa no identificada', 1, @empresa_id, NOW())
ON DUPLICATE KEY UPDATE nombre = nombre;

SELECT CONCAT('✓ Causas de nacidos muertos insertadas/actualizadas: ', (SELECT COUNT(*) FROM porcinos_causas_nacidos_muertos WHERE empresa_id = @empresa_id AND activo = 1)) AS resultado;

-- ============================================================================
-- CAUSAS DE MOMIFICADOS
-- ============================================================================

INSERT INTO porcinos_causas_momificados (nombre, descripcion, activo, empresa_id, fecha_creacion)
VALUES
('Infección Viral', 'Momificación por infección viral', 1, @empresa_id, NOW()),
('Infección Bacteriana', 'Momificación por infección bacteriana', 1, @empresa_id, NOW()),
('Problemas Placentarios', 'Problemas con la placenta', 1, @empresa_id, NOW()),
('Nutrición Inadecuada', 'Problemas nutricionales de la madre', 1, @empresa_id, NOW()),
('Causa Desconocida', 'Causa no identificada', 1, @empresa_id, NOW())
ON DUPLICATE KEY UPDATE nombre = nombre;

SELECT CONCAT('✓ Causas de momificados insertadas/actualizadas: ', (SELECT COUNT(*) FROM porcinos_causas_momificados WHERE empresa_id = @empresa_id AND activo = 1)) AS resultado;

-- ============================================================================
-- ACTUALIZAR RAZAS CON TIPO (si no tienen tipo asignado)
-- ============================================================================

UPDATE porcinos_razas_porcinos 
SET tipo = CASE 
    WHEN nombre IN ('Yorkshire', 'Landrace') THEN 'MADRE'
    WHEN nombre IN ('Duroc', 'Pietrain', 'Hampshire') THEN 'PADRILLO'
    ELSE 'HIBRIDO'
END
WHERE empresa_id = @empresa_id AND tipo IS NULL;

SELECT CONCAT('✓ Razas actualizadas con tipo') AS resultado;

-- ============================================================================
-- RESUMEN FINAL
-- ============================================================================

SELECT '' AS '';
SELECT '════════════════════════════════════════════════' AS '';
SELECT 'RESUMEN DE CATÁLOGOS INSERTADOS' AS '';
SELECT '════════════════════════════════════════════════' AS '';
SELECT '' AS '';

SELECT
    'TIPOS DE ALIMENTO' AS categoria,
    COUNT(*) AS cantidad
FROM porcinos_tipos_alimento_porcinos WHERE empresa_id = @empresa_id AND activo = 1

UNION ALL

SELECT 'TIPOS DE SERVICIO', COUNT(*) FROM porcinos_tipos_servicio_porcinos WHERE empresa_id = @empresa_id AND activo = 1
UNION ALL
SELECT 'CAUSAS DE MORTALIDAD', COUNT(*) FROM porcinos_causas_mortalidad_porcinos WHERE empresa_id = @empresa_id AND activo = 1
UNION ALL
SELECT 'MOTIVOS DE BAJA', COUNT(*) FROM porcinos_motivos_baja_porcinos WHERE empresa_id = @empresa_id AND activo = 1
UNION ALL
SELECT 'ESQUEMAS SANITARIOS', COUNT(*) FROM porcinos_esquemas_sanitarios_porcinos WHERE empresa_id = @empresa_id AND activo = 1
UNION ALL
SELECT 'TIPOS DE CORRAL', COUNT(*) FROM porcinos_tipos_corral_porcinos WHERE empresa_id = @empresa_id AND activo = 1
UNION ALL
SELECT 'TIPOS DE EVENTO SANITARIO', COUNT(*) FROM porcinos_tipos_evento_sanitario WHERE empresa_id = @empresa_id AND activo = 1
UNION ALL
SELECT 'PROVEEDORES DE GENÉTICA', COUNT(*) FROM porcinos_proveedores_genetica WHERE empresa_id = @empresa_id AND activo = 1
UNION ALL
SELECT 'CAUSAS NACIDOS MUERTOS', COUNT(*) FROM porcinos_causas_nacidos_muertos WHERE empresa_id = @empresa_id AND activo = 1
UNION ALL
SELECT 'CAUSAS MOMIFICADOS', COUNT(*) FROM porcinos_causas_momificados WHERE empresa_id = @empresa_id AND activo = 1;

SELECT '' AS '';
SELECT '════════════════════════════════════════════════' AS '';
SELECT '✓ PROCESO COMPLETADO EXITOSAMENTE' AS '';
SELECT '════════════════════════════════════════════════' AS '';

