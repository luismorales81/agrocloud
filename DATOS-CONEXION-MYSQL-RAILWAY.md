# 🔌 DATOS NECESARIOS PARA CONECTARSE A MYSQL EN RAILWAY

Este documento explica qué datos necesitas obtener de Railway para conectarte desde tu máquina local a MySQL y subir la estructura de la base de datos.

---

## 📋 DATOS QUE NECESITAS OBTENER DE RAILWAY

Para conectarte a MySQL en Railway desde tu máquina local, necesitas los siguientes datos:

### 1. **HOST (Servidor)**
- **Qué es:** La dirección del servidor MySQL en Railway
- **Dónde encontrarlo:** En la variable `MYSQLHOST` o en la URL de conexión

### 2. **PORT (Puerto)**
- **Qué es:** El puerto donde MySQL está escuchando
- **Dónde encontrarlo:** En la variable `MYSQLPORT` o en la URL de conexión
- **Valor típico:** `3306` (puerto estándar de MySQL)

### 3. **DATABASE (Base de Datos)**
- **Qué es:** El nombre de la base de datos
- **Dónde encontrarlo:** En la variable `MYSQLDATABASE` o en la URL de conexión
- **Valor típico:** `railway`

### 4. **USERNAME (Usuario)**
- **Qué es:** El nombre de usuario para conectarse
- **Dónde encontrarlo:** En la variable `MYSQLUSER` o en la URL de conexión
- **Valor típico:** `root`

### 5. **PASSWORD (Contraseña)**
- **Qué es:** La contraseña del usuario
- **Dónde encontrarlo:** En la variable `MYSQLPASSWORD` o en la URL de conexión
- **⚠️ IMPORTANTE:** Esta es información sensible, guárdala de forma segura

---

## 🔍 CÓMO OBTENER LOS DATOS EN RAILWAY

### **Método 1: Desde las Variables del Servicio MySQL**

1. Ve a **Railway Dashboard**: https://railway.app
2. Selecciona tu proyecto
3. Haz clic en el servicio **MySQL** (o el nombre de tu servicio de base de datos)
4. Ve a la pestaña **Variables**
5. Busca y copia los siguientes valores:

```
MYSQLHOST = [tu-host-aqui]
MYSQLPORT = [tu-puerto-aqui]
MYSQLDATABASE = [tu-base-de-datos-aqui]
MYSQLUSER = [tu-usuario-aqui]
MYSQLPASSWORD = [tu-contraseña-aqui]
```

### **Método 2: Desde la Variable MYSQL_URL**

Si Railway te proporciona una variable `MYSQL_URL` con formato:
```
mysql://usuario:contraseña@host:puerto/base_de_datos
```

Ejemplo:
```
mysql://root:abc123@containers-us-west-123.railway.app:3306/railway
```

**Extracción de datos:**
- **HOST:** `containers-us-west-123.railway.app`
- **PORT:** `3306`
- **DATABASE:** `railway`
- **USERNAME:** `root`
- **PASSWORD:** `abc123`

---

## 🛠️ CÓMO USAR ESTOS DATOS PARA CONECTARTE

### **Opción 1: MySQL Workbench (Recomendado)**

1. Abre **MySQL Workbench**
2. Haz clic en **"+"** para crear una nueva conexión
3. Completa los campos:

```
Connection Name: Railway MySQL
Hostname: [tu-MYSQLHOST]
Port: [tu-MYSQLPORT]
Username: [tu-MYSQLUSER]
Password: [tu-MYSQLPASSWORD] (haz clic en "Store in Keychain" si quieres guardarla)
Default Schema: [tu-MYSQLDATABASE]
```

4. Haz clic en **"Test Connection"** para verificar
5. Si funciona, haz clic en **"OK"** y luego **"Connect"**

### **Opción 2: Línea de Comandos (MySQL Client)**

Si tienes MySQL instalado localmente, puedes usar:

```bash
mysql -h [HOST] -P [PORT] -u [USERNAME] -p [DATABASE]
```

Ejemplo:
```bash
mysql -h containers-us-west-123.railway.app -P 3306 -u root -p railway
```

Te pedirá la contraseña, ingrésala cuando se solicite.

### **Opción 3: Desde tu Aplicación Spring Boot Local**

Si quieres conectarte desde tu aplicación local, configura en `application.properties`:

```properties
spring.datasource.url=jdbc:mysql://[HOST]:[PORT]/[DATABASE]?useSSL=true&requireSSL=true&verifyServerCertificate=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=[USERNAME]
spring.datasource.password=[PASSWORD]
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
```

---

## 📤 SUBIR LA ESTRUCTURA DE LA BASE DE DATOS

Una vez conectado, puedes subir la estructura de varias formas:

