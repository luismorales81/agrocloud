# Script de verificación de rutas API para PowerShell
# Detecta uso incorrecto de '/api/' literal en el código

param(
    [switch]$Fix = $false,
    [string]$Path = "src"
)

# Colores para consola
function Write-ColorOutput {
    param(
        [string]$Message,
        [string]$Color = "White"
    )
    Write-Host $Message -ForegroundColor $Color
}

function Write-Success {
    param([string]$Message)
    Write-ColorOutput "✅ $Message" "Green"
}

function Write-Error {
    param([string]$Message)
    Write-ColorOutput "❌ $Message" "Red"
}

function Write-Warning {
    param([string]$Message)
    Write-ColorOutput "⚠️  $Message" "Yellow"
}

function Write-Info {
    param([string]$Message)
    Write-ColorOutput "ℹ️  $Message" "Cyan"
}

# Patrones a detectar
$ForbiddenPatterns = @(
    "api\.get\('/api/",
    "api\.post\('/api/",
    "api\.put\('/api/",
    "api\.delete\('/api/",
    "api\.patch\('/api/",
    "fetch\('/api/",
    "axios\.get\('/api/",
    "axios\.post\('/api/",
    "axios\.put\('/api/",
    "axios\.delete\('/api/",
    "axios\.patch\('/api/"
)

# Archivos permitidos (pueden contener '/api' para configuración)
$AllowedFiles = @(
    "src/services/api.ts",
    "src/services/api.js"
)

Write-ColorOutput "🔍 Verificando rutas API en el proyecto..." "Cyan"
Write-ColorOutput "════════════════════════════════════════════════════════" "Cyan"

# Verificar que existe el directorio
if (-not (Test-Path $Path)) {
    Write-Error "Directorio $Path no encontrado"
    exit 1
}

# Obtener todos los archivos TypeScript/JavaScript
$SourceFiles = Get-ChildItem -Path $Path -Recurse -Include "*.ts", "*.tsx", "*.js", "*.jsx" | 
    Where-Object { $_.FullName -notmatch "node_modules|\.git|dist|build" }

Write-Info "Analizando $($SourceFiles.Count) archivos fuente..."

$TotalIssues = 0
$FilesWithIssues = @()

foreach ($File in $SourceFiles) {
    $RelativePath = $File.FullName.Replace((Get-Location).Path, "").TrimStart("\")
    
    # Saltar archivos permitidos
    if ($AllowedFiles -contains $RelativePath) {
        Write-Warning "⏭️  Saltando archivo permitido: $RelativePath"
        continue
    }
    
    $Content = Get-Content $File.FullName -Raw
    $Lines = $Content -split "`n"
    $Issues = @()
    
    for ($i = 0; $i -lt $Lines.Count; $i++) {
        $Line = $Lines[$i]
        foreach ($Pattern in $ForbiddenPatterns) {
            if ($Line -match $Pattern) {
                $Issues += @{
                    Line = $i + 1
                    Content = $Line.Trim()
                    Pattern = $Pattern
                }
            }
        }
    }
    
    if ($Issues.Count -gt 0) {
        $FilesWithIssues += @{
            File = $RelativePath
            Issues = $Issues
        }
        $TotalIssues += $Issues.Count
    }
}

Write-ColorOutput "════════════════════════════════════════════════════════" "Cyan"

if ($TotalIssues -eq 0) {
    Write-Success "¡Excelente! No se encontraron rutas API problemáticas."
    Write-Success "Todas las rutas siguen la convención correcta."
    exit 0
}

# Mostrar archivos con problemas
Write-Error "Se encontraron $TotalIssues problemas en $($FilesWithIssues.Count) archivos:"
Write-Host ""

foreach ($FileIssue in $FilesWithIssues) {
    Write-Error "📁 $($FileIssue.File):"
    foreach ($Issue in $FileIssue.Issues) {
        Write-ColorOutput "   Línea $($Issue.Line): $($Issue.Content)" "Red"
        Write-ColorOutput "   Patrón detectado: $($Issue.Pattern)" "Yellow"
    }
    Write-Host ""
}

# Mostrar recomendaciones
Write-ColorOutput "🔧 Recomendaciones:" "Cyan"
Write-ColorOutput "1. Reemplaza '/api/...' por '/v1/...' o rutas sin prefijo" "White"
Write-ColorOutput "2. Usa solo rutas como: '/v1/lotes', '/campos', '/insumos'" "White"
Write-ColorOutput "3. El prefijo '/api' se agrega automáticamente en api.ts" "White"
Write-ColorOutput "4. Ejemplo correcto: api.get('/v1/lotes')" "White"
Write-ColorOutput "5. Ejemplo incorrecto: api.get('/api/v1/lotes')" "Red"

Write-Host ""
Write-ColorOutput "📚 Documentación:" "Cyan"
Write-ColorOutput "Ver README.md sección 'Convención de Rutas API' para más detalles." "White"

exit 1
