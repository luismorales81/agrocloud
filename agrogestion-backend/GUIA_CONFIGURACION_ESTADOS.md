# 📋 Guía: Sistema de Configuración de Estados y Tareas

## 🎯 ¿Qué es y para qué sirve?

Este sistema permite **personalizar completamente** los estados de los lotes y las tareas disponibles para cada tipo de cultivo. En lugar de tener estados fijos en el código, ahora puedes:

- ✅ Definir tus propios estados (ej: "R3", "R6" para soja)
- ✅ Configurar qué tareas se pueden hacer en cada estado
- ✅ Definir el orden y flujo de los estados
- ✅ Personalizar por tipo de cultivo (Soja, Maíz, Trigo, etc.)
- ✅ Tener plantillas globales o personalizaciones por empresa

---

## 🏗️ Arquitectura del Sistema

### 1. **Tipos de Cultivo** (`cultivo_tipos_cultivo`)
Catálogo de cultivos disponibles:
- **Soja** (plantilla global)
- **Maíz** (plantilla global)
- **Trigo** (plantilla global)
- **Girasol** (plantilla global)
- **Sorgo** (plantilla global)
- Y cualquier otro que agregues

### 2. **Estados del Lote** (`cultivo_estados_lote`)
Estados personalizables para cada tipo de cultivo:

**Ejemplo para Soja:**
1. 🟢 **Disponible** (Estado inicial)
2. 🟡 **Preparado**
3. 🔵 **Sembrado**
4. 🌱 **Emergencia**
5. 🌾 **R3** (Inicio de llenado de granos)
6. 🌽 **R6** (Llenado completo)
7. 📦 **Listo para Cosecha**
8. ✅ **Cosechado** (Estado final)

Cada estado tiene:
- **Nombre**: Identificador del estado
- **Descripción**: Explicación del estado
- **Color**: Color para la UI (ej: `#10b981`)
- **Icono**: Emoji o icono (ej: `🟢`)
- **Orden**: Posición en el flujo
- **Es Estado Inicial**: Si es el primer estado del ciclo
- **Es Estado Final**: Si es el último estado del ciclo

### 3. **Transiciones** (`cultivo_transiciones_estado`)
Define qué cambios de estado son válidos:

**Ejemplo:**
- ✅ `Disponible` → `Preparado` ✓
- ✅ `Preparado` → `Sembrado` ✓
- ✅ `Sembrado` → `Emergencia` ✓
- ❌ `Sembrado` → `Cosechado` ✗ (no permitido)

Cada transición puede:
- **Requerir motivo**: Si el cambio necesita justificación

### 4. **Tareas por Estado** (`cultivo_tareas_por_estado`)
Define qué labores/tareas se pueden realizar en cada estado:

**Ejemplo para estado "Sembrado":**
- 💧 **Riego** (tipo: `RIEGO`)
- 🌿 **Fertilización** (tipo: `FERTILIZACION`)

**Ejemplo para estado "Emergencia":**
- 💧 **Riego** (tipo: `RIEGO`)
- 🌾 **Control de Malezas** (tipo: `CONTROL_MALEZAS`)

Cada tarea tiene:
- **Tipo de Labor**: Código del tipo (ej: `SIEMBRA`, `RIEGO`, `FERTILIZACION`)
- **Nombre**: Nombre amigable (ej: "Riego", "Fertilización")
- **Descripción**: Explicación de la tarea
- **Es Obligatoria**: Si la tarea debe realizarse en ese estado
- **Orden**: Orden de visualización

---

## 🔄 Cómo Funciona

### Flujo de Trabajo

1. **Seleccionar Tipo de Cultivo**
   - El usuario elige el tipo de cultivo (ej: "Soja")

2. **Ver Estados Configurados**
   - El sistema muestra los estados definidos para ese tipo de cultivo
   - Si hay personalización por empresa, usa esa; si no, usa la plantilla global

3. **Cambiar Estado del Lote**
   - Si el lote usa **estados configurados**, valida contra `cultivo_transiciones_estado` (plantilla o empresa)
   - Si requiere motivo (`requiereMotivo`), solicita justificación
   - Estados **Sembrado** y **Cosechado** son derivados por siembra/cosecha (no manual)
   - Sin configuración, usa la matriz legacy del enum `EstadoLote`

4. **Ver Tareas Disponibles**
   - Al seleccionar un estado, muestra las tareas permitidas
   - Las tareas **obligatorias** deben completarse para avanzar al siguiente estado (si hay transición)
   - El usuario puede crear labores solo de los tipos permitidos

