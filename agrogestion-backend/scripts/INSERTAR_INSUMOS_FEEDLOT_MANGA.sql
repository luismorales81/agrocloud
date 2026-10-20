-- Insumos de manga/feedlot para la empresa local Docker.
-- La tabla cultivo_insumos suele quedar vacía (no hay tabla legacy `insumos` para copiar).

SET @USUARIO_ID = (SELECT id FROM usuarios ORDER BY id LIMIT 1);
SET @EMPRESA_ID = (SELECT id FROM empresas ORDER BY id LIMIT 1);

INSERT INTO cultivo_insumos (
    nombre, descripcion, tipo, unidad_medida, precio_unitario,
    stock_minimo, stock_actual, activo, user_id, empresa_id, usuario_id,
    fecha_creacion, fecha_actualizacion
)
SELECT v.nombre, v.descripcion, v.tipo, v.unidad_medida, v.precio_unitario,
       v.stock_minimo, v.stock_actual, TRUE, @USUARIO_ID, @EMPRESA_ID, @USUARIO_ID,
       NOW(), NOW()
FROM (
    SELECT 'Vacuna aftosa' AS nombre, 'Vacuna para sesión de sanidad en manga' AS descripcion,
           'OTROS' AS tipo, 'ml' AS unidad_medida, 250.00 AS precio_unitario,
           10.00 AS stock_minimo, 500.00 AS stock_actual
    UNION ALL
    SELECT 'Vacuna clostridial', 'Vacuna polivalente clostridial', 'OTROS', 'ml', 180.00, 10.00, 300.00
    UNION ALL
    SELECT 'Ivermectina', 'Antiparasitario inyectable', 'OTROS', 'ml', 90.00, 10.00, 200.00
    UNION ALL
    SELECT 'Balanceado feedlot', 'Ración de engorde', 'OTROS', 'kg', 420.00, 100.00, 2000.00
    UNION ALL
    SELECT 'Maíz molido', 'Grano para dieta de feedlot', 'OTROS', 'kg', 300.00, 100.00, 1500.00
) v
WHERE @EMPRESA_ID IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM cultivo_insumos c
      WHERE c.nombre = v.nombre
        AND (c.empresa_id = @EMPRESA_ID OR c.empresa_id IS NULL)
  );

SELECT id, nombre, tipo, unidad_medida, stock_actual, empresa_id, activo
FROM cultivo_insumos
ORDER BY id;
