# Módulo de Usuarios y Roles - AgroGestion

## 📋 Descripción

Este módulo implementa un sistema completo de autenticación y autorización con JWT para el sistema AgroGestion. Incluye gestión de usuarios, roles y permisos con diferentes niveles de acceso.

## 🏗️ Arquitectura

### Backend (Spring Boot)
- **Entidades**: `User`, `Role`
- **Servicios**: `AuthService`, `JwtService`, `RoleService`
- **Controladores**: `AuthController`, `RoleController`
- **Configuración**: `SecurityConfig`, `JwtAuthenticationFilter`
- **DTOs**: `LoginRequest`, `LoginResponse`, `UserDto`, `CreateUserRequest`

### Frontend (React + TypeScript)
- **Componentes**: `Login`, `UsersManagement`, `RolesManagement`
- **Autenticación**: JWT token management
- **Interfaces**: TypeScript interfaces para type safety

## 🔐 Roles y Permisos

### Roles Predefinidos

1. **ADMINISTRADOR**
   - Acceso total al sistema
   - Puede gestionar usuarios y roles
   - Todos los permisos disponibles

2. **OPERARIO**
   - Registrar trabajos en lotes
   - Consumir insumos
   - Ver campos y lotes
   - Crear y actualizar labores

3. **INGENIERO AGRÓNOMO**
   - Ver lotes y campos
   - Registrar recomendaciones técnicas
   - Crear y gestionar cultivos
   - Generar reportes

4. **INVITADO**
   - Solo lectura
   - Ver información sin modificar

### Permisos Disponibles

```
USUARIOS: USER_CREATE, USER_READ, USER_UPDATE, USER_DELETE
ROLES: ROLE_CREATE, ROLE_READ, ROLE_UPDATE, ROLE_DELETE
CAMPOS: FIELD_CREATE, FIELD_READ, FIELD_UPDATE, FIELD_DELETE
LOTES: PLOT_CREATE, PLOT_READ, PLOT_UPDATE, PLOT_DELETE
CULTIVOS: CROP_CREATE, CROP_READ, CROP_UPDATE, CROP_DELETE
INSUMOS: INPUT_CREATE, INPUT_READ, INPUT_UPDATE, INPUT_DELETE
LABORES: LABOR_CREATE, LABOR_READ, LABOR_UPDATE, LABOR_DELETE
MAQUINARIA: MACHINERY_CREATE, MACHINERY_READ, MACHINERY_UPDATE, MACHINERY_DELETE
REPORTES: REPORT_CREATE, REPORT_READ, REPORT_UPDATE, REPORT_DELETE
DASHBOARD: DASHBOARD_READ
SISTEMA: SYSTEM_CONFIG
```

## 🚀 Instalación

### 1. Base de Datos

```sql
-- Ejecutar el script SQL
mysql -u root -p < crear_tablas_usuarios_roles.sql
```

### 2. Backend

```bash
cd agrogestion-backend

# Compilar el proyecto
mvn clean install

# Ejecutar la aplicación
mvn spring-boot:run
```

### 3. Frontend

```bash
cd agrogestion-frontend

# Instalar dependencias
npm install

# Ejecutar en modo desarrollo
npm run dev
```

## 👥 Usuarios de Prueba

| Rol | Email | Contraseña |
|-----|-------|------------|
| Administrador | admin@agrogestion.com | admin123 |
| Operario | operario@agrogestion.com | operario123 |
| Ingeniero | ingeniero@agrogestion.com | ingeniero123 |
| Invitado | invitado@agrogestion.com | invitado123 |

## 🔧 Configuración

### Backend (application.properties)

```properties
# JWT Configuration
jwt.secret=agrogestionSecretKey2024ForJWTTokenGenerationAndValidation
jwt.expiration=86400000
jwt.refresh-token.expiration=604800000

# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/agrogestion
spring.datasource.username=root
spring.datasource.password=

# Security Configuration
spring.security.user.name=admin
spring.security.user.password=admin
```

### Frontend

El frontend se conecta automáticamente al backend en `http://localhost:8080`.

## 📡 API Endpoints

### Autenticación

```
POST /api/auth/login
POST /api/auth/register
POST /api/auth/verify-email
POST /api/auth/request-password-reset
POST /api/auth/reset-password
```

### Usuarios

```
GET    /api/auth/users
GET    /api/auth/users/{id}
GET    /api/auth/users/filter
POST   /api/auth/register
PUT    /api/auth/users/{id}
PATCH  /api/auth/users/{id}/toggle-status
DELETE /api/auth/users/{id}
```

