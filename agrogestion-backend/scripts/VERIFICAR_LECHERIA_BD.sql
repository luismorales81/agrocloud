-- Verificación rápida del esquema y datos del módulo Lechería
-- Uso: mysql -u root -p agrocloud < scripts/VERIFICAR_LECHERIA_BD.sql

SELECT 'flyway' AS seccion, version, description, success
FROM flyway_schema_history
WHERE version IN ('1.158', '1.159', '1.160')
ORDER BY installed_rank;

SELECT 'tablas' AS seccion, table_name
FROM information_schema.tables
WHERE table_schema = DATABASE()
  AND table_name LIKE 'lecheria_%'
ORDER BY table_name;

SELECT 'modulo' AS seccion, m.code, cm.enabled, e.nombre AS empresa
FROM modules m
LEFT JOIN company_modules cm ON cm.module_id = m.id
LEFT JOIN empresas e ON e.id = cm.company_id
WHERE m.code = 'LECHERIA'
ORDER BY e.id;

SELECT 'parametros_especie' AS seccion, COUNT(*) AS total FROM lecheria_parametro_especie;

SELECT 'animales_demo' AS seccion, COUNT(*) AS total
FROM lecheria_animal WHERE empresa_id = 1 AND activo = TRUE;
