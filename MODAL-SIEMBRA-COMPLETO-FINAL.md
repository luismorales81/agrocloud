# ✅ Modal de Siembra Completo - Versión Final

## 🎯 Mejoras Implementadas

### 1. **Selección de Maquinaria del Inventario** ✅

#### Maquinaria Propia (del inventario):
```
┌──────────────────────────────────────────────┐
│ 🚜 Maquinaria                                │
│                                              │
│ [🏠 Propia] [🏢 Alquilada]                  │
│  ▔▔▔▔▔▔▔▔▔                                  │
│                                              │
│ [▼ Tractor John Deere - Tractor ($45/h)]    │
│ [Horas: 8] [+]                               │
│                                              │
│ 💰 Costo calculado: 8h × $45/h = $360       │
└──────────────────────────────────────────────┘
```

**Características:**
- ✅ Selector con maquinarias del inventario
- ✅ Muestra: Nombre - Tipo (Costo/hora)
- ✅ Input de horas de uso
- ✅ Cálculo automático: horas × costo_por_hora
- ✅ Sin campo de proveedor (es propia)

#### Maquinaria Alquilada:
```
┌────────────────────────────────────────────────┐
│ 🚜 Maquinaria                                  │
│                                                │
│ [🏠 Propia] [🏢 Alquilada]                    │
│             ▔▔▔▔▔▔▔▔▔▔▔                       │
│                                                │
│ [Descripción] [Proveedor] [Costo] [+]         │
└────────────────────────────────────────────────┘
```

**Características:**
- ✅ Campos de texto libre
- ✅ Requiere proveedor
- ✅ Costo manual (no se calcula)

---

### 2. **Verificación de Stock de Insumos** ✅

**Antes:**
```javascript
// No verificaba stock
setInsumosUsados([...insumosUsados, nuevoInsumo]);
```

**Ahora:**
```javascript
// Verifica stock antes de agregar
if (insumo.stockActual < cantidad) {
  alert(`⚠️ Stock insuficiente de ${insumo.nombre}.
  Disponible: ${insumo.stockActual} ${insumo.unidadMedida}
  Requerido: ${cantidad} ${insumo.unidadMedida}`);
  return;
}
```

**Beneficio:**
- ✅ Evita usar insumos no disponibles
- ✅ Mensaje claro mostrando stock disponible vs requerido
- ✅ Usuario sabe exactamente cuánto puede usar

---

### 3. **Resumen de Costos SIEMPRE Visible** ✅

**Ubicación:** Antes de los botones finales, siempre visible

```
┌──────────────────────────────────────────────────┐
│ 💰 Costo Total de Siembra          $4,751,760   │
├──────────────────────────────────────────────────┤
│   INSUMOS     │  MAQUINARIA  │   MANO DE OBRA   │
│ $4,750,000    │    $1,560    │      $200        │
│  1 item(s)    │   2 item(s)  │   1 persona(s)   │
└──────────────────────────────────────────────────┘
```

**Cuando NO hay recursos:**
```
┌──────────────────────────────────────────────────┐
│ 💰 Costo Total de Siembra                    $0 │
├──────────────────────────────────────────────────┤
│ No se han agregado recursos. El costo será $0.  │
└──────────────────────────────────────────────────┘
```

---

### 4. **Descuento Automático de Inventario** ✅

#### Backend: `SiembraService.java`

**Agregado:**
```java
@Autowired
private InventarioService inventarioService;

// Después de guardar insumos:
List<LaborInsumo> insumosGuardados = new ArrayList<>();

for (InsumoUsadoDTO insumoDTO : request.getInsumos()) {
    // ... guardar labor_insumo
    insumosGuardados.add(laborInsumo);
}

// Descontar del inventario
inventarioService.actualizarInventarioLabor(
    laborSiembra.getId(), 
    insumosGuardados, 
    null, 
    usuario
);
```

**Proceso:**
1. ✅ Usuario usa 127.5 kg de Semilla Soja
2. ✅ Backend guarda en `labor_insumos`
3. ✅ **InventarioService** descuenta 127.5 kg del stock
4. ✅ **Movimiento** registrado: SALIDA - 127.5 kg
5. ✅ Stock actualizado: 1000 → 872.5 kg

