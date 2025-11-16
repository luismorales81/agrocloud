# 🔓 CÓMO OBTENER LA URL PÚBLICA DE MYSQL EN RAILWAY

## ⚠️ PROBLEMA ACTUAL

Tienes la URL **INTERNA** de MySQL:
```
mysql-bjw_.railway.internal
```

Esta URL **SOLO funciona dentro de la red de Railway**. Para conectarte desde tu máquina local, necesitas la **URL PÚBLICA**.

---

## 🔍 CÓMO OBTENER LA URL PÚBLICA

### **Método 1: Buscar la Variable MYSQL_URL**

1. Ve a **Railway Dashboard**: https://railway.app
2. Selecciona tu proyecto
3. Haz clic en el servicio **MySQL**
4. Ve a la pestaña **Variables**
5. Busca la variable **`MYSQL_URL`** o **`DATABASE_URL`**

Esta variable debería tener un formato como:
```
mysql://root:password@containers-us-west-xxx.railway.app:3306/railway
```

**El HOST público es la parte después de `@` y antes de `:`**

Ejemplo:
- Si `MYSQL_URL` es: `mysql://root:password@containers-us-west-123.railway.app:3306/railway`
- Entonces el **HOST público** es: `containers-us-west-123.railway.app`

---

### **Método 2: Buscar en la Pestaña "Connect" o "Public Networking"**

1. En el servicio MySQL de Railway
2. Busca una pestaña llamada **"Connect"**, **"Public Networking"** o **"Networking"**
3. Ahí deberías ver la URL pública de conexión

---

### **Método 3: Verificar en la Configuración del Servicio**

1. En el servicio MySQL
2. Ve a la pestaña **Settings** o **Config**
3. Busca opciones relacionadas con **"Public Access"** o **"External Access"**
4. Railway puede mostrar la URL pública ahí

---

## 📝 ACTUALIZAR TUS SCRIPTS CON LA URL PÚBLICA

Una vez que tengas la URL pública, actualiza los scripts:

### **En `conectar-mysql-railway.bat`:**

```batch
set HOST=containers-us-west-123.railway.app
```

### **En `subir-estructura-railway.bat`:**

```batch
set HOST=containers-us-west-123.railway.app
```

### **En `MIS-DATOS-RAILWAY.txt`:**

Actualiza el HOST con la URL pública.

---

## 🔄 ALTERNATIVA: Usar Railway CLI

Si tienes Railway CLI instalado, puedes obtener la URL con:

```bash
railway variables
```

O específicamente:

```bash
railway variables --service mysql
```

---

## ⚠️ SI NO ENCUENTRAS LA URL PÚBLICA

Si Railway no proporciona una URL pública, puede ser que:

1. **El servicio MySQL no tenga acceso público habilitado**
   - Ve a Settings del servicio MySQL
   - Busca opciones de "Public Access" o "External Access"
   - Habilítala si está disponible

2. **Railway requiere usar un túnel o proxy**
   - Algunos servicios de Railway requieren usar Railway CLI para crear un túnel
   - Ejemplo: `railway connect mysql`

3. **Usar Railway CLI para conectarse**
   ```bash
   railway connect mysql
   ```
   Esto creará un túnel local que puedes usar para conectarte.

---

## ✅ VERIFICAR QUE FUNCIONA

Una vez que tengas la URL pública, prueba la conexión:

```bash
mysql -h [HOST_PUBLICO] -P 3306 -u root -p railway
```

Si funciona, verás el prompt de MySQL. Si no funciona, verifica:
- Que la URL sea correcta
- Que no haya problemas de firewall
- Que el servicio MySQL esté corriendo en Railway

---

## 📞 DATOS ACTUALES CONFIGURADOS

Con los datos que proporcionaste:

```
HOST: mysql-bjw_.railway.internal (INTERNA - necesita cambiar)
PORT: 3306
DATABASE: railway
USERNAME: root
PASSWORD: OxwHQZQdvdAmCNBwEsdhDCmxzHbgJGpy
```

**Solo necesitas cambiar el HOST por la URL pública** una vez que la obtengas de Railway.

---

**Fecha:** 2025-01-16
**Versión:** AgroCloud v1.0

