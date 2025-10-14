-- ========================================
-- Script para corregir tabla INGRESOS
-- ========================================
-- Problema: La tabla tiene columna 'tipo' pero el código Java espera 'tipo_ingreso'
-- 
-- INSTRUCCIONES:
-- 1. Abrí phpMyAdmin (http://localhost/phpmyadmin)
-- 2. Seleccioná la base de datos 'agrocloud'
-- 3. Hacé clic en la pestaña 'SQL'
-- 4. Copiá y pegá TODO este script
-- 5. Hacé clic en 'Continuar'
-- ========================================

USE agrocloud;

-- Paso 1: Verificar estructura actual
SELECT 'Estructura actual de la tabla ingresos:' AS info;
SHOW COLUMNS FROM ingresos;

-- Paso 2: Renombrar columna 'tipo' a 'tipo_ingreso' si existe
ALTER TABLE ingresos 
CHANGE COLUMN tipo tipo_ingreso 
ENUM('VENTA_CULTIVO', 'VENTA_ANIMAL', 'SERVICIOS_AGRICOLAS', 'SUBSIDIOS', 'OTROS_INGRESOS') NOT NULL;

-- Paso 3: Verificar que todos los campos necesarios existan
-- Agregar columna 'activo' si no existe
SET @col_exists = (SELECT COUNT(*) 
                   FROM INFORMATION_SCHEMA.COLUMNS 
                   WHERE TABLE_SCHEMA = 'agrocloud' 
                   AND TABLE_NAME = 'ingresos' 
                   AND COLUMN_NAME = 'activo');

-- Solo agregar si no existe
SET @query = IF(@col_exists = 0,
    'ALTER TABLE ingresos ADD COLUMN activo BOOLEAN NOT NULL DEFAULT TRUE',
    'SELECT "La columna activo ya existe" AS mensaje');
    
PREPARE stmt FROM @query;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Paso 4: Verificar estructura final
SELECT 'Estructura corregida de la tabla ingresos:' AS info;
SHOW COLUMNS FROM ingresos;

SELECT '✅ Tabla ingresos corregida exitosamente' AS resultado;

