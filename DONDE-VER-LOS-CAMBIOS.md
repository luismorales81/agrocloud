# 👀 Dónde Ver los Cambios - Guía Visual

## 🎯 Para Usuario: Admin Empresa

Con tu perfil **Admin Empresa**, deberías ver **TODO** lo siguiente:

---

## 📍 Cambio 1: Nueva Tarjeta en el Dashboard

### **Ubicación:**
- Inicia sesión en el sistema
- Verás el **Dashboard Principal**
- Busca en la **primera fila de tarjetas**

### **Cómo se ve:**
```
┌─────────────────────────────────────────────────────────────┐
│  Dashboard Principal                                        │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐    │
│  │   🏞️         │  │   📦         │  │   🌾         │    │
│  │   Campos     │  │   Lotes      │  │   Cultivos   │    │
│  └──────────────┘  └──────────────┘  └──────────────┘    │
│                                                             │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐    │
│  │   🧪         │  │   🆕 🧪      │  │   🚜         │    │
│  │   Insumos    │  │   Aplicaciones│  │   Maquinaria│    │
│  │              │  │   Agroquímicos│  │              │    │
│  │              │  │   [NUEVA]    │  │              │    │
│  └──────────────┘  └──────────────┘  └──────────────┘    │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### **Detalles de la tarjeta:**
- **Icono**: 🧪 (mismo que Insumos)
- **Título**: "Aplicaciones Agroquímicos"
- **Subtítulo**: "Gestión inteligente"
- **Descripción**: "Registro con cálculo automático de dosis"

---

## 📍 Cambio 2: Nueva Sección en Insumos

### **Ubicación:**
1. Haz clic en la tarjeta **"Insumos"**
2. Haz clic en el botón **"➕ Nuevo Insumo"**
3. Desplázate hacia abajo en el formulario

### **Cómo se ve:**
```
┌─────────────────────────────────────────────────────────────┐
│  Formulario: Nuevo Insumo                                   │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  Nombre: [________________]                                 │
│  Categoría: [Herbicidas ▼]                                 │
│  Unidad: [Litro ▼]                                         │
│  Precio: [________]                                         │
│  Stock: [________]                                          │
│  Proveedor: [______________]                                │
│  Fecha Venc: [________]                                     │
│  Estado: [Activo ▼]                                        │
│                                                             │
│  Descripción:                                               │
│  ┌─────────────────────────────────────────────────────┐  │
│  │                                                     │  │
│  │                                                     │  │
│  └─────────────────────────────────────────────────────┘  │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐  │
│  │ 🧪 Dosis de Aplicación Sugeridas (Opcional)        │  │
│  │                                        [➕ Configurar Dosis] │
│  ├─────────────────────────────────────────────────────┤  │
│  │                                                     │  │
│  │  Configure las dosis sugeridas para este insumo    │  │
│  │  según el tipo de aplicación. Esto facilitará el   │  │
│  │  cálculo automático de cantidades al crear         │  │
│  │  aplicaciones.                                      │  │
│  │                                                     │  │
│  │  [No hay dosis configuradas]                       │  │
│  │                                                     │  │
│  │          [➕ Agregar Primera Dosis]                │  │
│  │                                                     │  │
│  └─────────────────────────────────────────────────────┘  │
│                                                             │
│  [💾 Guardar]  [❌ Cancelar]                              │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### **Detalles de la sección:**
- **Título**: "🧪 Dosis de Aplicación Sugeridas (Opcional)"
- **Botón**: "➕ Configurar Dosis" (para mostrar/ocultar)
- **Contenido**: Formulario para agregar dosis

---

## 📍 Cambio 3: Nueva Página de Aplicaciones

### **Ubicación:**
1. Haz clic en la tarjeta **"Aplicaciones Agroquímicos"**
2. Se abrirá una página completamente nueva

