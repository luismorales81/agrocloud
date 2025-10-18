# 🔍 ANÁLISIS EXTENSO: LazyInitializationException

## 📋 Resumen Ejecutivo

Se ha identificado un problema sistemático de **LazyInitializationException** en la aplicación que ocurre cuando se intenta acceder a relaciones lazy-loaded de Hibernate después de que la sesión de Hibernate se ha cerrado.

### ❌ Problemas Encontrados

1. **Múltiples métodos en el modelo `User`** acceden a `userCompanyRoles` (lazy-loaded)
2. **Métodos en servicios** acceden a relaciones lazy sin cargar explícitamente
3. **Consultas en repositorios** no usan `JOIN FETCH` para cargar relaciones necesarias
4. **Inconsistencia entre desarrollo y producción** debido a configuraciones diferentes

---

## 🎯 MÉTODOS CRÍTICOS EN `User.java`

### 1. **`getAuthorities()` (Línea 125-134)** ⚠️ CRÍTICO
```java
@Override
public Collection<? extends GrantedAuthority> getAuthorities() {
    Set<String> authorities = userCompanyRoles.stream()
            .flatMap(ucr -> ucr.getRol().getRolePermissions().stream())
            .map(rp -> rp.getPermiso().getNombre())
            .collect(Collectors.toSet());
    
    return authorities.stream()
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());
}
```
**Problema:** Accede a `userCompanyRoles`, luego a `getRol()`, luego a `getRolePermissions()`, y finalmente a `getPermiso()`.
**Impacto:** CRÍTICO - Se llama en cada autenticación/autorización.

---

### 2. **`hasRoleInCompany()` (Línea 172-176)** ⚠️ ALTO
```java
public boolean hasRoleInCompany(String roleName, Long companyId) {
    return userCompanyRoles.stream()
            .anyMatch(ucr -> ucr.getRol().getNombre().equals(roleName) && 
                           ucr.getEmpresa().getId().equals(companyId));
}
```
**Problema:** Accede a `userCompanyRoles`, `getRol()`, y `getEmpresa()`.
**Impacto:** ALTO - Se usa en verificaciones de permisos.

---

### 3. **`getCompanies()` (Línea 178-183)** ⚠️ ALTO
```java
public List<Empresa> getCompanies() {
    return userCompanyRoles.stream()
            .map(UserCompanyRole::getEmpresa)
            .distinct()
            .collect(Collectors.toList());
}
```
**Problema:** Accede a `userCompanyRoles` y luego a `getEmpresa()`.
**Impacto:** ALTO - Se usa para obtener empresas del usuario.

---

### 4. **`getRolesInCompany()` (Línea 185-190)** ⚠️ MEDIO
```java
public List<Role> getRolesInCompany(Long companyId) {
    return userCompanyRoles.stream()
            .filter(ucr -> ucr.getEmpresa().getId().equals(companyId))
            .map(UserCompanyRole::getRol)
            .collect(Collectors.toList());
}
```
**Problema:** Accede a `userCompanyRoles`, `getEmpresa()`, y `getRol()`.
**Impacto:** MEDIO - Se usa para obtener roles específicos.

---

### 5. **`getRoles()` (Línea 340-345)** ⚠️ ALTO
```java
@JsonIgnore
public Set<Role> getRoles() {
    // Retorna todos los roles únicos del usuario a través de las empresas
    return userCompanyRoles.stream()
            .map(UserCompanyRole::getRol)
            .collect(Collectors.toSet());
}
```
**Problema:** Accede a `userCompanyRoles` y luego a `getRol()`.
**Impacto:** ALTO - Se usa en múltiples lugares para verificar roles.

---

### 6. **`isAdmin()` (Línea 347-351)** ⚠️ ALTO
```java
public boolean isAdmin() {
    return getRoles().stream()
            .anyMatch(role -> role.getNombre().equals("ADMINISTRADOR") || 
                            role.getNombre().equals("SUPERADMIN"));
}
```
**Problema:** Llama a `getRoles()` que accede a `userCompanyRoles`.
**Impacto:** ALTO - Se usa en verificaciones de permisos.

