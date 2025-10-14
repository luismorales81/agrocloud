# 🔧 Corrección: Tipo de Maquinaria en Frontend

## 🐛 Problema

Al crear una siembra con maquinaria, el backend recibía `tipoMaquinaria = null` causando error SQL:
```
Column 'tipo_maquinaria' cannot be null
```

## 🔍 Causa Raíz

El frontend **no estaba enviando** el campo `tipoMaquinaria` al backend:

### Backend ✅ (Ya corregido antes)
- DTO `MaquinariaAsignadaDTO` tiene campo `tipoMaquinaria`
- `SiembraService` mapea el campo correctamente

### Frontend ❌ (Faltaba)
- Interfaz `MaquinariaAsignada` no tenía el campo
- No se guardaba al agregar maquinaria
- No se enviaba en el request

## ✅ Solución Aplicada

### 1. Interfaz Actualizada (línea 44-50)
```typescript
interface MaquinariaAsignada {
  descripcion: string;
  proveedor?: string;
  tipoMaquinaria: string;  // ← AGREGADO
  costoTotal: number;
  observaciones?: string;
}
```

### 2. Maquinaria Propia (línea 241-246)
```typescript
const nuevaMaquinaria: MaquinariaAsignada = {
  descripcion: `${maquinaria.nombre} - ${maquinaria.tipo}`,
  proveedor: null,
  tipoMaquinaria: maquinaria.tipo,  // ← AGREGADO
  costoTotal: costo,
  observaciones: `${horas} horas × $${maquinaria.costoPorHora}/h`
};
```

### 3. Maquinaria Alquilada (línea 257-262)
```typescript
const nuevaMaquinaria: MaquinariaAsignada = {
  descripcion: formMaquinaria.descripcion,
  proveedor: formMaquinaria.proveedor,
  tipoMaquinaria: formMaquinaria.descripcion.split(' ')[0] || 'OTRO',  // ← AGREGADO
  costoTotal: parseFloat(formMaquinaria.costo),
  observaciones: formMaquinaria.observaciones || null
};
```

### 4. Request al Backend (línea 316-321)
```typescript
const maquinariaData = maquinarias.map(m => ({
  descripcion: m.descripcion,
  proveedor: m.proveedor,
  tipoMaquinaria: m.tipoMaquinaria,  // ← AGREGADO
  costoTotal: m.costoTotal,
  observaciones: m.observaciones
}));
```

## 📋 Archivo Modificado

- ✅ `agrogestion-frontend/src/components/SiembraModalHibrido.tsx`

## 🚀 Para Aplicar

El frontend debe recargarse automáticamente si está corriendo con `npm run dev`. 

Si no:
```bash
# Ctrl+C en la ventana del frontend
# Luego:
cd agrogestion-frontend
npm run dev
```

## ✅ Resultado Esperado

Ahora al crear una siembra con maquinaria:
1. ✅ Se guarda el `tipoMaquinaria` en el estado
2. ✅ Se envía al backend en el request
3. ✅ El backend lo mapea correctamente
4. ✅ Se guarda en la BD sin errores

---

**Fecha:** 30 de Septiembre, 2025  
**Archivo:** SiembraModalHibrido.tsx  
**Cambios:** 4 ediciones (interfaz + 2 asignaciones + request)
