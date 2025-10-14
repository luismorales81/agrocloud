# ✅ Checklist de Deployment: Testing y Producción

## 📋 Pre-Deployment

### Código
- [ ] Todos los cambios están commiteados
- [ ] Tests locales pasan correctamente
- [ ] Build del backend funciona: `mvn clean package -DskipTests`
- [ ] Build del frontend funciona: `npm run build`
- [ ] No hay secretos/passwords en el código
- [ ] Archivo `.gitignore` actualizado

### Base de Datos
- [ ] Scripts de migración preparados
- [ ] Datos iniciales/seed preparados (testing)
- [ ] Backup de datos actual (si aplica)

---

## 🚂 Railway - Testing

### Proyecto y MySQL
- [ ] Proyecto "AgroGestion-Testing" creado
- [ ] MySQL database agregada
- [ ] Variables de MySQL copiadas (MYSQLHOST, MYSQLUSER, etc.)

### Backend Service
- [ ] Repositorio conectado a Railway
- [ ] Variables de entorno configuradas (ver VARIABLES-ENTORNO-RAILWAY.md)
- [ ] Build command configurado: `cd agrogestion-backend && mvn clean package -DskipTests`
- [ ] Start command configurado: `cd agrogestion-backend && java -jar target/agrocloud-backend-1.0.0.jar`
- [ ] Deploy iniciado
- [ ] Deploy completado exitosamente

### Verificación Testing
- [ ] Health check responde: `curl https://[tu-url].up.railway.app/actuator/health`
- [ ] Logs no muestran errores críticos
- [ ] Base de datos conecta correctamente

---

## 🚂 Railway - Production

### Proyecto y MySQL
- [ ] Proyecto "AgroGestion-Production" creado
- [ ] MySQL database agregada (NUEVA, no la misma de testing)
- [ ] Variables de MySQL copiadas
- [ ] **Backup automático habilitado en MySQL**

### Backend Service
- [ ] Repositorio conectado
- [ ] Variables de entorno configuradas
- [ ] **JWT_SECRET único y diferente a testing**
- [ ] **HIBERNATE_DDL_AUTO=validate** (no update)
- [ ] Build y start commands configurados
- [ ] Deploy iniciado
- [ ] Deploy completado exitosamente

### Verificación Production
- [ ] Health check responde
- [ ] Logs limpios sin errores
- [ ] Base de datos conecta
- [ ] SSL/HTTPS funcionando

---

## 🎨 Vercel - Testing

### Proyecto
- [ ] Proyecto "agrogestion-testing" creado
- [ ] Repositorio importado
- [ ] Root directory: `agrogestion-frontend`
- [ ] Framework: Vite detectado
- [ ] Build command: `npm run build`
- [ ] Output directory: `dist`

### Variables de Entorno
- [ ] `VITE_API_URL` = `https://[railway-testing-url].up.railway.app`
- [ ] Variable aplicada a: **Preview** y **Development**

### Deployment
- [ ] Deploy completado
- [ ] URL testing funciona
- [ ] Login funciona
- [ ] No hay errores CORS en consola
- [ ] Todas las páginas cargan correctamente

---

## 🎨 Vercel - Production

### Proyecto
- [ ] Proyecto "agrogestion" (o "agrogestion-production") creado
- [ ] Repositorio importado
- [ ] Configuración igual a testing

### Variables de Entorno
- [ ] `VITE_API_URL` = `https://[railway-production-url].up.railway.app`
- [ ] Variable aplicada a: **Production**

### Deployment
- [ ] Deploy completado
- [ ] URL production funciona
- [ ] Login funciona
- [ ] No errores CORS
- [ ] Performance aceptable

---

## 🔄 Configuración de Ramas (Git Flow)

### Git Branches
- [ ] Rama `develop` creada para testing
- [ ] Rama `main` (o `master`) para production
- [ ] `.gitignore` incluye archivos sensibles

### Railway Auto-Deploy
- [ ] Testing: Auto-deploy desde `develop` ✅
- [ ] Production: Auto-deploy desde `main` ✅ (o manual)

### Vercel Auto-Deploy
- [ ] Testing: Production branch = `develop`
- [ ] Production: Production branch = `main`

---

## 🔐 Seguridad

### Secrets
- [ ] JWT_SECRET diferente en testing y production
- [ ] JWT_SECRET de production tiene 64+ caracteres
- [ ] Passwords de BD son seguros
- [ ] Variables sensibles NO están en el código