---

## 🎨 Vista Completa del Modal Mejorado

```
┌───────────────────────────────────────────────────────────┐
│  🌱 Sembrar Lote: A1 (25.5 ha)                      [✕]  │
├───────────────────────────────────────────────────────────┤
│  Cultivo a Sembrar *                                      │
│  [▼ Soja - DM 53i54                        ]              │
│                                                           │
│  ┌────────────────────────────────────────────────┐      │
│  │ 📊 Datos del Cultivo:                          │      │
│  │ Ciclo: 120 días | Rinde: 3500 kg/ha           │      │
│  │ Cosecha estimada: 28/01/2026                   │      │
│  └────────────────────────────────────────────────┘      │
│                                                           │
│  Fecha de Siembra * [30/09/2025]                         │
│  Observaciones [...]                                      │
│                                                           │
│  [ 📦 Agregar Recursos ]  ← Expandible                  │
│                                                           │
│  ┌─────────────────────────────────────────────────┐    │
│  │ 📦 Recursos Utilizados         [✕ Ocultar]     │    │
│  │                                                  │    │
│  │ [🌾 Insumos (1)] [🚜 Maquinaria (2)] [👷 M.O (1)] │    │
│  │               ▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔                 │    │
│  │                                                  │    │
│  │ [🏠 Propia] [🏢 Alquilada]  ← Selector tipo    │    │
│  │  ▔▔▔▔▔▔▔▔▔                                     │    │
│  │                                                  │    │
│  │ [▼ Tractor John Deere ($45/h)] [8h] [+]        │    │
│  │ 💰 Costo: 8h × $45/h = $360                    │    │
│  │                                                  │    │
│  │ ┌────────────────────────────────────────┐     │    │
│  │ │ Tractor John Deere  [🏠 Propia]       │     │    │
│  │ │ 8h × $45/h                             │     │    │
│  │ │ 💰 $360                        [🗑️]    │     │    │
│  │ ├────────────────────────────────────────┤     │    │
│  │ │ Sembradora  [🏢 Alquilada]            │     │    │
│  │ │ Proveedor: Maquinarias del Sur        │     │    │
│  │ │ 💰 $1,200                      [🗑️]    │     │    │
│  │ └────────────────────────────────────────┘     │    │
│  └─────────────────────────────────────────────────┘    │
│                                                           │
│  ┌───────────────────────────────────────────────────┐  │
│  │ 💰 Costo Total de Siembra          $4,751,760    │  │
│  ├───────────────────────────────────────────────────┤  │
│  │  INSUMOS     │  MAQUINARIA  │    MANO DE OBRA    │  │
│  │ $4,750,000   │   $1,560     │       $200         │  │
│  │  1 item(s)   │  2 item(s)   │    1 persona(s)    │  │
│  └───────────────────────────────────────────────────┘  │
│                                                           │
│  [Cancelar]                    [🌱 Confirmar Siembra]   │
└───────────────────────────────────────────────────────────┘
```

---

## 📋 Características Completas

### ✅ Datos Básicos:
1. **Cultivo** - Selector con lista
2. **Fecha** - Fecha de hoy por defecto
3. **Observaciones** - Opcional

### ✅ Información Automática:
- 📊 Ciclo de días (del cultivo)
- 📊 Rendimiento esperado (del cultivo)
- 📊 Fecha de cosecha estimada (calculada)

### ✅ Recursos Opcionales:

#### 🌾 Insumos:
- Selector de insumos del inventario
- Validación de stock disponible
- Cálculo automático de costo
- Descuento automático del inventario

#### 🚜 Maquinaria:
- **Propia**: Selector del inventario + horas → costo automático
- **Alquilada**: Descripción + proveedor + costo manual
- Etiqueta visual (🏠 Propia / 🏢 Alquilada)

#### 👷 Mano de Obra:
- Descripción
- Cantidad de personas
- Horas de trabajo (opcional)
- Proveedor (opcional)
- Costo total

