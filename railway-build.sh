#!/bin/bash

# Script de build alternativo para Railway
echo "🚀 Iniciando build alternativo para Railway..."

# Verificar Java
echo "📋 Verificando Java..."
java -version

# Instalar Maven si no está disponible
if ! command -v mvn &> /dev/null; then
    echo "📦 Instalando Maven..."
    apk add --no-cache maven
fi

# Verificar Maven
echo "📋 Verificando Maven..."
mvn -version

# Crear estructura de directorios
echo "📁 Creando estructura de directorios..."
mkdir -p src/main/java/com/agrocloud
mkdir -p src/main/resources

# Copiar archivos
echo "📋 Copiando archivos..."
cp agrogestion-backend/pom-test.xml pom.xml
cp agrogestion-backend/src/main/java/com/agrocloud/TestApplication.java src/main/java/com/agrocloud/
cp agrogestion-backend/src/main/resources/application-test.properties src/main/resources/

# Compilar aplicación
echo "🔨 Compilando aplicación..."
mvn clean package -DskipTests

# Verificar que el JAR se creó
echo "✅ Verificando JAR..."
ls -la target/

echo "🎉 Build completado exitosamente!"
