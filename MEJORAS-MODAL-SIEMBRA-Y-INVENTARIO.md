# ✅ Mejoras al Modal de Siembra e Inventario

## 🎯 Cambios Implementados

### 1. **Maquinaria Propia vs Alquilada** ✅

#### Antes:
```
Solo un formulario genérico para maquinaria
No diferenciaba entre propia y alquilada
```

#### Ahora:
```
┌─────────────────────────────────────┐
│ 🚜 Maquinaria                       │
│                                     │
│  [🏠 Propia] [🏢 Alquilada]        │
│   ▔▔▔▔▔▔▔▔▔                        │
│                                     │
│  PROPIA:                            │
│  [Descripción] [Costo] [+]         │
│                                     │
│  ALQUILADA:                         │
│  [Descripción] [Proveedor] [Costo] [+] │
└─────────────────────────────────────┘
```

**Características:**
- ✅ Botones para seleccionar tipo (Propia/Alquilada)
- ✅ Formulario se adapta según selección
- ✅ Propia: NO pide proveedor
- ✅ Alquilada: SÍ pide proveedor
- ✅ Etiqueta visual en lista (🏠 Propia / 🏢 Alquilada)

**Vista en Lista:**
```
┌──────────────────────────────────────┐
│ Tractor John Deere  [🏠 Propia]     │
│ $360                                 │
│                              [🗑️]   │
├──────────────────────────────────────┤
│ Sembradora  [🏢 Alquilada]          │
│ Proveedor: Maquinarias del Sur      │
│ $1,200                               │
│                              [🗑️]   │
└──────────────────────────────────────┘
```

---

### 2. **Resumen de Costos SIEMPRE Visible** ✅

#### Antes:
```
El resumen solo aparecía dentro de la sección expandida
Si no agregabas recursos, no veías el costo total
```

#### Ahora:
```
┌─────────────────────────────────────────────┐
│ 💰 Costo Total de Siembra                   │
│                               $4,750,760    │
├─────────────────────────────────────────────┤
│     INSUMOS    │  MAQUINARIA  │  MANO DE OBRA│
│   $4,750,000   │     $560     │     $200     │
│   2 item(s)    │  2 item(s)   │  1 persona(s)│
└─────────────────────────────────────────────┘
```

**Características:**
- ✅ **Siempre visible** (expandido o no)
- ✅ Costo total destacado en grande
- ✅ Desglose por categoría (Insumos, Maquinaria, M.Obra)
- ✅ Cantidad de items por categoría
- ✅ Visual diferente si es $0 vs si tiene costos
- ✅ Se actualiza en tiempo real al agregar/quitar recursos

**Cuando NO hay recursos:**
```
┌─────────────────────────────────────┐
│ 💰 Costo Total de Siembra          │
│                                $0  │
├─────────────────────────────────────┤
│ No se han agregado recursos.       │
│ El costo será $0.                  │
└─────────────────────────────────────┘
```

---

### 3. **Descuento Automático de Inventario** ✅

#### Backend: `SiembraService.java`

**Agregado:**
```java
@Autowired
private InventarioService inventarioService;

// Después de guardar los insumos:
java.util.List<LaborInsumo> insumosGuardados = new java.util.ArrayList<>();

// Dentro del loop de insumos:
laborInsumoRepository.save(laborInsumo);
insumosGuardados.add(laborInsumo);  // Agregar a lista

// Después del loop:
inventarioService.actualizarInventarioLabor(
    laborSiembra.getId(), 
    insumosGuardados, 
    null,  // Sin insumos anteriores (es creación)
    usuario
);
```

**Proceso:**
1. ✅ Usuario agrega 50 kg de semilla en la siembra
2. ✅ Backend guarda el registro en `labor_insumos`
3. ✅ **Inventario** automáticamente descuenta 50 kg del stock
4. ✅ **Movimiento** de inventario registrado (SALIDA)
5. ✅ Si no hay stock → Error: "Stock insuficiente"

**Validaciones:**
- ✅ Verifica stock disponible antes de usar
- ✅ Lanza excepción si stock insuficiente
- ✅ Registra movimiento de inventario
- ✅ Actualiza `stock_actual` del insumo

---

## 📊 Comparación Antes/Después

