-- =====================================================
-- SCRIPT DE VERIFICACIÓN DEL ESTADO DE LA BASE DE DATOS
-- =====================================================

-- Verificar si estamos en la base de datos correcta
SELECT 'VERIFICACIÓN DE BASE DE DATOS ACTUAL:' as titulo;
SELECT DATABASE() as base_actual;

-- Verificar si la base de datos agroclouddb existe
SELECT 'VERIFICACIÓN DE EXISTENCIA DE AGROCLOUDBD:' as titulo;
SELECT SCHEMA_NAME as base_datos
FROM INFORMATION_SCHEMA.SCHEMATA 
WHERE SCHEMA_NAME = 'agroclouddb';

-- Cambiar a agroclouddb si existe
USE agroclouddb;

-- Verificar tablas existentes
SELECT 'TABLAS EXISTENTES EN AGROCLOUDBD:' as titulo;
SELECT 
    TABLE_NAME,
    TABLE_ROWS,
    TABLE_TYPE
FROM INFORMATION_SCHEMA.TABLES 
WHERE TABLE_SCHEMA = 'agroclouddb'
ORDER BY TABLE_NAME;

-- Verificar si las tablas principales existen
SELECT 'VERIFICACIÓN DE TABLAS PRINCIPALES:' as titulo;

SELECT 
    'roles' as tabla,
    CASE 
        WHEN COUNT(*) > 0 THEN 'EXISTE'
        ELSE 'NO EXISTE'
    END as estado
FROM INFORMATION_SCHEMA.TABLES 
WHERE TABLE_SCHEMA = 'agroclouddb' AND TABLE_NAME = 'roles'

UNION ALL

SELECT 
    'usuarios' as tabla,
    CASE 
        WHEN COUNT(*) > 0 THEN 'EXISTE'
        ELSE 'NO EXISTE'
    END as estado
FROM INFORMATION_SCHEMA.TABLES 
WHERE TABLE_SCHEMA = 'agroclouddb' AND TABLE_NAME = 'usuarios'

UNION ALL

SELECT 
    'campos' as tabla,
    CASE 
        WHEN COUNT(*) > 0 THEN 'EXISTE'
        ELSE 'NO EXISTE'
    END as estado
FROM INFORMATION_SCHEMA.TABLES 
WHERE TABLE_SCHEMA = 'agroclouddb' AND TABLE_NAME = 'campos'

UNION ALL

SELECT 
    'lotes' as tabla,
    CASE 
        WHEN COUNT(*) > 0 THEN 'EXISTE'
        ELSE 'NO EXISTE'
    END as estado
FROM INFORMATION_SCHEMA.TABLES 
WHERE TABLE_SCHEMA = 'agroclouddb' AND TABLE_NAME = 'lotes'

UNION ALL

SELECT 
    'insumos' as tabla,
    CASE 
        WHEN COUNT(*) > 0 THEN 'EXISTE'
        ELSE 'NO EXISTE'
    END as estado
FROM INFORMATION_SCHEMA.TABLES 
WHERE TABLE_SCHEMA = 'agroclouddb' AND TABLE_NAME = 'insumos'

UNION ALL

SELECT 
    'maquinaria' as tabla,
    CASE 
        WHEN COUNT(*) > 0 THEN 'EXISTE'
        ELSE 'NO EXISTE'
    END as estado
FROM INFORMATION_SCHEMA.TABLES 
WHERE TABLE_SCHEMA = 'agroclouddb' AND TABLE_NAME = 'maquinaria'

UNION ALL

SELECT 
    'labores' as tabla,
    CASE 
        WHEN COUNT(*) > 0 THEN 'EXISTE'
        ELSE 'NO EXISTE'
    END as estado
FROM INFORMATION_SCHEMA.TABLES 
WHERE TABLE_SCHEMA = 'agroclouddb' AND TABLE_NAME = 'labores'

UNION ALL

SELECT 
    'mantenimiento_maquinaria' as tabla,
    CASE 
        WHEN COUNT(*) > 0 THEN 'EXISTE'
        ELSE 'NO EXISTE'
    END as estado
FROM INFORMATION_SCHEMA.TABLES 
WHERE TABLE_SCHEMA = 'agroclouddb' AND TABLE_NAME = 'mantenimiento_maquinaria';

