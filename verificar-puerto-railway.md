# 🔧 Verificación de Puerto en Railway

## Problema Común: Puerto Incorrecto

Railway asigna automáticamente un puerto a través de la variable de entorno `PORT`. Si la aplicación no lee esta variable correctamente, el healthcheck fallará.

## Verificación de Variables

### 1. Variables Requeridas en Railway
```bash
PORT=8080 (Railway lo asigna automáticamente)
JWT_SECRET=agrogestionSecretKey2024ForJWTTokenGenerationAndValidationSecureAndLongEnough
```

### 2. Verificar en Railway Dashboard
1. Ve a Railway Dashboard
2. Selecciona tu servicio backend
3. Ve a la pestaña **"Variables"**
4. Verifica que tengas:
   - `PORT` (asignado automáticamente por Railway)
   - `JWT_SECRET` (configurado manualmente)

## Verificación de Logs

### 1. Buscar en los Logs
En Railway Dashboard → Logs, busca estos mensajes:

#### Mensajes de Éxito:
```
Tomcat started on port(s): [puerto-asignado]
Started AgroCloudApplication
```

#### Mensajes de Error:
```
Port already in use
Address already in use
Failed to start web server
```

### 2. Verificar Puerto Asignado
En los logs deberías ver algo como:
```
Tomcat started on port(s): 12345 (http) with context path ''
```

## Solución de Problemas

### Problema: Puerto Hardcodeado
**Síntoma**: La app siempre intenta usar puerto 8080
**Solución**: Verificar que `server.port=${PORT:8080}` esté en la configuración

### Problema: Puerto Ocupado
**Síntoma**: "Port already in use" en logs
**Solución**: Railway asigna automáticamente el puerto

### Problema: App No Inicia
**Síntoma**: No hay mensajes de "Tomcat started"
**Solución**: Revisar logs para errores de inicio

## Verificación Manual

Una vez que la app esté corriendo, prueba:

```bash
# Obtener la URL de Railway
# Luego probar:
curl https://tu-app.railway.app/
curl https://tu-app.railway.app/health
curl https://tu-app.railway.app/ping
```

## Configuración Correcta

La aplicación debe:
1. ✅ Leer `${PORT}` de las variables de entorno
2. ✅ Iniciar en el puerto asignado por Railway
3. ✅ Responder en el endpoint `/`
4. ✅ Mostrar logs de inicio exitoso

## Próximos Pasos

1. **Verificar variables** en Railway
2. **Revisar logs** para mensajes de inicio
3. **Confirmar puerto** asignado por Railway
4. **Probar endpoints** manualmente