### ✅ Resumen de Costos:
- **Siempre visible** (incluso si es $0)
- Desglose por categoría
- Cantidad de items/personas
- Actualización en tiempo real

---

## 🔄 Flujos de Uso

### Caso 1: Siembra Rápida (Sin Recursos)
```
1. Selecciona cultivo
2. Confirma fecha
3. NO hace clic en "Agregar Recursos"
4. Ve resumen: $0
5. Confirma siembra
⏱️ 15 segundos
```

### Caso 2: Siembra con Maquinaria Propia
```
1. Selecciona cultivo
2. Clic en "Agregar Recursos"
3. Pestaña "Maquinaria"
4. Clic en "🏠 Propia"
5. Selecciona "Tractor John Deere"
6. Ingresa: 8 horas
7. Ve cálculo: 8h × $45/h = $360
8. Agrega
9. Resumen se actualiza: $360
10. Confirma siembra
⏱️ 1 minuto
```

### Caso 3: Siembra con Maquinaria Alquilada
```
1. Selecciona cultivo
2. Clic en "Agregar Recursos"
3. Pestaña "Maquinaria"
4. Clic en "🏢 Alquilada"
5. Descripción: "Sembradora"
6. Proveedor: "Maquinarias del Sur"
7. Costo: 1200
8. Agrega
9. Resumen se actualiza: $1,200
10. Confirma siembra
⏱️ 1 minuto
```

### Caso 4: Siembra Completa (Todos los Recursos)
```
1. Selecciona cultivo
2. Clic en "Agregar Recursos"

INSUMOS:
3. Semilla Soja: 127.5 kg
   → Verifica stock: ✅ Disponible: 1000 kg
   → Costo: $4,750,000

MAQUINARIA:
4. Propia - Tractor: 8h → $360
5. Alquilada - Sembradora (Maquinarias del Sur): $1,200

MANO DE OBRA:
6. Operador: 1 persona, 8h → $200

RESUMEN:
Total: $4,751,760
├─ Insumos: $4,750,000 (1 item)
├─ Maquinaria: $1,560 (2 items)
└─ M.Obra: $200 (1 persona)

7. Confirma siembra
⏱️ 3-4 minutos
```

---

## 🔧 Funcionalidades Técnicas

### Carga de Datos:
```typescript
await Promise.all([
  cargarCultivos(),      // → /api/v1/cultivos
  cargarInsumos(),       // → /api/insumos
  cargarMaquinarias()    // → /api/maquinaria (NUEVO)
]);
```

### Cálculo de Costos Maquinaria Propia:
```typescript
const horas = parseFloat(formMaquinaria.horasUso);
const maq = maquinariasInventario.find(m => m.id === maquinariaId);
const costo = horas * maq.costoPorHora;
// Ejemplo: 8 × 45 = 360
```

### Validación de Stock:
```typescript
if (insumo.stockActual < cantidad) {
  alert(`Stock insuficiente...`);
  return; // No permite agregar
}
```

### Descuento de Inventario (Backend):
```java
// Después de guardar labor_insumos
inventarioService.actualizarInventarioLabor(
    laborId, 
    insumosGuardados,  // Lista de LaborInsumo
    null,              // Sin anteriores (es creación)
    usuario
);

// InventarioService descuenta:
insumo.setStockActual(stockActual - cantidadUsada);
// Registra movimiento: SALIDA
```

---

## 📊 Comparación Final

| Aspecto | Antes | Ahora |
|---------|-------|-------|
| **Maquinaria Propia** | Texto libre | ✅ Selector del inventario |
| **Cálculo Costo Maq.** | Manual | ✅ Automático (horas × $/h) |
| **Maquinaria Alquilada** | Genérico | ✅ Con proveedor requerido |
| **Etiqueta Visual** | No tenía | ✅ 🏠 Propia / 🏢 Alquilada |
| **Stock Insumos** | No verificaba | ✅ Valida antes de agregar |
| **Descuento Inventario** | No funcionaba | ✅ Automático al sembrar |
| **Resumen Costos** | En sección | ✅ SIEMPRE visible |
| **Desglose** | No tenía | ✅ Por categoría + cantidad |
| **Vista sin recursos** | No mostraba nada | ✅ Muestra $0 |

