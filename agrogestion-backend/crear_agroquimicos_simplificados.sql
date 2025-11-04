-- Crear tabla de agroquímicos simplificada
-- Campos mínimos y esenciales para gestión eficiente

-- Crear tabla principal de agroquímicos
CREATE TABLE IF NOT EXISTS agroquimicos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre_comercial VARCHAR(100) NOT NULL,
    principio_activo VARCHAR(100) NOT NULL,
    tipo ENUM('HERBICIDA', 'INSECTICIDA', 'FUNGICIDA', 'COADYUVANTE', 'DESCONOCIDO') NOT NULL,
    forma_aplicacion ENUM('TERRESTRE', 'AEREA', 'MANUAL') NOT NULL,
    dosis_por_ha DECIMAL(10,2) NOT NULL,
    unidad_medida ENUM('LTS', 'KG', 'GR', 'ML') NOT NULL,
    fecha_vencimiento DATE NOT NULL,
    stock_actual DECIMAL(10,2) NOT NULL,
    stock_minimo DECIMAL(10,2) NULL,
    activo BOOLEAN DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Índices para optimización
    INDEX idx_agroquimicos_activo (activo),
    INDEX idx_agroquimicos_tipo (tipo),
    INDEX idx_agroquimicos_fecha_vencimiento (fecha_vencimiento),
    INDEX idx_agroquimicos_stock (stock_actual)
);

-- Tabla para cultivos permitidos ELIMINADA - No se utiliza en la lógica de negocio
-- CREATE TABLE IF NOT EXISTS agroquimico_cultivos_permitidos (
--     agroquimico_id BIGINT NOT NULL,
--     cultivo VARCHAR(100) NOT NULL,
--     PRIMARY KEY (agroquimico_id, cultivo),
--     FOREIGN KEY (agroquimico_id) REFERENCES agroquimicos(id) ON DELETE CASCADE
-- );

-- Insertar datos de prueba
INSERT INTO agroquimicos (nombre_comercial, principio_activo, tipo, forma_aplicacion, dosis_por_ha, unidad_medida, fecha_vencimiento, stock_actual, stock_minimo) VALUES
('Glifosato 48%', 'Glifosato', 'HERBICIDA', 'TERRESTRE', 2.5, 'LTS', '2025-12-31', 50.0, 10.0),
('Atrazina 50%', 'Atrazina', 'HERBICIDA', 'TERRESTRE', 3.0, 'LTS', '2025-11-30', 25.0, 5.0),
('Imidacloprid 20%', 'Imidacloprid', 'INSECTICIDA', 'TERRESTRE', 0.5, 'LTS', '2025-10-31', 15.0, 3.0),
('Mancozeb 80%', 'Mancozeb', 'FUNGICIDA', 'TERRESTRE', 2.0, 'KG', '2025-09-30', 30.0, 5.0),
('Aceite Mineral', 'Aceite Mineral', 'COADYUVANTE', 'TERRESTRE', 1.0, 'LTS', '2025-08-31', 40.0, 8.0);

-- Inserciones de cultivos permitidos ELIMINADAS - No se utiliza en la lógica de negocio
-- INSERT INTO agroquimico_cultivos_permitidos (agroquimico_id, cultivo) VALUES...

-- Verificar inserción
SELECT 
    a.id,
    a.nombre_comercial,
    a.principio_activo,
    a.tipo,
    a.forma_aplicacion,
    a.dosis_por_ha,
    a.unidad_medida,
    a.fecha_vencimiento,
    a.stock_actual,
    a.stock_minimo
FROM agroquimicos a
WHERE a.activo = TRUE
ORDER BY a.nombre_comercial;