---

### 7. **`isSuperAdmin()` (Línea 353-356)** ⚠️ ALTO
```java
public boolean isSuperAdmin() {
    return getRoles().stream()
            .anyMatch(role -> role.getNombre().equals("SUPERADMIN"));
}
```
**Problema:** Llama a `getRoles()` que accede a `userCompanyRoles`.
**Impacto:** ALTO - Se usa en verificaciones de permisos.

---

### 8. **`getEmpresa()` (Línea 363-369)** ⚠️ CRÍTICO
```java
public Empresa getEmpresa() {
    // Retorna la primera empresa del usuario (para compatibilidad)
    return userCompanyRoles.stream()
            .findFirst()
            .map(UserCompanyRole::getEmpresa)
            .orElse(null);
}
```
**Problema:** Accede a `userCompanyRoles` y luego a `getEmpresa()`.
**Impacto:** CRÍTICO - Se usa en múltiples servicios (DashboardService, etc.).

---

### 9. **`setRoles()` (Línea 380-400)** ⚠️ MEDIO
```java
public void setRoles(Set<Role> roles) {
    // ...
    if (!userCompanyRoles.isEmpty()) {
        empresaParaRol = userCompanyRoles.get(0).getEmpresa();
    }
    // ...
}
```
**Problema:** Accede a `userCompanyRoles` y luego a `getEmpresa()`.
**Impacto:** MEDIO - Se usa al actualizar roles.

---

### 10. **`setRolesConEmpresa()` (Línea 405-435)** ⚠️ MEDIO
```java
public void setRolesConEmpresa(Set<Role> roles, Empresa empresa) {
    // ...
    userCompanyRoles.removeIf(ucr -> ucr.getEmpresa() != null && ucr.getEmpresa().equals(empresa));
    // ...
    userCompanyRoles.add(ucr);
}
```
**Problema:** Accede a `userCompanyRoles` y luego a `getEmpresa()`.
**Impacto:** MEDIO - Se usa al actualizar roles.

---

### 11. **`esAdministradorEmpresa()` (Línea 466-470)** ⚠️ ALTO
```java
public boolean esAdministradorEmpresa(Long empresaId) {
    return userCompanyRoles.stream()
            .anyMatch(ucr -> ucr.getEmpresa().getId().equals(empresaId) && 
                            ucr.getRol().getNombre().equals("ADMINISTRADOR"));
}
```
**Problema:** Accede a `userCompanyRoles`, `getEmpresa()`, y `getRol()`.
**Impacto:** ALTO - Se usa en verificaciones de permisos.

---

### 12. **`tieneRolEnEmpresa()` (Línea 476-540)** ✅ YA CORREGIDO
```java
public boolean tieneRolEnEmpresa(com.agrocloud.model.enums.RolEmpresa rolBuscado) {
    // PRIMERO: Buscar en el sistema nuevo (tabla usuario_empresas)
    if (usuarioEmpresas != null && !usuarioEmpresas.isEmpty()) {
        // ... usa usuarioEmpresas (ya cargada con JOIN FETCH)
    }
    
    // SEGUNDO: Buscar en el sistema antiguo (tabla usuarios_empresas_roles)
    if (userCompanyRoles != null && !userCompanyRoles.isEmpty()) {
        // ... usa userCompanyRoles
    }
}
```
**Estado:** ✅ YA CORREGIDO - Usa el sistema nuevo primero.

---

