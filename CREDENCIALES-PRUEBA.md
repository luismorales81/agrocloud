# 🔑 Credenciales de Usuarios de Prueba

## 📋 Lista Completa de Usuarios (ACTUALIZADA 2025-10-08)

### 👑 SUPERADMIN
- **Email**: `admin@agrocloud.com`
- **Contraseña**: `admin123`
- **Rol Actual**: `SUPERADMIN`
- **Descripción**: Administrador del sistema con acceso completo
- **Permisos**: Gestión de empresas y usuarios globales, dashboard global (sin acceso a operaciones)

---

### 👨‍💼 ADMINISTRADOR (Empresa)
- **Email**: `admin.empresa@agrocloud.com`
- **Contraseña**: `admin123`
- **Rol Actual**: `ADMINISTRADOR`
- **Descripción**: Gerente de empresa con gestión completa
- **Permisos**: Gestión de usuarios, campos, lotes, cultivos, insumos, maquinaria, labores, cosechas, finanzas, reportes

---

### 👨‍💼 ADMINISTRADOR (Campo)
- **Email**: `admin.campo@agrocloud.com`
- **Contraseña**: `admin123`
- **Rol Actual**: `ADMINISTRADOR`
- **Descripción**: Administrador de campo con acceso operativo
- **Permisos**: Gestión completa de operaciones y finanzas

---

### 👨‍🌾 JEFE DE CAMPO (Pedro - ex PRODUCTOR)
- **Email**: `productor.pedro@agrocloud.com`
- **Contraseña**: `admin123`
- **Rol Actual**: `JEFE_CAMPO` ⭐ **NUEVO**
- **Descripción**: Responsable de operaciones agrícolas
- **Permisos**: Gestión de campos, lotes, cultivos, insumos, maquinaria, labores, cosechas (SIN finanzas)

---

### 👨‍🌾 JEFE DE CAMPO (María - ex ASESOR)
- **Email**: `asesor.maria@agrocloud.com`
- **Contraseña**: `admin123`
- **Rol Actual**: `JEFE_CAMPO` ⭐ **NUEVO**
- **Descripción**: Responsable de operaciones agrícolas (ex Asesor Agrónomo)
- **Permisos**: Gestión de campos, lotes, cultivos, insumos, maquinaria, labores, cosechas (SIN finanzas)

---

### 👨‍🌾 JEFE DE CAMPO (Juan - ex TÉCNICO)
- **Email**: `tecnico.juan@agrocloud.com`
- **Contraseña**: `admin123`
- **Rol Actual**: `JEFE_CAMPO` ⭐ **NUEVO**
- **Descripción**: Responsable de operaciones agrícolas (ex Técnico Agrícola)
- **Permisos**: Gestión de campos, lotes, cultivos, insumos, maquinaria, labores, cosechas (SIN finanzas)

---

### 💰 JEFE FINANCIERO (Raúl - ex CONTADOR)
- **Email**: `raul@agrocloud.com`
- **Contraseña**: `admin123`
- **Rol Actual**: `JEFE_FINANCIERO` ⭐ **NUEVO**
- **Descripción**: Responsable de finanzas y contabilidad
- **Permisos**: Gestión completa de finanzas, ver operaciones (solo lectura)

---

### 👷‍♂️ OPERARIO (Luis)
- **Email**: `operario.luis@agrocloud.com`
- **Contraseña**: `admin123`
- **Rol Actual**: `OPERARIO`
- **Descripción**: Operario de campo con acceso limitado
- **Permisos**: Registrar labores y cosechas, ver campos asignados

---

### 👁️ INVITADO (Ana)
- **Email**: `invitado.ana@agrocloud.com`
- **Contraseña**: `admin123`
- **Rol Actual**: `INVITADO`
- **Descripción**: Usuario invitado con acceso de solo lectura
- **Permisos**: Consulta de datos, visualización de reportes (solo lectura)

---

## 📝 Usuarios Adicionales

### 👨‍🌾 JEFE DE CAMPO (Pepe)
- **Email**: `pepe@agrocloud.com`
- **Contraseña**: `admin123`
- **Rol Actual**: `JEFE_CAMPO`

