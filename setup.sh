#!/bin/bash

# AgroGestion - Script de Instalación Rápida
# Este script configura automáticamente el entorno de desarrollo

echo "🌾 AgroGestion - Configuración Automática"
echo "=========================================="

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Función para imprimir mensajes
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Verificar sistema operativo
detect_os() {
    if [[ "$OSTYPE" == "linux-gnu"* ]]; then
        OS="linux"
    elif [[ "$OSTYPE" == "darwin"* ]]; then
        OS="macos"
    elif [[ "$OSTYPE" == "msys" ]] || [[ "$OSTYPE" == "cygwin" ]]; then
        OS="windows"
    else
        OS="unknown"
    fi
    print_status "Sistema operativo detectado: $OS"
}

# Verificar prerrequisitos
check_prerequisites() {
    print_status "Verificando prerrequisitos..."
    
    # Verificar Java
    if command -v java &> /dev/null; then
        JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2)
        print_success "Java encontrado: $JAVA_VERSION"
    else
        print_error "Java no está instalado. Por favor instala Java 17+"
        exit 1
    fi
    
    # Verificar Maven
    if command -v mvn &> /dev/null; then
        MAVEN_VERSION=$(mvn -version 2>&1 | head -n 1 | cut -d' ' -f3)
        print_success "Maven encontrado: $MAVEN_VERSION"
    else
        print_error "Maven no está instalado. Por favor instala Maven 3.6+"
        exit 1
    fi
    
    # Verificar Node.js
    if command -v node &> /dev/null; then
        NODE_VERSION=$(node --version)
        print_success "Node.js encontrado: $NODE_VERSION"
    else
        print_error "Node.js no está instalado. Por favor instala Node.js 16+"
        exit 1
    fi
    
    # Verificar npm
    if command -v npm &> /dev/null; then
        NPM_VERSION=$(npm --version)
        print_success "npm encontrado: $NPM_VERSION"
    else
        print_error "npm no está instalado"
        exit 1
    fi
    
    # Verificar MySQL
    if command -v mysql &> /dev/null; then
        MYSQL_VERSION=$(mysql --version | cut -d' ' -f6 | cut -d',' -f1)
        print_success "MySQL encontrado: $MYSQL_VERSION"
    else
        print_warning "MySQL no está instalado. Por favor instala MySQL 8.0+"
    fi
}

# Configurar base de datos
setup_database() {
    print_status "Configurando base de datos..."
    
    if ! command -v mysql &> /dev/null; then
        print_warning "MySQL no está disponible. Saltando configuración de BD."
        return
    fi
    
    # Verificar si MySQL está ejecutándose
    if ! pgrep -x "mysqld" > /dev/null; then
        print_warning "MySQL no está ejecutándose. Iniciando MySQL..."
        
        case $OS in
            "linux")
                sudo systemctl start mysql 2>/dev/null || sudo service mysql start 2>/dev/null
                ;;
            "macos")
                brew services start mysql 2>/dev/null
                ;;
            *)
                print_warning "Por favor inicia MySQL manualmente"
                ;;
        esac
    fi
    
    # Crear base de datos y usuario
    print_status "Creando base de datos y usuario..."
    
    mysql -u root -p -e "
        CREATE DATABASE IF NOT EXISTS agrogestion;
        CREATE USER IF NOT EXISTS 'agrogestion'@'localhost' IDENTIFIED BY 'agrogestion123';
        GRANT ALL PRIVILEGES ON agrogestion.* TO 'agrogestion'@'localhost';
        FLUSH PRIVILEGES;
    " 2>/dev/null || {
        print_warning "No se pudo configurar la base de datos automáticamente."
        print_status "Por favor ejecuta manualmente:"
        echo "mysql -u root -p"
        echo "CREATE DATABASE agrogestion;"
        echo "CREATE USER 'agrogestion'@'localhost' IDENTIFIED BY 'agrogestion123';"
        echo "GRANT ALL PRIVILEGES ON agrogestion.* TO 'agrogestion'@'localhost';"
        echo "FLUSH PRIVILEGES;"
        echo "EXIT;"
        echo "mysql -u agrogestion -p agrogestion < database_script.sql"
    }
}

