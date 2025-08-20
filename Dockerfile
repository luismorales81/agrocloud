# Dockerfile para AgroCloud Backend
FROM maven:3.9.5-openjdk-17 AS build

# Establecer directorio de trabajo
WORKDIR /app

# Copiar archivos de configuración de Maven
COPY agrogestion-backend/pom.xml .

# Copiar código fuente
COPY agrogestion-backend/src src

# Compilar la aplicación con Maven
RUN mvn clean package -DskipTests

# Segunda etapa: Runtime
FROM openjdk:17-slim

WORKDIR /app

# Copiar el JAR compilado desde la etapa de build
COPY --from=build /app/target/agrocloud-backend-1.0.0.jar app.jar

# Exponer puerto
EXPOSE 8080

# Healthcheck simple usando el endpoint raíz
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/ || exit 1

# Ejecutar la aplicación con perfil Railway
ENTRYPOINT ["java", "-Dspring.profiles.active=railway", "-jar", "app.jar"]
