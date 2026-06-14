-- Script simplificado para ejecutar manualmente la migración V1_104
-- Ejecutar con: mysql -u root -p123456 agrocloud < ejecutar_migracion_V1_104.sql

USE agrocloud;

-- Verificar y agregar columnas faltantes
ALTER TABLE porcinos_parametros_productivos_porcinos
ADD COLUMN IF NOT EXISTS dias_tolerancia_vencimiento_gestacion INT NULL COMMENT 'Días de tolerancia para el vencimiento de gestación',
ADD COLUMN IF NOT EXISTS dias_control_celo INT NULL COMMENT 'Días de control de celo',
ADD COLUMN IF NOT EXISTS dias_entre_celos INT NULL COMMENT 'Días entre celos',
ADD COLUMN IF NOT EXISTS dias_pasaje_maternidad INT NULL COMMENT 'Días antes del parto para pasaje a maternidad',
ADD COLUMN IF NOT EXISTS peso_promedio_nacimiento DECIMAL(10,2) NULL COMMENT 'Peso promedio al nacer (kg)',
ADD COLUMN IF NOT EXISTS peso_destete_objetivo DECIMAL(10,2) NULL COMMENT 'Peso objetivo al destete (kg)',
ADD COLUMN IF NOT EXISTS peso_venta_objetivo DECIMAL(10,2) NULL COMMENT 'Peso objetivo para venta (kg)',
ADD COLUMN IF NOT EXISTS lechones_vivos_parto_objetivo DECIMAL(5,2) NULL COMMENT 'Lechones vivos por parto objetivo',
ADD COLUMN IF NOT EXISTS lechones_destetados_objetivo DECIMAL(5,2) NULL COMMENT 'Lechones destetados objetivo',
ADD COLUMN IF NOT EXISTS partos_madre_anio_objetivo DECIMAL(5,2) NULL COMMENT 'Partos por madre por año objetivo';















