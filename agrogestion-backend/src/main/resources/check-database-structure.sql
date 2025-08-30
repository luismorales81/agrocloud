-- Script para verificar la estructura actual de la base de datos
-- Ejecutar este script para ver qué tablas existen

-- Mostrar todas las tablas en la base de datos
SHOW TABLES;

-- Verificar si existe la tabla users
SELECT 
    TABLE_NAME,
    TABLE_TYPE,
    ENGINE,
    TABLE_ROWS,
    CREATE_TIME
FROM INFORMATION_SCHEMA.TABLES 
WHERE TABLE_SCHEMA = DATABASE() 
AND TABLE_NAME = 'users';

-- Verificar si existe la tabla lotes
SELECT 
    TABLE_NAME,
    TABLE_TYPE,
    ENGINE,
    TABLE_ROWS,
    CREATE_TIME
FROM INFORMATION_SCHEMA.TABLES 
WHERE TABLE_SCHEMA = DATABASE() 
AND TABLE_NAME = 'lotes';

-- Verificar si existe la tabla campos
SELECT 
    TABLE_NAME,
    TABLE_TYPE,
    ENGINE,
    TABLE_ROWS,
    CREATE_TIME
FROM INFORMATION_SCHEMA.TABLES 
WHERE TABLE_SCHEMA = DATABASE() 
AND TABLE_NAME = 'campos';

-- Verificar si existe la tabla ingresos
SELECT 
    TABLE_NAME,
    TABLE_TYPE,
    ENGINE,
    TABLE_ROWS,
    CREATE_TIME
FROM INFORMATION_SCHEMA.TABLES 
WHERE TABLE_SCHEMA = DATABASE() 
AND TABLE_NAME = 'ingresos';

-- Mostrar la estructura de la tabla users si existe
SHOW CREATE TABLE users;

-- Mostrar la estructura de la tabla lotes si existe
SHOW CREATE TABLE lotes;

-- Mostrar la estructura de la tabla campos si existe
SHOW CREATE TABLE campos;
