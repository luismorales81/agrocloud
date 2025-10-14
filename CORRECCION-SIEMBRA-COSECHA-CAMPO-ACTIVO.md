# 🔧 Corrección: Campo `activo` en Labores de Siembra y Cosecha

## 🐛 Problema Detectado

Al crear labores de siembra o cosecha, se producía un **error 500** porque:

1. ✅ La migración agregó los campos de anulación a la tabla `labores`
2. ❌ El código Java esperaba el campo `activo` inicializado
3. ❌ `SiembraService` NO estaba inicializando `activo = true`

### Error SQL:
```
Unknown column 'fecha_anulacion' in 'field list'
```

## ✅ Solución Aplicada

### Archivos Modificados:

**1. `SiembraService.java` - Labor de Siembra (línea 84)**
```java
// ANTES:
laborSiembra.setUsuario(usuario);
laborSiembra.setObservaciones(request.getObservaciones());

// DESPUÉS:
laborSiembra.setUsuario(usuario);
laborSiembra.setActivo(true);  // ← AGREGADO
laborSiembra.setObservaciones(request.getObservaciones());
```

**2. `SiembraService.java` - Labor de Cosecha (línea 202)**
```java
// ANTES:
laborCosecha.setUsuario(usuario);
laborCosecha.setObservaciones(...);

// DESPUÉS:
laborCosecha.setUsuario(usuario);
laborCosecha.setActivo(true);  // ← AGREGADO
laborCosecha.setObservaciones(...);
```

## 📋 Pasos para Aplicar la Solución

### 1. ✅ Migración SQL Aplicada
```bash
# Ya ejecutado:
V1_11__Add_Anulacion_Fields_To_Labores.sql
```

### 2. ✅ Código Corregido
- `SiembraService.java` - Campo `activo` inicializado

### 3. ⚠️ REINICIAR BACKEND
```bash
# Detener el backend actual (Ctrl+C)
# Luego ejecutar:
.\iniciar-backend.bat

# O reiniciar todo:
.\iniciar-proyecto.bat
```

## 🧪 Probar la Corrección

1. **Detener el backend** actual
2. **Reiniciar** con `iniciar-backend.bat` o `iniciar-proyecto.bat`
3. **Crear una siembra** con insumos, maquinaria y mano de obra
4. **Verificar** que se crea correctamente sin error 500

## ✅ Resultado Esperado

```json
{
  "estado": "SEMBRADO",
  "cultivoActual": "Maíz",
  "fechaSiembra": "2025-09-30"
}
```

✅ Labor creada con `activo = true`  
✅ Insumos descontados del inventario  
✅ Costos registrados correctamente  
✅ Estado del lote actualizado  

---

**Fecha:** 30 de Septiembre, 2025  
**Corrección:** Inicialización del campo `activo` en labores de siembra y cosecha
