# ✅ Solución Aplicada: Usuario Consultor Externo Sin Acceso

## 🎯 Problema Identificado

El usuario "Consultor Externo" (Ana Invitada - `invitado.ana@agrocloud.com`) no podía ver ningún listado (insumos, lotes, campos, etc.) a pesar de tener los permisos correctos.

### 📊 Diagnóstico Realizado

Los resultados del diagnóstico mostraron:

```json
{
  "rol": "LECTURA",
  "rolActualizado": "CONSULTOR_EXTERNO",
  "permisos": ["canViewInsumos", "canViewLotes", ...],  ✅ Correcto
  "tieneRoles": {
    "CONSULTOR_EXTERNO": false  ❌ PROBLEMA AQUÍ
  },
  "deberiaVerInsumos": false  ❌ Por eso no veía nada
}
```

**Contradicción:**
- ✅ Los **permisos** se calculaban correctamente
- ❌ El método **`tieneRolEnEmpresa()`** devolvía `false`

## 🔍 Causa Raíz

El sistema tiene **DOS estructuras de roles** en paralelo:

1. **Sistema Antiguo** 📁
   - Tabla: `usuarios_empresas_roles`
   - Entidad: `UserCompanyRole`
   - Usa tabla `roles` separada
   
2. **Sistema Nuevo** 📁✨
   - Tabla: `usuario_empresas`
   - Entidad: `UsuarioEmpresa`  
   - Usa enum `RolEmpresa` directamente

**El problema:** El método `User.tieneRolEnEmpresa()` solo buscaba en el sistema antiguo (`userCompanyRoles`), pero el usuario Consultor Externo estaba guardado en el sistema nuevo (`usuario_empresas`).

## 🔧 Solución Implementada

### Cambio 1: Agregar Relación con Sistema Nuevo

**Archivo:** `agrogestion-backend/src/main/java/com/agrocloud/model/entity/User.java`

```java
// Relación con UserCompanyRole (tabla intermedia) - Sistema antiguo
@OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
@JsonIgnore
private List<UserCompanyRole> userCompanyRoles = new ArrayList<>();

// ✨ NUEVO: Relación con UsuarioEmpresa (nueva tabla multiempresa) - Sistema nuevo
@OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
@JsonIgnore
private List<UsuarioEmpresa> usuarioEmpresas = new ArrayList<>();
```

### Cambio 2: Modificar Método `tieneRolEnEmpresa()`

**Antes:** Solo buscaba en `userCompanyRoles` (sistema antiguo)

**Ahora:** Busca en ambos sistemas, priorizando el nuevo:

```java
public boolean tieneRolEnEmpresa(RolEmpresa rolBuscado) {
    String nombreRolBuscado = rolBuscado.name();
    
    // PRIMERO: Buscar en el sistema nuevo (tabla usuario_empresas)
    if (usuarioEmpresas != null && !usuarioEmpresas.isEmpty()) {
        boolean encontradoEnNuevo = usuarioEmpresas.stream()
                .filter(ue -> ue.getEstado() == EstadoUsuarioEmpresa.ACTIVO)
                .anyMatch(ue -> {
                    RolEmpresa rolActual = ue.getRol();
                    if (rolActual == null) return false;
                    
                    // Aplicar mapeo automático (ej: LECTURA -> CONSULTOR_EXTERNO)
                    RolEmpresa rolActualizado = rolActual.getRolActualizado();
                    
                    return rolActualizado == rolBuscado;
                });
        
        if (encontradoEnNuevo) return true;
    }
    
    // SEGUNDO: Buscar en el sistema antiguo (tabla usuarios_empresas_roles)
    if (userCompanyRoles != null && !userCompanyRoles.isEmpty()) {
        return userCompanyRoles.stream()
                .anyMatch(ucr -> {
                    Role role = ucr.getRol();
                    if (role == null) return false;
                    
                    String nombreRol = role.getNombre();
                    
                    // Mapeo manual de roles antiguos
                    if ("LECTURA".equals(nombreRol)) {
                        return "CONSULTOR_EXTERNO".equals(nombreRolBuscado);
                    }
                    // ... otros mapeos
                    
                    return nombreRol.equals(nombreRolBuscado);
                });
    }
    
    return false;
}
```