-- Verificar estructura de la tabla campos si existe
SELECT 'ESTRUCTURA DE LA TABLA CAMPOS (si existe):' as titulo;
SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    IS_NULLABLE,
    COLUMN_DEFAULT,
    COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'agroclouddb' 
AND TABLE_NAME = 'campos'
ORDER BY ORDINAL_POSITION;

-- Verificar datos en tablas si existen
SELECT 'DATOS EN TABLAS (si existen):' as titulo;

-- Verificar roles
SELECT 'ROLES:' as tabla;
SELECT 
    CASE 
        WHEN COUNT(*) > 0 THEN COUNT(*)
        ELSE 'TABLA NO EXISTE O ESTÁ VACÍA'
    END as cantidad 
FROM INFORMATION_SCHEMA.TABLES 
WHERE TABLE_SCHEMA = 'agroclouddb' AND TABLE_NAME = 'roles';

-- Verificar usuarios
SELECT 'USUARIOS:' as tabla;
SELECT 
    CASE 
        WHEN COUNT(*) > 0 THEN COUNT(*)
        ELSE 'TABLA NO EXISTE O ESTÁ VACÍA'
    END as cantidad 
FROM INFORMATION_SCHEMA.TABLES 
WHERE TABLE_SCHEMA = 'agroclouddb' AND TABLE_NAME = 'usuarios';

-- Verificar campos
SELECT 'CAMPOS:' as tabla;
SELECT 
    CASE 
        WHEN COUNT(*) > 0 THEN COUNT(*)
        ELSE 'TABLA NO EXISTE O ESTÁ VACÍA'
    END as cantidad 
FROM INFORMATION_SCHEMA.TABLES 
WHERE TABLE_SCHEMA = 'agroclouddb' AND TABLE_NAME = 'campos';

-- Verificar lotes
SELECT 'LOTES:' as tabla;
SELECT 
    CASE 
        WHEN COUNT(*) > 0 THEN COUNT(*)
        ELSE 'TABLA NO EXISTE O ESTÁ VACÍA'
    END as cantidad 
FROM INFORMATION_SCHEMA.TABLES 
WHERE TABLE_SCHEMA = 'agroclouddb' AND TABLE_NAME = 'lotes';

-- Verificar insumos
SELECT 'INSUMOS:' as tabla;
SELECT 
    CASE 
        WHEN COUNT(*) > 0 THEN COUNT(*)
        ELSE 'TABLA NO EXISTE O ESTÁ VACÍA'
    END as cantidad 
FROM INFORMATION_SCHEMA.TABLES 
WHERE TABLE_SCHEMA = 'agroclouddb' AND TABLE_NAME = 'insumos';

-- Verificar maquinaria
SELECT 'MAQUINARIA:' as tabla;
SELECT 
    CASE 
        WHEN COUNT(*) > 0 THEN COUNT(*)
        ELSE 'TABLA NO EXISTE O ESTÁ VACÍA'
    END as cantidad 
FROM INFORMATION_SCHEMA.TABLES 
WHERE TABLE_SCHEMA = 'agroclouddb' AND TABLE_NAME = 'maquinaria';

-- Verificar labores
SELECT 'LABORES:' as tabla;
SELECT 
    CASE 
        WHEN COUNT(*) > 0 THEN COUNT(*)
        ELSE 'TABLA NO EXISTE O ESTÁ VACÍA'
    END as cantidad 
FROM INFORMATION_SCHEMA.TABLES 
WHERE TABLE_SCHEMA = 'agroclouddb' AND TABLE_NAME = 'labores';

-- Verificar mantenimientos
SELECT 'MANTENIMIENTOS:' as tabla;
SELECT 
    CASE 
        WHEN COUNT(*) > 0 THEN COUNT(*)
        ELSE 'TABLA NO EXISTE O ESTÁ VACÍA'
    END as cantidad 
FROM INFORMATION_SCHEMA.TABLES 
WHERE TABLE_SCHEMA = 'agroclouddb' AND TABLE_NAME = 'mantenimiento_maquinaria';

-- =====================================================
-- RECOMENDACIONES
-- =====================================================

SELECT 'RECOMENDACIONES:' as titulo;
SELECT 'Si las tablas no existen, ejecutar: setup-agroclouddb-safe.sql' as recomendacion;
SELECT 'Si las tablas existen pero no hay datos, ejecutar solo la sección de INSERT' as recomendacion;
SELECT 'Si hay errores, verificar permisos del usuario agrocloudbd' as recomendacion;
