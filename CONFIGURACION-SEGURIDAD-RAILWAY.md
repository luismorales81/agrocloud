# 🔒 Configuración de Seguridad para Railway

## Variables de Entorno Requeridas

### Variables Críticas de Seguridad

Configura estas variables en Railway Dashboard → Tu Servicio → Variables:

#### 1. **Base de Datos**
```
SPRING_DATASOURCE_URL=jdbc:mysql://[HOST]:[PORT]/[DATABASE]?useSSL=true&requireSSL=true&verifyServerCertificate=false&serverTimezone=UTC&enabledTLSProtocols=TLSv1.2,TLSv1.3&allowPublicKeyRetrieval=true
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=[TU_PASSWORD_SEGURO]
DB_SSL_MODE=REQUIRED
```

**⚠️ IMPORTANTE:**
- Nunca hardcodees contraseñas en el código
- Usa contraseñas fuertes (mínimo 16 caracteres, mezcla de mayúsculas, minúsculas, números y símbolos)
- Rota las contraseñas periódicamente (cada 90 días recomendado)

#### 2. **JWT Secret**
```
JWT_SECRET=[GENERA_UN_SECRETO_FUERTE_ALEATORIO]
```

**Generar un secreto seguro:**
```bash
# En Linux/Mac
openssl rand -base64 32

# O usar un generador online seguro
```

**⚠️ IMPORTANTE:**
- El secreto debe tener al menos 32 caracteres
- No compartas este secreto públicamente
- Cambia el secreto si sospechas que fue comprometido

#### 3. **CORS (Opcional pero recomendado)**
```
CORS_ALLOWED_ORIGINS=https://tu-dominio.com,https://www.tu-dominio.com
```

**⚠️ IMPORTANTE:**
- En producción, especifica solo los dominios permitidos
- No uses `*` en producción
- Separa múltiples orígenes con comas

#### 4. **Perfil de Spring**
```
SPRING_PROFILES_ACTIVE=railway-mysql
```

### Variables Opcionales de Seguridad

#### Logging
```
LOG_LEVEL=INFO
```

#### Puerto (Railway lo configura automáticamente)
```
PORT=8080
```

## Configuración de la Base de Datos MySQL en Railway

### 1. Crear Base de Datos MySQL

1. En Railway Dashboard, haz clic en **"New"** → **"Database"** → **"MySQL"**
2. Railway creará automáticamente las variables de conexión

### 2. Obtener Variables de Conexión

1. Ve a tu servicio MySQL en Railway
2. Ve a la pestaña **"Variables"**
3. Copia los valores de:
   - `MYSQL_URL` o `MYSQLDATABASE_URL`
   - `MYSQLUSER`
   - `MYSQLPASSWORD`
   - `MYSQLHOST`
   - `MYSQLPORT`
   - `MYSQLDATABASE`

### 3. Configurar Variables en el Backend

En tu servicio Backend → Variables, configura:

```
SPRING_DATASOURCE_URL=jdbc:mysql://${MYSQLHOST}:${MYSQLPORT}/${MYSQLDATABASE}?useSSL=true&requireSSL=true&verifyServerCertificate=false&serverTimezone=UTC&enabledTLSProtocols=TLSv1.2,TLSv1.3&allowPublicKeyRetrieval=true
SPRING_DATASOURCE_USERNAME=${MYSQLUSER}
SPRING_DATASOURCE_PASSWORD=${MYSQLPASSWORD}
DB_SSL_MODE=REQUIRED
```

## Verificación de Seguridad

### 1. Verificar Conexión SSL

Una vez desplegado, verifica que la conexión use SSL:

```bash
# Revisa los logs del servicio en Railway
# Deberías ver conexiones exitosas sin errores SSL
```

### 2. Verificar Enmascaramiento de Datos

Prueba los endpoints que devuelven datos sensibles:

```bash
# Obtener empresa (CUIT debe estar enmascarado)
curl -H "Authorization: Bearer TU_TOKEN" \
  https://tu-backend.railway.app/api/empresas/1

# Respuesta esperada:
# {
#   "cuit": "20-****5678-9",  // Enmascarado
#   "emailContacto": "usua***@dominio.com"  // Enmascarado
# }
```

### 3. Verificar Protección SQL

Los servicios ahora validan automáticamente los parámetros contra inyección SQL.

## Mejores Prácticas de Seguridad

### ✅ Hacer

1. **Usar variables de entorno** para todas las credenciales
2. **Rotar contraseñas** periódicamente
3. **Revisar logs** regularmente para detectar intentos de acceso sospechosos
4. **Mantener dependencias actualizadas** (ejecutar `mvn dependency:check` periódicamente)
5. **Usar HTTPS** en producción (Railway lo proporciona automáticamente)
6. **Limitar acceso** a la base de datos solo desde la aplicación

### ❌ No Hacer

1. **Nunca** hardcodear contraseñas en el código
2. **Nunca** commitear archivos `.properties` con credenciales reales
3. **Nunca** usar `useSSL=false` en producción
4. **Nunca** exponer endpoints de administración públicamente sin autenticación
5. **Nunca** loguear contraseñas o datos sensibles completos

## Troubleshooting

### Error: "SSL connection required"

**Solución:**
1. Verifica que `DB_SSL_MODE=REQUIRED` esté configurado
2. Verifica que la URL de conexión incluya `useSSL=true&requireSSL=true`
3. Si Railway MySQL no soporta SSL, contacta soporte o usa `verifyServerCertificate=false`

### Error: "Connection refused"

**Solución:**
1. Verifica que el servicio MySQL esté corriendo en Railway
2. Verifica que las variables de entorno estén correctamente configuradas
3. Verifica que el host y puerto sean correctos

### Los datos no se enmascaran en las respuestas

**Solución:**
1. Verifica que los serializers estén correctamente importados en los DTOs
2. Verifica que el servicio `EnmascaramientoDatosService` esté disponible como bean
3. Revisa los logs para ver si hay errores de serialización

## Recursos Adicionales

- [Railway Documentation](https://docs.railway.app)
- [Spring Boot Security](https://spring.io/guides/topicals/spring-security-architecture)
- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
- [Ley 25.326 - Protección de Datos Personales (Argentina)](https://www.argentina.gob.ar/normativa/nacional/ley-25326-64790)

## Contacto de Seguridad

Si descubres una vulnerabilidad de seguridad, por favor:
1. **NO** la reportes públicamente
2. Contacta al equipo de desarrollo de forma privada
3. Proporciona detalles suficientes para reproducir el problema

