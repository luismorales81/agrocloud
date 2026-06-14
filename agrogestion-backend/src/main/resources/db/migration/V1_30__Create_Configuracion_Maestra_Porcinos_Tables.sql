-- ============================================================================
-- MIGRACIÓN: Crear tablas de Configuración Maestra del módulo Porcinos
-- Versión: V1_30
-- Fecha: 2025-01-XX
-- Descripción: Tablas para catálogos configurables, parámetros del establecimiento,
--              parámetros productivos y datos económicos
-- ============================================================================

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

-- Tabla: tipos_alimento_porcinos (Tipos de alimento configurables)
CREATE TABLE IF NOT EXISTS tipos_alimento_porcinos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(200) NOT NULL,
    categoria ENUM('BALANCEADO', 'GRANO_PROPIO', 'RACION_INICIADOR', 'RACION_TERMINADOR', 
                   'RACION_GESTACION', 'RACION_LACTANCIA', 'OTRO') NOT NULL,
    porcentaje_proteina DECIMAL(5,2),
    precio_kg DECIMAL(10,2),
    unidad_medida VARCHAR(50) NOT NULL DEFAULT 'kg',
    descripcion TEXT,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    empresa_id BIGINT NOT NULL,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE,
    
    INDEX idx_tipos_alimento_empresa (empresa_id),
    INDEX idx_tipos_alimento_categoria (categoria),
    INDEX idx_tipos_alimento_activo (activo),
    UNIQUE KEY uk_tipos_alimento_empresa_nombre (empresa_id, nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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

-- Tabla: tipos_corral_porcinos (Tipos de corral/nave/sala configurables)
CREATE TABLE IF NOT EXISTS tipos_corral_porcinos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    tipo ENUM('SALA_GESTACION', 'MATERNIDAD', 'RECRIA', 'ENGORDE', 'ENFERMERIA', 'OTRO') NOT NULL,
    capacidad_maxima INT,
    descripcion TEXT,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    empresa_id BIGINT NOT NULL,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE,
    
    INDEX idx_tipos_corral_empresa (empresa_id),
    INDEX idx_tipos_corral_tipo (tipo),
    INDEX idx_tipos_corral_activo (activo),
    UNIQUE KEY uk_tipos_corral_empresa_nombre (empresa_id, nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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
-- DATOS POR DEFECTO (se insertarán por empresa en el servicio)
-- ============================================================================

-- Nota: Los datos por defecto se insertarán mediante el servicio de inicialización
-- cuando se cree una nueva empresa o se habilite el módulo porcinos

