# Sistema Multiempresa con Spring Boot y Spring Security

Este documento describe el modelo de entidades JPA para un sistema multiempresa que permite que un usuario tenga diferentes roles en diferentes empresas.

## Arquitectura del Sistema

### Entidades Principales

1. **User** - Usuarios del sistema
2. **Company** - Empresas
3. **Role** - Roles del sistema
4. **Permission** - Permisos específicos
5. **UserCompanyRole** - Relación usuario-empresa-rol (tabla intermedia)
6. **RolePermission** - Relación rol-permiso (tabla intermedia)

### Relaciones

```
User ←→ UserCompanyRole ←→ Company
  ↓           ↓
  └───────────┼──→ Role ←→ RolePermission ←→ Permission
```

## Características Principales

### 1. Multiempresa
- Un usuario puede pertenecer a múltiples empresas
- Cada usuario puede tener diferentes roles en cada empresa
- Los permisos se calculan basándose en los roles activos del usuario en la empresa actual

### 2. Seguridad con Spring Security
- La entidad `User` implementa `UserDetails`
- Los permisos se devuelven como `GrantedAuthority`
- Integración completa con `@PreAuthorize` y `@PostAuthorize`

### 3. Flexibilidad de Roles y Permisos
- Roles granulares (ADMINISTRADOR, PRODUCTOR, ASESOR, etc.)
- Permisos específicos por módulo y acción
- Asignación dinámica de permisos a roles

## Estructura de Base de Datos

### Tablas Principales

```sql
-- Usuarios
usuarios (id, username, email, password, activo, fecha_creacion, fecha_actualizacion)

-- Empresas
empresas (id, nombre, cuit, descripcion, direccion, ciudad, provincia, codigo_postal, telefono, email, activo, fecha_creacion, fecha_actualizacion)

-- Roles
roles (id, nombre, descripcion, activo, fecha_creacion, fecha_actualizacion)

-- Permisos
permisos (id, nombre, descripcion, modulo, accion, activo, fecha_creacion, fecha_actualizacion)
```

### Tablas Intermedias

```sql
-- Usuario-Empresa-Rol
usuarios_empresas_roles (id, usuario_id, empresa_id, rol_id, activo, fecha_creacion, fecha_actualizacion)

-- Rol-Permiso
roles_permisos (id, rol_id, permiso_id, activo, fecha_creacion, fecha_actualizacion)
```

## Uso del Sistema

### 1. Crear Usuario
```java
User user = multiTenantService.createUser("juan", "juan@empresa.com", "password123");
```

### 2. Crear Empresa
```java
Company company = multiTenantService.createCompany("Mi Empresa", "20-12345678-9", "Descripción");
```

### 3. Asignar Rol a Usuario en Empresa
```java
UserCompanyRole ucr = multiTenantService.assignRoleToUserInCompany(userId, companyId, roleId);
```

### 4. Verificar Permisos
```java
boolean hasPermission = multiTenantService.userHasPermissionInCompany(userId, companyId, "USER_READ");
```

### 5. Obtener Permisos del Usuario
```java
Set<String> permissions = multiTenantService.getUserPermissionsInCompany(userId, companyId);
```

## Roles Predefinidos

### Roles del Sistema (Estructura Corregida)
- **SUPERADMIN**: Control total del sistema (máximo nivel)
- **ADMIN_EMPRESA**: Administrador de empresa con acceso completo a la empresa
- **PRODUCTOR**: Productor agrícola
- **ASESOR**: Asesor técnico
- **CONTADOR**: Contador de la empresa
- **TECNICO**: Técnico agrícola
- **OPERARIO**: Operario de campo
- **LECTURA**: Usuario con permisos de solo lectura
- **USUARIO_REGISTRADO**: Usuario registrado básico

### Jerarquía de Roles
```
Nivel 1: SUPERADMIN (Control total del sistema)
Nivel 2: ADMIN_EMPRESA (Administrador de empresa)
Nivel 3: PRODUCTOR, ASESOR, CONTADOR (Roles operativos)
Nivel 4: TECNICO (Rol técnico)
Nivel 5: OPERARIO (Rol operativo)
Nivel 6: LECTURA (Solo lectura)
Nivel 7: USUARIO_REGISTRADO (Usuario básico)
```

