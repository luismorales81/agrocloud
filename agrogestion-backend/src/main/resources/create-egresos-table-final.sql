-- Script final para crear la tabla de egresos
-- Ejecutar en la base de datos agrocloud

-- Crear tabla de egresos sin claves foráneas
CREATE TABLE IF NOT EXISTS egresos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    concepto VARCHAR(200) NOT NULL,
    descripcion VARCHAR(500),
    tipo_egreso ENUM('INSUMOS', 'COMBUSTIBLE', 'MANO_OBRA', 'MAQUINARIA', 'SERVICIOS', 'IMPUESTOS', 'OTROS_EGRESOS') NOT NULL,
    fecha_egreso DATE NOT NULL,
    monto DECIMAL(15,2) NOT NULL,
    unidad_medida VARCHAR(100),
    cantidad DECIMAL(10,2),
    proveedor VARCHAR(200),
    estado ENUM('REGISTRADO', 'PAGADO', 'CANCELADO') DEFAULT 'REGISTRADO',
    observaciones VARCHAR(1000),
    lote_id BIGINT NULL,
    insumo_id BIGINT NULL,
    usuario_id BIGINT NOT NULL,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Crear índices para mejorar el rendimiento
CREATE INDEX idx_egresos_usuario_fecha ON egresos(usuario_id, fecha_egreso);
CREATE INDEX idx_egresos_lote ON egresos(lote_id);
CREATE INDEX idx_egresos_insumo ON egresos(insumo_id);
CREATE INDEX idx_egresos_tipo ON egresos(tipo_egreso);
CREATE INDEX idx_egresos_estado ON egresos(estado);

-- Insertar datos de ejemplo
INSERT INTO egresos (concepto, descripcion, tipo_egreso, fecha_egreso, monto, unidad_medida, cantidad, proveedor, estado, lote_id, usuario_id) VALUES
('Compra de fertilizante', 'Fertilizante NPK para el cultivo de soja', 'INSUMOS', '2025-01-15', 25000.00, 'kg', 500.00, 'Agroinsumos S.A.', 'PAGADO', NULL, 1),
('Combustible para tractor', 'Diesel para labores de siembra', 'COMBUSTIBLE', '2025-01-20', 15000.00, 'litros', 200.00, 'YPF', 'PAGADO', NULL, 1),
('Mano de obra cosecha', 'Pago a trabajadores temporarios', 'MANO_OBRA', '2025-02-10', 45000.00, 'jornales', 30.00, 'Cuadrilla local', 'PAGADO', NULL, 1),
('Mantenimiento sembradora', 'Reparación y ajuste de sembradora', 'MAQUINARIA', '2025-01-25', 12000.00, 'servicio', 1.00, 'Taller Mecánico Rural', 'PAGADO', NULL, 1),
('Impuestos rurales', 'Impuesto inmobiliario rural', 'IMPUESTOS', '2025-01-30', 8000.00, 'cuota', 1.00, 'Municipalidad', 'PAGADO', NULL, 1),
('Compra de semillas', 'Semillas de soja certificadas', 'INSUMOS', '2025-01-10', 35000.00, 'kg', 100.00, 'Semillas del Sur', 'PAGADO', NULL, 1),
('Servicio de fumigación', 'Aplicación de herbicidas', 'SERVICIOS', '2025-02-05', 18000.00, 'hectáreas', 50.00, 'Fumigaciones Express', 'PAGADO', NULL, 1),
('Compra de herbicidas', 'Glifosato para control de malezas', 'INSUMOS', '2025-01-18', 22000.00, 'litros', 100.00, 'Agroquímicos Central', 'PAGADO', NULL, 1);

-- Verificar que la tabla se creó correctamente
SELECT 'Tabla egresos creada exitosamente' as mensaje;
SELECT COUNT(*) as total_egresos FROM egresos;