### **Cómo se ve:**
```
┌─────────────────────────────────────────────────────────────┐
│  🧪 Gestión de Aplicaciones de Agroquímicos                │
│  Registre y gestione las aplicaciones de agroquímicos       │
│  con cálculo automático de dosis                            │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌─────────────────────────────────────────────────────┐  │
│  │  📊 Resumen                                         │  │
│  ├─────────────────────────────────────────────────────┤  │
│  │  Total Aplicaciones    Insumos Utilizados           │  │
│  │       0                     0                       │  │
│  │                                                      │  │
│  │  Cantidad Total Aplicada                            │  │
│  │       0.00                                           │  │
│  └─────────────────────────────────────────────────────┘  │
│                                                             │
│  [➕ Nueva Aplicación]                                     │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐  │
│  │  📝 Nueva Aplicación de Agroquímico                 │  │
│  ├─────────────────────────────────────────────────────┤  │
│  │  Labor: [Seleccionar labor ▼]                      │  │
│  │  Insumo: [Seleccionar insumo ▼]                    │  │
│  │  Tipo: [Foliar ▼]                                  │  │
│  │  Superficie: [0.00] ha                              │  │
│  │  Dosis: [0.00] por ha                               │  │
│  │  Cantidad Total: [0.00]                             │  │
│  │  Fecha: [2025-01-20]                                │  │
│  │                                                      │  │
│  │  Observaciones:                                     │  │
│  │  ┌──────────────────────────────────────────────┐  │  │
│  │  │                                              │  │  │
│  │  └──────────────────────────────────────────────┘  │  │
│  │                                                      │  │
│  │  [💾 Guardar Aplicación]  [❌ Cancelar]            │  │
│  └─────────────────────────────────────────────────────┘  │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐  │
│  │  Buscar: [________________]                         │  │
│  │  Filtrar: [Todos los insumos ▼]                    │  │
│  └─────────────────────────────────────────────────────┘  │
│                                                             │
│  📋 Aplicaciones Registradas                               │
│  ┌─────────────────────────────────────────────────────┐  │
│  │  📭                                                 │  │
│  │  No hay aplicaciones registradas                    │  │
│  └─────────────────────────────────────────────────────┘  │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

## 🔍 Pasos para Verificar

### **Paso 1: Verifica que el Frontend esté Corriendo**

1. Abre la terminal donde ejecutaste `npm run dev`
2. Deberías ver algo como:
   ```
   VITE v5.x.x  ready in xxx ms
   
   ➜  Local:   http://localhost:5173/
   ➜  Network: use --host to expose
   ```

### **Paso 2: Abre el Navegador**

1. Abre tu navegador
2. Ve a `http://localhost:5173` (o la URL que te mostró)
3. **Importante**: Presiona **Ctrl + Shift + R** para limpiar la caché

### **Paso 3: Inicia Sesión**

1. Ingresa con tu usuario **Admin Empresa**
2. Verás el Dashboard

### **Paso 4: Busca la Nueva Tarjeta**

1. En el Dashboard, busca en la **primera fila de tarjetas**
2. Deberías ver:
   - Insumos (🧪)
   - **Aplicaciones Agroquímicos** (🧪) ← NUEVA
   - Maquinaria (🚜)

---

## ❌ Si Aún No Ves los Cambios

### **Problema 1: El frontend no se reinició**

**Solución:**
```bash
# Detén el frontend (Ctrl+C)
# Luego reinícialo:
cd agrogestion-frontend
npm run dev
```

### **Problema 2: El navegador tiene caché**

**Solución:**
1. Presiona **Ctrl + Shift + R** (limpiar caché y recargar)
2. O abre una ventana de incógnito

### **Problema 3: Hay errores en la consola**

**Solución:**
1. Abre las **Herramientas de Desarrollador** (F12)
2. Ve a la pestaña **Console**
3. Busca errores en rojo
4. Copia los errores y envíamelos

### **Problema 4: El puerto está ocupado**

**Solución:**
```bash
# En Windows, busca el proceso:
netstat -ano | findstr :5173

# Mata el proceso (reemplaza PID con el número):
taskkill /PID <PID> /F

# Luego reinicia el frontend
```

---

## 🎯 Checklist de Verificación

- [ ] Frontend corriendo (puerto 5173)
- [ ] Navegador abierto en http://localhost:5173
- [ ] Caché limpiada (Ctrl + Shift + R)
- [ ] Sesión iniciada como Admin Empresa
- [ ] Dashboard visible
- [ ] Tarjeta "Aplicaciones Agroquímicos" visible

---

## 📸 Captura de Pantalla Esperada

```
Dashboard Principal
├── Campos (🏞️)
├── Lotes (📦)
├── Cultivos (🌾)
├── Cosechas (🌽)
├── Insumos (🧪)
├── 🆕 Aplicaciones Agroquímicos (🧪) ← ESTA ES LA NUEVA
├── Maquinaria (🚜)
├── Labores (🔧)
├── Finanzas (💰)
└── Reportes (📊)
```

---

## 🆘 ¿Necesitas Ayuda?

Si después de seguir todos estos pasos aún no ves los cambios:

1. **Abre la consola del navegador** (F12)
2. **Copia cualquier error** que veas
3. **Envíame**:
   - El error de la consola
   - Tu perfil de usuario
   - Una captura de pantalla del Dashboard

---

¿Ves la tarjeta "Aplicaciones Agroquímicos" ahora?











