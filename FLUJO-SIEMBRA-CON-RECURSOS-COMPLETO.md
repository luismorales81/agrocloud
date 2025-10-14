# 🌱 Flujo Completo: Siembra como Labor con Recursos

## 🎯 Problema Identificado

**Usuario reporta:** "En caso de realizar la siembra es usual que se elija el cultivo pero también se debería cargar como labor, por los insumos, maquinaria y mano de obra que se usaría."

**Análisis:** ¡TIENES TODA LA RAZÓN!

La siembra NO es solo "asignar un cultivo al lote". Es una **labor agrícola completa** que:
- ✅ Consume INSUMOS (semillas, fertilizantes pre-siembra)
- ✅ Utiliza MAQUINARIA (tractores, sembradoras)
- ✅ Requiere MANO DE OBRA (operarios)
- ✅ Tiene COSTOS asociados
- ✅ Debe registrarse para calcular rentabilidad

---

## ✅ BUENAS NOTICIAS: El Backend YA lo hace

### Código en `SiembraService.java` (líneas 71-130):

```java
// 1. Crear la labor de siembra automáticamente
Labor laborSiembra = new Labor();
laborSiembra.setTipoLabor(TipoLabor.SIEMBRA);
laborSiembra.setDescripcion("Siembra de " + cultivo.getNombre() + " en lote " + lote.getNombre());
laborSiembra.setFechaInicio(request.getFechaSiembra());
laborSiembra.setFechaFin(request.getFechaSiembra());
laborSiembra.setEstado(EstadoLabor.COMPLETADA);
laborSiembra.setResponsable(usuario.getEmail());
laborSiembra.setLote(lote);
laborSiembra.setUsuario(usuario);

// 2. Procesar INSUMOS usados
if (request.getInsumos() != null) {
    for (InsumoUsadoDTO insumoDTO : request.getInsumos()) {
        // Crea LaborInsumo con semillas, fertilizantes, etc.
        // Calcula costos automáticamente
    }
}

// 3. Procesar MAQUINARIA
if (request.getMaquinaria() != null) {
    for (MaquinariaAsignadaDTO maqDTO : request.getMaquinaria()) {
        // Registra tractor, sembradora, etc.
        // Calcula costos de uso
    }
}

// 4. Procesar MANO DE OBRA
if (request.getManoObra() != null) {
    for (ManoObraDTO moDTO : request.getManoObra()) {
        // Registra operarios
        // Calcula costos laborales
    }
}

// 5. Actualizar costo total de la labor
laborSiembra.setCostoTotal(costoTotal);

// 6. Cambiar estado del lote
lote.setEstado(EstadoLote.SEMBRADO);
lote.setCultivoActual(cultivo.getNombre());
```

**EL BACKEND YA HACE TODO ESTO** ✅

---

## ❌ PROBLEMA: El Frontend envía arrays vacíos

### Código actual en `SiembraModal.tsx` (líneas 77-85):

```javascript
body: JSON.stringify({
  cultivoId: parseInt(formData.cultivoId),
  fechaSiembra: formData.fechaSiembra,
  densidadSiembra: parseFloat(formData.densidadSiembra),
  observaciones: formData.observaciones,
  insumos: [],         // ❌ VACÍO
  maquinaria: [],      // ❌ VACÍO
  manoObra: []         // ❌ VACÍO
})
```

**Resultado:** Se crea la labor, pero SIN recursos asignados → No se registran costos reales

---

## 🔧 SOLUCIONES PROPUESTAS

### Opción 1: Modal Simple + Registro Manual Posterior (ACTUAL)

#### Flujo:
```
1. Usuario hace clic en "🌱 Sembrar"
   ├─ Modal simple: solo cultivo, fecha, densidad
   └─ Se envía con arrays vacíos
   
2. Se crea Labor de Siembra SIN recursos
   ├─ Estado: COMPLETADA
   ├─ Costo total: $0
   └─ Sin insumos/maquinaria/mano de obra
   
3. Usuario DESPUÉS va a "Labores"
   ├─ Busca la labor de siembra recién creada
   ├─ La edita para agregar recursos
   └─ Actualiza costos
```

**Ventajas:**
- ✅ Siembra rápida
- ✅ No abruma al usuario

**Desventajas:**
- ❌ Dos pasos separados
- ❌ Fácil olvidar registrar recursos
- ❌ Costos incorrectos hasta que se edite

---

### Opción 2: Modal Completo (RECOMENDADO)

#### Flujo:
```
1. Usuario hace clic en "🌱 Sembrar"
   ├─ Modal con pestañas/secciones:
   │  ├─ Pestaña 1: Datos Básicos (cultivo, fecha, densidad)
   │  ├─ Pestaña 2: Insumos (semillas, fertilizantes)
   │  ├─ Pestaña 3: Maquinaria (tractor, sembradora)
   │  └─ Pestaña 4: Mano de Obra (operarios)
   └─ Se envía completo
   
2. Se crea Labor de Siembra CON recursos
   ├─ Estado: COMPLETADA
   ├─ Costo total: $X (calculado automáticamente)
   └─ Todos los recursos registrados
   
3. Usuario puede ver costos en tiempo real
   └─ Puede comparar rentabilidad cultivo vs cultivo
```