5. **Camino del lote (Labores)**
   - Al elegir un lote aparece el panel con estados, próximo paso, tareas pendientes y mensaje de avance
   - API: `GET /api/estados-lotes/lote/{id}/progreso`

6. **Vista guiada (Configuración)**
   - Tab **Vista guiada**: diagrama del ciclo, validación automática y asistente paso a paso
   - API validación: `GET /api/v1/configuracion-estados/validacion-completa`

7. **Recálculo diario automático**
   - Job a las **01:00** recalcula estados por tiempo/tareas en lotes activos con cultivo
   - Manual: `POST /api/estados-lotes/recalcular-todos` o botón en Vista guiada

### Campos nuevos por estado

| Campo | Uso |
|-------|-----|
| `dias_minimos` | Días desde siembra para alcanzar el estado (avance por tiempo) |
| `modo_avance` | EVENTO, TIEMPO, TAREAS o MIXTO (orientación en UI) |

---

## 🎨 Plantillas vs Personalizaciones

### Plantillas Globales
- Son configurables por **tipo de cultivo**
- Disponibles para todas las empresas
- No se pueden modificar directamente
- Se pueden **copiar** a una empresa para personalizar

### Personalizaciones por Empresa
- Son específicas de una **empresa**
- Se crean copiando una plantilla
- Se pueden modificar libremente
- Tienen prioridad sobre las plantillas

**Ejemplo:**
1. Empresa "AgroXYZ" usa la plantilla de "Soja" (8 estados)
2. Copia la plantilla a su empresa
3. Agrega un estado "R4" entre R3 y R6
4. Ahora tiene 9 estados personalizados
5. Los lotes de esa empresa usan los 9 estados

---

## 📱 Interfaz de Usuario

### Pantalla de Configuración (`/cultivos/configuracion`)

**Tabs principales:**

1. **📋 Estados**
   - Lista de estados con orden
   - Botones para subir/bajar orden
   - Crear, editar, eliminar estados
   - Configurar color e icono

2. **🔄 Transiciones**
   - Lista de transiciones permitidas
   - Crear nuevas transiciones
   - Eliminar transiciones

3. **✅ Tareas**
   - Agrupadas por estado
   - Ver tareas disponibles en cada estado
   - Crear, editar, eliminar tareas
   - Marcar tareas como obligatorias

---

## 💡 Ejemplos Prácticos

### Ejemplo 1: Configurar Estados para Maíz

1. Ir a **Configuración** → Seleccionar "Maíz"
2. En tab **Estados**, hacer clic en **"+ Agregar Estado"**
3. Crear estados:
   - "V2" (2 hojas)
   - "V6" (6 hojas)
   - "VT" (Inicio de espigado)
   - "R1" (Inicio de llenado)
   - etc.

### Ejemplo 2: Agregar Tarea Obligatoria

1. Seleccionar estado "Sembrado"
2. En tab **Tareas**, hacer clic en **"+ Agregar Tarea"**
3. Configurar:
   - Tipo: `FERTILIZACION`
   - Nombre: "Fertilización de Arranque"
   - Marcar como **Obligatoria**
4. Guardar

Ahora, cuando un lote esté en estado "Sembrado", el sistema mostrará que la fertilización de arranque es obligatoria.

### Ejemplo 3: Personalizar para tu Empresa

1. Seleccionar "Soja"
2. Hacer clic en **"📋 Copiar Plantilla"**
3. Esto crea una copia personalizable
4. Ahora puedes:
   - Agregar estados adicionales
   - Modificar transiciones
   - Agregar/quitar tareas
   - Cambiar colores e iconos

---

## 🔧 Integración con el Sistema

### En el Módulo de Lotes

Cuando un lote está en un estado configurado:
- El sistema muestra el **nombre, color e icono** del estado configurado
- Si no hay estado configurado, usa el enum tradicional como fallback

### En el Módulo de Labores

Al crear una labor:
- El sistema valida que el **tipo de labor** esté permitido en el estado actual
- Muestra solo las tareas disponibles para ese estado
- Destaca las tareas obligatorias

### Validación de Transiciones

Al cambiar el estado de un lote:
- El sistema verifica que la transición esté permitida
- Si no está permitida, muestra un error
- Si requiere motivo, solicita justificación

---

## 🚀 Ventajas del Sistema

1. **Flexibilidad Total**
   - Adapta el sistema a cualquier tipo de cultivo
   - No necesitas modificar código

