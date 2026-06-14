# Script robusto para ejecutar la migración V1_104
$ErrorActionPreference = "Continue"
$mysqlPath = "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
$user = "root"
$password = "123456"
$database = "agrocloud"
$table = "porcinos_parametros_productivos_porcinos"

$columns = @(
    @{Name="dias_tolerancia_vencimiento_gestacion"; Type="INT"; Comment="Días de tolerancia para el vencimiento de gestación"},
    @{Name="dias_control_celo"; Type="INT"; Comment="Días de control de celo"},
    @{Name="dias_entre_celos"; Type="INT"; Comment="Días entre celos"},
    @{Name="dias_pasaje_maternidad"; Type="INT"; Comment="Días antes del parto para pasaje a maternidad"},
    @{Name="peso_promedio_nacimiento"; Type="DECIMAL(10,2)"; Comment="Peso promedio al nacer (kg)"},
    @{Name="peso_destete_objetivo"; Type="DECIMAL(10,2)"; Comment="Peso objetivo al destete (kg)"},
    @{Name="peso_venta_objetivo"; Type="DECIMAL(10,2)"; Comment="Peso objetivo para venta (kg)"},
    @{Name="lechones_vivos_parto_objetivo"; Type="DECIMAL(5,2)"; Comment="Lechones vivos por parto objetivo"},
    @{Name="lechones_destetados_objetivo"; Type="DECIMAL(5,2)"; Comment="Lechones destetados objetivo"},
    @{Name="partos_madre_anio_objetivo"; Type="DECIMAL(5,2)"; Comment="Partos por madre por año objetivo"}
)

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Ejecutando migración V1_104" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

foreach ($column in $columns) {
    $sql = "ALTER TABLE $table ADD COLUMN $($column.Name) $($column.Type) NULL COMMENT '$($column.Comment)';"
    Write-Host "Procesando: $($column.Name)..." -NoNewline
    
    try {
        $output = & $mysqlPath -u $user -p$password $database -e $sql 2>&1
        $exitCode = $LASTEXITCODE
        
        if ($exitCode -eq 0) {
            Write-Host " [OK]" -ForegroundColor Green
        } else {
            $outputStr = $output | Out-String
            if ($outputStr -match "Duplicate column name") {
                Write-Host " [YA EXISTE]" -ForegroundColor Yellow
            } else {
                Write-Host " [ERROR]" -ForegroundColor Red
                Write-Host "   $outputStr" -ForegroundColor Red
            }
        }
    } catch {
        Write-Host " [EXCEPCIÓN]" -ForegroundColor Red
        Write-Host "   $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Verificando columnas..." -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

$checkSql = @"
SELECT COLUMN_NAME 
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = '$database' 
  AND TABLE_NAME = '$table'
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
"@

$result = & $mysqlPath -u $user -p$password $database -e $checkSql 2>&1
Write-Host $result

Write-Host ""
Write-Host "Migración completada!" -ForegroundColor Green















