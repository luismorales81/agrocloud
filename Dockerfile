# Dockerfile para AgroCloud Backend
FROM maven:3.9.5-eclipse-temurin-17 AS build

# Establecer directorio de trabajo
WORKDIR /app

# Copiar archivos de configuración de Maven
COPY agrogestion-backend/pom.xml .

# Copiar código fuente
COPY agrogestion-backend/src src

# Copiar configuración de aplicación
COPY agrogestion-backend/src/main/resources/application.properties src/main/resources/

# Compilar la aplicación con Maven
RUN mvn clean package -DskipTests

# Segunda etapa: Runtime
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Instalar wget para healthcheck
RUN apk add --no-cache wget

# Copiar el JAR compilado desde la etapa de build
COPY --from=build /app/target/agrocloud-backend-1.0.0.jar app.jar

# Exponer puerto
EXPOSE 8080

# Healthcheck simple usando el endpoint raíz
HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:${PORT:-8080}/ || exit 1

# Ejecutar la aplicación con perfil Railway (fallback a H2)
ENTRYPOINT ["java", "-Dspring.profiles.active=railway", "-jar", "app.jar"]
