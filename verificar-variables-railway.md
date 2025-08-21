# 🔧 Verificación de Variables de Entorno en Railway

## Variables Requeridas

Verifica que tengas estas variables configuradas en Railway:

### 1. Variables de Base de Datos
```bash
DATABASE_URL=mysql://root:WSoobrppUQbaPINdsRcoQVkUvtYKjmSe@mysql.railway.internal:3306/railway
DB_USERNAME=root
DB_PASSWORD=WSoobrppUQbaPINdsRcoQVkUvtYKjmSe
```

### 2. Variable JWT
```bash
JWT_SECRET=agrogestionSecretKey2024ForJWTTokenGenerationAndValidationSecureAndLongEnough
```

## Pasos de Verificación

### Paso 1: Ir a Railway Dashboard
1. Ve a https://railway.app/dashboard
2. Selecciona tu proyecto AgroGestion
3. Ve a la pestaña **"Variables"**

### Paso 2: Verificar Variables
Asegúrate de que tengas estas 4 variables:

| Variable | Valor Esperado |
|----------|----------------|
| `DATABASE_URL` | `mysql://root:WSoobrppUQbaPINdsRcoQVkUvtYKjmSe@mysql.railway.internal:3306/railway` |
| `DB_USERNAME` | `root` |
| `DB_PASSWORD` | `WSoobrppUQbaPINdsRcoQVkUvtYKjmSe` |
| `JWT_SECRET` | `agrogestionSecretKey2024ForJWTTokenGenerationAndValidationSecureAndLongEnough` |

### Paso 3: Verificar Logs
1. Ve a la pestaña **"Logs"**
2. Busca errores específicos:
   - `Connection refused`
   - `Database connection failed`
   - `Access denied`
   - `JWT secret not found`

## Errores Comunes y Soluciones

### Error: "Connection refused"
**Causa**: La aplicación no puede conectarse a MySQL
**Solución**: Verifica que el servicio MySQL esté activo en Railway

### Error: "Access denied"
**Causa**: Credenciales incorrectas
**Solución**: Verifica `DB_USERNAME` y `DB_PASSWORD`

### Error: "JWT secret not found"
**Causa**: Variable JWT_SECRET faltante
**Solución**: Agrega la variable JWT_SECRET

### Error: "Port already in use"
**Causa**: Puerto ocupado
**Solución**: Railway asigna automáticamente el puerto

## Verificación Manual

Una vez que las variables estén configuradas, prueba:

```bash
# Healthcheck básico
curl https://tu-app.railway.app/

# Health simple
curl https://tu-app.railway.app/health

# Ping
curl https://tu-app.railway.app/ping
```

## Próximos Pasos

1. **Verifica las variables** usando esta guía
2. **Revisa los logs** para errores específicos
3. **Confirma que MySQL esté activo** en Railway
4. **Prueba los endpoints** manualmente
