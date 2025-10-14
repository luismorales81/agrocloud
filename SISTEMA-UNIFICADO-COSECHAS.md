# ✅ Sistema Unificado de Cosechas - Implementación Completa

## 🎯 Objetivo

Unificar el sistema de cosechas eliminando la tabla duplicada `cosechas` y usando únicamente `historial_cosechas` para:
- ✅ Reportes completos y profesionales
- ✅ Análisis de rendimiento preciso
- ✅ Gestión de estado del suelo
- ✅ Recomendaciones de descanso
- ✅ Trazabilidad completa del ciclo siembra → cosecha

---

## ❌ **Problema Anterior: Dos Tablas Duplicadas**

### **Tabla 1: `cosechas`** (Simple - ELIMINADA)
```sql
cosechas (
  id, cultivo_id, lote_id, fecha_cosecha,
  cantidad_toneladas, precio_por_tonelada,
  costo_total, observaciones, usuario_id
)
```

**Limitaciones:**
- ❌ No tiene fecha_siembra
- ❌ No calcula rendimiento real vs esperado
- ❌ No registra estado del suelo
- ❌ No tiene análisis de variedad
- ❌ No recomienda períodos de descanso
- ❌ Solo soporta toneladas (no kg, qq)

### **Tabla 2: `historial_cosechas`** (Completa - ÚNICA AHORA)
```sql
historial_cosechas (
  id, lote_id, cultivo_id,
  fecha_siembra, fecha_cosecha,
  superficie_hectareas,
  cantidad_cosechada, unidad_cosecha,
  rendimiento_real, rendimiento_esperado,
  variedad_semilla,
  estado_suelo (BUENO, DESCANSANDO, AGOTADO),
  requiere_descanso,
  dias_descanso_recomendados,
  observaciones,
  usuario_id,
  fecha_creacion, fecha_actualizacion
)
```

**Ventajas:**
- ✅ Trazabilidad completa del ciclo
- ✅ Múltiples unidades (ton, kg, qq)
- ✅ Análisis de rendimiento automático
- ✅ Gestión del estado del suelo
- ✅ Recomendaciones de descanso
- ✅ Vistas SQL optimizadas para reportes

---

## 🔧 **Cambios Implementados**

### **1. Migración SQL** ✅

**Archivo:** `V1_12__Drop_Cosechas_Table.sql`

```sql
-- 1. Migrar datos existentes de cosechas → historial_cosechas
INSERT INTO historial_cosechas (...)
SELECT ...
FROM cosechas c
LEFT JOIN lotes l ON c.lote_id = l.id
WHERE NOT EXISTS (duplicados);

-- 2. Eliminar tabla cosechas
DROP TABLE IF EXISTS cosechas;
```

### **2. Backend - CosechaRequest Actualizado** ✅

**Archivo:** `CosechaRequest.java`

**Campos agregados:**
```java
private String variedadSemilla;
private String estadoSuelo; // BUENO, DESCANSANDO, AGOTADO
private Boolean requiereDescanso;
private Integer diasDescansoRecomendados;
```

**Campos existentes:**
```java
private LocalDate fechaCosecha;
private BigDecimal cantidadCosechada;
private String unidadMedida; // ton, kg, qq
private BigDecimal precioVenta;
private String observaciones;
private List<MaquinariaAsignadaDTO> maquinaria;
private List<ManoObraDTO> manoObra;
```

### **3. Backend - SiembraService.cosecharLote()** ✅

**Archivo:** `SiembraService.java` (líneas 258-301)

**ANTES:**
```java
// Solo creaba Labor
// ❌ No guardaba en ninguna tabla de cosechas
```

