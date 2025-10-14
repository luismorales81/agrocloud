# 🔧 Corrección: OPERARIO ahora ve TODOS los lotes de la empresa

## 🐛 Problema Detectado

El usuario **OPERARIO (Luis)** no podía registrar labores porque **el combo de lotes estaba vacío**.

### ¿Por qué estaba vacío?

El backend solo mostraba al OPERARIO:
- Sus lotes propios (creados por él) ← ❌ Un OPERARIO no crea lotes
- Lotes de sus usuarios dependientes ← ❌ Un OPERARIO no tiene usuarios a cargo

**Resultado:** 0 lotes disponibles = No puede registrar labores ❌

---

## 🤔 Análisis: ¿Qué lotes debería ver un OPERARIO?

### Caso de Uso Real:

Un **OPERARIO** es un empleado que:
- 👷‍♂️ Trabaja en el campo realizando tareas
- 📝 Registra las labores que realiza
- 🚜 Trabaja en **diferentes lotes según las necesidades**
- 🏢 No es "dueño" de lotes específicos

**Ejemplo:**
```
Lunes:    Luis siembra en Lote A1 (50 ha de soja)
Martes:   Luis fertiliza en Lote B2 (30 ha de maíz)
Miércoles: Luis aplica herbicida en Lote C3 (40 ha de trigo)
Jueves:   Luis vuelve a Lote A1 (monitoreo)
```

Los lotes **NO son de Luis**, son de la empresa. Luis simplemente registra el trabajo que hace.

---

## ✅ Solución Implementada

### Cambio en `PlotService.java`:

**Antes:**
```java
// Solo ADMIN y JEFE_CAMPO veían todos los lotes de la empresa
if (user.esAdministradorEmpresa(...) || user.tieneRolEnEmpresa(RolEmpresa.JEFE_CAMPO)) {
    // Ver TODOS los lotes de la empresa
}
```

**Ahora:**
```java
// ADMIN, JEFE_CAMPO y OPERARIO ven todos los lotes de la empresa
if (user.esAdministradorEmpresa(...) || 
    user.tieneRolEnEmpresa(RolEmpresa.JEFE_CAMPO) ||
    user.tieneRolEnEmpresa(RolEmpresa.OPERARIO)) {  // ← AGREGADO
    
    System.out.println("[PLOT_SERVICE] Usuario es Admin/JEFE_CAMPO/OPERARIO de empresa, 
                        mostrando TODOS los lotes de la empresa");
    
    // Ver TODOS los lotes de la empresa
}
```

---

## 🎯 Resultado

### Dashboard de OPERARIO - Antes:
```
Al ir a "Labores" → "Nueva Labor"
Combo de Lotes: [Vacío]  ❌
No puede registrar labores
```

### Dashboard de OPERARIO - Ahora:
```
Al ir a "Labores" → "Nueva Labor"
Combo de Lotes:
  - Lote A1 (Soja - 50 ha)    ✅
  - Lote B2 (Maíz - 30 ha)    ✅
  - Lote C3 (Trigo - 40 ha)   ✅
Puede seleccionar y registrar labores
```

---

## 📊 Comparación de Acceso a Lotes

| Rol | ¿Puede crear lotes? | ¿Qué lotes ve? | ¿Para qué? |
|-----|-------------------|----------------|------------|
| **ADMINISTRADOR** | ✅ Sí | Todos de la empresa | Gestión completa |
| **JEFE_CAMPO** | ✅ Sí | Todos de la empresa | Planificación y supervisión |
| **OPERARIO** | ❌ No | Todos de la empresa ← **AHORA SÍ** | Registrar labores realizadas |
| **JEFE_FINANCIERO** | ❌ No (solo lectura) | Todos de la empresa | Consulta de costos |
| **INVITADO** | ❌ No (solo lectura) | Todos de la empresa | Consulta |

---

## 🔍 Lógica de Negocio

### ¿Por qué el OPERARIO necesita ver todos los lotes?

**Escenario típico:**

1. **Jefe de Campo planifica:**
   ```
   "Luis, mañana siembras Lote A1 y aplicas fertilizante en Lote B2"
   ```

2. **Luis (Operario) registra su trabajo:**
   ```
   Labor 1:
   - Lote: A1
   - Tipo: Siembra
   - Fecha: 09/10/2025
   - Responsable: Luis Operario
   - Horas: 8
   
   Labor 2:
   - Lote: B2
   - Tipo: Fertilización
   - Fecha: 09/10/2025
   - Responsable: Luis Operario
   - Horas: 4
   ```

3. **Sistema registra:**
   ```
   ✅ Labor creada en Lote A1 por Luis
   ✅ Labor creada en Lote B2 por Luis
   📊 Costos de mano de obra calculados
   📈 Seguimiento de progreso actualizado
   ```

**Si Luis NO viera los lotes:** ❌ No podría registrar nada

---

## 🔒 Seguridad y Permisos

### ¿Es seguro que OPERARIO vea todos los lotes?

**SÍ, porque:**

1. ✅ **Solo ve los lotes (lectura)**, no puede modificarlos
2. ✅ **No ve información financiera** de los lotes
3. ✅ **Solo registra labores**, que es su función
4. ✅ Es necesario para su trabajo diario
5. ✅ Ve los mismos lotes que el JEFE_CAMPO (quien supervisa su trabajo)

### Matriz de Permisos sobre Lotes:

