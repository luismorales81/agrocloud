-- ============================================================================
-- SCRIPT PARA ELIMINAR CONFIGURACIONES DUPLICADAS RESTANTES
-- Todas estas configuraciones están duplicadas en Parámetros Productivos
-- y deben eliminarse de Configuraciones Generales
-- ============================================================================

USE agrocloud;

-- Verificar configuraciones existentes antes de eliminar
SELECT 
    'CONFIGURACIONES ANTES DE ELIMINAR' AS estado,
    clave,
    valor,
    categoria,
    empresa_id,
    activo
FROM porcinos_configuraciones_porcinos
WHERE clave IN (
    'DIAS_ALERTA_PARTOS', 'DIAS_ALERTA_ECOGRAFIAS', 'DIAS_ALERTA_DESTETES', 'DIAS_ALERTA_REVISIONES',
    'UMBRAL_MORTALIDAD_LACTANCIA', 'UMBRAL_MORTALIDAD_RECRIA', 'UMBRAL_MINIMO_PREÑEZ',
    'PESO_PROMEDIO_NACIMIENTO', 'PESO_DESTETE_OBJETIVO', 'PESO_VENTA_OBJETIVO',
    'LECHONES_VIVOS_PARTO_OBJETIVO', 'LECHONES_DESTETADOS_OBJETIVO', 'PARTOS_MADRE_ANIO_OBJETIVO',
    'HABILITAR_NOTIFICACIONES', 'FORMATO_FECHA', 'IDIOMA_INTERFAZ'
)
ORDER BY empresa_id, categoria, clave;

-- Eliminar configuraciones duplicadas (marcar como inactivas)
-- NOTA: Estas configuraciones están duplicadas en Parámetros Productivos
UPDATE porcinos_configuraciones_porcinos
SET activo = 0,
    fecha_actualizacion = NOW()
WHERE clave IN (
    'DIAS_ALERTA_PARTOS',           -- Duplicado de ParametrosProductivos.diasAntelacionAlertarPartos
    'DIAS_ALERTA_ECOGRAFIAS',       -- Duplicado de ParametrosProductivos.diasAntelacionAlertarEcografias
    'DIAS_ALERTA_DESTETES',         -- Duplicado de ParametrosProductivos.diasAntelacionAlertarDestetes
    'DIAS_ALERTA_REVISIONES',       -- Duplicado de ParametrosProductivos.diasAntelacionAlertarRevisionesSanitarias
    'UMBRAL_MORTALIDAD_LACTANCIA',  -- Duplicado de ParametrosProductivos.umbralMortalidadLactanciaPorcentaje
    'UMBRAL_MORTALIDAD_RECRIA',     -- Duplicado de ParametrosProductivos.umbralMortalidadRecriaPorcentaje
    'UMBRAL_MINIMO_PREÑEZ',         -- Duplicado de ParametrosProductivos.porcentajeMinimoPrenezAntesAdvertencia
    'PESO_PROMEDIO_NACIMIENTO',     -- Duplicado de ParametrosProductivos.pesoPromedioNacimiento
    'PESO_DESTETE_OBJETIVO',        -- Duplicado de ParametrosProductivos.pesoDesteteObjetivo
    'PESO_VENTA_OBJETIVO',          -- Duplicado de ParametrosProductivos.pesoVentaObjetivo
    'LECHONES_VIVOS_PARTO_OBJETIVO', -- Duplicado de ParametrosProductivos.lechonesVivosPartoObjetivo
    'LECHONES_DESTETADOS_OBJETIVO',  -- Duplicado de ParametrosProductivos.lechonesDestetadosObjetivo
    'PARTOS_MADRE_ANIO_OBJETIVO',    -- Duplicado de ParametrosProductivos.partosMadreAnioObjetivo
    'HABILITAR_NOTIFICACIONES',      -- No implementado en el código
    'FORMATO_FECHA',                 -- No implementado en el código
    'IDIOMA_INTERFAZ'                -- No implementado en el código
)
  AND activo = 1;

-- Verificar resultado
SELECT 
    'CONFIGURACIONES DESPUÉS DE ELIMINAR' AS estado,
    clave,
    valor,
    categoria,
    empresa_id,
    activo
FROM porcinos_configuraciones_porcinos
WHERE clave IN (
    'DIAS_CACHORRA',  -- Esta debe mantenerse activa
    'DIAS_ALERTA_PARTOS', 'DIAS_ALERTA_ECOGRAFIAS', 'DIAS_ALERTA_DESTETES', 'DIAS_ALERTA_REVISIONES',
    'UMBRAL_MORTALIDAD_LACTANCIA', 'UMBRAL_MORTALIDAD_RECRIA', 'UMBRAL_MINIMO_PREÑEZ',
    'PESO_PROMEDIO_NACIMIENTO', 'PESO_DESTETE_OBJETIVO', 'PESO_VENTA_OBJETIVO',
    'LECHONES_VIVOS_PARTO_OBJETIVO', 'LECHONES_DESTETADOS_OBJETIVO', 'PARTOS_MADRE_ANIO_OBJETIVO',
    'HABILITAR_NOTIFICACIONES', 'FORMATO_FECHA', 'IDIOMA_INTERFAZ'
)
ORDER BY empresa_id, activo DESC, categoria, clave;

-- Verificar que DIAS_CACHORRA sigue activa (debe mantenerse porque se usa en MadreService)
SELECT 
    'VERIFICACIÓN: DIAS_CACHORRA DEBE ESTAR ACTIVA' AS verificacion,
    clave,
    valor,
    empresa_id,
    activo
FROM porcinos_configuraciones_porcinos
WHERE clave = 'DIAS_CACHORRA'
  AND activo = 1;

-- Resumen de configuraciones eliminadas por categoría
SELECT 
    'RESUMEN: Configuraciones eliminadas por categoría' AS mensaje,
    categoria,
    COUNT(*) AS cantidad_eliminadas
FROM porcinos_configuraciones_porcinos
WHERE clave IN (
    'DIAS_ALERTA_PARTOS', 'DIAS_ALERTA_ECOGRAFIAS', 'DIAS_ALERTA_DESTETES', 'DIAS_ALERTA_REVISIONES',
    'UMBRAL_MORTALIDAD_LACTANCIA', 'UMBRAL_MORTALIDAD_RECRIA', 'UMBRAL_MINIMO_PREÑEZ',
    'PESO_PROMEDIO_NACIMIENTO', 'PESO_DESTETE_OBJETIVO', 'PESO_VENTA_OBJETIVO',
    'LECHONES_VIVOS_PARTO_OBJETIVO', 'LECHONES_DESTETADOS_OBJETIVO', 'PARTOS_MADRE_ANIO_OBJETIVO',
    'HABILITAR_NOTIFICACIONES', 'FORMATO_FECHA', 'IDIOMA_INTERFAZ'
)
  AND activo = 0
GROUP BY categoria
ORDER BY categoria;

-- Resumen final: solo debe quedar DIAS_CACHORRA activa en Configuraciones Generales
SELECT 
    'RESUMEN FINAL: Configuraciones activas en Configuraciones Generales' AS mensaje,
    COUNT(*) AS total_activas,
    GROUP_CONCAT(clave SEPARATOR ', ') AS claves_activas
FROM porcinos_configuraciones_porcinos
WHERE activo = 1
  AND empresa_id IN (SELECT id FROM empresas WHERE activo = 1);
