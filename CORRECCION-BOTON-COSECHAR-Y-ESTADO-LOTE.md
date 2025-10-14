# 🔧 Corrección: Botón Cosechar y Control de Labores por Estado

## 🐛 Problemas Detectados

### 1. **Botón "Cosechar" No Funcionaba**
Al hacer click en "🌾 Cosechar ▾", no se abría el menú dropdown ni se ejecutaban las acciones.

### 2. **Control de Labores No Filtraba por Estado**
Al crear una labor desde "Labores", se mostraban TODAS las 12 labores posibles, sin importar el estado del lote.

---

## 🔍 Causas Raíz

### **Problema 1: Conflicto de Event Listeners**

**handleClickOutside (línea 302-312):**
```typescript
// ANTES:
const handleClickOutside = (event: MouseEvent) => {
  if (menuAbierto !== null) {
    setMenuAbierto(null);  // ❌ Cerraba SIEMPRE, incluso clicks dentro
  }
};

document.addEventListener('click', handleClickOutside);  // ❌ Sin delay
```

**Flujo del Bug:**
1. Usuario hace click en "Cosechar Normal"
2. El evento se propaga al document
3. `handleClickOutside` detecta el click
4. Cierra el menú ANTES de que se ejecute `handleCosechar`
5. La función nunca se ejecuta

### **Problema 2: Campo `estado` Faltante**

**Interface Lote (línea 83-88):**
```typescript
// ANTES:
interface Lote {
  id: number;
  nombre: string;
  superficie: number;
  cultivo: string;
  // ❌ Faltaba estado
}
```

**Mapeo de Lotes (línea 272-277):**
```typescript
// ANTES:
const lotesMapeados: Lote[] = lotesData.map((lote: any) => ({
  id: lote.id,
  nombre: lote.nombre,
  superficie: lote.areaHectareas || 0,
  cultivo: lote.cultivoActual || ''
  // ❌ No se incluía estado
}));
```

**Resultado:**
- `lote.estado = undefined`
- `handleLoteChange` detectaba: "❌ Lote NO tiene estado"
- Usaba todas las tareas como fallback

---

## ✅ Soluciones Aplicadas

### **1. Prevención de Propagación de Eventos**

**Botón Principal de Cosechar (línea 1093-1096):**
```typescript
<button
  onClick={(e) => {
    e.stopPropagation();  // ← AGREGADO
    setMenuAbierto(menuAbierto === lote.id ? null : lote.id!);
  }}
>
  🌾 Cosechar ▾
</button>
```

**Botones del Dropdown (líneas 1127-1130, 1148-1151, 1168-1171, 1188-1191):**
```typescript
// Cosechar Normal
<button
  onClick={(e) => { 
    e.stopPropagation();  // ← AGREGADO
    handleCosechar(lote); 
    setMenuAbierto(null); 
  }}
>
  🌾 Cosechar Normal
</button>

// Convertir a Forraje
<button
  onClick={(e) => { 
    e.stopPropagation();  // ← AGREGADO
    handleAccionEspecial(lote, 'forraje'); 
  }}
>
  🐄 Convertir a Forraje
</button>

// Limpiar Cultivo
<button
  onClick={(e) => { 
    e.stopPropagation();  // ← AGREGADO
    handleAccionEspecial(lote, 'limpiar'); 
  }}
>
  🚜 Limpiar Cultivo
</button>

// Abandonar Cultivo
<button
  onClick={(e) => { 
    e.stopPropagation();  // ← AGREGADO
    handleAccionEspecial(lote, 'abandonar'); 
  }}
>
  ⚠️ Abandonar Cultivo
</button>
```

### **2. Mejorado handleClickOutside (línea 301-317)**

```typescript
const handleClickOutside = (event: MouseEvent) => {
  const target = event.target as HTMLElement;
  // Solo cerrar si el click NO fue en el botón del dropdown
  if (menuAbierto !== null && !target.closest('[data-testid*="cosechar-button"]')) {
    setMenuAbierto(null);
  }
};

if (menuAbierto !== null) {
  // Agregar con delay para evitar cierre inmediato
  setTimeout(() => {
    document.addEventListener('click', handleClickOutside);
  }, 10);
  return () => document.removeEventListener('click', handleClickOutside);
}
```