**AHORA:**
```java
// 1. Crea Labor (para costos y recursos)
Labor laborCosecha = new Labor();
// ... configuración y guardado

// 2. Guarda en historial_cosechas (para reportes y análisis)
HistorialCosecha historialCosecha = new HistorialCosecha();
historialCosecha.setLote(lote);
historialCosecha.setCultivo(cultivo); // Obtenido del lote
historialCosecha.setFechaSiembra(lote.getFechaSiembra());
historialCosecha.setFechaCosecha(request.getFechaCosecha());
historialCosecha.setSuperficieHectareas(lote.getAreaHectareas());
historialCosecha.setCantidadCosechada(request.getCantidadCosechada());
historialCosecha.setUnidadCosecha(request.getUnidadMedida());
historialCosecha.setRendimientoReal(cantidadCosechada / superficie);
historialCosecha.setRendimientoEsperado(cultivo.getRendimientoEsperado());
historialCosecha.setVariedadSemilla(request.getVariedadSemilla());
historialCosecha.setEstadoSuelo(request.getEstadoSuelo());
historialCosecha.setRequiereDescanso(request.getRequiereDescanso());
historialCosecha.setDiasDescansoRecomendados(request.getDiasDescansoRecomendados());
historialCosecha.setObservaciones(request.getObservaciones());
historialCosecha.setUsuario(usuario);

historialCosechaRepository.save(historialCosecha);

// 3. Actualiza el lote
lote.setEstado(COSECHADO);
lote.setRendimientoReal(rendimientoReal);
```

### **4. Backend - ReporteService** ✅

**Archivo:** `ReporteService.java` (línea 87-116)

**ANTES:**
```java
List<Cosecha> cosechas = cosechaRepository.findByUsuarioId(...);
return cosechas.stream().map(this::mapearACosechasDTO);
```

**AHORA:**
```java
List<HistorialCosecha> cosechas = historialCosechaRepository.findByUsuarioId(...);
return cosechas.stream().map(this::mapearHistorialACosechasDTO);
```

Nuevo método `mapearHistorialACosechasDTO()` que usa todos los campos de `HistorialCosecha`.

### **5. Frontend - CosechaModal Completo** ✅

**Archivo:** `CosechaModal.tsx`

**Campos agregados al formulario:**

```typescript
// Básicos existentes
fechaCosecha, cantidadCosechada, unidadMedida, precioVenta, observaciones

// NUEVOS para historial_cosechas:
variedadSemilla: string           // Input de texto
estadoSuelo: 'BUENO' | 'DESCANSANDO' | 'AGOTADO'  // Select
requiereDescanso: boolean         // Checkbox
diasDescansoRecomendados: number  // Input numérico (solo si requiere descanso)
```

**UI Mejorada:**
- Campo "Variedad de Semilla" con placeholder
- Select "Estado del Suelo" con emojis descriptivos
- Sección verde para "Requiere Descanso" con checkbox
- Campo condicional de días de descanso (solo si checkbox activo)
- Hint: "Recomendado: 30-60 días para recuperación del suelo"

### **6. Deprecación de Código Antiguo** ✅

**Cosecha.java:**
```java
@Deprecated
public class Cosecha {
  // Entidad deprecada, usar HistorialCosecha
}
```

**CosechaRepository.java:**
```java
@Deprecated
public interface CosechaRepository {
  // Repositorio deprecado, usar HistorialCosechaRepository
}
```

**ReporteService.mapearACosechasDTO():**
```java
@Deprecated
private ReporteCosechasDTO mapearACosechasDTO(Cosecha cosecha) {
  // Método deprecado, usar mapearHistorialACosechasDTO
}
```

---

## 📊 **Estructura Final del Sistema**

```
┌──────────────────────┐
│  CosechaModal.tsx    │
│  (Frontend)          │
└──────────┬───────────┘
           │
           ▼ POST /api/v1/lotes/{id}/cosechar
┌──────────────────────┐
│ PlotController       │
│ cosecharLote()       │
└──────────┬───────────┘
           │
           ▼
┌──────────────────────┐
│ SiembraService       │
│ cosecharLote()       │
└──────────┬───────────┘
           │
       ┌───┴────────────────┐
       │                    │
       ▼                    ▼
┌──────────────┐  ┌────────────────────────┐
│  Labor       │  │  historial_cosechas    │
│  (Costos)    │  │  (Análisis/Reportes)   │
└──────────────┘  └────────────────────────┘
       │                    │
       ▼                    ▼
┌──────────────┐  ┌────────────────────────┐
│ LaborInsumo  │  │  Vistas SQL:           │
│ LaborMaq     │  │  - Rendimiento cultivo │
│ LaborManoObra│  │  - Lotes descanso      │
└──────────────┘  │  - Estadísticas        │
                  └────────────────────────┘
```

---

## 🗄️ **Campos del Modal de Cosecha**