### Cambio 3: Agregar Getters/Setters

```java
public List<UsuarioEmpresa> getUsuarioEmpresas() {
    return usuarioEmpresas;
}

public void setUsuarioEmpresas(List<UsuarioEmpresa> usuarioEmpresas) {
    this.usuarioEmpresas = usuarioEmpresas;
}
```

## ✅ Resultado Esperado

Después de esta corrección:

1. ✅ El método `tieneRolEnEmpresa(RolEmpresa.CONSULTOR_EXTERNO)` devolverá **`true`**
2. ✅ El servicio `InsumoService.getInsumosByUser()` permitirá el acceso
3. ✅ El usuario Consultor Externo **verá todos los listados**:
   - Insumos
   - Maquinaria
   - Campos
   - Lotes
   - Cultivos
   - Cosechas
   - Labores
4. ❌ Seguirá sin poder:
   - Ver finanzas
   - Crear/editar/eliminar recursos
   - Gestionar usuarios

## 🧪 Pasos para Verificar

### 1. Esperar a que termine la compilación
El backend está compilándose en este momento.

### 2. Reiniciar el backend
```bash
.\REINICIAR-BACKEND-AHORA.bat
```

O detén el backend actual (Ctrl+C) y ejecuta:
```bash
.\iniciar-backend.bat
```

### 3. Probar en el Frontend

1. **Cierra sesión** en el frontend (importante para limpiar caché)
2. **Inicia sesión** nuevamente con el usuario Consultor Externo:
   - Email: `invitado.ana@agrocloud.com`
   - Contraseña: (tu contraseña)
3. Ve a cada sección del menú:
   - 📦 **Insumos** → Deberías ver la lista de insumos
   - 🚜 **Maquinaria** → Deberías ver la lista de maquinaria
   - 🌾 **Campos** → Deberías ver los campos
   - 📋 **Lotes** → Deberías ver los lotes
   - 🌱 **Cultivos** → Deberías ver los cultivos
   - 🌾 **Cosechas** → Deberías ver las cosechas
   - 👷 **Labores** → Deberías ver las labores

### 4. Probar con la Página de Diagnóstico (Opcional)

Si quieres verificar que todo está correcto, abre nuevamente `test-diagnostico-usuario.html` y:

1. Inicia sesión
2. Haz clic en **"Verificar Acceso a Insumos"**
3. Deberías ver:
```json
{
  "tieneRolConsultorExterno": true,  ✅ Ahora debe ser true
  "deberiaVerInsumos": true  ✅ Ahora debe ser true
}
```

## 📝 Archivos Modificados

1. **agrogestion-backend/src/main/java/com/agrocloud/model/entity/User.java**
   - Agregada relación con `UsuarioEmpresa`
   - Modificado método `tieneRolEnEmpresa()` para buscar en ambos sistemas
   - Agregados getters/setters

2. **agrogestion-backend/src/main/java/com/agrocloud/dto/UsuarioEmpresaDTO.java** (previo)
   - Mejorada serialización JSON

3. **agrogestion-backend/src/main/java/com/agrocloud/controller/DiagnosticoController.java** (nuevo)
   - Creado controlador de diagnóstico

## 🎓 Lección Aprendida

Cuando se migra de un sistema a otro, es importante:

1. ✅ Mantener **retrocompatibilidad** con el sistema antiguo
2. ✅ **Verificar ambas fuentes** de datos durante la transición
3. ✅ Tener **herramientas de diagnóstico** para identificar problemas
4. ✅ **Mapear automáticamente** roles antiguos a nuevos

Esta solución asegura que:
- Los usuarios nuevos (en `usuario_empresas`) funcionan correctamente ✅
- Los usuarios antiguos (en `usuarios_empresas_roles`) siguen funcionando ✅
- La transición es transparente para el usuario ✅

---

## 🚀 Próximos Pasos

1. **Reinicia el backend** cuando termine la compilación
2. **Prueba con el usuario Consultor Externo**
3. **Avísame si ahora puede ver los listados**
4. Si funciona correctamente, podemos eliminar los archivos de diagnóstico temporal

---

**Fecha de Solución:** 10 de Octubre de 2025  
**Estado:** ✅ Corrección implementada, pendiente de prueba

