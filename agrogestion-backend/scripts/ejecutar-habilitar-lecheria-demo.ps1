# Habilita modulo LECHERIA + datos demo para AgroCloud Demo
# Ajustar $Mysql, $Db, $User, $Pass segun entorno local.

$Mysql = "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
$Db = "agrocloud"
$User = "root"
$Pass = "123456"
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path

Write-Host "Ejecutando HABILITAR_LECHERIA_EMPRESA_DEMO.sql ..."
& $Mysql -u $User "-p$Pass" $Db -e "source $($ScriptDir)\HABILITAR_LECHERIA_EMPRESA_DEMO.sql"
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

Write-Host "Ejecutando DEMO_INSERTAR_LECHERIA.sql ..."
Get-Content "$ScriptDir\DEMO_INSERTAR_LECHERIA.sql" | & $Mysql -u $User "-p$Pass" $Db
if ($LASTEXITCODE -eq 0) {
    Write-Host "OK: Modulo LECHERIA con datos demo cargados."
} else {
    Write-Host "Error al cargar datos demo. Codigo: $LASTEXITCODE"
    exit $LASTEXITCODE
}