### 13. **`perteneceAEmpresa()` (Línea 446-464)** ✅ YA CORREGIDO
```java
public boolean perteneceAEmpresa(Long empresaId) {
    // PRIMERO: Buscar en el sistema nuevo (tabla usuario_empresas)
    if (usuarioEmpresas != null && !usuarioEmpresas.isEmpty()) {
        // ... usa usuarioEmpresas (ya cargada con JOIN FETCH)
    }
    
    // SEGUNDO: Buscar en el sistema antiguo (tabla usuarios_empresas_roles)
    if (userCompanyRoles != null && !userCompanyRoles.isEmpty()) {
        // ... usa userCompanyRoles
    }
}
```
**Estado:** ✅ YA CORREGIDO - Usa el sistema nuevo primero.

---

## 🔧 SERVICIOS AFECTADOS

### 1. **DashboardService** ⚠️ CRÍTICO
**Método:** `obtenerEstadisticasAdmin()` (Línea 363-486)
**Problema:** Llama a `usuario.getEmpresa()` y `user.perteneceAEmpresa(empresaId)`.
**Estado:** ⚠️ PARCIALMENTE CORREGIDO - `perteneceAEmpresa` ya está corregido, pero `getEmpresa()` sigue siendo problemático.

---

### 2. **EmpresaController** ✅ YA CORREGIDO
**Método:** `obtenerMisEmpresas()` (Línea 122-141)
**Problema:** Accedía a `relacion.getUsuario()` y `relacion.getEmpresa()`.
**Estado:** ✅ CORREGIDO - Usa datos del usuario autenticado.

---

### 3. **UsuarioEmpresaRepository** ✅ YA CORREGIDO
**Método:** `findEmpresasActivasByUsuarioId()` (Línea 54)
**Problema:** No usaba `JOIN FETCH` para cargar relaciones.
**Estado:** ✅ CORREGIDO - Ahora usa `JOIN FETCH ue.empresa JOIN FETCH ue.usuario`.

---

## 📊 RESUMEN DE IMPACTO

| Método | Impacto | Estado | Acciones Necesarias |
|--------|---------|--------|---------------------|
| `getAuthorities()` | CRÍTICO | ⚠️ PENDIENTE | Refactorizar para usar sistema nuevo |
| `hasRoleInCompany()` | ALTO | ⚠️ PENDIENTE | Refactorizar para usar sistema nuevo |
| `getCompanies()` | ALTO | ⚠️ PENDIENTE | Refactorizar para usar sistema nuevo |
| `getRolesInCompany()` | MEDIO | ⚠️ PENDIENTE | Refactorizar para usar sistema nuevo |
| `getRoles()` | ALTO | ⚠️ PENDIENTE | Refactorizar para usar sistema nuevo |
| `isAdmin()` | ALTO | ⚠️ PENDIENTE | Refactorizar para usar sistema nuevo |
| `isSuperAdmin()` | ALTO | ⚠️ PENDIENTE | Refactorizar para usar sistema nuevo |
| `getEmpresa()` | CRÍTICO | ⚠️ PENDIENTE | Refactorizar para usar sistema nuevo |
| `setRoles()` | MEDIO | ⚠️ PENDIENTE | Refactorizar para usar sistema nuevo |
| `setRolesConEmpresa()` | MEDIO | ⚠️ PENDIENTE | Refactorizar para usar sistema nuevo |
| `esAdministradorEmpresa()` | ALTO | ⚠️ PENDIENTE | Refactorizar para usar sistema nuevo |
| `tieneRolEnEmpresa()` | CRÍTICO | ✅ CORREGIDO | - |
| `perteneceAEmpresa()` | CRÍTICO | ✅ CORREGIDO | - |

---

## 🚀 PLAN DE ACCIÓN RECOMENDADO

### FASE 1: Métodos Críticos (Prioridad ALTA) 🔴
1. **`getAuthorities()`** - Se llama en cada autenticación
2. **`getEmpresa()`** - Se usa en DashboardService y otros servicios
3. **`isAdmin()` / `isSuperAdmin()`** - Se usan en verificaciones de permisos

### FASE 2: Métodos de Utilidad (Prioridad MEDIA) 🟡
4. **`getCompanies()`** - Se usa para obtener empresas del usuario
5. **`getRoles()`** - Se usa en múltiples lugares
6. **`hasRoleInCompany()`** - Se usa en verificaciones de permisos
7. **`esAdministradorEmpresa()`** - Se usa en verificaciones de permisos

