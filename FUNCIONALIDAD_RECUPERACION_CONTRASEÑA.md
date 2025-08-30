# Funcionalidad de Recuperación y Cambio de Contraseña

## Descripción General

Se ha implementado un sistema completo de gestión de contraseñas que incluye:

1. **Recuperación de contraseña por email**: Permite a los usuarios solicitar un enlace de recuperación
2. **Cambio de contraseña desde el perfil**: Permite a los usuarios cambiar su contraseña desde dentro del sistema
3. **Verificación de email**: Sistema para verificar emails de usuarios (preparado para futuras implementaciones)

## Funcionalidades Implementadas

### 1. Recuperación de Contraseña

#### Backend
- **Endpoint**: `POST /api/auth/request-password-reset`
- **DTO**: `PasswordResetRequest`
- **Funcionalidad**: 
  - Valida que el email existe en el sistema
  - Genera un token único de recuperación
  - Establece una fecha de expiración (24 horas)
  - Envía un email con el enlace de recuperación
  - Almacena el token en la base de datos

- **Endpoint**: `POST /api/auth/reset-password`
- **DTO**: `PasswordResetConfirm`
- **Funcionalidad**:
  - Valida el token de recuperación
  - Verifica que no haya expirado
  - Actualiza la contraseña del usuario
  - Limpia el token de recuperación

#### Frontend
- **Página**: `/forgot-password`
- **Funcionalidad**:
  - Formulario para ingresar email
  - Validación de campos
  - Mensaje de confirmación
  - Redirección al login

- **Página**: `/reset-password`
- **Funcionalidad**:
  - Recibe token por URL
  - Formulario para nueva contraseña
  - Validación de contraseñas
  - Confirmación de cambio exitoso

### 2. Cambio de Contraseña desde el Perfil

#### Backend
- **Endpoint**: `POST /api/auth/change-password`
- **DTO**: `ChangePasswordRequest`
- **Funcionalidad**:
  - Requiere autenticación
  - Valida la contraseña actual
  - Verifica que las nuevas contraseñas coincidan
  - Actualiza la contraseña

#### Frontend
- **Componente**: `ChangePasswordModal`
- **Funcionalidad**:
  - Modal reutilizable
  - Validación de contraseña actual
  - Validación de nueva contraseña
  - Confirmación de contraseña

### 3. Verificación de Email

#### Backend
- **Endpoint**: `POST /api/auth/verify-email`
- **Funcionalidad**:
  - Valida token de verificación
  - Marca email como verificado
  - Limpia token de verificación

## Estructura de Base de Datos

### Nuevos Campos en la Tabla `usuarios`

```sql
ALTER TABLE usuarios 
ADD COLUMN reset_password_token VARCHAR(255) NULL,
ADD COLUMN reset_password_token_expiry DATETIME NULL,
ADD COLUMN email_verified BOOLEAN DEFAULT FALSE,
ADD COLUMN verification_token VARCHAR(255) NULL;
```

### Índices Creados

```sql
CREATE INDEX idx_usuarios_reset_token ON usuarios(reset_password_token);
CREATE INDEX idx_usuarios_verification_token ON usuarios(verification_token);
CREATE INDEX idx_usuarios_email_verified ON usuarios(email_verified);
```

## Flujo de Recuperación de Contraseña

1. **Usuario solicita recuperación**:
   - Accede a `/forgot-password`
   - Ingresa su email
   - Sistema valida email y genera token

2. **Sistema envía email**:
   - Genera token único con expiración
   - Envía email con enlace de recuperación
   - Almacena token en base de datos

3. **Usuario resetea contraseña**:
   - Hace clic en enlace del email
   - Accede a `/reset-password?token=XXX`
   - Ingresa nueva contraseña
   - Sistema valida token y actualiza contraseña

4. **Confirmación**:
   - Usuario recibe confirmación
   - Puede acceder al sistema con nueva contraseña

## Flujo de Cambio de Contraseña

