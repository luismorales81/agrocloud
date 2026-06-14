-- Plantillas de labores sugeridas por wizard IA (cultivos)
CREATE TABLE IF NOT EXISTS cultivo_plantilla_labor (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id BIGINT NOT NULL,
    tipo_cultivo_id BIGINT NOT NULL,
    nombre VARCHAR(200) NOT NULL,
    tipo_labor VARCHAR(50) NOT NULL,
    dia_relativo_siembra INT NOT NULL DEFAULT 0,
    estado_esperado_nombre VARCHAR(100) NULL,
    duracion_estimada_hs INT NULL,
    insumos_sugeridos VARCHAR(500) NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_plantilla_labor_empresa FOREIGN KEY (empresa_id) REFERENCES empresas(id),
    CONSTRAINT fk_plantilla_labor_tipo FOREIGN KEY (tipo_cultivo_id) REFERENCES cultivo_tipos_cultivo(id)
);

-- Plan de recría reutilizable (porcinos)
CREATE TABLE IF NOT EXISTS porcinos_plan_recria (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id BIGINT NOT NULL,
    nombre VARCHAR(200) NOT NULL,
    descripcion VARCHAR(500) NULL,
    raza_objetivo VARCHAR(120) NULL,
    proposito VARCHAR(30) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_plan_recria_empresa FOREIGN KEY (empresa_id) REFERENCES empresas(id)
);

CREATE TABLE IF NOT EXISTS porcinos_plan_recria_etapa (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    plan_recria_id BIGINT NOT NULL,
    orden INT NOT NULL,
    codigo_etapa VARCHAR(30) NOT NULL,
    duracion_estimada_dias INT NULL,
    peso_ingreso_minimo_kg DECIMAL(10,2) NULL,
    peso_objetivo_kg DECIMAL(10,2) NULL,
    ganancia_diaria_esperada_kg DECIMAL(10,4) NULL,
    umbral_mortalidad_pct DECIMAL(5,2) NULL,
    CONSTRAINT fk_plan_etapa_plan FOREIGN KEY (plan_recria_id) REFERENCES porcinos_plan_recria(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS porcinos_plan_recria_receta_sugerencia (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    plan_recria_id BIGINT NOT NULL,
    etapa_codigo VARCHAR(30) NOT NULL,
    nombre_insumo_compuesto VARCHAR(200) NOT NULL,
    kg_por_animal_dia DECIMAL(12,4) NOT NULL,
    CONSTRAINT fk_plan_receta_plan FOREIGN KEY (plan_recria_id) REFERENCES porcinos_plan_recria(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS porcinos_plan_recria_recordatorio (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    plan_recria_id BIGINT NOT NULL,
    tipo VARCHAR(50) NOT NULL,
    dias_desde_ingreso INT NOT NULL,
    descripcion VARCHAR(500) NOT NULL,
    CONSTRAINT fk_plan_rec_plan FOREIGN KEY (plan_recria_id) REFERENCES porcinos_plan_recria(id) ON DELETE CASCADE
);
