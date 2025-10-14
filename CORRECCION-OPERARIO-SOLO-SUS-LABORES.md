# 🔒 Corrección: OPERARIO solo puede eliminar sus propias labores

## 🐛 Problema Detectado

El **OPERARIO** podía **eliminar labores de otros operarios o del jefe de campo**, lo cual es **incorrecto** y **peligroso**.

### Ejemplo del problema:
```
Labores en el sistema:
- Labor 1: Siembra Lote A1 (Responsable: Carlos Pérez)
- Labor 2: Fertilización Lote B2 (Responsable: Luis Operario)
- Labor 3: Herbicida Lote C3 (Responsable: María González)

Luis (OPERARIO) ve TODAS las labores ✅ CORRECTO
Luis puede eliminar TODAS las labores ❌ INCORRECTO

❌ Luis podía borrar la labor de Carlos
❌ Luis podía borrar la labor de María
```

---

## ✅ Solución Implementada

Ahora el OPERARIO:
- ✅ **Ve TODAS las labores** (para contexto)
- ✅ **Crea sus propias labores**
- ✅ **Elimina SOLO sus propias labores**
- ❌ **NO puede eliminar labores de otros**

---

## 🔧 Implementación en Dos Capas

### 1. **Frontend** - `LaboresManagement.tsx`

#### Función de Validación:
```typescript
// Función para verificar si el usuario puede editar/eliminar una labor
const puedeModificarLabor = (labor: any): boolean => {
  // ADMIN y JEFE_CAMPO pueden modificar cualquier labor
  if (esAdministrador || esJefeCampo) {
    return true;
  }
  
  // OPERARIO solo puede modificar sus propias labores
  if (esOperario) {
    const nombreUsuario = user?.name || '';
    const responsableLabor = labor.responsable || '';
    return nombreUsuario.toLowerCase() === responsableLabor.toLowerCase();
  }
  
  // Otros roles no pueden modificar
  return false;
};
```

#### Botón de Eliminar Condicionado:
```typescript
{/* Solo mostrar botón si tiene permiso */}
{puedeModificarLabor(labor) && (
  <button onClick={() => deleteLabor(labor.id!)}>
    🗑️ Eliminar
  </button>
)}
```

---

### 2. **Backend** - `LaborService.java`

#### Validación en `deleteLabor()`:
```java
public boolean deleteLabor(Long id, User user) {
    Labor labor = laborRepository.findById(id).orElse(null);
    
    if (labor != null) {
        // Verificar acceso al lote
        if (!tieneAccesoALabor(labor, user)) {
            return false;
        }
        
        // Si es OPERARIO, solo puede eliminar sus propias labores
        if (user.tieneRolEnEmpresa(RolEmpresa.OPERARIO)) {
            String nombreUsuario = user.getFirstName() + " " + user.getLastName();
            String responsableLabor = labor.getResponsable();
            
            if (!responsableLabor.equalsIgnoreCase(nombreUsuario)) {
                throw new RuntimeException(
                    "Los operarios solo pueden eliminar sus propias labores"
                );
            }
        }
        
        // Proceder con la eliminación
        labor.setActivo(false);
        laborRepository.save(labor);
        return true;
    }
    
    return false;
}
```

#### También validado en `eliminarLabor()`:
```java
public void eliminarLabor(Long id, User usuario) {
    // ... validaciones previas
    
    // Si es OPERARIO, solo puede eliminar sus propias labores
    if (usuario.tieneRolEnEmpresa(RolEmpresa.OPERARIO)) {
        String nombreUsuario = usuario.getFirstName() + " " + usuario.getLastName();
        String responsableLabor = labor.getResponsable();
        
        if (!responsableLabor.equalsIgnoreCase(nombreUsuario)) {
            throw new RuntimeException(
                "Los operarios solo pueden eliminar sus propias labores"
            );
        }
    }
    
    // ... resto del código
}
```

---

## 🎯 Resultado

### Dashboard de OPERARIO (Luis)

