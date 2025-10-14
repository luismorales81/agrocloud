-- Script para corregir la estructura de la tabla ingresos
-- Ejecutar este script en la base de datos MySQL

USE agrocloud;

-- Agregar las columnas faltantes a la tabla ingresos
ALTER TABLE ingresos 
ADD COLUMN cantidad DECIMAL(10,2) NULL AFTER monto,
ADD COLUMN unidad_medida VARCHAR(50) NULL AFTER cantidad,
ADD COLUMN cliente_comprador VARCHAR(200) NULL AFTER unidad_medida,
ADD COLUMN observaciones TEXT NULL AFTER cliente_comprador,
ADD COLUMN estado VARCHAR(50) NOT NULL DEFAULT 'REGISTRADO' AFTER observaciones,
ADD COLUMN tipo_ingreso VARCHAR(50) NOT NULL DEFAULT 'OTROS_INGRESOS' AFTER estado;

-- Renombrar la columna 'tipo' a 'concepto' si es necesario
-- ALTER TABLE ingresos CHANGE COLUMN tipo concepto VARCHAR(200) NOT NULL;

-- Actualizar los registros existentes para que tengan valores por defecto
UPDATE ingresos SET 
    estado = 'REGISTRADO',
    tipo_ingreso = 'OTROS_INGRESOS'
WHERE estado IS NULL OR tipo_ingreso IS NULL;

-- Crear índices para las nuevas columnas
CREATE INDEX idx_ingresos_estado ON ingresos(estado);
CREATE INDEX idx_ingresos_tipo_ingreso ON ingresos(tipo_ingreso);

-- Verificar la estructura final
DESCRIBE ingresos;