### FASE 3: Métodos de Modificación (Prioridad BAJA) 🟢
8. **`setRoles()`** - Se usa al actualizar roles
9. **`setRolesConEmpresa()`** - Se usa al actualizar roles
10. **`getRolesInCompany()`** - Se usa para obtener roles específicos

---

## 💡 ESTRATEGIA DE CORRECCIÓN

### Opción 1: Refactorizar para usar el sistema nuevo (RECOMENDADO) ✅
```java
// ANTES (Sistema antiguo - lazy-loaded)
public boolean isAdmin() {
    return getRoles().stream()
            .anyMatch(role -> role.getNombre().equals("ADMINISTRADOR") || 
                            role.getNombre().equals("SUPERADMIN"));
}

// DESPUÉS (Sistema nuevo - ya cargado con JOIN FETCH)
public boolean isAdmin() {
    // PRIMERO: Buscar en el sistema nuevo
    if (usuarioEmpresas != null && !usuarioEmpresas.isEmpty()) {
        boolean esAdmin = usuarioEmpresas.stream()
                .filter(ue -> ue.getEstado() == EstadoUsuarioEmpresa.ACTIVO)
                .anyMatch(ue -> {
                    RolEmpresa rol = ue.getRol();
                    if (rol == null) return false;
                    RolEmpresa rolActualizado = rol.getRolActualizado();
                    return rolActualizado == RolEmpresa.ADMINISTRADOR || 
                           rolActualizado == RolEmpresa.SUPERADMIN;
                });
        if (esAdmin) return true;
    }
    
    // SEGUNDO: Buscar en el sistema antiguo (fallback)
    if (userCompanyRoles != null && !userCompanyRoles.isEmpty()) {
        return userCompanyRoles.stream()
                .anyMatch(ucr -> {
                    Role role = ucr.getRol();
                    return role != null && 
                           ("ADMINISTRADOR".equals(role.getNombre()) || 
                            "SUPERADMIN".equals(role.getNombre()));
                });
    }
    
    return false;
}
```

### Opción 2: Usar `@Transactional` en servicios
```java
@Transactional(readOnly = true)
public Map<String, Object> obtenerEstadisticasDashboard(User usuario) {
    // La sesión de Hibernate permanece abierta durante toda la transacción
    // Las relaciones lazy se cargan automáticamente
}
```
**Problema:** No es una solución definitiva, solo pospone el problema.

### Opción 3: Usar `JOIN FETCH` en todas las consultas
```java
@Query("SELECT u FROM User u " +
       "LEFT JOIN FETCH u.userCompanyRoles ucr " +
       "LEFT JOIN FETCH ucr.empresa " +
       "LEFT JOIN FETCH ucr.rol " +
       "WHERE u.id = :userId")
Optional<User> findByIdWithRoles(@Param("userId") Long userId);
```
**Problema:** Puede causar problemas de rendimiento con múltiples relaciones.

---

## 📝 CONCLUSIÓN

El problema de **LazyInitializationException** es **sistemático** y afecta a **12 métodos críticos** en el modelo `User`. 

**Recomendación:** Implementar la **Opción 1** (refactorizar para usar el sistema nuevo) en todos los métodos críticos de la **FASE 1** y **FASE 2**.

**Prioridad:** CRÍTICA - El sistema no funcionará correctamente en producción sin estos cambios.

---

## 🔗 ARCHIVOS RELACIONADOS

- `agrogestion-backend/src/main/java/com/agrocloud/model/entity/User.java`
- `agrogestion-backend/src/main/java/com/agrocloud/service/DashboardService.java`
- `agrogestion-backend/src/main/java/com/agrocloud/controller/EmpresaController.java`
- `agrogestion-backend/src/main/java/com/agrocloud/repository/UsuarioEmpresaRepository.java`