| Rol | Ver Lotes | Crear Lotes | Editar Lotes | Eliminar Lotes | Registrar Labores |
|-----|-----------|-------------|--------------|----------------|-------------------|
| **ADMINISTRADOR** | ✅ Todos | ✅ Sí | ✅ Sí | ✅ Sí | ✅ Sí |
| **JEFE_CAMPO** | ✅ Todos | ✅ Sí | ✅ Sí | ✅ Sí | ✅ Sí |
| **OPERARIO** | ✅ Todos | ❌ No | ❌ No | ❌ No | ✅ Sí |
| **JEFE_FINANCIERO** | 👁️ Solo lectura | ❌ No | ❌ No | ❌ No | ❌ No |
| **INVITADO** | 👁️ Solo lectura | ❌ No | ❌ No | ❌ No | ❌ No |

---

## 📝 Cambios en el Código

### 1. **PlotService.java** - Lotes

**Archivo:** `agrogestion-backend/src/main/java/com/agrocloud/service/PlotService.java`

**Línea 52-57:** Agregado `OPERARIO` a la condición que permite ver todos los lotes de la empresa.

```java
// Admin, JEFE_CAMPO u OPERARIO ven TODOS los lotes de su empresa
if (user.esAdministradorEmpresa(...) || 
    user.tieneRolEnEmpresa(RolEmpresa.JEFE_CAMPO) ||
    user.tieneRolEnEmpresa(RolEmpresa.OPERARIO)) {  // ← AGREGADO
    
    // Obtener TODOS los lotes de la empresa
    Empresa empresa = user.getEmpresa();
    // ... código para obtener todos los lotes
}
```

### 2. **FieldService.java** - Campos

**Archivo:** `agrogestion-backend/src/main/java/com/agrocloud/service/FieldService.java`

**Línea 60-64:** Agregado `OPERARIO` para que también vea todos los campos de la empresa.

```java
// Admin, JEFE_CAMPO u OPERARIO ven TODOS los campos de su empresa
if (user.esAdministradorEmpresa(...) || 
    user.tieneRolEnEmpresa(RolEmpresa.JEFE_CAMPO) ||
    user.tieneRolEnEmpresa(RolEmpresa.OPERARIO)) {  // ← AGREGADO
    
    // Obtener TODOS los campos de la empresa
}
```

### 3. **LaborService.java** - Labores

**Archivo:** `agrogestion-backend/src/main/java/com/agrocloud/service/LaborService.java`

**Línea 99-103:** Agregado `OPERARIO` para que vea TODAS las labores de la empresa.

```java
// Admin, JEFE_CAMPO u OPERARIO ven TODAS las labores de la empresa
if (user.esAdministradorEmpresa(...) || 
    user.tieneRolEnEmpresa(RolEmpresa.JEFE_CAMPO) ||
    user.tieneRolEnEmpresa(RolEmpresa.OPERARIO)) {  // ← AGREGADO
    
    // Obtener TODOS los lotes y luego todas las labores
}
```

**Razón:** El OPERARIO necesita ver TODAS las labores para tener contexto de qué se hizo en cada lote antes de registrar su propia labor.

---

## 🧪 Cómo Verificar

### Paso 1: Reiniciar el backend
```bash
cd agrogestion-backend
mvnw spring-boot:run
```

### Paso 2: Login como OPERARIO
```
Email: operario.luis@agrocloud.com
Contraseña: admin123
```

### Paso 3: Ir a "Labores" → "Nueva Labor"

**Resultado esperado:**
```
Combo de Lotes:
  [Seleccionar lote]
  Lote A1 - Soja (50.00 ha)     ✅
  Lote B2 - Maíz (30.00 ha)     ✅
  Lote C3 - Trigo (40.00 ha)    ✅
```

### Paso 4: Registrar una labor

**Formulario:**
```
Lote: Lote A1
Tipo de Labor: Siembra
Fecha: 09/10/2025
Responsable: Luis Operario
Estado: Completada
```

**Resultado esperado:** ✅ Labor registrada exitosamente

---

## 💡 Beneficios de Este Cambio

1. ✅ **OPERARIO puede hacer su trabajo**: Registrar labores
2. ✅ **Flexibilidad**: Puede trabajar en cualquier lote
3. ✅ **Trazabilidad**: Se registra quién hizo qué en cada lote
4. ✅ **Control**: El JEFE_CAMPO ve las labores registradas por operarios
5. ✅ **Realista**: Refleja el flujo de trabajo real en el campo

---

## 🔄 Flujo Completo

### Planificación (JEFE_CAMPO):
```
1. Juan (Jefe de Campo) crea Lote A1
2. Juan planifica: "Siembra de soja para mañana"
3. Juan asigna: "Luis, mañana siembras Lote A1"
```

### Ejecución (OPERARIO):
```
1. Luis (Operario) va a "Labores"
2. Luis crea nueva labor:
   - Lote: A1 (puede verlo en el combo)
   - Tipo: Siembra
   - Responsable: Luis Operario
3. Luis registra: "Labor completada"
```

### Seguimiento (JEFE_CAMPO):
```
1. Juan revisa labores
2. Ve: "Luis completó siembra en Lote A1"
3. Puede verificar calidad, costos, tiempos, etc.
```

---

## ✅ Estado

- ✅ **Corrección aplicada en PlotService** (acceso a lotes)
- ✅ **Corrección aplicada en FieldService** (acceso a campos)
- ✅ **Corrección aplicada en LaborService** (acceso a labores)
- ✅ **Sin errores de compilación**
- ⏳ **Pendiente: Reiniciar backend y probar**

---

## 🚀 Para Aplicar el Cambio

### Reiniciar el backend:
```bash
cd agrogestion-backend
# Detener el backend actual (Ctrl+C)
# Iniciar nuevamente
mvnw spring-boot:run
```

O usa el script:
```bash
.\iniciar-backend.bat
```

---

## 📅 Fecha de Corrección

**Fecha:** 9 de Octubre, 2025  
**Versión:** Backend v1.17.0  
**Prioridad:** Alta (Funcionalidad bloqueada)  
**Tipo:** Corrección de lógica de permisos

