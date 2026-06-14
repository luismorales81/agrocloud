# Script para actualizar versiones en el proyecto AgroGestion
# Uso: .\scripts\bump-version.ps1 -Version "1.2.0" -Module "agroquimicos"

param(
    [Parameter(Mandatory=$true)]
    [string]$Version,
    
    [Parameter(Mandatory=$false)]
    [string]$Module = "",
    
    [Parameter(Mandatory=$false)]
    [ValidateSet("major", "minor", "patch")]
    [string]$BumpType = "minor"
)

$ErrorActionPreference = "Stop"

Write-Host "🚀 Actualizando versiones del proyecto AgroGestion" -ForegroundColor Cyan
Write-Host ""

# Rutas de archivos
$BackendPom = "agrogestion-backend\pom.xml"
$FrontendPackage = "agrogestion-frontend\package.json"
$Changelog = "CHANGELOG.md"
$MatrizVersiones = "MATRIZ-VERSIONES.md"

# Función para actualizar versión en pom.xml
function Update-BackendVersion {
    param([string]$NewVersion)
    
    if (Test-Path $BackendPom) {
        Write-Host "📝 Actualizando versión del backend..." -ForegroundColor Yellow
        $content = Get-Content $BackendPom -Raw
        $content = $content -replace '(<version>)(\d+\.\d+\.\d+)(</version>)', "`$1$NewVersion`$3"
        Set-Content $BackendPom -Value $content -NoNewline
        Write-Host "✅ Backend actualizado a versión $NewVersion" -ForegroundColor Green
    } else {
        Write-Host "⚠️  No se encontró $BackendPom" -ForegroundColor Yellow
    }
}

# Función para actualizar versión en package.json
function Update-FrontendVersion {
    param([string]$NewVersion)
    
    if (Test-Path $FrontendPackage) {
        Write-Host "📝 Actualizando versión del frontend..." -ForegroundColor Yellow
        $content = Get-Content $FrontendPackage -Raw | ConvertFrom-Json
        $content.version = $NewVersion
        $content | ConvertTo-Json -Depth 10 | Set-Content $FrontendPackage
        Write-Host "✅ Frontend actualizado a versión $NewVersion" -ForegroundColor Green
    } else {
        Write-Host "⚠️  No se encontró $FrontendPackage" -ForegroundColor Yellow
    }
}

# Función para actualizar HealthController
function Update-HealthControllerVersion {
    param([string]$NewVersion)
    
    $healthController = "agrogestion-backend\src\main\java\com\agrocloud\controller\HealthController.java"
    
    if (Test-Path $healthController) {
        Write-Host "📝 Actualizando versión en HealthController..." -ForegroundColor Yellow
        $content = Get-Content $healthController -Raw
        $content = $content -replace '(BACKEND_VERSION = ")(\d+\.\d+\.\d+)(")', "`$1$NewVersion`$3"
        Set-Content $healthController -Value $content -NoNewline
        Write-Host "✅ HealthController actualizado" -ForegroundColor Green
    }
}

# Función para leer versión actual
function Get-CurrentVersion {
    param([string]$File, [string]$Type)
    
    if ($Type -eq "pom") {
        if (Test-Path $File) {
            $content = Get-Content $File -Raw
            if ($content -match '<version>(\d+\.\d+\.\d+)</version>') {
                return $matches[1]
            }
        }
    } elseif ($Type -eq "json") {
        if (Test-Path $File) {
            $content = Get-Content $File -Raw | ConvertFrom-Json
            return $content.version
        }
    }
    return "0.0.0"
}

# Obtener versiones actuales
$currentBackendVersion = Get-CurrentVersion -File $BackendPom -Type "pom"
$currentFrontendVersion = Get-CurrentVersion -File $FrontendPackage -Type "json"

Write-Host "📊 Versiones actuales:" -ForegroundColor Cyan
Write-Host "   Backend:  $currentBackendVersion" -ForegroundColor Gray
Write-Host "   Frontend: $currentFrontendVersion" -ForegroundColor Gray
Write-Host ""

# Si no se especifica versión, calcular bump automático
if (-not $Version) {
    $parts = $currentBackendVersion -split '\.'
    $major = [int]$parts[0]
    $minor = [int]$parts[1]
    $patch = [int]$parts[2]
    
    switch ($BumpType) {
        "major" { $major++; $minor = 0; $patch = 0 }
        "minor" { $minor++; $patch = 0 }
        "patch" { $patch++ }
    }
    
    $Version = "$major.$minor.$patch"
}

Write-Host "🎯 Nueva versión: $Version" -ForegroundColor Cyan
if ($Module) {
    Write-Host "📦 Módulo: $Module" -ForegroundColor Cyan
}
Write-Host ""

# Confirmar acción
$confirm = Read-Host "¿Continuar con la actualización? (S/N)"
if ($confirm -ne "S" -and $confirm -ne "s") {
    Write-Host "❌ Operación cancelada" -ForegroundColor Red
    exit
}

# Actualizar versiones
Update-BackendVersion -NewVersion $Version
Update-FrontendVersion -NewVersion $Version
Update-HealthControllerVersion -NewVersion $Version

Write-Host ""
Write-Host "✅ Versiones actualizadas exitosamente!" -ForegroundColor Green
Write-Host ""
Write-Host "📋 Próximos pasos:" -ForegroundColor Cyan
Write-Host "   1. Actualizar CHANGELOG.md con los cambios" -ForegroundColor Gray
Write-Host "   2. Actualizar MATRIZ-VERSIONES.md si corresponde" -ForegroundColor Gray
Write-Host "   3. Commit de los cambios:" -ForegroundColor Gray
Write-Host "      git add ." -ForegroundColor DarkGray
Write-Host "      git commit -m `"chore: Bump version to $Version`"" -ForegroundColor DarkGray
if ($Module) {
    Write-Host "      git commit -m `"chore(modulo-$Module): Bump version to $Version`"" -ForegroundColor DarkGray
}
Write-Host "   4. Crear tag: git tag -a v$Version -m `"Release v$Version`"" -ForegroundColor DarkGray

