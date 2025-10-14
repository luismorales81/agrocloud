# 🔄 Instrucciones para Ver los Cambios en el Frontend

## ✅ Estado Actual
- ✅ Backend corriendo en: `http://localhost:8080`
- ✅ Frontend corriendo en: `http://localhost:3000`
- ✅ Código actualizado con modales simplificados

## ⚠️ Problema
Tu navegador tiene la **versión antigua en caché**. Por eso no ves los botones de "🌱 Sembrar" y "🌾 Cosechar".

---

## 🚀 SOLUCIÓN: Limpiar Caché del Navegador

### Opción 1: Recarga Forzada (MÁS RÁPIDA) ⚡

1. **Abre el navegador** en `http://localhost:3000`
2. **Abre las DevTools**:
   - Presiona `F12` o
   - Clic derecho → "Inspeccionar"
3. **Clic derecho en el botón de recargar** (🔄) del navegador
4. **Selecciona**: "Vaciar caché y recargar de manera forzada" o "Empty Cache and Hard Reload"

![Diagrama]
```
Clic derecho en 🔄 → "Vaciar caché y recargar de manera forzada"
```

### Opción 2: Atajo de Teclado (RÁPIDA) ⌨️

1. Ve a `http://localhost:3000`
2. Presiona **Ctrl + Shift + Delete** (o **Cmd + Shift + Delete** en Mac)
3. Selecciona **solo "Imágenes y archivos en caché"**
4. Haz clic en **"Borrar datos"**
5. Presiona **Ctrl + Shift + R** para recargar sin caché

### Opción 3: Modo Incógnito (TEMPORAL) 🕵️

1. Abre una **ventana de incógnito** (Ctrl + Shift + N)
2. Ve a `http://localhost:3000`
3. Inicia sesión normalmente
4. Deberías ver los cambios inmediatamente

### Opción 4: Limpiar TODO el caché del navegador (MÁS COMPLETA) 🧹

**Chrome/Edge:**
1. Presiona `Ctrl + Shift + Delete`
2. Selecciona "Todo el tiempo"
3. Marca: "Imágenes y archivos en caché"
4. Clic en "Borrar datos"
5. Recarga la página

**Firefox:**
1. Presiona `Ctrl + Shift + Delete`
2. Selecciona "Todo"
3. Marca: "Caché"
4. Clic en "Limpiar ahora"
5. Recarga la página

---

## 📋 Qué Deberías Ver Después de Limpiar la Caché

### 1️⃣ En la Pantalla de **LOTES**

Deberías ver una tabla con botones contextuales según el estado:

```
┌────────────────────────────────────────────────────────────┐
│ Nombre │ Superficie │ Cultivo │ Estado     │ Acciones     │
├────────────────────────────────────────────────────────────┤
│ Lote A1│ 25.5 ha    │ Soja    │ DISPONIBLE │ 🌱 Sembrar   │ ← NUEVO
│ Lote A2│ 30.0 ha    │ Maíz    │ SEMBRADO   │ 🌾 Cosechar ▾│ ← NUEVO
│ Lote B1│ 40.0 ha    │ Trigo   │ EN_COSECHA │ 📋 Historial │
└────────────────────────────────────────────────────────────┘
```

### 2️⃣ Modal de Siembra (al hacer clic en "🌱 Sembrar")

```
┌─────────────────────────────────────┐
│  🌱 Sembrar Lote                    │
├─────────────────────────────────────┤
│  Cultivo a Sembrar *                │
│  [▼ Seleccionar cultivo]            │
│                                     │
│  Fecha de Siembra *                 │
│  [30/09/2025]                       │
│                                     │
│  Densidad de Siembra *              │
│  [50000] plantas/ha                 │
│                                     │
│  Observaciones                      │
│  [...]                              │
│                                     │
│  [Cancelar]  [🌱 Confirmar Siembra] │
└─────────────────────────────────────┘
```

**SIN campos de:**
- ❌ Insumos
- ❌ Maquinaria
- ❌ Mano de obra