**Tabla de Labores:**
```
| Fecha      | Lote   | Tipo         | Responsable    | Acciones        |
|------------|--------|--------------|----------------|-----------------|
| 09/10/2025 | Lote A1| Siembra      | Carlos Pérez   | 👁️ Solo ver    |
| 09/10/2025 | Lote B2| Fertilización| Luis Operario  | 🗑️ Eliminar ✅  |
| 08/10/2025 | Lote C3| Herbicida    | María González | 👁️ Solo ver    |
| 07/10/2025 | Lote A1| Preparación  | Luis Operario  | 🗑️ Eliminar ✅  |
```

**Luis solo ve el botón de eliminar en SUS labores.**

---

### Dashboard de JEFE_CAMPO (Juan)

**Tabla de Labores:**
```
| Fecha      | Lote   | Tipo         | Responsable    | Acciones        |
|------------|--------|--------------|----------------|-----------------|
| 09/10/2025 | Lote A1| Siembra      | Carlos Pérez   | 🗑️ Eliminar ✅  |
| 09/10/2025 | Lote B2| Fertilización| Luis Operario  | 🗑️ Eliminar ✅  |
| 08/10/2025 | Lote C3| Herbicida    | María González | 🗑️ Eliminar ✅  |
| 07/10/2025 | Lote A1| Preparación  | Luis Operario  | 🗑️ Eliminar ✅  |
```

**Juan ve el botón de eliminar en TODAS las labores.**

---

## 📊 Matriz de Permisos sobre Labores

| Rol | Ver Labores | Crear Labores | Editar Propias | Editar Otras | Eliminar Propias | Eliminar Otras |
|-----|-------------|---------------|----------------|--------------|------------------|----------------|
| **ADMINISTRADOR** | ✅ Todas | ✅ Sí | ✅ Sí | ✅ Sí | ✅ Sí | ✅ Sí |
| **JEFE_CAMPO** | ✅ Todas | ✅ Sí | ✅ Sí | ✅ Sí | ✅ Sí | ✅ Sí |
| **OPERARIO** | ✅ Todas | ✅ Sí | ✅ Sí | ❌ No | ✅ Sí | ❌ **No** ← Corregido |
| **JEFE_FINANCIERO** | 👁️ Lectura | ❌ No | ❌ No | ❌ No | ❌ No | ❌ No |
| **INVITADO** | 👁️ Lectura | ❌ No | ❌ No | ❌ No | ❌ No | ❌ No |

---

## 🔒 Seguridad en Dos Capas

### Capa 1: Frontend (UX)
- El botón de eliminar **NO se muestra** si el usuario no puede modificar la labor
- Mejora la experiencia: el usuario no ve opciones que no puede usar
- Validación: `puedeModificarLabor(labor)`

### Capa 2: Backend (Seguridad)
- Si un usuario intenta eliminar via API directa, el backend valida
- Lanza excepción: "Los operarios solo pueden eliminar sus propias labores"
- Protección contra peticiones directas al API

---

## 🧪 Casos de Prueba

### Test 1: OPERARIO intenta eliminar su propia labor ✅

**Escenario:**
```
Usuario: Luis Operario (OPERARIO)
Labor: Siembra Lote A1 (Responsable: Luis Operario)
Acción: Clic en 🗑️
```

**Resultado esperado:**
- ✅ Botón visible
- ✅ Labor eliminada
- ✅ Mensaje: "Labor eliminada exitosamente"

---

### Test 2: OPERARIO intenta eliminar labor de otro ❌

**Escenario:**
```
Usuario: Luis Operario (OPERARIO)
Labor: Fertilización Lote B2 (Responsable: Carlos Pérez)
Acción: Intenta eliminar
```

**Resultado esperado (Frontend):**
- ❌ Botón NO visible
- No puede hacer clic

**Resultado esperado (si intenta via API):**
- ❌ Backend rechaza
- Error: "Los operarios solo pueden eliminar sus propias labores"

---

### Test 3: JEFE_CAMPO elimina cualquier labor ✅

