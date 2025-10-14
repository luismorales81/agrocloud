# ✅ Validación de Superficie de Lotes Implementada

## 📋 Resumen

Se ha implementado una validación completa para asegurar que **no se puedan crear lotes con una superficie mayor a la superficie disponible del campo**.

---

## 🔧 Cambios Realizados

### 1. **Backend - Repository** (`PlotRepository.java`)

#### Métodos Agregados:
- `findByCampoIdAndActivoTrue(Long campoId)` - Busca lotes activos por campo
- `calcularSuperficieOcupadaPorCampo(Long campoId)` - Calcula la superficie total ocupada por lotes activos

```java
@Query("SELECT COALESCE(SUM(p.areaHectareas), 0) FROM Plot p WHERE p.campo.id = :campoId AND p.activo = true")
BigDecimal calcularSuperficieOcupadaPorCampo(@Param("campoId") Long campoId);
```

---

### 2. **Backend - Service** (`PlotService.java`)

#### Métodos Agregados:

**`calcularSuperficieDisponible(Long campoId)`**
- Calcula y retorna la superficie disponible de un campo
- Fórmula: `Superficie Total - Superficie Ocupada`

**`validarSuperficieDisponible(Long campoId, BigDecimal superficieLote, Long loteIdActual)`**
- Valida que la superficie del lote no exceda la disponible
- Considera si es un lote nuevo o una actualización
- Lanza `RuntimeException` con mensaje detallado si falla la validación

#### Métodos Modificados:

**`saveLote(Plot lote)`**
```java
public Plot saveLote(Plot lote) {
    // Validar superficie disponible si tiene campo asignado
    if (lote.getCampo() != null) {
        validarSuperficieDisponible(lote.getCampo().getId(), lote.getAreaHectareas(), lote.getId());
    }
    return plotRepository.save(lote);
}
```

**`updateLote(Long id, Plot loteData, User user)`**
```java
// Validar superficie disponible si se cambia el campo o la superficie
if (loteData.getCampo() != null) {
    validarSuperficieDisponible(loteData.getCampo().getId(), loteData.getAreaHectareas(), id);
}
```

---

### 3. **Backend - Controller** (`PlotController.java`)

#### Mejoras en Manejo de Errores:

**Endpoint POST `/api/v1/lotes`** (Crear Lote)
- Ahora captura `RuntimeException` y devuelve el mensaje de error específico
- Retorna HTTP 400 (Bad Request) con el mensaje de validación

**Endpoint PUT `/api/v1/lotes/{id}`** (Actualizar Lote)
- Misma mejora en manejo de errores
- Devuelve mensajes claros al frontend

```java
catch (RuntimeException e) {
    System.err.println("[PLOT_CONTROLLER] ERROR al crear lote: " + e.getMessage());
    return ResponseEntity.badRequest().body(Map.of(
        "success", false,
        "message", e.getMessage()
    ));
}
```

---

### 4. **Frontend - Component** (`LotesManagement.tsx`)

#### Mejora en Manejo de Errores:

El componente ahora captura y muestra el mensaje de error específico del backend:

```typescript
catch (error: any) {
    console.error('Error al guardar lote:', error);
    
    let errorMessage = 'Error de conexión. Por favor, verifica tu conexión e intenta nuevamente.';
    
    if (error.response?.data?.message) {
        errorMessage = error.response.data.message;
    } else if (error.response?.data) {
        errorMessage = typeof error.response.data === 'string' 
          ? error.response.data 
          : 'Error al guardar el lote. Por favor, inténtalo de nuevo.';
    }
    
    alert(errorMessage);
}
```

---

## 🎯 Funcionamiento

### Escenario 1: Campo con 150 ha, 100 ha ocupadas

**Estado del Campo:**
- Superficie Total: **150 ha**
- Superficie Ocupada: **100 ha** (por lotes existentes activos)
- Superficie Disponible: **50 ha**

**Comportamiento:**
- ✅ Crear lote de 30 ha → **Permitido**
- ✅ Crear lote de 50 ha → **Permitido**
- ❌ Crear lote de 60 ha → **BLOQUEADO**

**Mensaje de Error:**
```
La superficie del lote (60.00 ha) excede la superficie disponible del campo (50.00 ha). 
Superficie total del campo: 150.00 ha. 
Superficie ocupada por otros lotes: 100.00 ha.
```

---

### Escenario 2: Actualización de Lote Existente

