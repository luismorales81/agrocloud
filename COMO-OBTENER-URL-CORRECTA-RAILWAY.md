# 🔍 CÓMO OBTENER LA URL CORRECTA DE RAILWAY

## ❌ PROBLEMA ACTUAL

Estás usando:
```
mysql.railway.internal
```

Esta es una URL **INTERNA** que **NO funciona** en contenedores Docker.

---

## ✅ SOLUCIÓN

Necesitas usar la URL **PÚBLICA** de Railway.

---

## 📋 PASO A PASO

### **OPCIÓN 1: Usar la Variable MYSQL_URL de Railway**

1. Ve a Railway Dashboard
2. Selecciona el servicio **MySQL**
3. Ve a la pestaña **Variables**
4. Busca la variable **`MYSQL_URL`**

**La URL se verá así:**
```
mysql://root:password@containers-us-west-xxx.railway.app:6789/railway
```

**NOTA:** Debe tener `containers-us-west-xxx.railway.app` (NO `mysql.railway.internal`)

---

### **OPCIÓN 2: Si NO ves MYSQL_URL**

Si no ves la variable `MYSQL_URL` en Railway:

1. Ve a la pestaña **Settings** del servicio MySQL
2. Busca la sección **Connect**
3. Verás información de conexión como:
   - **Host:** `containers-us-west-xxx.railway.app`
   - **Port:** `6789`
   - **Database:** `railway`
   - **User:** `root`
   - **Password:** `tu_password`

---

### **OPCIÓN 3: Usar la Variable MYSQL_URL_PUBLIC**

Algunas veces Railway proporciona una variable `MYSQL_URL_PUBLIC`:

1. Ve a Railway Dashboard
2. Selecciona el servicio **MySQL**
3. Ve a la pestaña **Variables**
4. Busca la variable **`MYSQL_URL_PUBLIC`**

---

## 🔧 CONVERTIR LA URL

Una vez que tengas la URL de Railway, conviértela así:

### **Ejemplo:**

**MYSQL_URL en Railway:**
```
mysql://root:WSoobrppUQbaPINdsRcoQVkUvtYKjmSe@containers-us-west-123.railway.app:5432/railway
```

**SPRING_DATASOURCE_URL:**
```
jdbc:mysql://containers-us-west-123.railway.app:5432/railway?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
```

**Variables separadas:**
```
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=WSoobrppUQbaPINdsRcoQVkUvtYKjmSe
```

---

## ⚠️ IMPORTANTE

### **URLs que NO funcionan:**
- ❌ `mysql.railway.internal` - URL interna
- ❌ `localhost` - No funciona en contenedores
- ❌ `127.0.0.1` - No funciona en contenedores

### **URLs que SÍ funcionan:**
- ✅ `containers-us-west-xxx.railway.app` - URL pública
- ✅ `containers-us-east-xxx.railway.app` - URL pública
- ✅ Cualquier URL que termine en `.railway.app`

---

## 🆘 SI NO ENCUENTRAS LA URL PÚBLICA

### **Opción 1: Usar la Variable MYSQL_URL de Railway**

Railway proporciona automáticamente una variable `MYSQL_URL` cuando creas un servicio MySQL.

1. Ve a Railway Dashboard
2. Selecciona el servicio **MySQL**
3. Ve a la pestaña **Variables**
4. Si NO ves `MYSQL_URL`, haz clic en **+ New Variable**
5. Railway debería sugerirte agregar `MYSQL_URL`

### **Opción 2: Usar la Variable MYSQL_URL_PUBLIC**

1. Ve a Railway Dashboard
2. Selecciona el servicio **MySQL**
3. Ve a la pestaña **Variables**
4. Busca `MYSQL_URL_PUBLIC`
5. Si no existe, crea una variable con ese nombre

### **Opción 3: Contactar a Railway**

Si ninguna de las opciones anteriores funciona, contacta al soporte de Railway o revisa su documentación.

---

## 📝 EJEMPLO COMPLETO

### **Escenario:**

Tienes un servicio MySQL en Railway y necesitas conectarte desde tu aplicación Spring Boot.

### **Paso 1: Obtener la URL**

En Railway, ve al servicio MySQL → Variables:
```
MYSQL_URL=mysql://root:abc123@containers-us-west-123.railway.app:5432/railway
```

### **Paso 2: Extraer los Componentes**

- **Host:** `containers-us-west-123.railway.app`
- **Port:** `5432`
- **Database:** `railway`
- **Username:** `root`
- **Password:** `abc123`

### **Paso 3: Configurar las Variables en el Backend**

En Railway, ve al servicio Backend → Variables:

```
SPRING_PROFILES_ACTIVE=railway-mysql

SPRING_DATASOURCE_URL=jdbc:mysql://containers-us-west-123.railway.app:5432/railway?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true

SPRING_DATASOURCE_USERNAME=root

SPRING_DATASOURCE_PASSWORD=abc123

JWT_SECRET=railway-jwt-secret-2025
```

---

## 🎯 VERIFICACIÓN

Después de configurar las variables:

1. Haz un redeploy en Railway
2. Ve a los logs del deployment
3. Busca mensajes como:
   ```
   ✅ Connected to database
   ✅ Started AgroCloudApplication
   ```

Si ves errores como:
```
❌ Driver com.mysql.cj.jdbc.Driver claims to not accept jdbcUrl
```

Significa que la URL todavía es incorrecta. Verifica que:
- La URL use `containers-us-west-xxx.railway.app` (NO `mysql.railway.internal`)
- La URL NO tenga `mysql://` duplicado
- La URL NO incluya las credenciales

---

## 📞 AYUDA ADICIONAL

Si después de seguir estos pasos sigue fallando:

1. Comparte tu `MYSQL_URL` de Railway (sin la contraseña)
2. Comparte tu `SPRING_DATASOURCE_URL` configurada
3. Comparte los logs del error

---

**Fecha:** 2025-01-14
**Versión:** AgroCloud v1.0

