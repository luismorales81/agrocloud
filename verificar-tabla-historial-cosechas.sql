-- Script para verificar la estructura de la tabla historial_cosechas
-- Ejecutar después de que Flyway haya aplicado la migración V1.4

-- 1. Verificar que la tabla existe
SELECT 'Verificando existencia de la tabla historial_cosechas...' AS mensaje;
SHOW TABLES LIKE 'historial_cosechas';

-- 2. Verificar la estructura de la tabla
SELECT 'Estructura de la tabla historial_cosechas:' AS mensaje;
DESCRIBE historial_cosechas;

-- 3. Verificar las restricciones (foreign keys)
SELECT 'Restricciones de la tabla historial_cosechas:' AS mensaje;
SELECT 
    CONSTRAINT_NAME,
    CONSTRAINT_TYPE,
    TABLE_NAME,
    COLUMN_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE 
WHERE TABLE_NAME = 'historial_cosechas' 
AND TABLE_SCHEMA = DATABASE();

-- 4. Verificar los índices
SELECT 'Índices de la tabla historial_cosechas:' AS mensaje;
SHOW INDEX FROM historial_cosechas;

-- 5. Verificar las vistas creadas
SELECT 'Vistas relacionadas con historial_cosechas:' AS mensaje;
SHOW FULL TABLES WHERE Table_type = 'VIEW' AND Table_name LIKE '%historial%';

-- 6. Verificar comentarios de la tabla
SELECT 'Comentarios de la tabla historial_cosechas:' AS mensaje;
SELECT 
    COLUMN_NAME,
    COLUMN_COMMENT,
    DATA_TYPE,
    IS_NULLABLE,
    COLUMN_DEFAULT
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'historial_cosechas' 
AND TABLE_SCHEMA = DATABASE()
ORDER BY ORDINAL_POSITION;

-- 7. Verificar restricciones CHECK
SELECT 'Restricciones CHECK de la tabla historial_cosechas:' AS mensaje;
SELECT 
    CONSTRAINT_NAME,
    CHECK_CLAUSE
FROM INFORMATION_SCHEMA.CHECK_CONSTRAINTS 
WHERE CONSTRAINT_SCHEMA = DATABASE()
AND CONSTRAINT_NAME LIKE '%historial%';

-- 8. Contar registros (debería ser 0 inicialmente)
SELECT 'Cantidad de registros en historial_cosechas:' AS mensaje;
SELECT COUNT(*) AS total_registros FROM historial_cosechas;

-- 9. Verificar que las tablas relacionadas existen
SELECT 'Verificando tablas relacionadas:' AS mensaje;
SELECT 
    'lotes' AS tabla,
    COUNT(*) AS registros
FROM lotes
UNION ALL
SELECT 
    'cultivos' AS tabla,
    COUNT(*) AS registros
FROM cultivos
UNION ALL
SELECT 
    'users' AS tabla,
    COUNT(*) AS registros
FROM users;

-- 10. Verificar permisos de la tabla
SELECT 'Permisos de la tabla historial_cosechas:' AS mensaje;
SELECT 
    GRANTEE,
    PRIVILEGE_TYPE,
    IS_GRANTABLE
FROM INFORMATION_SCHEMA.TABLE_PRIVILEGES 
WHERE TABLE_NAME = 'historial_cosechas' 
AND TABLE_SCHEMA = DATABASE();

-- Mensaje final
SELECT 'Verificación completada. Si todos los resultados son correctos, la tabla está lista para usar.' AS mensaje;
