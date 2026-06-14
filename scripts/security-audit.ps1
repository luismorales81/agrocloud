# Script para ejecutar análisis de seguridad con herramientas OWASP
# Uso: .\scripts\security-audit.ps1

param(
    [Parameter(Mandatory=$false)]
    [ValidateSet("noir", "dependency-check", "zap", "all")]
    [string]$Tool = "all",
    
    [Parameter(Mandatory=$false)]
    [string]$OutputDir = "security-reports"
)

$ErrorActionPreference = "Stop"

Write-Host "🔒 Análisis de Seguridad OWASP - AgroGestion" -ForegroundColor Cyan
Write-Host ""

# Crear directorio de reportes
if (-not (Test-Path $OutputDir)) {
    New-Item -ItemType Directory -Path $OutputDir | Out-Null
    Write-Host "✅ Directorio creado: $OutputDir" -ForegroundColor Green
}

# Función para verificar Docker
function Test-Docker {
    try {
        docker --version | Out-Null
        return $true
    } catch {
        return $false
    }
}

# Función para ejecutar OWASP Noir
function Invoke-Noir {
    Write-Host "🔍 Ejecutando OWASP Noir..." -ForegroundColor Yellow
    
    if (Test-Docker) {
        Write-Host "   Usando Docker..." -ForegroundColor Gray
        docker run --rm -v "${PWD}:/workspace" ghcr.io/owasp-noir/noir:main --format json > "$OutputDir/noir-report.json" 2>&1
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host "✅ Reporte Noir generado: $OutputDir/noir-report.json" -ForegroundColor Green
        } else {
            Write-Host "⚠️  Error ejecutando Noir. Verifica que Docker esté funcionando." -ForegroundColor Yellow
        }
    } else {
        # Intentar ejecutar noir directamente
        try {
            noir --format json > "$OutputDir/noir-report.json" 2>&1
            Write-Host "✅ Reporte Noir generado: $OutputDir/noir-report.json" -ForegroundColor Green
        } catch {
            Write-Host "❌ Noir no encontrado. Instala con: docker pull ghcr.io/owasp-noir/noir:main" -ForegroundColor Red
        }
    }
}

# Función para ejecutar Dependency-Check (Backend)
function Invoke-DependencyCheck {
    Write-Host "🔍 Ejecutando OWASP Dependency-Check (Backend)..." -ForegroundColor Yellow
    
    $backendDir = "agrogestion-backend"
    
    if (-not (Test-Path $backendDir)) {
        Write-Host "⚠️  Directorio backend no encontrado" -ForegroundColor Yellow
        return
    }
    
    Push-Location $backendDir
    
    try {
        # Verificar si el plugin está configurado
        $pomContent = Get-Content pom.xml -Raw
        
        if ($pomContent -match "dependency-check-maven") {
            Write-Host "   Ejecutando Maven Dependency-Check..." -ForegroundColor Gray
            mvn dependency-check:check
            
            if ($LASTEXITCODE -eq 0) {
                Write-Host "✅ Dependency-Check completado" -ForegroundColor Green
                Write-Host "   Revisa: target/dependency-check-report.html" -ForegroundColor Gray
            }
        } else {
            Write-Host "⚠️  Plugin dependency-check-maven no configurado en pom.xml" -ForegroundColor Yellow
            Write-Host "   Agrega el plugin al pom.xml para habilitar" -ForegroundColor Gray
        }
    } catch {
        Write-Host "❌ Error ejecutando Dependency-Check: $_" -ForegroundColor Red
    } finally {
        Pop-Location
    }
}

# Función para ejecutar npm audit (Frontend)
function Invoke-NpmAudit {
    Write-Host "🔍 Ejecutando npm audit (Frontend)..." -ForegroundColor Yellow
    
    $frontendDir = "agrogestion-frontend"
    
    if (-not (Test-Path $frontendDir)) {
        Write-Host "⚠️  Directorio frontend no encontrado" -ForegroundColor Yellow
        return
    }
    
    Push-Location $frontendDir
    
    try {
        Write-Host "   Ejecutando npm audit..." -ForegroundColor Gray
        npm audit --json > "../$OutputDir/npm-audit-report.json" 2>&1
        npm audit > "../$OutputDir/npm-audit-report.txt" 2>&1
        
        Write-Host "✅ npm audit completado" -ForegroundColor Green
        Write-Host "   Reportes generados en: $OutputDir/" -ForegroundColor Gray
    } catch {
        Write-Host "❌ Error ejecutando npm audit: $_" -ForegroundColor Red
    } finally {
        Pop-Location
    }
}

# Función para ejecutar OWASP ZAP
function Invoke-Zap {
    param([string]$TargetUrl = "http://localhost:8080")
    
    Write-Host "🔍 Ejecutando OWASP ZAP..." -ForegroundColor Yellow
    Write-Host "   Target: $TargetUrl" -ForegroundColor Gray
    
    if (-not (Test-Docker)) {
        Write-Host "❌ Docker no está disponible. ZAP requiere Docker." -ForegroundColor Red
        return
    }
    
    Write-Host "   ⚠️  Asegúrate de que la aplicación esté corriendo en $TargetUrl" -ForegroundColor Yellow
    $confirm = Read-Host "   ¿Continuar? (S/N)"
    
    if ($confirm -ne "S" -and $confirm -ne "s") {
        Write-Host "   Operación cancelada" -ForegroundColor Yellow
        return
    }
    
    try {
        Write-Host "   Ejecutando baseline scan..." -ForegroundColor Gray
        docker run --rm -v "${PWD}/$OutputDir:/zap/wrk" owasp/zap2docker-stable zap-baseline.py `
            -t $TargetUrl `
            -J /zap/wrk/zap-report.json `
            -r /zap/wrk/zap-report.html
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host "✅ ZAP scan completado" -ForegroundColor Green
            Write-Host "   Reportes generados en: $OutputDir/" -ForegroundColor Gray
        }
    } catch {
        Write-Host "❌ Error ejecutando ZAP: $_" -ForegroundColor Red
    }
}

# Ejecutar herramientas según parámetro
switch ($Tool) {
    "noir" {
        Invoke-Noir
    }
    "dependency-check" {
        Invoke-DependencyCheck
        Invoke-NpmAudit
    }
    "zap" {
        $url = Read-Host "URL del servidor a escanear (default: http://localhost:8080)"
        if ([string]::IsNullOrWhiteSpace($url)) {
            $url = "http://localhost:8080"
        }
        Invoke-Zap -TargetUrl $url
    }
    "all" {
        Write-Host "Ejecutando todas las herramientas..." -ForegroundColor Cyan
        Write-Host ""
        
        Invoke-Noir
        Write-Host ""
        
        Invoke-DependencyCheck
        Write-Host ""
        
        Invoke-NpmAudit
        Write-Host ""
        
        Write-Host "📊 Resumen de reportes generados:" -ForegroundColor Cyan
        Write-Host "   - Noir: $OutputDir/noir-report.json" -ForegroundColor Gray
        Write-Host "   - npm audit: $OutputDir/npm-audit-report.json" -ForegroundColor Gray
        Write-Host ""
        Write-Host "💡 Para ejecutar ZAP, usa: .\scripts\security-audit.ps1 -Tool zap" -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "✅ Análisis de seguridad completado" -ForegroundColor Green
Write-Host "📁 Reportes guardados en: $OutputDir/" -ForegroundColor Cyan

