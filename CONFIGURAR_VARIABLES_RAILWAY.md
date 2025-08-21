x|# 🔧 Configurar Variables de Entorno en Railway

## 📋 Variables Requeridas

Configura estas variables exactas en tu proyecto de Railway:

### **1. Variables de Base de Datos**

```bash
DATABASE_URL=mysql://root:WSoobrppUQbaPINdsRcoQVkUvtYKjmSe@mysql.railway.internal:3306/railway
DB_USERNAME=root
DB_PASSWORD=WSoobrppUQbaPINdsRcoQVkUvtYKjmSe
```

### **2. Variable de Seguridad**

```bash
JWT_SECRET=agrogestionSecretKey2024ForJWTTokenGenerationAndValidationSecureAndLongEnough
```

## 🚀 Pasos para Configurar

### **Paso 1: Ir a Railway Dashboard**
1. Ve a [railway.app](https://railway.app)
2. Selecciona tu proyecto
3. Ve a la pestaña "Variables"

### **Paso 2: Agregar Variables**
1. Haz clic en "New Variable"
2. Agrega cada variable una por una:

| Variable | Valor |
|----------|-------|
| `DATABASE_URL` | `mysql://root:WSoobrppUQbaPINdsRcoQVkUvtYKjmSe@mysql.railway.internal:3306/railway` |
| `DB_USERNAME` | `root` |
| `DB_PASSWORD` | `WSoobrppUQbaPINdsRcoQVkUvtYKjmSe` |
| `JWT_SECRET` | `agrogestionSecretKey2024ForJWTTokenGenerationAndValidationSecureAndLongEnough` |

### **Paso 3: Verificar Configuración**
1. Asegúrate de que todas las variables estén guardadas
2. Verifica que no haya espacios extra
3. Confirma que los valores sean exactos

## 🔍 Verificación

### **Verificar en Logs**
Después de configurar las variables, deberías ver en los logs:

```bash
# ✅ Conexión exitosa a MySQL
HikariPool-1 - Added connection conn0: url=mysql://root:****@mysql.railway.internal:3306/railway

# ❌ Error si las variables no están configuradas
Driver com.mysql.cj.jdbc.Driver claims to not accept jdbcUrl, ${DATABASE_URL}
```

### **Verificar Variables**
En Railway Dashboard, verifica que:
- ✅ Todas las variables estén presentes
- ✅ Los valores sean correctos
- ✅ No haya espacios o caracteres extra

## 🚨 Solución de Problemas

### **Error: Variables no resueltas**
Si ves `${DATABASE_URL}` en lugar del valor real:
1. Verifica que las variables estén configuradas en Railway
2. Asegúrate de que los nombres sean exactos (mayúsculas/minúsculas)
3. Reinicia el despliegue después de configurar las variables

### **Error: Conexión rechazada**
Si la conexión a MySQL falla:
1. Verifica que el servicio MySQL esté activo en Railway
2. Confirma que las credenciales sean correctas
3. Verifica que la URL de conexión sea válida

## 📞 Soporte

Si tienes problemas:
1. Verifica los logs de Railway
2. Confirma la configuración de variables
3. Revisa que el servicio MySQL esté funcionando
