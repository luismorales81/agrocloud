# ============================================================
# Imagen de producción — AgroGestion Backend (Spring Boot)
# Multi-stage. Sin secretos, perfiles ni URLs fijas en la imagen.
# Contexto de build: agrogestion-backend/
# ============================================================

# --- Etapa 1: compilación ---
FROM maven:3.9.9-eclipse-temurin-17 AS compilacion

WORKDIR /app

COPY pom.xml .
RUN mvn -B dependency:go-offline -DskipTests

COPY src ./src
RUN mvn -B -DskipTests package \
    && find target -maxdepth 1 -type f -name '*.jar' ! -name '*original*' -exec cp {} /app/aplicacion.jar \;

# --- Etapa 2: ejecución ---
FROM eclipse-temurin:17-jre-alpine AS ejecucion

RUN apk add --no-cache wget tzdata \
    && addgroup -S agrocloud \
    && adduser -S agrocloud -G agrocloud \
    && mkdir -p /app/eula-pdfs /app/logs \
    && chown -R agrocloud:agrocloud /app

WORKDIR /app

COPY --from=compilacion --chown=agrocloud:agrocloud /app/aplicacion.jar app.jar

USER agrocloud

# Perfil, datasource, JWT y CORS se inyectan en tiempo de ejecución.
ENV TZ=UTC
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"
ENV SERVER_PORT=8080

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --start-period=300s --retries=8 \
    CMD wget --no-verbose --tries=1 --spider http://127.0.0.1:${SERVER_PORT:-8080}/api/health || exit 1

STOPSIGNAL SIGTERM

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Duser.timezone=${TZ} -jar app.jar"]
