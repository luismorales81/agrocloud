# 🚀 MIGRAR DATOS LOCAL A RAILWAY

## 📋 INFORMACIÓN DE TU RAILWAY

**Host:** `gondola.proxy.rlwy.net`  
**Puerto:** `54893`  
**Usuario:** `root`  
**Contraseña:** `WSoobrppUQbaPINdsRcoQVkUvtYKjmSe`  
**Base de datos:** `railway`

---

## 🎯 OPCIONES DE MIGRACIÓN

### **OPCIÓN 1: Script Automático (Recomendado)**

Ejecuta el script `migrar-datos-railway-simple.bat`:

```bash
migrar-datos-railway-simple.bat
```

Este script:
1. ✅ Exporta los datos de tu base de datos local
2. ✅ Los importa automáticamente a Railway
3. ✅ Guarda un backup por si necesitas revertir

---

### **OPCIÓN 2: Script Interactivo**

Ejecuta el script `migrar-datos-local-a-railway.bat`:

```bash
migrar-datos-local-a-railway.bat
```

Este script te pedirá los datos de Railway interactivamente.

---

### **OPCIÓN 3: Manual (Línea de Comandos)**

#### **Paso 1: Exportar datos locales**

```bash
mysqldump -u root -p123456 agrocloud > backup-agrocloud.sql
```

#### **Paso 2: Importar a Railway**

```bash
mysql -h gondola.proxy.rlwy.net -P 54893 -u root -pWSoobrppUQbaPINdsRcoQVkUvtYKjmSe railway < backup-agrocloud.sql
```

---

## ⚠️ ADVERTENCIAS

### **⚠️ ANTES DE MIGRAR:**

1. **Backup de Railway:** Si ya tienes datos en Railway, haz un backup primero
2. **Verificar estructura:** Asegúrate de que las tablas existan en Railway
3. **Datos de prueba:** Considera migrar solo datos de prueba primero

---

### **⚠️ PROBLEMAS COMUNES:**

#### **Error: "Access denied"**
- Verifica que las credenciales sean correctas
- Verifica que el usuario tenga permisos de escritura

#### **Error: "Unknown database"**
- Verifica que la base de datos `railway` exista en Railway
- Verifica que el nombre de la base de datos sea correcto

#### **Error: "Can't connect to MySQL server"**
- Verifica que el host y puerto sean correctos
- Verifica que Railway MySQL esté corriendo
- Verifica que tu firewall permita la conexión

---

## 📊 QUÉ DATOS SE MIGRARÁN

El script migrará **TODOS** los datos de tu base de datos local:

- ✅ Usuarios y roles
- ✅ Empresas
- ✅ Campos y lotes
- ✅ Cultivos y cosechas
- ✅ Insumos y maquinaria
- ✅ Labores
- ✅ Ingresos y egresos
- ✅ Balance
- ✅ Inventario de granos
- ✅ Todo lo demás

---

## 🔄 DESPUÉS DE LA MIGRACIÓN

### **Paso 1: Verificar la migración**

Conéctate a Railway MySQL y verifica:

```bash
mysql -h gondola.proxy.rlwy.net -P 54893 -u root -pWSoobrppUQbaPINdsRcoQVkUvtYKjmSe railway

# Dentro de MySQL:
SHOW TABLES;
SELECT COUNT(*) FROM users;
SELECT COUNT(*) FROM empresas;
# etc...
```

### **Paso 2: Probar la aplicación**

1. Ve a tu aplicación en Railway
2. Intenta iniciar sesión con un usuario migrado
3. Verifica que los datos se muestren correctamente

### **Paso 3: Limpiar datos de prueba (Opcional)**

Si migraste datos de prueba que no necesitas:

```sql
-- Ejemplo: Eliminar usuarios de prueba
DELETE FROM users WHERE email LIKE '%test%';
DELETE FROM users WHERE email LIKE '%prueba%';
```

---

## 🆘 TROUBLESHOOTING

### **Problema 1: El script no encuentra mysqldump**

**Solución:**
- Asegúrate de que MySQL esté instalado
- Agrega MySQL al PATH de Windows
- Usa la ruta completa a mysqldump

### **Problema 2: Timeout al conectar a Railway**

**Solución:**
- Verifica tu conexión a Internet
- Intenta de nuevo (puede ser temporal)
- Verifica que Railway MySQL esté corriendo

### **Problema 3: Datos duplicados**

**Solución:**
- Si ya tienes datos en Railway, elimínalos primero
- O usa `INSERT IGNORE` en lugar de `INSERT`
- O modifica el script para hacer `TRUNCATE` antes de importar

---

## 📝 EJEMPLO COMPLETO

### **Escenario:**

Tienes datos en tu base de datos local `agrocloud` y quieres migrarlos a Railway.

### **Paso 1: Ejecutar el script**

```bash
migrar-datos-railway-simple.bat
```

### **Paso 2: Verificar**

```bash
mysql -h gondola.proxy.rlwy.net -P 54893 -u root -pWSoobrppUQbaPINdsRcoQVkUvtYKjmSe railway -e "SELECT COUNT(*) FROM users;"
```

### **Paso 3: Probar la aplicación**

1. Ve a tu aplicación en Railway
2. Inicia sesión con un usuario migrado
3. Verifica que todo funcione correctamente

---

## 🎯 RESUMEN

**Script recomendado:** `migrar-datos-railway-simple.bat`

**Comando manual:**
```bash
mysqldump -u root -p123456 agrocloud > backup.sql
mysql -h gondola.proxy.rlwy.net -P 54893 -u root -pWSoobrppUQbaPINdsRcoQVkUvtYKjmSe railway < backup.sql
```

---

**Fecha:** 2025-01-14  
**Versión:** AgroCloud v1.0