### CORS
- [ ] Testing: Solo URLs de testing permitidas
- [ ] Production: Solo URLs de production permitidas
- [ ] No se permite `*` en production

### SSL/HTTPS
- [ ] Vercel tiene HTTPS automático ✅
- [ ] Railway tiene HTTPS automático ✅
- [ ] Conexión BD usa SSL en production

---

## 📊 Migración de Datos

### Testing
- [ ] Base de datos inicializada
- [ ] Usuarios de prueba creados
- [ ] Datos de ejemplo cargados
- [ ] Roles configurados correctamente

### Production
- [ ] Backup de datos existente (si migras)
- [ ] Scripts de migración ejecutados
- [ ] Datos verificados
- [ ] Usuarios admin creados
- [ ] Roles y permisos verificados

---

## 🧪 Testing Post-Deployment

### Testing Environment
- [ ] Login con usuario de prueba funciona
- [ ] Crear lote funciona
- [ ] Crear insumo funciona
- [ ] Crear labor funciona
- [ ] Reportes cargan datos
- [ ] Consultor Externo tiene permisos correctos (solo lectura)
- [ ] No hay errores en logs de Railway
- [ ] No hay errores en consola del navegador

### Production Environment
- [ ] Login con usuario real funciona
- [ ] Todas las funcionalidades principales testeadas
- [ ] Performance es aceptable (<2s carga inicial)
- [ ] Mobile responsive funciona
- [ ] Datos se persisten correctamente
- [ ] Roles y permisos funcionan como esperado

---

## 📝 Documentación

### URLs Documentadas
- [ ] Testing Frontend: `https://agrogestion-testing.vercel.app`
- [ ] Testing Backend: `https://[tu-url].up.railway.app`
- [ ] Production Frontend: `https://agrogestion.vercel.app`
- [ ] Production Backend: `https://[tu-url].up.railway.app`

### Credenciales Guardadas (en lugar seguro)
- [ ] Admin testing
- [ ] Admin production
- [ ] Credenciales Railway
- [ ] Credenciales Vercel

### Documentos Actualizados
- [ ] README con URLs de ambos ambientes
- [ ] Variables de entorno documentadas
- [ ] Proceso de deployment documentado

---

## 🎯 Dominios Personalizados (Opcional)

### Vercel
- [ ] Testing: `testing.agrogestion.com` configurado
- [ ] Production: `agrogestion.com` configurado
- [ ] DNS configurado correctamente
- [ ] SSL certificado válido

### Railway
- [ ] Testing: `api-testing.agrogestion.com` configurado
- [ ] Production: `api.agrogestion.com` configurado
- [ ] DNS configurado
- [ ] CORS actualizado con nuevos dominios

---

## 📈 Monitoreo (Post-Deployment)

### Railway
- [ ] Logs monitoreados por 24 horas
- [ ] No hay crashes/restarts inesperados
- [ ] Uso de recursos es normal (<80% CPU/RAM)

### Vercel
- [ ] Analytics configurado
- [ ] Errores monitoreados
- [ ] Performance dentro de límites aceptables

### Alertas
- [ ] Configurar notificaciones si el servicio cae
- [ ] Monitorear logs de errores
- [ ] Configurar uptime monitoring (opcional)

---

## 🎉 Checklist Final

- [ ] **Testing está 100% funcional**
- [ ] **Production está 100% funcional**
- [ ] **Usuarios pueden acceder a ambos ambientes**
- [ ] **No hay errores críticos en logs**
- [ ] **Backup de BD production configurado**
- [ ] **Documentación actualizada**
- [ ] **Equipo notificado de las nuevas URLs**
- [ ] **Plan de rollback definido (por si algo falla)**

---

## 🆘 Contactos de Soporte

- **Railway:** https://railway.app/help
- **Vercel:** https://vercel.com/support
- **Documentación:** Ver archivos MD en el proyecto

---

## 📅 Próximas Tareas Post-Deployment

- [ ] Configurar monitoreo continuo
- [ ] Configurar backups automáticos (semanal/diario)
- [ ] Documentar proceso de actualización
- [ ] Crear runbook para incidentes
- [ ] Capacitar equipo en proceso de deployment

---

**Fecha de Deployment:**  
**Testing:** __/__/____  
**Production:** __/__/____  
**Responsable:** _________________

**¡Deployment Exitoso! 🎉**

