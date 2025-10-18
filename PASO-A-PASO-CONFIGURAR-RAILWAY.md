# 🚨 URGENTE - Configurar Variables en Railway

## ❌ PROBLEMA ACTUAL

El error muestra que la aplicación sigue usando la URL incorrecta:
```
SPRING_DATASOURCE_URL="jdbc:mysql://mysql.railway.internal:3306/railway..."
```

**Esto significa que NO has configurado la variable `SPRING_DATASOURCE_URL` en Railway.**

---

## ✅ SOLUCIÓN PASO A PASO (CON IMÁGENES)

### **PASO 1: Ir a Railway Dashboard**

1. Abre tu navegador
2. Ve a: https://railway.app
3. Inicia sesión con tu cuenta
4. Selecciona tu proyecto

---

### **PASO 2: Obtener la URL de la Base de Datos**

1. En el panel izquierdo, busca el servicio **MySQL** (o el nombre de tu base de datos)
2. Haz clic en él
3. En la parte superior, verás varias pestañas: **Metrics**, **Variables**, **Settings**, etc.
4. Haz clic en la pestaña **Variables**

---

### **PASO 3: Copiar la URL de MySQL**

En la pestaña Variables, verás algo como:

```
MYSQL_URL = mysql://root:password123@containers-us-west-xxx.railway.app:6789/railway
```

**🔴 IMPORTANTE:** Copia TODO el valor de `MYSQL_URL`

---

### **PASO 4: Convertir la URL al Formato JDBC**

Si tu `MYSQL_URL` es:
```
mysql://root:password123@containers-us-west-xxx.railway.app:6789/railway
```

Entonces tu `SPRING_DATASOURCE_URL` debe ser:
```
jdbc:mysql://containers-us-west-xxx.railway.app:6789/railway?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
```

**Nota:** Solo agregué `jdbc:` al principio y los parámetros al final. El resto es igual.

---

### **PASO 5: Ir al Servicio Backend**

1. En el panel izquierdo, busca el servicio **Backend** (tu aplicación Spring Boot)
2. Haz clic en él
3. Ve a la pestaña **Variables**

---

### **PASO 6: Agregar la Variable SPRING_DATASOURCE_URL**

1. Haz clic en el botón **+ New Variable** (o **+ Add Variable**)
2. En el campo **Name**, escribe exactamente:
   ```
   SPRING_DATASOURCE_URL
   ```
3. En el campo **Value**, pega la URL convertida (del PASO 4)
4. Haz clic en **Add** o **Save**

---

### **PASO 7: Verificar que la Variable se Agregó**

En la lista de variables, deberías ver:
```
SPRING_DATASOURCE_URL = jdbc:mysql://containers-us-west-xxx.railway.app:6789/railway?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
```

---

### **PASO 8: Agregar las Otras Variables**

Repite el PASO 6 para agregar estas variables:

#### **Variable 2: SPRING_PROFILES_ACTIVE**
```
Name: SPRING_PROFILES_ACTIVE
Value: railway-mysql
```

#### **Variable 3: SPRING_DATASOURCE_USERNAME**
```
Name: SPRING_DATASOURCE_USERNAME
Value: root
```

#### **Variable 4: SPRING_DATASOURCE_PASSWORD**
```
Name: SPRING_DATASOURCE_PASSWORD
Value: password123
```
(Usa la contraseña de tu `MYSQL_URL`)

#### **Variable 5: JWT_SECRET** (Opcional)
```
Name: JWT_SECRET
Value: railway-jwt-secret-2025
```

---

### **PASO 9: Verificar Todas las Variables**

Tu lista de variables debería verse así:

```
SPRING_PROFILES_ACTIVE = railway-mysql
SPRING_DATASOURCE_URL = jdbc:mysql://containers-us-west-xxx.railway.app:6789/railway?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
SPRING_DATASOURCE_USERNAME = root
SPRING_DATASOURCE_PASSWORD = password123
JWT_SECRET = railway-jwt-secret-2025
```

---

### **PASO 10: Trigger un Nuevo Deployment**

Después de agregar las variables:

1. Railway debería detectar automáticamente los cambios
2. Iniciará un nuevo deployment
3. Si no inicia automáticamente, ve a la pestaña **Deployments**
4. Haz clic en **Deploy** o **Redeploy**

---

### **PASO 11: Monitorear el Deployment**

1. Ve a la pestaña **Deployments**
2. Haz clic en el deployment más reciente
3. Ve a la pestaña **Logs**
4. Busca mensajes como:
   ```
   ✅ Started AgroCloudApplication
   ✅ Connected to database
   ```

---

## 🔍 VERIFICACIÓN

### ✅ **Si Funciona:**
Verás en los logs:
```
Started AgroCloudApplication in X.XXX seconds
Connected to database successfully
Health check: UP
```

### ❌ **Si Sigue Fallando:**
Verás en los logs:
```
Driver com.mysql.cj.jdbc.Driver claims to not accept jdbcUrl
```

**Si esto pasa:**
1. Verifica que copiaste correctamente la URL
2. Verifica que agregaste `jdbc:` al principio
3. Verifica que no hay espacios en la URL
4. Verifica que la variable se llama exactamente `SPRING_DATASOURCE_URL`

---

## 🆘 TROUBLESHOOTING

### **Problema 1: No veo la variable MYSQL_URL**

**Solución:**
- Asegúrate de estar en el servicio MySQL (no en el Backend)
- Verifica que el servicio MySQL esté corriendo

### **Problema 2: La URL tiene caracteres especiales**

**Solución:**
- Si la contraseña tiene caracteres especiales, pueden necesitar encoding
- Ejemplo: `@` se convierte en `%40`, `#` se convierte en `%23`

### **Problema 3: Railway no inicia el deployment**

**Solución:**
- Haz clic en la pestaña **Deployments**
- Haz clic en **Redeploy** o **Deploy**

### **Problema 4: Sigue usando la URL incorrecta**

**Solución:**
1. Verifica que guardaste la variable
2. Verifica que no hay otra variable con el mismo nombre
3. Elimina la variable antigua y crea una nueva
4. Haz un redeploy

---

## 📝 EJEMPLO COMPLETO

### **Caso Real:**

**MYSQL_URL en Railway:**
```
mysql://root:abc123@containers-us-west-123.railway.app:5432/railway
```

**Variables a configurar en el Backend:**

```
SPRING_PROFILES_ACTIVE = railway-mysql

SPRING_DATASOURCE_URL = jdbc:mysql://containers-us-west-123.railway.app:5432/railway?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true

SPRING_DATASOURCE_USERNAME = root

SPRING_DATASOURCE_PASSWORD = abc123

JWT_SECRET = railway-jwt-secret-2025
```

---

## ⚠️ IMPORTANTE

- **NO uses `mysql.railway.internal`** - Es una URL interna que NO funciona
- **USA la URL pública** que Railway te proporciona en `MYSQL_URL`
- **Agrega `jdbc:`** al principio de la URL
- **Agrega los parámetros** al final: `?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true`

---

## 📞 ¿NECESITAS AYUDA?

Si después de seguir estos pasos sigue fallando:

1. Copia tu `MYSQL_URL` completa
2. Copia tu `SPRING_DATASOURCE_URL` completa
3. Compártelas (sin la contraseña) para que pueda ayudarte

---

**Fecha:** 2025-01-14
**Versión:** AgroCloud v1.0