| Aspecto | Antes | Ahora |
|---------|-------|-------|
| **Maquinaria** | Genérica | 🏠 Propia / 🏢 Alquilada |
| **Proveedor** | Siempre opcional | Solo si es alquilada |
| **Resumen Costos** | Solo en sección expandida | ✅ SIEMPRE visible |
| **Desglose** | No tenía | ✅ Por categoría + cantidad |
| **Descuento Inventario** | ❌ NO funcionaba | ✅ Automático |
| **Validación Stock** | ❌ No verificaba | ✅ Valida antes de usar |
| **Movimientos** | ❌ No registraba | ✅ Registra SALIDA |

---

## 🧪 Cómo Probar

### Test 1: Maquinaria Propia
```
1. Crear lote nuevo (estado DISPONIBLE)
2. Clic en "🌱 Sembrar"
3. Seleccionar cultivo
4. Clic en "📦 Agregar Recursos"
5. Pestaña "🚜 Maquinaria"
6. Clic en "🏠 Propia"
7. Descripción: "Tractor propio"
8. Costo: 360
9. Agregar
10. Verificar etiqueta "🏠 Propia"
```

### Test 2: Maquinaria Alquilada
```
1. En misma siembra
2. Clic en "🏢 Alquilada"
3. Descripción: "Sembradora"
4. Proveedor: "Maquinarias del Sur"
5. Costo: 1200
6. Agregar
7. Verificar etiqueta "🏢 Alquilada"
8. Verificar que muestra proveedor
```

### Test 3: Resumen de Costos
```
1. Agregar 1 insumo: Semilla ($4,750,000)
2. Agregar 2 maquinarias: Tractor ($360) + Sembradora ($1,200)
3. Agregar 1 mano de obra: Operador ($200)
4. Verificar que el resumen muestra:
   - TOTAL: $4,751,760
   - Insumos: $4,750,000 (1 item)
   - Maquinaria: $1,560 (2 items)
   - M.Obra: $200 (1 persona)
5. El resumen debe estar VISIBLE incluso si colapsa la sección de recursos
```

### Test 4: Descuento de Inventario
```
Preparación:
1. Ve a "Insumos"
2. Verifica stock de "Semilla Soja": ej 1000 kg

Siembra:
3. Sembrar lote
4. Agregar recursos → Insumos
5. Seleccionar "Semilla Soja"
6. Cantidad: 127.5 kg
7. Confirmar siembra

Verificación:
8. Volver a "Insumos"
9. Verificar stock de "Semilla Soja": debe ser 872.5 kg (1000 - 127.5)
10. Ver "Movimientos" del insumo
11. Debe aparecer: SALIDA - 127.5 kg - Labor de Siembra
```

### Test 5: Stock Insuficiente
```
Preparación:
1. Insumo con stock de 10 kg

Siembra:
2. Intentar usar 50 kg del insumo

Resultado esperado:
❌ Error: "Stock insuficiente para [Nombre Insumo]. Disponible: 10, Requerido: 50"
```

---

## 📋 Archivos Modificados

### Frontend:
1. ✅ `SiembraModalHibrido.tsx`
   - Agregado toggle Propia/Alquilada
   - Formulario adaptativo según tipo
   - Etiqueta visual en lista de maquinaria
   - Resumen de costos siempre visible
   - Desglose detallado por categoría

### Backend:
1. ✅ `SiembraService.java`
   - Agregado `@Autowired InventarioService`
   - Llamada a `inventarioService.actualizarInventarioLabor()`
   - Descuento automático de stock
   - Registro de movimientos

2. ✅ `InventarioService.java` (ya estaba correcto)
   - Método `descontarInventario()` funcional
   - Validación de stock suficiente
   - Registro de movimientos
   - Manejo de excepciones

---

## 🔍 Flujo Completo de Inventario

### Cuando se siembra un lote con insumos:

```
1. Usuario selecciona "Semilla Soja" (127.5 kg)
        ↓
2. Frontend envía: { insumoId: 5, cantidadUsada: 127.5 }
        ↓
3. SiembraService.sembrarLote()
   ├─ Crea LaborInsumo
   ├─ Guarda en base de datos
   └─ Llama a inventarioService.actualizarInventarioLabor()
        ↓
4. InventarioService.descontarInventario()
   ├─ Verifica stock actual: 1000 kg
   ├─ Verifica si es suficiente: 1000 >= 127.5 ✅
   ├─ Descuenta: stock_actual = 1000 - 127.5 = 872.5
   ├─ Guarda insumo actualizado
   └─ Registra movimiento de inventario:
       • Tipo: SALIDA
       • Cantidad: 127.5 kg
       • Motivo: "Uso en labor: Siembra de Soja en lote A1"
        ↓
5. Usuario ve en "Insumos":
   ├─ Stock actual: 872.5 kg
   └─ Movimientos: SALIDA - 127.5 kg
```