1. **Usuario accede al perfil**:
   - Abre modal de cambio de contraseña
   - Ingresa contraseña actual

2. **Validación y cambio**:
   - Sistema valida contraseña actual
   - Usuario ingresa nueva contraseña
   - Sistema actualiza contraseña

3. **Confirmación**:
   - Usuario recibe confirmación
   - Modal se cierra automáticamente

## Seguridad Implementada

### Tokens de Recuperación
- **Generación**: UUID aleatorio
- **Expiración**: 24 horas
- **Uso único**: Se elimina después del uso
- **Almacenamiento**: Base de datos con hash

### Validaciones
- **Contraseña actual**: Verificación antes de cambio
- **Nueva contraseña**: Mínimo 6 caracteres
- **Confirmación**: Verificación de coincidencia
- **Email**: Validación de formato y existencia

### Autenticación
- **Endpoints protegidos**: Requieren token JWT
- **Validación de usuario**: Solo puede cambiar su propia contraseña
- **Logs**: Registro de todas las operaciones

## Configuración de Email

### Servicio de Email
- **Clase**: `EmailService`
- **Funcionalidad**: Simulada (logs en consola)
- **Preparado para**: Integración con JavaMailSender

### Configuración Pendiente
```java
// En EmailService.java
@Autowired
private JavaMailSender javaMailSender;

// Configurar en application.properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=tu-email@gmail.com
spring.mail.password=tu-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

## Instalación y Configuración

### 1. Actualizar Base de Datos
```bash
# Ejecutar el script SQL
mysql -u root -p < update-user-table-password-recovery.sql
```

### 2. Configurar Email (Opcional)
- Agregar dependencias de JavaMail en `pom.xml`
- Configurar propiedades de email en `application.properties`
- Implementar envío real en `EmailService`

### 3. Verificar Rutas Frontend
- Las nuevas rutas ya están configuradas en `App.tsx`
- Los componentes están importados correctamente

## Uso de la Funcionalidad

### Para Usuarios
1. **Recuperar contraseña**:
   - Ir a login → "¿Olvidaste tu contraseña?"
   - Ingresar email
   - Revisar email y hacer clic en enlace
   - Ingresar nueva contraseña

2. **Cambiar contraseña**:
   - Acceder al perfil del usuario
   - Seleccionar "Cambiar contraseña"
   - Ingresar contraseña actual y nueva
   - Confirmar cambio

### Para Administradores
- **Monitoreo**: Logs en consola del backend
- **Gestión**: Endpoints disponibles para gestión de usuarios
- **Seguridad**: Tokens expiran automáticamente

## Pruebas

### Casos de Prueba Recomendados
1. **Recuperación exitosa**:
   - Email válido → Token generado → Contraseña cambiada

2. **Token expirado**:
   - Esperar 24 horas → Intentar usar token → Error

3. **Email inexistente**:
   - Email no registrado → Error apropiado

4. **Cambio de contraseña**:
   - Contraseña actual correcta → Cambio exitoso
   - Contraseña actual incorrecta → Error

5. **Validaciones**:
   - Contraseña muy corta → Error
   - Contraseñas no coinciden → Error

## Mantenimiento

### Limpieza de Tokens
```sql
-- Eliminar tokens expirados (ejecutar periódicamente)
DELETE FROM usuarios 
WHERE reset_password_token_expiry < NOW() 
AND reset_password_token IS NOT NULL;
```

### Monitoreo
- Revisar logs del backend para errores
- Monitorear uso de endpoints de recuperación
- Verificar funcionamiento del servicio de email

## Consideraciones Futuras

1. **Integración con proveedores de email**:
   - SendGrid, Mailgun, AWS SES
   - Plantillas HTML para emails

2. **Autenticación de dos factores**:
   - SMS, Google Authenticator
   - Backup codes

3. **Políticas de contraseña**:
   - Requisitos de complejidad
   - Historial de contraseñas
   - Expiración automática

4. **Notificaciones de seguridad**:
   - Alertas por cambios de contraseña
   - Logs de acceso sospechoso