**Ventajas:**
- ✅ Todo en un solo paso
- ✅ Costos correctos desde el inicio
- ✅ Historial completo de recursos
- ✅ Mejor para análisis de rentabilidad

**Desventajas:**
- ⚠️ Modal más complejo
- ⚠️ Requiere más tiempo del usuario

---

### Opción 3: Modal Híbrido (EQUILIBRIO)

#### Flujo:
```
1. Usuario hace clic en "🌱 Sembrar"
   ├─ Modal con 2 pasos (wizard):
   │
   │  PASO 1: DATOS BÁSICOS (Obligatorio)
   │  ├─ Cultivo *
   │  ├─ Fecha de siembra *
   │  ├─ Densidad *
   │  └─ Botón: "Siguiente →"
   │
   │  PASO 2: RECURSOS (Opcional)
   │  ├─ "¿Quieres registrar los recursos ahora?"
   │  ├─ [Sí, agregar recursos] → Muestra formularios
   │  └─ [No, agregar después] → Envía con arrays vacíos
   │
   └─ Se envía según elección del usuario
```

**Ventajas:**
- ✅ Flexible: usuario elige
- ✅ Siembra rápida si tiene prisa
- ✅ Registro completo si quiere detalle
- ✅ No se pierde la funcionalidad

**Desventajas:**
- ⚠️ Requiere desarrollo del wizard
- ⚠️ Complejidad media

---

## 🎨 Diseño del Modal Completo (Opción 2)

### Vista Previa:

```
┌─────────────────────────────────────────────────────────┐
│  🌱 Sembrar Lote: A1 (25.5 ha)                    [✕]  │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  [1. Básico] [2. Insumos] [3. Maquinaria] [4. M.Obra] │
│  ▔▔▔▔▔▔▔▔▔▔                                            │
│                                                         │
│  Cultivo a Sembrar *                                    │
│  [▼ Soja                        ]                       │
│                                                         │
│  Fecha de Siembra *                                     │
│  [30/09/2025]                                           │
│                                                         │
│  Densidad de Siembra *                                  │
│  [50000] plantas/ha                                     │
│                                                         │
│  Observaciones                                          │
│  [Condiciones óptimas, suelo preparado]                │
│                                                         │
│  ┌───────────────────────────────────────────────┐    │
│  │ 💡 Consejo:                                   │    │
│  │ Registra los recursos para calcular costos   │    │
│  │ reales y analizar rentabilidad.               │    │
│  └───────────────────────────────────────────────┘    │
│                                                         │
│  [Cancelar]                          [Siguiente →]     │
└─────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────┐
│  🌱 Sembrar Lote: A1 (25.5 ha)                    [✕]  │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  [1. Básico] [2. Insumos] [3. Maquinaria] [4. M.Obra] │
│              ▔▔▔▔▔▔▔▔▔▔                                │
│                                                         │
│  Insumos Utilizados                                     │
│                                                         │
│  ┌─────────────────────────────────────────────┐      │
│  │ Semilla Soja DM 53i54                       │      │
│  │ Cantidad: 127.5 kg | Costo: $4,250,000     │      │
│  │ [✏️ Editar] [🗑️ Eliminar]                 │      │
│  └─────────────────────────────────────────────┘      │
│                                                         │
│  ┌─────────────────────────────────────────────┐      │
│  │ Fertilizante Fosfatado                      │      │
│  │ Cantidad: 50 kg | Costo: $500,000           │      │
│  │ [✏️ Editar] [🗑️ Eliminar]                 │      │
│  └─────────────────────────────────────────────┘      │
│                                                         │
│  [+ Agregar Insumo]                                    │
│                                                         │
│  Costo Total Insumos: $4,750,000                       │
│                                                         │
│  [← Anterior]                        [Siguiente →]     │
└─────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────┐
│  🌱 Sembrar Lote: A1 (25.5 ha)                    [✕]  │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  [1. Básico] [2. Insumos] [3. Maquinaria] [4. M.Obra] │
│                           ▔▔▔▔▔▔▔▔▔▔▔▔▔                │
│                                                         │
│  Maquinaria Utilizada                                   │
│                                                         │
│  ┌─────────────────────────────────────────────┐      │
│  │ Tractor John Deere 5075E                    │      │
│  │ Tipo: Propia | Costo: $360                  │      │
│  │ [✏️ Editar] [🗑️ Eliminar]                 │      │
│  └─────────────────────────────────────────────┘      │
│                                                         │
│  ┌─────────────────────────────────────────────┐      │
│  │ Sembradora de Precisión                     │      │
│  │ Tipo: Propia | Costo: $200                  │      │
│  │ [✏️ Editar] [🗑️ Eliminar]                 │      │
│  └─────────────────────────────────────────────┘      │
│                                                         │
│  [+ Agregar Maquinaria]                                │
│                                                         │
│  Costo Total Maquinaria: $560                          │
│                                                         │
│  [← Anterior]                        [Siguiente →]     │
└─────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────┐
│  🌱 Sembrar Lote: A1 (25.5 ha)                    [✕]  │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  [1. Básico] [2. Insumos] [3. Maquinaria] [4. M.Obra] │
│                                            ▔▔▔▔▔▔▔▔▔   │
│                                                         │
│  Mano de Obra                                           │
│                                                         │
│  ┌─────────────────────────────────────────────┐      │
│  │ Operador de tractor                         │      │
│  │ Personas: 1 | Horas: 8 | Costo: $200        │      │
│  │ [✏️ Editar] [🗑️ Eliminar]                 │      │
│  └─────────────────────────────────────────────┘      │
│                                                         │
│  [+ Agregar Mano de Obra]                              │
│                                                         │
│  Costo Total Mano de Obra: $200                        │
│                                                         │
│  ┌───────────────────────────────────────────────┐    │
│  │ 💰 RESUMEN DE COSTOS                          │    │
│  │                                                │    │
│  │ Insumos:      $4,750,000                      │    │
│  │ Maquinaria:   $560                            │    │
│  │ Mano de Obra: $200                            │    │
│  │ ─────────────────────────                     │    │
│  │ TOTAL:        $4,750,760                      │    │
│  │                                                │    │
│  │ Costo por ha: $186,304/ha                     │    │
│  └───────────────────────────────────────────────┘    │
│                                                         │
│  [← Anterior]  [Cancelar]    [🌱 Confirmar Siembra]   │
└─────────────────────────────────────────────────────────┘
```

