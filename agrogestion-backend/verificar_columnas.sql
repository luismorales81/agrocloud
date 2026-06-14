-- Script para verificar si las columnas fueron agregadas
USE agrocloud;

SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    IS_NULLABLE,
    COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'agrocloud' 
  AND TABLE_NAME = 'porcinos_parametros_productivos_porcinos'
  AND COLUMN_NAME IN (
    'dias_tolerancia_vencimiento_gestacion',
    'dias_control_celo',
    'dias_entre_celos',
    'dias_pasaje_maternidad',
    'peso_promedio_nacimiento',
    'peso_destete_objetivo',
    'peso_venta_objetivo',
    'lechones_vivos_parto_objetivo',
    'lechones_destetados_objetivo',
    'partos_madre_anio_objetivo'
  )
ORDER BY COLUMN_NAME;