---

## 🚀 Para Ver los Cambios

### Backend:
El backend ya está corriendo con los cambios aplicados (descuento de inventario).

### Frontend:
Vite debería haber detectado los cambios automáticamente.

### Pasos:
1. **Refresca** el navegador: Ctrl + Shift + R
2. **Ve a "Lotes"**
3. **Clic en "🌱 Sembrar"** en un lote DISPONIBLE
4. **Verifica:**
   - ✅ Combo de cultivos tiene opciones
   - ✅ Panel informativo del cultivo
   - ✅ Botón "📦 Agregar Recursos"
5. **Si expandis recursos:**
   - ✅ Pestaña Maquinaria tiene botones "🏠 Propia" / "🏢 Alquilada"
   - ✅ Al seleccionar Propia: Muestra selector del inventario
   - ✅ Al ingresar horas: Calcula costo automáticamente
   - ✅ Resumen de costos siempre visible abajo

---

## ✅ Checklist de Verificación

### Maquinaria Propia:
- [ ] Botón "🏠 Propia" disponible
- [ ] Muestra selector con maquinarias del inventario
- [ ] Selector muestra: Nombre - Tipo ($XX/h)
- [ ] Al seleccionar maquinaria, pide horas
- [ ] Al ingresar horas, calcula costo automáticamente
- [ ] Mensaje muestra: "8h × $45/h = $360"
- [ ] Al agregar, muestra etiqueta "🏠 Propia"
- [ ] NO pide proveedor

### Maquinaria Alquilada:
- [ ] Botón "🏢 Alquilada" disponible
- [ ] Muestra campos de texto
- [ ] Requiere: Descripción, Proveedor, Costo
- [ ] Al agregar, muestra etiqueta "🏢 Alquilada"
- [ ] Muestra proveedor en la lista

### Insumos:
- [ ] Si insumo tiene stock < cantidad requerida
- [ ] Muestra alerta de stock insuficiente
- [ ] NO permite agregar el insumo
- [ ] Mensaje claro con disponible vs requerido

### Resumen:
- [ ] Resumen SIEMPRE visible (incluso con $0)
- [ ] Muestra costo total en grande
- [ ] Muestra desglose: Insumos, Maquinaria, M.Obra
- [ ] Muestra cantidad de items por categoría
- [ ] Se actualiza en tiempo real al agregar/quitar

### Inventario (después de sembrar):
- [ ] Stock de insumos usado se descuenta
- [ ] Movimiento registrado en historial
- [ ] Stock actual actualizado correctamente

---

## 📄 Archivos Modificados

### Frontend:
- ✅ `SiembraModalHibrido.tsx`
  - Carga de maquinarias del inventario
  - Toggle Propia/Alquilada
  - Formularios adaptativos
  - Cálculo automático costos maquinaria propia
  - Validación de stock insumos
  - Resumen de costos siempre visible
  - Mejoras visuales en listas

### Backend:
- ✅ `SiembraService.java`
  - Integración con InventarioService
  - Descuento automático de stock
- ✅ `SiembraRequest.java`
  - Densidad opcional
- ✅ `EstadoLote.java`
  - Estados unificados para cosecha

---

## 🎉 Resultado Final

**Modal ultra-completo que:**
1. ✅ Es rápido si no tienes datos (15 seg)
2. ✅ Es completo si tienes información (3-4 min)
3. ✅ Maneja maquinaria propia del inventario
4. ✅ Maneja maquinaria alquilada con proveedor
5. ✅ Valida stock de insumos
6. ✅ Descuenta inventario automáticamente
7. ✅ Calcula costos en tiempo real
8. ✅ Muestra resumen siempre visible
9. ✅ Registra movimientos de inventario
10. ✅ Previene errores de stock insuficiente

---

**Refresca el navegador y prueba todas las funcionalidades.** 🌱

**¿Funciona como esperabas?** 🚀

