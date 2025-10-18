-- ========================================
-- CREAR VISTAS EN RAILWAY MYSQL
-- ========================================

USE railway;

-- Vista 1: vista_historial_cosechas_completo
CREATE OR REPLACE VIEW vista_historial_cosechas_completo AS
SELECT 
    c.id AS cosecha_id,
    c.lote_id,
    l.nombre AS lote_nombre,
    c.cultivo_id,
    cult.nombre AS cultivo_nombre,
    c.fecha_siembra,
    c.fecha_cosecha,
    c.superficie_hectareas,
    c.rendimiento_kg_ha,
    c.produccion_total_kg,
    c.humedad_porcentaje,
    c.precio_venta_por_kg,
    c.ingreso_total,
    c.costo_total,
    c.rentabilidad,
    c.observaciones,
    c.created_at,
    c.updated_at
FROM cosechas c
LEFT JOIN lotes l ON c.lote_id = l.id
LEFT JOIN cultivos cult ON c.cultivo_id = cult.id;

-- Vista 2: vista_balance_mensual
CREATE OR REPLACE VIEW vista_balance_mensual AS
SELECT 
    DATE_FORMAT(fecha, '%Y-%m') AS mes,
    SUM(CASE WHEN tipo = 'INGRESO' THEN monto ELSE 0 END) AS total_ingresos,
    SUM(CASE WHEN tipo = 'COSTO' THEN monto ELSE 0 END) AS total_costos,
    SUM(CASE WHEN tipo = 'INGRESO' THEN monto ELSE -monto END) AS balance
FROM (
    SELECT fecha_ingreso AS fecha, monto, 'INGRESO' AS tipo FROM ingresos
    UNION ALL
    SELECT fecha_egreso AS fecha, monto, 'COSTO' AS tipo FROM egresos
) AS balance_completo
GROUP BY DATE_FORMAT(fecha, '%Y-%m')
ORDER BY mes DESC;

-- Vista 3: vista_lotes_con_cultivo
CREATE OR REPLACE VIEW vista_lotes_con_cultivo AS
SELECT 
    l.id AS lote_id,
    l.nombre AS lote_nombre,
    l.area_hectareas,
    l.estado,
    l.tipo_suelo,
    l.campo_id,
    c.nombre AS campo_nombre,
    cult.id AS cultivo_id,
    cult.nombre AS cultivo_nombre,
    cult.tipo AS cultivo_tipo,
    l.created_at,
    l.updated_at
FROM lotes l
LEFT JOIN campos c ON l.campo_id = c.id
LEFT JOIN cultivos cult ON l.cultivo_actual = cult.id;

-- Vista 4: vista_labores_detalladas
CREATE OR REPLACE VIEW vista_labores_detalladas AS
SELECT 
    lab.id AS labor_id,
    lab.tipo AS labor_tipo,
    lab.fecha_inicio,
    lab.fecha_fin,
    lab.estado,
    lab.observaciones,
    lab.lote_id,
    l.nombre AS lote_nombre,
    lab.responsable,
    lab.horas_trabajo,
    lab.costo_total,
    lab.costo_base,
    lab.costo_maquinaria,
    lab.costo_mano_obra,
    lab.created_at,
    lab.updated_at
FROM labores lab
LEFT JOIN lotes l ON lab.lote_id = l.id
WHERE lab.activo = 1;

-- Vista 5: vista_inventario_granos_actual
CREATE OR REPLACE VIEW vista_inventario_granos_actual AS
SELECT 
    ig.id AS inventario_id,
    ig.lote_id,
    l.nombre AS lote_nombre,
    ig.cultivo_id,
    cult.nombre AS cultivo_nombre,
    ig.cantidad_kg,
    ig.precio_por_kg,
    ig.valor_total,
    ig.ubicacion,
    ig.condiciones_almacenamiento,
    ig.fecha_ingreso,
    ig.created_at,
    ig.updated_at
FROM inventario_granos ig
LEFT JOIN lotes l ON ig.lote_id = l.id
LEFT JOIN cultivos cult ON ig.cultivo_id = cult.id
WHERE ig.cantidad_kg > 0;

