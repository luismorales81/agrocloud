USE agrocloud;

ALTER TABLE porcinos_parametros_productivos_porcinos ADD COLUMN dias_tolerancia_vencimiento_gestacion INT NULL COMMENT 'Días de tolerancia para el vencimiento de gestación';
ALTER TABLE porcinos_parametros_productivos_porcinos ADD COLUMN dias_control_celo INT NULL COMMENT 'Días de control de celo';
ALTER TABLE porcinos_parametros_productivos_porcinos ADD COLUMN dias_entre_celos INT NULL COMMENT 'Días entre celos';
ALTER TABLE porcinos_parametros_productivos_porcinos ADD COLUMN dias_pasaje_maternidad INT NULL COMMENT 'Días antes del parto para pasaje a maternidad';
ALTER TABLE porcinos_parametros_productivos_porcinos ADD COLUMN peso_promedio_nacimiento DECIMAL(10,2) NULL COMMENT 'Peso promedio al nacer (kg)';
ALTER TABLE porcinos_parametros_productivos_porcinos ADD COLUMN peso_destete_objetivo DECIMAL(10,2) NULL COMMENT 'Peso objetivo al destete (kg)';
ALTER TABLE porcinos_parametros_productivos_porcinos ADD COLUMN peso_venta_objetivo DECIMAL(10,2) NULL COMMENT 'Peso objetivo para venta (kg)';
ALTER TABLE porcinos_parametros_productivos_porcinos ADD COLUMN lechones_vivos_parto_objetivo DECIMAL(5,2) NULL COMMENT 'Lechones vivos por parto objetivo';
ALTER TABLE porcinos_parametros_productivos_porcinos ADD COLUMN lechones_destetados_objetivo DECIMAL(5,2) NULL COMMENT 'Lechones destetados objetivo';
ALTER TABLE porcinos_parametros_productivos_porcinos ADD COLUMN partos_madre_anio_objetivo DECIMAL(5,2) NULL COMMENT 'Partos por madre por año objetivo';