---

## 📊 Comparación de Opciones

| Aspecto | Opción 1: Simple | Opción 2: Completo | Opción 3: Híbrido |
|---------|------------------|--------------------|--------------------|
| **Rapidez** | ⭐⭐⭐⭐⭐ | ⭐⭐ | ⭐⭐⭐⭐ |
| **Completitud** | ⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| **Facilidad uso** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐ |
| **Precisión costos** | ⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| **Flexibilidad** | ⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ |

---

## 🎯 RECOMENDACIÓN FINAL

### Implementar Opción 3: Modal Híbrido

**Razones:**
1. ✅ Permite siembra rápida si el usuario tiene prisa
2. ✅ Permite registro completo si quiere detalle
3. ✅ No pierde funcionalidad
4. ✅ Mejor experiencia de usuario
5. ✅ Compatible con el backend actual (ya soporta ambos casos)

### Mejoras Inmediatas:

1. **Mantener Modal Simple** (ya existe)
2. **Agregar botón "Agregar Recursos"** en el modal simple
3. **Si el usuario hace clic:**
   - Expande el modal para mostrar secciones de insumos/maquinaria/mano de obra
4. **Si el usuario NO hace clic:**
   - Envía con arrays vacíos (como ahora)
5. **Mensaje informativo:**
   - "💡 Puedes agregar recursos ahora o editarlos después en Labores"

---

## 🔍 DEBUG: ¿Por qué no ves los botones?

### Verificaciones:

1. **Abre:** `http://localhost:3000` en el navegador
2. **Abre:** DevTools (F12) → Pestaña "Console"
3. **Pega** este código:

```javascript
// Ver estados de tus lotes
const token = localStorage.getItem('token');
fetch('http://localhost:8080/api/v1/lotes', {
  headers: { 'Authorization': `Bearer ${token}` }
})
.then(r => r.json())
.then(lotes => {
  console.table(lotes.map(l => ({
    Nombre: l.nombre,
    Estado: l.estado,
    Cultivo: l.cultivoActual || '-',
    'Botón Sembrar': ['DISPONIBLE', 'PREPARADO', 'EN_PREPARACION'].includes(l.estado) ? '✅ SÍ' : '❌ NO',
    'Botón Cosechar': ['SEMBRADO', 'EN_CRECIMIENTO', 'EN_FLORACION', 'EN_FRUTIFICACION', 'LISTO_PARA_COSECHA'].includes(l.estado) ? '✅ SÍ' : '❌ NO'
  })));
});
```

4. **Revisa** la tabla en la consola

### Causas Probables:

- **Todos tus lotes están COSECHADOS** → No mostrarán botones (es correcto)
- **No tienes lotes creados** → Crea uno nuevo
- **Cambios no recargados** → Ctrl+Shift+R (recarga sin caché)

---

## ✅ Próximos Pasos

1. **Primero:** Diagnosticar por qué no ves los botones (usa el script de debug)
2. **Segundo:** Decidir qué opción de modal prefieres (Simple/Completo/Híbrido)
3. **Tercero:** Implementar mejoras al modal según tu elección

**¿Qué opción te parece mejor?** 🌱