-- Vista 6: vista_insumos_stock_bajo
CREATE OR REPLACE VIEW vista_insumos_stock_bajo AS
SELECT 
    id,
    nombre,
    tipo,
    stock_actual,
    unidad_medida,
    precio_unitario,
    stock_minimo,
    CASE 
        WHEN stock_actual <= stock_minimo THEN 'CRÍTICO'
        WHEN stock_actual <= (stock_minimo * 1.5) THEN 'BAJO'
        ELSE 'NORMAL'
    END AS nivel_stock,
    proveedor,
    fecha_vencimiento,
    created_at,
    updated_at
FROM insumos
WHERE stock_actual <= (stock_minimo * 1.5)
ORDER BY stock_actual ASC;

-- Vista 7: vista_maquinaria_mantenimiento
CREATE OR REPLACE VIEW vista_maquinaria_mantenimiento AS
SELECT 
    id,
    nombre,
    tipo,
    marca,
    modelo,
    año,
    estado,
    kilometros_uso,
    kilometros_ultimo_mantenimiento,
    fecha_ultimo_mantenimiento,
    kilometros_proximo_mantenimiento,
    costo_por_hora,
    observaciones,
    created_at,
    updated_at,
    CASE 
        WHEN kilometros_uso >= kilometros_proximo_mantenimiento THEN 'URGENTE'
        WHEN kilometros_uso >= (kilometros_proximo_mantenimiento * 0.8) THEN 'PRÓXIMO'
        ELSE 'OK'
    END AS estado_mantenimiento
FROM maquinaria
WHERE estado = 'ACTIVA'
ORDER BY kilometros_uso DESC;

-- Vista 8: vista_usuarios_empresas
CREATE OR REPLACE VIEW vista_usuarios_empresas AS
SELECT 
    u.id AS usuario_id,
    u.nombre AS usuario_nombre,
    u.email AS usuario_email,
    u.activo AS usuario_activo,
    e.id AS empresa_id,
    e.nombre AS empresa_nombre,
    e.razon_social AS empresa_razon_social,
    ue.rol AS rol_usuario,
    ue.estado AS estado_rol,
    ue.fecha_asignacion,
    u.created_at AS usuario_created_at,
    e.created_at AS empresa_created_at
FROM usuarios u
LEFT JOIN usuario_empresas ue ON u.id = ue.usuario_id
LEFT JOIN empresas e ON ue.empresa_id = e.id;

-- Vista 9: vista_cosechas_por_cultivo
CREATE OR REPLACE VIEW vista_cosechas_por_cultivo AS
SELECT 
    cult.id AS cultivo_id,
    cult.nombre AS cultivo_nombre,
    cult.tipo AS cultivo_tipo,
    COUNT(c.id) AS total_cosechas,
    SUM(c.superficie_hectareas) AS superficie_total,
    AVG(c.rendimiento_kg_ha) AS rendimiento_promedio,
    SUM(c.produccion_total_kg) AS produccion_total,
    SUM(c.ingreso_total) AS ingreso_total,
    SUM(c.costo_total) AS costo_total,
    SUM(c.rentabilidad) AS rentabilidad_total
FROM cultivos cult
LEFT JOIN cosechas c ON cult.id = c.cultivo_id
GROUP BY cult.id, cult.nombre, cult.tipo;

-- Vista 10: vista_labores_por_tipo
CREATE OR REPLACE VIEW vista_labores_por_tipo AS
SELECT 
    tipo AS labor_tipo,
    COUNT(*) AS total_labores,
    SUM(horas_trabajo) AS total_horas,
    SUM(costo_total) AS costo_total,
    AVG(costo_total) AS costo_promedio,
    SUM(CASE WHEN estado = 'COMPLETADA' THEN 1 ELSE 0 END) AS labores_completadas,
    SUM(CASE WHEN estado = 'EN_PROGRESO' THEN 1 ELSE 0 END) AS labores_en_progreso,
    SUM(CASE WHEN estado = 'PLANIFICADA' THEN 1 ELSE 0 END) AS labores_planificadas
FROM labores
WHERE activo = 1
GROUP BY tipo
ORDER BY total_labores DESC;

