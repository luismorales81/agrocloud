# ========== LEGACY - NO USAR PARA PRODUCCIÓN ==========
# Para producción usar: agrogestion-backend/Dockerfile
# Este archivo fija perfil railway-h2; el perfil debe definirse por variable de entorno.
# Dockerfile para AgroCloud Backend (solo referencia/legacy)
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
RUN mvn clean package -DskipTests && ls -la target/

# Segunda etapa: Runtime
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Instalar wget para healthcheck
RUN apk add --no-cache wget

# Copiar el JAR compilado desde la etapa de build
COPY --from=build /app/target/*.jar app.jar

# Exponer puerto
EXPOSE 8080

# Healthcheck mejorado con más tiempo de inicio
HEALTHCHECK --interval=30s --timeout=15s --start-period=120s --retries=5 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:${PORT:-8080}/ || exit 1

# Perfil por variable de entorno (ej: SPRING_PROFILES_ACTIVE=prod). No fijar en imagen.
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS:-} -jar app.jar"]
