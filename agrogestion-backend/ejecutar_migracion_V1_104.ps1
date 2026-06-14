# Script PowerShell para ejecutar la migración V1_104
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

Write-Host "Ejecutando migración V1_104..." -ForegroundColor Green

foreach ($column in $columns) {
    $sql = "ALTER TABLE $table ADD COLUMN $($column.Name) $($column.Type) NULL COMMENT '$($column.Comment)';"
    Write-Host "Agregando columna: $($column.Name)..." -ForegroundColor Yellow
    
    $result = & $mysqlPath -u $user -p$password $database -e $sql 2>&1
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "  ✓ Columna $($column.Name) agregada correctamente" -ForegroundColor Green
    } else {
        if ($result -match "Duplicate column name") {
            Write-Host "  → Columna $($column.Name) ya existe, omitiendo..." -ForegroundColor Cyan
        } else {
            Write-Host "  ✗ Error al agregar columna $($column.Name): $result" -ForegroundColor Red
        }
    }
}

Write-Host "`nMigración completada!" -ForegroundColor Green