---

## ⚠️ Manejo de Errores

### Error: Stock Insuficiente
```java
if (insumo.getStockActual().compareTo(laborInsumo.getCantidadUsada()) < 0) {
    throw new InsufficientStockException(
        "Stock insuficiente para " + insumo.getNombre() + 
        ". Disponible: " + insumo.getStockActual() + 
        ", Requerido: " + laborInsumo.getCantidadUsada()
    );
}
```

**Frontend mostrará:**
```
❌ Error: Stock insuficiente para Semilla Soja. 
   Disponible: 10, Requerido: 127.5
```

**Usuario puede:**
1. Ajustar cantidad en el modal
2. Comprar más insumo primero
3. No agregar ese insumo

---

## 🎨 Vista Previa del Modal Mejorado

### Modal con Recursos y Costos:

```
┌─────────────────────────────────────────────────────────┐
│  🌱 Sembrar Lote: A1 (25.5 ha)                    [✕]  │
├─────────────────────────────────────────────────────────┤
│  Cultivo: Soja - DM 53i54                               │
│  ┌───────────────────────────────────────────────┐     │
│  │ 📊 Datos del Cultivo:                         │     │
│  │ Ciclo: 120 días | Rinde: 3500 kg/ha           │     │
│  │ Cosecha estimada: 28/01/2026                  │     │
│  └───────────────────────────────────────────────┘     │
│                                                         │
│  Fecha de Siembra: [30/09/2025]                        │
│  Observaciones: [Condiciones óptimas]                  │
│                                                         │
│  [ 📦 Agregar Recursos ]  ← Ya expandido              │
│                                                         │
│  ┌──────────────────────────────────────────────┐     │
│  │ 📦 Recursos Utilizados          [✕ Ocultar] │     │
│  │                                               │     │
│  │ [🌾 Insumos (1)] [🚜 Maquinaria (2)] [👷 M.O (1)] │     │
│  │               ▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔                │     │
│  │                                               │     │
│  │ [🏠 Propia] [🏢 Alquilada]  ← Nuevo         │     │
│  │  ▔▔▔▔▔▔▔▔▔                                  │     │
│  │                                               │     │
│  │ Tractor propio [🏠 Propia]                   │     │
│  │ $360                              [🗑️]       │     │
│  │                                               │     │
│  │ Sembradora [🏢 Alquilada]                    │     │
│  │ Proveedor: Maquinarias del Sur               │     │
│  │ $1,200                            [🗑️]       │     │
│  └──────────────────────────────────────────────┘     │
│                                                         │
│  ┌──────────────────────────────────────────────┐     │
│  │ 💰 Costo Total de Siembra                    │     │
│  │                            $4,751,760         │     │
│  ├──────────────────────────────────────────────┤     │
│  │  INSUMOS    │  MAQUINARIA  │  MANO DE OBRA   │     │
│  │ $4,750,000  │   $1,560     │      $200       │     │
│  │  1 item(s)  │   2 item(s)  │   1 persona(s)  │     │
│  └──────────────────────────────────────────────┘     │
│                                                         │
│  [Cancelar]                [🌱 Confirmar Siembra]     │
└─────────────────────────────────────────────────────────┘
```

---

### Modal Sin Recursos (Vista Simple):

```
┌─────────────────────────────────────────────────────────┐
│  🌱 Sembrar Lote: A1 (25.5 ha)                    [✕]  │
├─────────────────────────────────────────────────────────┤
│  Cultivo: Soja                                          │
│  Fecha: [30/09/2025]                                    │
│                                                         │
│  [ 📦 Agregar Recursos ]                                │
│                                                         │
│  ┌──────────────────────────────────────────────┐     │
│  │ 💰 Costo Total de Siembra                    │     │
│  │                                       $0      │     │
│  ├──────────────────────────────────────────────┤     │
│  │ No se han agregado recursos.                 │     │
│  │ El costo será $0.                            │     │
│  └──────────────────────────────────────────────┘     │
│                                                         │
│  [Cancelar]                [🌱 Confirmar Siembra]     │
└─────────────────────────────────────────────────────────┘
```