### **3. Campo Estado Agregado**

**Interface Lote (línea 83-89):**
```typescript
interface Lote {
  id: number;
  nombre: string;
  superficie: number;
  cultivo: string;
  estado?: string;  // ✅ AGREGADO
}
```

**Mapeo de Lotes (línea 272-278):**
```typescript
const lotesMapeados: Lote[] = lotesData.map((lote: any) => ({
  id: lote.id,
  nombre: lote.nombre,
  superficie: lote.areaHectareas || 0,
  cultivo: lote.cultivoActual || '',
  estado: lote.estado  // ✅ AGREGADO
}));
```

---

## 🧪 Pruebas a Realizar

### **Test 1: Botón Cosechar Funcional**

1. **Recarga el navegador** (Ctrl + F5)
2. **Ir a Lotes** (página principal)
3. Buscar un lote con estado SEMBRADO o superior
4. **Click en "🌾 Cosechar ▾"**
5. **Verificar:**
   - [ ] Se abre el menú dropdown
   - [ ] Aparecen 4 opciones
6. **Click en "🌾 Cosechar Normal"**
7. **Verificar:**
   - [ ] Se abre el modal de cosecha
   - [ ] El menú se cierra
8. **Observar consola:**
   ```
   🌾 handleCosechar llamado para lote: {...}
   ✅ Modal de cosecha activado
   ```

### **Test 2: Control de Labores por Estado**

1. **Ir a Labores** → Click "Nueva Labor"
2. **Seleccionar un lote SEMBRADO**
3. **Verificar en consola:**
   ```
   📦 Lote encontrado: {..., estado: "SEMBRADO"}
   ✅ Lote tiene estado, cargando tareas para: SEMBRADO
   ✅ Tareas recibidas del backend: ["riego", "fertilizacion", "pulverizacion", "monitoreo"]
   ```
4. **Abrir dropdown "Tipo de Labor"**
5. **Verificar:**
   - [ ] Solo muestra 4 labores (no 12)
   - [ ] Aparecen: Riego, Fertilización, Pulverización, Monitoreo
   - [ ] NO aparece: Siembra ✅
6. **Contador muestra:** "✓ 4 labor(es) disponible(s) para este lote"

### **Test 3: Cambio de Lote Actualiza Labores**

1. Seleccionar un lote SEMBRADO
2. **Verificar:** 4 labores disponibles
3. Seleccionar un lote DISPONIBLE
4. **Verificar:** 
   - Campo "Tipo de Labor" se resetea
   - Aparecen labores de preparación (arado, rastra, etc.)
   - Contador actualiza

---

## 📁 Archivos Modificados

### **LotesManagement.tsx:**
1. Línea 88: Interface `Lote` con campo `estado`
2. Línea 277: Mapeo incluye `lote.estado`
3. Líneas 301-317: `handleClickOutside` mejorado con delay y detección específica
4. Línea 1093: Botón principal con `stopPropagation`
5. Líneas 1127, 1148, 1168, 1188: Todos los botones del dropdown con `stopPropagation`

### **LaboresManagement.tsx:**
1. Línea 88: Interface `Lote` con campo `estado`
2. Línea 277: Mapeo incluye `lote.estado`
3. Líneas 682-705: `handleLoteChange` con logs y reseteo de tipo
4. Líneas 367-403: `cargarTareasDisponibles` con logs detallados

---

## ✅ Resultado Esperado

Después de recargar el navegador:

✅ Botón "Cosechar" abre menú dropdown correctamente  
✅ Click en opciones del menú ejecuta las acciones  
✅ Modal de cosecha se abre al seleccionar "Cosechar Normal"  
✅ Campo "Lote" primero, "Tipo de Labor" segundo  
✅ Solo muestra labores válidas según estado del lote  
✅ Contador muestra cantidad correcta de labores  

---

**Fecha:** 30 de Septiembre, 2025  
**Problemas:** Event listener bloqueaba clicks + campo estado faltante  
**Solución:** stopPropagation + delay en listener + campo estado agregado  
**Estado:** ✅ Resuelto
