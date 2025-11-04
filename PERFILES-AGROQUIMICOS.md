# 👥 Perfiles con Acceso a Gestión de Agroquímicos

## 📋 Permisos Configurados

La funcionalidad de **Gestión Inteligente de Agroquímicos** está disponible para los mismos perfiles que tienen acceso a **Insumos**.

### **Permisos Requeridos:**

1. **Ver aplicaciones**: `canViewInsumos`
2. **Crear aplicaciones**: `canCreateInsumos`
3. **Eliminar aplicaciones**: `canDeleteInsumos`

---

## ✅ Perfiles con Acceso Completo

Los siguientes perfiles pueden **ver, crear y eliminar** aplicaciones de agroquímicos:

| Perfil | Ver | Crear | Eliminar | Notas |
|--------|-----|-------|----------|-------|
| **ADMIN** | ✅ | ✅ | ✅ | Acceso completo |
| **PROPIETARIO** | ✅ | ✅ | ✅ | Acceso completo |
| **JEFE_CAMPO** | ✅ | ✅ | ✅ | Acceso completo |
| **TECNICO_AGRICOLA** | ✅ | ✅ | ✅ | Acceso completo |
| **CONTADOR** | ✅ | ✅ | ✅ | Acceso completo |
| **ASESOR** | ✅ | ✅ | ✅ | Acceso completo |

---

## 👁️ Perfiles con Acceso de Solo Lectura

Los siguientes perfiles pueden **solo ver** las aplicaciones (no pueden crear ni eliminar):

| Perfil | Ver | Crear | Eliminar | Notas |
|--------|-----|-------|----------|-------|
| **OPERARIO** | ✅ | ❌ | ❌ | Solo lectura |
| **CONSULTOR_EXTERNO** | ✅ | ❌ | ❌ | Solo lectura |
| **INVITADO** | ✅ | ❌ | ❌ | Solo lectura |
| **LECTURA** | ✅ | ❌ | ❌ | Solo lectura |

---

## 🔍 Cómo Verificar tu Perfil

### **Opción 1: En el Dashboard**

1. Inicia sesión en el sistema
2. En la parte superior derecha, verás tu nombre de usuario
3. Al lado, debería aparecer tu **rol/empresa**

### **Opción 2: En la Consola del Navegador**

1. Abre las **Herramientas de Desarrollador** (F12)
2. Ve a la pestaña **Console**
3. Escribe: `localStorage.getItem('userRole')`
4. Presiona Enter
5. Verás tu rol actual

---

## 🎯 Qué Puedes Hacer Según tu Perfil

### **Si tienes acceso completo:**

1. **Ver aplicaciones**:
   - Dashboard → Aplicaciones Agroquímicos
   - Verás estadísticas y lista de aplicaciones

2. **Crear aplicaciones**:
   - Botón "➕ Nueva Aplicación"
   - Formulario con cálculo automático
   - Validación de stock

3. **Eliminar aplicaciones**:
   - Botón "🗑️ Eliminar" en cada aplicación
   - Restaura el stock automáticamente

4. **Configurar dosis en insumos**:
   - Insumos → Nuevo Insumo → Configurar Dosis
   - Agregar múltiples dosis por tipo de aplicación

### **Si tienes acceso de solo lectura:**

1. **Ver aplicaciones**:
   - Dashboard → Aplicaciones Agroquímicos
   - Verás estadísticas y lista de aplicaciones
   - ❌ NO verás el botón "➕ Nueva Aplicación"
   - ❌ NO verás el botón "🗑️ Eliminar"

2. **Ver dosis en insumos**:
   - Puedes ver los insumos y sus dosis configuradas
   - ❌ NO puedes crear ni editar insumos

---

## 🔧 Si No Ves la Funcionalidad

### **Problema 1: No ves la tarjeta "Aplicaciones Agroquímicos"**

**Causa**: Tu perfil no tiene el permiso `canViewInsumos`

**Solución**: Contacta al administrador para que te asigne el rol correcto

---

### **Problema 2: Ves la tarjeta pero no puedes crear aplicaciones**

**Causa**: Tu perfil no tiene el permiso `canCreateInsumos`

**Solución**: Contacta al administrador para que te asigne permisos de creación

---

### **Problema 3: No ves el botón "Configurar Dosis" en Insumos**

**Causa**: Tu perfil no tiene el permiso `canCreateInsumos`

**Solución**: Contacta al administrador para que te asigne permisos de creación

---

## 📊 Resumen de Permisos

```
┌─────────────────────────────────────────────────────────┐
│  Gestión de Agroquímicos - Matriz de Permisos          │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  ADMIN                    │ ✅ Ver │ ✅ Crear │ ✅ Del │
│  PROPIETARIO              │ ✅ Ver │ ✅ Crear │ ✅ Del │
│  JEFE_CAMPO               │ ✅ Ver │ ✅ Crear │ ✅ Del │
│  TECNICO_AGRICOLA         │ ✅ Ver │ ✅ Crear │ ✅ Del │
│  CONTADOR                 │ ✅ Ver │ ✅ Crear │ ✅ Del │
│  ASESOR                   │ ✅ Ver │ ✅ Crear │ ✅ Del │
│  ───────────────────────────────────────────────────── │
│  OPERARIO                 │ ✅ Ver │ ❌ Crear │ ❌ Del │
│  CONSULTOR_EXTERNO        │ ✅ Ver │ ❌ Crear │ ❌ Del │
│  INVITADO                 │ ✅ Ver │ ❌ Crear │ ❌ Del │
│  LECTURA                  │ ✅ Ver │ ❌ Crear │ ❌ Del │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

---

## 🆘 Necesito Acceso

Si necesitas acceso a esta funcionalidad:

1. Contacta al **administrador del sistema**
2. Solicita que te asigne uno de estos roles:
   - ADMIN
   - PROPIETARIO
   - JEFE_CAMPO
   - TECNICO_AGRICOLA
   - CONTADOR
   - ASESOR

---

## 📝 Notas Importantes

1. **Los permisos se heredan del módulo de Insumos**
   - Si puedes ver/crear/eliminar insumos, puedes hacer lo mismo con aplicaciones

2. **La seguridad es por empresa**
   - Solo verás aplicaciones de tu empresa

3. **Los permisos se validan en el backend**
   - Aunque modifiques el frontend, el backend validará tus permisos

4. **El stock se valida automáticamente**
   - No puedes crear aplicaciones si no hay stock suficiente

---

¿Tienes alguna pregunta sobre los permisos? Contacta al administrador del sistema.











