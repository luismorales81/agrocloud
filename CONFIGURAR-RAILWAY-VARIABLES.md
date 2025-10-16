# 🔧 CONFIGURACIÓN DE VARIABLES EN RAILWAY

## ⚠️ PASO CRÍTICO - Configurar URL de Base de Datos

Este es el paso **MÁS IMPORTANTE** para que la aplicación funcione en Railway.

---

## 📋 PASO A PASO

### 1️⃣ **Obtener la URL de la Base de Datos**

1. Ve a **Railway Dashboard**: https://railway.app
2. Selecciona tu proyecto
3. Haz clic en el servicio **MySQL** (o el nombre de tu base de datos)
4. Ve a la pestaña **Variables**
5. Busca la variable `MYSQL_URL`
6. Copia su valor (ejemplo: `mysql://root:password@containers-us-west-xxx.railway.app:xxxx/railway`)

---

### 2️⃣ **Configurar Variables en el Servicio Backend**

1. En Railway Dashboard, selecciona el servicio **Backend** (tu aplicación Spring Boot)
2. Ve a la pestaña **Variables**
3. Haz clic en **+ New Variable**

---

### 3️⃣ **Agregar las Variables Requeridas**

Agrega las siguientes variables (una por una):

#### **Variable 1: SPRING_PROFILES_ACTIVE**
```
Nombre: SPRING_PROFILES_ACTIVE
Valor: railway-mysql
```

#### **Variable 2: SPRING_DATASOURCE_URL** 🔴 CRÍTICO
```
Nombre: SPRING_DATASOURCE_URL
Valor: jdbc:mysql://[HOST]:[PORT]/[DATABASE]?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
```

**⚠️ IMPORTANTE:** Reemplaza `[HOST]`, `[PORT]` y `[DATABASE]` con los valores de tu `MYSQL_URL`.

**Ejemplo completo:**
```
jdbc:mysql://containers-us-west-xxx.railway.app:xxxx/railway?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
```

#### **Variable 3: SPRING_DATASOURCE_USERNAME**
```
Nombre: SPRING_DATASOURCE_USERNAME
Valor: root
```
(O el usuario que configuraste en MySQL)

#### **Variable 4: SPRING_DATASOURCE_PASSWORD**
```
Nombre: SPRING_DATASOURCE_PASSWORD
Valor: [TU_PASSWORD]
```
(O la contraseña que configuraste en MySQL)

#### **Variable 5: JWT_SECRET** (Opcional pero recomendado)
```
Nombre: JWT_SECRET
Valor: railway-jwt-secret-2025
```
(O cualquier string seguro que quieras usar)

---

## 🔍 CÓMO EXTRAER LOS VALORES DE MYSQL_URL

Si tu `MYSQL_URL` es:
```
mysql://root:password123@containers-us-west-xxx.railway.app:6789/railway
```

Entonces:
- **HOST:** `containers-us-west-xxx.railway.app`
- **PORT:** `6789`
- **DATABASE:** `railway`
- **USERNAME:** `root`
- **PASSWORD:** `password123`

Y tu `SPRING_DATASOURCE_URL` debería ser:
```
jdbc:mysql://containers-us-west-xxx.railway.app:6789/railway?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
```

---

## ✅ VERIFICACIÓN

Después de configurar las variables:

1. Ve a la pestaña **Deployments** en Railway
2. Haz clic en el deployment más reciente
3. Ve a la pestaña **Logs**
4. Busca mensajes como:
   ```
   ✅ Started AgroCloudApplication
   ✅ Connected to database
   ```

Si ves errores como:
```
❌ Driver com.mysql.cj.jdbc.Driver claims to not accept jdbcUrl
```

Significa que la URL no está correctamente configurada. Verifica que:
- La URL use `jdbc:mysql://` (no solo `mysql://`)
- La URL incluya el puerto correcto
- La URL incluya el nombre de la base de datos

---

## 🚀 DESPUÉS DE CONFIGURAR

Una vez que hayas configurado las variables:

1. Railway detectará automáticamente los cambios
2. Iniciará un nuevo deployment
3. El health check debería pasar exitosamente
4. La aplicación estará disponible

---

## 📞 SOPORTE

Si tienes problemas:

1. Verifica los logs en Railway
2. Confirma que todas las variables están configuradas
3. Asegúrate de que el servicio MySQL esté corriendo
4. Verifica que la URL sea correcta (sin espacios, con el formato correcto)

---

**Fecha:** 2025-01-14
**Versión:** AgroCloud v1.0

