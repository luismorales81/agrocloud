#!/bin/bash

echo "🚀 Iniciando build directo con Java..."

# Verificar Java
echo "📋 Verificando Java..."
java -version

# Crear directorio de salida
echo "📁 Creando directorio de salida..."
mkdir -p target/classes

# Compilar directamente con Java
echo "🔨 Compilando con Java..."
javac -d target/classes agrogestion-backend/src/main/java/com/agrocloud/HttpServer.java

# Verificar compilación
echo "✅ Verificando compilación..."
ls -la target/classes/com/agrocloud/

# Crear JAR manualmente
echo "📦 Creando JAR..."
cd target/classes
jar cfm ../agrocloud-http.jar ../../META-INF/MANIFEST.MF com/agrocloud/*.class
cd ../..

# Verificar JAR
echo "✅ Verificando JAR..."
ls -la target/agrocloud-http.jar

echo "🎉 Build completado exitosamente!"
echo "🚀 Ejecutando servidor..."
java -jar target/agrocloud-http.jar