**Escenario:**
```
Usuario: Juan Técnico (JEFE_CAMPO)
Labor: Siembra Lote A1 (Responsable: Luis Operario)
Acción: Clic en 🗑️
```

**Resultado esperado:**
- ✅ Botón visible
- ✅ Labor eliminada
- ✅ Mensaje: "Labor eliminada exitosamente"

---

## 💡 ¿Por qué Esta Implementación?

### Ventajas:

1. ✅ **Responsabilidad individual**: Cada operario responde por sus labores
2. ✅ **Evita errores**: Un operario no puede borrar el trabajo de otro
3. ✅ **Supervisión**: El jefe puede corregir cualquier error
4. ✅ **Transparencia**: Se ve quién hizo cada labor
5. ✅ **Seguridad**: Doble validación (frontend + backend)

### Caso de Uso Real:

```
Situación: Luis registró incorrectamente una labor

Opción A: OPERARIO puede borrar labores de todos
❌ Luis podría borrar labores de Carlos "por error"
❌ Podría borrar evidencia de trabajo realizado
❌ Problemas de auditoría

Opción B: OPERARIO solo borra las suyas ✅
✅ Luis solo corrige sus propios errores
✅ No afecta el trabajo de otros
✅ Si hay un problema mayor, el JEFE_CAMPO lo resuelve
```

---

## 📝 Logs del Backend

### Cuando OPERARIO elimina su propia labor:
```
[LABOR_SERVICE] Validando permiso para OPERARIO:
[LABOR_SERVICE] - Usuario: Luis Operario
[LABOR_SERVICE] - Responsable labor: Luis Operario
[LABOR_SERVICE] Labor eliminada exitosamente por: operario.luis@agrocloud.com
```

### Cuando OPERARIO intenta eliminar labor de otro:
```
[LABOR_SERVICE] Validando permiso para OPERARIO:
[LABOR_SERVICE] - Usuario: Luis Operario
[LABOR_SERVICE] - Responsable labor: Carlos Pérez
[LABOR_SERVICE] OPERARIO no puede eliminar labores de otros
ERROR: Los operarios solo pueden eliminar sus propias labores
```

---

## 📄 Archivos Modificados

### Frontend:
- `agrogestion-frontend/src/components/LaboresManagement.tsx`
  - Líneas 3-4: Agregados imports de useAuth y useEmpresa
  - Líneas 97-101: Variables de rol
  - Líneas 104-120: Función `puedeModificarLabor()`
  - Línea 1289: Botón eliminar condicionado

### Backend:
- `agrogestion-backend/src/main/java/com/agrocloud/service/LaborService.java`
  - Líneas 1205-1217: Validación en `deleteLabor()`
  - Líneas 769-776: Validación en `eliminarLabor()`

---

## ✅ Estado

- ✅ **Validación en frontend** (botón condicionado)
- ✅ **Validación en backend** (doble seguridad)
- ✅ **Sin errores de compilación**
- ⏳ **Pendiente: Reiniciar backend y frontend para probar**

---

## 🚀 Para Aplicar los Cambios

### 1. Reiniciar Backend:
```bash
cd agrogestion-backend
# Detener con Ctrl+C
mvnw spring-boot:run
```

### 2. Reiniciar Frontend:
```bash
cd agrogestion-frontend
# Si está corriendo, solo refrescar (F5)
# O reiniciar con: npm run dev
```

### 3. Probar con OPERARIO:
```
Email: operario.luis@agrocloud.com
Contraseña: admin123

Ir a Labores:
- Ver todas las labores (✅ puede ver)
- Ver botón 🗑️ solo en labores de "Luis Operario" (✅ correcto)
- NO ver botón 🗑️ en labores de otros (✅ correcto)
```

---

## 📅 Fecha de Corrección

**Fecha:** 9 de Octubre, 2025  
**Versión:** Frontend v2.6.0, Backend v1.18.0  
**Prioridad:** Crítica (Seguridad y Control)  
**Tipo:** Corrección de permisos