### 👨‍🌾 JEFE DE CAMPO (Leo)
- **Email**: `leo@agrocloud.com`
- **Contraseña**: `admin123`
- **Rol Actual**: `JEFE_CAMPO`

---

## ⚠️ IMPORTANTE

### Contraseña Universal para Desarrollo:
**TODOS los usuarios de prueba usan la misma contraseña: `admin123`**

### Antes de Producción:
- ❌ **NO usar estas credenciales en producción**
- ✅ Crear usuarios nuevos con contraseñas seguras
- ✅ Eliminar o desactivar usuarios de prueba
- ✅ Implementar política de contraseñas fuertes
- ✅ Habilitar autenticación de dos factores (2FA)

---

## 🧪 Cómo usar en el Login

### Método 1: Escribir manualmente
```
Email: tecnico.juan@agrocloud.com
Contraseña: admin123
```

### Método 2: Auto-completar desde el panel
1. Hacer clic en **"Mostrar usuarios"**
2. Hacer clic en cualquier tarjeta de usuario
3. El email y contraseña se completan automáticamente
4. Hacer clic en **"Iniciar Sesión"**

---

## 📊 Matriz de Permisos por Rol (ACTUALIZADA)

| Funcionalidad | SUPERADMIN | ADMINISTRADOR | JEFE_CAMPO | JEFE_FINANCIERO | OPERARIO | INVITADO |
|---------------|-----------|---------------|------------|-----------------|----------|----------|
| **Dashboard Global** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Gestión Empresas** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Gestión Usuarios** | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ |
| **Campos y Lotes** | ❌ | ✅ | ✅ | 👁️ | 👁️ | 👁️ |
| **Cultivos** | ❌ | ✅ | ✅ | 👁️ | 👁️ | 👁️ |
| **Insumos** | ❌ | ✅ | ✅ | 👁️ | 👁️ | 👁️ |
| **Maquinaria** | ❌ | ✅ | ✅ | 👁️ | 👁️ | 👁️ |
| **Labores** | ❌ | ✅ | ✅ | 👁️ | ✅ | 👁️ |
| **Cosechas** | ❌ | ✅ | ✅ | 👁️ | ✅ | 👁️ |
| **Finanzas** | ❌ | ✅ | ❌ | ✅ | ❌ | ❌ |
| **Reportes Operativos** | ❌ | ✅ | ✅ | 👁️ | 👁️ | ✅ |
| **Reportes Financieros** | ❌ | ✅ | ❌ | ✅ | ❌ | ❌ |

**Leyenda:**
- ✅ = Crear/Editar/Eliminar  
- 👁️ = Solo Lectura  
- ❌ = Sin Acceso

---

## 🔄 Cambios Implementados

### **Roles Nuevos:**
- **JEFE_CAMPO**: Fusión de PRODUCTOR + ASESOR + TÉCNICO
- **JEFE_FINANCIERO**: Reemplaza a CONTADOR
- **CONSULTOR_EXTERNO**: Reemplaza a LECTURA (no visible en login actual)

### **Diferencias Clave:**
- ✅ **JEFE_CAMPO** puede gestionar operaciones pero NO finanzas
- ✅ **JEFE_FINANCIERO** puede gestionar finanzas pero NO operaciones
- ✅ **OPERARIO** puede registrar labores y cosechas
- ✅ **SUPERADMIN** NO tiene acceso a operaciones (solo admin global)

---

## 🔒 Seguridad

### Para Desarrollo:
- ✅ Las contraseñas están hasheadas en la base de datos con BCrypt
- ✅ JWT tokens para autenticación
- ✅ Sesiones manejadas correctamente

### Para Producción:
- ⚠️ Cambiar TODAS las contraseñas
- ⚠️ Usar contraseñas fuertes (min 12 caracteres, mayúsculas, minúsculas, números, símbolos)
- ⚠️ Implementar política de expiración de contraseñas
- ⚠️ Habilitar logging de intentos fallidos
- ⚠️ Implementar rate limiting
- ⚠️ Considerar 2FA para administradores

---

**Última actualización:** 2025-10-06

