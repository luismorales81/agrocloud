-- ============================================================================
-- SCRIPT DE EJECUCIÓN MANUAL - Configuración Maestra Porcinos
-- ============================================================================
-- Este script puede ejecutarse manualmente en MySQL si Flyway no lo ejecuta
-- automáticamente. Normalmente Flyway lo ejecuta al iniciar la aplicación.
--
-- Para ejecutar manualmente:
-- mysql -u root -p agrocloud < scripts/V1_30_Ejecutar_Manual.sql
-- ============================================================================

USE agrocloud;

-- ============================================================================
-- CATÁLOGOS CONFIGURABLES
-- ============================================================================

-- Tabla: razas_porcinos (Razas configurables)
CREATE TABLE IF NOT EXISTS razas_porcinos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    tipo ENUM('MADRE', 'PADRILLO', 'HIBRIDO') NOT NULL,
    descripcion TEXT,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    empresa_id BIGINT NOT NULL,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE,
    
    INDEX idx_razas_empresa (empresa_id),
    INDEX idx_razas_tipo (tipo),
    INDEX idx_razas_activo (activo),
    UNIQUE KEY uk_razas_empresa_nombre (empresa_id, nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: tipos_alimento_porcinos (ELIMINADA - V1_114)
-- NOTA: TipoAlimentoPorcino fue eliminado porque es redundante con:
-- - Recetas: InsumoCompuesto (tipo RACION) asociado a etapas mediante RecetaAlimentacionPorEtapa
-- - Balanceados comerciales: Insumo (tabla cultivo_insumos)
-- - Granos propios: Cultivo + InventarioGrano
-- TipoAlimentoPorcino solo existía como catálogo sin integración funcional
-- en el sistema de consumo actual (ConsumoDiarioAutomatico)
-- 
-- CREATE TABLE IF NOT EXISTS tipos_alimento_porcinos (...); -- ELIMINADO

-- Tabla: tipos_servicio_porcinos (Tipos de servicio reproductivo configurables)
CREATE TABLE IF NOT EXISTS tipos_servicio_porcinos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    tipo ENUM('MONTA_NATURAL_DIRECTA', 'IA_POSCERVICAL', 'IA_TRADICIONAL', 'SERVICIO_REPETIDO') NOT NULL,
    descripcion TEXT,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    empresa_id BIGINT NOT NULL,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE,
    
    INDEX idx_tipos_servicio_empresa (empresa_id),
    INDEX idx_tipos_servicio_tipo (tipo),
    INDEX idx_tipos_servicio_activo (activo),
    UNIQUE KEY uk_tipos_servicio_empresa_nombre (empresa_id, nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: causas_mortalidad_porcinos (Causas de mortalidad configurables por etapa)
CREATE TABLE IF NOT EXISTS causas_mortalidad_porcinos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    etapa ENUM('LACTANCIA', 'RECRIA', 'ENGORDE', 'GESTACION', 'GENERAL') NOT NULL,
    descripcion TEXT,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    empresa_id BIGINT NOT NULL,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE,
    
    INDEX idx_causas_mortalidad_empresa (empresa_id),
    INDEX idx_causas_mortalidad_etapa (etapa),
    INDEX idx_causas_mortalidad_activo (activo),
    UNIQUE KEY uk_causas_mortalidad_empresa_nombre_etapa (empresa_id, nombre, etapa)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: motivos_baja_porcinos (Motivos de baja configurables)
CREATE TABLE IF NOT EXISTS motivos_baja_porcinos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    tipo ENUM('VENTA', 'MUERTE', 'REEMPLAZO', 'PROBLEMAS_SANITARIOS', 'PROBLEMAS_REPRODUCTIVOS') NOT NULL,
    descripcion TEXT,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    empresa_id BIGINT NOT NULL,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE,
    
    INDEX idx_motivos_baja_empresa (empresa_id),
    INDEX idx_motivos_baja_tipo (tipo),
    INDEX idx_motivos_baja_activo (activo),
    UNIQUE KEY uk_motivos_baja_empresa_nombre (empresa_id, nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: esquemas_sanitarios_porcinos (Esquemas sanitarios configurables)
CREATE TABLE IF NOT EXISTS esquemas_sanitarios_porcinos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(200) NOT NULL,
    tipo ENUM('VACUNA', 'DESPARASITACION', 'ANTIBIOTICO', 'OTRO') NOT NULL,
    producto VARCHAR(200),
    dosis VARCHAR(100),
    frecuencia_dias INT,
    fecha_programada DATE,
    aplicable_a VARCHAR(100),
    descripcion TEXT,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    empresa_id BIGINT NOT NULL,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE,
    
    INDEX idx_esquemas_sanitarios_empresa (empresa_id),
    INDEX idx_esquemas_sanitarios_tipo (tipo),
    INDEX idx_esquemas_sanitarios_activo (activo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: tipos_corral_porcinos (DEPRECADA - ELIMINADA en V1_113)
-- NOTA: Esta tabla fue eliminada porque estaba en superposición con UbicacionInterna (porcinos_ubicaciones_internas).
-- El sistema ahora usa únicamente UbicacionInterna (estructura jerárquica: Galpón → Sala → Corral)
-- que tiene FK en Madre, Padrillo y Recria.
-- CREATE TABLE IF NOT EXISTS tipos_corral_porcinos (...)
-- Esta tabla ya no debe crearse. Use porcinos_ubicaciones_internas en su lugar.

-- ============================================================================
-- PARÁMETROS DEL ESTABLECIMIENTO
-- ============================================================================

-- Tabla: parametros_establecimiento_porcinos (Parámetros generales del establecimiento)
CREATE TABLE IF NOT EXISTS parametros_establecimiento_porcinos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre_establecimiento VARCHAR(200) NOT NULL,
    provincia VARCHAR(100),
    localidad VARCHAR(100),
    razon_social VARCHAR(200),
    unidad_manejo ENUM('LOTES', 'GRUPOS', 'ANIMALES_INDIVIDUALES') NOT NULL,
    maximo_madres INT,
    maximo_padrillos INT,
    maxima_capacidad_recria_engorde INT,
    categorias_habilitadas VARCHAR(500),
    ciclos_productivos_propios TEXT,
    empresa_id BIGINT NOT NULL,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE,
    
    INDEX idx_parametros_establecimiento_empresa (empresa_id),
    UNIQUE KEY uk_parametros_establecimiento_empresa (empresa_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- PARÁMETROS PRODUCTIVOS
-- ============================================================================

-- Tabla: parametros_productivos_porcinos (Parámetros productivos configurables)
CREATE TABLE IF NOT EXISTS parametros_productivos_porcinos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    -- Ciclo de producción
    dias_promedio_gestacion INT,
    dias_lactancia INT,
    dias_recria_antes_engorde INT,
    dias_engorde INT,
    cantidad_maxima_servicios_padrillo_dia INT,
    tiempo_espera_entre_servicios_horas INT,
    -- Alertas y recordatorios
    dias_antelacion_alertar_partos INT,
    dias_antelacion_alertar_ecografias INT,
    dias_antelacion_alertar_destetes INT,
    dias_antelacion_alertar_pasaje_maternidad INT,
    dias_antelacion_alertar_revisiones_sanitarias INT,
    umbral_mortalidad_lactancia_porcentaje DECIMAL(5,2),
    umbral_mortalidad_recria_porcentaje DECIMAL(5,2),
    porcentaje_minimo_prenez_antes_advertencia DECIMAL(5,2),
    empresa_id BIGINT NOT NULL,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE,
    
    INDEX idx_parametros_productivos_empresa (empresa_id),
    UNIQUE KEY uk_parametros_productivos_empresa (empresa_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- DATOS ECONÓMICOS
-- ============================================================================

-- Tabla: datos_economicos_porcinos (Datos económicos configurables)
CREATE TABLE IF NOT EXISTS datos_economicos_porcinos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    -- Costos por categoría
    costo_madre_gestacion_dia DECIMAL(10,2),
    costo_madre_lactancia_dia DECIMAL(10,2),
    costo_lechon DECIMAL(10,2),
    costo_engorde_dia DECIMAL(10,2),
    costo_mano_obra_dia DECIMAL(10,2),
    precio_venta_cerdo_terminado_kg DECIMAL(10,2),
    porcentaje_merma_transporte DECIMAL(5,2),
    -- Integración con cultivos
    kg_maiz_por_racion_engorde DECIMAL(10,2),
    porcentaje_mezcla_alimento_propio_balanceado DECIMAL(5,2),
    indice_conversion_objetivo DECIMAL(5,2),
    metodo_imputacion_costo_cultivo ENUM('PROMEDIO_PONDERADO', 'PRECIO_MERCADO', 'PRECIO_MANUAL'),
    precio_manual_cultivo_kg DECIMAL(10,2),
    empresa_id BIGINT NOT NULL,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE,
    
    INDEX idx_datos_economicos_empresa (empresa_id),
    UNIQUE KEY uk_datos_economicos_empresa (empresa_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- VERIFICACIÓN
-- ============================================================================

-- Verificar que las tablas se crearon correctamente
SELECT 
    'razas_porcinos' AS tabla,
    COUNT(*) AS registros
FROM razas_porcinos
UNION ALL
-- SELECT 'tipos_alimento_porcinos' AS tabla, COUNT(*) AS registros FROM tipos_alimento_porcinos -- ELIMINADA (V1_114)
UNION ALL
SELECT 
    'tipos_servicio_porcinos' AS tabla,
    COUNT(*) AS registros
FROM tipos_servicio_porcinos
UNION ALL
SELECT 
    'causas_mortalidad_porcinos' AS tabla,
    COUNT(*) AS registros
FROM causas_mortalidad_porcinos
UNION ALL
SELECT 
    'motivos_baja_porcinos' AS tabla,
    COUNT(*) AS registros
FROM motivos_baja_porcinos
UNION ALL
SELECT 
    'esquemas_sanitarios_porcinos' AS tabla,
    COUNT(*) AS registros
FROM esquemas_sanitarios_porcinos
-- UNION ALL
-- SELECT 
--     'tipos_corral_porcinos' AS tabla,
--     COUNT(*) AS registros
-- FROM tipos_corral_porcinos
-- NOTA: tipos_corral_porcinos fue eliminada (V1_113) - Reemplazada por porcinos_ubicaciones_internas
UNION ALL
SELECT 
    'parametros_establecimiento_porcinos' AS tabla,
    COUNT(*) AS registros
FROM parametros_establecimiento_porcinos
UNION ALL
SELECT 
    'parametros_productivos_porcinos' AS tabla,
    COUNT(*) AS registros
FROM parametros_productivos_porcinos
UNION ALL
SELECT 
    'datos_economicos_porcinos' AS tabla,
    COUNT(*) AS registros
FROM datos_economicos_porcinos;

-- Mostrar todas las tablas creadas
SHOW TABLES LIKE '%porcinos%';