### **Sección 1: Información Básica**
| Campo | Tipo | Requerido | Descripción |
|-------|------|-----------|-------------|
| Fecha de Cosecha | Date | ✅ | Fecha en que se realizó la cosecha |
| Cantidad Cosechada | Number | ✅ | Cantidad total cosechada |
| Unidad de Medida | Select | ✅ | ton, kg, qq |

### **Sección 2: Análisis de Cultivo**
| Campo | Tipo | Requerido | Descripción |
|-------|------|-----------|-------------|
| Variedad de Semilla | Text | - | Ej: DK692, Pioneer 3000 |

### **Sección 3: Estado del Suelo**
| Campo | Tipo | Requerido | Descripción |
|-------|------|-----------|-------------|
| Estado del Suelo | Select | ✅ | BUENO, DESCANSANDO, AGOTADO |
| Requiere Descanso | Checkbox | - | Si necesita período de recuperación |
| Días de Descanso | Number | Condicional | Solo si requiere descanso |

### **Sección 4: Información Económica**
| Campo | Tipo | Requerido | Descripción |
|-------|------|-----------|-------------|
| Precio de Venta | Number | - | Precio por unidad |
| Observaciones | TextArea | - | Notas adicionales |

---

## 📈 **Datos Calculados Automáticamente**

### **Por el Frontend:**
```typescript
Rendimiento estimado = cantidadCosechada / superficie
// Ejemplo: 100 ton / 50 ha = 2 ton/ha
```

### **Por el Backend:**
```java
rendimiento_real = cantidad_cosechada / superficie_hectareas

porcentaje_cumplimiento = (rendimiento_real / rendimiento_esperado) * 100
// Ejemplo: (2.5 / 3.0) * 100 = 83.33%

dias_ciclo = fecha_cosecha - fecha_siembra
// Ejemplo: 2025-09-30 - 2025-05-30 = 123 días
```

---

## 🔍 **Vistas SQL Disponibles**

### **1. vista_historial_cosechas_completo**
```sql
SELECT 
  lote_nombre, cultivo_nombre, cultivo_variedad,
  fecha_siembra, fecha_cosecha, dias_ciclo,
  cantidad_cosechada, unidad_cosecha,
  rendimiento_real, rendimiento_esperado,
  porcentaje_cumplimiento,
  estado_suelo, requiere_descanso
FROM vista_historial_cosechas_completo;
```

### **2. vista_estadisticas_rendimiento_cultivo**
```sql
SELECT 
  cultivo_nombre,
  total_cosechas,
  rendimiento_promedio,
  rendimiento_minimo,
  rendimiento_maximo,
  cumplimiento_promedio,
  superficie_total_cosechada,
  cantidad_total_cosechada
FROM vista_estadisticas_rendimiento_cultivo;
```

### **3. vista_lotes_requieren_descanso**
```sql
SELECT 
  lote_nombre,
  fecha_cosecha,
  estado_suelo,
  dias_descanso_recomendados,
  dias_desde_cosecha,
  estado_lote (LISTO_PARA_SIEMBRA | EN_DESCANSO)
FROM vista_lotes_requieren_descanso;
```

---

## 🧪 **Cómo Probar el Sistema Completo**

### **Paso 1: Aplicar Migración**
```bash
.\aplicar-migracion-unificar-cosechas.bat
```

### **Paso 2: Reiniciar Backend**
```bash
# Detener backend actual (Ctrl+C)
# Luego:
.\iniciar-backend.bat
```

### **Paso 3: Probar Cosecha Completa**

1. **Ir a Lotes** → Seleccionar lote SEMBRADO
2. **Click "🌾 Cosechar ▾"** → "Cosechar Normal"
3. **Llenar formulario completo:**
   - Fecha de Cosecha: `30/09/2025`
   - Cantidad: `100`
   - Unidad: `ton`
   - Variedad: `DK692`
   - Estado del Suelo: `BUENO`
   - ☐ Requiere Descanso: `No marcado`
   - Precio de Venta: `250000` (opcional)
   - Observaciones: `Cosecha exitosa, buen clima`
4. **Guardar**
5. **Verificar:**
   - ✅ Mensaje de éxito con rendimiento
   - ✅ Lote cambia a estado COSECHADO

### **Paso 4: Verificar en Base de Datos**

