# 🔍 Verificación de Configuración Railway

## Variables de Entorno Requeridas

Verifica que tengas estas variables configuradas en Railway:

### Base de Datos
```bash
DATABASE_URL=jdbc:mysql://tu-host-mysql:3306/tu-base-datos
DB_USERNAME=tu-usuario
DB_PASSWORD=tu-contraseña
```

### JWT
```bash
JWT_SECRET=tu-clave-secreta-muy-larga-y-segura
```

### Puerto (opcional, Railway lo asigna automáticamente)
```bash
PORT=8080
```

## Pasos de Verificación

### 1. Verificar Variables de Entorno
- Ve a Railway Dashboard
- Selecciona tu servicio
- Ve a "Variables"
- Verifica que todas las variables estén configuradas

### 2. Verificar Logs
- Ve a la pestaña "Logs"
- Busca errores específicos:
  - `Connection refused`
  - `Database connection failed`
  - `Port already in use`
  - `JWT secret not found`

### 3. Verificar Base de Datos
- Asegúrate de que la base de datos MySQL esté activa
- Verifica que las credenciales sean correctas
- Confirma que la base de datos sea accesible desde Railway

### 4. Probar Healthcheck Manualmente
Una vez que la aplicación esté corriendo, prueba:
```bash
curl https://tu-app.railway.app/actuator/health
```

## Soluciones Comunes

### Problema: Puerto Incorrecto
**Solución**: Railway asigna automáticamente el puerto. Usa `${PORT}` en la configuración.

### Problema: Base de Datos No Conecta
**Solución**: Verifica que la base de datos MySQL esté activa y accesible.

### Problema: Variables de Entorno Faltantes
**Solución**: Configura todas las variables requeridas en Railway.

### Problema: JWT Secret Faltante
**Solución**: Genera una clave secreta segura y configúrala.
