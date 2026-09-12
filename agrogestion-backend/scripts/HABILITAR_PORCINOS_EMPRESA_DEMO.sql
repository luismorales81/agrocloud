-- =============================================================================
-- Habilitar módulo Porcinos (pigs) para empresa demo / prueba
-- =============================================================================
-- Corrige error 402 en /api/v1/porcinos/planes-recria
--
-- Uso:
--   mysql -u root -p agrocloud < HABILITAR_PORCINOS_EMPRESA_DEMO.sql
--   o: scripts\ejecutar-habilitar-porcinos-demo.bat
-- =============================================================================

SET @USUARIO_DEMO   := 'admin';
SET @NOMBRE_EMPRESA := 'AgroCloud Demo';
SET @EMPRESA_ID     := NULL;

SET @EMPRESA_RESUELTA := @EMPRESA_ID;

SET @EMPRESA_RESUELTA := COALESCE(
    @EMPRESA_RESUELTA,
    (SELECT e.id FROM empresas e WHERE e.nombre = @NOMBRE_EMPRESA LIMIT 1),
    (
        SELECT ue.empresa_id
        FROM usuario_empresas ue
        INNER JOIN usuarios u ON u.id = ue.usuario_id
        WHERE u.username = @USUARIO_DEMO AND ue.estado = 'ACTIVO'
        ORDER BY ue.id
        LIMIT 1
    ),
    (
        SELECT uer.empresa_id
        FROM usuarios_empresas_roles uer
        INNER JOIN usuarios u ON u.id = uer.usuario_id
        WHERE u.username = @USUARIO_DEMO
        ORDER BY uer.id
        LIMIT 1
    ),
    1
);

SELECT @EMPRESA_RESUELTA AS empresa_id_destino,
       (SELECT nombre FROM empresas WHERE id = @EMPRESA_RESUELTA LIMIT 1) AS empresa_nombre;

INSERT INTO modules (name, code, description, active)
SELECT 'Porcinos', 'pigs', 'Módulo de gestión de porcinos', TRUE
WHERE NOT EXISTS (SELECT 1 FROM modules WHERE code = 'pigs');

UPDATE modules SET active = TRUE WHERE code = 'pigs';

SET @MODULO_PORCINOS_ID := (SELECT id FROM modules WHERE code = 'pigs' LIMIT 1);

INSERT INTO company_modules (company_id, module_id, enabled)
SELECT @EMPRESA_RESUELTA, @MODULO_PORCINOS_ID, TRUE
WHERE @EMPRESA_RESUELTA IS NOT NULL
  AND @MODULO_PORCINOS_ID IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM company_modules cm
      WHERE cm.company_id = @EMPRESA_RESUELTA AND cm.module_id = @MODULO_PORCINOS_ID
  );

UPDATE company_modules cm
INNER JOIN modules m ON m.id = cm.module_id
SET cm.enabled = TRUE
WHERE cm.company_id = @EMPRESA_RESUELTA AND m.code = 'pigs';

SELECT e.id AS empresa_id, e.nombre AS empresa, m.code AS modulo, cm.enabled AS habilitado
FROM company_modules cm
INNER JOIN empresas e ON e.id = cm.company_id
INNER JOIN modules m ON m.id = cm.module_id
WHERE e.id = @EMPRESA_RESUELTA AND m.code = 'pigs';

SELECT 'PORCINOS habilitado' AS resultado, @EMPRESA_RESUELTA AS empresa_id;
