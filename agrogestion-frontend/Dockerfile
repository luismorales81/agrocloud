# ============================================================
# Imagen de producción — AgroGestion Frontend (SPA estático)
# Multi-stage: Node compila Vite y nginx sirve dist/.
# Contexto de build: agrogestion-frontend/
#
# VITE_* se inyectan como ARG de build (Vite las embebe en el JS).
# No incluir secretos de servidor. VITE_GOOGLE_MAPS_API_KEY es
# una clave de navegador (restringir por HTTP referrer en GCP).
# ============================================================

# --- Etapa 1: compilación ---
FROM node:22-alpine AS compilacion

WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci

COPY . .

ARG VITE_API_BASE_URL=/api
ARG VITE_GOOGLE_MAPS_API_KEY=

ENV VITE_API_BASE_URL=$VITE_API_BASE_URL
ENV VITE_GOOGLE_MAPS_API_KEY=$VITE_GOOGLE_MAPS_API_KEY

RUN npm run build

# --- Etapa 2: nginx (solo archivos estáticos) ---
FROM nginx:1.27-alpine AS ejecucion

RUN apk add --no-cache wget

COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=compilacion /app/dist /usr/share/nginx/html

EXPOSE 80

HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://127.0.0.1:80/ || exit 1

STOPSIGNAL SIGQUIT

CMD ["nginx", "-g", "daemon off;"]
