# 🚀 Deployment Paso a Paso - AgroGestion v2.0

## 📑 Índice
1. [Preparación Local](#preparación-local)
2. [Railway Testing](#railway-testing)
3. [Vercel Testing](#vercel-testing)
4. [Probar Testing](#probar-testing)
5. [Railway Production](#railway-production)
6. [Vercel Production](#vercel-production)
7. [Verificación Final](#verificación-final)

---

## 🔧 Preparación Local

### 1. Verificar que todo funciona localmente

```bash
# Backend
cd agrogestion-backend
mvn clean package -DskipTests
# Debe terminar con BUILD SUCCESS

# Frontend
cd ../agrogestion-frontend
npm run build
# Debe crear la carpeta dist/ sin errores
```

### 2. Commitear todos los cambios

```bash
git add .
git commit -m "feat: preparar para deployment testing y production"
git push origin main
```

### 3. Crear rama develop (si no existe)

```bash
git checkout -b develop
git push origin develop
```

---

## 🚂 Railway Testing

### PASO 1: Crear Proyecto

1. Ve a https://railway.app/new
2. Click en **"Empty Project"**
3. Nombra el proyecto: **"AgroGestion-Testing"**

### PASO 2: Agregar MySQL

1. En el proyecto, click **"+ New"**
2. Selecciona **"Database"** → **"Add MySQL"**
3. Espera ~1 minuto a que se cree
4. Click en el servicio MySQL
5. Ve a **"Variables"** y guarda estas variables (las necesitarás):
   ```
   MYSQLHOST
   MYSQLPORT
   MYSQLUSER
   MYSQLPASSWORD
   MYSQLDATABASE
   ```

### PASO 3: Conectar y Migrar la Base de Datos

**Opción A: Desde Railway CLI**

```bash
# Instalar Railway CLI
npm install -g @railway/cli

# Login
railway login

# Conectar al proyecto
railway link

# Conectar a MySQL
railway run mysql -h $MYSQLHOST -P $MYSQLPORT -u $MYSQLUSER -p$MYSQLPASSWORD $MYSQLDATABASE

# Dentro de MySQL, ejecutar:
source database-migration-master-testing.sql;
quit;
```

**Opción B: Desde MySQL local**

```bash
mysql -h [MYSQLHOST] -P [MYSQLPORT] -u [MYSQLUSER] -p[MYSQLPASSWORD] [MYSQLDATABASE] < database-migration-master-testing.sql
```

Reemplaza los valores entre [] con los de Railway.

**Opción C: Usando Railway Web Console**

1. En Railway, click en MySQL
2. Click en **"Data"** (pestaña arriba)
3. Click en **"Query"**
4. Copia y pega el contenido de `database-migration-master-testing.sql`
5. Click **"Execute"**

### PASO 4: Agregar Servicio Backend

1. En el proyecto, click **"+ New"**
2. Selecciona **"GitHub Repo"**
3. Autoriza GitHub si es necesario
4. Selecciona tu repositorio **"AgroGestion"**
5. Railway detectará automáticamente el proyecto Maven

### PASO 5: Configurar Backend

1. Click en el servicio Backend
2. Ve a **"Settings"**:
   - **Service Name:** `backend-testing`
   - **Watch Paths:** `agrogestion-backend/**`

3. Ve a **"Variables"** y agrega:

```env
SPRING_PROFILES_ACTIVE=testing
DATABASE_URL=jdbc:mysql://${{MySQL.MYSQLHOST}}:${{MySQL.MYSQLPORT}}/${{MySQL.MYSQLDATABASE}}?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
DB_USERNAME=${{MySQL.MYSQLUSER}}
DB_PASSWORD=${{MySQL.MYSQLPASSWORD}}
HIBERNATE_DDL_AUTO=update
SHOW_SQL=false
JWT_SECRET=testing_secret_key_CHANGE_ME_12345678901234567890ABCDEFGHIJKLMNOP
JWT_EXPIRATION=86400000
CORS_ALLOWED_ORIGINS=https://agrogestion-testing.vercel.app,http://localhost:3000
PORT=8080
```

**💡 Tip:** Railway auto-completa las referencias `${{MySQL.VARIABLE}}`

4. Click **"Deploy"** (arriba a la derecha)

### PASO 6: Obtener URL del Backend

1. Una vez desplegado (2-3 minutos), ve a **"Settings"**
2. En **"Networking"**, click **"Generate Domain"**
3. Copia la URL generada, ejemplo:
   ```
   https://backend-testing-production-xxxx.up.railway.app
   ```
4. **GUÁRDALA**, la necesitas para Vercel

---

## 🎨 Vercel Testing

### PASO 1: Crear Proyecto

1. Ve a https://vercel.com/new
2. Click **"Import Git Repository"**
3. Selecciona tu repositorio de GitHub
4. Configura:
   - **Project Name:** `agrogestion-testing`
   - **Framework Preset:** Vite (auto-detectado)
   - **Root Directory:** `agrogestion-frontend`
   - **Build Command:** `npm run build` (auto-detectado)
   - **Output Directory:** `dist` (auto-detectado)

### PASO 2: Configurar Variables de Entorno

En **"Environment Variables"**, agrega:

| Name | Value | Environment |
|------|-------|-------------|
| `VITE_API_URL` | `https://[tu-backend-testing].up.railway.app` | Preview, Development |
| `VITE_ENVIRONMENT` | `testing` | Preview, Development |

⚠️ **No incluyas** `/api` al final de la URL, el código lo agrega automáticamente.

### PASO 3: Deploy

1. Click **"Deploy"**
2. Espera ~2 minutos
3. Una vez completado, obtendrás una URL:
   ```
   https://agrogestion-testing.vercel.app
   ```

### PASO 4: Actualizar CORS en Railway

1. Vuelve a Railway → Backend Testing
2. Ve a **"Variables"**
3. Actualiza `CORS_ALLOWED_ORIGINS` con la URL real de Vercel:
   ```
   https://agrogestion-testing.vercel.app,http://localhost:3000
   ```
4. El servicio se reiniciará automáticamente

---

## 🧪 Probar Testing

### 1. Abrir la Aplicación

Ve a: `https://agrogestion-testing.vercel.app`

### 2. Iniciar Sesión

Usa las credenciales de testing:
```
Email: admin.testing@agrogestion.com
Password: password123
```

### 3. Verificar Funcionalidades

- [ ] Login funciona
- [ ] Dashboard carga correctamente
- [ ] Puedes ver campos, lotes, cultivos
- [ ] Puedes crear un lote
- [ ] Puedes ver insumos y maquinaria
- [ ] Puedes crear una labor
- [ ] Reportes cargan datos
- [ ] No hay errores en consola del navegador (F12)

### 4. Verificar CORS

Abre la consola del navegador (F12) y busca:
- ✅ No debe haber errores de CORS
- ✅ Las peticiones a la API deben ser exitosas

Si hay errores CORS, verifica `CORS_ALLOWED_ORIGINS` en Railway.

---

## 🚀 Railway Production

### PASO 1: Crear Proyecto

1. Ve a https://railway.app/new
2. Click en **"Empty Project"**
3. Nombra el proyecto: **"AgroGestion-Production"**

### PASO 2: Agregar MySQL

Igual que Testing, pero:
- **⚠️ Base de datos NUEVA y SEPARADA**
- **⚠️ Habilita backups automáticos**

En Railway MySQL:
1. Click en MySQL service
2. Ve a **"Settings"** → **"Backups"**
3. Habilita backups automáticos (si está disponible)

### PASO 3: Migrar Base de Datos Production

```bash
# Conectar a MySQL de Production
mysql -h [MYSQLHOST] -P [MYSQLPORT] -u [MYSQLUSER] -p[MYSQLPASSWORD] [MYSQLDATABASE] < database-migration-master-production.sql
```

### PASO 4: Agregar Backend y Configurar

Igual que Testing, pero con estas variables:

```env
SPRING_PROFILES_ACTIVE=production
DATABASE_URL=jdbc:mysql://${{MySQL.MYSQLHOST}}:${{MySQL.MYSQLPORT}}/${{MySQL.MYSQLDATABASE}}?useSSL=true&serverTimezone=UTC&allowPublicKeyRetrieval=true
DB_USERNAME=${{MySQL.MYSQLUSER}}
DB_PASSWORD=${{MySQL.MYSQLPASSWORD}}
HIBERNATE_DDL_AUTO=validate
SHOW_SQL=false
JWT_SECRET=[GENERA_UNO_UNICO_AQUI_64_CARACTERES_MINIMO]
JWT_EXPIRATION=86400000
CORS_ALLOWED_ORIGINS=https://agrogestion.vercel.app
PORT=8080
```

**⚠️ CRÍTICO:** Genera un JWT_SECRET único:

```bash
# Windows PowerShell
[System.Convert]::ToBase64String([System.Security.Cryptography.RandomNumberGenerator]::GetBytes(64))

# O usa este:
openssl rand -base64 64
```

### PASO 5: Obtener URL y Deploy

1. Genera dominio en Railway
2. Guarda la URL
3. Deploy automático iniciará

---

## 🎨 Vercel Production

### PASO 1: Crear Proyecto

Similar a Testing:
- **Project Name:** `agrogestion` (o `agrogestion-production`)
- **Root Directory:** `agrogestion-frontend`

### PASO 2: Variables de Entorno

| Name | Value | Environment |
|------|-------|-------------|
| `VITE_API_URL` | `https://[tu-backend-production].up.railway.app` | Production |
| `VITE_ENVIRONMENT` | `production` | Production |

### PASO 3: Deploy

1. Click **"Deploy"**
2. Espera ~2 minutos

### PASO 4: Actualizar CORS en Railway Production

Actualiza la variable con la URL real de Vercel.

---

## ✅ Verificación Final

### Testing Environment

1. URL Frontend: `https://agrogestion-testing.vercel.app`
2. URL Backend Health: `https://[backend].up.railway.app/actuator/health`
   - Debe responder: `{"status":"UP"}`
3. Login funciona con usuarios de testing
4. Todas las funcionalidades operan correctamente

### Production Environment

1. URL Frontend funciona
2. Backend health check responde
3. Login funciona con admin de production
4. **⚠️ Cambiar password inmediatamente**
5. Actualizar datos de la empresa
6. Crear usuarios reales
7. Configurar datos reales

---

## 🎯 Resumen de URLs

Cuando termines, tendrás:

### Testing:
- **Frontend:** `https://agrogestion-testing.vercel.app`
- **Backend:** `https://backend-testing-xxxx.up.railway.app`
- **Usuarios:** 5 usuarios de prueba (password: password123)

### Production:
- **Frontend:** `https://agrogestion.vercel.app`
- **Backend:** `https://backend-xxxx.up.railway.app`
- **Usuarios:** Solo admin inicial (cambiar password)

---

## 📝 Checklist Final

- [ ] Testing funciona 100%
- [ ] Production funciona 100%
- [ ] Passwords de production cambiadas
- [ ] Datos de empresa actualizados
- [ ] Backups configurados
- [ ] CORS configurado correctamente
- [ ] No hay errores en logs
- [ ] Documentación actualizada
- [ ] Equipo notificado de nuevas URLs

---

## 🎉 ¡Listo!

Tu aplicación está desplegada en dos ambientes completamente funcionales.

**Siguiente:** Configura monitoreo y backups automáticos.

---

**Duración estimada:** 45-60 minutos total  
**Dificultad:** Media  
**Prerequisitos:** Cuentas en Railway y Vercel

