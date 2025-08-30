-- Script para verificar si las tablas ingresos y egresos existen
USE agrocloud;

-- Verificar si la tabla ingresos existe
SELECT 
    CASE 
        WHEN COUNT(*) > 0 THEN '✅ Tabla INGRESOS existe'
        ELSE '❌ Tabla INGRESOS NO existe'
    END as estado_ingresos
FROM information_schema.tables 
WHERE table_schema = 'agrocloud' AND table_name = 'ingresos';

-- Verificar si la tabla egresos existe
SELECT 
    CASE 
        WHEN COUNT(*) > 0 THEN '✅ Tabla EGRESOS existe'
        ELSE '❌ Tabla EGRESOS NO existe'
    END as estado_egresos
FROM information_schema.tables 
WHERE table_schema = 'agrocloud' AND table_name = 'egresos';

-- Mostrar todas las tablas en la base de datos
SELECT table_name as tablas_existentes
FROM information_schema.tables 
WHERE table_schema = 'agrocloud'
ORDER BY table_name;
