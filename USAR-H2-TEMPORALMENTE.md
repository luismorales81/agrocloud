# 🚀 USAR H2 DATABASE TEMPORALMENTE EN RAILWAY

## ⚠️ SOLUCIÓN TEMPORAL

Si no puedes obtener la URL pública de MySQL en Railway, puedes usar H2 (base de datos en memoria) temporalmente para probar el deployment.

---

## 📋 PASO A PASO

### **PASO 1: Modificar el Dockerfile**

Edita el archivo `agrogestion-backend/Dockerfile.simple`:

```dockerfile
# ANTES:
ENV SPRING_PROFILES_ACTIVE=railway-mysql

# DESPUÉS:
ENV SPRING_PROFILES_ACTIVE=railway-h2
```

---

### **PASO 2: Verificar que Existe el Perfil H2**

El archivo `application-railway-h2.properties` ya existe en tu proyecto.

---

### **PASO 3: Configurar Variables en Railway**

En Railway → Servicio Backend → Variables:

```
SPRING_PROFILES_ACTIVE=railway-h2
```

**NO necesitas configurar variables de base de datos** porque H2 es en memoria.

---

### **PASO 4: Hacer Commit y Push**

```bash
git add agrogestion-backend/Dockerfile.simple
git commit -m "fix: Usar H2 temporalmente para probar deployment en Railway"
git push origin main
```

---

### **PASO 5: Monitorear el Deployment**

1. Railway detectará el push
2. Iniciará un nuevo deployment
3. La aplicación debería iniciar correctamente
4. El health check debería pasar

---

## ⚠️ LIMITACIONES DE H2

- **Datos temporales:** Los datos se pierden al reiniciar la aplicación
- **No es para producción:** Solo para pruebas
- **Rendimiento:** No es tan rápido como MySQL

---

## ✅ VENTAJAS DE H2

- **Funciona inmediatamente:** No requiere configuración de base de datos
- **Perfecto para pruebas:** Ideal para verificar que el deployment funciona
- **Sin problemas de conectividad:** No depende de URLs públicas

---

## 🔄 VOLVER A MYSQL

Una vez que confirmes que el deployment funciona con H2:

1. Obtén la URL pública de MySQL en Railway
2. Configura las variables correctamente
3. Cambia el perfil de vuelta a `railway-mysql`
4. Haz commit y push

---

## 📝 NOTAS

- Esta es una **solución temporal** para probar el deployment
- **NO uses H2 en producción**
- Una vez que el deployment funcione, vuelve a MySQL

---

**Fecha:** 2025-01-14
**Versión:** AgroCloud v1.0