### Roles

```
GET    /api/roles
GET    /api/roles/{id}
GET    /api/roles/name/{name}
POST   /api/roles
PUT    /api/roles/{id}
DELETE /api/roles/{id}
POST   /api/roles/{id}/permissions
DELETE /api/roles/{id}/permissions
GET    /api/roles/permissions/available
GET    /api/roles/check-permission
GET    /api/roles/stats
POST   /api/roles/initialize
```

### Estadísticas

```
GET /api/auth/stats
```

## 🎯 Funcionalidades

### Gestión de Usuarios
- ✅ Crear, editar, eliminar usuarios
- ✅ Asignar roles a usuarios
- ✅ Activar/desactivar usuarios
- ✅ Verificar emails
- ✅ Reset de contraseñas
- ✅ Filtros y búsqueda
- ✅ Estadísticas de usuarios

### Gestión de Roles
- ✅ Crear, editar, eliminar roles
- ✅ Asignar permisos a roles
- ✅ Verificar permisos
- ✅ Estadísticas de roles
- ✅ Roles predefinidos

### Autenticación
- ✅ Login con JWT
- ✅ Logout
- ✅ Verificación de tokens
- ✅ Refresh tokens
- ✅ Protección de rutas

### Seguridad
- ✅ Contraseñas encriptadas (BCrypt)
- ✅ Tokens JWT seguros
- ✅ CORS configurado
- ✅ Validación de datos
- ✅ Manejo de errores

## 🔒 Seguridad

### JWT Token
- **Algoritmo**: HS256
- **Expiración**: 24 horas
- **Refresh Token**: 7 días
- **Claims**: username, roles, permissions

### Contraseñas
- **Encriptación**: BCrypt
- **Salt rounds**: 10
- **Longitud mínima**: 6 caracteres

### Validaciones
- Email único
- Contraseña segura
- Roles válidos
- Permisos existentes

## 🎨 Interfaz de Usuario

### Login
- Diseño moderno y responsive
- Validación en tiempo real
- Mensajes de error claros
- Información de usuarios de prueba

### Gestión de Usuarios
- Tabla con filtros avanzados
- Formularios de creación/edición
- Estadísticas visuales
- Acciones en lote

### Gestión de Roles
- Vista de permisos organizados
- Asignación visual de permisos
- Estadísticas de uso
- Validación de dependencias

## 🧪 Testing

### Backend Tests
```bash
# Ejecutar tests unitarios
mvn test

# Ejecutar tests de integración
mvn verify
```

### Frontend Tests
```bash
# Ejecutar tests
npm test

# Ejecutar tests con coverage
npm run test:coverage
```

## 📊 Monitoreo

### Logs
- Logs de autenticación
- Logs de operaciones CRUD
- Logs de errores de seguridad
- Logs de rendimiento

### Métricas
- Usuarios activos
- Intentos de login fallidos
- Uso de permisos
- Rendimiento de endpoints

## 🚨 Troubleshooting

### Problemas Comunes

1. **Error de conexión a la base de datos**
   - Verificar que MySQL esté ejecutándose
   - Verificar credenciales en application.properties

2. **Error de JWT**
   - Verificar que el secret esté configurado
   - Verificar que el token no haya expirado

3. **Error de CORS**
   - Verificar configuración de CORS en SecurityConfig
   - Verificar que el frontend esté en el puerto correcto

4. **Error de permisos**
   - Verificar que el usuario tenga el rol correcto
   - Verificar que el rol tenga los permisos necesarios

### Logs de Debug

```properties
# Habilitar logs de debug
logging.level.com.agrogestion=DEBUG
logging.level.org.springframework.security=DEBUG
```

## 🔄 Actualizaciones

### Versión 1.0.0
- ✅ Sistema básico de usuarios y roles
- ✅ Autenticación JWT
- ✅ Gestión de permisos
- ✅ Interfaz de usuario completa

### Próximas Versiones
- 🔄 Autenticación de dos factores
- 🔄 Integración con LDAP/Active Directory
- 🔄 Auditoría de cambios
- 🔄 Notificaciones por email
- 🔄 API rate limiting

## 📞 Soporte

Para soporte técnico o reportar bugs:
- 📧 Email: soporte@agrogestion.com
- 📱 Teléfono: +54 11 1234-5678
- 🌐 Web: https://agrogestion.com/soporte

## 📄 Licencia

Este proyecto está bajo la licencia MIT. Ver el archivo LICENSE para más detalles.

---

**Desarrollado con ❤️ para el sector agrícola**
