-- ========================================
-- DATOS DE PRUEBA PARA TESTS AUTOMATIZADOS
-- ========================================
-- Script para insertar datos iniciales para las pruebas

USE agrocloud_test;

-- Insertar roles globales
INSERT INTO roles (nombre, descripcion, activo) VALUES
('SUPERADMIN', 'Controla el sistema completo, puede crear/eliminar empresas y usuarios', TRUE),
('USUARIO_REGISTRADO', 'Usuario común que puede loguearse y acceder a empresas', TRUE),
('INVITADO', 'Acceso muy limitado o de prueba', TRUE);

-- Insertar roles específicos por empresa
INSERT INTO roles (nombre, descripcion, activo) VALUES
('ADMINISTRADOR', 'Administrador de la empresa', TRUE),
('OPERARIO', 'Trabajador de campo', TRUE),
('TECNICO', 'Técnico agrícola', TRUE),
('PRODUCTOR', 'Productor agrícola', TRUE),
('ASESOR', 'Asesor técnico', TRUE),
('MANTENIMIENTO', 'Personal de mantenimiento', TRUE);

-- Insertar usuarios de prueba
INSERT INTO usuarios (nombre_usuario, email, password, nombre, apellido, telefono, activo) VALUES
('superadmin', 'superadmin@agrocloud.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'Super', 'Administrador', '1234567890', TRUE),
('admin1', 'admin1@empresa1.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'Juan', 'Administrador', '1234567891', TRUE),
('operario1', 'operario1@empresa1.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'Carlos', 'Operario', '1234567892', TRUE),
('tecnico1', 'tecnico1@empresa1.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'María', 'Técnica', '1234567893', TRUE),
('productor1', 'productor1@empresa1.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'Pedro', 'Productor', '1234567894', TRUE),
('asesor1', 'asesor1@empresa1.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'Ana', 'Asesora', '1234567895', TRUE),
('mantenimiento1', 'mantenimiento1@empresa1.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'Luis', 'Mantenimiento', '1234567896', TRUE);

-- Asignar roles globales a usuarios
INSERT INTO usuario_roles (usuario_id, rol_id) VALUES
(1, 1), -- superadmin -> SUPERADMIN
(2, 2), -- admin1 -> USUARIO_REGISTRADO
(3, 2), -- operario1 -> USUARIO_REGISTRADO
(4, 2), -- tecnico1 -> USUARIO_REGISTRADO
(5, 2), -- productor1 -> USUARIO_REGISTRADO
(6, 2), -- asesor1 -> USUARIO_REGISTRADO
(7, 2); -- mantenimiento1 -> USUARIO_REGISTRADO

-- Insertar empresas de prueba
INSERT INTO empresas (nombre, descripcion, direccion, telefono, email, estado, activo) VALUES
('Empresa Agrícola Test 1', 'Empresa de prueba para tests automatizados', 'Ruta 1 Km 10', '011-1234-5678', 'contacto@empresa1.com', 'ACTIVO', TRUE),
('Empresa Agrícola Test 2', 'Segunda empresa de prueba', 'Ruta 2 Km 15', '011-2345-6789', 'contacto@empresa2.com', 'ACTIVO', TRUE);

-- Asignar roles específicos por empresa
INSERT INTO usuario_empresa_roles (usuario_id, empresa_id, rol_nombre, estado) VALUES
(2, 1, 'ADMINISTRADOR', 'ACTIVO'),
(3, 1, 'OPERARIO', 'ACTIVO'),
(4, 1, 'TECNICO', 'ACTIVO'),
(5, 1, 'PRODUCTOR', 'ACTIVO'),
(6, 1, 'ASESOR', 'ACTIVO'),
(7, 1, 'MANTENIMIENTO', 'ACTIVO');

-- Insertar campos de prueba
INSERT INTO campos (nombre, ubicacion, area_hectareas, tipo_suelo, descripcion, estado, activo, usuario_id, empresa_id) VALUES
('Campo Norte', 'Zona Norte - Departamento Test', 100.50, 'Franco', 'Campo principal para pruebas de cultivos', 'ACTIVO', TRUE, 2, 1),
('Campo Sur', 'Zona Sur - Departamento Test', 75.25, 'Franco-arcilloso', 'Campo secundario para pruebas', 'ACTIVO', TRUE, 2, 1);

-- Insertar lotes de prueba
INSERT INTO lotes (nombre, descripcion, area_hectareas, estado, tipo_suelo, activo, campo_id, usuario_id, empresa_id) VALUES
('Lote A1', 'Lote de prueba para soja', 25.50, 'DISPONIBLE', 'Franco', TRUE, 1, 2, 1),
('Lote A2', 'Lote de prueba para maíz', 30.00, 'DISPONIBLE', 'Franco', TRUE, 1, 2, 1),
('Lote B1', 'Lote de prueba para trigo', 20.75, 'PREPARADO', 'Franco-arcilloso', TRUE, 2, 2, 1),
('Lote B2', 'Lote de prueba para girasol', 24.00, 'SEMBRADO', 'Franco-arcilloso', TRUE, 2, 2, 1);

-- Insertar cultivos de prueba
INSERT INTO cultivos (nombre, tipo, variedad, ciclo_dias, rendimiento_esperado, unidad_rendimiento, precio_por_tonelada, descripcion, estado, activo, usuario_id, empresa_id) VALUES
('Soja', 'Oleaginosa', 'DM 53i53', 120, 3500.00, 'kg/ha', 450.00, 'Soja de primera calidad', 'ACTIVO', TRUE, 2, 1),
('Maíz', 'Cereal', 'DK 72-10', 140, 12000.00, 'kg/ha', 180.00, 'Maíz híbrido de alto rendimiento', 'ACTIVO', TRUE, 2, 1),
('Trigo', 'Cereal', 'Baguette 11', 180, 4000.00, 'kg/ha', 250.00, 'Trigo panadero', 'ACTIVO', TRUE, 2, 1),
('Girasol', 'Oleaginosa', 'Paraíso 31', 110, 2500.00, 'kg/ha', 380.00, 'Girasol confitero', 'ACTIVO', TRUE, 2, 1);

-- Insertar insumos de prueba
INSERT INTO insumos (nombre, tipo, descripcion, unidad_medida, precio_unitario, stock_disponible, stock_minimo, proveedor, activo, usuario_id, empresa_id) VALUES
('Semilla Soja DM 53i53', 'Semilla', 'Semilla de soja de primera calidad', 'kg', 15.50, 1000.00, 100.00, 'Semillas del Norte', TRUE, 2, 1),
('Fertilizante Urea', 'Fertilizante', 'Fertilizante nitrogenado', 'kg', 0.85, 5000.00, 500.00, 'Fertilizantes SA', TRUE, 2, 1),
('Herbicida Glifosato', 'Agroquímico', 'Herbicida no selectivo', 'litro', 8.50, 200.00, 50.00, 'Agroquímicos del Sur', TRUE, 2, 1),
('Insecticida Deltametrina', 'Agroquímico', 'Insecticida piretroide', 'litro', 12.00, 100.00, 25.00, 'Agroquímicos del Sur', TRUE, 2, 1);

-- Insertar maquinaria de prueba
INSERT INTO maquinaria (nombre, tipo, marca, modelo, año, descripcion, estado, costo_por_hora, activo, usuario_id, empresa_id) VALUES
('Tractor John Deere', 'Tractor', 'John Deere', '6120R', 2020, 'Tractor de 120 HP', 'DISPONIBLE', 25.00, TRUE, 2, 1),
('Sembradora Neumática', 'Sembradora', 'Agrometal', 'AS-3000', 2019, 'Sembradora de 24 surcos', 'DISPONIBLE', 15.00, TRUE, 2, 1),
('Pulverizadora', 'Pulverizadora', 'Metalfor', 'Fumigadora 2000', 2021, 'Pulverizadora de 2000 litros', 'DISPONIBLE', 20.00, TRUE, 2, 1),
('Cosechadora', 'Cosechadora', 'Case IH', 'Axial Flow 2388', 2018, 'Cosechadora de 300 HP', 'EN_MANTENIMIENTO', 35.00, TRUE, 2, 1);

-- Insertar labores de prueba
INSERT INTO labores (tipo_labor, fecha_inicio, fecha_fin, descripcion, costo_total, estado, observaciones, responsable, activo, lote_id, usuario_id, empresa_id) VALUES
('SIEMBRA', '2024-01-15', '2024-01-15', 'Siembra de soja en lote A1', 1250.00, 'COMPLETADA', 'Siembra realizada con sembradora neumática', 'Carlos Operario', TRUE, 1, 3, 1),
('FERTILIZACION', '2024-02-01', '2024-02-01', 'Fertilización nitrogenada', 850.00, 'COMPLETADA', 'Aplicación de urea', 'María Técnica', TRUE, 1, 4, 1),
('RIEGO', '2024-02-15', '2024-02-15', 'Riego complementario', 300.00, 'COMPLETADA', 'Riego por aspersión', 'Carlos Operario', TRUE, 1, 3, 1),
('COSECHA', '2024-05-15', '2024-05-16', 'Cosecha de soja', 2100.00, 'PLANIFICADA', 'Cosecha programada para mayo', 'Carlos Operario', TRUE, 1, 3, 1);

-- Insertar ingresos de prueba
INSERT INTO ingresos (concepto, descripcion, tipo_ingreso, fecha_ingreso, monto, unidad_medida, cantidad, cliente_comprador, estado, lote_id, usuario_id, empresa_id) VALUES
('Venta Soja 2023', 'Venta de soja cosecha 2023', 'VENTA_CULTIVO', '2023-06-15', 45000.00, 'toneladas', 100.00, 'Acopio del Norte', 'CONFIRMADO', 1, 2, 1),
('Venta Maíz 2023', 'Venta de maíz cosecha 2023', 'VENTA_CULTIVO', '2023-07-20', 36000.00, 'toneladas', 200.00, 'Acopio del Sur', 'CONFIRMADO', 2, 2, 1);

-- Insertar egresos de prueba
INSERT INTO egresos (concepto, descripcion, tipo_egreso, fecha_egreso, monto, proveedor, estado, lote_id, usuario_id, empresa_id) VALUES
('Compra Semillas', 'Compra de semillas para siembra', 'INSUMOS', '2024-01-10', 15500.00, 'Semillas del Norte', 'PAGADO', 1, 2, 1),
('Compra Fertilizantes', 'Compra de fertilizantes', 'INSUMOS', '2024-01-12', 4250.00, 'Fertilizantes SA', 'PAGADO', 1, 2, 1),
('Mantenimiento Maquinaria', 'Mantenimiento de cosechadora', 'MANTENIMIENTO', '2024-01-20', 2500.00, 'Taller Mecánico Central', 'PENDIENTE', NULL, 2, 1);

-- Insertar cosechas de prueba
INSERT INTO cosechas (lote_id, cultivo_id, fecha_cosecha, cantidad_cosechada, unidad_medida, rendimiento, precio_por_unidad, valor_total, observaciones, activo, usuario_id, empresa_id) VALUES
(1, 1, '2023-06-10', 89.25, 'toneladas', 3500.00, 450.00, 40162.50, 'Cosecha de soja con buen rendimiento', TRUE, 2, 1),
(2, 2, '2023-07-15', 360.00, 'toneladas', 12000.00, 180.00, 64800.00, 'Cosecha de maíz excelente', TRUE, 2, 1);

-- Insertar historial de cosechas
INSERT INTO historial_cosechas (lote_id, cultivo_id, fecha_cosecha, cantidad_cosechada, unidad_medida, rendimiento, precio_por_unidad, valor_total, observaciones, usuario_id, empresa_id) VALUES
(1, 1, '2023-06-10', 89.25, 'toneladas', 3500.00, 450.00, 40162.50, 'Cosecha de soja con buen rendimiento', 2, 1),
(2, 2, '2023-07-15', 360.00, 'toneladas', 12000.00, 180.00, 64800.00, 'Cosecha de maíz excelente', 2, 1),
(1, 1, '2022-06-05', 78.50, 'toneladas', 3080.00, 420.00, 32970.00, 'Cosecha de soja año anterior', 2, 1);