-- Vista 11: vista_ingresos_egresos_mensual
CREATE OR REPLACE VIEW vista_ingresos_egresos_mensual AS
SELECT 
    DATE_FORMAT(fecha, '%Y-%m') AS mes,
    DATE_FORMAT(fecha, '%Y') AS año,
    DATE_FORMAT(fecha, '%m') AS mes_numero,
    SUM(CASE WHEN tipo = 'INGRESO' THEN monto ELSE 0 END) AS total_ingresos,
    SUM(CASE WHEN tipo = 'COSTO' THEN monto ELSE 0 END) AS total_egresos,
    SUM(CASE WHEN tipo = 'INGRESO' THEN monto ELSE -monto END) AS balance_neto,
    COUNT(CASE WHEN tipo = 'INGRESO' THEN 1 END) AS cantidad_ingresos,
    COUNT(CASE WHEN tipo = 'COSTO' THEN 1 END) AS cantidad_egresos
FROM (
    SELECT fecha_ingreso AS fecha, monto, 'INGRESO' AS tipo FROM ingresos
    UNION ALL
    SELECT fecha_egreso AS fecha, monto, 'COSTO' AS tipo FROM egresos
) AS transacciones
GROUP BY DATE_FORMAT(fecha, '%Y-%m'), DATE_FORMAT(fecha, '%Y'), DATE_FORMAT(fecha, '%m')
ORDER BY mes DESC;

-- Vista 12: vista_campos_con_lotes
CREATE OR REPLACE VIEW vista_campos_con_lotes AS
SELECT 
    c.id AS campo_id,
    c.nombre AS campo_nombre,
    c.superficie_total_hectareas,
    c.ubicacion,
    c.tipo_suelo,
    c.estado,
    COUNT(l.id) AS cantidad_lotes,
    SUM(l.area_hectareas) AS superficie_ocupada,
    c.superficie_total_hectareas - COALESCE(SUM(l.area_hectareas), 0) AS superficie_disponible,
    c.created_at,
    c.updated_at
FROM campos c
LEFT JOIN lotes l ON c.id = l.campo_id
GROUP BY c.id, c.nombre, c.superficie_total_hectareas, c.ubicacion, c.tipo_suelo, c.estado, c.created_at, c.updated_at;

-- Vista 13: vista_rendimiento_por_lote
CREATE OR REPLACE VIEW vista_rendimiento_por_lote AS
SELECT 
    l.id AS lote_id,
    l.nombre AS lote_nombre,
    l.area_hectareas,
    c.id AS cosecha_id,
    c.fecha_siembra,
    c.fecha_cosecha,
    cult.nombre AS cultivo_nombre,
    c.rendimiento_kg_ha,
    c.produccion_total_kg,
    c.ingreso_total,
    c.costo_total,
    c.rentabilidad,
    DATEDIFF(c.fecha_cosecha, c.fecha_siembra) AS dias_ciclo
FROM lotes l
LEFT JOIN cosechas c ON l.id = c.lote_id
LEFT JOIN cultivos cult ON c.cultivo_id = cult.id
WHERE c.id IS NOT NULL
ORDER BY c.fecha_cosecha DESC;

-- Vista 14: vista_costos_por_labor
CREATE OR REPLACE VIEW vista_costos_por_labor AS
SELECT 
    l.id AS labor_id,
    l.tipo AS labor_tipo,
    l.lote_id,
    lot.nombre AS lote_nombre,
    l.fecha_inicio,
    l.fecha_fin,
    l.responsable,
    l.costo_base,
    l.costo_maquinaria,
    l.costo_mano_obra,
    l.costo_total,
    l.horas_trabajo,
    CASE 
        WHEN l.horas_trabajo > 0 THEN l.costo_total / l.horas_trabajo
        ELSE 0
    END AS costo_por_hora
FROM labores l
LEFT JOIN lotes lot ON l.lote_id = lot.id
WHERE l.activo = 1
ORDER BY l.fecha_inicio DESC;

-- Vista 15: vista_inventario_valor_total
CREATE OR REPLACE VIEW vista_inventario_valor_total AS
SELECT 
    cult.id AS cultivo_id,
    cult.nombre AS cultivo_nombre,
    SUM(ig.cantidad_kg) AS cantidad_total_kg,
    AVG(ig.precio_por_kg) AS precio_promedio_kg,
    SUM(ig.valor_total) AS valor_total_inventario,
    COUNT(ig.id) AS cantidad_lotes
FROM cultivos cult
LEFT JOIN inventario_granos ig ON cult.id = ig.cultivo_id
GROUP BY cult.id, cult.nombre
HAVING cantidad_total_kg > 0
ORDER BY valor_total_inventario DESC;

-- Mensaje de confirmación
SELECT 'Vistas creadas exitosamente' AS mensaje;

