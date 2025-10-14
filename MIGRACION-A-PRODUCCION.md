# 🚀 Guía de Migración a Producción - AgroCloud

## 📋 Resumen de cambios realizados

### ✅ **Backend actualizaciones:**
1. ✅ `SecurityConfig.java` - CORS dinámico con variables de entorno
2. ✅ `EmailService.java` - URLs dinámicas configurables
3. ✅ `application-prod.properties` - Archivo de configuración para producción

### ✅ **Archivos hardcodeados encontrados:**
- **Frontend**: 63 referencias a `localhost:8080`
- **Backend**: 29 referencias a `localhost` en CORS y configuraciones

---

## 🔧 Pasos para migrar a Producción

### **1. Frontend - Configuración**

#### A. Crear archivos de entorno

En `agrogestion-frontend/` crear:

**`.env.production`**:
```env
VITE_API_BASE_URL=https://api.tudominio.com
VITE_ENV=production
VITE_APP_NAME=AgroCloud
VITE_APP_VERSION=1.0.0
```

#### B. Actualizar código (CRÍTICO)

Todos los `fetch()` hardcodeados deben usar el servicio `api.ts`:

**Antes:**
```typescript
const response = await fetch('http://localhost:8080/api/labores', {...});
```

**Después:**
```typescript
import api from '../services/api';
const response = await api.get('/api/labores');
```

#### C. Build para producción
```bash
cd agrogestion-frontend
npm run build  # Usa automáticamente .env.production
```

---

### **2. Backend - Configuración**

#### A. Variables de entorno en el servidor

Configurar en el servidor de producción:

```bash
# Base de datos
export DATABASE_URL="jdbc:mysql://tu-servidor-mysql:3306/agrocloud?useSSL=true&serverTimezone=UTC"
export DATABASE_USERNAME="agrocloud_prod"
export DATABASE_PASSWORD="password-muy-seguro-y-largo"

# JWT (cambiar por uno único y seguro)
export JWT_SECRET="tu-secreto-jwt-production-cambiar-por-aleatorio-largo-min-64-caracteres"
export JWT_EXPIRATION="86400000"

# CORS - Frontend URL
export CORS_ALLOWED_ORIGINS="https://app.tudominio.com,https://www.tudominio.com"

# Frontend URL para emails
export FRONTEND_URL="https://app.tudominio.com"

# Puerto
export SERVER_PORT="8080"
```

#### B. Build del backend
```bash
cd agrogestion-backend
mvn clean package -DskipTests
```

#### C. Ejecutar en producción
```bash
java -jar target/agrogestion-backend-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=prod \
  --spring.config.location=classpath:/application-prod.properties
```

---

### **3. Base de Datos**

#### Opción A: MySQL en servidor dedicado
```bash
# Crear base de datos
mysql -u root -p
CREATE DATABASE agrocloud CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# Crear usuario
CREATE USER 'agrocloud_prod'@'%' IDENTIFIED BY 'password-seguro';
GRANT ALL PRIVILEGES ON agrocloud.* TO 'agrocloud_prod'@'%';
FLUSH PRIVILEGES;
```

#### Opción B: Servicio gestionado (recomendado)
- AWS RDS MySQL
- Azure Database for MySQL
- Google Cloud SQL
- DigitalOcean Managed Database

---

### **4. Despliegue con Docker (Recomendado)**

#### Crear `docker-compose.prod.yml`:

```yaml
version: '3.8'

services:
  backend:
    build: ./agrogestion-backend
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - DATABASE_URL=jdbc:mysql://db:3306/agrocloud?useSSL=false&serverTimezone=UTC
      - DATABASE_USERNAME=agrocloud
      - DATABASE_PASSWORD=${DB_PASSWORD}
      - JWT_SECRET=${JWT_SECRET}
      - FRONTEND_URL=https://app.tudominio.com
      - CORS_ALLOWED_ORIGINS=https://app.tudominio.com
    depends_on:
      - db
    restart: unless-stopped

  db:
    image: mysql:8.0
    environment:
      - MYSQL_ROOT_PASSWORD=${DB_ROOT_PASSWORD}
      - MYSQL_DATABASE=agrocloud
      - MYSQL_USER=agrocloud
      - MYSQL_PASSWORD=${DB_PASSWORD}
    volumes:
      - mysql-data:/var/lib/mysql
    restart: unless-stopped

  frontend:
    build:
      context: ./agrogestion-frontend
      args:
        - VITE_API_BASE_URL=https://api.tudominio.com
    ports:
      - "80:80"
    restart: unless-stopped

volumes:
  mysql-data:
```

