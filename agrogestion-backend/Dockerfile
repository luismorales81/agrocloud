# Dockerfile para AgroCloud Backend
FROM openjdk:17-jdk-slim as build

# Instalar Maven
RUN apt-get update && apt-get install -y maven

# Establecer directorio de trabajo
WORKDIR /app

# Copiar archivos de configuración de Maven
COPY pom.xml .
COPY mvnw .
COPY mvnw.cmd .
COPY .mvn .mvn

# Descargar dependencias (cacheado si pom.xml no cambia)
RUN mvn dependency:go-offline -B

# Copiar código fuente
COPY src src

# Compilar la aplicación
RUN mvn clean package -DskipTests

# Etapa de ejecución
FROM openjdk:17-jdk-slim

# Instalar curl para healthcheck
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Crear usuario no-root
RUN addgroup --system spring && adduser --system spring --ingroup spring
USER spring:spring

# Establecer directorio de trabajo
WORKDIR /app

# Copiar el JAR desde la etapa de build
COPY --from=build /app/target/*.jar app.jar

# Exponer puerto
EXPOSE 8080

# Healthcheck
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
