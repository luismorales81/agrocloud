# ESQUEMA DE ROLES Y DATOS - AGROCLOUD

## 📊 **ESTRUCTURA ACTUAL DE DATOS**

### **Usuarios y Jerarquía:**
```
1. admin (SUPERADMIN) - parent_user_id: NULL
   └── Ve TODO el sistema

2. admin_empresa (ADMINISTRADOR) - parent_user_id: NULL
   └── Ve datos de su empresa "AgroCloud Demo"

3. admin.campo (ADMINISTRADOR) - parent_user_id: 2 (admin_empresa)
   └── Ve datos de su empresa "AgroCloud Demo" + sus dependientes

4. tecnico.juan (TECNICO) - parent_user_id: 5 (admin.campo)
   └── Ve solo sus datos

5. asesor.maria (ASESOR) - parent_user_id: 5 (admin.campo)
   └── Ve solo sus datos

6. productor.pedro (PRODUCTOR) - parent_user_id: 5 (admin.campo)
   └── Ve solo sus datos

7. operario.luis (OPERARIO) - parent_user_id: 6 (tecnico.juan)
   └── Ve solo sus datos

8. invitado.ana (INVITADO) - parent_user_id: 2 (admin_empresa)
   └── Ve solo sus datos
```

### **Empresas:**
- **AgroCloud Demo** (ID: 1) - Empresa principal
- **Empresa Admin** (ID: 3) - Empresa del superadmin
- **MoralesAgro** (ID: 5)
- **CespedesAgro** (ID: 6)

### **Campos Actuales:**
- **admin_empresa** tiene 5 campos:
  1. Campo Principal (activo)
  2. Campo Norte (activo)
  3. Campo Sur (activo)
  4. Campo Este (INACTIVO)
  5. Campo en santiago - zona sur (activo)

## 🎯 **ESQUEMA DE ROLES Y PERMISOS**

### **1. SUPERADMIN (admin)**
- **Ve**: TODO el sistema (todas las empresas, usuarios, datos)
- **Puede**: Crear/eliminar empresas, usuarios, cualquier dato
- **Dashboard**: Estadísticas globales del sistema
- **Campos**: Todos los campos de todas las empresas

### **2. ADMINISTRADOR (admin_empresa, admin.campo)**
- **Ve**: Datos de su empresa + datos de usuarios dependientes
- **Puede**: Gestionar usuarios de su empresa, crear/editar datos
- **Dashboard**: Estadísticas de su empresa
- **Campos**: Sus campos + campos de usuarios dependientes

### **3. TECNICO, ASESOR, PRODUCTOR, OPERARIO, INVITADO**
- **Ve**: Solo sus propios datos
- **Puede**: Crear/editar solo sus datos
- **Dashboard**: Estadísticas personales
- **Campos**: Solo sus campos

## 🔍 **PROBLEMA IDENTIFICADO**

### **Usuario admin_empresa:**
- **Tiene**: 5 campos (4 activos + 1 inactivo)
- **Debería ver**: 5 campos en dashboard Y en lista de campos
- **Problema**: Dashboard muestra 5, pero lista de campos muestra solo 4

### **Causa del problema:**
1. **DashboardService**: Cuenta campos de TODA la empresa (correcto)
2. **FieldService**: Solo cuenta campos del usuario actual (INCORRECTO)

## ✅ **SOLUCIÓN REQUERIDA**

### **Para ADMINISTRADOR (admin_empresa):**
- **Dashboard**: Sumar campos de TODOS los usuarios de su empresa
- **Lista de campos**: Mostrar campos de TODOS los usuarios de su empresa

### **Para otros roles:**
- **Dashboard**: Solo sus datos
- **Lista de campos**: Solo sus campos

## 📋 **LÓGICA CORRECTA**

### **DashboardService.obtenerEstadisticasAdmin():**
```java
// Para ADMINISTRADOR: sumar datos de TODA la empresa
if (usuario.tieneRol("ADMINISTRADOR")) {
    // Obtener todos los usuarios de la empresa
    List<User> usuariosEmpresa = obtenerUsuariosDeEmpresa(usuario.getEmpresa());
    
    // Sumar estadísticas de todos los usuarios
    for (User userEmpresa : usuariosEmpresa) {
        camposTotal += fieldRepository.countByUserId(userEmpresa.getId());
        // ... otros datos
    }
}
```

### **FieldService.getFieldsByUser():**
```java
// Para ADMINISTRADOR: mostrar campos de TODA la empresa
if (usuario.tieneRol("ADMINISTRADOR")) {
    // Obtener todos los usuarios de la empresa
    List<User> usuariosEmpresa = obtenerUsuariosDeEmpresa(usuario.getEmpresa());
    
    // Obtener campos de todos los usuarios
    List<Field> todosLosCampos = new ArrayList<>();
    for (User userEmpresa : usuariosEmpresa) {
        todosLosCampos.addAll(fieldRepository.findByUserIdAndActivoTrue(userEmpresa.getId()));
    }
    return todosLosCampos;
}
```

## 🎯 **RESULTADO ESPERADO**

### **admin_empresa debería ver:**
- **Dashboard**: 5 campos (4 activos + 1 inactivo)
- **Lista de campos**: 5 campos (4 activos + 1 inactivo)
- **Coincidencia**: ✅ SÍ

### **Otros usuarios deberían ver:**
- **Dashboard**: Solo sus datos
- **Lista de campos**: Solo sus campos
- **Coincidencia**: ✅ SÍ
