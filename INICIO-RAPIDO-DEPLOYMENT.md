# ⚡ Inicio Rápido: Deployment en 10 Pasos

## 🎯 Testing Environment (30-45 minutos)

### 📝 Antes de Empezar:
- [ ] Cuenta en Railway.app
- [ ] Cuenta en Vercel.com
- [ ] Código en GitHub

---

### 1️⃣ Railway → Crear Proyecto Testing (2 min)
```
1. https://railway.app/new
2. "Empty Project"
3. Nombre: "AgroGestion-Testing"
```

### 2️⃣ Railway → Agregar MySQL (1 min)
```
1. Click "+"
2. "Database" → "MySQL"
3. Esperar 1 minuto
```

### 3️⃣ Railway → Migrar Base de Datos (5 min)
```
1. Copiar credenciales MySQL (en Variables tab):
   - MYSQLHOST
   - MYSQLPORT
   - MYSQLUSER
   - MYSQLPASSWORD
   - MYSQLDATABASE

2. Ejecutar en tu PC:

mysql -h [MYSQLHOST] -P [MYSQLPORT] -u [MYSQLUSER] -p[MYSQLPASSWORD] [MYSQLDATABASE] < database-migration-testing-completo.sql

(Reemplaza los valores entre [] con los de Railway)
(Pega la password cuando la pida)

Este script:
- ✅ Elimina tablas existentes
- ✅ Crea 19 tablas nuevas
- ✅ Inserta roles, cultivos, usuarios de prueba
```

### 4️⃣ Railway → Agregar Backend (3 min)
```
1. Click "+"
2. "GitHub Repo"
3. Selecciona tu repo
4. Root directory: "/" (raíz)
```

### 5️⃣ Railway → Configurar Backend (5 min)
```
1. Click en el servicio Backend
2. "Settings" → Service Name: "backend-testing"
3. "Variables" → Add Variable → Bulk Import
4. Pega esto (reemplaza los valores):

SPRING_PROFILES_ACTIVE=testing
DATABASE_URL=jdbc:mysql://${{MySQL.MYSQLHOST}}:${{MySQL.MYSQLPORT}}/${{MySQL.MYSQLDATABASE}}?useSSL=false&serverTimezone=UTC
DB_USERNAME=${{MySQL.MYSQLUSER}}
DB_PASSWORD=${{MySQL.MYSQLPASSWORD}}
HIBERNATE_DDL_AUTO=update
JWT_SECRET=testing_secret_CAMBIALO_123456789012345678901234567890
CORS_ALLOWED_ORIGINS=https://agrogestion-testing.vercel.app,http://localhost:3000
PORT=8080

5. Click "Add"
6. Railway empezará a compilar y desplegar
```

### 6️⃣ Railway → Obtener URL del Backend (2 min)
```
1. Espera a que termine el deploy (2-3 min)
2. "Settings" → "Networking"
3. Click "Generate Domain"
4. COPIA LA URL (ej: https://backend-testing-xxxx.up.railway.app)
```

### 7️⃣ Vercel → Crear Proyecto Testing (3 min)
```
1. https://vercel.com/new
2. "Import Git Repository"
3. Selecciona tu repo
4. Configurar:
   - Name: agrogestion-testing
   - Root: agrogestion-frontend
   - Framework: Vite (auto)
```

### 8️⃣ Vercel → Configurar Variables (2 min)
```
1. En "Environment Variables":
   
   VITE_API_URL = [URL de Railway del paso 6]
   
   Ejemplo: https://backend-testing-xxxx.up.railway.app
   
   Environment: Preview + Development
   
2. Click "Deploy"
```

### 9️⃣ Actualizar CORS en Railway (2 min)
```
1. Vuelve a Railway
2. Backend → Variables
3. Edita CORS_ALLOWED_ORIGINS:
   
   https://agrogestion-testing-xxxx.vercel.app,http://localhost:3000
   
   (Usa la URL real de Vercel)
4. Save (se reiniciará automáticamente)
```

### 🔟 Probar Testing (10 min)
```
1. Abre: https://agrogestion-testing.vercel.app
2. Login:
   - Email: admin.testing@agrogestion.com
   - Password: password123
3. Verifica:
   - ✅ Dashboard carga
   - ✅ Puedes ver lotes, campos
   - ✅ Puedes crear una labor
   - ✅ No hay errores CORS (F12)
```

---

## 🚀 Production Environment (20-30 minutos)

### Repite los pasos 1-10 pero con estos cambios:

| Paso | Cambio |
|------|--------|
| 1 | Nombre: "AgroGestion-**Production**" |
| 3 | Usa: `database-migration-master-**production**.sql` |
| 5 | Variables diferentes (JWT único, HIBERNATE=validate) |
| 5 | CORS solo con URL de Vercel Production |
| 7 | Name: `agrogestion` (sin -testing) |
| 8 | Variable con URL de Railway Production |
| 10 | Login: admin@tudominio.com / Admin2025!Temp |
| 10 | **⚠️ CAMBIAR PASSWORD inmediatamente** |

---

## ✅ Checklist Final

### Testing:
- [ ] Railway proyecto creado
- [ ] MySQL migrado con datos de prueba
- [ ] Backend desplegado y corriendo
- [ ] Vercel desplegado
- [ ] Login funciona con usuarios de testing
- [ ] No hay errores CORS
- [ ] Todas las funcionalidades operan

### Production:
- [ ] Railway proyecto creado (SEPARADO de testing)
- [ ] MySQL migrado con datos mínimos
- [ ] Backend desplegado
- [ ] Vercel desplegado
- [ ] Login funciona con admin
- [ ] **Password del admin cambiada**
- [ ] Datos de empresa actualizados
- [ ] Backups configurados
- [ ] Todo funciona perfectamente

---

## 🎁 URLs Finales

Al terminar tendrás:

**Testing:**
- Frontend: `https://agrogestion-testing.vercel.app`
- Backend: `https://backend-testing-xxxx.up.railway.app`

**Production:**
- Frontend: `https://agrogestion.vercel.app`
- Backend: `https://backend-xxxx.up.railway.app`

---

## 📊 Tiempo Total Estimado

| Ambiente | Tiempo |
|----------|--------|
| Testing | 30-45 minutos |
| Production | 20-30 minutos |
| **TOTAL** | **~1 hora** |

---

## 🆘 Si Algo Falla

1. **Revisa los logs** en Railway/Vercel
2. **Consulta** DEPLOYMENT-PASO-A-PASO.md (más detallado)
3. **Verifica** que las URLs y variables estén correctas
4. **Espera** 2-3 minutos (Railway puede tardar en compilar)

---

¿Listo? **¡Empieza con el paso 1! 🚀**