### **Método 1: Desde MySQL Workbench**

1. Conéctate a la base de datos en Railway
2. Ve a **File** → **Open SQL Script**
3. Selecciona tu archivo SQL (ej: `estructura_local_completa.sql`)
4. Haz clic en el botón **⚡ Execute** (o presiona `Ctrl+Shift+Enter`)
5. Espera a que se ejecute completamente

### **Método 2: Desde Línea de Comandos**

```bash
mysql -h [HOST] -P [PORT] -u [USERNAME] -p [DATABASE] < estructura_local_completa.sql
```

Ejemplo:
```bash
mysql -h containers-us-west-123.railway.app -P 3306 -u root -p railway < estructura_local_completa.sql
```

### **Método 3: Usando un Script Batch (Windows)**

Crea un archivo `subir-estructura-railway.bat`:

```batch
@echo off
echo Conectando a MySQL en Railway...
mysql -h [TU_HOST] -P [TU_PUERTO] -u [TU_USUARIO] -p[TU_CONTRASEÑA] [TU_BASE_DE_DATOS] < estructura_local_completa.sql
echo Estructura subida exitosamente!
pause
```

**⚠️ NOTA:** Reemplaza los valores entre corchetes con tus datos reales.

---

## 📝 EJEMPLO COMPLETO - CONFIGURACIÓN ACTUAL

### **Datos Configurados:**

```
HOST (Público) = ballast.proxy.rlwy.net
PORT (Público) = 41199
DATABASE = railway
USERNAME = root
PASSWORD = OxwHQZQdvdAmCNBwEsdhDCmxzHbgJGpy
```

### **Conexión en MySQL Workbench:**

```
Connection Name: Railway MySQL
Hostname: ballast.proxy.rlwy.net
Port: 41199
Username: root
Password: OxwHQZQdvdAmCNBwEsdhDCmxzHbgJGpy
Default Schema: railway
```

### **Comando de Línea de Comandos:**

```bash
mysql -h ballast.proxy.rlwy.net -P 41199 -u root -p railway
```

O para ejecutar un script SQL:

```bash
mysql -h ballast.proxy.rlwy.net -P 41199 -u root -pOxwHQZQdvdAmCNBwEsdhDCmxzHbgJGpy railway < estructura_local_completa.sql
```

### **URL JDBC para Spring Boot:**

```properties
spring.datasource.url=jdbc:mysql://ballast.proxy.rlwy.net:41199/railway?useSSL=true&requireSSL=true&verifyServerCertificate=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=OxwHQZQdvdAmCNBwEsdhDCmxzHbgJGpy
```

---

## ⚠️ CONSIDERACIONES IMPORTANTES

### **1. Seguridad SSL/TLS**
- Railway puede requerir conexiones SSL
- Si tienes problemas, prueba con `useSSL=true` o `useSSL=false` según corresponda
- En MySQL Workbench, ve a **Advanced** y configura SSL según sea necesario

### **2. Firewall y Red**
- Asegúrate de que tu conexión no esté bloqueada por firewall
- Railway puede tener restricciones de IP, verifica en la configuración

### **3. Tamaño de Archivos**
- Si tu archivo SQL es muy grande, puede tardar en ejecutarse
- Considera dividirlo en partes más pequeñas si es necesario

### **4. Backup Antes de Modificar**
- **SIEMPRE** haz un backup antes de modificar la base de datos en producción
- Railway puede tener opciones de backup automático, revísalas

---

## 🆘 SOLUCIÓN DE PROBLEMAS

### **Error: "Can't connect to MySQL server"**
- Verifica que el HOST y PORT sean correctos
- Verifica que el servicio MySQL esté corriendo en Railway
- Verifica tu conexión a internet

### **Error: "Access denied for user"**
- Verifica que el USERNAME y PASSWORD sean correctos
- Verifica que el usuario tenga permisos para acceder desde tu IP

### **Error: "SSL connection required"**
- Agrega `?useSSL=true` a tu URL de conexión
- En MySQL Workbench, habilita SSL en la configuración avanzada

### **Error: "Unknown database"**
- Verifica que el nombre de la base de datos sea correcto
- Asegúrate de que la base de datos exista en Railway

---

## 📞 PRÓXIMOS PASOS

Una vez que tengas los datos y te hayas conectado exitosamente:

1. ✅ Verifica la conexión ejecutando un `SELECT 1;`
2. ✅ Revisa las tablas existentes con `SHOW TABLES;`
3. ✅ Haz un backup de la estructura actual (si hay datos importantes)
4. ✅ Ejecuta tu script SQL para subir la estructura
5. ✅ Verifica que las tablas se crearon correctamente

---

**Fecha:** 2025-01-16
**Versión:** AgroCloud v1.0

