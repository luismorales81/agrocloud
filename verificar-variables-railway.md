# 🔧 Verificación de Variables de Entorno en Railway

## Variables Mínimas Requeridas

Para que la aplicación funcione en Railway, necesitas configurar estas variables:

### Opción 1: Con MySQL (Recomendado)
```bash
DATABASE_URL=jdbc:mysql://tu-host-mysql:3306/tu-base-datos?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
DB_USERNAME=tu-usuario-mysql
DB_PASSWORD=tu-contraseña-mysql
JWT_SECRET=tu-clave-secreta-muy-larga-y-segura
```

### Opción 2: Sin MySQL (Fallback a H2)
```bash
JWT_SECRET=tu-clave-secreta-muy-larga-y-segura
```

## Pasos para Configurar Variables en Railway

### 1. Ir a Railway Dashboard
- Ve a https://railway.app/dashboard
- Selecciona tu proyecto
- Ve a la pestaña "Variables"

### 2. Agregar Variables
Haz clic en "New Variable" y agrega:

#### Variable: `JWT_SECRET`
**Valor**: `agrogestionSecretKey2024ForJWTTokenGenerationAndValidationSecureAndLongEnough`

#### Variable: `DATABASE_URL` (si tienes MySQL)
**Valor**: `jdbc:mysql://tu-host-mysql:3306/tu-base-datos?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true`

#### Variable: `DB_USERNAME` (si tienes MySQL)
**Valor**: `tu-usuario-mysql`

#### Variable: `DB_PASSWORD` (si tienes MySQL)
**Valor**: `tu-contraseña-mysql`

### 3. Verificar Configuración
- Asegúrate de que todas las variables estén guardadas
- Verifica que no haya espacios extra
- Confirma que los valores sean correctos

## Verificación de Logs

### 1. Ver Logs en Railway
- Ve a la pestaña "Logs"
- Busca estos mensajes de éxito:
  ```
  Started AgroCloudApplication
  Tomcat started on port(s): 8080
  ```

### 2. Buscar Errores Comunes
- `Connection refused` → Problema de base de datos
- `Port already in use` → Puerto ocupado
- `JWT secret not found` → Variable JWT_SECRET faltante
- `Database connection failed` → Credenciales incorrectas

## Prueba Manual

Una vez configurado, prueba estos endpoints:

```bash
# Healthcheck básico
curl https://tu-app.railway.app/

# Healthcheck simple
curl https://tu-app.railway.app/health

# Ping
curl https://tu-app.railway.app/ping
```

## Solución de Problemas

### Si el healthcheck sigue fallando:
1. Verifica que todas las variables estén configuradas
2. Revisa los logs para errores específicos
3. Asegúrate de que la base de datos esté activa (si usas MySQL)
4. Prueba con la configuración de H2 (sin MySQL)

### Si usas MySQL y no conecta:
1. Verifica que la base de datos esté activa
2. Confirma las credenciales
3. Asegúrate de que la base de datos sea accesible desde Railway
4. Verifica que el puerto 3306 esté abierto