### Permisos por Módulo

#### Gestión de Usuarios
- `USER_READ`, `USER_CREATE`, `USER_UPDATE`, `USER_DELETE`

#### Gestión de Empresas
- `COMPANY_READ`, `COMPANY_CREATE`, `COMPANY_UPDATE`, `COMPANY_DELETE`

#### Gestión de Roles
- `ROLE_READ`, `ROLE_CREATE`, `ROLE_UPDATE`, `ROLE_DELETE`

#### Gestión de Permisos
- `PERMISSION_READ`, `PERMISSION_CREATE`, `PERMISSION_UPDATE`, `PERMISSION_DELETE`

#### Campos y Lotes
- `FIELD_READ`, `FIELD_CREATE`, `FIELD_UPDATE`, `FIELD_DELETE`
- `PLOT_READ`, `PLOT_CREATE`, `PLOT_UPDATE`, `PLOT_DELETE`

#### Cultivos y Cosechas
- `CROP_READ`, `CROP_CREATE`, `CROP_UPDATE`, `CROP_DELETE`
- `HARVEST_READ`, `HARVEST_CREATE`, `HARVEST_UPDATE`, `HARVEST_DELETE`

#### Insumos y Maquinaria
- `INPUT_READ`, `INPUT_CREATE`, `INPUT_UPDATE`, `INPUT_DELETE`
- `MACHINERY_READ`, `MACHINERY_CREATE`, `MACHINERY_UPDATE`, `MACHINERY_DELETE`

#### Labores
- `LABOR_READ`, `LABOR_CREATE`, `LABOR_UPDATE`, `LABOR_DELETE`

#### Finanzas
- `FINANCE_READ`, `FINANCE_CREATE`, `FINANCE_UPDATE`, `FINANCE_DELETE`

#### Reportes
- `REPORT_READ`, `REPORT_CREATE`, `REPORT_EXPORT`

## Configuración de Spring Security

### SecurityConfig.java
```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    
    @Bean
    public UserDetailsService userDetailsService() {
        return multiTenantService; // Implementa UserDetailsService
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/api/admin/**").hasAuthority("ADMIN_ACCESS")
                .anyRequest().authenticated()
            )
            .httpBasic();
        return http.build();
    }
}
```

### Uso en Controladores
```java
@RestController
public class ExampleController {
    
    @GetMapping("/admin/users")
    @PreAuthorize("hasAuthority('USER_READ')")
    public ResponseEntity<List<User>> getUsers() {
        // Solo usuarios con permiso USER_READ pueden acceder
    }
    
    @PostMapping("/admin/companies")
    @PreAuthorize("hasAuthority('COMPANY_CREATE')")
    public ResponseEntity<Company> createCompany(@RequestBody Company company) {
        // Solo usuarios con permiso COMPANY_CREATE pueden acceder
    }
}
```

## Ventajas del Sistema

1. **Escalabilidad**: Fácil agregar nuevas empresas y usuarios
2. **Flexibilidad**: Roles y permisos configurables
3. **Seguridad**: Integración completa con Spring Security
4. **Mantenibilidad**: Código limpio y bien estructurado
5. **Auditabilidad**: Fechas de creación y actualización en todas las entidades

## Consideraciones de Rendimiento

1. **Lazy Loading**: Las relaciones están configuradas con `FetchType.LAZY`
2. **Índices**: Índices en campos de búsqueda frecuente
3. **Caché**: Considerar implementar caché para consultas frecuentes
4. **Paginación**: Usar paginación en listados grandes

## Próximos Pasos

1. Implementar caché con Redis
2. Agregar auditoría completa con Spring Data JPA
3. Implementar notificaciones por email
4. Agregar tests unitarios y de integración
5. Documentar APIs con Swagger/OpenAPI