2. **Multi-tenant**
   - Cada empresa puede tener su configuración
   - Plantillas globales para empezar rápido

3. **Escalable**
   - Agrega nuevos tipos de cultivo fácilmente
   - Agrega estados y tareas sin límites

4. **User-Friendly**
   - Interfaz visual e intuitiva
   - No requiere conocimientos técnicos

5. **Mantenible**
   - Cambios en la configuración sin reiniciar
   - Historial de cambios automático

---

## 📊 Estructura de Datos

```
TipoCultivo (Soja)
  └── Estados (8 estados)
      ├── Disponible
      │   └── Tareas: Arado, Rastra
      ├── Preparado
      │   └── Tareas: Siembra (obligatoria)
      ├── Sembrado
      │   └── Tareas: Riego, Fertilización
      ├── Emergencia
      │   └── Tareas: Riego, Control de Malezas
      ├── R3
      │   └── Tareas: Fertilización, Riego
      ├── R6
      │   └── Tareas: Riego, Control de Plagas
      ├── Listo para Cosecha
      │   └── Tareas: Cosecha (obligatoria)
      └── Cosechado
          └── Tareas: Arado
      
  └── Transiciones (8 transiciones)
      ├── Disponible → Preparado
      ├── Preparado → Sembrado
      ├── Sembrado → Emergencia
      ├── Emergencia → R3
      ├── R3 → R6
      ├── R6 → Listo para Cosecha
      ├── Listo para Cosecha → Cosechado
      └── Cosechado → Disponible
```

---

## 🎓 Casos de Uso

### Caso 1: Cultivo Orgánico
- Agregar estados específicos para certificación
- Configurar tareas permitidas (sin agroquímicos)
- Validar transiciones según normativas

### Caso 2: Invernadero
- Estados diferentes (Preparación, Siembra, Crecimiento, Cosecha)
- Tareas específicas (Control de temperatura, Riego por goteo)
- Ciclos más cortos

### Caso 3: Rotación de Cultivos
- Estados compartidos entre cultivos
- Transiciones que permiten cambiar de cultivo
- Tareas adaptadas a cada etapa

---

## 🔍 Endpoints API

### Tipos de Cultivo
- `GET /api/v1/configuracion-estados/tipos-cultivo` - Listar todos
- `GET /api/v1/configuracion-estados/tipos-cultivo/plantillas` - Solo plantillas
- `POST /api/v1/configuracion-estados/tipos-cultivo` - Crear nuevo

### Estados
- `GET /api/v1/configuracion-estados/estados?tipoCultivoId=X` - Listar estados
- `POST /api/v1/configuracion-estados/estados` - Crear estado
- `PUT /api/v1/configuracion-estados/estados/{id}` - Actualizar estado
- `PUT /api/v1/configuracion-estados/estados/reordenar` - Reordenar estados

### Transiciones
- `GET /api/v1/configuracion-estados/transiciones?tipoCultivoId=X` - Listar transiciones
- `POST /api/v1/configuracion-estados/transiciones` - Crear transición
- `GET /api/v1/configuracion-estados/transiciones/validar` - Validar transición

### Tareas
- `GET /api/v1/configuracion-estados/tareas?estadoId=X` - Listar tareas
- `POST /api/v1/configuracion-estados/tareas` - Crear tarea
- `PUT /api/v1/configuracion-estados/tareas/{id}` - Actualizar tarea

---

## ⚠️ Notas Importantes

1. **Compatibilidad**: El sistema mantiene compatibilidad con el enum `EstadoLote` tradicional. Los lotes existentes seguirán funcionando.

2. **Prioridad**: Si un lote tiene `estadoConfigurado`, se usa ese; si no, se usa el enum.

3. **Plantillas**: Las plantillas globales no se pueden eliminar, solo las personalizaciones.

4. **Validaciones**: 
   - No se puede eliminar un estado que está en uso
   - No se pueden crear transiciones circulares
   - Las tareas deben tener un tipo de labor válido

5. **Performance**: Las consultas están optimizadas con índices para búsquedas rápidas.

---

## 🎯 Próximos Pasos

1. **Usar en Lotes**: Actualizar el módulo de lotes para usar estados configurados
2. **Validar en Labores**: Integrar validación de tareas permitidas
3. **Reportes**: Generar reportes basados en estados configurados
4. **Notificaciones**: Alertas cuando faltan tareas obligatorias

---

¿Tienes preguntas? ¡Consulta la documentación o contacta al equipo de desarrollo!













