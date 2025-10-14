# 🔐 Variables de Entorno para Railway

## 🧪 Testing Environment

Copia y pega estas variables en Railway (Proyecto Testing):

```env
SPRING_PROFILES_ACTIVE=testing
DATABASE_URL=jdbc:mysql://${MYSQLHOST}:${MYSQLPORT}/${MYSQLDATABASE}?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
DB_USERNAME=${MYSQLUSER}
DB_PASSWORD=${MYSQLPASSWORD}
HIBERNATE_DDL_AUTO=update
SHOW_SQL=false
JWT_SECRET=testing_secret_key_CHANGE_ME_123456789012345678901234567890
JWT_EXPIRATION=86400000
CORS_ALLOWED_ORIGINS=https://agrogestion-testing.vercel.app,http://localhost:3000,http://localhost:5173
PORT=8080
```

### Notas para Testing:
- Las variables `${MYSQLHOST}`, `${MYSQLPORT}`, etc. serán reemplazadas automáticamente por Railway
- `HIBERNATE_DDL_AUTO=update` permite actualizaciones automáticas del schema
- `SHOW_SQL=false` para no llenar los logs

---

## 🚀 Production Environment

Copia y pega estas variables en Railway (Proyecto Production):

```env
SPRING_PROFILES_ACTIVE=production
DATABASE_URL=jdbc:mysql://${MYSQLHOST}:${MYSQLPORT}/${MYSQLDATABASE}?useSSL=true&serverTimezone=UTC&allowPublicKeyRetrieval=true
DB_USERNAME=${MYSQLUSER}
DB_PASSWORD=${MYSQLPASSWORD}
HIBERNATE_DDL_AUTO=validate
SHOW_SQL=false
JWT_SECRET=GENERA_UN_SECRET_UNICO_MUY_LARGO_Y_SEGURO_AQUI_123456789012345678901234567890
JWT_EXPIRATION=86400000
CORS_ALLOWED_ORIGINS=https://agrogestion.vercel.app,https://agrogestion-production.vercel.app
PORT=8080
```

### Notas para Production:
- **⚠️ IMPORTANTE:** Cambia `JWT_SECRET` por un valor único y muy seguro
- `HIBERNATE_DDL_AUTO=validate` evita cambios automáticos (más seguro)
- `useSSL=true` para conexiones seguras
- Solo incluye las URLs del frontend en producción

---

## 🔑 Generar JWT_SECRET Seguro

Usa uno de estos métodos:

### Opción 1: Node.js
```bash
node -e "console.log(require('crypto').randomBytes(64).toString('hex'))"
```

### Opción 2: OpenSSL
```bash
openssl rand -hex 64
```

### Opción 3: Python
```bash
python -c "import secrets; print(secrets.token_hex(64))"
```

### Opción 4: Online (usar con precaución)
https://generate-secret.vercel.app/64

---

## 📝 Variables Opcionales

Si necesitas configuraciones adicionales:

```env
# Logging
LOGGING_LEVEL_ROOT=WARN
LOGGING_LEVEL_COM_AGROCLOUD=INFO

# Pool de Conexiones
SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE=10
SPRING_DATASOURCE_HIKARI_MINIMUM_IDLE=5
SPRING_DATASOURCE_HIKARI_CONNECTION_TIMEOUT=30000

# Tamaño de archivos (si usas uploads)
SPRING_SERVLET_MULTIPART_MAX_FILE_SIZE=10MB
SPRING_SERVLET_MULTIPART_MAX_REQUEST_SIZE=10MB
```

---

## ✅ Checklist de Configuración

### Testing:
- [ ] Todas las variables copiadas
- [ ] JWT_SECRET cambiado (diferente a producción)
- [ ] CORS incluye URL de Vercel Testing
- [ ] MySQL database creada y conectada

### Production:
- [ ] Todas las variables copiadas
- [ ] JWT_SECRET ÚNICO y MUY SEGURO (64+ caracteres)
- [ ] CORS solo incluye URLs de producción
- [ ] MySQL database creada con backup habilitado
- [ ] Variables verificadas 2 veces

---

## 🚨 Seguridad

### ❌ NUNCA:
- Commitear archivos con JWT_SECRET real
- Usar el mismo JWT_SECRET en testing y production
- Compartir credenciales de production en Slack/Discord
- Usar passwords débiles

### ✅ SIEMPRE:
- Usar secretos únicos por ambiente
- Rotar JWT_SECRET periódicamente
- Mantener respaldos de las variables
- Documentar cambios de configuración

---

## 📊 Cómo Verificar que Funcionan

### 1. Health Check
```bash
curl https://agrogestion-testing.up.railway.app/actuator/health
```

Debería responder:
```json
{"status":"UP"}
```

### 2. CORS
Desde el frontend, abre la consola del navegador. Si hay errores CORS, verifica `CORS_ALLOWED_ORIGINS`.

### 3. Base de Datos
Verifica en los logs de Railway que se conecta exitosamente:
```
HikariPool-1 - Start completed.
```

---

## 🔄 Actualizar Variables

Si necesitas cambiar alguna variable:

1. Ve a Railway → Tu Proyecto → Variables
2. Click en la variable a cambiar
3. Edita el valor
4. Click en **"Update"**
5. Railway reiniciará automáticamente el servicio

⚠️ El servicio se reiniciará, habrá ~30-60 segundos de downtime.

---

**Fecha:** 10 de Octubre de 2025  
**Versión:** 1.0

