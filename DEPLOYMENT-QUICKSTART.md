# 🚀 Quick Start: Deployment en 30 minutos

## ⚡ Pasos Rápidos

### 1️⃣ Railway Testing (10 min)

1. **Crear proyecto:**
   - Ve a https://railway.app
   - New Project → "AgroGestion-Testing"

2. **Agregar MySQL:**
   - Click "+" → Database → MySQL
   - Espera 1 minuto

3. **Agregar Backend:**
   - Click "+" → GitHub Repo
   - Selecciona tu repo
   - Variables (copia de `VARIABLES-ENTORNO-RAILWAY.md`):
   ```
   SPRING_PROFILES_ACTIVE=testing
   DATABASE_URL=jdbc:mysql://${MYSQLHOST}:${MYSQLPORT}/${MYSQLDATABASE}?useSSL=false&serverTimezone=UTC
   DB_USERNAME=${MYSQLUSER}
   DB_PASSWORD=${MYSQLPASSWORD}
   HIBERNATE_DDL_AUTO=update
   JWT_SECRET=testing_secret_CHANGE_ME_123456789012345678901234567890
   CORS_ALLOWED_ORIGINS=https://agrogestion-testing.vercel.app,http://localhost:3000
   ```
   - Deploy automático

4. **Copiar URL:**
   - Ej: `https://xxx.up.railway.app`

---

### 2️⃣ Vercel Testing (5 min)

1. **Crear proyecto:**
   - Ve a https://vercel.com
   - Add New → Project
   - Import tu repo

2. **Configurar:**
   - Name: `agrogestion-testing`
   - Root: `agrogestion-frontend`
   - Framework: Vite (auto-detectado)

3. **Variable de entorno:**
   - Key: `VITE_API_URL`
   - Value: `https://[tu-railway-url].up.railway.app`
   - Environment: Preview + Development

4. **Deploy**

---

### 3️⃣ Railway Production (10 min)

Repite paso 1 pero:
- Nombre: "AgroGestion-Production"
- Variables diferentes (JWT_SECRET único)
- `HIBERNATE_DDL_AUTO=validate`

---

### 4️⃣ Vercel Production (5 min)

Repite paso 2 pero:
- Name: `agrogestion`
- Variable: URL de Railway Production
- Environment: Production

---

## ✅ Verificar

**Testing:**
```bash
curl https://[railway-testing].up.railway.app/actuator/health
```

**Production:**
```bash
curl https://[railway-production].up.railway.app/actuator/health
```

Ambos deben responder: `{"status":"UP"}`

---

## 📝 Documentos Completos

- `GUIA-DEPLOYMENT-TESTING-PRODUCCION.md` - Guía completa paso a paso
- `VARIABLES-ENTORNO-RAILWAY.md` - Todas las variables explicadas
- `CHECKLIST-DEPLOYMENT.md` - Checklist detallado

---

## 🆘 Problemas Comunes

**Error: CORS**  
→ Verifica `CORS_ALLOWED_ORIGINS` en Railway

**Error: Database connection**  
→ Verifica que MySQL esté conectado en Railway

**Error: 404**  
→ Espera 2-3 minutos, Railway está compilando

---

¿Listo? ¡Empecemos! 🚀

