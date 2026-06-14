-- ============================================================================
-- SCRIPT PARA ELIMINAR LAS ÚLTIMAS CONFIGURACIONES DUPLICADAS
-- Estas también están duplicadas en Parámetros Productivos
-- ============================================================================

USE agrocloud;

-- Verificar configuraciones activas restantes
SELECT 
    'CONFIGURACIONES ACTIVAS ANTES DE ELIMINAR' AS estado,
    clave,
    valor,
    categoria,
    empresa_id,
    activo
FROM porcinos_configuraciones_porcinos
WHERE activo = 1
ORDER BY empresa_id, categoria, clave;

-- Eliminar las últimas configuraciones duplicadas
-- NOTA: Estas también están duplicadas en Parámetros Productivos
UPDATE porcinos_configuraciones_porcinos
SET activo = 0,
    fecha_actualizacion = NOW()
WHERE clave IN (
    'DIAS_RECRIA',                      -- Duplicado de ParametrosProductivos.diasRecriaAntesEngorde
    'DIAS_ENGORDE',                     -- Duplicado de ParametrosProductivos.diasEngorde
    'DIAS_PASAJE_MATERNIDAD',           -- Duplicado de ParametrosProductivos.diasPasajeMaternidad
    'MAX_SERVICIOS_PADRILLO_DIA',       -- Duplicado de ParametrosProductivos.cantidadMaximaServiciosPadrilloDia
    'HORAS_ESPERA_ENTRE_SERVICIOS'      -- Duplicado de ParametrosProductivos.tiempoEsperaEntreServiciosHoras
)
  AND activo = 1;

-- Verificar resultado final: SOLO debe quedar DIAS_CACHORRA activa
SELECT 
    'CONFIGURACIONES ACTIVAS FINALES (Solo debe estar DIAS_CACHORRA)' AS estado,
    clave,
    valor,
    categoria,
    empresa_id,
    activo
FROM porcinos_configuraciones_porcinos
WHERE activo = 1
ORDER BY empresa_id, categoria, clave;

-- Resumen final
SELECT 
    'RESUMEN FINAL' AS mensaje,
    COUNT(*) AS total_configuraciones_activas,
    CASE 
        WHEN COUNT(*) = 1 AND MAX(clave) = 'DIAS_CACHORRA' THEN '✓ Correcto: Solo DIAS_CACHORRA está activa'
        WHEN COUNT(*) = 0 THEN '⚠ Advertencia: No hay configuraciones activas'
        ELSE CONCAT('✗ Error: Hay ', COUNT(*), ' configuraciones activas. Solo debería estar DIAS_CACHORRA')
    END AS estado
FROM porcinos_configuraciones_porcinos
WHERE activo = 1;