#### Crear `.env` (NO subir a Git):
```env
DB_ROOT_PASSWORD=root-password-seguro
DB_PASSWORD=password-seguro
JWT_SECRET=jwt-secret-muy-largo-y-aleatorio
```

#### Ejecutar:
```bash
docker-compose -f docker-compose.prod.yml up -d
```

---

### **5. Configuración de Nginx (Reverse Proxy)**

```nginx
# /etc/nginx/sites-available/agrocloud

# Backend API
server {
    listen 443 ssl;
    server_name api.tudominio.com;

    ssl_certificate /etc/letsencrypt/live/tudominio.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/tudominio.com/privkey.pem;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}

# Frontend
server {
    listen 443 ssl;
    server_name app.tudominio.com;

    ssl_certificate /etc/letsencrypt/live/tudominio.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/tudominio.com/privkey.pem;

    root /var/www/agrocloud-frontend/dist;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }
}

# Redirect HTTP to HTTPS
server {
    listen 80;
    server_name api.tudominio.com app.tudominio.com;
    return 301 https://$server_name$request_uri;
}
```

---

### **6. Certificado SSL (Let's Encrypt)**

```bash
# Instalar Certbot
sudo apt install certbot python3-certbot-nginx

# Obtener certificado
sudo certbot --nginx -d app.tudominio.com -d api.tudominio.com

# Renovación automática (ya configurada)
sudo certbot renew --dry-run
```

---

## ⚠️ Checklist de Seguridad

Antes de ir a producción:

- [ ] ✅ Cambiar `JWT_SECRET` por uno aleatorio y largo (min 64 caracteres)
- [ ] ✅ Usar contraseñas fuertes para la base de datos
- [ ] ✅ Configurar SSL/TLS (HTTPS)
- [ ] ✅ Configurar firewall (solo puertos 80, 443, 22)
- [ ] ✅ No exponer puerto 3306 (MySQL) públicamente
- [ ] ✅ Configurar backups automáticos de la base de datos
- [ ] ✅ Configurar monitoreo (logs, métricas)
- [ ] ✅ Revisar que `.env` esté en `.gitignore`
- [ ] ✅ Configurar rate limiting en Nginx
- [ ] ✅ Habilitar logging de accesos y errores

---

## 📊 Monitoreo y Logs

### Backend logs:
```bash
# Ver logs en tiempo real
tail -f logs/agrocloud.log

# Logs con Docker
docker logs -f agrocloud-backend
```

### Configurar logrotate:
```bash
# /etc/logrotate.d/agrocloud
/var/log/agrocloud/*.log {
    daily
    rotate 30
    compress
    delaycompress
    notifempty
    create 0640 www-data www-data
}
```

---

## 🆘 Troubleshooting

### Error: CORS
- Verificar variable `CORS_ALLOWED_ORIGINS`
- Verificar que incluye `https://` no `http://`

### Error: Database connection
- Verificar `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`
- Verificar que MySQL está corriendo
- Verificar firewall del servidor MySQL

### Error: 404 en rutas del frontend
- Configurar Nginx con `try_files $uri /index.html`
- Verificar que el build se hizo correctamente

---

## 📞 Soporte

Para problemas en producción, revisar:
1. Logs del backend: `/var/log/agrocloud/`
2. Logs de Nginx: `/var/log/nginx/error.log`
3. Logs de Docker: `docker logs agrocloud-backend`

---

**Fecha de creación:** 2025-10-06  
**Versión:** 1.0