### 3️⃣ Modal de Cosecha (al hacer clic en "🌾 Cosechar")

```
┌─────────────────────────────────────┐
│  🌾 Cosechar Lote                   │
├─────────────────────────────────────┤
│  Fecha de Cosecha *                 │
│  [30/09/2025]                       │
│                                     │
│  Cantidad Cosechada *               │
│  [100] [▼ Toneladas]                │
│  Rendimiento estimado: 3.92 ton/ha  │
│                                     │
│  Calidad de la Cosecha *            │
│  [▼ ✅ Buena]                       │
│                                     │
│  [Cancelar] [🌾 Confirmar Cosecha]  │
└─────────────────────────────────────┘
```

**SIN campos de:**
- ❌ Humedad de cosecha (eliminado)
- ❌ Maquinaria manual
- ❌ Mano de obra manual

---

## 🔍 Cómo Verificar que Tienes la Versión Correcta

### Método 1: Inspeccionar el HTML
1. Abre DevTools (F12)
2. Ve a la pestaña "Elements" o "Elementos"
3. Busca (Ctrl+F) por el texto: `"Sembrar Lote"`
4. Si lo encuentras en el código HTML → ✅ Versión correcta
5. Si NO lo encuentras → ❌ Caché no limpiada

### Método 2: Consola del Navegador
1. Abre DevTools (F12)
2. Ve a "Console"
3. Escribe: `document.querySelector('[data-testid*="sembrar"]')`
4. Si devuelve un elemento → ✅ Versión correcta
5. Si devuelve `null` → ❌ Caché no limpiada

### Método 3: Ver el Código Fuente
1. Presiona `Ctrl+U` para ver el código fuente
2. Busca (Ctrl+F): `SiembraModal`
3. Si aparece → ✅ Versión correcta
4. Si NO aparece → ❌ Caché no limpiada

---

## ❓ Si AÚN NO ves los cambios...

### 1. Verifica que el servidor esté corriendo
```bash
# En PowerShell
netstat -ano | Select-String ":3000"
```

Deberías ver:
```
TCP    [::1]:3000    [::]:0    LISTENING    [PID]
```

### 2. Verifica la URL
- ✅ Correcta: `http://localhost:3000`
- ❌ Incorrecta: `http://localhost:8080` (esto es el backend)

### 3. Cierra TODAS las pestañas del localhost y abre una nueva
A veces el caché se mantiene en pestañas abiertas.

### 4. Reinicia completamente el frontend
```bash
# Detener el servidor (Ctrl+C en la terminal)
# Luego en PowerShell:
cd agrogestion-frontend
Remove-Item -Recurse -Force .vite
npm run dev
```

### 5. Usa otro navegador
Prueba con otro navegador (Chrome, Firefox, Edge) para verificar.

---

## 📱 Contacto/Ayuda

Si después de seguir TODOS estos pasos aún no ves los cambios:

1. **Toma una captura de pantalla** de la pantalla de Lotes
2. **Abre DevTools** (F12) → pestaña "Network"
3. **Recarga la página** (Ctrl+R)
4. **Toma una captura** de los archivos que se cargan
5. Envía las capturas para diagnóstico

---

## ✅ Lista de Verificación

Marca cada paso que completes:

- [ ] Abrí DevTools (F12)
- [ ] Hice clic derecho en recargar → "Vaciar caché y recargar de manera forzada"
- [ ] Cerré TODAS las pestañas de localhost:3000
- [ ] Abrí una nueva pestaña
- [ ] Fui a `http://localhost:3000`
- [ ] Inicié sesión
- [ ] Fui a la sección "Lotes"
- [ ] **VEO** el botón "🌱 Sembrar" en lotes con estado DISPONIBLE
- [ ] **VEO** el botón "🌾 Cosechar ▾" en lotes con estado SEMBRADO

Si marcaste TODOS los pasos y NO ves los botones, hay un problema más profundo.

---

**El código está actualizado. Solo necesitas limpiar la caché del navegador.** 🎉