# Configurar backend
setup_backend() {
    print_status "Configurando backend..."
    
    if [ ! -d "agrogestion-backend" ]; then
        print_error "Directorio agrogestion-backend no encontrado"
        return
    fi
    
    cd agrogestion-backend
    
    # Verificar si application.properties existe
    if [ ! -f "src/main/resources/application.properties" ]; then
        print_error "application.properties no encontrado"
        cd ..
        return
    fi
    
    print_success "Backend configurado correctamente"
    cd ..
}

# Configurar frontend
setup_frontend() {
    print_status "Configurando frontend..."
    
    if [ ! -d "agrogestion-frontend" ]; then
        print_error "Directorio agrogestion-frontend no encontrado"
        return
    fi
    
    cd agrogestion-frontend
    
    # Instalar dependencias
    print_status "Instalando dependencias del frontend..."
    npm install
    
    if [ $? -eq 0 ]; then
        print_success "Dependencias del frontend instaladas correctamente"
    else
        print_error "Error al instalar dependencias del frontend"
        cd ..
        return
    fi
    
    cd ..
}

# Crear archivo de configuración
create_env_file() {
    print_status "Creando archivo de configuración..."
    
    cat > .env << EOF
# AgroGestion - Variables de Entorno
# Backend
SPRING_PROFILES_ACTIVE=dev
MYSQL_HOST=localhost
MYSQL_PORT=3306
MYSQL_DATABASE=agrogestion
MYSQL_USERNAME=agrogestion
MYSQL_PASSWORD=agrogestion123

# Frontend
VITE_API_BASE_URL=http://localhost:8080/api
VITE_APP_TITLE=AgroGestion
EOF
    
    print_success "Archivo .env creado"
}

# Crear script de inicio
create_start_script() {
    print_status "Creando script de inicio..."
    
    cat > start.sh << 'EOF'
#!/bin/bash

# AgroGestion - Script de Inicio
echo "🌾 Iniciando AgroGestion..."

# Función para manejar la salida
cleanup() {
    echo "Deteniendo servicios..."
    kill $BACKEND_PID $FRONTEND_PID 2>/dev/null
    exit 0
}

# Capturar Ctrl+C
trap cleanup SIGINT

# Iniciar backend
echo "Iniciando backend..."
cd agrogestion-backend
mvn spring-boot:run &
BACKEND_PID=$!
cd ..

# Esperar un momento para que el backend inicie
sleep 10

# Iniciar frontend
echo "Iniciando frontend..."
cd agrogestion-frontend
npm run dev &
FRONTEND_PID=$!
cd ..

echo "✅ AgroGestion iniciado correctamente!"
echo "🌐 Frontend: http://localhost:5173"
echo "🔧 Backend: http://localhost:8080"
echo "📚 API Docs: http://localhost:8080/swagger-ui.html"
echo ""
echo "Presiona Ctrl+C para detener todos los servicios"

# Esperar a que se detengan los procesos
wait
EOF
    
    chmod +x start.sh
    print_success "Script de inicio creado: ./start.sh"
}

# Función principal
main() {
    detect_os
    check_prerequisites
    setup_database
    setup_backend
    setup_frontend
    create_env_file
    create_start_script
    
    echo ""
    echo "🎉 ¡Configuración completada!"
    echo ""
    echo "📋 Próximos pasos:"
    echo "1. Configura MySQL si no se hizo automáticamente"
    echo "2. Ejecuta: ./start.sh"
    echo "3. Abre http://localhost:5173 en tu navegador"
    echo ""
    echo "👥 Usuarios de prueba:"
    echo "   admin / admin123"
    echo "   tecnico / tecnico123"
    echo "   productor / productor123"
    echo ""
    echo "📚 Documentación: README.md"
}

# Ejecutar función principal
main "$@"