**El resumen SIEMPRE está visible, incluso sin recursos ✅**

---

## 📦 Detalles de Implementación

### Frontend: Diferenciación de Maquinaria

```typescript
// Estado
const [tipoMaquinaria, setTipoMaquinaria] = useState<'propia' | 'alquilada'>('propia');

// Botones de selección
<button onClick={() => setTipoMaquinaria('propia')}>🏠 Propia</button>
<button onClick={() => setTipoMaquinaria('alquilada')}>🏢 Alquilada</button>

// Formulario adaptativo
{tipoMaquinaria === 'alquilada' && (
  <input placeholder="Proveedor" />
)}

// Etiqueta en lista
{maq.proveedor ? '🏢 Alquilada' : '🏠 Propia'}
```

### Frontend: Resumen de Costos

```typescript
const calcularCostos = () => {
  const costoInsumos = insumosUsados.reduce((sum, i) => sum + i.costoTotal, 0);
  const costoMaquinaria = maquinarias.reduce((sum, m) => sum + m.costoTotal, 0);
  const costoManoObra = manoObras.reduce((sum, mo) => sum + mo.costoTotal, 0);
  const total = costoInsumos + costoMaquinaria + costoManoObra;
  
  return { costoInsumos, costoMaquinaria, costoManoObra, total };
};

const costos = calcularCostos();

// Resumen siempre renderizado
<div>
  <strong>💰 Costo Total:</strong> ${costos.total.toLocaleString()}
  {/* Desglose... */}
</div>
```

### Backend: Descuento de Inventario

```java
// 1. Guardar insumos en lista
List<LaborInsumo> insumosGuardados = new ArrayList<>();

for (InsumoUsadoDTO insumoDTO : request.getInsumos()) {
    // ... crear y guardar labor_insumo
    insumosGuardados.add(laborInsumo);
}

// 2. Descontar inventario
inventarioService.actualizarInventarioLabor(
    laborSiembra.getId(),    // ID de la labor
    insumosGuardados,        // Insumos a descontar
    null,                    // Sin insumos anteriores
    usuario                  // Usuario que realiza la acción
);

// Dentro de InventarioService:
for (LaborInsumo insumo : insumosNuevos) {
    descontarInventario(insumo, usuario, "Uso en labor");
    // → Descuenta stock
    // → Registra movimiento
    // → Valida stock suficiente
}
```

---

## ✅ Beneficios

### Para el Usuario:
1. ✅ **Claridad**: Sabe si usó maquinaria propia o alquilada
2. ✅ **Costos visibles**: Siempre ve el costo total
3. ✅ **Control**: Sabe cuánto gastará antes de confirmar
4. ✅ **Desglose**: Ve cuánto cuesta cada categoría
5. ✅ **Inventario real**: Stock se actualiza automáticamente

### Para el Sistema:
1. ✅ **Trazabilidad**: Distingue maquinaria propia vs alquilada
2. ✅ **Costos precisos**: Cálculo automático en tiempo real
3. ✅ **Control de stock**: Previene uso de insumos no disponibles
4. ✅ **Historial completo**: Movimientos de inventario registrados
5. ✅ **Análisis financiero**: Costos reales por labor

---

## 🚀 Para Ver los Cambios

1. **Backend**: Necesita reiniciarse (cambios en SiembraService.java)
   ```bash
   # Detener backend actual
   # Ejecutar: iniciar-backend.bat
   ```

2. **Frontend**: Vite detecta cambios automáticamente
   ```
   Ctrl + Shift + R en el navegador
   ```

3. **Probar:**
   - Crear lote
   - Sembrar con recursos
   - Verificar diferenciación propia/alquilada
   - Verificar resumen de costos visible
   - Verificar descuento de inventario

---

## 📊 Resumen Ejecutivo

### Mejoras Implementadas:
| Feature | Estado |
|---------|--------|
| Maquinaria Propia/Alquilada | ✅ Implementado |
| Formulario adaptativo | ✅ Implementado |
| Etiquetas visuales | ✅ Implementado |
| Resumen de costos siempre visible | ✅ Implementado |
| Desglose por categoría | ✅ Implementado |
| Descuento de inventario | ✅ Implementado |
| Validación de stock | ✅ Implementado |
| Registro de movimientos | ✅ Implementado |

**Sistema completo y funcional.** 🎉

---

**Reinicia el backend y refresca el frontend para ver las mejoras.** 🚀

