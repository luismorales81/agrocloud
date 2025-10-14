# ============================================================
# DOCKERFILE SIMPLIFICADO - Spring Boot para Railway
# AgroGestion Backend
# ============================================================

# ============================================================
# ETAPA 1: BUILD (Compilación con Maven)
# ============================================================
FROM maven:3.9.5-eclipse-temurin-17 AS build

# Establecer directorio de trabajo
WORKDIR /app

# Copiar archivos de configuración de Maven
COPY pom.xml .

# Descargar dependencias (para cache de Docker)
RUN mvn dependency:go-offline -B

# Copiar código fuente
COPY src ./src

# Compilar la aplicación (saltando tests)
RUN mvn clean package -DskipTests

# ============================================================
# ETAPA 2: RUNTIME (Ejecución con JRE)
# ============================================================
FROM eclipse-temurin:17-jre-alpine AS runtime

# Instalar herramientas necesarias
RUN apk add --no-cache wget

# Establecer directorio de trabajo
WORKDIR /app

# Copiar el JAR desde la etapa de build
COPY --from=build /app/target/*.jar app.jar

# Exponer puerto
EXPOSE 8080

# Variables de entorno por defecto
ENV SPRING_PROFILES_ACTIVE=testing
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Comando de arranque
ENTRYPOINT ["java", "-jar", "app.jar"]