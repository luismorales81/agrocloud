#!/bin/bash

# Script de verificación de rutas API para Linux/Mac
# Detecta uso incorrecto de '/api/' literal en el código

set -e

# Colores para consola
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
WHITE='\033[1;37m'
NC='\033[0m' # No Color

# Función para imprimir con color
print_color() {
    local color=$1
    local message=$2
    echo -e "${color}${message}${NC}"
}

print_success() {
    print_color $GREEN "✅ $1"
}

print_error() {
    print_color $RED "❌ $1"
}

print_warning() {
    print_color $YELLOW "⚠️  $1"
}

print_info() {
    print_color $CYAN "ℹ️  $1"
}

# Configuración
SRC_DIR=${1:-"src"}
TOTAL_ISSUES=0

# Patrones a detectar
PATTERNS=(
    "api\.get('/api/"
    "api\.post('/api/"
    "api\.put('/api/"
    "api\.delete('/api/"
    "api\.patch('/api/"
    "fetch('/api/"
    "axios\.get('/api/"
    "axios\.post('/api/"
    "axios\.put('/api/"
    "axios\.delete('/api/"
    "axios\.patch('/api/"
)

# Archivos permitidos
ALLOWED_FILES=(
    "src/services/api.ts"
    "src/services/api.js"
)

print_color $CYAN "🔍 Verificando rutas API en el proyecto..."
print_color $CYAN "════════════════════════════════════════════════════════"

# Verificar que existe el directorio
if [ ! -d "$SRC_DIR" ]; then
    print_error "Directorio $SRC_DIR no encontrado"
    exit 1
fi

# Obtener archivos fuente
SOURCE_FILES=$(find "$SRC_DIR" -name "*.ts" -o -name "*.tsx" -o -name "*.js" -o -name "*.jsx" | grep -v node_modules | grep -v .git | grep -v dist | grep -v build)

print_info "Analizando $(echo "$SOURCE_FILES" | wc -l) archivos fuente..."

# Verificar cada archivo
while IFS= read -r file; do
    if [ -z "$file" ]; then
        continue
    fi
    
    relative_path=$(echo "$file" | sed "s|^$(pwd)/||")
    
    # Saltar archivos permitidos
    skip_file=false
    for allowed in "${ALLOWED_FILES[@]}"; do
        if [[ "$relative_path" == *"$allowed"* ]]; then
            print_warning "⏭️  Saltando archivo permitido: $relative_path"
            skip_file=true
            break
        fi
    done
    
    if [ "$skip_file" = true ]; then
        continue
    fi
    
    file_issues=0
    
    # Verificar cada patrón
    for pattern in "${PATTERNS[@]}"; do
        if grep -n "$pattern" "$file" > /dev/null 2>&1; then
            if [ $file_issues -eq 0 ]; then
                print_error "📁 $relative_path:"
            fi
            
            grep -n "$pattern" "$file" | while IFS=: read -r line_num line_content; do
                print_color $RED "   Línea $line_num: $(echo "$line_content" | sed 's/^[[:space:]]*//')"
                print_color $YELLOW "   Patrón detectado: $pattern"
            done
            
            file_issues=$((file_issues + 1))
        fi
    done
    
    if [ $file_issues -gt 0 ]; then
        TOTAL_ISSUES=$((TOTAL_ISSUES + file_issues))
    fi
    
done <<< "$SOURCE_FILES"

print_color $CYAN "════════════════════════════════════════════════════════"

if [ $TOTAL_ISSUES -eq 0 ]; then
    print_success "¡Excelente! No se encontraron rutas API problemáticas."
    print_success "Todas las rutas siguen la convención correcta."
    exit 0
fi

# Mostrar recomendaciones
print_error "Se encontraron $TOTAL_ISSUES problemas en el código."
echo ""

print_color $CYAN "🔧 Recomendaciones:"
print_color $WHITE "1. Reemplaza '/api/...' por '/v1/...' o rutas sin prefijo"
print_color $WHITE "2. Usa solo rutas como: '/v1/lotes', '/campos', '/insumos'"
print_color $WHITE "3. El prefijo '/api' se agrega automáticamente en api.ts"
print_color $WHITE "4. Ejemplo correcto: api.get('/v1/lotes')"
print_color $RED "5. Ejemplo incorrecto: api.get('/api/v1/lotes')"

echo ""
print_color $CYAN "📚 Documentación:"
print_color $WHITE "Ver README.md sección 'Convención de Rutas API' para más detalles."

exit 1