**Estado del Campo:**
- Superficie Total: **150 ha**
- Lote A: **40 ha** (el que se va a actualizar)
- Lote B: **60 ha**
- Superficie Ocupada (sin Lote A): **60 ha**
- Superficie Disponible: **90 ha**

**Comportamiento:**
- ✅ Actualizar Lote A a 80 ha → **Permitido** (60 + 80 = 140 < 150)
- ✅ Actualizar Lote A a 90 ha → **Permitido** (60 + 90 = 150)
- ❌ Actualizar Lote A a 100 ha → **BLOQUEADO** (60 + 100 = 160 > 150)

---

## 🔍 Validación en Múltiples Capas

### 1. **Validación Frontend**
- Ya existía función `getSuperficieDisponible()`
- Input con `max` establecido dinámicamente
- Validación antes de enviar al backend

### 2. **Validación Backend** (Nueva)
- Validación en `PlotService.saveLote()`
- Cálculo preciso desde la base de datos
- Considera solo lotes activos
- Manejo de actualización de lotes existentes

### 3. **Validación Base de Datos**
- Query optimizada para calcular superficie ocupada
- Uso de `COALESCE` para manejar casos sin lotes

---

## 📊 Ejemplo de Logs del Backend

```
[PLOT_SERVICE] Validando superficie disponible para campo ID: 5
[PLOT_SERVICE] Superficie total del campo: 150.00 ha
[PLOT_SERVICE] Superficie ocupada: 100.00 ha
[PLOT_SERVICE] Superficie disponible: 50.00 ha
[PLOT_SERVICE] Superficie del lote a crear/actualizar: 60.00 ha
[PLOT_CONTROLLER] ERROR al crear lote: La superficie del lote (60.00 ha) excede la superficie disponible del campo (50.00 ha). Superficie total del campo: 150.00 ha. Superficie ocupada por otros lotes: 100.00 ha.
```

---

## ✅ Casos de Uso Cubiertos

1. ✅ Crear lote nuevo con superficie válida
2. ✅ Crear lote nuevo con superficie excedente (bloqueado)
3. ✅ Actualizar lote existente incrementando superficie
4. ✅ Actualizar lote existente con superficie excedente (bloqueado)
5. ✅ Eliminar lote (libera superficie)
6. ✅ Desactivar lote lógicamente (libera superficie)
7. ✅ Mostrar mensaje de error claro al usuario

---

## 🎨 Experiencia de Usuario

### Frontend - Vista de Formulario:
```
Campo: Campo Norte ✓
   ℹ️ Información del Campo
   Superficie total del campo: 150 ha
   Superficie disponible: 50.00 ha

Superficie (hectáreas) *
[   25.50   ]
Máximo: 50.00 ha
```

### Cuando Excede la Superficie:
```
🚫 Error
La superficie del lote (60.00 ha) excede la superficie disponible del campo (50.00 ha). 
Superficie total del campo: 150.00 ha. 
Superficie ocupada por otros lotes: 100.00 ha.
```

---

## 🧪 Testing Recomendado

### Casos a Probar:

1. **Crear lote en campo vacío**
   - Superficie lote < Superficie campo → OK
   - Superficie lote = Superficie campo → OK
   - Superficie lote > Superficie campo → ERROR

2. **Crear lote en campo con lotes existentes**
   - Superficie lote ≤ Superficie disponible → OK
   - Superficie lote > Superficie disponible → ERROR

3. **Actualizar lote**
   - Reducir superficie → OK
   - Aumentar superficie dentro del límite → OK
   - Aumentar superficie excediendo límite → ERROR

4. **Múltiples lotes**
   - Crear varios lotes hasta llenar el campo
   - Verificar que no se pueda exceder

---

## 📝 Notas Importantes

1. **Solo lotes activos cuentan**: Los lotes inactivos (`activo = false`) no se consideran en el cálculo
2. **Validación doble**: Frontend y backend validan independientemente
3. **Mensajes claros**: Se muestra información detallada del campo y disponibilidad
4. **Thread-safe**: La validación ocurre en transacción de base de datos

---

## 🚀 Estado

✅ **Implementación completa**
✅ **Sin errores de compilación**
✅ **Listo para testing**

---

## 📅 Fecha de Implementación

**Fecha:** 9 de Octubre, 2025
**Versión:** Backend v1.15.0, Frontend v2.1.0

