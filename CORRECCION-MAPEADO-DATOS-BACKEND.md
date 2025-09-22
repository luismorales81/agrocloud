# Corrección - Mapeo de Datos al Backend

## ❌ **Problema Identificado**
Los datos de **maquinaria y mano de obra no se estaban guardando** en la base de datos, aunque se enviaban correctamente desde el frontend.

### **Causa Raíz:**
**Incompatibilidad en el mapeo de nombres de campos** entre frontend y backend:

- **Frontend enviaba**: `maquinaria_asignada`, `mano_obra` (snake_case)
- **Backend esperaba**: `maquinariaAsignada`, `manoObra` (camelCase)

## ✅ **Solución Implementada**

### **Corrección en el Frontend:**
```typescript
// ANTES (incorrecto)
const laborCompleta = {
  // ... otros campos
  insumos_usados: selectedInsumos,        // ❌ snake_case
  maquinaria_asignada: maquinariaTransformada,  // ❌ snake_case
  mano_obra: manoObraTransformada         // ❌ snake_case
};

// DESPUÉS (correcto)
const laborCompleta = {
  // ... otros campos
  insumosUsados: selectedInsumos,         // ✅ camelCase
  maquinariaAsignada: maquinariaTransformada,   // ✅ camelCase
  manoObra: manoObraTransformada          // ✅ camelCase
};
```

### **Mapeo Correcto:**
- ✅ **Frontend → Backend**: `insumosUsados` → `insumosUsados`
- ✅ **Frontend → Backend**: `maquinariaAsignada` → `maquinariaAsignada`
- ✅ **Frontend → Backend**: `manoObra` → `manoObra`

## 🔧 **Verificación del Problema**

### **Datos en Base de Datos:**
- ✅ **Labor creada**: ID 9 con descripción "noncaocnianc"
- ❌ **Maquinaria**: 0 registros para labor ID 9
- ❌ **Mano de obra**: 0 registros para labor ID 9

### **Logs del Frontend:**
```javascript
// Datos enviados correctamente
maquinaria_asignada: (2) [{…}, {…}]  // ❌ Nombre incorrecto
mano_obra: [{…}]                     // ❌ Nombre incorrecto
```

### **Backend:**
- ✅ **Controlador**: Recibía la labor correctamente
- ✅ **Servicio**: Procesaba los datos si llegaban
- ❌ **Problema**: Los campos `@Transient` no recibían datos por nombre incorrecto

## 🎯 **Resultado Esperado**

### **Después de la Corrección:**
- ✅ **Frontend**: Envía datos con nombres correctos (camelCase)
- ✅ **Backend**: Recibe datos en campos `@Transient` correctos
- ✅ **Procesamiento**: Guarda maquinaria y mano de obra en tablas correspondientes
- ✅ **Base de Datos**: Registros de `labor_maquinaria` y `labor_mano_obra` creados

### **Flujo Corregido:**
1. **Frontend**: Envía `maquinariaAsignada` y `manoObra` (camelCase)
2. **Backend**: Recibe en campos `@Transient` correspondientes
3. **Servicio**: Procesa y guarda en `labor_maquinaria` y `labor_mano_obra`
4. **Base de Datos**: Registros creados correctamente

**La corrección del mapeo de nombres de campos debería resolver el problema de guardado de datos de maquinaria y mano de obra.**