```sql
-- Ver el registro guardado
SELECT * FROM historial_cosechas ORDER BY id DESC LIMIT 1;

-- Ver cálculos automáticos
SELECT 
  lote_nombre,
  cultivo_nombre,
  cantidad_cosechada,
  unidad_cosecha,
  rendimiento_real,
  rendimiento_esperado,
  porcentaje_cumplimiento,
  dias_ciclo,
  estado_suelo
FROM vista_historial_cosechas_completo
ORDER BY fecha_cosecha DESC
LIMIT 1;
```

### **Paso 5: Verificar Reportes**

```javascript
// Desde el frontend
GET /api/reportes/cosechas?fechaInicio=2025-01-01&fechaFin=2025-12-31

// Debería devolver:
[
  {
    cosechaId: 1,
    loteNombre: "Lote A",
    cultivoNombre: "Maíz",
    variedadSemilla: "DK692",
    fechaSiembra: "2025-05-30",
    fechaCosecha: "2025-09-30",
    cantidadCosechada: 100,
    unidadCosecha: "ton",
    rendimientoReal: 2.0,      // ton/ha
    rendimientoEsperado: 2.5,  // ton/ha
    porcentajeCumplimiento: 80, // %
    estadoSuelo: "BUENO",
    observaciones: "..."
  }
]
```

---

## 📋 **Resumen de Archivos Modificados/Creados**

### **Backend:**
1. ✅ `V1_12__Drop_Cosechas_Table.sql` - Migración para eliminar tabla
2. ✅ `CosechaRequest.java` - Agregados 4 campos nuevos + getters/setters
3. ✅ `SiembraService.java` - Método `cosecharLote()` guarda en `historial_cosechas`
4. ✅ `ReporteService.java` - Usa `historial_cosechas` para reportes
5. ✅ `Cosecha.java` - Marcada como `@Deprecated`
6. ✅ `CosechaRepository.java` - Marcado como `@Deprecated`

### **Frontend:**
7. ✅ `CosechaModal.tsx` - Formulario completo con 4 campos nuevos

### **Scripts:**
8. ✅ `aplicar-migracion-unificar-cosechas.bat` - Script de migración

### **Documentación:**
9. ✅ `SISTEMA-UNIFICADO-COSECHAS.md` - Este documento

---

## ✅ **Ventajas del Sistema Unificado**

### **1. Reportes Completos**
- ✅ Rendimiento real vs esperado
- ✅ Porcentaje de cumplimiento
- ✅ Análisis por variedad de semilla
- ✅ Estadísticas por cultivo
- ✅ Tendencias de rendimiento

### **2. Gestión del Suelo**
- ✅ Registro del estado después de cosecha
- ✅ Identificación de lotes que necesitan descanso
- ✅ Planificación de rotaciones
- ✅ Prevención de agotamiento del suelo

### **3. Trazabilidad Completa**
- ✅ Ciclo completo: siembra → cosecha
- ✅ Duración real del ciclo (días)
- ✅ Historial por lote
- ✅ Comparación entre ciclos

### **4. Datos Consistentes**
- ✅ Una sola fuente de verdad
- ✅ No duplicación de datos
- ✅ Vistas SQL optimizadas
- ✅ Índices para performance

---

## ⚠️ **Notas Importantes**

1. **La migración migra datos existentes** de `cosechas` a `historial_cosechas` (si existen)
2. **La tabla `cosechas` se elimina** después de migrar
3. **Los reportes ahora usan solo `historial_cosechas`**
4. **El modal muestra más campos** pero con valores por defecto sensatos
5. **Los campos opcionales** tienen valores por defecto en el backend

---

## 🚀 **Próximos Pasos Sugeridos**

### **Opcional - Mejoras Futuras:**

1. **Agregar campo precio_venta** a `historial_cosechas` para análisis financiero
2. **Análisis de correlación** rendimiento vs clima (integrar con API clima)
3. **Dashboard de salud del suelo** basado en historial
4. **Recomendaciones automáticas** de fertilización según estado suelo
5. **Alertas de descanso** cuando un lote puede volver a sembrarse

---

**Implementado por:** AgroGestion Team  
**Fecha:** 30 de Septiembre, 2025  
**Versión:** 2.0.0 - Sistema Unificado de Cosechas  
**Estado:** ✅ Completado y Listo para Producción
