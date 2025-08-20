# Dockerfile para AgroCloud Backend
FROM openjdk:17-slim

# Establecer directorio de trabajo
WORKDIR /app

# Copiar archivos de configuración de Maven
COPY agrogestion-backend/pom.xml .
COPY agrogestion-backend/mvnw .
COPY agrogestion-backend/mvnw.cmd .
COPY agrogestion-backend/.mvn .mvn

# Copiar código fuente
COPY agrogestion-backend/src src

# Compilar la aplicación con Maven Wrapper
RUN chmod +x mvnw && ./mvnw clean package -DskipTests

# Exponer puerto
EXPOSE 8080

# Healthcheck simple
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/health || exit 1

# Ejecutar la aplicación con perfil Railway
ENTRYPOINT ["java", "-Dspring.profiles.active=railway", "-jar", "target/agrocloud-backend-1.0.0.jar"]
