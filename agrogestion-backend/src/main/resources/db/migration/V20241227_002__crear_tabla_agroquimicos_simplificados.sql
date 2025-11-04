-- Migración para crear tabla de agroquímicos simplificados
-- Campos mínimos y esenciales para gestión eficiente
-- Usando nombre diferente para evitar conflictos con tabla existente

-- Crear tabla principal de agroquímicos simplificados
CREATE TABLE IF NOT EXISTS agroquimicos_simplificados (
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
    INDEX idx_agroquimicos_simplificados_activo (activo),
    INDEX idx_agroquimicos_simplificados_tipo (tipo),
    INDEX idx_agroquimicos_simplificados_fecha_vencimiento (fecha_vencimiento),
    INDEX idx_agroquimicos_simplificados_stock (stock_actual)
);

-- Crear tabla para cultivos permitidos (relación muchos a muchos)
CREATE TABLE IF NOT EXISTS agroquimicos_simplificados_cultivos (
    agroquimico_id BIGINT NOT NULL,
    cultivo VARCHAR(100) NOT NULL,
    PRIMARY KEY (agroquimico_id, cultivo),
    FOREIGN KEY (agroquimico_id) REFERENCES agroquimicos_simplificados(id) ON DELETE CASCADE
);

-- Insertar datos de prueba
INSERT INTO agroquimicos_simplificados (nombre_comercial, principio_activo, tipo, forma_aplicacion, dosis_por_ha, unidad_medida, fecha_vencimiento, stock_actual, stock_minimo) VALUES
('Glifosato 48%', 'Glifosato', 'HERBICIDA', 'TERRESTRE', 2.5, 'LTS', '2025-12-31', 50.0, 10.0),
('Atrazina 50%', 'Atrazina', 'HERBICIDA', 'TERRESTRE', 3.0, 'LTS', '2025-11-30', 25.0, 5.0),
('Imidacloprid 20%', 'Imidacloprid', 'INSECTICIDA', 'TERRESTRE', 0.5, 'LTS', '2025-10-31', 15.0, 3.0),
('Mancozeb 80%', 'Mancozeb', 'FUNGICIDA', 'TERRESTRE', 2.0, 'KG', '2025-09-30', 30.0, 5.0),
('Aceite Mineral', 'Aceite Mineral', 'COADYUVANTE', 'TERRESTRE', 1.0, 'LTS', '2025-08-31', 40.0, 8.0);

-- Insertar cultivos permitidos
INSERT INTO agroquimicos_simplificados_cultivos (agroquimico_id, cultivo) VALUES
(1, 'Soja'),
(1, 'Maíz'),
(1, 'Trigo'),
(2, 'Maíz'),
(2, 'Sorgo'),
(3, 'Soja'),
(3, 'Maíz'),
(3, 'Girasol'),
(4, 'Soja'),
(4, 'Maíz'),
(4, 'Trigo'),
(5, 'Soja'),
(5, 'Maíz'),
(5, 'Trigo');

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
    a.stock_minimo,
    GROUP_CONCAT(ac.cultivo) as cultivos_permitidos
FROM agroquimicos_simplificados a
LEFT JOIN agroquimicos_simplificados_cultivos ac ON a.id = ac.agroquimico_id
WHERE a.activo = TRUE
GROUP BY a.id
ORDER BY a.nombre_comercial;
